package com.cvyuh.utils.branding;

public final class BrandResolvers {
    private static final BrandResolver DEFAULT = new DefaultBrandResolver();
    public static BrandResolver current() { return DEFAULT; }
}
