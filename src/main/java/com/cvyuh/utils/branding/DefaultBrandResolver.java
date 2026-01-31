package com.cvyuh.utils.branding;

import java.util.Map;

public final class DefaultBrandResolver implements BrandResolver {

    @Override
    public Map<String, String> brandingFor(String realm) {
        // CVYUH as *default*, not hardcoded in rules
        return Map.of(
                "OpenAM", "CVYUH",
                "ForgeRock", "CVYUH"
        );
    }
}
