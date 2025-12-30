package com.cvyuh.utils.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MultivaluedMap;
import org.jboss.logging.MDC;

import java.util.*;
import java.util.stream.Collectors;

public class ProcessHeaders {

    // TODO: Add pattern based masking, for all tokens in body?
    private static final Set<String> SENSITIVE_HEADERS = new HashSet<>(List.of(
            "cookie", "authorization"
    ));

    /**
     * Process headers for structured logging
     * This adds each header as a separate MDC entry with a prefix
     */
    public static <T> void processHeaders(MultivaluedMap<String, T> headers, String prefix) {
        if (headers == null || headers.isEmpty()) {
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Create a structured map for all headers
            Map<String, Object> headersMap = new HashMap<>();

            for (Map.Entry<String, List<T>> entry : headers.entrySet()) {
                String name = entry.getKey();
                List<T> values = entry.getValue();

                // Sanitize sensitive headers
                if (isSensitiveHeader(name)) {
                    headersMap.put(name, "********");
                } else if (values.size() == 1) {
                    // Single value header
                    headersMap.put(name, values.get(0).toString());
                } else {
                    // Multi-value header - convert to list of strings
                    List<String> stringValues = values.stream()
                            .map(Object::toString)
                            .collect(Collectors.toList());
                    headersMap.put(name, stringValues);
                }
            }

            // Convert the map to a JSON string and add to MDC
            String headersJson = objectMapper.writeValueAsString(headersMap);
            MDC.put(prefix + "Headers", headersJson);
        } catch (Exception e) {
            // If JSON conversion fails, fall back to toString()
            MDC.put(prefix + "Headers", headers.toString());
        }
    }

    /**
     * Check if a header is sensitive and should be masked
     */
    private static boolean isSensitiveHeader ( String headerName ) {
        if (headerName == null) {
            return false;
        }
        return SENSITIVE_HEADERS.contains(headerName.toLowerCase());
    }
}
