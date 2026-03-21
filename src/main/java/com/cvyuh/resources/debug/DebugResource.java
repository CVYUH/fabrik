package com.cvyuh.resources.debug;

import com.cvyuh.resources.Constants;
import com.cvyuh.service.am.AMPaths;
import com.cvyuh.service.am.AMService;
import com.cvyuh.service.am.AMShardUtils;
import com.cvyuh.utils.log.LoggingContext;
import com.cvyuh.utils.response.HttpMethod;
import com.cvyuh.utils.response.ResponseHandler;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.MDC;

import java.util.List;

/**
 * Shard-aware debug endpoint.
 * POST /api/v0/debug/{shard}?_action=status|elevate|restore
 * Shard: "am-0", "am-1", etc. Routes to specific AM instance via amlbcookie.
 */
@Path(Constants.V0.DEBUG)
public class DebugResource implements ResponseHandler {

    @Inject @RestClient AMService amService;

    private void preProcess() {
        MDC.put(LoggingContext.SERVICE, DebugResource.class.getName());
    }

    @POST
    @Path("/{shard}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response debugShard(
            @PathParam("shard") String shard,
            @QueryParam("_action") String action,
            @Context HttpHeaders httpHeaders,
            String body
    ) {
        preProcess();
        return handleResponse(
                v -> {
                    MultivaluedMap<String, String> headers = AMShardUtils.shardHeaders(shard, httpHeaders.getRequestHeaders());

                    MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
                    if (action != null) query.putSingle("_action", action);

                    return amService.doPost(AMPaths.DEBUG, headers, query, body);
                },
                "debug/" + shard, HttpMethod.POST, new MultivaluedHashMap<>()
        );
    }

}
