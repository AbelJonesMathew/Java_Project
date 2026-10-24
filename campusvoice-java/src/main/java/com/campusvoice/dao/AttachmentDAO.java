package com.campusvoice.dao;

import com.campusvoice.model.Attachment;
import java.util.List;

public interface AttachmentDAO {
    boolean saveAttachment(Attachment attachment);
    boolean saveAttachment(Attachment attachment, byte[] content);
    boolean storeContent(String attachmentId, byte[] content);
    byte[] getContent(String attachmentId) throws java.sql.SQLException;
    List<Attachment> findByComplaintId(String complaintId);
}
