package com.cvyuh.resources.am;

import jakarta.ws.rs.HeaderParam;

public class AMHeader {
    @HeaderParam("X-OpenAM-Username") String username;
    @HeaderParam("X-OpenAM-Password") String password;
    @HeaderParam("Accept-API-Version") String acceptApiVersion;
    @HeaderParam("Cookie") String cookie;
    @HeaderParam("Authorization") String authorization;

    public String getUsername () {
        return username;
    }

    public void setUsername ( String username ) {
        this.username = username;
    }

    public String getPassword () {
        return password;
    }

    public void setPassword ( String password ) {
        this.password = password;
    }

    public String getAcceptApiVersion () {
        return acceptApiVersion;
    }

    public void setAcceptApiVersion ( String acceptApiVersion ) {
        this.acceptApiVersion = acceptApiVersion;
    }

    public String getCookie () {
        return cookie;
    }

    public void setCookie ( String cookie ) {
        this.cookie = cookie;
    }

    public String getAuthorization () {
        return authorization;
    }

    public void setAuthorization ( String authorization ) {
        this.authorization = authorization;
    }

    public AMHeader merge(AMHeader other) {
        if (other == null) {
            return this;
        }

        AMHeader merged = new AMHeader();
        merged.setAuthorization(other.authorization != null ? other.authorization : this.authorization);
        merged.setCookie(other.cookie != null ? other.cookie : this.cookie);
        merged.setAcceptApiVersion(other.acceptApiVersion != null ? other.acceptApiVersion : this.acceptApiVersion);
        merged.setUsername(other.username != null ? other.username : this.username);
        merged.setPassword(other.password != null ? other.password : this.password);

        return merged;
    }

}
