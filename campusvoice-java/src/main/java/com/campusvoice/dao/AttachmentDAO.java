package com.campusvoice.dao;

import com.campusvoice.model.Attachment;
import java.util.List;

public interface AttachmentDAO {
    boolean saveAttachment(Attachment attachment);
    List<Attachment> findByComplaintId(String complaintId);
}
