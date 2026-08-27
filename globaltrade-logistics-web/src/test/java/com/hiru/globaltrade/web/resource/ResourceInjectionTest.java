package com.hiru.globaltrade.web.resource;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceInjectionTest {
    private final List<Class<?>> resources = List.of(
            AlertResource.class,
            ComplianceResource.class,
            DashboardResource.class,
            InventoryResource.class,
            ShipmentResource.class,
            VendorResource.class
    );

    @Test
    void restResourcesAreCdiManagedForServiceInjection() {
        resources.forEach(resource -> {
            assertThat(resource.getAnnotation(RequestScoped.class))
                    .as(resource.getSimpleName() + " should be CDI request scoped")
                    .isNotNull();

            for (Field field : resource.getDeclaredFields()) {
                if (field.getName().endsWith("Service")) {
                    assertThat(field.getAnnotation(Inject.class))
                            .as(resource.getSimpleName() + "." + field.getName() + " should use CDI injection")
                            .isNotNull();
                    assertThat(field.getAnnotation(EJB.class))
                            .as(resource.getSimpleName() + "." + field.getName() + " should not depend on unmanaged EJB injection")
                            .isNull();
                }
            }
        });
    }
}
