package com.cvyuh.utils.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MultivaluedMap;
import org.jboss.logging.MDC;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for processing and logging HTTP request and response bodies.
 */
public class ProcessBody {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_BODY_SIZE = 8192; // 8KB limit for body logging

    /**
     * Process and log a request or response body
     *
     * @param body The body content as a string
     * @param contentType The content type of the body
     * @param mdcKey The MDC key to store the body under
     */
    public static void processBody(String body, String contentType, String mdcKey) {
        if (body == null || body.isEmpty()) {
            return;
        }

        try {
            // Truncate body if it exceeds the maximum size
            String processedBody = body;
            boolean truncated = false;

            if (body.length() > MAX_BODY_SIZE) {
                processedBody = body.substring(0, MAX_BODY_SIZE);
                truncated = true;
            }

            // For JSON content, try to parse and pretty-print
            if (contentType != null && contentType.toLowerCase().contains("application/json")) {
                try {
                    // Parse and re-serialize to ensure it's valid JSON
                    Object json = objectMapper.readValue(processedBody, Object.class);
                    String jsonString = objectMapper.writeValueAsString(json);
                    MDC.put(mdcKey, jsonString);

                    if (truncated) {
                        MDC.put(mdcKey + "Truncated", "true");
                    }
                } catch (Exception e) {
                    // If parsing fails, log the raw body
                    MDC.put(mdcKey, processedBody);
                    MDC.put(mdcKey + "Error", "Failed to parse JSON: " + e.getMessage());

                    if (truncated) {
                        MDC.put(mdcKey + "Truncated", "true");
                    }
                }
            } else {
                // For non-JSON content, log as-is
                MDC.put(mdcKey, processedBody);

                if (truncated) {
                    MDC.put(mdcKey + "Truncated", "true");
                }
            }
        } catch (Exception e) {
            MDC.put(mdcKey + "Error", "Failed to process body: " + e.getMessage());
        }
    }

    /**
     * Read body from an input stream and process it
     *
     * @param inputStream The input stream to read from
     * @param contentType The content type of the body
     * @param mdcKey The MDC key to store the body under
     * @return The input stream reset to its original position
     */
    public static InputStream readAndProcessBody(InputStream inputStream, String contentType, String mdcKey) {
        if (inputStream == null) {
            return null;
        }

        try {
            // Buffer the input stream so it can be read multiple times
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            bis.mark(Integer.MAX_VALUE);

            // Read the body
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = bis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            // Reset the stream so it can be read again
            bis.reset();

            // Process the body
            String bodyText = baos.toString(StandardCharsets.UTF_8);
            processBody(bodyText, contentType, mdcKey);

            return bis;
        } catch (Exception e) {
            MDC.put(mdcKey + "Error", "Failed to read body: " + e.getMessage());
            return inputStream;
        }
    }

    /**
     * Process an object body
     *
     * @param entity The entity object
     * @param contentType The content type
     * @param mdcKey The MDC key to store the body under
     */
    public static void processObjectBody(Object entity, String contentType, String mdcKey) {
        if (entity == null) {
            return;
        }

        try {
            // Handle different types of entities
            if (entity instanceof String) {
                // Direct string processing
                processBody((String) entity, contentType, mdcKey);
            } else if (entity instanceof InputStream) {
                // For InputStreams, read the content
                InputStream is = (InputStream) entity;
                is.mark(Integer.MAX_VALUE);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                is.reset();
                processBody(baos.toString(StandardCharsets.UTF_8), contentType, mdcKey);
            } else if (entity.getClass().getName().contains("InputStreamWrapper")) {
                // Special handling for RESTEasy InputStreamWrapper
                MDC.put(mdcKey, "Stream content (not directly accessible)");
            } else {
                // For other objects, try to convert to JSON
                String json = objectMapper.writeValueAsString(entity);
                processBody(json, "application/json", mdcKey);
            }
        } catch (Exception e) {
            MDC.put(mdcKey + "Error", "Failed to process object body: " + e.getMessage());
            MDC.put(mdcKey, entity.toString());
        }
    }

    /**
     * Process query parameters for logging
     *
     * @param queryParams The query parameters
     * @param mdcKey The MDC key to store the parameters under
     */
    public static void processQueryParams(MultivaluedMap<String, String> queryParams, String mdcKey) {
        if (queryParams == null || queryParams.isEmpty()) {
            return;
        }

        try {
            // Convert to a regular map for serialization
            Map<String, Object> paramsMap = new HashMap<>();

            for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                String name = entry.getKey();
                List<String> values = entry.getValue();

                if (values.size() == 1) {
                    paramsMap.put(name, values.get(0));
                } else {
                    paramsMap.put(name, values);
                }
            }

            // Convert to JSON and add to MDC
            String paramsJson = objectMapper.writeValueAsString(paramsMap);
            MDC.put(mdcKey, paramsJson);
        } catch (Exception e) {
            MDC.put(mdcKey + "Error", "Failed to process query parameters: " + e.getMessage());
        }
    }
}
