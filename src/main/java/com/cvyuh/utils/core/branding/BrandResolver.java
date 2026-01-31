package com.cvyuh.utils.core.branding;

import java.util.Map;

public interface BrandResolver {

    /**
     * Returns replacement map for this realm / tenant.
     * Example:
     *   OpenAM     -> ACME
     *   ForgeRock  -> ACME
     */
    Map<String, String> brandingFor(String realm);
}
