package com.hiru.globaltrade.ejb;

import com.hiru.globaltrade.common.dto.ShipmentCommand;
import com.hiru.globaltrade.common.enums.ShipmentPriority;
import com.hiru.globaltrade.common.exception.BusinessRuleException;
import com.hiru.globaltrade.ejb.interceptor.VendorValidationInterceptor;
import jakarta.interceptor.InvocationContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VendorValidationInterceptorTest {
    private final VendorValidationInterceptor interceptor = new VendorValidationInterceptor();

    @Test
    void noArgumentInvocationsProceedWithoutValidationFailure() throws Exception {
        Object result = interceptor.validateShipmentCommand(new StubInvocationContext(null));

        assertThat(result).isEqualTo("proceeded");
    }

    @Test
    void invalidRouteIsRejectedAsBusinessRuleFailure() {
        ShipmentCommand command = new ShipmentCommand(
                "GTL-TEST-001",
                "Colombo",
                "colombo",
                "Maersk",
                "VEN-SG-001",
                ShipmentPriority.STANDARD,
                "CUS-TEST",
                LocalDateTime.now().plusDays(1)
        );

        assertThatThrownBy(() -> interceptor.validateShipmentCommand(new StubInvocationContext(new Object[]{command})))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Origin and destination must be different for a shipment.");
    }

    private static final class StubInvocationContext implements InvocationContext {
        private final Object[] parameters;

        private StubInvocationContext(Object[] parameters) {
            this.parameters = parameters;
        }

        @Override
        public Object getTarget() {
            return this;
        }

        @Override
        public Object getTimer() {
            return null;
        }

        @Override
        public Method getMethod() {
            return null;
        }

        @Override
        public Constructor<?> getConstructor() {
            return null;
        }

        @Override
        public Object[] getParameters() {
            return parameters;
        }

        @Override
        public void setParameters(Object[] params) {
        }

        @Override
        public Map<String, Object> getContextData() {
            return new HashMap<>();
        }

        @Override
        public Object proceed() {
            return "proceeded";
        }
    }
}
