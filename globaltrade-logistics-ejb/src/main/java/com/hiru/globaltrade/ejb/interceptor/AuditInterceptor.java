package com.hiru.globaltrade.ejb.interceptor;

import com.hiru.globaltrade.ejb.service.TelemetryServiceBean;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

import java.security.Principal;

public class AuditInterceptor {
    @EJB
    private TelemetryServiceBean telemetryService;

    @Resource
    private jakarta.ejb.SessionContext sessionContext;

    @AroundInvoke
    public Object audit(InvocationContext context) throws Exception {
        String outcome = "SUCCESS";
        try {
            return context.proceed();
        } catch (Exception ex) {
            outcome = "FAILED";
            throw ex;
        } finally {
            telemetryService.audit(
                    currentActor(),
                    context.getMethod().getDeclaringClass().getSimpleName() + "." + context.getMethod().getName(),
                    context.getTarget().getClass().getSimpleName(),
                    outcome,
                    "server-side-ejb"
            );
        }
    }

    private String currentActor() {
        if (sessionContext == null) {
            return "system";
        }
        Principal principal = sessionContext.getCallerPrincipal();
        return principal == null ? "system" : principal.getName();
    }
}
