package com.cvyuh.service.am;

import com.cvyuh.utils.exception.ExceptionWrapper;
import com.cvyuh.utils.log.LoggingFilter;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/")
@RegisterProvider(LoggingFilter.class)
@RegisterProvider(ExceptionWrapper.class)
@RegisterRestClient
public interface AMService {
    @GET
    @Path("{path: .+}")
    Response doGet(
            @PathParam("path") @Encoded String path,
            @RestHeader MultivaluedMap<String, String> headers,
            @RestQuery MultivaluedMap<String, String> queryParams
    );

    @POST
    @Path("{path: .+}")
    Response doPost(
            @PathParam("path") @Encoded String path,
            @RestHeader MultivaluedMap<String, String> headers,
            @RestQuery MultivaluedMap<String, String> queryParams,
            String jsonBody
    );

    @PUT
    @Path("{path: .+}")
    Response doPut(
            @PathParam("path") @Encoded String path,
            @RestHeader MultivaluedMap<String, String> headers,
            String jsonBody
    );

    @PATCH
    @Path("{path: .+}")
    Response doPatch(
            @PathParam("path") @Encoded String path,
            @RestHeader MultivaluedMap<String, String> headers,
            String jsonBody
    );

    @DELETE
    @Path("{path: .+}")
    Response doDelete(
            @PathParam("path") @Encoded String path,
            @RestHeader MultivaluedMap<String, String> headers
    );
}
