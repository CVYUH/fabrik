package com.cvyuh.utils.log;

import io.quarkus.logging.Log;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import org.jboss.logmanager.Level;

import java.io.IOException;
import java.io.InputStream;

import static com.cvyuh.utils.log.ProcessBody.*;
import static com.cvyuh.utils.log.ProcessHeaders.processHeaders;

@Provider
@Priority(Priorities.USER - 100)
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter,
        ClientRequestFilter, ClientResponseFilter {

    private static final Logger logger = Logger.getLogger(LoggingFilter.class);

    private static final String CLIENT_TIMER_KEY = "client-request-timer";
    private static final String SERVER_TIMER_KEY = "server-request-timer";

    @Context
    private ResourceInfo resourceInfo;

    // Server-side request handling
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Extract or generate request IDs
        String requestId = requestContext.getHeaderString("X-Request-ID");
        String correlationId = requestContext.getHeaderString("X-Correlation-ID");
        LoggingContext.initialize(requestId, correlationId);

        Level level = null;

        if (resourceInfo != null && resourceInfo.getResourceClass() != null) {
            MDC.put(LoggingContext.RESOURCE, resourceInfo.getResourceClass().getName());
            level = LoggingResource.findEffectiveLogLevel(resourceInfo.getResourceClass().getName());
        }
        if (resourceInfo != null && resourceInfo.getResourceMethod() != null) {
            MDC.put(LoggingContext.METHOD, resourceInfo.getResourceMethod().getName());
        }

        // Skip if level is null or higher than INFO
        if (level == null || level.intValue() > Level.INFO.intValue()) {
            return;
        }

        // Set path in MDC
        String path = requestContext.getUriInfo().getPath();
        MDC.put(LoggingContext.PATH, path);

        // Start server request timer
        long startTime = System.currentTimeMillis();
        requestContext.setProperty(SERVER_TIMER_KEY, startTime);

        // Process headers for structured logging - only at TRACE level
        if (level.intValue() <= Level.TRACE.intValue()) {
            processHeaders(requestContext.getHeaders(), "req");
        }

        // Process queryParams for structured logging - at DEBUG level or lower
        if (level.intValue() <= Level.DEBUG.intValue()) {
            processQueryParams(requestContext.getUriInfo().getQueryParameters(), "queryParams");
        }

        // Process request body if present - at DEBUG level or lower
        if (level.intValue() <= Level.DEBUG.intValue() && requestContext.hasEntity()) {
            String contentType = requestContext.getHeaderString("Content-Type");
            InputStream entityStream = requestContext.getEntityStream();
            // This reads the body, processes it, and returns the stream reset to its original position
            InputStream processedStream = readAndProcessBody(entityStream, contentType, "reqBody");
            requestContext.setEntityStream(processedStream);
        }

        // Log the request at the appropriate level
        MDC.put("direction", "RECEIVED");
        MDC.put(LoggingContext.METHOD, requestContext.getMethod());

        // Log at the appropriate level
        String resourceClass = (String) MDC.get(LoggingContext.RESOURCE);
        if (resourceClass != null) {
            logAtLevelWithMessage(resourceClass, level, "Request received");
        }

        // Clear headers from MDC after logging
        MDC.remove("reqHeaders");
    }

    // Server-side response handling
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        // Get resource class and level
        String resourceClass = (String) MDC.get(LoggingContext.RESOURCE);
        if (resourceClass == null) {
            return;
        }

        Level level = LoggingResource.findEffectiveLogLevel(resourceClass);
        if (level == null || level.intValue() > Level.INFO.intValue()) {
            return;
        }

        // Calculate request duration
        long duration = 0;
        Object startTimeObj = requestContext.getProperty(SERVER_TIMER_KEY);
        if (startTimeObj instanceof Long) {
            duration = System.currentTimeMillis() - (Long) startTimeObj;
        }

        // Set status and duration in MDC
        MDC.put("status", responseContext.getStatus());
        MDC.put("duration", duration);
        MDC.put("direction", "COMPLETED");
        MDC.put(LoggingContext.METHOD, requestContext.getMethod());

        // Process response headers only at TRACE level
        if (level.intValue() <= Level.TRACE.intValue()) {
            processHeaders(responseContext.getHeaders(), "res");
        }

        // Log the response at the appropriate level
        logAtLevelWithMessage(resourceClass, level, "Request completed");

        // Clean up
        LoggingContext.clear();
    }

    // Client-side request handling
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String serviceClass = (String) MDC.get(LoggingContext.SERVICE);
        Level level = LoggingResource.findEffectiveLogLevel(serviceClass);
        if (level == null || level.intValue() > Level.INFO.intValue()) {
            return;
        }

        // Propagate request ID and correlation ID to outgoing requests
        String requestId = (String) MDC.get(LoggingContext.REQUEST_ID);
        String correlationId = (String) MDC.get(LoggingContext.CORRELATION_ID);

        if (requestId != null) {
            requestContext.getHeaders().add("X-Request-ID", requestId);
        }

        if (correlationId != null) {
            requestContext.getHeaders().add("X-Correlation-ID", correlationId);
        }

        // Store start time for duration calculation
        requestContext.setProperty(CLIENT_TIMER_KEY, System.currentTimeMillis());

        // Set client request info in MDC
        MDC.put("direction", "OUTGOING");
        MDC.put(LoggingContext.METHOD, requestContext.getMethod());
        MDC.put("url", requestContext.getUri().toString());

        // Process client request headers at TRACE level
        if (level.intValue() <= Level.TRACE.intValue()) {
            processHeaders(requestContext.getHeaders(), "clientReq");
        }

        // Process queryParams for structured logging at DEBUG level
        if (level.intValue() <= Level.DEBUG.intValue()) {
            String query = requestContext.getUri().getQuery();
            if (query != null && !query.isEmpty()) {
                // Convert query string to MultivaluedMap
                MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
                for (String param : query.split("&")) {
                    String[] keyValue = param.split("=", 2);
                    String key = keyValue[0];
                    String value = keyValue.length > 1 ? keyValue[1] : "";
                    queryParams.add(key, value);
                }
                processQueryParams(queryParams, "clientQueryParams");
            }
        }

        // Process client request body if present at DEBUG level
        if (level.intValue() <= Level.DEBUG.intValue() && requestContext.hasEntity()) {
            String contentType = requestContext.getMediaType() != null ?
                    requestContext.getMediaType().toString() : null;
            processObjectBody(requestContext.getEntity(), contentType, "clientReqBody");
        }

        // Log outgoing request at the appropriate level
        logAtLevelWithMessage(serviceClass, level, "Client request sent");

        // Clear headers from MDC after logging
        MDC.remove("clientReqHeaders");
    }

    // Client-side response handling
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
            throws IOException {
        String serviceClass = (String) MDC.get(LoggingContext.SERVICE);
        Level level = LoggingResource.findEffectiveLogLevel(serviceClass);
        if (level == null || level.intValue() > Level.INFO.intValue()) {
            return;
        }

        // Calculate duration if start time was set
        long duration = 0;
        Object startTimeObj = requestContext.getProperty(CLIENT_TIMER_KEY);
        if (startTimeObj instanceof Long) {
            duration = System.currentTimeMillis() - (Long) startTimeObj;
        }

        // Set client response info in MDC
        MDC.put("direction", "RESPONSE");
        MDC.put(LoggingContext.METHOD, requestContext.getMethod());
        MDC.put("url", requestContext.getUri().toString());
        MDC.put("status", responseContext.getStatus());
        MDC.put("duration", duration);

        // Process client response headers only at TRACE level
        if (level.intValue() <= Level.TRACE.intValue()) {
            processHeaders(responseContext.getHeaders(), "clientRes");
        }

        // Process client response body if present at TRACE level
        if (level.intValue() <= Level.TRACE.intValue() && responseContext.hasEntity()) {
            String contentType = responseContext.getMediaType() != null ?
                    responseContext.getMediaType().toString() : null;
            InputStream entityStream = responseContext.getEntityStream();
            // This reads the body, processes it, and returns the stream reset to its original position
            InputStream processedStream = readAndProcessBody(entityStream, contentType, "clientResBody");
            responseContext.setEntityStream(processedStream);
        }

        // Log response at the appropriate level
        logAtLevelWithMessage(serviceClass, level, "Client response received");

        // Clear headers from MDC after logging
        MDC.remove("clientResHeaders");
        MDC.remove("clientResBody");
    }

    /**
     * Log a message at the appropriate level based on the configured level
     *
     * @param name The logger name (class name)
     * @param level The log level to use
     * @param message The message to log
     */
    private void logAtLevelWithMessage(String name, Level level, String message) {
        if (name == null || level == null) {
            Log.info(message + " (default)");
            return;
        }

        Logger logger = Logger.getLogger(name);

        // Log at the appropriate level
        if (level.intValue() <= Level.TRACE.intValue()) {
            logger.trace(message);
        } else if (level.intValue() <= Level.DEBUG.intValue()) {
            logger.debug(message);
        } else {
            logger.info(message);
        }
    }

}
