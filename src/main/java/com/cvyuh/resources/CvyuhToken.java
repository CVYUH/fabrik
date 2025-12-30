package com.cvyuh.resources;

import jakarta.ws.rs.CookieParam;

public class CvyuhToken {

    @CookieParam(Constants.TOKEN.CVYUH) private String cvyuhToken;

    public String getCvyuhToken() {
        return cvyuhToken;
    }

    public void setCvyuhToken(String cvyuhToken) {
        this.cvyuhToken = cvyuhToken;
    }
}
