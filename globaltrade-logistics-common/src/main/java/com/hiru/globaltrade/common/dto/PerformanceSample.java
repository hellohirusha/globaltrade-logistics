package com.hiru.globaltrade.common.dto;

import java.time.Instant;

public record PerformanceSample(
        String operation,
        long durationMillis,
        String outcome,
        Instant capturedAt
) {
}
