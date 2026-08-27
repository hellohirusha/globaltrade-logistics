package com.hiru.globaltrade.web;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardAssetTest {
    @Test
    void dashboardContainsAllOperationSections() throws Exception {
        String html = Files.readString(Path.of("src/main/webapp/index.html"));

        assertThat(html).contains("Workspace role");
        assertThat(html).contains("Live shipments");
        assertThat(html).contains("Priority shipments");
        assertThat(html).contains("Create inventory item");
        assertThat(html).contains("Update inventory");
        assertThat(html).contains("Create vendor");
        assertThat(html).contains("Vendor performance");
        assertThat(html).contains("Compliance audit trail");
        assertThat(html).contains("Performance telemetry");
        assertThat(html).contains("shipmentVendorSelect");
        assertThat(html).contains("shipmentReferenceSelect");
        assertThat(html).contains("inventoryItemSelect");
        assertThat(html).contains("vendorScoreSelect");
    }

    @Test
    void dashboardDoesNotAdvertiseDemoDataFallback() throws Exception {
        String html = Files.readString(Path.of("src/main/webapp/index.html"));
        String script = Files.readString(Path.of("src/main/webapp/assets/app.js"));

        assertThat(html).doesNotContain("fallback preview");
        assertThat(script).doesNotContain("bundled demo data");
        assertThat(script).contains("api(\"/shipments\")");
        assertThat(script).contains("submitJson(\"/inventory\", \"POST\"");
        assertThat(script).contains("submitJson(\"/vendors\", \"POST\"");
        assertThat(script).contains("populateSelects()");
        assertThat(script).contains("syncInventorySelection()");
    }
}
