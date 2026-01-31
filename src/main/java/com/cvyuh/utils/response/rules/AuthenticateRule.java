package com.cvyuh.utils.response.rules;


import com.cvyuh.utils.misc.Json;
import com.cvyuh.utils.response.HttpMethod;
import com.cvyuh.utils.response.ResponseRewriteRule;
import com.cvyuh.utils.response.RewriteContext;
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
        } else {
            branded = applyBranding(header, ctx);
        }

        root.put("header", branded);
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
