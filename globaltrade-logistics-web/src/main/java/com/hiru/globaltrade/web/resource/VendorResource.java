package com.hiru.globaltrade.web.resource;

import com.hiru.globaltrade.common.dto.ApiEnvelope;
import com.hiru.globaltrade.common.dto.VendorCommand;
import com.hiru.globaltrade.common.dto.VendorScoreCommand;
import com.hiru.globaltrade.common.dto.VendorView;
import com.hiru.globaltrade.common.service.VendorService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/vendors")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VendorResource {
    @Inject
    private VendorService vendorService;

    @GET
    public ApiEnvelope<List<VendorView>> all() {
        return ApiEnvelope.ok(vendorService.findAll());
    }

    @POST
    public ApiEnvelope<VendorView> create(@Valid VendorCommand command) {
        return ApiEnvelope.ok(vendorService.create(command));
    }

    @PUT
    @Path("/score")
    public ApiEnvelope<VendorView> evaluate(@Valid VendorScoreCommand command) {
        return ApiEnvelope.ok(vendorService.evaluate(command));
    }
}
