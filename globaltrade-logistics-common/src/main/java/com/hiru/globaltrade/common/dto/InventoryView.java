package com.hiru.globaltrade.common.dto;

import com.hiru.globaltrade.common.enums.InventoryStatus;

import java.time.Instant;

public record InventoryView(
        Long id,
        String sku,
        String name,
        String warehouseCode,
        int quantityOnHand,
        int reorderPoint,
        int reorderQuantity,
        InventoryStatus status,
        Instant lastUpdated
) {
}
