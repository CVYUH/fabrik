package com.cvyuh.resources.keys;

import com.cvyuh.resources.Constants;
import com.cvyuh.service.am.AMPaths;
import com.cvyuh.service.am.AMService;
import com.cvyuh.service.am.AMShardUtils;
import com.cvyuh.service.provision.ProvisionPaths;
import com.cvyuh.service.provision.ProvisionService;
import com.cvyuh.utils.log.LoggingContext;
import com.cvyuh.utils.misc.Json;
import com.cvyuh.utils.response.HttpMethod;
import com.cvyuh.utils.response.ResponseHandler;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import java.util.List;

/**
 * Aggregated key view — collects keys from Vault (via Provision) and each AM shard,
 * merges into a unified comparison response.

 * GET  /api/v0/keys                       — consolidated view (all tenants, all shards)
 * GET  /api/v0/keys/{tenant}              — single tenant comparison
 * POST /api/v0/keys/{tenant}/{purpose}    — generate key (Vault)
 * DELETE /api/v0/keys/{tenant}/{purpose}  — delete key (Vault)
 * POST /api/v0/keys/{tenant}/{purpose}/import — push from Vault to AM shards
 */
@Path(Constants.V0.KEYS)
public class KeysResource implements ResponseHandler {

    private static final Logger LOG = Logger.getLogger(KeysResource.class);

    @Inject @RestClient AMService amService;
    @Inject @RestClient ProvisionService provisionService;

    private void preProcess() {
        MDC.put(LoggingContext.SERVICE, KeysResource.class.getName());
    }

    // ======================== Endpoints ========================

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response compareAll(@Context HttpHeaders httpHeaders) {
        preProcess();
        return handleResponse(v -> doCompareAll(httpHeaders), "keys", HttpMethod.GET, null);
    }

    @GET
    @Path("/{tenant}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response compare(@PathParam("tenant") String tenant, @Context HttpHeaders httpHeaders) {
        preProcess();
        return handleResponse(v -> doCompare(tenant, httpHeaders), "keys/" + tenant, HttpMethod.GET, null);
    }

    @POST
    @Path("/{tenant}/{purpose}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generate(@PathParam("tenant") String tenant, @PathParam("purpose") String purpose, String body) {
        preProcess();
        return handleResponse(
                v -> provisionService.doPost(ProvisionPaths.tenantKey(tenant, purpose), body),
                "keys/" + tenant + "/" + purpose, HttpMethod.POST, null);
    }

    @DELETE
    @Path("/{tenant}/{purpose}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("tenant") String tenant, @PathParam("purpose") String purpose) {
        preProcess();
        return handleResponse(
                v -> provisionService.doDelete(ProvisionPaths.tenantKey(tenant, purpose)),
                "keys/" + tenant + "/" + purpose, HttpMethod.DELETE, null);
    }

    /**
     * Import key from Vault to all AM shards.
     * Reads key from Vault (via Provision), pushes to each shard via replaceKey.
     */
    @POST
    @Path("/{tenant}/{purpose}/import")
    @Produces(MediaType.APPLICATION_JSON)
    public Response importToAM(@PathParam("tenant") String tenant, @PathParam("purpose") String purpose,
                               @Context HttpHeaders httpHeaders) {
        preProcess();
        return handleResponse(v -> doImportToAM(tenant, purpose, httpHeaders), "keys/" + tenant + "/" + purpose + "/import", HttpMethod.POST, null);
    }

    /**
     * Renew a realm key: generate new → update Vault → replace on all AM shards.
     */
    @POST
    @Path("/{tenant}/{purpose}/renew")
    @Produces(MediaType.APPLICATION_JSON)
    public Response renewRealmKey(@PathParam("tenant") String tenant, @PathParam("purpose") String purpose,
                                  String body, @Context HttpHeaders httpHeaders) {
        preProcess();
        return handleResponse(v -> doRenewRealmKey(tenant, purpose, body, httpHeaders), "keys/" + tenant + "/" + purpose + "/renew", HttpMethod.POST, null);
    }

