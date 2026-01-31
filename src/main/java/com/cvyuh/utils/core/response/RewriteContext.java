package com.cvyuh.utils.core.response;

import com.cvyuh.utils.core.HttpMethod;
import jakarta.ws.rs.core.MultivaluedMap;

public record RewriteContext(
        String realm,
        HttpMethod method,
        String path,
        MultivaluedMap<String, Object> query
) {}