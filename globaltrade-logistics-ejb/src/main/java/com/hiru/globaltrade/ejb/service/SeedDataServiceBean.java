package com.hiru.globaltrade.ejb.service;

import com.hiru.globaltrade.common.enums.AlertSeverity;
import com.hiru.globaltrade.common.enums.ShipmentPriority;
import com.hiru.globaltrade.common.enums.ShipmentStatus;
import com.hiru.globaltrade.common.enums.VendorTier;
import com.hiru.globaltrade.ejb.entity.AlertEntity;
import com.hiru.globaltrade.ejb.entity.InventoryItemEntity;
import com.hiru.globaltrade.ejb.entity.ShipmentEntity;
import com.hiru.globaltrade.ejb.entity.VendorEntity;
import com.hiru.globaltrade.ejb.policy.RiskScoringPolicy;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Singleton
@Startup
public class SeedDataServiceBean {
    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager entityManager;

    private final RiskScoringPolicy riskScoringPolicy = new RiskScoringPolicy();

    @Schedule(hour = "*", minute = "*", second = "8", persistent = false, info = "DEMO_DATA_SEED")
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void seed() {
        Long vendors = entityManager.createQuery("select count(v) from VendorEntity v", Long.class).getSingleResult();
        if (vendors > 0) {
            return;
        }

        VendorEntity transOcean = vendor("VEN-SG-001", "TransOcean Freight Network", "Singapore", BigDecimal.valueOf(94.50), VendorTier.STRATEGIC);
        VendorEntity alpine = vendor("VEN-DE-014", "Alpine Customs Brokerage", "Germany", BigDecimal.valueOf(82.25), VendorTier.APPROVED);
        VendorEntity pacific = vendor("VEN-CN-021", "Pacific Supplier Hub", "China", BigDecimal.valueOf(61.40), VendorTier.WATCHLIST);

        inventory("GT-PALLET-100", "Smart export pallet", "CMB-WH-01", 74, 40, 120);
        inventory("GT-SENSOR-500", "IoT shipment sensor", "SIN-WH-02", 14, 25, 80);
        inventory("GT-SEAL-220", "Tamper proof customs seal", "HAM-WH-03", 0, 60, 200);
        inventory("GT-LABEL-310", "Hazmat compliance label pack", "DXB-WH-04", 180, 90, 250);

        shipment("GTL-2026-0001", "Colombo", "Singapore", "OceanLink", transOcean, ShipmentStatus.IN_TRANSIT, ShipmentPriority.EXPRESS, "CUS-LK-SG-8831", LocalDateTime.now().plusHours(18));
        shipment("GTL-2026-0002", "Hamburg", "Rotterdam", "EuroRail Cargo", alpine, ShipmentStatus.CUSTOMS_REVIEW, ShipmentPriority.CRITICAL, "CUS-DE-NL-4410", LocalDateTime.now().plusHours(9));
        shipment("GTL-2026-0003", "Shenzhen", "Los Angeles", "Pacific Blue", pacific, ShipmentStatus.DELAYED, ShipmentPriority.CRITICAL, "CUS-CN-US-1944", LocalDateTime.now().minusHours(5));
        shipment("GTL-2026-0004", "Dubai", "Nairobi", "AirBridge", transOcean, ShipmentStatus.PICKED_UP, ShipmentPriority.STANDARD, "CUS-AE-KE-7002", LocalDateTime.now().plusDays(3));
        shipment("GTL-2026-0005", "Mumbai", "Doha", "Gulf Express", alpine, ShipmentStatus.DELIVERED, ShipmentPriority.STANDARD, "CUS-IN-QA-1120", LocalDateTime.now().minusDays(2));

        alert(AlertSeverity.CRITICAL, "Customs seal stockout", "GT-SEAL-220 is out of stock at HAM-WH-03 and blocks export release.");
        alert(AlertSeverity.WARNING, "Vendor watchlist", "VEN-CN-021 requires extra validation before new critical shipments.");
        alert(AlertSeverity.INFO, "Route optimization complete", "The Singapore lane was rebalanced to protect critical delivery windows.");
    }

    private VendorEntity vendor(String code, String name, String country, BigDecimal score, VendorTier tier) {
        VendorEntity vendor = new VendorEntity();
        vendor.setVendorCode(code);
        vendor.setName(name);
        vendor.setCountry(country);
        vendor.setScore(score);
        vendor.setTier(tier);
        entityManager.persist(vendor);
        return vendor;
    }

    private void inventory(String sku, String name, String warehouse, int quantity, int reorderPoint, int reorderQuantity) {
        InventoryItemEntity item = new InventoryItemEntity();
        item.setSku(sku);
        item.setName(name);
        item.setWarehouseCode(warehouse);
        item.setQuantityOnHand(quantity);
        item.setReorderPoint(reorderPoint);
        item.setReorderQuantity(reorderQuantity);
        entityManager.persist(item);
    }

    private void shipment(String reference, String origin, String destination, String carrier, VendorEntity vendor, ShipmentStatus status,
                          ShipmentPriority priority, String customsReference, LocalDateTime estimatedDelivery) {
        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setReference(reference);
        shipment.setOrigin(origin);
        shipment.setDestination(destination);
        shipment.setCarrier(carrier);
        shipment.setVendor(vendor);
        shipment.setStatus(status);
        shipment.setPriority(priority);
        shipment.setCustomsReference(customsReference);
        shipment.setEstimatedDelivery(estimatedDelivery);
        shipment.setRiskScore(riskScoringPolicy.score(priority, vendor.getTier(), estimatedDelivery));
        entityManager.persist(shipment);
    }

    private void alert(AlertSeverity severity, String title, String message) {
        AlertEntity alert = new AlertEntity();
        alert.setSeverity(severity);
        alert.setTitle(title);
        alert.setMessage(message);
        entityManager.persist(alert);
    }
}
