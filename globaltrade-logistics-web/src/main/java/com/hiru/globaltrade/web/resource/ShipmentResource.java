package com.hiru.globaltrade.web.resource;

import com.hiru.globaltrade.common.dto.ApiEnvelope;
import com.hiru.globaltrade.common.dto.ShipmentCommand;
import com.hiru.globaltrade.common.dto.ShipmentStatusCommand;
import com.hiru.globaltrade.common.dto.ShipmentView;
import com.hiru.globaltrade.common.service.ShipmentService;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/shipments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShipmentResource {
    @EJB
    private ShipmentService shipmentService;

    @GET
    public ApiEnvelope<List<ShipmentView>> active() {
        return ApiEnvelope.ok(shipmentService.findActiveShipments());
    }

    @POST
    public ApiEnvelope<ShipmentView> create(@Valid ShipmentCommand command) {
        return ApiEnvelope.ok(shipmentService.create(command));
    }

    @PUT
    @Path("/status")
    public ApiEnvelope<ShipmentView> updateStatus(@Valid ShipmentStatusCommand command) {
        return ApiEnvelope.ok(shipmentService.updateStatus(command));
    }
}
