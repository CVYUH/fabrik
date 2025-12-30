package com.cvyuh.utils.core;

import java.time.Instant;

public class ApiError {

    public String error;        // short reason (Unauthorized, BadRequest, etc.)
    public String message;      // backend / human-readable message
    public String backend;      // openam | vault | openidm | wazuh
    public String path;         // requested path
    public int status;          // HTTP status
    public String requestId;    // correlation
    public Instant timestamp = Instant.now();

    public static ApiError of(int status, String error, String message,
                              String backend, String path, String requestId) {
        ApiError e = new ApiError();
        e.status = status;
        e.error = error;
        e.message = message;
        e.backend = backend;
        e.path = path;
        e.requestId = requestId;
        return e;
    }
}
