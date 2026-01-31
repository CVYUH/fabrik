package com.cvyuh.utils.response.rules;

import com.cvyuh.utils.response.HttpMethod;
import com.cvyuh.utils.response.ResponseRewriteRule;
import com.cvyuh.utils.response.RewriteContext;
import jakarta.ws.rs.core.MultivaluedMap;

public final class SchemaRule implements ResponseRewriteRule {

    @Override
    public boolean matches(HttpMethod method, String path, MultivaluedMap<String, String> query) {
        return method == HttpMethod.POST && "schema".equals(query.getFirst("_action"));
    }

    @Override
    public String rewrite(String body, RewriteContext ctx) {

        // Branding ON
        String out = body;

        var branding = ctx.brandResolver().brandingFor(ctx.realm());

        for (var e : branding.entrySet()) {
            out = out.replace(e.getKey(), e.getValue());
        }

        // Deep rewrite only for authn schema
        if (ctx.path().endsWith("-config/authentication")) {
            out = rewriteAuthNSchema(out);
        }

        return out;
    }

    private String rewriteAuthNSchema(String json) {
        // optional
        return json;
    }
}
