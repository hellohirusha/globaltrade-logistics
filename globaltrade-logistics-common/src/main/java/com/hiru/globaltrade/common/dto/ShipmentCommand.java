package com.hiru.globaltrade.common.dto;

import com.hiru.globaltrade.common.enums.ShipmentPriority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ShipmentCommand(
        @NotBlank String reference,
        @NotBlank String origin,
        @NotBlank String destination,
        @NotBlank String carrier,
        @NotBlank String vendorCode,
        @NotNull ShipmentPriority priority,
        @NotBlank String customsReference,
        @NotNull @FutureOrPresent LocalDateTime estimatedDelivery
) {
}
