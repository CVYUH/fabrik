package com.cvyuh.utils.core.response;

import com.cvyuh.utils.core.HttpMethod;
import com.cvyuh.utils.core.branding.BrandResolver;
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
