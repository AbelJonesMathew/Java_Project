package com.campusvoice.service;

import com.campusvoice.dao.impl.AttachmentDAOImpl;
import com.campusvoice.model.Attachment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class AttachmentServiceTest {
    @TempDir Path directory;
    private Attachment attachment() {
        Attachment attachment = new Attachment();
        attachment.setComplaintId("a1111111-1111-1111-1111-111111111111");
        attachment.setUploadedBy("55555555-5555-5555-5555-555555555555");
        attachment.setFileName("evidence.png");
        attachment.setFileType("image/png");
        return attachment;
    }
    @Test void receiverDownloadsEvidenceAfterOriginalFileIsRemoved() throws Exception {
        byte[] image = new byte[]{(byte)137, 80, 78, 71, 13, 10, 26, 10, 1, 2, 3};
        Path original = Files.write(directory.resolve("evidence.png"), image);
        Attachment attachment = attachment();
        new AttachmentService().upload(attachment, original);
        Files.delete(original);
        Attachment fresh = new AttachmentDAOImpl().findByComplaintId(attachment.getComplaintId()).stream()
                .filter(a -> a.getId().equals(attachment.getId())).findFirst().orElseThrow();
        assertTrue(fresh.getStoragePath().startsWith("database:"));
        Path received = new AttachmentService(directory.resolve("receiver")).download(fresh, false);
        assertArrayEquals(image, Files.readAllBytes(received));
    }
    @Test void ownerCanMigrateOlderAttachmentAndReceiverCanThenReadIt() throws Exception {
        byte[] image = new byte[]{10, 20, 30};
        Path original = Files.write(directory.resolve("old.png"), image);
        Attachment attachment = attachment();
        attachment.setStoragePath(original.toString());
        attachment.setFileSize(image.length);
        AttachmentDAOImpl dao = new AttachmentDAOImpl();
        assertTrue(dao.saveAttachment(attachment));
        AttachmentService service = new AttachmentService(directory.resolve("receiver"));
        assertThrows(java.io.IOException.class, () -> service.download(attachment, false));
        service.download(attachment, true);
        Files.delete(original);
        assertArrayEquals(image, Files.readAllBytes(service.download(attachment, false)));
    }
    @Test void oversizedEvidenceIsRejectedBeforeSavingMetadata() throws Exception {
        Path original = directory.resolve("too-large.png");
        try (var file = new java.io.RandomAccessFile(original.toFile(), "rw")) {
            file.setLength(AttachmentDAOImpl.MAX_FILE_BYTES + 1L);
        }
        Attachment attachment = attachment();
        assertThrows(java.io.IOException.class, () -> new AttachmentService().upload(attachment, original));
        assertNull(attachment.getId());
    }
}
