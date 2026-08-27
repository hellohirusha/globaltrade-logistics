package com.hiru.globaltrade.ejb.service;

import com.hiru.globaltrade.ejb.entity.ComplianceAuditEntity;
import com.hiru.globaltrade.ejb.entity.PerformanceMetricEntity;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
@PermitAll
public class TelemetryServiceBean {
    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager entityManager;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void audit(String actor, String action, String resource, String outcome, String ipAddress) {
        ComplianceAuditEntity event = new ComplianceAuditEntity();
        event.setActor(actor);
        event.setAction(action);
        event.setResource(resource);
        event.setOutcome(outcome);
        event.setIpAddress(ipAddress);
        entityManager.persist(event);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void performance(String operation, long durationMillis, String outcome) {
        PerformanceMetricEntity metric = new PerformanceMetricEntity();
        metric.setOperation(operation);
        metric.setDurationMillis(durationMillis);
        metric.setOutcome(outcome);
        entityManager.persist(metric);
    }
}
