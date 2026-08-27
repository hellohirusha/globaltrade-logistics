package com.hiru.globaltrade.common;

import com.hiru.globaltrade.common.security.LogisticsRoles;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogisticsRolesTest {
    @Test
    void exposesEnterpriseRolesRequiredByAssignment() {
        assertThat(LogisticsRoles.ADMIN).isEqualTo("GLOBALTRADE_ADMIN");
        assertThat(LogisticsRoles.COORDINATOR).contains("LOGISTICS");
        assertThat(LogisticsRoles.CUSTOMS_AGENT).contains("CUSTOMS");
        assertThat(LogisticsRoles.VENDOR_REPRESENTATIVE).contains("VENDOR");
    }
}
