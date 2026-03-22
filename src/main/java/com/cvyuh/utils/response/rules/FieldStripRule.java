package com.cvyuh.utils.response.rules;

import com.cvyuh.utils.response.HttpMethod;
import com.cvyuh.utils.response.ResponseRewriteRule;
import com.cvyuh.utils.response.RewriteContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.cvyuh.utils.misc.Json;
import jakarta.ws.rs.core.MultivaluedMap;
import org.jboss.logging.Logger;

import java.util.*;

/**
 * Strips fields from JSON responses based on a path → fields configuration.
 *
 * Matches any GET or POST (_action=schema) on configured path patterns.
 * Strips fields at any nesting depth — walks the JSON tree and removes
 * matching field names from all objects.
 *
 * Configuration is static — add entries to STRIP_CONFIG for new services.
 * No new Java class needed per service.
 */
public final class FieldStripRule implements ResponseRewriteRule {

    private static final Logger LOG = Logger.getLogger(FieldStripRule.class);

    /**
     * Path suffix → set of field names to strip.
     * Matches if the request path ends with the key.
     */
    private static final Map<String, Set<String>> STRIP_CONFIG = new LinkedHashMap<>();

    static {
        // OAuth2 Provider — hide implementation details from realm admins
        STRIP_CONFIG.put("realm-config/services/oauth-oidc", Set.of(
                "scopeImplementationClass",
                "forgerock-oauth2-provider-scope-implementation-class",
                "responseTypeClasses",
                "forgerock-oauth2-provider-response-type-map-class",
                "tokenSigningHmacSharedSecret"
        ));

        // Add more services here:
        // STRIP_CONFIG.put("realm-config/services/some-service", Set.of("field1", "field2"));
    }

    @Override
    public boolean matches(HttpMethod method, String path, MultivaluedMap<String, String> query) {
        if (method != HttpMethod.GET && !(method == HttpMethod.POST && "schema".equals(queryAction(query)))) {
            return false;
        }
        for (String suffix : STRIP_CONFIG.keySet()) {
            if (path.contains(suffix)) return true;
        }
        return false;
    }

    @Override
    public String rewrite(String body, RewriteContext ctx) {
        Set<String> fieldsToStrip = resolveFields(ctx.path());
        if (fieldsToStrip.isEmpty()) return body;

        try {
            JsonNode root = Json.MAPPER.readTree(body);
            stripFields(root, fieldsToStrip);
            return Json.MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            LOG.debugf("FieldStripRule: failed to parse/strip JSON for path=%s (%s)", ctx.path(), e.getMessage());
            return body;
        }
    }

    private Set<String> resolveFields(String path) {
        for (Map.Entry<String, Set<String>> entry : STRIP_CONFIG.entrySet()) {
            if (path.contains(entry.getKey())) return entry.getValue();
        }
        return Set.of();
    }

    /**
     * Recursively walk the JSON tree and remove matching field names from all objects.
     * Handles both schema responses (nested under properties) and data responses (nested under config groups).
     */
    private void stripFields(JsonNode node, Set<String> fields) {
        if (node == null) return;

        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            // Remove direct matches
            for (String field : fields) {
                obj.remove(field);
            }
            // Recurse into remaining children
            Iterator<Map.Entry<String, JsonNode>> it = obj.fields();
            while (it.hasNext()) {
                stripFields(it.next().getValue(), fields);
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                stripFields(child, fields);
            }
        }
    }
}
