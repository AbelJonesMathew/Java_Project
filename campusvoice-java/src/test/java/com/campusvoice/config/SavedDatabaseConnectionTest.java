package com.campusvoice.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

class SavedDatabaseConnectionTest {
    @TempDir Path directory;
    @Test void encryptedSettingsRoundTripWithoutPlaintextCredentials() throws Exception {
        Properties settings = new Properties();
        settings.setProperty("url", "jdbc:postgresql://example.invalid/postgres");
        settings.setProperty("user", "test-admin");
        settings.setProperty("password", "fake-test-password-മലയാളം-=!");
        Path file = directory.resolve("connection.dpapi");
        SavedDatabaseConnection.save(file, settings);
        assertEquals(settings, SavedDatabaseConnection.load(file).orElseThrow());
        String stored = new String(Files.readAllBytes(file), java.nio.charset.StandardCharsets.ISO_8859_1);
        assertFalse(stored.contains("fake-test-password"));
        assertFalse(stored.contains("example.invalid"));
        var acl = Files.getFileAttributeView(file, java.nio.file.attribute.AclFileAttributeView.class);
        assertEquals(1, acl.getAcl().size());
        assertEquals(Files.getOwner(file), acl.getAcl().get(0).principal());
    }
    @Test void missingOrCorruptSettingsDoNotReturnCredentials() throws Exception {
        Path file = directory.resolve("connection.dpapi");
        assertTrue(SavedDatabaseConnection.load(file).isEmpty());
        Files.write(file, new byte[]{1, 2, 3});
        assertThrows(java.io.IOException.class, () -> SavedDatabaseConnection.load(file));
    }
}
