package com.hiru.globaltrade.common.dto;

import java.time.Instant;

public record ComplianceAuditView(
        Long id,
        String actor,
        String action,
        String resource,
        String outcome,
        String ipAddress,
        Instant createdAt
) {
}
