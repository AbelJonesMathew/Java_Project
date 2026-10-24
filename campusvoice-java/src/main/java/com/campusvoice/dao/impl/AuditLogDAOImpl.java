package com.campusvoice.dao.impl;

import com.campusvoice.config.DatabaseConfig;
import com.campusvoice.dao.AuditLogDAO;
import com.campusvoice.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuditLogDAOImpl implements AuditLogDAO {
    private static final Logger logger = LoggerFactory.getLogger(AuditLogDAOImpl.class);

    @Override
    public boolean logAction(AuditLog log) {
        String sql = "INSERT INTO audit_logs (id, user_id, action, entity, entity_id, details, ip_address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (log.getId() == null || log.getId().isBlank()) {
                log.setId(UUID.randomUUID().toString());
            }
            try {
                stmt.setObject(1, UUID.fromString(log.getId()));
            } catch (Exception e) {
                stmt.setString(1, log.getId());
            }

            if (log.getUserId() != null && !log.getUserId().isBlank()) {
                try {
                    stmt.setObject(2, UUID.fromString(log.getUserId()));
                } catch (Exception e) {
                    stmt.setString(2, log.getUserId());
                }
            } else {
                stmt.setNull(2, java.sql.Types.OTHER);
            }
            stmt.setString(3, log.getAction());
            stmt.setString(4, log.getEntity());
            stmt.setString(5, log.getEntityId());
            stmt.setString(6, log.getDetails());
            stmt.setString(7, log.getIpAddress() != null ? log.getIpAddress() : "127.0.0.1");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error writing audit log", e);
            return false;
        }
    }

    @Override
    public List<AuditLog> findAll(int limit) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT a.*, u.full_name AS user_name FROM audit_logs a LEFT JOIN users u ON a.user_id = u.id ORDER BY a.created_at DESC LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit > 0 ? limit : 100);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                AuditLog al = new AuditLog();
                al.setId(rs.getString("id"));
                al.setUserId(rs.getString("user_id"));
                al.setUserName(rs.getString("user_name"));
                al.setAction(rs.getString("action"));
                al.setEntity(rs.getString("entity"));
                al.setEntityId(rs.getString("entity_id"));
                al.setDetails(rs.getString("details"));
                al.setIpAddress(rs.getString("ip_address"));
                al.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(al);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving audit logs", e);
        }
        return list;
    }
}
