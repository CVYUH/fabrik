package com.cvyuh.utils.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

public class ExceptionUtil {

    private static final Logger logger = Logger.getLogger(ExceptionUtil.class);

    public static Response handleException(Exception e, String path) {
        if (e instanceof WebApplicationException) {
            Response res = ((WebApplicationException) e).getResponse();
            logger.errorf(
                    "Error invoking service: status=%d path=%s message=%s",
                    res != null ? res.getStatus() : -1,
                    path,
                    e.getMessage()
            );
            logger.error(path + ": WebApplicationException Exception", e);
            return res;
        } else {
            logger.error("Unhandled Exception: " + path, e);
            return Response.serverError().build();
        }
    }
}
