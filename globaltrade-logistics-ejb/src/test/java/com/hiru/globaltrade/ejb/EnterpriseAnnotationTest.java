package com.hiru.globaltrade.ejb;

import com.hiru.globaltrade.ejb.service.ShipmentServiceBean;
import com.hiru.globaltrade.ejb.service.SupplyChainTimerBean;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.ejb.Timeout;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class EnterpriseAnnotationTest {
    @Test
    void shipmentServiceIsAStatelessEnterpriseBean() {
        assertThat(ShipmentServiceBean.class.getAnnotation(Stateless.class)).isNotNull();
    }

    @Test
    void timerBeanProvidesDeclarativeAndProgrammaticTimerCallbacks() {
        boolean hasSchedule = Arrays.stream(SupplyChainTimerBean.class.getDeclaredMethods())
                .anyMatch(method -> method.getAnnotation(Schedule.class) != null);
        boolean hasTimeout = Arrays.stream(SupplyChainTimerBean.class.getDeclaredMethods())
                .anyMatch(method -> method.getAnnotation(Timeout.class) != null);

        assertThat(hasSchedule).isTrue();
        assertThat(hasTimeout).isTrue();
    }
}
