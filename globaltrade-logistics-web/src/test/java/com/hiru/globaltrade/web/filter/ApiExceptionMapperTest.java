package com.hiru.globaltrade.web.filter;

import com.hiru.globaltrade.common.exception.BusinessRuleException;
import com.hiru.globaltrade.common.exception.ResourceNotFoundException;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.EJBException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionMapperTest {
    private final ApiExceptionMapper mapper = new ApiExceptionMapper();

    @Test
    void mapsResourceNotFoundInsideEjbExceptionToNotFound() {
        EJBException exception = new EJBException(new ResourceNotFoundException("Vendor", "VEN-X"));

        assertThat(mapper.statusFor(exception)).isEqualTo(Response.Status.NOT_FOUND);
        assertThat(mapper.messageFor(exception)).isEqualTo("Vendor was not found for identifier VEN-X");
    }

    @Test
    void mapsBusinessRuleInsideEjbExceptionToBadRequest() {
        EJBException exception = new EJBException(new BusinessRuleException("Shipment reference already exists."));

        assertThat(mapper.statusFor(exception)).isEqualTo(Response.Status.BAD_REQUEST);
        assertThat(mapper.messageFor(exception)).isEqualTo("Shipment reference already exists.");
    }

    @Test
    void mapsAccessFailuresToForbidden() {
        EJBAccessException exception = new EJBAccessException("Caller is not allowed.");

        assertThat(mapper.statusFor(exception)).isEqualTo(Response.Status.FORBIDDEN);
        assertThat(mapper.messageFor(exception)).isEqualTo("Access denied for this operation.");
    }
}
