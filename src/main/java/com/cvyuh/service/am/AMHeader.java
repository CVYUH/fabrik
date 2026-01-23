package com.cvyuh.service.am;

import jakarta.ws.rs.HeaderParam;

public class AMHeader {
    @HeaderParam("X-OpenAM-Username") String username;
    @HeaderParam("X-OpenAM-Password") String password;

    @HeaderParam("cookie") String cookie;
    @HeaderParam("authorization") String authorization;

    @HeaderParam("Host") String host;
    @HeaderParam("Referer") String referer;
    @HeaderParam("Accept-API-Version") String acceptAPIVersion;
    @HeaderParam("X-Forwarded-For") String xForwardedFor;
    @HeaderParam("X-Forwarded-Proto") String xForwardedProto;
    @HeaderParam("User-Agent") String userAgent;

    @HeaderParam("X-Request-Id") String xRequestId;
    @HeaderParam("X-Correlation-Id") String xCorrelationId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getAcceptAPIVersion() {
        return acceptAPIVersion;
    }

    public void setAcceptAPIVersion(String acceptAPIVersion) {
        this.acceptAPIVersion = acceptAPIVersion;
    }

    public String getXForwardedFor() {
        return xForwardedFor;
    }

    public void setXForwardedFor(String xForwardedFor) {
        this.xForwardedFor = xForwardedFor;
    }

    public String getXForwardedProto() {
        return xForwardedProto;
    }

    public void setXForwardedProto(String xForwardedProto) {
        this.xForwardedProto = xForwardedProto;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getXRequestId() {
        return xRequestId;
    }

    public void setXRequestId(String xRequestId) {
        this.xRequestId = xRequestId;
    }

    public String getXCorrelationId() {
        return xCorrelationId;
    }

    public void setXCorrelationId(String xCorrelationId) {
        this.xCorrelationId = xCorrelationId;
    }
}
