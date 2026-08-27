package com.hiru.globaltrade.ejb.service;

import com.hiru.globaltrade.common.service.InventoryService;
import com.hiru.globaltrade.common.service.ShipmentService;
import com.hiru.globaltrade.common.service.VendorService;
import com.hiru.globaltrade.common.security.LogisticsRoles;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;

import java.util.logging.Logger;

@Singleton
@Startup
@RunAs(LogisticsRoles.ADMIN)
public class SupplyChainTimerBean {
    private static final Logger LOGGER = Logger.getLogger(SupplyChainTimerBean.class.getName());

    @Resource
    private TimerService timerService;

    @EJB
    private ShipmentService shipmentService;

    @EJB
    private InventoryService inventoryService;

    @EJB
    private VendorService vendorService;

    @PostConstruct
    void createProgrammaticHealthTimer() {
        boolean timerExists = timerService.getTimers().stream()
                .anyMatch(timer -> "GLOBALTRADE_HEALTH_SWEEP".equals(timer.getInfo()));
        if (timerExists) {
            return;
        }
        TimerConfig timerConfig = new TimerConfig("GLOBALTRADE_HEALTH_SWEEP", true);
        timerService.createIntervalTimer(30_000L, 300_000L, timerConfig);
    }

    @Schedule(hour = "*", minute = "*/15", second = "0", persistent = true, info = "SHIPMENT_DELAY_MONITOR")
    public void monitorDelays() {
        int delayed = shipmentService.monitorShipmentDelays();
        LOGGER.info(() -> "Shipment delay monitor completed. Delayed shipments marked: " + delayed);
    }

    @Schedule(hour = "*", minute = "*/20", second = "0", persistent = true, info = "INVENTORY_REPLENISHMENT_MONITOR")
    public void monitorInventory() {
        int signals = inventoryService.monitorReplenishment();
        LOGGER.info(() -> "Inventory replenishment monitor completed. Signals raised: " + signals);
    }

    @Timeout
    public void programmaticHealthSweep(Timer timer) {
        int vendors = vendorService.refreshVendorTiers();
        LOGGER.info(() -> "Programmatic health sweep completed for vendors: " + vendors + " timer=" + timer.getInfo());
    }
}
