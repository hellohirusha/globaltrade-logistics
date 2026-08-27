package com.hiru.globaltrade.common.dto;

import com.hiru.globaltrade.common.enums.ShipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShipmentStatusCommand(
        @NotBlank String reference,
        @NotNull ShipmentStatus status,
        @NotBlank String reason
) {
}
