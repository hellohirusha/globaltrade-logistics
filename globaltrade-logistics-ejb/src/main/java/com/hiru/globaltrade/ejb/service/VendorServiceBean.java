package com.hiru.globaltrade.ejb.service;

import com.hiru.globaltrade.common.dto.VendorCommand;
import com.hiru.globaltrade.common.dto.VendorScoreCommand;
import com.hiru.globaltrade.common.dto.VendorView;
import com.hiru.globaltrade.common.enums.AlertSeverity;
import com.hiru.globaltrade.common.enums.VendorTier;
import com.hiru.globaltrade.common.exception.BusinessRuleException;
import com.hiru.globaltrade.common.exception.ResourceNotFoundException;
import com.hiru.globaltrade.common.security.LogisticsRoles;
import com.hiru.globaltrade.common.service.VendorService;
import com.hiru.globaltrade.ejb.entity.AlertEntity;
import com.hiru.globaltrade.ejb.entity.VendorEntity;
import com.hiru.globaltrade.ejb.interceptor.AuditInterceptor;
import com.hiru.globaltrade.ejb.interceptor.PerformanceInterceptor;
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
@RolesAllowed({LogisticsRoles.ADMIN, LogisticsRoles.COORDINATOR, LogisticsRoles.VENDOR_REPRESENTATIVE})
@Interceptors({AuditInterceptor.class, PerformanceInterceptor.class})
public class VendorServiceBean implements VendorService {
    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager entityManager;

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<VendorView> findAll() {
        return entityManager.createQuery("select v from VendorEntity v order by v.tier asc, v.score desc", VendorEntity.class)
                .getResultList()
                .stream()
                .map(MappingSupport::vendor)
                .toList();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public VendorView create(VendorCommand command) {
        if (exists(command.vendorCode())) {
            throw new BusinessRuleException("Vendor code already exists.");
        }
        VendorEntity vendor = new VendorEntity();
        vendor.setVendorCode(command.vendorCode());
        vendor.setName(command.name());
        vendor.setCountry(command.country());
        vendor.setScore(BigDecimal.valueOf(command.score()).setScale(2, RoundingMode.HALF_UP));
        vendor.setTier(tier(vendor.getScore()));
        vendor.setActive(command.active());
        vendor.setLastEvaluated(Instant.now());
        entityManager.persist(vendor);
        entityManager.flush();
        return MappingSupport.vendor(vendor);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public VendorView evaluate(VendorScoreCommand command) {
        VendorEntity vendor = find(command.vendorCode());
        BigDecimal score = BigDecimal.valueOf(command.onTimeScore() * 0.45d + command.complianceScore() * 0.35d + command.disruptionScore() * 0.20d)
                .setScale(2, RoundingMode.HALF_UP);
        vendor.setScore(score);
        vendor.setTier(tier(score));
        vendor.setLastEvaluated(Instant.now());
        if (vendor.getTier() == VendorTier.WATCHLIST || vendor.getTier() == VendorTier.SUSPENDED) {
            alert(AlertSeverity.WARNING, "Vendor performance risk", vendor.getVendorCode() + " moved to " + vendor.getTier() + " with score " + score);
        }
        return MappingSupport.vendor(vendor);
    }

    @Override
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

    private VendorEntity find(String vendorCode) {
        List<VendorEntity> vendors = entityManager.createQuery("select v from VendorEntity v where v.vendorCode = :code", VendorEntity.class)
                .setParameter("code", vendorCode)
                .setMaxResults(1)
                .getResultList();
        if (vendors.isEmpty()) {
            throw new ResourceNotFoundException("Vendor", vendorCode);
        }
        return vendors.get(0);
    }

    private boolean exists(String vendorCode) {
        return entityManager.createQuery("select count(v) from VendorEntity v where v.vendorCode = :code", Long.class)
                .setParameter("code", vendorCode)
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
