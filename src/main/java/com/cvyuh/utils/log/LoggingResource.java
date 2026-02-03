package com.cvyuh.utils.log;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logmanager.Level;
import org.jboss.logmanager.LogContext;
import org.jboss.logmanager.Logger;

import java.util.*;

@Path("/api/logging")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoggingResource {

    private static final LogContext logContext = LogContext.getLogContext();
    private static final Logger logger = logContext.getLogger(LoggingResource.class.getName());

    /**
     * 1. Get all log levels
     */
    @GET
    @Path("/levels")
    public Response getAllLogLevels() {
        logger.info("Fetching all log levels");

        Map<String, String> levels = new TreeMap<>();
        Enumeration<String> loggerNames = logContext.getLoggerNames();

        List<String> loggerNameList = Collections.list(loggerNames);

        for (String name : loggerNameList) {
            Logger logger = logContext.getLogger(name);
            levels.put(name, (logger.getLevel() != null) ? logger.getLevel().getName() : "INHERITED");
        }

        return Response.ok(levels).build();
    }


    /**
     * 2. Get the effective log level of a specific class or package
     */
    @GET
    @Path("/levels/{classOrPackageName}")
    public Response getEffectiveLogLevel(@PathParam("classOrPackageName") String classOrPackageName) {
        Logger logger = logContext.getLogger(classOrPackageName);
        Level configuredLevel = (Level) logger.getLevel();
        Level effectiveLevel = findEffectiveLogLevel(logger);

        return Response.ok(Map.of(
                "logger", classOrPackageName,
                "configuredLevel", (configuredLevel != null) ? configuredLevel.getName() : "INHERITED",
                "effectiveLevel", (effectiveLevel != null) ? effectiveLevel.getName() : "UNKNOWN",
                "jbossLevel", convertToJBossLevel(effectiveLevel)
        )).build();
    }

    /**
     * 3. Set log level for a specific class or package
     */
    @PUT
    @Path("/levels/{classOrPackageName}/set/{level}")
    public Response setLogLevel(@PathParam("classOrPackageName") String classOrPackageName, @PathParam("level") String level) {
        try {
            Level jbossLevel = parseLevel(level);
            Logger logger = logContext.getLogger(classOrPackageName);
            logger.setLevel(jbossLevel);

            return Response.ok(Map.of("logger", classOrPackageName, "level", jbossLevel.getName())).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid log level: " + level))
                    .build();
        }
    }

    /**
     * 4. Get full hierarchy of log levels up to root
     */
    @GET
    @Path("/levels/{classOrPackageName}/full")
    public Response getLogHierarchy(@PathParam("classOrPackageName") String classOrPackageName) {
        Logger logger = logContext.getLogger(classOrPackageName);
        List<Map<String, String>> hierarchy = new ArrayList<>();
        Set<String> visitedLoggers = new HashSet<>(); // Prevent infinite loops

        while (logger != null && visitedLoggers.add(logger.getName())) {
            Level level = (Level) logger.getLevel();
            Level effectiveLevel = findEffectiveLogLevel(logger);

            hierarchy.add(Map.of(
                    "logger", logger.getName(),
                    "configuredLevel", (level != null) ? level.getName() : "INHERITED",
                    "effectiveLevel", (effectiveLevel != null) ? effectiveLevel.getName() : "UNKNOWN",
                    "jbossLevel", convertToJBossLevel(effectiveLevel)
            ));

            logger = logger.getParent(); // Move to parent logger
        }

        return Response.ok(hierarchy).build();
    }

    public static Level findEffectiveLogLevel ( String classOrPackageName ) {
        Logger logger = logContext.getLogger(classOrPackageName);
        return findEffectiveLogLevel(logger);
    }


    /**
     * Helper function: Traverse logger hierarchy to find the nearest set level
     */
    private static Level findEffectiveLogLevel(Logger logger) {
        Set<String> visitedLoggers = new HashSet<>();

        while (logger != null && visitedLoggers.add(logger.getName())) {
            if (logger.getLevel() != null) {
                return (Level) logger.getLevel();
            }
            logger = logger.getParent();
        }
        return Level.FATAL; // Default fallback if no level is explicitly set
    }

    /**
     * Helper function: Convert JBoss log levels to readable format
     */
    private String convertToJBossLevel(Level level) {
        if (level == null) return "INFO"; // Default
        int value = level.intValue();

        if (value <= Level.TRACE.intValue()) return "TRACE";
        if (value <= Level.DEBUG.intValue()) return "DEBUG";
        if (value <= Level.INFO.intValue()) return "INFO";
        if (value <= Level.WARN.intValue()) return "WARN";
        return "ERROR";
    }

    /**
     * Helper function: Parse level name to JBoss Logging Level
     */
    private Level parseLevel(String levelName) {
        return switch (levelName.toUpperCase()) {
            case "FATAL", "SEVERE", "ERROR" -> Level.ERROR;
            case "WARN", "WARNING" -> Level.WARN;
            case "INFO" -> Level.INFO;
            case "DEBUG", "FINE" -> Level.DEBUG;
            case "TRACE", "FINER", "FINEST" -> Level.TRACE;
            default -> throw new IllegalArgumentException("Unknown level: " + levelName);
        };
    }
}
