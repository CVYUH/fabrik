package com.cvyuh.utils.response;

import com.cvyuh.utils.branding.BrandResolver;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

public record RewriteContext(
        String realm,
        HttpMethod method,
        String path,
        MultivaluedMap<String, String> query,
        int status,
        MediaType mediaType,
        BrandResolver brandResolver
) {}
