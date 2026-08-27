package com.hiru.globaltrade.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VendorScoreCommand(
        @NotBlank String vendorCode,
        @NotNull @Min(0) @Max(100) Integer onTimeScore,
        @NotNull @Min(0) @Max(100) Integer complianceScore,
        @NotNull @Min(0) @Max(100) Integer disruptionScore
) {
}
