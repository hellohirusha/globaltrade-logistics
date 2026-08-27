package com.hiru.globaltrade.ejb.policy;

import com.hiru.globaltrade.common.enums.ShipmentPriority;
import com.hiru.globaltrade.common.enums.VendorTier;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class RiskScoringPolicy {
    public int score(ShipmentPriority priority, VendorTier vendorTier, LocalDateTime estimatedDelivery) {
        int score = switch (priority) {
            case CRITICAL -> 45;
            case EXPRESS -> 28;
            case STANDARD -> 15;
        };

        score += switch (vendorTier) {
            case SUSPENDED -> 40;
            case WATCHLIST -> 28;
            case APPROVED -> 10;
            case STRATEGIC -> 4;
        };

        long hoursUntilDelivery = ChronoUnit.HOURS.between(LocalDateTime.now(), estimatedDelivery);
        if (hoursUntilDelivery < 0) {
            score += 35;
        } else if (hoursUntilDelivery <= 24) {
            score += 18;
        } else if (hoursUntilDelivery <= 72) {
            score += 8;
        }

        return Math.min(100, Math.max(0, score));
    }
}
