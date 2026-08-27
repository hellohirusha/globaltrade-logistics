package com.hiru.globaltrade.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InventoryItemCommand(
        @NotBlank String sku,
        @NotBlank String name,
        @NotBlank String warehouseCode,
        @NotNull @Min(0) Integer quantityOnHand,
        @NotNull @Min(1) Integer reorderPoint,
        @NotNull @Min(1) Integer reorderQuantity
) {
}
