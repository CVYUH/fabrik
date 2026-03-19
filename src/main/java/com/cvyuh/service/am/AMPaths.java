package com.cvyuh.service.am;

/**
 * AM REST API paths — used by Fabrik when calling AM via AMService.
 * These are the paths after stripping the /ctrl prefix (what AMService sees).
 * Mirror of Provision's AMConstants.URLs but for Fabrik's context.
 */
public final class AMPaths {

    private static final String ROOT = "json/";
    private static final String GLOBAL = ROOT + "global-config/";
    private static final String REALM = ROOT + "realms/{realm}/";

    private AMPaths() {}

    // Global (rootRouter)
    public static final String DEBUG = ROOT + "debug";
    public static final String KEYSTORE = ROOT + "keystore";
    public static final String ENTITLEMENT_SETUP = ROOT + "entitlement/setup";
    public static final String REALMS = GLOBAL + "realms";
    public static final String SERVERS = GLOBAL + "servers";

    // Realm-scoped (realmRouter)
    public static final String REALM_KEYSTORE = REALM + "keystore";

    public static String realm(String path, String realmName) {
        return path.replace("{realm}", realmName);
    }
}
