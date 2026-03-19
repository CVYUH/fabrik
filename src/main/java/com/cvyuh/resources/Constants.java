package com.cvyuh.resources;

import java.util.List;

public final class Constants {

    private static final String API = "/api/";

    public static final class V0 {
        private static final String ROOT = API + "v0/";
        public static final String AM = ROOT + "am";
        public static final String KEYS = ROOT + "keys";
        public static final String DEBUG = ROOT + "debug";
    }

    // V1 when needed:
    // public static final class V1 {
    //     private static final String ROOT = API + "v1/";
    //     public static final String AM = ROOT + "am";
    // }

    /** AM shard internal hosts — derived from AM_SHARD_HOSTS env or BASE_DOMAIN. */
    public static List<String> amShardHosts() {
        String env = System.getenv("AM_SHARD_HOSTS");
        if (env != null && !env.isBlank()) {
            return List.of(env.split(","));
        }
        String baseDomain = System.getenv("BASE_DOMAIN");
        String tld = baseDomain != null ? baseDomain : "cvyuh.local";
        boolean dcMode = "true".equalsIgnoreCase(System.getenv("DC_MODE"));
        return dcMode
                ? List.of("am.int." + tld)
                : List.of("am-0.int." + tld, "am-1.int." + tld);
    }
}
