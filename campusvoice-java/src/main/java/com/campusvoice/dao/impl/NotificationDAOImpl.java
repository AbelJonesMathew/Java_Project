package com.campusvoice.dao.impl;

import com.campusvoice.config.DatabaseConfig;
import com.campusvoice.dao.NotificationDAO;
import com.campusvoice.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotificationDAOImpl implements NotificationDAO {
    private static final Logger logger = LoggerFactory.getLogger(NotificationDAOImpl.class);

    @Override
    public boolean createNotification(Notification n) {
        String sql = "INSERT INTO notifications (id, user_id, title, message, type, entity_id, is_read) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (n.getId() == null || n.getId().isBlank()) {
                n.setId(UUID.randomUUID().toString());
            }
            try {
                stmt.setObject(1, UUID.fromString(n.getId()));
            } catch (Exception e) {
                stmt.setString(1, n.getId());
            }

            if (n.getUserId() != null && !n.getUserId().isBlank()) {
                try {
                    stmt.setObject(2, UUID.fromString(n.getUserId()));
                } catch (Exception e) {
                    stmt.setString(2, n.getUserId());
                }
            } else {
                stmt.setNull(2, java.sql.Types.OTHER);
            }

            stmt.setString(3, n.getTitle());
            stmt.setString(4, n.getMessage());
            stmt.setString(5, n.getType());
            stmt.setString(6, n.getEntityId());
            stmt.setBoolean(7, n.isRead());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating notification for user {}", n.getUserId(), e);
            return false;
        }
    }

    @Override
    public List<Notification> findByUserId(String userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE CAST(user_id AS VARCHAR) = ? ORDER BY created_at DESC LIMIT 50";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getString("id"));
                n.setUserId(rs.getString("user_id"));
                n.setTitle(rs.getString("title"));
                n.setMessage(rs.getString("message"));
                n.setType(rs.getString("type"));
                n.setEntityId(rs.getString("entity_id"));
                n.setRead(rs.getBoolean("is_read"));
                n.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(n);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving notifications for user {}", userId, e);
        }
        return list;
    }

    @Override
    public boolean markAsRead(String notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE CAST(id AS VARCHAR) = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error marking notification {} read", notificationId, e);
            return false;
        }
    }

    @Override
    public boolean markAllAsRead(String userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE CAST(user_id AS VARCHAR) = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error marking notifications read for user {}", userId, e);
            return false;
        }
    }
}
