package com.campusvoice.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasher {
    private static final Logger logger = LoggerFactory.getLogger(PasswordHasher.class);
    private static final String DEFAULT_SALT = "cvsalt123";

    public static String hashPassword(String rawPassword) {
        return hashPasswordWithSalt(rawPassword, generateSalt());
    }

    public static String hashPasswordWithSalt(String rawPassword, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes());
            byte[] hash = digest.digest(rawPassword.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return salt + ":" + hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm missing", e);
            return rawPassword;
        }
    }

    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (storedHash == null || rawPassword == null) return false;
        
        String[] parts = storedHash.split(":");
        if (parts.length != 2) {
            // Fallback check against raw default salt
            String testHash = hashPasswordWithSalt(rawPassword, DEFAULT_SALT);
            return testHash.equals(storedHash);
        }
        
        String salt = parts[0];
        String computedHash = hashPasswordWithSalt(rawPassword, salt);
        return computedHash.equals(storedHash);
    }

    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt).replaceAll("[^a-zA-Z0-9]", "");
    }
}
