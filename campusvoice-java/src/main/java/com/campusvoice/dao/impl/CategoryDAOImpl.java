package com.campusvoice.dao.impl;

import com.campusvoice.config.DatabaseConfig;
import com.campusvoice.dao.CategoryDAO;
import com.campusvoice.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDAOImpl implements CategoryDAO {
    private static final Logger logger = LoggerFactory.getLogger(CategoryDAOImpl.class);

    @Override
    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM complaint_categories ORDER BY category_name ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Category(
                    rs.getInt("id"),
                    rs.getString("category_name"),
                    rs.getString("description"),
                    rs.getString("icon_name")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving categories", e);
        }
        if (list.isEmpty()) {
            list.add(new Category(1, "Infrastructure", "Physical buildings & safety", "building"));
            list.add(new Category(2, "Academic", "Lectures & lab equipment", "book"));
            list.add(new Category(8, "Electricity", "Power & wiring", "zap"));
            list.add(new Category(10, "Internet/WiFi", "Network speed & connectivity", "wifi"));
            list.add(new Category(15, "Library", "Books & seating", "bookmark"));
            list.add(new Category(3, "Hostel", "Hostel rooms & mess", "home"));
            list.add(new Category(4, "Transport", "College buses & parking", "bus"));
            list.add(new Category(20, "General", "General campus grievances", "folder"));
        }
        return list;
    }

    @Override
    public Optional<Category> findById(int id) {
        String sql = "SELECT * FROM complaint_categories WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new Category(
                    rs.getInt("id"),
                    rs.getString("category_name"),
                    rs.getString("description"),
                    rs.getString("icon_name")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error finding category by id {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Category> findByName(String name) {
        String sql = "SELECT * FROM complaint_categories WHERE LOWER(category_name) = LOWER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new Category(
                    rs.getInt("id"),
                    rs.getString("category_name"),
                    rs.getString("description"),
                    rs.getString("icon_name")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error finding category by name {}", name, e);
        }
        return Optional.empty();
    }
}
