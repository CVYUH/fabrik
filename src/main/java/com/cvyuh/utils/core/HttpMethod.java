package com.cvyuh.utils.core;

public enum HttpMethod {

    GET,
    POST,
    PUT,
    PATCH,
    DELETE;

    public static HttpMethod from( io.vertx.core.http.HttpMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("HttpMethod cannot be null");
        }

        // MUST switch on method.name(), not method
        return switch (method.name()) {
            case "GET"    -> GET;
            case "POST"   -> POST;
            case "PUT"    -> PUT;
            case "PATCH"  -> PATCH;
            case "DELETE" -> DELETE;

            default -> throw new IllegalArgumentException(
                    "Unsupported HTTP method for headless calls: " + method.name()
            );
        };
    }

    public static HttpMethod from( String name) {
        return HttpMethod.valueOf(name.toUpperCase());
    }
}
