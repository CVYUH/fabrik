package com.cvyuh.utils.exception;

import com.cvyuh.utils.core.ResponseHandler;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

public class ExceptionWrapper implements ResponseExceptionMapper<RuntimeException> {

    @Override
    public RuntimeException toThrowable(Response response) {
        String msg = ResponseHandler.readEntity(response);
        Response newResp = Response.status(response.getStatus()).entity(msg).build();
        return new WebApplicationException(msg, newResp);
    }
}
