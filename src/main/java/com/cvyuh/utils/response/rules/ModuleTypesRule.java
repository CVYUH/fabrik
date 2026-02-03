package com.cvyuh.utils.response.rules;

import com.cvyuh.utils.misc.Json;
import com.cvyuh.utils.response.HttpMethod;
import com.cvyuh.utils.response.ResponseRewriteRule;
import com.cvyuh.utils.response.RewriteContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Set;

public final class ModuleTypesRule implements ResponseRewriteRule {

    // pre-registered / allowed module ids
    private static final Set<String> ALLOWED = Set.of(
            //"amAuthADAgent", "amAuthGeofence", //TODO
            "datastore", "ldap", "authenticatoroath", "webauthnreg", "webauthnauth",
            "activedirectory", "oauth2"
    );

    @Override
    public boolean matches(HttpMethod method, String path, MultivaluedMap<String, String> query) {
        return method == HttpMethod.POST
                && path.endsWith("/realm-config/authentication/modules")
                && "getAllTypes".equals(query.getFirst("_action"));
    }

    @Override
    public String rewrite(String body, RewriteContext ctx) {

        ObjectNode root = Json.parseObject(body);
        if (root == null || !root.has("result") || !root.get("result").isArray()) {
            return body;
        }

        ArrayNode original = (ArrayNode) root.get("result");
        ArrayNode filtered = root.arrayNode();

        for (JsonNode node : original) {
            String id = node.path("_id").asText(null);
            if (id != null && ALLOWED.contains(id)) {
                filtered.add(node);
            }
        }

        root.set("result", filtered);
        return Json.stringify(root);
    }
}
