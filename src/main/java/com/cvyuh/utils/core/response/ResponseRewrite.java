package com.cvyuh.utils.core.response;

import com.cvyuh.utils.core.HttpMethod;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class ResponseRewrite {

    public static Response rewriteIfNeeded(Response original, String path, HttpMethod method, EnumSet<RewritePhase> phases) {
        if (original == null || !original.hasEntity()) {
            return original;
        }

        MediaType mediaType = original.getMediaType();
        if (!isTextual(mediaType)) {
            return original;
        }

        original.bufferEntity(); // critical

        String body = ResponseHandler.readEntity(original);
        if (body == null || body.isEmpty()) {
            return rebuild(original, body);
        }

        String realm = resolveRealm(original, path);

        // ----- PASS 1 -----
        String rewritten = body;
        if (phases.contains(RewritePhase.PASS1)) {
            rewritten = rewriteBranding(rewritten, realm);
        }

        // ----- PASS 2 -----
        if (phases.contains(RewritePhase.PASS2)) {
            RewriteContext ctx = new RewriteContext(realm, method, path, original.getHeaders());

            for (ResponseRewriteRule rule : RewriteRegistry.rules()) {
                if (rule.matches(method, path, ctx.query())) {
                    rewritten = rule.rewrite(rewritten, ctx);
                }
            }
        }

        return rebuild(original, rewritten);
    }

    static String rewriteBranding(String body, String realm) {
        Map<String, String> replacements = brandingForRealm(realm);
        if (replacements.isEmpty()) {
            return body;
        }

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            body = body.replace(entry.getKey(), entry.getValue());
        }
        return body;
    }

    static Map<String, String> brandingForRealm(String realm) {
        return Map.of(
                "OpenAM", "CVYUH",
                "ForgeRock", "CVYUH"
        );
    }

    static boolean isTextual(MediaType mediaType) {
        if (mediaType == null) return false;

        String type = mediaType.toString().toLowerCase();
        return type.contains("json")
                || type.contains("text")
                || type.contains("javascript")
                || type.contains("html");
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

            // Skip Content-Length (will be recalculated)
            if (HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(key)) {
                continue;
            }

            for (Object value : entry.getValue()) {
                builder.header(key, value);
            }
        }
    }

    // figure out realm
    static String resolveRealm(Response response, String path) {
        // examples:
        // - from path
        // - from query param
        // - from cookie
        // - from header
        return "default";
    }


}
