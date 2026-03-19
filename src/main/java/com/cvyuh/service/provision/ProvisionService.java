package com.cvyuh.service.provision;

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
public interface ProvisionService {

    @GET
    @Path("{path: .+}")
    Response doGet(@PathParam("path") @Encoded String path);

    @POST
    @Path("{path: .+}")
    Response doPost(@PathParam("path") @Encoded String path, String jsonBody);

    @DELETE
    @Path("{path: .+}")
    Response doDelete(@PathParam("path") @Encoded String path);
}
