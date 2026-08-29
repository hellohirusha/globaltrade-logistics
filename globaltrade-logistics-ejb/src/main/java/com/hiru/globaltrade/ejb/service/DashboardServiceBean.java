package com.hiru.globaltrade.ejb.service;

import com.hiru.globaltrade.common.dto.AlertView;
import com.hiru.globaltrade.common.dto.DashboardSnapshot;
import com.hiru.globaltrade.common.dto.InventoryView;
import com.hiru.globaltrade.common.dto.PerformanceSample;
import com.hiru.globaltrade.common.dto.ShipmentView;
import com.hiru.globaltrade.common.enums.AlertSeverity;
import com.hiru.globaltrade.common.enums.InventoryStatus;
import com.hiru.globaltrade.common.enums.ShipmentStatus;
import com.hiru.globaltrade.common.enums.VendorTier;
import com.hiru.globaltrade.common.security.LogisticsRoles;
import com.hiru.globaltrade.common.service.DashboardService;
import com.hiru.globaltrade.ejb.entity.AlertEntity;
import com.hiru.globaltrade.ejb.entity.InventoryItemEntity;
import com.hiru.globaltrade.ejb.entity.PerformanceMetricEntity;
import com.hiru.globaltrade.ejb.entity.ShipmentEntity;
import com.hiru.globaltrade.ejb.interceptor.PerformanceInterceptor;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Stateless
@DeclareRoles({
        LogisticsRoles.ADMIN,
        LogisticsRoles.COORDINATOR,
        LogisticsRoles.WAREHOUSE_MANAGER,
        LogisticsRoles.CUSTOMS_AGENT,
        LogisticsRoles.VENDOR_REPRESENTATIVE,
        LogisticsRoles.CUSTOMER
})
@RolesAllowed({LogisticsRoles.ADMIN, LogisticsRoles.COORDINATOR, LogisticsRoles.WAREHOUSE_MANAGER, LogisticsRoles.CUSTOMS_AGENT})
@Interceptors(PerformanceInterceptor.class)
public class DashboardServiceBean implements DashboardService {
    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager entityManager;

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public DashboardSnapshot snapshot() {
        long activeShipments = count("""
                select count(s) from ShipmentEntity s
                where s.status not in (:delivered, :cancelled)
                """, ShipmentStatus.DELIVERED, ShipmentStatus.CANCELLED);
        long delayedShipments = countByShipmentStatus(ShipmentStatus.DELAYED);
        long customsReviews = countByShipmentStatus(ShipmentStatus.CUSTOMS_REVIEW);
        long lowStockItems = countByInventoryStatus(InventoryStatus.LOW_STOCK)
                + countByInventoryStatus(InventoryStatus.REPLENISHMENT_DUE)
                + countByInventoryStatus(InventoryStatus.STOCKOUT);
        long openCriticalAlerts = entityManager.createQuery("""
                        select count(a) from AlertEntity a
                        where a.acknowledged = false and a.severity = :severity
                        """, Long.class)
                .setParameter("severity", AlertSeverity.CRITICAL)
                .getSingleResult();
        long watchlistVendors = entityManager.createQuery("select count(v) from VendorEntity v where v.tier in (:watchlist, :suspended)", Long.class)
                .setParameter("watchlist", VendorTier.WATCHLIST)
                .setParameter("suspended", VendorTier.SUSPENDED)
                .getSingleResult();

        return new DashboardSnapshot(
                activeShipments,
                delayedShipments,
                customsReviews,
                lowStockItems,
                openCriticalAlerts,
                watchlistVendors,
                percentageDeliveredOnTime(),
                averageVendorScore(),
                transactionSuccessRate(),
                Instant.now(),
                priorityShipments(),
                inventorySignals(),
                openAlerts(),
                performanceSamples()
        );
    }

    private long count(String jpql, ShipmentStatus first, ShipmentStatus second) {
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("delivered", first)
                .setParameter("cancelled", second)
                .getSingleResult();
    }

    private long countByShipmentStatus(ShipmentStatus status) {
        return entityManager.createQuery("select count(s) from ShipmentEntity s where s.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    private long countByInventoryStatus(InventoryStatus status) {
        return entityManager.createQuery("select count(i) from InventoryItemEntity i where i.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    private BigDecimal percentageDeliveredOnTime() {
        long total = entityManager.createQuery("select count(s) from ShipmentEntity s", Long.class).getSingleResult();
        if (total == 0) {
            return BigDecimal.valueOf(100);
        }
        long delivered = countByShipmentStatus(ShipmentStatus.DELIVERED);
        return BigDecimal.valueOf(delivered * 100.0d / total).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal averageVendorScore() {
        Double average = entityManager.createQuery("select avg(v.score) from VendorEntity v where v.active = true", Double.class).getSingleResult();
        return average == null ? BigDecimal.ZERO : BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal transactionSuccessRate() {
        long total = entityManager.createQuery("select count(p) from PerformanceMetricEntity p", Long.class).getSingleResult();
        if (total == 0) {
            return BigDecimal.valueOf(100);
        }
        long successful = entityManager.createQuery("select count(p) from PerformanceMetricEntity p where p.outcome = 'SUCCESS'", Long.class).getSingleResult();
        return BigDecimal.valueOf(successful * 100.0d / total).setScale(2, RoundingMode.HALF_UP);
    }

    private List<ShipmentView> priorityShipments() {
        return entityManager.createQuery("""
                        select s from ShipmentEntity s
                        where s.status not in (:delivered, :cancelled)
                        order by s.riskScore desc, s.estimatedDelivery asc
                        """, ShipmentEntity.class)
                .setParameter("delivered", ShipmentStatus.DELIVERED)
                .setParameter("cancelled", ShipmentStatus.CANCELLED)
                .setMaxResults(8)
                .getResultList()
                .stream()
                .map(MappingSupport::shipment)
                .toList();
    }

    private List<InventoryView> inventorySignals() {
        return entityManager.createQuery("""
                        select i from InventoryItemEntity i
                        where i.status <> :healthy
                        order by i.status desc, i.quantityOnHand asc
                        """, InventoryItemEntity.class)
                .setParameter("healthy", InventoryStatus.HEALTHY)
                .setMaxResults(8)
                .getResultList()
                .stream()
                .map(MappingSupport::inventory)
                .toList();
    }

    private List<AlertView> openAlerts() {
        return entityManager.createQuery("select a from AlertEntity a where a.acknowledged = false order by a.raisedAt desc", AlertEntity.class)
                .setMaxResults(8)
                .getResultList()
                .stream()
                .map(MappingSupport::alert)
                .toList();
    }

    private List<PerformanceSample> performanceSamples() {
        return entityManager.createQuery("select p from PerformanceMetricEntity p order by p.capturedAt desc", PerformanceMetricEntity.class)
                .setMaxResults(12)
                .getResultList()
                .stream()
                .map(MappingSupport::performance)
                .toList();
    }
}
