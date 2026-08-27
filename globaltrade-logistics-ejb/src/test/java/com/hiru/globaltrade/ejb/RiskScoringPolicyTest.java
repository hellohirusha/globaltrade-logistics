package com.hiru.globaltrade.ejb;

import com.hiru.globaltrade.common.enums.ShipmentPriority;
import com.hiru.globaltrade.common.enums.VendorTier;
import com.hiru.globaltrade.ejb.policy.RiskScoringPolicy;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RiskScoringPolicyTest {
    private final RiskScoringPolicy policy = new RiskScoringPolicy();

    @Test
    void criticalShipmentFromWatchlistVendorProducesHighRiskScore() {
        int score = policy.score(ShipmentPriority.CRITICAL, VendorTier.WATCHLIST, LocalDateTime.now().plusHours(8));

        assertThat(score).isGreaterThanOrEqualTo(90);
    }

    @Test
    void strategicVendorWithStandardFutureShipmentProducesLowerRiskScore() {
        int score = policy.score(ShipmentPriority.STANDARD, VendorTier.STRATEGIC, LocalDateTime.now().plusDays(5));

        assertThat(score).isLessThan(30);
    }
}
