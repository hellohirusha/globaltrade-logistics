package com.hiru.globaltrade.common.service;

import com.hiru.globaltrade.common.dto.DashboardSnapshot;
import jakarta.ejb.Local;

@Local
public interface DashboardService {
    DashboardSnapshot snapshot();
}
