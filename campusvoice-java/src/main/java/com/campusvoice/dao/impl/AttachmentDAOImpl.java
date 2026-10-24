package com.campusvoice.dao.impl;

import com.campusvoice.config.DatabaseConfig;
import com.campusvoice.dao.AttachmentDAO;
import com.campusvoice.model.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.*;

public class AttachmentDAOImpl implements AttachmentDAO {
    private static final Logger logger = LoggerFactory.getLogger(AttachmentDAOImpl.class);
    public static final int MAX_FILE_BYTES = 10 * 1024 * 1024;

    private void ensureTables(Connection connection) throws SQLException {
        boolean postgres = connection.getMetaData().getDatabaseProductName().equals("PostgreSQL");
        String idType = postgres ? "UUID" : "VARCHAR(50)";
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS complaint_attachments (id " + idType
                    + " PRIMARY KEY, complaint_id " + idType + " NOT NULL, file_name VARCHAR(255) NOT NULL, "
                    + "file_type VARCHAR(100) NOT NULL, file_size BIGINT NOT NULL, storage_path TEXT NOT NULL, "
                    + "uploaded_by " + idType + ", created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            statement.execute("CREATE TABLE IF NOT EXISTS complaint_attachment_data (attachment_id VARCHAR(50) PRIMARY KEY, "
                    + "file_content " + (postgres ? "BYTEA" : "VARBINARY") + " NOT NULL)");
        }
    }
    private void bindId(PreparedStatement statement, int index, String id) throws SQLException {
        if (id == null || id.isBlank()) { statement.setNull(index, Types.OTHER); return; }
        if (statement.getConnection().getMetaData().getDatabaseProductName().equals("PostgreSQL"))
            statement.setObject(index, UUID.fromString(id));
        else statement.setString(index, id);
    }
    @Override public boolean saveAttachment(Attachment attachment) { return saveAttachment(attachment, null); }

    @Override public boolean saveAttachment(Attachment attachment, byte[] content) {
        if (content != null && content.length > MAX_FILE_BYTES) throw new IllegalArgumentException("Evidence files must be 10 MB or smaller.");
        String id = UUID.randomUUID().toString();
        String complaintId = attachment.getComplaintId();
        if (complaintId != null && complaintId.startsWith("#"))
            complaintId = new ComplaintDAOImpl().findByCode(complaintId).map(c -> c.getId()).orElse(complaintId);
        String storage = content == null ? attachment.getStoragePath() : "database:" + id;
        try (Connection connection = DatabaseConfig.getConnection()) {
            ensureTables(connection);
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO complaint_attachments (id, complaint_id, file_name, file_type, file_size, storage_path, uploaded_by) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    bindId(statement, 1, id);
                    bindId(statement, 2, complaintId);
                    statement.setString(3, attachment.getFileName());
                    statement.setString(4, attachment.getFileType() != null ? attachment.getFileType() : "application/octet-stream");
                    statement.setLong(5, content != null ? content.length : attachment.getFileSize());
                    statement.setString(6, storage);
                    bindId(statement, 7, attachment.getUploadedBy());
                    statement.executeUpdate();
                }
                if (content != null) insertContent(connection, id, content);
                connection.commit();
                attachment.setId(id);
                attachment.setStoragePath(storage);
                return true;
            } catch (SQLException | RuntimeException error) { connection.rollback(); throw error; }
        } catch (SQLException error) {
            logger.error("Could not save evidence for complaint {}", attachment.getComplaintId(), error);
            return false;
        }
    }
    private void insertContent(Connection connection, String id, byte[] content) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO complaint_attachment_data (attachment_id, file_content) VALUES (?, ?)")) {
            statement.setString(1, id);
            statement.setBytes(2, content);
            statement.executeUpdate();
        }
    }
    @Override public byte[] getContent(String attachmentId) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection()) {
            ensureTables(connection);
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT file_content FROM complaint_attachment_data WHERE attachment_id = ?")) {
                statement.setString(1, attachmentId);
                try (ResultSet result = statement.executeQuery()) { return result.next() ? result.getBytes(1) : null; }
            }
        }
    }
    @Override public boolean storeContent(String attachmentId, byte[] content) {
        if (content == null || content.length > MAX_FILE_BYTES) throw new IllegalArgumentException("Evidence files must be 10 MB or smaller.");
        try (Connection connection = DatabaseConfig.getConnection()) {
            ensureTables(connection);
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE complaint_attachments SET storage_path = ?, file_size = ? WHERE CAST(id AS VARCHAR) = ?")) {
                    statement.setString(1, "database:" + attachmentId);
                    statement.setLong(2, content.length);
                    statement.setString(3, attachmentId);
                    if (statement.executeUpdate() != 1) { connection.rollback(); return false; }
                }
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM complaint_attachment_data WHERE attachment_id = ?")) {
                    statement.setString(1, attachmentId);
                    statement.executeUpdate();
                }
                insertContent(connection, attachmentId, content);
                connection.commit();
                return true;
            } catch (SQLException error) { connection.rollback(); throw error; }
        } catch (SQLException error) { logger.error("Could not upload existing evidence {}", attachmentId, error); return false; }
    }
    @Override public List<Attachment> findByComplaintId(String complaintId) {
        String target = complaintId;
        if (target != null && target.startsWith("#")) target = new ComplaintDAOImpl().findByCode(target).map(c -> c.getId()).orElse(target);
        List<Attachment> attachments = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection()) {
            ensureTables(connection);
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM complaint_attachments WHERE CAST(complaint_id AS VARCHAR) = ? ORDER BY created_at DESC")) {
                statement.setString(1, target);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        Attachment attachment = new Attachment();
                        attachment.setId(result.getString("id"));
                        attachment.setComplaintId(result.getString("complaint_id"));
                        attachment.setFileName(result.getString("file_name"));
                        attachment.setFileType(result.getString("file_type"));
                        attachment.setFileSize(result.getLong("file_size"));
                        attachment.setStoragePath(result.getString("storage_path"));
                        attachment.setUploadedBy(result.getString("uploaded_by"));
                        attachment.setCreatedAt(result.getTimestamp("created_at"));
                        attachments.add(attachment);
                    }
                }
            }
        } catch (SQLException error) { logger.error("Could not load evidence for complaint {}", complaintId, error); }
        return attachments;
    }
}
