package com.campusvoice;

import com.campusvoice.config.DatabaseConfig;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {
    @Test
    public void testDatabaseConnectivity() {
        assertDoesNotThrow(() -> {
            try (Connection conn = DatabaseConfig.getConnection()) {
                assertNotNull(conn, "Database connection should be established");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users")) {
                    assertTrue(rs.next());
                    int userCount = rs.getInt(1);
                    assertTrue(userCount >= 9, "Users should be initialized with all standard roles");
                }

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT count(*) FROM complaints")) {
                    assertTrue(rs.next());
                    int complaintCount = rs.getInt(1);
                    assertTrue(complaintCount >= 5, "Complaints should be merged with all 5 initial tickets");
                }
            }
        });
    }
}
