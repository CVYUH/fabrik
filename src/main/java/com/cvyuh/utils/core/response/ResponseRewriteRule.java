package com.cvyuh.utils.core.response;

import com.cvyuh.utils.core.HttpMethod;
import jakarta.ws.rs.core.MultivaluedMap;

public interface ResponseRewriteRule {

    boolean matches(HttpMethod method, String path, MultivaluedMap<String, String> query);

    String rewrite(String body, RewriteContext ctx);
}
