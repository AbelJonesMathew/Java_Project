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
import java.util.Optional;
import java.util.UUID;

public class ComplaintDAOImpl implements ComplaintDAO {
    private static final Logger logger = LoggerFactory.getLogger(ComplaintDAOImpl.class);

    private static final String BASE_SELECT = 
        "SELECT c.*, cat.category_name, " +
        "COALESCE(u.full_name, 'Student') AS student_name, " +
        "COALESCE(au.full_name, 'Unassigned') AS assigned_to_name " +
        "FROM complaints c " +
        "JOIN complaint_categories cat ON c.category_id = cat.id " +
        "LEFT JOIN users u ON (c.student_id = u.id OR CAST(c.student_id AS VARCHAR) = u.reg_no) " +
        "LEFT JOIN users au ON (c.assigned_to = au.id OR CAST(c.assigned_to AS VARCHAR) = au.reg_no) ";

    @Override
    public boolean createComplaint(Complaint complaint) {
        String sql = "INSERT INTO complaints (id, complaint_code, student_id, title, description, original_text, " +
                     "original_language, translated_text, category_id, priority, priority_confidence, ai_reason, " +
                     "status, is_anonymous, is_group_complaint, assigned_to) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (complaint.getId() == null || complaint.getId().isBlank()) {
                complaint.setId(UUID.randomUUID().toString());
            }
            if (complaint.getComplaintCode() == null || complaint.getComplaintCode().isBlank()) {
                complaint.setComplaintCode("#CP" + String.format("%03d", (int)(Math.random() * 900 + 100)));
            }
            if (complaint.getStatus() == null || complaint.getStatus().isBlank()) {
                complaint.setStatus("SUBMITTED");
            }
            if (complaint.getPriority() == null || complaint.getPriority().isBlank()) {
                complaint.setPriority("MEDIUM");
            }
            if (complaint.getOriginalLanguage() == null || complaint.getOriginalLanguage().isBlank()) {
                complaint.setOriginalLanguage("English");
            }

            try {
                stmt.setObject(1, UUID.fromString(complaint.getId()));
            } catch (Exception e) {
                stmt.setString(1, complaint.getId());
            }

            stmt.setString(2, complaint.getComplaintCode());

            if (complaint.getStudentId() != null && !complaint.getStudentId().isBlank()) {
                try {
                    stmt.setObject(3, UUID.fromString(complaint.getStudentId()));
                } catch (Exception e) {
                    stmt.setString(3, complaint.getStudentId());
                }
            } else {
                stmt.setNull(3, java.sql.Types.OTHER);
            }

            stmt.setString(4, complaint.getTitle());
            stmt.setString(5, complaint.getDescription());
            stmt.setString(6, complaint.getOriginalText() != null ? complaint.getOriginalText() : complaint.getDescription());
            stmt.setString(7, complaint.getOriginalLanguage());
            stmt.setString(8, complaint.getTranslatedText() != null ? complaint.getTranslatedText() : complaint.getDescription());
            stmt.setInt(9, complaint.getCategoryId() > 0 ? complaint.getCategoryId() : 1);
            stmt.setString(10, complaint.getPriority());
            stmt.setDouble(11, complaint.getPriorityConfidence() > 0 ? complaint.getPriorityConfidence() : 85.0);
            stmt.setString(12, complaint.getAiReason() != null ? complaint.getAiReason() : "Grievance registered.");
            stmt.setString(13, complaint.getStatus());
            stmt.setBoolean(14, complaint.isAnonymous());
            stmt.setBoolean(15, complaint.isGroupComplaint());

