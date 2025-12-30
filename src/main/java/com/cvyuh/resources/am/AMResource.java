package com.cvyuh.resources.am;

import com.cvyuh.resources.Constants;
import com.cvyuh.resources.CvyuhToken;
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

    private String preProcess(UriInfo uriInfo, AMHeader amheader, String cvyuhToken) {
        MDC.put(LoggingContext.SERVICE, AMService.class.getName());

        // Ensure header is not null
        /*if (amheader == null) {
            amheader = new AMHeader();
        }*/

        // Set authentication headers
        cvyuhToken = ((cvyuhToken != null) && (cvyuhToken.length() > 0)) ? cvyuhToken : "";
        String authorization = "Bearer " + cvyuhToken;
        String cookie = Constants.TOKEN.AM + "=" + cvyuhToken;
        amheader.setAuthorization(authorization);
        amheader.setCookie(cookie);
        String subPath = uriInfo.getPath().substring(Constants.CONTEXT.AM.length());
        return "/openam" + subPath;
    }

    @GET
    @Path("{path:.*}")
    public Response doGet(
            @Context UriInfo uriInfo,
            @BeanParam AMHeader header,
            @BeanParam AMQuery query,
            @BeanParam CvyuhToken cvyuhToken
    ) {
        String path = preProcess(uriInfo, header, cvyuhToken.getCvyuhToken());
        return handleResponse(v -> amService.doGet(path, header, query), path);
    }

    @POST
    @Path("{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPost(
            @Context UriInfo uriInfo,
            @BeanParam AMHeader header,
            @BeanParam AMQuery query,
            @BeanParam CvyuhToken cvyuhToken,
            String jsonBody
    ) {
        String path = preProcess(uriInfo, header, cvyuhToken.getCvyuhToken());
        JsonObject jsonData = jsonBody != null && !jsonBody.trim().isEmpty() ? new JsonObject(jsonBody) : new JsonObject();
        return handleResponse(v -> amService.doPost(path, header, query, jsonData.encode()), path);
    }

    @PUT
    @Path("{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPut(
            @Context UriInfo uriInfo,
            @BeanParam AMHeader header,
            @BeanParam CvyuhToken cvyuhToken,
            String jsonBody
    ) {
        String path = preProcess(uriInfo, header, cvyuhToken.getCvyuhToken());
        JsonObject jsonData = jsonBody != null && !jsonBody.trim().isEmpty() ? new JsonObject(jsonBody) : new JsonObject();
        return handleResponse(v -> amService.doPut(path, header, jsonData.encode()), path);
    }

    @PATCH
    @Path("{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPatch(
            @Context UriInfo uriInfo,
            @BeanParam AMHeader header,
            @BeanParam CvyuhToken cvyuhToken,
            String jsonBody
    ) {
        String path = preProcess(uriInfo, header, cvyuhToken.getCvyuhToken());
        JsonObject jsonData = jsonBody != null && !jsonBody.trim().isEmpty() ? new JsonObject(jsonBody) : new JsonObject();
        return handleResponse(v -> amService.doPatch(path, header, jsonData.encode()), path);
    }

    @DELETE
    @Path("{path:.*}")
    public Response doDelete(
            @Context UriInfo uriInfo,
            @BeanParam AMHeader amHeader,
            @BeanParam CvyuhToken cvyuhToken
    ) {
        String path = preProcess(uriInfo, amHeader, cvyuhToken.getCvyuhToken());
        return handleResponse(v -> amService.doDelete(path, amHeader), path);
    }
}
