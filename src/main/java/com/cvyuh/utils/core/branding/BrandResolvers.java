package com.cvyuh.utils.core.branding;

public final class BrandResolvers {
    private static final BrandResolver DEFAULT = new DefaultBrandResolver();
    public static BrandResolver current() { return DEFAULT; }
}
