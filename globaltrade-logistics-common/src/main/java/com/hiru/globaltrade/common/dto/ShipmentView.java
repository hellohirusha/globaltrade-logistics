package com.hiru.globaltrade.common.dto;

import com.hiru.globaltrade.common.enums.ShipmentPriority;
import com.hiru.globaltrade.common.enums.ShipmentStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record ShipmentView(
        Long id,
        String reference,
        String origin,
        String destination,
        String carrier,
        String vendorCode,
        ShipmentStatus status,
        ShipmentPriority priority,
        String customsReference,
        int riskScore,
        LocalDateTime estimatedDelivery,
        Instant lastUpdated
) {
}
