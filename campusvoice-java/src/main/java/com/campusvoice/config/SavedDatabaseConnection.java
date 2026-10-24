package com.campusvoice.config;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Windows DPAPI encryption, bound to the current Windows account. No plaintext credential file. */
public final class SavedDatabaseConnection {
    private SavedDatabaseConnection() {}
    private static Path path() {
        return Path.of(System.getProperty("user.home"), ".campusvoice", "connection.dpapi");
    }
    public static void save(String url, String user, String password) throws IOException {
        Properties settings = new Properties();
        settings.setProperty("url", url);
        settings.setProperty("user", user);
        settings.setProperty("password", password);
        save(path(), settings);
    }
    public static Optional<Properties> load() throws IOException { return load(path()); }

    static void save(Path target, Properties settings) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        settings.store(buffer, "CampusVoice connection");
        byte[] plain = buffer.toByteArray();
        byte[] encrypted;
        try { encrypted = crypt(plain, true); }
        finally { Arrays.fill(plain, (byte) 0); }
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), "connection-", ".tmp");
        try {
            AclFileAttributeView acl = Files.getFileAttributeView(temporary, AclFileAttributeView.class);
            if (acl == null) throw new IOException("Windows file permissions are unavailable.");
            acl.setAcl(List.of(AclEntry.newBuilder().setType(AclEntryType.ALLOW)
                    .setPrincipal(Files.getOwner(temporary)).setPermissions(EnumSet.allOf(AclEntryPermission.class)).build()));
            Files.write(temporary, encrypted);
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temporary); }
    }

    static Optional<Properties> load(Path target) throws IOException {
        if (!Files.exists(target)) return Optional.empty();
        byte[] plain = crypt(Files.readAllBytes(target), false);
        try {
            Properties settings = new Properties();
            settings.load(new ByteArrayInputStream(plain));
            for (String key : List.of("url", "user", "password")) {
                if (settings.getProperty(key) == null) throw new IOException("Saved connection is incomplete.");
            }
            return Optional.of(settings);
        } finally { Arrays.fill(plain, (byte) 0); }
    }

    private static byte[] crypt(byte[] input, boolean encrypt) throws IOException {
        if (!System.getProperty("os.name").startsWith("Windows"))
            throw new IOException("Remembering database settings requires Windows DPAPI.");
        String operation = encrypt ? "Protect" : "Unprotect";
        String script = "$ErrorActionPreference='Stop'; Add-Type -AssemblyName System.Security; "
                + "$data=[Convert]::FromBase64String([Console]::In.ReadToEnd()); "
                + "$result=[System.Security.Cryptography.ProtectedData]::" + operation
                + "($data,$null,[System.Security.Cryptography.DataProtectionScope]::CurrentUser); "
                + "[Console]::Out.Write([Convert]::ToBase64String($result))";
        Path powershell = Path.of(System.getenv("SystemRoot"), "System32", "WindowsPowerShell", "v1.0", "powershell.exe");
        Process process = new ProcessBuilder(powershell.toString(), "-NoProfile", "-NonInteractive",
                "-WindowStyle", "Hidden", "-Command", script)
                .redirectError(ProcessBuilder.Redirect.DISCARD).start();
        try {
            try (var out = process.getOutputStream()) { out.write(Base64.getEncoder().encode(input)); }
            if (!process.waitFor(20, TimeUnit.SECONDS)) throw new IOException("Windows credential encryption timed out.");
            if (process.exitValue() != 0) throw new IOException("Windows could not protect or restore the connection settings.");
            try { return Base64.getDecoder().decode(process.getInputStream().readAllBytes()); }
            catch (IllegalArgumentException error) { throw new IOException("Invalid encrypted connection response."); }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IOException("Credential encryption interrupted.");
        } finally { if (process.isAlive()) process.destroyForcibly(); }
    }
}
