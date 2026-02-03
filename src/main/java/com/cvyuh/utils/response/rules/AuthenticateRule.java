package com.cvyuh.utils.response.rules;


import com.cvyuh.utils.misc.Json;
import com.cvyuh.utils.response.HttpMethod;
import com.cvyuh.utils.response.ResponseRewriteRule;
import com.cvyuh.utils.response.RewriteContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Map;

public final class AuthenticateRule implements ResponseRewriteRule {

    @Override
    public boolean matches(HttpMethod method, String path, MultivaluedMap<String, String> query) {
        return method == HttpMethod.POST && path.endsWith("/authenticate");
    }

    @Override
    public String rewrite(String body, RewriteContext ctx) {

        ObjectNode root = Json.parseObject(body);
        if (root == null) {
            return body; // not JSON or not object
        }

        if (!root.has("header")) {
            return body;
        }

        String header = root.get("header").asText();
        String branded = null;
        if(header.equals("WebAuthn Authentication")) {
            branded = "Sign in to CVYUH";
        } else if (header.equals("WebAuthn Registration")) {
            branded = "";
        } else {
            branded = applyBranding(header, ctx);
        }

        root.put("header", branded);

        // ---- TextOutputCallback branding (UI-aligned) ----
        if (root.has("callbacks") && root.get("callbacks").isArray()) {
            ArrayNode callbacks = (ArrayNode) root.get("callbacks");

            for (JsonNode cbNode : callbacks) {
                if (!cbNode.isObject()) continue;

                ObjectNode callback = (ObjectNode) cbNode;
                if (!"TextOutputCallback".equals(callback.path("type").asText())) {
                    continue;
                }

                ArrayNode output = (ArrayNode) callback.get("output");
                if (output == null) continue;

                String messageType = null;
                ObjectNode messageNode = null;

                for (JsonNode outNode : output) {
                    if (!outNode.isObject()) continue;

                    ObjectNode out = (ObjectNode) outNode;
                    String name = out.path("name").asText();

                    if ("messageType".equals(name)) {
                        messageType = out.path("value").asText();
                    } else if ("message".equals(name)) {
                        messageNode = out;
                    }
                }

                // Only brand plain-text messages (messageType == "0")
                if (!"0".equals(messageType) || messageNode == null) {
                    continue;
                }

                String text = messageNode.path("value").asText(null);
                if (text == null || text.isBlank()) {
                    continue;
                }

                messageNode.put("value", applyBranding(text, ctx));
            }
        }

        return Json.stringify(root);
    }

    private String applyBranding(String text, RewriteContext ctx) {
        Map<String, String> branding =
                ctx.brandResolver().brandingFor(ctx.realm());

        String out = text;
        for (var e : branding.entrySet()) {
            out = out.replace(e.getKey(), e.getValue());
        }
        return out;
    }
}
