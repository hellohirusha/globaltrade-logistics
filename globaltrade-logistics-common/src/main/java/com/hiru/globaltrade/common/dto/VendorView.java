package com.hiru.globaltrade.common.dto;

import com.hiru.globaltrade.common.enums.VendorTier;

import java.math.BigDecimal;
import java.time.Instant;

public record VendorView(
        Long id,
        String vendorCode,
        String name,
        String country,
        BigDecimal score,
        VendorTier tier,
        boolean active,
        Instant lastEvaluated
) {
}
