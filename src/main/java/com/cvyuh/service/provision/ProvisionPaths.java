package com.cvyuh.service.provision;

/**
 * Provision REST API paths — used by Fabrik when calling Provision via ProvisionService.
 * Mirror of AMPaths but for Provision's context.
 */
public final class ProvisionPaths {

    private ProvisionPaths() {}

    private static final String KV_V2 = "kv/v2/";

    /** Tenant key paths: kv/v2/keys/{tenant}/{purpose} */
    public static final String KEYS = KV_V2 + "keys/";

    /** Stock key paths: kv/v2/keys/stock/{alias} */
    public static final String KEYS_STOCK = KEYS + "stock/";

    public static String tenantKey(String tenant, String purpose) {
        return KEYS + tenant + "/" + purpose;
    }

    public static String tenantKeys(String tenant) {
        return KEYS + tenant;
    }

    public static String stockKey(String alias) {
        return KEYS_STOCK + alias;
    }
}
