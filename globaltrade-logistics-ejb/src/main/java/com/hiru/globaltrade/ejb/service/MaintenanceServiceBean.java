package com.hiru.globaltrade.ejb.service;

import com.hiru.globaltrade.common.enums.AlertSeverity;
import com.hiru.globaltrade.common.enums.ShipmentStatus;
import com.hiru.globaltrade.common.enums.VendorTier;
import com.hiru.globaltrade.ejb.entity.AlertEntity;
import com.hiru.globaltrade.ejb.entity.InventoryItemEntity;
import com.hiru.globaltrade.ejb.entity.ShipmentEntity;
import com.hiru.globaltrade.ejb.entity.VendorEntity;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
@PermitAll
public class MaintenanceServiceBean {
    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager entityManager;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public int monitorShipmentDelays() {
        List<ShipmentEntity> late = entityManager.createQuery("""
                        select s from ShipmentEntity s
                        where s.estimatedDelivery < :now
                          and s.status not in (:delivered, :cancelled, :delayed)
                        """, ShipmentEntity.class)
                .setParameter("now", LocalDateTime.now())
                .setParameter("delivered", ShipmentStatus.DELIVERED)
                .setParameter("cancelled", ShipmentStatus.CANCELLED)
                .setParameter("delayed", ShipmentStatus.DELAYED)
                .getResultList();
        late.forEach(shipment -> {
            shipment.setStatus(ShipmentStatus.DELAYED);
            alert(AlertSeverity.CRITICAL, "Shipment delay detected", shipment.getReference() + " missed the promised delivery window.");
        });
        return late.size();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public int monitorReplenishment() {
        List<InventoryItemEntity> items = entityManager.createQuery("""
                        select i from InventoryItemEntity i
                        where i.quantityOnHand <= i.reorderPoint
                        """, InventoryItemEntity.class)
                .getResultList();
        items.forEach(item -> alert(AlertSeverity.WARNING, "Automated replenishment signal", item.getSku() + " should reorder " + item.getReorderQuantity() + " units."));
        return items.size();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public int refreshVendorTiers() {
        List<VendorEntity> vendors = entityManager.createQuery("select v from VendorEntity v where v.active = true", VendorEntity.class)
                .getResultList();
        vendors.forEach(vendor -> vendor.setTier(tier(vendor.getScore())));
        return vendors.size();
    }

    private VendorTier tier(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return VendorTier.STRATEGIC;
        }
        if (score.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return VendorTier.APPROVED;
        }
        if (score.compareTo(BigDecimal.valueOf(45)) >= 0) {
            return VendorTier.WATCHLIST;
        }
        return VendorTier.SUSPENDED;
    }

    private void alert(AlertSeverity severity, String title, String message) {
        AlertEntity alert = new AlertEntity();
        alert.setSeverity(severity);
        alert.setTitle(title);
        alert.setMessage(message);
        entityManager.persist(alert);
    }
}
