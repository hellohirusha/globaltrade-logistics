package com.hiru.globaltrade.web.resource;

import com.hiru.globaltrade.common.dto.AlertView;
import com.hiru.globaltrade.common.dto.ApiEnvelope;
import com.hiru.globaltrade.common.service.AlertService;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/alerts")
@Produces(MediaType.APPLICATION_JSON)
public class AlertResource {
    @EJB
    private AlertService alertService;

    @GET
    public ApiEnvelope<List<AlertView>> open() {
        return ApiEnvelope.ok(alertService.findOpenAlerts());
    }

    @PUT
    @Path("/{id}/acknowledge")
    public ApiEnvelope<AlertView> acknowledge(@PathParam("id") long id) {
        return ApiEnvelope.ok(alertService.acknowledge(id));
    }
}
