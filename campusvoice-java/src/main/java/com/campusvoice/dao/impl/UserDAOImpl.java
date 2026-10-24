package com.campusvoice.dao.impl;

import com.campusvoice.config.DatabaseConfig;
import com.campusvoice.dao.UserDAO;
import com.campusvoice.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserDAOImpl implements UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    @Override
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (id, reg_no, full_name, email, phone, password_hash, role_id, department_id, year, semester, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (user.getId() == null || user.getId().isBlank()) {
                user.setId(UUID.randomUUID().toString());
            }
            if (DatabaseConfig.isH2Fallback()) {
                stmt.setString(1, user.getId());
            } else {
                try {
                    stmt.setObject(1, UUID.fromString(user.getId()));
                } catch (Exception e) {
                    stmt.setObject(1, UUID.randomUUID());
                }
            }

            stmt.setString(2, user.getRegNo());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getPasswordHash());
            stmt.setInt(7, user.getRoleId());
            if (user.getDepartmentId() != null) stmt.setInt(8, user.getDepartmentId());
            else stmt.setNull(8, Types.INTEGER);
            
            if (user.getYear() != null) stmt.setInt(9, user.getYear());
            else stmt.setNull(9, Types.INTEGER);

            if (user.getSemester() != null) stmt.setInt(10, user.getSemester());
            else stmt.setNull(10, Types.INTEGER);

            stmt.setBoolean(11, user.isActive());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating user {}", user.getRegNo(), e);
            return false;
        }
    }

    @Override
    public Optional<User> findByRegNo(String regNo) {
        String sql = "SELECT u.*, r.role_name, d.dept_name FROM users u JOIN roles r ON u.role_id = r.id LEFT JOIN departments d ON u.department_id = d.id WHERE LOWER(u.reg_no) = LOWER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, regNo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding user by regNo {}", regNo, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(String id) {
        String sql = "SELECT u.*, r.role_name, d.dept_name FROM users u JOIN roles r ON u.role_id = r.id LEFT JOIN departments d ON u.department_id = d.id WHERE u.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (DatabaseConfig.isH2Fallback()) {
                stmt.setString(1, id);
            } else {
                try {
                    stmt.setObject(1, UUID.fromString(id));
                } catch (Exception e) {
                    stmt.setString(1, id);
                }
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding user by id {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT u.*, r.role_name, d.dept_name FROM users u JOIN roles r ON u.role_id = r.id LEFT JOIN departments d ON u.department_id = d.id WHERE LOWER(u.email) = LOWER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding user by email {}", email, e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.*, r.role_name, d.dept_name FROM users u JOIN roles r ON u.role_id = r.id LEFT JOIN departments d ON u.department_id = d.id ORDER BY u.created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Error listing users", e);
        }
        return list;
    }

    @Override
    public List<User> findByRole(int roleId) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.*, r.role_name, d.dept_name FROM users u JOIN roles r ON u.role_id = r.id LEFT JOIN departments d ON u.department_id = d.id WHERE u.role_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Error listing users by role {}", roleId, e);
        }
        return list;
    }

    @Override
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, role_id = ?, department_id = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPhone());
            stmt.setInt(4, user.getRoleId());
            if (user.getDepartmentId() != null) stmt.setInt(5, user.getDepartmentId());
            else stmt.setNull(5, Types.INTEGER);
            stmt.setBoolean(6, user.isActive());
            if (DatabaseConfig.isH2Fallback()) {
                stmt.setString(7, user.getId());
            } else {
                stmt.setObject(7, UUID.fromString(user.getId()));
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating user {}", user.getId(), e);
            return false;
        }
    }

    @Override
    public boolean updatePassword(String userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            if (DatabaseConfig.isH2Fallback()) {
                stmt.setString(2, userId);
            } else {
                stmt.setObject(2, UUID.fromString(userId));
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating password for user {}", userId, e);
            return false;
        }
    }

    @Override
    public boolean deactivateUser(String userId) {
        String sql = "UPDATE users SET is_active = FALSE WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (DatabaseConfig.isH2Fallback()) {
                stmt.setString(1, userId);
            } else {
                stmt.setObject(1, UUID.fromString(userId));
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deactivating user {}", userId, e);
            return false;
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        Object idObj = rs.getObject("id");
        u.setId(idObj != null ? idObj.toString() : null);
        u.setRegNo(rs.getString("reg_no"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRoleId(rs.getInt("role_id"));
        u.setRoleName(rs.getString("role_name"));
        int deptId = rs.getInt("department_id");
        if (!rs.wasNull()) u.setDepartmentId(deptId);
        u.setDepartmentName(rs.getString("dept_name"));
        int yr = rs.getInt("year");
        if (!rs.wasNull()) u.setYear(yr);
        int sem = rs.getInt("semester");
        if (!rs.wasNull()) u.setSemester(sem);
        u.setActive(rs.getBoolean("is_active"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
