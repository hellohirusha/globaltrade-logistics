package com.hiru.globaltrade.ejb.service;

import com.hiru.globaltrade.common.dto.AlertView;
import com.hiru.globaltrade.common.exception.ResourceNotFoundException;
import com.hiru.globaltrade.common.security.LogisticsRoles;
import com.hiru.globaltrade.common.service.AlertService;
import com.hiru.globaltrade.ejb.entity.AlertEntity;
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
@RolesAllowed({LogisticsRoles.ADMIN, LogisticsRoles.COORDINATOR, LogisticsRoles.WAREHOUSE_MANAGER, LogisticsRoles.CUSTOMS_AGENT})
@Interceptors({AuditInterceptor.class, PerformanceInterceptor.class})
public class AlertServiceBean implements AlertService {
    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager entityManager;

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<AlertView> findOpenAlerts() {
        return entityManager.createQuery("select a from AlertEntity a where a.acknowledged = false order by a.raisedAt desc", AlertEntity.class)
                .setMaxResults(50)
                .getResultList()
                .stream()
                .map(MappingSupport::alert)
                .toList();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public AlertView acknowledge(long alertId) {
        AlertEntity alert = entityManager.find(AlertEntity.class, alertId);
        if (alert == null) {
            throw new ResourceNotFoundException("Alert", alertId);
        }
        alert.setAcknowledged(true);
        return MappingSupport.alert(alert);
    }
}
