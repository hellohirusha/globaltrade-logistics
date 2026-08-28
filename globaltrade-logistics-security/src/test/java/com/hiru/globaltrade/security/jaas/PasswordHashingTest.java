package com.hiru.globaltrade.security.jaas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHashingTest {
    @Test
    void verifiesSeededAdminCredentialHash() {
        boolean matches = PasswordHashing.matches(
                "GlobalTrade#2026!".toCharArray(),
                "Z2xvYmFsdHJhZGUtYWRtaW4tMjAyNg==",
                120000,
                "o6Ny+yG45fNuXrfkSrF8ymEtiB2EOSoRP2u6TZ1ithI="
        );

        assertThat(matches).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "admin,Z2xvYmFsdHJhZGUtYWRtaW4tMjAyNg==,o6Ny+yG45fNuXrfkSrF8ymEtiB2EOSoRP2u6TZ1ithI=",
            "coordinator,Z2xvYmFsdHJhZGUtY29vcmRpbmF0b3ItMjAyNg==,68iLtMB8q/O22GUfM9soJGDR21gFUQQcQCqJszuyIIg=",
            "warehouse,Z2xvYmFsdHJhZGUtd2FyZWhvdXNlLTIwMjY=,EFJhVfD01QO5X9KcmQ7gb2xQlExErpzHuQ7eb90RoQg=",
            "customs,Z2xvYmFsdHJhZGUtY3VzdG9tcy0yMDI2,qjIq0w+vLkfu1NjhJ0D7uPlNy9oo3EUZimPKNPM7fSI=",
            "vendor,Z2xvYmFsdHJhZGUtdmVuZG9yLTIwMjY=,ln8ZUvPp3+jk4Z0VxOFEZSUUv7foh4fGVOdFizFex8k=",
            "customer,Z2xvYmFsdHJhZGUtY3VzdG9tZXItMjAyNg==,hJRPd2GgOkQuJwWP6I34bVrEGEpW5LdX0rh/EKT6Guk="
    })
    void verifiesEverySeededBootstrapCredential(String username, String salt, String hash) {
        assertThat(username).isNotBlank();

        boolean matches = PasswordHashing.matches("GlobalTrade#2026!".toCharArray(), salt, 120000, hash);

        assertThat(matches).isTrue();
    }

    @Test
    void rejectsIncorrectCredentialWithConstantLengthHashComparison() {
        boolean matches = PasswordHashing.matches(
                "wrong-password".toCharArray(),
                "Z2xvYmFsdHJhZGUtYWRtaW4tMjAyNg==",
                120000,
                "o6Ny+yG45fNuXrfkSrF8ymEtiB2EOSoRP2u6TZ1ithI="
        );

        assertThat(matches).isFalse();
    }

    @Test
    void producesStablePbkdf2HashForKnownSalt() {
        String hash = PasswordHashing.hash(
                "GlobalTrade#2026!".toCharArray(),
                "globaltrade-admin-2026".getBytes(StandardCharsets.UTF_8),
                120000
        );

        assertThat(hash).isEqualTo("o6Ny+yG45fNuXrfkSrF8ymEtiB2EOSoRP2u6TZ1ithI=");
    }
}
