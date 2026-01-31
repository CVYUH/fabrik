package com.cvyuh.utils.branding;

import java.util.Map;

public final class AcmeBrandResolver implements BrandResolver {

    @Override
    public Map<String, String> brandingFor(String realm) {
        if ("acme".equals(realm)) {
            return Map.of(
                    "OpenAM", "ACME",
                    "ForgeRock", "ACME"
            );
        }
        return Map.of();
    }
}
