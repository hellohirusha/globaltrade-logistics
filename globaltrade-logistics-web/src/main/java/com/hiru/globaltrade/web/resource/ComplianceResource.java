package com.hiru.globaltrade.web.resource;

import com.hiru.globaltrade.common.dto.ApiEnvelope;
import com.hiru.globaltrade.common.dto.ComplianceAuditView;
import com.hiru.globaltrade.common.service.ComplianceService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/compliance")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class ComplianceResource {
    @Inject
    private ComplianceService complianceService;

    @GET
    public ApiEnvelope<List<ComplianceAuditView>> recent(@QueryParam("limit") @DefaultValue("25") int limit) {
        return ApiEnvelope.ok(complianceService.recentEvents(limit));
    }
}
