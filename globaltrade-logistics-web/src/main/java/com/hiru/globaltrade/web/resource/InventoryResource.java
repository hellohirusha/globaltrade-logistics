package com.hiru.globaltrade.web.resource;

import com.hiru.globaltrade.common.dto.ApiEnvelope;
import com.hiru.globaltrade.common.dto.InventoryAdjustmentCommand;
import com.hiru.globaltrade.common.dto.InventoryView;
import com.hiru.globaltrade.common.service.InventoryService;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryResource {
    @EJB
    private InventoryService inventoryService;

    @GET
    public ApiEnvelope<List<InventoryView>> all() {
        return ApiEnvelope.ok(inventoryService.findAll());
    }

    @PUT
    public ApiEnvelope<InventoryView> adjust(@Valid InventoryAdjustmentCommand command) {
        return ApiEnvelope.ok(inventoryService.adjust(command));
    }
}
