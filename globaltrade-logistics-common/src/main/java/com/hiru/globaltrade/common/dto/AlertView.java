package com.hiru.globaltrade.common.dto;

import com.hiru.globaltrade.common.enums.AlertSeverity;

import java.time.Instant;

public record AlertView(
        Long id,
        AlertSeverity severity,
        String title,
        String message,
        boolean acknowledged,
        Instant raisedAt
) {
}
