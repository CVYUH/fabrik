package com.cvyuh.utils.response;

import com.cvyuh.utils.response.rules.AuthenticateRule;
import com.cvyuh.utils.response.rules.ModuleTypesRule;
import com.cvyuh.utils.response.rules.SchemaRule;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.List;

public final class RewriteRegistry {

    private RewriteRegistry() {}

    private static final List<ResponseRewriteRule> RULES = List.of(
            new SchemaRule(),
            new AuthenticateRule(),
            new ModuleTypesRule()
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
