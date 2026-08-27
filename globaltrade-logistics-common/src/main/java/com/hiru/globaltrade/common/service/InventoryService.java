package com.hiru.globaltrade.common.service;

import com.hiru.globaltrade.common.dto.InventoryAdjustmentCommand;
import com.hiru.globaltrade.common.dto.InventoryItemCommand;
import com.hiru.globaltrade.common.dto.InventoryView;
import jakarta.ejb.Local;

import java.util.List;

@Local
public interface InventoryService {
    List<InventoryView> findAll();

    InventoryView create(InventoryItemCommand command);

    InventoryView adjust(InventoryAdjustmentCommand command);

    int monitorReplenishment();
}
