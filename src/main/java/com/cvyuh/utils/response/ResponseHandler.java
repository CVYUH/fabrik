package com.cvyuh.utils.response;

import com.cvyuh.utils.exception.ExceptionUtil;
import com.cvyuh.utils.misc.Timed;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.function.Function;

public interface ResponseHandler {

    Logger logger = Logger.getLogger(ResponseHandler.class);

    static String readEntity(Response resp) {
        try {
            if (resp == null) {
                return "No response";
            }

            if (resp.hasEntity()) {
                try {
                    return resp.readEntity(String.class);
                } catch (IllegalStateException ignore) {
                    Object entity = resp.getEntity();
                    return entity != null ? entity.toString() : "No message";
                }
            }
        } catch (Exception e) {
            return "No message (entity already consumed)";
        }

        return "No message";
    }

    // direct ui -> quarkus -> client -> quarkus rewriteIfNeeded -> ui
    default Response handleResponse(Function<Void, Response> serviceCall, String path, HttpMethod method, MultivaluedMap<String, String> requestQuery) {
        try {
            Timed.Result<Response> service = Timed.run(()
                    -> serviceCall.apply(null));

            Timed.Result<Response> rewritten = Timed.run(()
                    -> ResponseRewrite.rewriteIfNeeded(service.value(), path, method, requestQuery));

            long overall = service.elapsedMs() + rewritten.elapsedMs();
            if(logger.isDebugEnabled()) {
                logger.debugf(
                        "Timings | rewrite=%dms service=%dms total=%dms | %s %s",
                        rewritten.elapsedMs(),
                        service.elapsedMs(),
                        overall,
                        method,
                        path
                );
            }
            return rewritten.value();
        } catch (Exception e) {
            return ExceptionUtil.handleException(e, path);
        }
    }

    // intercepted ui -> quarkus -> quarkus [polyglot {js <-> java}] -> client -> quarkus -> ui
    default Response handleResponseRouterAware(
            Function<Void, Response> serviceCall,
            String method,
            String path,
            String serviceRouter    // TODO: ServiceRouter serviceRouter
    ) {
        /*
        try {
            Response response = serviceCall.apply(null);
            if(serviceRouter != null) {
                serviceRouter.interceptResponse(response, method, path);
            }
            return response;
        } catch (Exception e) {
            return ExceptionUtil.handleException(e, path);
        }
         */
        return null;
    }
}
