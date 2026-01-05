package com.cvyuh.service.am;

import jakarta.ws.rs.QueryParam;

public class AMQuery {

    @QueryParam("_action") private String action;
    @QueryParam("_queryId") private String queryId;
    @QueryParam("_queryFilter") private String queryFilter;
    @QueryParam("tokenId") private String tokenId;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String getQueryFilter() {
        return queryFilter;
    }

    public void setQueryFilter(String queryFilter) {
        this.queryFilter = queryFilter;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
}
