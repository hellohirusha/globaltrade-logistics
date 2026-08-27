package com.hiru.globaltrade.common.service;

import com.hiru.globaltrade.common.dto.ComplianceAuditView;
import jakarta.ejb.Local;

import java.util.List;

@Local
public interface ComplianceService {
    List<ComplianceAuditView> recentEvents(int limit);
}
