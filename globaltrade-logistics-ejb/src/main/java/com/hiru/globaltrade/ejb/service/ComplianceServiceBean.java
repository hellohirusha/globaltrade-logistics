package com.hiru.globaltrade.ejb.service;

import com.hiru.globaltrade.common.dto.ComplianceAuditView;
import com.hiru.globaltrade.common.security.LogisticsRoles;
import com.hiru.globaltrade.common.service.ComplianceService;
import com.hiru.globaltrade.ejb.entity.ComplianceAuditEntity;
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
@RolesAllowed({LogisticsRoles.ADMIN, LogisticsRoles.CUSTOMS_AGENT, LogisticsRoles.COORDINATOR})
@Interceptors(PerformanceInterceptor.class)
public class ComplianceServiceBean implements ComplianceService {
    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager entityManager;

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<ComplianceAuditView> recentEvents(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return entityManager.createQuery("select c from ComplianceAuditEntity c order by c.createdAt desc", ComplianceAuditEntity.class)
                .setMaxResults(safeLimit)
                .getResultList()
                .stream()
                .map(MappingSupport::audit)
                .toList();
    }
}
