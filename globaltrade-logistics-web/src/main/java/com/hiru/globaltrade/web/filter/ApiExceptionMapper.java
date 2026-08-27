package com.hiru.globaltrade.web.filter;

import com.hiru.globaltrade.common.dto.ApiEnvelope;
import com.hiru.globaltrade.common.exception.BusinessRuleException;
import com.hiru.globaltrade.common.exception.ResourceNotFoundException;
import jakarta.ejb.AccessLocalException;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.EJBException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<RuntimeException> {
    private static final Logger LOGGER = Logger.getLogger(ApiExceptionMapper.class.getName());

    @Override
    public Response toResponse(RuntimeException exception) {
        ApiError error = classify(exception);
        if (error.status().getFamily() == Response.Status.Family.SERVER_ERROR) {
            LOGGER.log(Level.SEVERE, error.message(), exception);
        }
        return failure(error.status(), error.message());
    }

    Response.Status statusFor(RuntimeException exception) {
        return classify(exception).status();
    }

    String messageFor(RuntimeException exception) {
        return classify(exception).message();
    }

    private ApiError classify(RuntimeException exception) {
        Throwable resolved = unwrap(exception);
        if (resolved instanceof ResourceNotFoundException) {
            return new ApiError(Response.Status.NOT_FOUND, resolved.getMessage());
        }
        if (resolved instanceof BusinessRuleException || resolved instanceof ConstraintViolationException || resolved instanceof IllegalArgumentException) {
            return new ApiError(Response.Status.BAD_REQUEST, resolved.getMessage());
        }
        if (resolved instanceof EJBAccessException || resolved instanceof AccessLocalException) {
            return new ApiError(Response.Status.FORBIDDEN, "Access denied for this operation.");
        }

        return new ApiError(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected platform error. See Payara server logs for the correlated stack trace.");
    }

    private Throwable unwrap(Throwable exception) {
        Throwable current = exception;
        for (int depth = 0; depth < 12 && shouldUnwrap(current); depth++) {
            current = current.getCause();
        }
        return current;
    }

    private boolean shouldUnwrap(Throwable exception) {
        return exception.getCause() != null
                && exception.getCause() != exception
                && exception instanceof EJBException;
    }

    private Response failure(Response.Status status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(ApiEnvelope.failure(message))
                .build();
    }

    private record ApiError(Response.Status status, String message) {
    }
}
