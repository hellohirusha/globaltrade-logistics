package com.hiru.globaltrade.ejb.interceptor;

import com.hiru.globaltrade.ejb.service.TelemetryServiceBean;
import jakarta.ejb.EJB;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

public class PerformanceInterceptor {
    @EJB
    private TelemetryServiceBean telemetryService;

    @AroundInvoke
    public Object measure(InvocationContext context) throws Exception {
        long started = System.nanoTime();
        String outcome = "SUCCESS";
        try {
            return context.proceed();
        } catch (Exception ex) {
            outcome = "FAILED";
            throw ex;
        } finally {
            telemetryService.performance(
                    context.getMethod().getDeclaringClass().getSimpleName() + "." + context.getMethod().getName(),
                    (System.nanoTime() - started) / 1_000_000L,
                    outcome
            );
        }
    }
}
