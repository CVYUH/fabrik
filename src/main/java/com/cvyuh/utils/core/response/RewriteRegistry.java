package com.cvyuh.utils.core.response;

import java.util.List;

public final class RewriteRegistry {

    private static final List<ResponseRewriteRule> RULES = List.of(
            // add here
    );

    public static List<ResponseRewriteRule> rules() {
        return RULES;
    }
}