package com.cvyuh.utils.response;

import jakarta.ws.rs.core.MultivaluedMap;

public interface ResponseRewriteRule {

    boolean matches(HttpMethod method, String path, MultivaluedMap<String, String> query);

    String rewrite(String body, RewriteContext ctx);

    /** Safe query param lookup — returns null if query map is null. */
    default String queryAction(MultivaluedMap<String, String> query) {
        return query != null ? query.getFirst("_action") : null;
    }
}