    /**
     * Renew a stock key (realm=ALL): generate new → replace on all AM shards. No Vault.
     */
    @POST
    @Path("/renew/{alias}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response renewStockKey(@PathParam("alias") String alias, String body, @Context HttpHeaders httpHeaders) {
        preProcess();
        return handleResponse(v -> doRenewStockKey(alias, body, httpHeaders), "keys/renew/" + alias, HttpMethod.POST, null);
    }

    // ======================== Core Logic ========================

    private Response doCompareAll(HttpHeaders httpHeaders) {
        MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();

        // 1) Fetch realms to know tenant names
        MultivaluedMap<String, String> realmQuery = new MultivaluedHashMap<>();
        realmQuery.putSingle("_queryFilter", "true");
        Response realmsResp = amService.doGet(AMPaths.REALMS, headers, realmQuery);

        java.util.List<String> tenants = new java.util.ArrayList<>();
        tenants.add("root"); // root realm always included
        if (realmsResp.getStatus() == 200) {
            ObjectNode realmsBody = Json.parseObject(realmsResp.readEntity(String.class));
            ArrayNode realmsResult = (ArrayNode) realmsBody.get("result");
            if (realmsResult != null) {
                for (var realm : realmsResult) {
                    String name = realm.has("name") ? realm.get("name").asText() : "";
                    if (!name.isEmpty() && !"/".equals(name)) {
                        tenants.add(name);
                    }
                }
            }
        }

        // 2) Fetch AM keys per shard
        java.util.Map<String, ArrayNode> amKeysPerShard = new java.util.LinkedHashMap<>();
        for (String shardHost : Constants.amShardHosts()) {
            amKeysPerShard.put(shardHost, fetchAllAMKeys(shardHost, headers));
        }

        // 3) Union of all aliases across shards — metadata from first shard that has it
        java.util.Map<String, ObjectNode> aliasMetadata = new java.util.LinkedHashMap<>();
        for (var shardKeys : amKeysPerShard.values()) {
            for (var key : shardKeys) {
                String alias = key.has("alias") ? key.get("alias").asText() : "";
                if (!alias.isEmpty() && !aliasMetadata.containsKey(alias)) {
                    aliasMetadata.put(alias, (ObjectNode) key);
                }
            }
        }

        // 4) Fetch Vault keys per tenant
        java.util.Map<String, java.util.Set<String>> vaultKeysPerTenant = new java.util.LinkedHashMap<>();
        for (String tenant : tenants) {
            vaultKeysPerTenant.put(tenant, fetchVaultAliases(tenant));
        }

        // 5) Build rows — one per alias
        ArrayNode rows = Json.MAPPER.createArrayNode();

        for (var entry : aliasMetadata.entrySet()) {
            String alias = entry.getKey();
            ObjectNode meta = entry.getValue();
            rows.add(buildRow(alias, meta, tenants, vaultKeysPerTenant, amKeysPerShard));
        }

        // 6) Vault keys not in AM (missing-am)
        for (String tenant : tenants) {
            java.util.Set<String> vaultPurposes = vaultKeysPerTenant.getOrDefault(tenant, java.util.Set.of());
            for (String purpose : vaultPurposes) {
                String alias = tenant + "-" + purpose;
                if (aliasMetadata.containsKey(alias)) continue;

                ObjectNode row = Json.MAPPER.createObjectNode();
                row.put("alias", alias);
                row.put("realm", tenant);
                row.put("purpose", purpose);
                row.put("isKeyEntry", false);
                row.put("subject", "");
                row.put("notAfter", "");
                row.put("inVault", true);

                ObjectNode shardStatus = Json.MAPPER.createObjectNode();
                for (String shard : Constants.amShardHosts()) shardStatus.put(shard, false);
                row.set("amShards", shardStatus);
                row.put("status", "missing-am");

                rows.add(row);
            }
        }

        ObjectNode result = Json.MAPPER.createObjectNode();
        result.set("tenants", Json.MAPPER.valueToTree(tenants));
        result.set("shards", Json.MAPPER.valueToTree(Constants.amShardHosts()));
        result.set("keys", rows);
        return Response.ok(result.toString()).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Import from Vault to AM shards — reads key from Vault via Provision, pushes to each shard.
     */
    private Response doImportToAM(String tenant, String purpose, HttpHeaders httpHeaders) {
        // Read key from Provision (which reads from Vault)
        Response vaultResp = provisionService.doGet(ProvisionPaths.tenantKey(tenant, purpose));
        if (vaultResp.getStatus() != 200) {
            return Response.status(vaultResp.getStatus()).entity("{\"error\":\"Key not found in Vault\"}").type(MediaType.APPLICATION_JSON).build();
        }

        ObjectNode vaultBody = Json.parseObject(vaultResp.readEntity(String.class));
        ObjectNode keyData = vaultBody.path("data").path("data").deepCopy();
        if (keyData == null || !keyData.has("privateKeyBase64")) {
            return Response.status(404).entity("{\"error\":\"Key data not found in Vault response\"}").type(MediaType.APPLICATION_JSON).build();
        }

        String alias = tenant + "-" + purpose;
        return replaceKeyOnAllShards(alias, keyData, httpHeaders);
    }

    /**
     * Renew realm key: generate via Provision → Vault, then replace on AM shards.
     */
    private Response doRenewRealmKey(String tenant, String purpose, String body, HttpHeaders httpHeaders) {
        // Generate + store in Vault via Provision
        Response genResp = provisionService.doPost(ProvisionPaths.tenantKey(tenant, purpose), body != null ? body : "{}");
        if (genResp.getStatus() >= 300) {
            return Response.status(genResp.getStatus()).entity(genResp.readEntity(String.class)).type(MediaType.APPLICATION_JSON).build();
        }

        // Now import from Vault to AM
        return doImportToAM(tenant, purpose, httpHeaders);
    }

    /**
     * Renew stock key (no Vault): generate locally via Provision, replace on AM shards.
     */
    private Response doRenewStockKey(String alias, String body, HttpHeaders httpHeaders) {
        // Generate a key via Provision's stock key endpoint (no Vault storage)
        Response genResp = provisionService.doPost(ProvisionPaths.stockKey(alias), body != null ? body : "{}");
        if (genResp.getStatus() >= 300) {
            return Response.status(genResp.getStatus()).entity(genResp.readEntity(String.class)).type(MediaType.APPLICATION_JSON).build();
        }

        // Parse the generated key data from Provision response
        ObjectNode genBody = Json.parseObject(genResp.readEntity(String.class));
        if (genBody == null || !genBody.has("privateKeyBase64")) {
            return Response.status(500).entity("{\"error\":\"Provision did not return key data\"}").type(MediaType.APPLICATION_JSON).build();
        }

        return replaceKeyOnAllShards(alias, genBody, httpHeaders);
    }

    /**
     * Replace a key on all AM shards via replaceKey action.
     */
    private Response replaceKeyOnAllShards(String alias, ObjectNode keyData, HttpHeaders httpHeaders) {
        MultivaluedMap<String, String> incomingHeaders = httpHeaders.getRequestHeaders();
        ArrayNode results = Json.MAPPER.createArrayNode();

        ObjectNode replaceBody = Json.MAPPER.createObjectNode();
        replaceBody.put("alias", alias);
        replaceBody.put("privateKeyBase64", keyData.get("privateKeyBase64").asText());
        replaceBody.put("certificateBase64", keyData.get("certificateBase64").asText());

        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        query.putSingle("_action", "replaceKey");

        for (String shardHost : Constants.amShardHosts()) {
            MultivaluedMap<String, String> headers = AMShardUtils.shardHeaders(shardHost, incomingHeaders);
            Response r = amService.doPost(AMPaths.KEYSTORE, headers, query, replaceBody.toString());

            ObjectNode shardResult = Json.MAPPER.createObjectNode();
            shardResult.put("shard", shardHost);
            shardResult.put("status", r.getStatus());
            shardResult.put("body", r.readEntity(String.class));
            results.add(shardResult);
        }

        ObjectNode result = Json.MAPPER.createObjectNode();
        result.put("alias", alias);
        result.set("shards", results);
        return Response.ok(result.toString()).type(MediaType.APPLICATION_JSON).build();
    }

    private Response doCompare(String tenant, HttpHeaders httpHeaders) {
        MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();

        ObjectNode result = Json.MAPPER.createObjectNode();
        result.put("tenant", tenant);

        ObjectNode vaultSection = fetchVaultKeys(tenant);
        result.set("vault", vaultSection);

        ArrayNode amShards = Json.MAPPER.createArrayNode();
        for (String shardHost : Constants.amShardHosts()) {
            amShards.add(fetchAMKeys(tenant, shardHost, headers));
        }
        result.set("am", amShards);

        result.set("comparison", buildComparison(tenant, vaultSection, amShards));
        return Response.ok(result.toString()).type(MediaType.APPLICATION_JSON).build();
    }

    // ======================== Helpers ========================

    private ObjectNode buildRow(String alias, ObjectNode meta, java.util.List<String> tenants,
                                java.util.Map<String, java.util.Set<String>> vaultKeysPerTenant,
                                java.util.Map<String, ArrayNode> amKeysPerShard) {
        ObjectNode row = Json.MAPPER.createObjectNode();
        row.put("alias", alias);

        String realm = "ALL";
        String purpose = alias;
        for (String tenant : tenants) {
            if (alias.startsWith(tenant + "-")) {
                realm = tenant;
                purpose = alias.substring(tenant.length() + 1);
                break;
            }
        }
        row.put("realm", realm);
        row.put("purpose", purpose);
        row.put("isKeyEntry", meta.has("isKeyEntry") && meta.get("isKeyEntry").asBoolean());
        row.put("subject", meta.has("subject") ? meta.get("subject").asText() : "");
        row.put("notAfter", meta.has("notAfter") ? meta.get("notAfter").asText() : "");

        // Per-shard presence
        ObjectNode shardStatus = Json.MAPPER.createObjectNode();
        for (var shardEntry : amKeysPerShard.entrySet()) {
            boolean found = false;
            for (var key : shardEntry.getValue()) {
                if (key.has("alias") && alias.equals(key.get("alias").asText())) { found = true; break; }
            }
            shardStatus.put(shardEntry.getKey(), found);
        }
        row.set("amShards", shardStatus);

        // Vault status
        boolean inVault = false;
        if (!"ALL".equals(realm)) {
            java.util.Set<String> vaultAliases = vaultKeysPerTenant.getOrDefault(realm, java.util.Set.of());
            inVault = vaultAliases.contains(purpose);
        }
        row.put("inVault", inVault);

        // Status
        boolean inAllShards = amKeysPerShard.values().stream().allMatch(keys -> {
            for (var k : keys) { if (k.has("alias") && alias.equals(k.get("alias").asText())) return true; }
            return false;
        });

        if ("ALL".equals(realm)) {
            row.put("status", inAllShards ? "ok" : "partial-am");
        } else if (inVault && inAllShards) {
            row.put("status", "ok");
        } else if (inVault && !inAllShards) {
            row.put("status", "partial-am");
        } else if (!inVault && inAllShards) {
            row.put("status", "missing-vault");
        } else {
            row.put("status", "inconsistent");
        }

        return row;
    }

    private MultivaluedMap<String, String> shardHeaders(String shardHost, MultivaluedMap<String, String> incomingHeaders) {
        return AMShardUtils.shardHeaders(shardHost, incomingHeaders);
    }

    private ArrayNode fetchAllAMKeys(String shardHost, MultivaluedMap<String, String> incomingHeaders) {
        MultivaluedMap<String, String> headers = shardHeaders(shardHost, incomingHeaders);
        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        query.putSingle("_queryFilter", "true");

        Response r = amService.doGet(AMPaths.KEYSTORE, headers, query);
        if (r.getStatus() == 200) {
            ObjectNode body = Json.parseObject(r.readEntity(String.class));
            ArrayNode result = (ArrayNode) body.get("result");
            return result != null ? result : Json.MAPPER.createArrayNode();
        }
        return Json.MAPPER.createArrayNode();
    }

    private java.util.Set<String> fetchVaultAliases(String tenant) {
        java.util.Set<String> purposes = new java.util.LinkedHashSet<>();
        Response r = provisionService.doGet(ProvisionPaths.tenantKeys(tenant));
        if (r.getStatus() == 200) {
            ObjectNode body = Json.parseObject(r.readEntity(String.class));
            if (body != null && body.has("data")) {
                var data = body.get("data");
                var keys = data.has("keys") ? data.get("keys") : null;
                if (keys != null && keys.isArray()) {
                    for (var k : keys) purposes.add(k.asText().replace("/", ""));
                }
            }
        }
        return purposes;
    }

    private ObjectNode fetchVaultKeys(String tenant) {
        ObjectNode section = Json.MAPPER.createObjectNode();
        Response r = provisionService.doGet(ProvisionPaths.tenantKeys(tenant));
        section.put("status", r.getStatus());
        if (r.getStatus() == 200) {
            section.set("keys", Json.parseObject(r.readEntity(String.class)));
        }
        return section;
    }

    private ObjectNode fetchAMKeys(String tenant, String shardHost, MultivaluedMap<String, String> incomingHeaders) {
        ObjectNode shardResult = Json.MAPPER.createObjectNode();
        shardResult.put("shard", shardHost);
        MultivaluedMap<String, String> headers = shardHeaders(shardHost, incomingHeaders);
        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        query.putSingle("_queryFilter", "true");

        Response r = amService.doGet(AMPaths.KEYSTORE, headers, query);
        shardResult.put("status", r.getStatus());
        if (r.getStatus() == 200) {
            ObjectNode body = Json.parseObject(r.readEntity(String.class));
            ArrayNode allKeys = (ArrayNode) body.get("result");
            ArrayNode tenantKeys = Json.MAPPER.createArrayNode();
            if (allKeys != null) {
                for (var key : allKeys) {
                    String alias = key.has("alias") ? key.get("alias").asText() : "";
                    if (alias.startsWith(tenant + "-")) tenantKeys.add(key);
                }
            }
            shardResult.set("keys", tenantKeys);
        }
        return shardResult;
    }

    private ArrayNode buildComparison(String tenant, ObjectNode vaultSection, ArrayNode amShards) {
        ArrayNode comparison = Json.MAPPER.createArrayNode();

        java.util.Set<String> vaultAliases = new java.util.LinkedHashSet<>();
        if (vaultSection.has("keys") && vaultSection.get("keys").has("data")) {
            var keys = vaultSection.get("keys").get("data").get("keys");
            if (keys != null && keys.isArray()) {
                for (var k : keys) vaultAliases.add(tenant + "-" + k.asText());
            }
        }

        java.util.Map<String, java.util.Set<String>> amAliasesPerShard = new java.util.LinkedHashMap<>();
        for (var shard : amShards) {
            String shardName = shard.get("shard").asText();
            java.util.Set<String> aliases = new java.util.LinkedHashSet<>();
            if (shard.has("keys")) {
                for (var key : shard.get("keys")) aliases.add(key.get("alias").asText());
            }
            amAliasesPerShard.put(shardName, aliases);
        }

        java.util.Set<String> allAliases = new java.util.LinkedHashSet<>(vaultAliases);
        amAliasesPerShard.values().forEach(allAliases::addAll);

        for (String alias : allAliases) {
            ObjectNode row = Json.MAPPER.createObjectNode();
            row.put("alias", alias);
            row.put("inVault", vaultAliases.contains(alias));

            ObjectNode shardStatus = Json.MAPPER.createObjectNode();
            for (var entry : amAliasesPerShard.entrySet()) {
                shardStatus.put(entry.getKey(), entry.getValue().contains(alias));
            }
            row.set("amShards", shardStatus);

            boolean inVault = vaultAliases.contains(alias);
            boolean inAllShards = amAliasesPerShard.values().stream().allMatch(s -> s.contains(alias));
            boolean inNoShards = amAliasesPerShard.values().stream().noneMatch(s -> s.contains(alias));

            if (inVault && inAllShards) row.put("status", "ok");
            else if (inVault && inNoShards) row.put("status", "missing-am");
            else if (inVault && !inAllShards) row.put("status", "partial-am");
            else if (!inVault && inAllShards) row.put("status", "missing-vault");
            else row.put("status", "inconsistent");

            comparison.add(row);
        }
        return comparison;
    }
}
