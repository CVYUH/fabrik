package com.cvyuh.utils.core.response;

import com.cvyuh.utils.core.HttpMethod;
import com.cvyuh.utils.core.branding.BrandResolvers;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

public final class ResponseRewrite {

    private ResponseRewrite() {}

    /**
     * Rewrite only if a registry rule matches (method + path + requestQuery).
     * Otherwise return response untouched
     */
    public static Response rewriteIfNeeded(Response original, String path, HttpMethod method, MultivaluedMap<String, String> requestQuery) {
        if (original == null || !original.hasEntity()) {
            return original;
        }

        if (!isTextual(original.getMediaType())) {
            return original;
        }

        // IMPORTANT: Do not buffer/read unless a rule might apply.
        if (!RewriteRegistry.anyMatch(method, path, requestQuery)) {
            return original;
        }

        // Now we know at least one rule matches → safe to buffer and rewrite.
        original.bufferEntity(); // critical to allow read + later rebuild safely

        String body = ResponseHandler.readEntity(original);
        if (body == null || body.isEmpty()) {
            return rebuild(original, body);
        }

        RewriteContext ctx = new RewriteContext(
                resolveRealm(path),
                method,
                path,
                requestQuery,
                original.getStatus(),
                original.getMediaType(),
                BrandResolvers.current() // static access for now
        );

        String rewritten = body;
        for (ResponseRewriteRule rule : RewriteRegistry.rules()) {
            if (rule.matches(method, path, requestQuery)) {
                rewritten = rule.rewrite(rewritten, ctx);
            }
        }

        // If no change, you can return original, but we've already consumed entity.
        // So we rebuild in all rewrite paths.
        return rebuild(original, rewritten);
    }

    static boolean isTextual(MediaType mediaType) {
        if (mediaType == null) return false;

        String type = mediaType.toString().toLowerCase();
        return type.contains("json")
                || type.contains("text")
                || type.contains("javascript")
                || type.contains("html")
                || type.contains("xml");
    }

    static Response rebuild(Response original, String body) {
        Response.ResponseBuilder builder = Response
                .status(original.getStatus())
                .type(original.getMediaType())
                .entity(body);

        copyHeaders(original.getHeaders(), builder);
        return builder.build();
    }

    static void copyHeaders(MultivaluedMap<String, Object> headers,
                            Response.ResponseBuilder builder) {
        for (Map.Entry<String, List<Object>> entry : headers.entrySet()) {
            String key = entry.getKey();

            // Content-Length must be recalculated
            if (HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(key)) {
                continue;
            }

            for (Object value : entry.getValue()) {
                builder.header(key, value);
            }
        }
    }

    /**
     * Realm resolution is intentionally loose and safe.
     * Extend later if needed (from path parsing).
     */
    static String resolveRealm(String path) {
        // Example heuristic later:
        // /realms/root/realms/acme/...  -> acme
        return "default";
    }
}
