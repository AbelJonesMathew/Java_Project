package com.campusvoice.service;

import com.campusvoice.dao.impl.AttachmentDAOImpl;
import com.campusvoice.model.Attachment;
import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;

public class AttachmentService {
    private final AttachmentDAOImpl dao = new AttachmentDAOImpl();
    private final Path downloadDirectory;
    public AttachmentService() { this(Path.of(System.getProperty("user.home"), ".campusvoice", "downloads")); }
    public AttachmentService(Path downloadDirectory) { this.downloadDirectory = downloadDirectory; }
    public void upload(Attachment attachment, Path file) throws IOException {
        byte[] content = readContent(file);
        if (!dao.saveAttachment(attachment, content)) throw new IOException("The evidence could not be uploaded to the shared database.");
    }
    public Path download(Attachment attachment, boolean allowLegacyUpload) throws IOException, SQLException {
        byte[] content = dao.getContent(attachment.getId());
        if (content == null && allowLegacyUpload && attachment.getStoragePath() != null
                && !attachment.getStoragePath().startsWith("database:")) {
            Path original = Path.of(attachment.getStoragePath());
            if (Files.isRegularFile(original)) {
                content = readContent(original);
                if (!dao.storeContent(attachment.getId(), content)) throw new IOException("Could not share the existing evidence file.");
            }
        }
        if (content == null) throw new IOException("This older attachment exists only on the uploader's computer. "
                + "Open it there with v1.0.3 to upload it to the shared database, then retry here.");
        Path directory = downloadDirectory;
        Files.createDirectories(directory);
        Path temporary = Files.createTempDirectory(directory, "evidence-");
        String name = attachment.getFileName() == null ? "evidence" : attachment.getFileName().replaceAll("[^a-zA-Z0-9._-]", "_");
        if (name.isBlank() || name.equals(".") || name.equals("..")) name = "evidence";
        Path destination = temporary.resolve(name);
        Files.write(destination, content);
        return destination;
    }

    private byte[] readContent(Path file) throws IOException {
        if (Files.size(file) > AttachmentDAOImpl.MAX_FILE_BYTES) throw new IOException("Evidence files must be 10 MB or smaller.");
        try (var input = Files.newInputStream(file)) {
            byte[] content = input.readNBytes(AttachmentDAOImpl.MAX_FILE_BYTES + 1);
            if (content.length > AttachmentDAOImpl.MAX_FILE_BYTES) throw new IOException("Evidence files must be 10 MB or smaller.");
            return content;
        }
    }
}
