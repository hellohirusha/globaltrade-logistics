package com.hiru.globaltrade.security.jaas;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SecuritySchemaTest {
    @Test
    void mysqlSchemaDefinesSupplyChainAuthenticationTables() throws Exception {
        String schema = Files.readString(Path.of("../deploy/mysql/schema.sql"));

        assertThat(schema)
                .contains("CREATE TABLE IF NOT EXISTS auth_users")
                .contains("CREATE TABLE IF NOT EXISTS auth_user_roles")
                .contains("CREATE TABLE IF NOT EXISTS login_audit")
                .contains("CREATE TABLE IF NOT EXISTS security_events")
                .contains("password_hash")
                .contains("password_salt")
                .contains("password_iterations")
                .contains("account_locked")
                .contains("GLOBALTRADE_ADMIN")
                .contains("VENDOR_REPRESENTATIVE")
                .contains("CUSTOMER_PORTAL_USER");
    }

    @Test
    void payaraSetupReferencesCustomJaasLoginModule() throws Exception {
        String setup = Files.readString(Path.of("../deploy/payara/setup-domain.asadmin"));
        String loginConf = Files.readString(Path.of("../deploy/payara/login.conf.snippet"));

        assertThat(setup)
                .contains("globaltradeSupplyChainRealm")
                .contains("com.hiru.globaltrade.security.jaas.SupplyChainLoginModule")
                .contains("password=${ALIAS=globaltrade.db.password}")
                .contains("datasource-jndi=jdbc/globaltradeDS")
                .contains("auth_users")
                .contains("auth_user_roles")
                .doesNotContain("auth.db.password")
                .doesNotContain("GlobalTrade#2026!");
        assertThat(loginConf).contains("SupplyChainLoginModule required");
    }
}
