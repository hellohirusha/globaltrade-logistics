package com.hiru.globaltrade.common.service;

import com.hiru.globaltrade.common.dto.AlertView;
import jakarta.ejb.Local;

import java.util.List;

@Local
public interface AlertService {
    List<AlertView> findOpenAlerts();

    AlertView acknowledge(long alertId);
}
