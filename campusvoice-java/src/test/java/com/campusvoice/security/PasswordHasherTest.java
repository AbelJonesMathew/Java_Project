package com.campusvoice.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordHasherTest {

    @Test
    public void testHashPasswordAndVerification() {
        String rawPassword = "Password123!";
        String hash = PasswordHasher.hashPassword(rawPassword);

        assertNotNull(hash);
        assertTrue(hash.contains(":"));

        assertTrue(PasswordHasher.verifyPassword(rawPassword, hash));
        assertFalse(PasswordHasher.verifyPassword("WrongPassword", hash));
    }

    @Test
    public void testPrecomputedSaltVerification() {
        String precomputed = "cvsalt123:9d7cea4adcaaf9e38ebd9ffe2e073366103ed9ad721141bb0a74ad05c267a0f8";
        assertTrue(PasswordHasher.verifyPassword("Password123!", precomputed));
    }
}
