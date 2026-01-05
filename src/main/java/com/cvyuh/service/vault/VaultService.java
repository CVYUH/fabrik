package com.cvyuh.service.vault;

import com.cvyuh.utils.exception.ExceptionWrapper;
import com.cvyuh.utils.log.LoggingFilter;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/")
@RegisterProvider(LoggingFilter.class)
@RegisterProvider(ExceptionWrapper.class)
@RegisterRestClient
public interface VaultService {
    @GET
    @Path("{path: .+}")
    Response doGet(
            @PathParam("path") @Encoded String path,
            @BeanParam VaultHeader header,
            @BeanParam VaultQuery query
    );

    @POST
    @Path("{path: .+}")
    Response doPost(
            @PathParam("path") @Encoded String path,
            @BeanParam VaultHeader header,
            @BeanParam VaultQuery query,
            String jsonBody
    );

    @PUT
    @Path("{path: .+}")
    Response doPut(
            @PathParam("path") @Encoded String path,
            @BeanParam VaultHeader header,
            String jsonBody
    );

    @PATCH
    @Path("{path: .+}")
    Response doPatch(
            @PathParam("path") @Encoded String path,
            @BeanParam VaultHeader header,
            String jsonBody
    );

    @DELETE
    @Path("{path: .+}")
    Response doDelete(
            @PathParam("path") @Encoded String path,
            @BeanParam VaultHeader header
    );
}
