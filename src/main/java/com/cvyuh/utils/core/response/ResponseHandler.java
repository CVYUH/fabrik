package com.cvyuh.utils.core.response;

import com.cvyuh.utils.core.HttpMethod;
import com.cvyuh.utils.exception.ExceptionUtil;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.util.function.Function;

public interface ResponseHandler {

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
            Response response = serviceCall.apply(null);
            return ResponseRewrite.rewriteIfNeeded(response, path, method, requestQuery);
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
