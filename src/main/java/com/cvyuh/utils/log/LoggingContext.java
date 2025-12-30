package com.cvyuh.utils.log;

import org.jboss.logging.MDC;

import java.util.UUID;

/**
 * Simplified request context for tracking request-specific data.
 */
public class LoggingContext {
    public static final String REQUEST_ID = "requestId";
    public static final String CORRELATION_ID = "correlationId";
    public static final String RESOURCE = "resourceClass";
    public static final String METHOD = "method";
    public static final String SERVICE = "serviceClass";
    public static final String PATH = "path";

    // Fields that should be preserved across MDC clears
    private static final String[] PRESERVED_FIELDS = {
            RESOURCE, METHOD, PATH, SERVICE
    };

    public static String initialize(String requestId, String correlationId) {
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = requestId;
        }

        MDC.put(REQUEST_ID, requestId);
        MDC.put(CORRELATION_ID, correlationId);
        return requestId;
    }

    public static void clear() {
        // Save preserved fields
        String[] preservedValues = new String[PRESERVED_FIELDS.length];
        for (int i = 0; i < PRESERVED_FIELDS.length; i++) {
            preservedValues[i] = (String) MDC.get(PRESERVED_FIELDS[i]);
        }

        // Clear all MDC values
        MDC.clear();

        // Restore preserved fields
        for (int i = 0; i < PRESERVED_FIELDS.length; i++) {
            if (preservedValues[i] != null) {
                MDC.put(PRESERVED_FIELDS[i], preservedValues[i]);
            }
        }
    }
}
