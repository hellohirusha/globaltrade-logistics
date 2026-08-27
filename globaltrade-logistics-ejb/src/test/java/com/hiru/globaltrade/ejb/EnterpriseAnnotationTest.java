package com.hiru.globaltrade.ejb;

import com.hiru.globaltrade.ejb.service.MaintenanceServiceBean;
import com.hiru.globaltrade.ejb.service.ShipmentServiceBean;
import com.hiru.globaltrade.ejb.service.SupplyChainTimerBean;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.ejb.Timeout;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Test
    void scheduledMaintenanceMethodsDoNotRequireInteractiveSecurityPrincipal() throws Exception {
        assertThat(SupplyChainTimerBean.class.getAnnotation(RunAs.class)).isNull();
        assertThat(MaintenanceServiceBean.class.getAnnotation(Stateless.class)).isNotNull();
        assertThat(MaintenanceServiceBean.class.getAnnotation(PermitAll.class)).isNotNull();
        assertThat(Arrays.stream(SupplyChainTimerBean.class.getDeclaredFields())
                .anyMatch(field -> field.getType().equals(MaintenanceServiceBean.class))).isTrue();

        for (String methodName : new String[]{"monitorShipmentDelays", "monitorReplenishment", "refreshVendorTiers"}) {
            Method method = MaintenanceServiceBean.class.getMethod(methodName);
            TransactionAttribute transactionAttribute = method.getAnnotation(TransactionAttribute.class);

            assertThat(transactionAttribute).isNotNull();
            assertThat(transactionAttribute.value()).isEqualTo(TransactionAttributeType.REQUIRES_NEW);
        }
    }

    @Test
    void ejbDescriptorDeclaresEnterpriseSecurityRoles() throws Exception {
        String ejbDescriptor = Files.readString(Path.of("src/main/resources/META-INF/ejb-jar.xml"));

        assertThat(ejbDescriptor)
                .contains("<role-name>GLOBALTRADE_ADMIN</role-name>")
                .contains("<role-name>LOGISTICS_COORDINATOR</role-name>")
                .contains("<role-name>WAREHOUSE_MANAGER</role-name>")
                .contains("<role-name>CUSTOMS_AGENT</role-name>")
                .contains("<role-name>VENDOR_REPRESENTATIVE</role-name>")
                .contains("<role-name>CUSTOMER_PORTAL_USER</role-name>");
    }
}
