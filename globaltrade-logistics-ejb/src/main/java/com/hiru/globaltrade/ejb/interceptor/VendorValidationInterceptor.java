package com.hiru.globaltrade.ejb.interceptor;

import com.hiru.globaltrade.common.dto.ShipmentCommand;
import com.hiru.globaltrade.common.exception.BusinessRuleException;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

public class VendorValidationInterceptor {
    @AroundInvoke
    public Object validateShipmentCommand(InvocationContext context) throws Exception {
        for (Object parameter : context.getParameters()) {
            if (parameter instanceof ShipmentCommand command) {
                if (command.origin().equalsIgnoreCase(command.destination())) {
                    throw new BusinessRuleException("Origin and destination must be different for a shipment.");
                }
                if (command.vendorCode().startsWith("SUSP-")) {
                    throw new BusinessRuleException("Suspended vendor codes cannot create new shipments.");
                }
            }
        }
        return context.proceed();
    }
}
