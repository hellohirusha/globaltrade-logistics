package com.hiru.globaltrade.ejb.service;

import com.hiru.globaltrade.common.dto.AlertView;
import com.hiru.globaltrade.common.dto.ComplianceAuditView;
import com.hiru.globaltrade.common.dto.InventoryView;
import com.hiru.globaltrade.common.dto.PerformanceSample;
import com.hiru.globaltrade.common.dto.ShipmentView;
import com.hiru.globaltrade.common.dto.VendorView;
import com.hiru.globaltrade.ejb.entity.AlertEntity;
import com.hiru.globaltrade.ejb.entity.ComplianceAuditEntity;
import com.hiru.globaltrade.ejb.entity.InventoryItemEntity;
import com.hiru.globaltrade.ejb.entity.PerformanceMetricEntity;
import com.hiru.globaltrade.ejb.entity.ShipmentEntity;
import com.hiru.globaltrade.ejb.entity.VendorEntity;

final class MappingSupport {
    private MappingSupport() {
    }

    static ShipmentView shipment(ShipmentEntity entity) {
        return new ShipmentView(
                entity.getId(),
                entity.getReference(),
                entity.getOrigin(),
                entity.getDestination(),
                entity.getCarrier(),
                entity.getVendor().getVendorCode(),
                entity.getStatus(),
                entity.getPriority(),
                entity.getCustomsReference(),
                entity.getRiskScore(),
                entity.getEstimatedDelivery(),
                entity.getLastUpdated()
        );
    }

    static VendorView vendor(VendorEntity entity) {
        return new VendorView(
                entity.getId(),
                entity.getVendorCode(),
                entity.getName(),
                entity.getCountry(),
                entity.getScore(),
                entity.getTier(),
                entity.isActive(),
                entity.getLastEvaluated()
        );
    }

    static InventoryView inventory(InventoryItemEntity entity) {
        return new InventoryView(
                entity.getId(),
                entity.getSku(),
                entity.getName(),
                entity.getWarehouseCode(),
                entity.getQuantityOnHand(),
                entity.getReorderPoint(),
                entity.getReorderQuantity(),
                entity.getStatus(),
                entity.getLastUpdated()
        );
    }

    static AlertView alert(AlertEntity entity) {
        return new AlertView(
                entity.getId(),
                entity.getSeverity(),
                entity.getTitle(),
                entity.getMessage(),
                entity.isAcknowledged(),
                entity.getRaisedAt()
        );
    }

    static ComplianceAuditView audit(ComplianceAuditEntity entity) {
        return new ComplianceAuditView(
                entity.getId(),
                entity.getActor(),
                entity.getAction(),
                entity.getResource(),
                entity.getOutcome(),
                entity.getIpAddress(),
                entity.getCreatedAt()
        );
    }

    static PerformanceSample performance(PerformanceMetricEntity entity) {
        return new PerformanceSample(entity.getOperation(), entity.getDurationMillis(), entity.getOutcome(), entity.getCapturedAt());
    }
}
