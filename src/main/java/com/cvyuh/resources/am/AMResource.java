package com.cvyuh.resources.am;

import com.cvyuh.resources.Constants;
import com.cvyuh.service.am.AMHeader;
import com.cvyuh.service.am.AMQuery;
import com.cvyuh.service.am.AMService;
import com.cvyuh.utils.core.ResponseHandler;
import com.cvyuh.utils.log.LoggingContext;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

@Path(Constants.CONTEXT.AM)
public class AMResource implements ResponseHandler {

    private static final Logger logger = org.jboss.logging.Logger.getLogger(AMResource.class);

    @Inject
    @RestClient
    AMService amService;

    private String preProcess(UriInfo uriInfo, AMHeader amHeader, HttpHeaders httpHeaders) {
        MDC.put(LoggingContext.SERVICE, AMService.class.getName());

        // DO NOT touch cookies
        // No Authorization header derived from cookies here
        String cookieHeader = httpHeaders.getHeaderString("Cookie");
        if (cookieHeader != null && !cookieHeader.isEmpty()) {
            amHeader.setCookie(cookieHeader);
        }

        String subPath = uriInfo.getPath().substring(Constants.CONTEXT.AM.length());
        return "json" + subPath;
    }

    @GET
    @Path("{path:.*}")
    public Response doGet(
            @Context UriInfo uriInfo,
            @BeanParam AMHeader header,
            @BeanParam AMQuery query,
            @Context HttpHeaders httpHeaders
    ) {
        String path = preProcess(uriInfo, header, httpHeaders);
        return handleResponse(v -> amService.doGet(path, header, query), path);
    }

    @POST
    @Path("{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPost(
            @Context UriInfo uriInfo,
            @BeanParam AMHeader header,
            @BeanParam AMQuery query,
            @Context HttpHeaders httpHeaders,
            String jsonBody
    ) {
        String path = preProcess(uriInfo, header, httpHeaders);
        JsonObject jsonData = jsonBody != null && !jsonBody.trim().isEmpty() ? new JsonObject(jsonBody) : new JsonObject();
        return handleResponse(v -> amService.doPost(path, header, query, jsonData.encode()), path);
    }

    @PUT
    @Path("{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPut(
            @Context UriInfo uriInfo,
            @BeanParam AMHeader header,
            @Context HttpHeaders httpHeaders,
            String jsonBody
    ) {
        String path = preProcess(uriInfo, header, httpHeaders);
        JsonObject jsonData = jsonBody != null && !jsonBody.trim().isEmpty() ? new JsonObject(jsonBody) : new JsonObject();
        return handleResponse(v -> amService.doPut(path, header, jsonData.encode()), path);
    }

    @PATCH
    @Path("{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPatch(
            @Context UriInfo uriInfo,
            @BeanParam AMHeader header,
            @Context HttpHeaders httpHeaders,
            String jsonBody
    ) {
        String path = preProcess(uriInfo, header, httpHeaders);
        JsonObject jsonData = jsonBody != null && !jsonBody.trim().isEmpty() ? new JsonObject(jsonBody) : new JsonObject();
        return handleResponse(v -> amService.doPatch(path, header, jsonData.encode()), path);
    }

    @DELETE
    @Path("{path:.*}")
    public Response doDelete(
            @Context UriInfo uriInfo,
            @BeanParam AMHeader amHeader,
            @Context HttpHeaders httpHeaders
    ) {
        String path = preProcess(uriInfo, amHeader, httpHeaders);
        return handleResponse(v -> amService.doDelete(path, amHeader), path);
    }
}
