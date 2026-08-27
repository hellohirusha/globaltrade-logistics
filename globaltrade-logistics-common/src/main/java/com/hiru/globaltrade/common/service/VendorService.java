package com.hiru.globaltrade.common.service;

import com.hiru.globaltrade.common.dto.VendorScoreCommand;
import com.hiru.globaltrade.common.dto.VendorView;
import jakarta.ejb.Local;

import java.util.List;

@Local
public interface VendorService {
    List<VendorView> findAll();

    VendorView evaluate(VendorScoreCommand command);

    int refreshVendorTiers();
}
