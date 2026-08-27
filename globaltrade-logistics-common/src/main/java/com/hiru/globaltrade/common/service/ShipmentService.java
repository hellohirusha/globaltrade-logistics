package com.hiru.globaltrade.common.service;

import com.hiru.globaltrade.common.dto.ShipmentCommand;
import com.hiru.globaltrade.common.dto.ShipmentStatusCommand;
import com.hiru.globaltrade.common.dto.ShipmentView;
import jakarta.ejb.Local;

import java.util.List;

@Local
public interface ShipmentService {
    ShipmentView create(ShipmentCommand command);

    ShipmentView updateStatus(ShipmentStatusCommand command);

    List<ShipmentView> findActiveShipments();

    int monitorShipmentDelays();
}
