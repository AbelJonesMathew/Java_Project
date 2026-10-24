package com.campusvoice.dao.impl;

import com.campusvoice.config.DatabaseConfig;
import com.campusvoice.dao.CommentDAO;
import com.campusvoice.dao.ComplaintDAO;
import com.campusvoice.model.Comment;
import com.campusvoice.model.Complaint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommentDAOImpl implements CommentDAO {
    private static final Logger logger = LoggerFactory.getLogger(CommentDAOImpl.class);

    @Override
    public boolean createComment(Comment comment) {
        String sql = "INSERT INTO complaint_comments (id, complaint_id, user_id, comment_text, is_internal) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (comment.getId() == null || comment.getId().isBlank()) {
                comment.setId(UUID.randomUUID().toString());
            }

            String cid = comment.getComplaintId();
            if (cid != null && (cid.startsWith("#") || cid.startsWith("CP"))) {
                ComplaintDAO cDao = new ComplaintDAOImpl();
                cid = cDao.findByCode(cid).map(Complaint::getId).orElse(cid);
            }

            try {
                stmt.setObject(1, UUID.fromString(comment.getId()));
            } catch (Exception e) {
                stmt.setString(1, comment.getId());
            }

            try {
                stmt.setObject(2, UUID.fromString(cid));
            } catch (Exception e) {
                stmt.setString(2, cid);
            }

            if (comment.getUserId() != null && !comment.getUserId().isBlank()) {
                try {
                    stmt.setObject(3, UUID.fromString(comment.getUserId()));
                } catch (Exception e) {
                    stmt.setString(3, comment.getUserId());
                }
            } else {
                stmt.setNull(3, java.sql.Types.OTHER);
            }

            stmt.setString(4, comment.getCommentText());
            stmt.setBoolean(5, comment.isInternal());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating comment for complaint {}", comment.getComplaintId(), e);
            return false;
        }
    }

    @Override
    public List<Comment> findByComplaintId(String complaintId) {
        List<Comment> list = new ArrayList<>();
        String sql = "SELECT c.*, COALESCE(u.full_name, CAST(c.user_id AS VARCHAR)) AS user_name, COALESCE(r.role_name, 'OFFICIAL') AS user_role " +
                     "FROM complaint_comments c " +
                     "LEFT JOIN users u ON (c.user_id = u.id OR CAST(c.user_id AS VARCHAR) = u.reg_no) " +
                     "LEFT JOIN roles r ON u.role_id = r.id " +
                     "WHERE CAST(c.complaint_id AS VARCHAR) = ? OR c.complaint_id IN (SELECT id FROM complaints WHERE complaint_code = ?) " +
                     "ORDER BY c.created_at ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, complaintId);
            stmt.setString(2, complaintId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Comment cm = new Comment();
                cm.setId(rs.getString("id"));
                cm.setComplaintId(rs.getString("complaint_id"));
                cm.setUserId(rs.getString("user_id"));
                cm.setUserName(rs.getString("user_name"));
                cm.setUserRole(rs.getString("user_role"));
                cm.setCommentText(rs.getString("comment_text"));
                cm.setInternal(rs.getBoolean("is_internal"));
                cm.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(cm);
            }
        } catch (SQLException e) {
            logger.error("Error listing comments for complaint {}", complaintId, e);
        }
        return list;
    }
}
