package com.hiru.globaltrade.ejb.service;

import com.hiru.globaltrade.common.dto.InventoryAdjustmentCommand;
import com.hiru.globaltrade.common.dto.InventoryItemCommand;
import com.hiru.globaltrade.common.dto.InventoryView;
import com.hiru.globaltrade.common.enums.AlertSeverity;
import com.hiru.globaltrade.common.enums.InventoryStatus;
import com.hiru.globaltrade.common.exception.BusinessRuleException;
import com.hiru.globaltrade.common.exception.ResourceNotFoundException;
import com.hiru.globaltrade.common.security.LogisticsRoles;
import com.hiru.globaltrade.common.service.InventoryService;
import com.hiru.globaltrade.ejb.entity.AlertEntity;
import com.hiru.globaltrade.ejb.entity.InventoryItemEntity;
import com.hiru.globaltrade.ejb.interceptor.AuditInterceptor;
import com.hiru.globaltrade.ejb.interceptor.PerformanceInterceptor;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
@RolesAllowed({LogisticsRoles.ADMIN, LogisticsRoles.COORDINATOR, LogisticsRoles.WAREHOUSE_MANAGER})
@Interceptors({AuditInterceptor.class, PerformanceInterceptor.class})
public class InventoryServiceBean implements InventoryService {
    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager entityManager;

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<InventoryView> findAll() {
        return entityManager.createQuery("select i from InventoryItemEntity i order by i.status desc, i.sku asc", InventoryItemEntity.class)
                .getResultList()
                .stream()
                .map(MappingSupport::inventory)
                .toList();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public InventoryView create(InventoryItemCommand command) {
        if (exists(command.sku(), command.warehouseCode())) {
            throw new BusinessRuleException("Inventory item already exists for this warehouse.");
        }
        InventoryItemEntity item = new InventoryItemEntity();
        item.setSku(command.sku());
        item.setName(command.name());
        item.setWarehouseCode(command.warehouseCode());
        item.setQuantityOnHand(command.quantityOnHand());
        item.setReorderPoint(command.reorderPoint());
        item.setReorderQuantity(command.reorderQuantity());
        entityManager.persist(item);
        entityManager.flush();
        return MappingSupport.inventory(item);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public InventoryView adjust(InventoryAdjustmentCommand command) {
        InventoryItemEntity item = find(command.sku(), command.warehouseCode());
        item.setQuantityOnHand(command.quantityOnHand());
        item.setReorderPoint(command.reorderPoint());
        item.setReorderQuantity(command.reorderQuantity());
        entityManager.flush();
        if (item.getStatus() == InventoryStatus.STOCKOUT || item.getStatus() == InventoryStatus.REPLENISHMENT_DUE) {
            alert(AlertSeverity.CRITICAL, "Inventory replenishment required", item.getSku() + " at " + item.getWarehouseCode() + " is " + item.getStatus());
        }
        return MappingSupport.inventory(item);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public int monitorReplenishment() {
        List<InventoryItemEntity> items = entityManager.createQuery("""
                        select i from InventoryItemEntity i
                        where i.quantityOnHand <= i.reorderPoint
                        """, InventoryItemEntity.class)
                .getResultList();
        items.forEach(item -> alert(
                AlertSeverity.WARNING,
                "Automated replenishment signal",
                item.getSku() + " should reorder " + item.getReorderQuantity() + " units."
        ));
        return items.size();
    }

    private InventoryItemEntity find(String sku, String warehouseCode) {
        List<InventoryItemEntity> items = entityManager.createQuery("""
                        select i from InventoryItemEntity i
                        where i.sku = :sku and i.warehouseCode = :warehouseCode
                        """, InventoryItemEntity.class)
                .setParameter("sku", sku)
                .setParameter("warehouseCode", warehouseCode)
                .setMaxResults(1)
                .getResultList();
        if (items.isEmpty()) {
            throw new ResourceNotFoundException("Inventory item", sku + " at " + warehouseCode);
        }
        return items.get(0);
    }

    private boolean exists(String sku, String warehouseCode) {
        return entityManager.createQuery("""
                        select count(i) from InventoryItemEntity i
                        where i.sku = :sku and i.warehouseCode = :warehouseCode
                        """, Long.class)
                .setParameter("sku", sku)
                .setParameter("warehouseCode", warehouseCode)
                .getSingleResult() > 0;
    }

    private void alert(AlertSeverity severity, String title, String message) {
        AlertEntity alert = new AlertEntity();
        alert.setSeverity(severity);
        alert.setTitle(title);
        alert.setMessage(message);
        entityManager.persist(alert);
    }
}
