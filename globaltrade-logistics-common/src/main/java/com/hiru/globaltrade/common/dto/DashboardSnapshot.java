package com.hiru.globaltrade.common.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record DashboardSnapshot(
        long activeShipments,
        long delayedShipments,
        long customsReviews,
        long lowStockItems,
        long openCriticalAlerts,
        long watchlistVendors,
        BigDecimal onTimeDeliveryRate,
        BigDecimal averageVendorScore,
        BigDecimal transactionSuccessRate,
        Instant generatedAt,
        List<ShipmentView> priorityShipments,
        List<InventoryView> inventorySignals,
        List<AlertView> openAlerts,
        List<PerformanceSample> performanceSamples
) {
}
