package com.hiru.globaltrade.security.jaas;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

final class PasswordHashing {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_LENGTH_BITS = 256;

    private PasswordHashing() {
    }

    static String hash(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
            byte[] hash = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to hash supplied credential.", ex);
        }
    }

    static boolean matches(char[] password, String salt, int iterations, String expectedHash) {
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        byte[] expected = Base64.getDecoder().decode(expectedHash);
        byte[] actual = Base64.getDecoder().decode(hash(password, saltBytes, iterations));
        return MessageDigest.isEqual(expected, actual);
    }
}
