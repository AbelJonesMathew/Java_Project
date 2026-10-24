package com.campusvoice.dao.impl;

import com.campusvoice.config.DatabaseConfig;
import com.campusvoice.dao.AttachmentDAO;
import com.campusvoice.dao.ComplaintDAO;
import com.campusvoice.model.Attachment;
import com.campusvoice.model.Complaint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AttachmentDAOImpl implements AttachmentDAO {
    private static final Logger logger = LoggerFactory.getLogger(AttachmentDAOImpl.class);

    // Ensure the complaint_attachments table exists (H2 fallback creates it here too)
    static {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS complaint_attachments (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "complaint_id VARCHAR(50) NOT NULL, " +
                "file_name VARCHAR(255) NOT NULL, " +
                "file_type VARCHAR(100) NOT NULL, " +
                "file_size BIGINT NOT NULL, " +
                "storage_path TEXT NOT NULL, " +
                "uploaded_by VARCHAR(50), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
        } catch (SQLException e) {
            // Table may already exist with UUID type in PostgreSQL – that's fine
            LoggerFactory.getLogger(AttachmentDAOImpl.class)
                .debug("complaint_attachments table already exists or using PostgreSQL schema: {}", e.getMessage());
        }
    }

    @Override
    public boolean saveAttachment(Attachment a) {
        String sql = "INSERT INTO complaint_attachments (id, complaint_id, file_name, file_type, file_size, storage_path, uploaded_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String id = UUID.randomUUID().toString();
            try {
                stmt.setObject(1, UUID.fromString(id));
            } catch (Exception e) {
                stmt.setString(1, id);
            }

            String targetComplaintId = a.getComplaintId();
            if (targetComplaintId != null && (targetComplaintId.startsWith("#") || targetComplaintId.startsWith("CP") || targetComplaintId.length() < 30)) {
                try {
                    ComplaintDAO cDao = new ComplaintDAOImpl();
                    Optional<Complaint> found = cDao.findByCode(targetComplaintId);
                    if (found.isPresent()) {
                        targetComplaintId = found.get().getId();
                    }
                } catch (Exception ignored) {}
            }

            try {
                stmt.setObject(2, UUID.fromString(targetComplaintId));
            } catch (Exception e) {
                stmt.setString(2, targetComplaintId);
            }

            stmt.setString(3, a.getFileName());
            stmt.setString(4, a.getFileType() != null ? a.getFileType() : "application/octet-stream");
            stmt.setLong(5, a.getFileSize());
            stmt.setString(6, a.getStoragePath());

            if (a.getUploadedBy() != null && !a.getUploadedBy().isBlank()) {
                try {
                    stmt.setObject(7, UUID.fromString(a.getUploadedBy()));
                } catch (Exception e) {
                    stmt.setString(7, a.getUploadedBy());
                }
            } else {
                stmt.setNull(7, java.sql.Types.OTHER);
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error saving attachment for complaint {}", a.getComplaintId(), e);
            return false;
        }
    }

    @Override
    public List<Attachment> findByComplaintId(String complaintId) {
        List<Attachment> list = new ArrayList<>();
        String targetComplaintId = complaintId;
        if (targetComplaintId != null && (targetComplaintId.startsWith("#") || targetComplaintId.startsWith("CP") || targetComplaintId.length() < 30)) {
            try {
                ComplaintDAO cDao = new ComplaintDAOImpl();
                Optional<Complaint> found = cDao.findByCode(targetComplaintId);
                if (found.isPresent()) {
                    targetComplaintId = found.get().getId();
                }
            } catch (Exception ignored) {}
        }

        String sql = "SELECT * FROM complaint_attachments WHERE CAST(complaint_id AS VARCHAR) = ? OR CAST(complaint_id AS VARCHAR) = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, complaintId);
            stmt.setString(2, targetComplaintId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Attachment a = new Attachment();
                a.setId(rs.getString("id"));
                a.setComplaintId(rs.getString("complaint_id"));
                a.setFileName(rs.getString("file_name"));
                a.setFileType(rs.getString("file_type"));
                a.setFileSize(rs.getLong("file_size"));
                a.setStoragePath(rs.getString("storage_path"));
                a.setUploadedBy(rs.getString("uploaded_by"));
                a.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(a);
            }
        } catch (SQLException e) {
            logger.error("Error loading attachments for complaint {}", complaintId, e);
        }
        return list;
    }
}
