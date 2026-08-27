package com.hiru.globaltrade.web;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardAssetTest {
    @Test
    void dashboardContainsAllAssignmentOperationSections() throws Exception {
        String html = Files.readString(Path.of("src/main/webapp/index.html"));

        assertThat(html).contains("Priority shipments");
        assertThat(html).contains("Inventory control");
        assertThat(html).contains("Vendor performance");
        assertThat(html).contains("Compliance audit trail");
        assertThat(html).contains("Performance telemetry");
    }
}
