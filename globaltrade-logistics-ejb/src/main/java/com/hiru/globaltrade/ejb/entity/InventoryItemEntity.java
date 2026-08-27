package com.hiru.globaltrade.ejb.entity;

import com.hiru.globaltrade.common.enums.InventoryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "inventory_items", uniqueConstraints = @UniqueConstraint(columnNames = {"sku", "warehouseCode"}))
public class InventoryItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String sku;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 40)
    private String warehouseCode;

    @Column(nullable = false)
    private int quantityOnHand;

    @Column(nullable = false)
    private int reorderPoint;

    @Column(nullable = false)
    private int reorderQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private InventoryStatus status = InventoryStatus.HEALTHY;

    @Column(nullable = false)
    private Instant lastUpdated;

    @Version
    private long version;

    @PrePersist
    @PreUpdate
    void timestamp() {
        lastUpdated = Instant.now();
        if (quantityOnHand <= 0) {
            status = InventoryStatus.STOCKOUT;
        } else if (quantityOnHand <= reorderPoint) {
            status = InventoryStatus.REPLENISHMENT_DUE;
        } else if (quantityOnHand <= reorderPoint * 2) {
            status = InventoryStatus.LOW_STOCK;
        } else {
            status = InventoryStatus.HEALTHY;
        }
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public int getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(int quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public int getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(int reorderPoint) {
        this.reorderPoint = reorderPoint;
    }

    public int getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(int reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    public InventoryStatus getStatus() {
        return status;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }
}
