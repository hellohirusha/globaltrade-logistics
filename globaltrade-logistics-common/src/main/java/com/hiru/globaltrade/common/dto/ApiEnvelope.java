package com.hiru.globaltrade.common.dto;

import java.time.Instant;

public record ApiEnvelope<T>(
        boolean success,
        String message,
        T data,
        Instant generatedAt
) {
    public static <T> ApiEnvelope<T> ok(T data) {
        return new ApiEnvelope<>(true, "ok", data, Instant.now());
    }

    public static <T> ApiEnvelope<T> failure(String message) {
        return new ApiEnvelope<>(false, message, null, Instant.now());
    }
}
