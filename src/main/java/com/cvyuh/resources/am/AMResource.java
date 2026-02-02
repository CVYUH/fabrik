package com.cvyuh.resources.am;

import com.cvyuh.resources.Constants;
import com.cvyuh.service.am.AMService;
import com.cvyuh.utils.log.LoggingContext;
import com.cvyuh.utils.response.HttpMethod;
import com.cvyuh.utils.response.ResponseHandler;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import java.util.function.Function;

@Path(Constants.CONTEXT.AM)
public class AMResource implements ResponseHandler {

    private static final Logger logger = Logger.getLogger(AMResource.class);

    @Inject
    @RestClient
    AMService amService;

    private String preProcess(UriInfo uriInfo) {
        MDC.put(LoggingContext.SERVICE, AMService.class.getName());
        String subPath = uriInfo.getPath().substring(Constants.CONTEXT.AM.length());
        return "json" + subPath;
    }

    @GET
    @Path("{path:.*}")
    public Response doGet(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders) {
        String path = preProcess(uriInfo);
        return handleResponse(
                v -> amService.doGet(path, httpHeaders.getRequestHeaders(), uriInfo.getQueryParameters()),
                path, HttpMethod.GET, uriInfo.getQueryParameters()
        );
    }

    @POST
    @Path("{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPost(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, String jsonBody) {
        String path = preProcess(uriInfo);
        return handleResponse(
                v -> amService.doPost(path, httpHeaders.getRequestHeaders(), uriInfo.getQueryParameters(), jsonBody),
                path, HttpMethod.POST, uriInfo.getQueryParameters()
        );
    }

    @PUT
    @Path("{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPut(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, String jsonBody) {
        String path = preProcess(uriInfo);
        return handleResponse(
                v -> amService.doPut(path, httpHeaders.getRequestHeaders(), jsonBody),
                path, HttpMethod.PUT, null
        );
    }

    @PATCH
    @Path("{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPatch(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, String jsonBody) {
        String path = preProcess(uriInfo);
        return handleResponse(
                v -> amService.doPatch(path, httpHeaders.getRequestHeaders(), jsonBody),
                path, HttpMethod.PATCH, null
        );
    }

    @DELETE
    @Path("{path:.*}")
    public Response doDelete(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders) {
        String path = preProcess(uriInfo);
        return handleResponse(
                v -> amService.doDelete(path, httpHeaders.getRequestHeaders()),
                path, HttpMethod.DELETE, null
        );
    }
}
