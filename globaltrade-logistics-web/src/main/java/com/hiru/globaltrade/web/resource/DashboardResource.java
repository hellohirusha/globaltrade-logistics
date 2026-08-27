package com.hiru.globaltrade.web.resource;

import com.hiru.globaltrade.common.dto.ApiEnvelope;
import com.hiru.globaltrade.common.dto.DashboardSnapshot;
import com.hiru.globaltrade.common.service.DashboardService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/dashboard")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class DashboardResource {
    @Inject
    private DashboardService dashboardService;

    @GET
    public ApiEnvelope<DashboardSnapshot> snapshot() {
        return ApiEnvelope.ok(dashboardService.snapshot());
    }
}
