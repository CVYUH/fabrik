package com.cvyuh.utils.exception;

import com.cvyuh.resources.Constants;
import com.cvyuh.utils.core.ApiError;
import com.cvyuh.utils.core.ResponseHandler;
import com.cvyuh.utils.log.LoggingContext;
import io.quarkus.logging.Log;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.MDC;

public class ExceptionUtil {
    public static Response handleException(Exception e, String path) {
        if (e instanceof NotFoundException) {
            Log.error("path: " + path +": Not Found ", e);
            return buildError(Response.Status.NOT_FOUND.getStatusCode(), e.getClass().getSimpleName(), e.getMessage(), path);
        } else if (e instanceof NotAllowedException) {
            Log.error("path: " + path +": Not Allowed ", e);
            return buildError(Response.Status.METHOD_NOT_ALLOWED.getStatusCode(), e.getClass().getSimpleName(), e.getMessage(), path);
        } else if (e instanceof NotAuthorizedException) {
            Log.error("path: " + path +": Not Authorized ", e);
            return buildError(Response.Status.UNAUTHORIZED.getStatusCode(), e.getClass().getSimpleName(), e.getMessage(), path);
        } else if (e instanceof ForbiddenException) {
            Log.error("path: " + path +": Forbidden ", e);
            return buildError(Response.Status.FORBIDDEN.getStatusCode(), e.getClass().getSimpleName(), e.getMessage(), path);
        } else if (e instanceof BadRequestException) {
            Log.error("path: " + path +": Bad Request ", e);
            return buildError(Response.Status.BAD_REQUEST.getStatusCode(), e.getClass().getSimpleName(), e.getMessage(), path);
        } else if (e instanceof NotAcceptableException) {
            Log.error("path: " + path +": Not Acceptable ", e);
            return buildError(Response.Status.NOT_ACCEPTABLE.getStatusCode(), e.getClass().getSimpleName(), e.getMessage(), path);
        }else if (e instanceof InternalServerErrorException) {
            Log.error("path: " + path +": Not Acceptable ", e);
            return buildError(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getClass().getSimpleName(), e.getMessage(), path);
        } else if (e instanceof WebApplicationException wae) {
            Response original = wae.getResponse();
            int status = original != null ? original.getStatus() : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            String message = original != null ? ResponseHandler.readEntity(original) : e.getMessage();
            Log.error("path: " + path +": WebApplicationException " + status + " - " + message, e);
            return buildError(status, e.getClass().getSimpleName(), message, path);
        } else {
            Log.error("Unhandled Exception: " + path, e);
            return Response.serverError().build();
        }
    }

    private static Response buildError(int status, String error, String message, String context) {

        ApiError apiError = new ApiError();
        apiError.status = status;
        apiError.error = error;
        apiError.message = message;
        apiError.backend = extractBackend(context);
        apiError.path = context;
        apiError.requestId = (String) MDC.get(LoggingContext.REQUEST_ID);

        return Response.status(status).entity(apiError).build();
    }

    private static String extractBackend(String context) {
        if (context == null) return "internal";
        if (context.contains(Constants.CONTEXT.AM)) return "access-manager";
        return "internal";
    }
}
