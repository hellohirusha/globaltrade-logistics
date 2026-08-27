package com.hiru.globaltrade.web.filter;

import com.hiru.globaltrade.common.dto.ApiEnvelope;
import com.hiru.globaltrade.common.exception.BusinessRuleException;
import com.hiru.globaltrade.common.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<RuntimeException> {
    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof ResourceNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiEnvelope.failure(exception.getMessage()))
                    .build();
        }
        if (exception instanceof BusinessRuleException || exception instanceof ConstraintViolationException || exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiEnvelope.failure(exception.getMessage()))
                    .build();
        }
        return Response.serverError()
                .entity(ApiEnvelope.failure("Unexpected platform error. See Payara server logs for the correlated stack trace."))
                .build();
    }
}