            if (complaint.getAssignedTo() != null && !complaint.getAssignedTo().isBlank()) {
                try {
                    stmt.setObject(16, UUID.fromString(complaint.getAssignedTo()));
                } catch (Exception e) {
                    stmt.setString(16, complaint.getAssignedTo());
                }
            } else {
                stmt.setNull(16, java.sql.Types.OTHER);
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating complaint", e);
            return false;
        }
    }

    @Override
    public Optional<Complaint> findById(String id) {
        String sql = BASE_SELECT + "WHERE CAST(c.id AS VARCHAR) = ? OR c.complaint_code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToComplaint(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding complaint by id {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Complaint> findByCode(String complaintCode) {
        String sql = BASE_SELECT + "WHERE LOWER(c.complaint_code) = LOWER(?) OR CAST(c.id AS VARCHAR) = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, complaintCode);
            stmt.setString(2, complaintCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToComplaint(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding complaint by code {}", complaintCode, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Complaint> findByStudentId(String studentId) {
        List<Complaint> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE (CAST(c.student_id AS VARCHAR) = ? OR c.student_id IN (SELECT id FROM users WHERE reg_no = ?) OR CAST(c.student_id AS VARCHAR) IN (SELECT reg_no FROM users WHERE CAST(id AS VARCHAR) = ?)) ORDER BY c.created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            stmt.setString(2, studentId);
            stmt.setString(3, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToComplaint(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding complaints for student {}", studentId, e);
        }
        return list;
    }

    @Override
    public List<Complaint> findByDepartmentId(int departmentId) {
        List<Complaint> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE (u.department_id = ? OR c.category_id = ?) ORDER BY c.created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, departmentId);
            stmt.setInt(2, departmentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToComplaint(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding complaints for department {}", departmentId, e);
        }
        return list;
    }

    @Override
    public List<Complaint> findByAssignedTo(String userId) {
        List<Complaint> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE (CAST(c.assigned_to AS VARCHAR) = ? OR c.assigned_to IN (SELECT id FROM users WHERE reg_no = ?) OR CAST(c.assigned_to AS VARCHAR) IN (SELECT reg_no FROM users WHERE CAST(id AS VARCHAR) = ?)) ORDER BY c.created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, userId);
            stmt.setString(3, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToComplaint(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding assigned complaints for user {}", userId, e);
        }
        return list;
    }

    @Override
    public List<Complaint> findByStatus(String status) {
        List<Complaint> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE LOWER(c.status) = LOWER(?) ORDER BY c.created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToComplaint(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding complaints by status {}", status, e);
        }
        return list;
    }

    @Override
    public List<Complaint> findByPriority(String priority) {
        List<Complaint> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE UPPER(c.priority) = UPPER(?) ORDER BY c.created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, priority);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToComplaint(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding complaints by priority {}", priority, e);
        }
        return list;
    }

    @Override
    public List<Complaint> findAll() {
        List<Complaint> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY c.created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToComplaint(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all complaints", e);
        }
        return list;
    }

    @Override
    public List<Complaint> searchComplaints(String keyword, String priority, String status, Integer categoryId) {
        List<Complaint> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(c.title) LIKE ? OR LOWER(c.description) LIKE ? OR LOWER(c.complaint_code) LIKE ?) ");
            String match = "%" + keyword.trim().toLowerCase() + "%";
            params.add(match);
            params.add(match);
            params.add(match);
        }
        if (priority != null && !priority.isBlank() && !priority.equalsIgnoreCase("ALL")) {
            sql.append("AND UPPER(c.priority) = ? ");
            params.add(priority.toUpperCase());
        }
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND UPPER(c.status) = ? ");
            params.add(status.toUpperCase());
        }
        if (categoryId != null && categoryId > 0) {
            sql.append("AND c.category_id = ? ");
            params.add(categoryId);
        }
        sql.append("ORDER BY c.created_at DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToComplaint(rs));
            }
        } catch (SQLException e) {
            logger.error("Error searching complaints", e);
        }
        return list;
    }

    @Override
    public boolean updateStatus(String complaintId, String newStatus, String remark, String userId) {
        String sql = "UPDATE complaints SET status = ?, updated_at = CURRENT_TIMESTAMP " +
                     (newStatus.equalsIgnoreCase("RESOLVED") ? ", resolved_at = CURRENT_TIMESTAMP " : "") +
                     "WHERE id = ? OR complaint_code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setString(2, complaintId);
            stmt.setString(3, complaintId);
            boolean updated = stmt.executeUpdate() > 0;
            if (updated && remark != null && !remark.isBlank()) {
                try {
                    CommentDAO commentDAO = new CommentDAOImpl();
                    commentDAO.createComment(new Comment(complaintId, userId != null ? userId : "System", remark, false));
                } catch (Exception ignored) {}
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Error updating status for complaint {}", complaintId, e);
            return false;
        }
    }

    @Override
    public boolean updatePriority(String complaintId, String newPriority, String reason) {
        String sql = "UPDATE complaints SET priority = ?, ai_reason = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? OR complaint_code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPriority);
            stmt.setString(2, reason);
            stmt.setString(3, complaintId);
            stmt.setString(4, complaintId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating priority for complaint {}", complaintId, e);
            return false;
        }
    }

    @Override
    public boolean assignComplaint(String complaintId, String assignedToUserId, String assignedBy, String remark) {
        String sql = "UPDATE complaints SET assigned_to = ?, status = 'ASSIGNED', updated_at = CURRENT_TIMESTAMP WHERE id = ? OR complaint_code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, assignedToUserId);
            stmt.setString(2, complaintId);
            stmt.setString(3, complaintId);
            boolean updated = stmt.executeUpdate() > 0;
            if (updated && remark != null && !remark.isBlank()) {
                try {
                    CommentDAO commentDAO = new CommentDAOImpl();
                    commentDAO.createComment(new Comment(complaintId, assignedBy != null ? assignedBy : "HOD", remark, false));
                } catch (Exception ignored) {}
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Error assigning complaint {}", complaintId, e);
            return false;
        }
    }

    @Override
    public long countTotal() {
        return countQuery("SELECT COUNT(*) FROM complaints");
    }

    @Override
    public long countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM complaints WHERE UPPER(status) = UPPER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            logger.error("Error counting status {}", status, e);
        }
        return 0;
    }

    @Override
    public long countByPriority(String priority) {
        String sql = "SELECT COUNT(*) FROM complaints WHERE UPPER(priority) = UPPER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, priority);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            logger.error("Error counting priority {}", priority, e);
        }
        return 0;
    }

    private long countQuery(String sql) {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            logger.error("Error executing count query", e);
        }
        return 0;
    }

    private Complaint mapResultSetToComplaint(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setId(rs.getString("id"));
        c.setComplaintCode(rs.getString("complaint_code"));
        c.setStudentId(rs.getString("student_id"));
        c.setStudentName(rs.getString("student_name"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setOriginalText(rs.getString("original_text"));
        c.setOriginalLanguage(rs.getString("original_language"));
        c.setTranslatedText(rs.getString("translated_text"));
        c.setCategoryId(rs.getInt("category_id"));
        c.setCategoryName(rs.getString("category_name"));
        c.setPriority(rs.getString("priority"));
        c.setPriorityConfidence(rs.getDouble("priority_confidence"));
        c.setAiReason(rs.getString("ai_reason"));
        c.setStatus(rs.getString("status"));
        c.setAnonymous(rs.getBoolean("is_anonymous"));
        c.setGroupComplaint(rs.getBoolean("is_group_complaint"));
        c.setAssignedTo(rs.getString("assigned_to"));
        c.setAssignedToName(rs.getString("assigned_to_name"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setUpdatedAt(rs.getTimestamp("updated_at"));
        c.setResolvedAt(rs.getTimestamp("resolved_at"));
        return c;
    }
}
