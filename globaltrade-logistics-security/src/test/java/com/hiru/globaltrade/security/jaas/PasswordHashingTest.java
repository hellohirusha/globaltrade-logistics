package com.hiru.globaltrade.security.jaas;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHashingTest {
    private static final char[] TEST_PASSWORD = "test-only-password".toCharArray();
    private static final String TEST_SALT = "Z2xvYmFsdHJhZGUtdGVzdC1jcmVkZW50aWFs";
    private static final String TEST_HASH = "0hDwGkV23+CsMbXVwCBB8Xn70GH1faQ2BRztd9Rawd4=";
    private static final int TEST_ITERATIONS = 120000;

    @Test
    void verifiesKnownCredentialHash() {
        boolean matches = PasswordHashing.matches(
                TEST_PASSWORD,
                TEST_SALT,
                TEST_ITERATIONS,
                TEST_HASH
        );

        assertThat(matches).isTrue();
    }

    @Test
    void rejectsIncorrectCredentialWithConstantLengthHashComparison() {
        boolean matches = PasswordHashing.matches(
                "wrong-password".toCharArray(),
                TEST_SALT,
                TEST_ITERATIONS,
                TEST_HASH
        );

        assertThat(matches).isFalse();
    }

    @Test
    void producesStablePbkdf2HashForKnownSalt() {
        String hash = PasswordHashing.hash(
                TEST_PASSWORD,
                "globaltrade-test-credential".getBytes(StandardCharsets.UTF_8),
                TEST_ITERATIONS
        );

        assertThat(hash).isEqualTo(TEST_HASH);
    }
}
