package com.cvyuh.resources.am;

import com.cvyuh.resources.Constants;
import com.cvyuh.service.am.AMCookie;
import com.cvyuh.service.am.AMHeader;
import com.cvyuh.service.am.AMQuery;
import com.cvyuh.service.am.AMService;
import com.cvyuh.utils.core.ResponseHandler;
import com.cvyuh.utils.log.LoggingContext;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

@Path(Constants.CONTEXT.AM)
public class AMResource implements ResponseHandler {

    private static final Logger logger = org.jboss.logging.Logger.getLogger(AMResource.class);

    @Inject
    @RestClient
    AMService amService;

    private String preProcess(UriInfo uriInfo, AMHeader amheader, AMCookie amCookie) {
        MDC.put(LoggingContext.SERVICE, AMService.class.getName());

        // Set authentication headers
        String amToken = amCookie.getAm();
        amToken = ((amToken != null) && (amToken.length() > 0)) ? amToken : "";
        String authorization = "Bearer " + amToken;
        amheader.setAuthorization(authorization);
        //String cookie = Constants.COOKIE.AM + "=" + amToken;
        //amheader.setCookie(cookie);
        String subPath = uriInfo.getPath().substring(Constants.CONTEXT.AM.length());
        return "json" + subPath;
    }

    @GET
    @Path("{path:.*}")
    public Response doGet(
            @Context UriInfo uriInfo,
            @BeanParam AMHeader header,
            @BeanParam AMQuery query,
            @BeanParam AMCookie amCookie
    ) {
        String path = preProcess(uriInfo, header, amCookie);
        return handleResponse(v -> amService.doGet(path, header, amCookie, query), path);
    }

    @POST
    @Path("{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPost(
            @Context UriInfo uriInfo,
            @BeanParam AMHeader header,
            @BeanParam AMQuery query,
            @BeanParam AMCookie amCookie,
            String jsonBody
    ) {
        String path = preProcess(uriInfo, header, amCookie);
        JsonObject jsonData = jsonBody != null && !jsonBody.trim().isEmpty() ? new JsonObject(jsonBody) : new JsonObject();
        return handleResponse(v -> amService.doPost(path, header, amCookie, query, jsonData.encode()), path);
    }

    @PUT
    @Path("{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPut(
            @Context UriInfo uriInfo,
            @BeanParam AMHeader header,
            @BeanParam AMCookie amCookie,
            String jsonBody
    ) {
        String path = preProcess(uriInfo, header, amCookie);
        JsonObject jsonData = jsonBody != null && !jsonBody.trim().isEmpty() ? new JsonObject(jsonBody) : new JsonObject();
        return handleResponse(v -> amService.doPut(path, header, amCookie, jsonData.encode()), path);
    }

    @PATCH
    @Path("{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPatch(
            @Context UriInfo uriInfo,
            @BeanParam AMHeader header,
            @BeanParam AMCookie amCookie,
            String jsonBody
    ) {
        String path = preProcess(uriInfo, header, amCookie);
        JsonObject jsonData = jsonBody != null && !jsonBody.trim().isEmpty() ? new JsonObject(jsonBody) : new JsonObject();
        return handleResponse(v -> amService.doPatch(path, header, amCookie, jsonData.encode()), path);
    }

    @DELETE
    @Path("{path:.*}")
    public Response doDelete(
            @Context UriInfo uriInfo,
            @BeanParam AMHeader amHeader,
            @BeanParam AMCookie amCookie
    ) {
        String path = preProcess(uriInfo, amHeader, amCookie);
        return handleResponse(v -> amService.doDelete(path, amHeader, amCookie), path);
    }
}
