package com.hiru.globaltrade.ejb.service;

import com.hiru.globaltrade.common.dto.ShipmentCommand;
import com.hiru.globaltrade.common.dto.ShipmentStatusCommand;
import com.hiru.globaltrade.common.dto.ShipmentView;
import com.hiru.globaltrade.common.enums.AlertSeverity;
import com.hiru.globaltrade.common.enums.ShipmentStatus;
import com.hiru.globaltrade.common.exception.BusinessRuleException;
import com.hiru.globaltrade.common.exception.ResourceNotFoundException;
import com.hiru.globaltrade.common.security.LogisticsRoles;
import com.hiru.globaltrade.common.service.ShipmentService;
import com.hiru.globaltrade.ejb.entity.AlertEntity;
import com.hiru.globaltrade.ejb.entity.ShipmentEntity;
import com.hiru.globaltrade.ejb.entity.VendorEntity;
import com.hiru.globaltrade.ejb.interceptor.AuditInterceptor;
import com.hiru.globaltrade.ejb.interceptor.PerformanceInterceptor;
import com.hiru.globaltrade.ejb.interceptor.VendorValidationInterceptor;
import com.hiru.globaltrade.ejb.policy.RiskScoringPolicy;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
@RolesAllowed({LogisticsRoles.ADMIN, LogisticsRoles.COORDINATOR, LogisticsRoles.CUSTOMS_AGENT})
@Interceptors({AuditInterceptor.class, PerformanceInterceptor.class, VendorValidationInterceptor.class})
public class ShipmentServiceBean implements ShipmentService {
    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager entityManager;

    private final RiskScoringPolicy riskScoringPolicy = new RiskScoringPolicy();

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public ShipmentView create(ShipmentCommand command) {
        if (findByReferenceOrNull(command.reference()) != null) {
            throw new BusinessRuleException("Shipment reference already exists: " + command.reference());
        }
        VendorEntity vendor = findVendor(command.vendorCode());
        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setReference(command.reference());
        shipment.setOrigin(command.origin());
        shipment.setDestination(command.destination());
        shipment.setCarrier(command.carrier());
        shipment.setVendor(vendor);
        shipment.setPriority(command.priority());
        shipment.setCustomsReference(command.customsReference());
        shipment.setEstimatedDelivery(command.estimatedDelivery());
        shipment.setRiskScore(riskScoringPolicy.score(command.priority(), vendor.getTier(), command.estimatedDelivery()));
        entityManager.persist(shipment);
        if (shipment.getRiskScore() >= 70) {
            alert(AlertSeverity.CRITICAL, "High risk shipment created", command.reference() + " requires immediate operations review.");
        }
        return MappingSupport.shipment(shipment);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public ShipmentView updateStatus(ShipmentStatusCommand command) {
        ShipmentEntity shipment = findByReference(command.reference());
        shipment.setStatus(command.status());
        shipment.setRiskScore(riskScoringPolicy.score(shipment.getPriority(), shipment.getVendor().getTier(), shipment.getEstimatedDelivery()));
        if (command.status() == ShipmentStatus.DELAYED || command.status() == ShipmentStatus.CUSTOMS_REVIEW) {
            alert(AlertSeverity.WARNING, "Shipment status requires attention", command.reference() + ": " + command.reason());
        }
        return MappingSupport.shipment(shipment);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<ShipmentView> findActiveShipments() {
        return entityManager.createQuery("""
                        select s from ShipmentEntity s
                        where s.status not in (:delivered, :cancelled)
                        order by s.priority desc, s.estimatedDelivery asc
                        """, ShipmentEntity.class)
                .setParameter("delivered", ShipmentStatus.DELIVERED)
                .setParameter("cancelled", ShipmentStatus.CANCELLED)
                .setMaxResults(100)
                .getResultList()
                .stream()
                .map(MappingSupport::shipment)
                .toList();
    }

    @Override
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

    private VendorEntity findVendor(String vendorCode) {
        try {
            return entityManager.createQuery("select v from VendorEntity v where v.vendorCode = :code and v.active = true", VendorEntity.class)
                    .setParameter("code", vendorCode)
                    .getSingleResult();
        } catch (NoResultException ex) {
            throw new ResourceNotFoundException("Vendor", vendorCode);
        }
    }

    private ShipmentEntity findByReference(String reference) {
        ShipmentEntity shipment = findByReferenceOrNull(reference);
        if (shipment == null) {
            throw new ResourceNotFoundException("Shipment", reference);
        }
        return shipment;
    }

    private ShipmentEntity findByReferenceOrNull(String reference) {
        List<ShipmentEntity> shipments = entityManager.createQuery("select s from ShipmentEntity s where s.reference = :reference", ShipmentEntity.class)
                .setParameter("reference", reference)
                .setMaxResults(1)
                .getResultList();
        return shipments.isEmpty() ? null : shipments.get(0);
    }

    private void alert(AlertSeverity severity, String title, String message) {
        AlertEntity alert = new AlertEntity();
        alert.setSeverity(severity);
        alert.setTitle(title);
        alert.setMessage(message);
        entityManager.persist(alert);
    }
}
