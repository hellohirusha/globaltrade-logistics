package com.hiru.globaltrade.ejb.interceptor;

import com.hiru.globaltrade.common.dto.ShipmentCommand;
import com.hiru.globaltrade.common.exception.BusinessRuleException;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

public class VendorValidationInterceptor {
    @AroundInvoke
    public Object validateShipmentCommand(InvocationContext context) throws Exception {
        Object[] parameters = context.getParameters();
        if (parameters == null) {
            return context.proceed();
        }
        for (Object parameter : parameters) {
            if (parameter instanceof ShipmentCommand command) {
                if (command.origin() != null
                        && command.destination() != null
                        && command.origin().equalsIgnoreCase(command.destination())) {
                    throw new BusinessRuleException("Origin and destination must be different for a shipment.");
                }
                if (command.vendorCode() != null && command.vendorCode().startsWith("SUSP-")) {
                    throw new BusinessRuleException("Suspended vendor codes cannot create new shipments.");
                }
            }
        }
        return context.proceed();
    }
}
