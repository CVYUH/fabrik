package com.cvyuh.resources.vault;

import jakarta.ws.rs.HeaderParam;

public class VaultHeader {

    @HeaderParam("X-Vault-Token")
    String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public VaultHeader merge(VaultHeader other) {
        if (other == null) return this;
        VaultHeader h = new VaultHeader();
        h.setToken(other.token != null ? other.token : this.token);
        return h;
    }
}

