package com.cvyuh.utils.core.response;

import com.cvyuh.utils.core.HttpMethod;
import com.cvyuh.utils.core.response.rules.SchemaWhitelabelRule;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.List;

public final class RewriteRegistry {

    private RewriteRegistry() {}

    private static final List<ResponseRewriteRule> RULES = List.of(
            new SchemaWhitelabelRule()
            // new SomeOtherRule(),
            // ...
    );

    public static List<ResponseRewriteRule> rules() {
        return RULES;
    }

    // if nothing matches, do NOT buffer/read entity.
    public static boolean anyMatch(HttpMethod method, String path, MultivaluedMap<String, String> query) {
        for (ResponseRewriteRule rule : RULES) {
            if (rule.matches(method, path, query)) return true;
        }
        return false;
    }
}
