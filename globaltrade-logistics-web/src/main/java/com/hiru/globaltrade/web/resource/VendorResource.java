package com.hiru.globaltrade.web.resource;

import com.hiru.globaltrade.common.dto.ApiEnvelope;
import com.hiru.globaltrade.common.dto.VendorScoreCommand;
import com.hiru.globaltrade.common.dto.VendorView;
import com.hiru.globaltrade.common.service.VendorService;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/vendors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VendorResource {
    @EJB
    private VendorService vendorService;

    @GET
    public ApiEnvelope<List<VendorView>> all() {
        return ApiEnvelope.ok(vendorService.findAll());
    }

    @PUT
    @Path("/score")
    public ApiEnvelope<VendorView> evaluate(@Valid VendorScoreCommand command) {
        return ApiEnvelope.ok(vendorService.evaluate(command));
    }
}
