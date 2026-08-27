package com.hiru.globaltrade.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VendorCommand(
        @NotBlank String vendorCode,
        @NotBlank String name,
        @NotBlank String country,
        @NotNull @Min(0) @Max(100) Integer score,
        boolean active
) {
}
