package com.cvyuh.utils.exception;

import io.quarkus.logging.Log;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class ExceptionUtil {
    public static Response handleException(Exception e, String path) {
        if (e instanceof WebApplicationException) {
            Response res = ((WebApplicationException) e).getResponse();
            Log.error("Error invoking service: " + res.getStatus() + " - " + e.getMessage());
            Log.error(path + ": WebApplicationException Exception ", e);
            return res;
        } else {
            Log.error("Unhandled Exception: " + path, e);
            return Response.serverError().build();
        }
    }
}
