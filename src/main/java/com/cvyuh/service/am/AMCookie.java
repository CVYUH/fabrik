package com.cvyuh.service.am;

import com.cvyuh.resources.Constants;
import jakarta.ws.rs.CookieParam;

public class AMCookie {

    @CookieParam(Constants.COOKIE.AM) private String am;
    @CookieParam(Constants.COOKIE.AM_I18) private String amI18;
    @CookieParam(Constants.COOKIE.AM_LB) private String amLB;
    @CookieParam(Constants.COOKIE.AM_AUTH) private String amAuth;

    public String getAm() {
        return am;
    }

    public void setAm(String am) {
        this.am = am;
    }

    public String getAmI18() {
        return amI18;
    }

    public void setAmI18(String amI18) {
        this.amI18 = amI18;
    }

    public String getAmLB() {
        return amLB;
    }

    public void setAmLB(String amLB) {
        this.amLB = amLB;
    }

    public String getAmAuth() {
        return amAuth;
    }

    public void setAmAuth(String amAuth) {
        this.amAuth = amAuth;
    }
}
