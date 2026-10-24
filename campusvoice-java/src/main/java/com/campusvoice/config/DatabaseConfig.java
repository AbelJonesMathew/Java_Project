package com.campusvoice.config;

import com.campusvoice.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    private static String dbUrl = System.getenv("SUPABASE_DB_URL");
    private static String dbUser = System.getenv("SUPABASE_DB_USER");
    private static String dbPassword = System.getenv("SUPABASE_DB_PASSWORD");

    private static boolean isH2Fallback = false;
    private static boolean schemaInitialized = false;

    static {
        if (Boolean.getBoolean("campusvoice.offline") || Boolean.getBoolean("campusvoice.local")) {
            activateH2Fallback("Offline mode explicitly requested.");
        } else {
            loadDotEnv();
            parseAndCleanUrl();
        }
    }

    private static void loadDotEnv() {
        if (dbUrl != null && !dbUrl.isBlank()) return;

        File[] files = new File[]{
            new File(System.getProperty("user.home"), ".campusvoice/.env"),
            new File(".env"),
            new File("../.env"),
            new File("../../.env")
        };

        for (File f : files) {
            if (f.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) continue;
                        int idx = line.indexOf('=');
                        if (idx > 0) {
                            String key = line.substring(0, idx).trim();
                            String val = line.substring(idx + 1).trim();
                            if ("SUPABASE_DB_URL".equalsIgnoreCase(key) && (dbUrl == null || dbUrl.isBlank())) {
                                dbUrl = val;
                            } else if ("SUPABASE_DB_USER".equalsIgnoreCase(key) && (dbUser == null || dbUser.isBlank())) {
                                dbUser = val;
                            } else if ("SUPABASE_DB_PASSWORD".equalsIgnoreCase(key) && (dbPassword == null || dbPassword.isBlank())) {
                                dbPassword = val;
                            }
                        }
                    }
                    if (dbUrl != null && !dbUrl.isBlank()) {
                        logger.info("Loaded SUPABASE_DB_URL from .env file successfully.");
                        break;
                    }
                } catch (Exception e) {
                    logger.warn("Could not read .env file: {}", e.getMessage());
                }
            }
        }
    }

    private static void parseAndCleanUrl() {
        if (dbUrl == null || dbUrl.isBlank()) {
            activateH2Fallback("SUPABASE_DB_URL is not set.");
            return;
        }

        String working = dbUrl.trim();
        if (working.startsWith("postgres://")) {
            working = "jdbc:postgresql://" + working.substring("postgres://".length());
        } else if (working.startsWith("postgresql://")) {
            working = "jdbc:postgresql://" + working.substring("postgresql://".length());
        } else if (!working.startsWith("jdbc:")) {
            working = "jdbc:postgresql://" + working;
        }

        // Extract user:pass@ if embedded inside host portion of URL
        int atIndex = working.indexOf('@');
        if (atIndex > 0) {
            int schemeEnd = working.indexOf("://");
            if (schemeEnd > 0 && atIndex > schemeEnd + 3) {
                String userPassPart = working.substring(schemeEnd + 3, atIndex);
                String rest = working.substring(atIndex + 1);

                int colonIdx = userPassPart.indexOf(':');
                if (colonIdx > 0) {
                    if (dbUser == null || dbUser.isBlank()) {
                        dbUser = userPassPart.substring(0, colonIdx);
                    }
                    if (dbPassword == null || dbPassword.isBlank()) {
                        dbPassword = userPassPart.substring(colonIdx + 1);
                    }
                } else if (dbUser == null || dbUser.isBlank()) {
                    dbUser = userPassPart;
                }
                working = working.substring(0, schemeEnd + 3) + rest;
            }
        }

        if (working.contains("postgresql")) {
            if (!working.contains("sslmode=")) {
                working += (working.contains("?") ? "&" : "?") + "sslmode=require";
            }
            if (!working.contains("stringtype=")) {
                working += (working.contains("?") ? "&" : "?") + "stringtype=unspecified";
            }
        }

        dbUrl = working;
        logger.info("Configured Supabase / PostgreSQL Database URL: {}", dbUrl);
    }

    private static void activateH2Fallback(String reason) {
        if (Boolean.getBoolean("campusvoice.offline")) {
            dbUrl = "jdbc:h2:mem:campusvoice;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;NON_KEYWORDS=YEAR";
        } else {
            File directory = new File(System.getProperty("campusvoice.dataDir",
                    new File(System.getProperty("user.home"), ".campusvoice/data").getPath()));
            if (!directory.isDirectory() && !directory.mkdirs()) {
                throw new IllegalStateException("Cannot create CampusVoice data directory: " + directory);
            }
            dbUrl = "jdbc:h2:file:" + new File(directory, "campusvoice").getAbsolutePath().replace('\\', '/')
                    + ";AUTO_SERVER=TRUE;MODE=PostgreSQL;NON_KEYWORDS=YEAR";
        }
        dbUser = "sa";
        dbPassword = "";
        isH2Fallback = true;
        schemaInitialized = false;
        logger.info("Using {} H2 database: {}", Boolean.getBoolean("campusvoice.offline") ? "test" : "persistent local", reason);
    }

    public static synchronized Connection getConnection() throws SQLException {
        try {
            if (!isH2Fallback) {
                try {
                    Class.forName("org.postgresql.Driver");
                    DriverManager.setLoginTimeout(12);
                    Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                    if (!schemaInitialized) {
                        ensurePostgreSQLSeed(conn);
                        schemaInitialized = true;
                    }
                    return conn;
                } catch (Exception pgEx) {
                    throw new SQLException("Cannot connect to the configured shared database. Check connectivity and database settings; local mode has not been activated.", pgEx);
                }
            }

            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            if (!schemaInitialized) {
                initH2Schema(conn);
                schemaInitialized = true;
            }
            return conn;
        } catch (ClassNotFoundException e) {
            logger.error("JDBC Driver class not found", e);
            throw new SQLException("Database Driver Missing", e);
        }
    }

    private static synchronized void initH2Schema(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS roles (id INT PRIMARY KEY, role_name VARCHAR(50), description TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS departments (id INT PRIMARY KEY, dept_code VARCHAR(20), dept_name VARCHAR(100), head_of_dept_name VARCHAR(100))");
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id VARCHAR(50) PRIMARY KEY, reg_no VARCHAR(50) UNIQUE, full_name VARCHAR(100), email VARCHAR(100) UNIQUE, phone VARCHAR(20), password_hash VARCHAR(255), role_id INT, department_id INT, \"year\" INT, semester INT, is_active BOOLEAN DEFAULT TRUE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS complaint_categories (id INT PRIMARY KEY, category_name VARCHAR(100), description TEXT, icon_name VARCHAR(50))");
            stmt.execute("CREATE TABLE IF NOT EXISTS complaints (id VARCHAR(50) PRIMARY KEY, complaint_code VARCHAR(20), student_id VARCHAR(50), title VARCHAR(200), description TEXT, original_text TEXT, original_language VARCHAR(50), translated_text TEXT, category_id INT, priority VARCHAR(20), priority_confidence DECIMAL(5,2), ai_reason TEXT, status VARCHAR(30), is_anonymous BOOLEAN DEFAULT FALSE, is_group_complaint BOOLEAN DEFAULT FALSE, assigned_to VARCHAR(50), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, resolved_at TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS complaint_comments (id VARCHAR(50) PRIMARY KEY, complaint_id VARCHAR(50), user_id VARCHAR(50), comment_text TEXT, is_internal BOOLEAN DEFAULT FALSE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS complaint_groups (id VARCHAR(50) PRIMARY KEY, complaint_id VARCHAR(50), group_code VARCHAR(20), creator_student_id VARCHAR(50), title VARCHAR(200), target_supporters_count INT DEFAULT 10, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS complaint_group_members (id VARCHAR(50) PRIMARY KEY, group_id VARCHAR(50), student_id VARCHAR(50), joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS notifications (id VARCHAR(50) PRIMARY KEY, user_id VARCHAR(50), title VARCHAR(150), message TEXT, type VARCHAR(50), entity_id VARCHAR(50), is_read BOOLEAN DEFAULT FALSE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS ai_analysis (id VARCHAR(50) PRIMARY KEY, complaint_id VARCHAR(50), detected_language VARCHAR(50), original_priority VARCHAR(20), final_priority VARCHAR(20), confidence DECIMAL(5,2), reason TEXT, risk_factors TEXT, human_overridden BOOLEAN DEFAULT FALSE, model_version VARCHAR(30), analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS audit_logs (id VARCHAR(50) PRIMARY KEY, user_id VARCHAR(50), action VARCHAR(100), entity VARCHAR(100), entity_id VARCHAR(100), details TEXT, ip_address VARCHAR(50), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS escalations (id VARCHAR(50) PRIMARY KEY, complaint_id VARCHAR(50), escalated_from_role VARCHAR(50), escalated_to_role VARCHAR(50), reason TEXT, sla_hours_exceeded INT DEFAULT 0, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Seed a new local database once; never overwrite existing complaints on restart.
            try (java.sql.ResultSet existing = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (existing.next() && existing.getLong(1) > 0) return;
            }

            // ROLES
            stmt.execute("MERGE INTO roles KEY(id) VALUES (1, 'STUDENT', 'Enrolled student submitting complaints')");
            stmt.execute("MERGE INTO roles KEY(id) VALUES (2, 'HOD', 'Head of Department managing department issues')");
            stmt.execute("MERGE INTO roles KEY(id) VALUES (3, 'PRINCIPAL', 'Institutional Head handling escalations & oversight')");
            stmt.execute("MERGE INTO roles KEY(id) VALUES (4, 'MANAGEMENT', 'Administrator with executive control')");

            // DEPARTMENTS
            stmt.execute("MERGE INTO departments KEY(id) VALUES (1, 'CS', 'Computer Science & Engineering', 'Dr.Joby PP')");
            stmt.execute("MERGE INTO departments KEY(id) VALUES (2, 'EC', 'Electronics & Communication', 'Dr. Radhakrishnan')");
            stmt.execute("MERGE INTO departments KEY(id) VALUES (3, 'ME', 'Mechanical Engineering', 'Prof. Thomas')");
            stmt.execute("MERGE INTO departments KEY(id) VALUES (4, 'CE', 'Civil Engineering', 'Dr. Lakshmi')");
            stmt.execute("MERGE INTO departments KEY(id) VALUES (5, 'AD', 'General Administration', 'Mr. Suresh Kumar')");

            // CATEGORIES (All 17 Categories)
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (1, 'Infrastructure', 'Physical buildings & safety', 'building')");
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (2, 'Academic', 'Lectures & lab equipment', 'book')");
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (3, 'Faculty', 'Teacher availability & feedback', 'user-check')");
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (4, 'Examination', 'Timetable, hall tickets & grades', 'file-text')");
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (5, 'Hostel', 'Rooms, food quality & amenities', 'home')");
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (6, 'Transport', 'College buses & routes', 'truck')");
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (7, 'Food/Canteen', 'Hygiene, quality & pricing', 'coffee')");
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (8, 'Electricity', 'Power cuts & exposed wiring', 'zap')");
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (9, 'Water', 'Drinking water & purifiers', 'droplet')");
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (10, 'Internet/WiFi', 'Campus WiFi speed & connectivity', 'wifi')");
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (11, 'Security', 'Campus security, gates & lighting', 'shield')");
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (12, 'Harassment', 'Physical/verbal misconduct & threats', 'alert-triangle')");
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (13, 'Ragging', 'Zero-tolerance anti-ragging complaints', 'alert-octagon')");
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (14, 'Sanitation', 'Restroom cleanliness & hygiene', 'trash-2')");
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (15, 'Library', 'Books, study hall & journal access', 'bookmark')");
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (16, 'Fees/Finance', 'Fee structure & scholarship disbursements', 'dollar-sign')");
            stmt.execute("MERGE INTO complaint_categories KEY(id) VALUES (17, 'Other', 'General campus grievances', 'help-circle')");

            String demoHash = PasswordHasher.hashPasswordWithSalt("Password123!", "cvsalt123");

            // USERS (Institutional Administration, Faculty & Students)
            stmt.execute("MERGE INTO users KEY(reg_no) VALUES ('11111111-1111-1111-1111-111111111111', 'ADMIN001', 'Palai Diocese Management', 'admin@campusvoice.edu', '9876543210', '" + demoHash + "', 4, 5, NULL, NULL, TRUE, CURRENT_TIMESTAMP)");
            stmt.execute("MERGE INTO users KEY(reg_no) VALUES ('22222222-2222-2222-2222-222222222222', 'PRIN001', 'Dr. VP Deavsia (Principal)', 'principal@campusvoice.edu', '9876543211', '" + demoHash + "', 3, 5, NULL, NULL, TRUE, CURRENT_TIMESTAMP)");
            stmt.execute("MERGE INTO users KEY(reg_no) VALUES ('33333333-3333-3333-3333-333333333333', 'HODCS01', 'Dr.Joby PP (HOD CSE)', 'hod.cs@campusvoice.edu', '9876543212', '" + demoHash + "', 2, 1, NULL, NULL, TRUE, CURRENT_TIMESTAMP)");
            stmt.execute("UPDATE users SET full_name = 'Dr.Joby PP (HOD CSE)' WHERE reg_no = 'HODCS01'");
            stmt.execute("UPDATE users SET full_name = 'Dr. VP Deavsia (Principal)' WHERE reg_no = 'PRIN001'");
            stmt.execute("UPDATE users SET full_name = 'Palai Diocese Management' WHERE reg_no = 'ADMIN001'");
            stmt.execute("UPDATE departments SET head_of_dept_name = 'Dr.Joby PP' WHERE id = 1");
            stmt.execute("MERGE INTO users KEY(reg_no) VALUES ('44444444-4444-4444-4444-444444444444', 'HODEC01', 'Dr. Radhakrishnan (HOD ECE)', 'hod.ec@campusvoice.edu', '9876543213', '" + demoHash + "', 2, 2, NULL, NULL, TRUE, CURRENT_TIMESTAMP)");
            stmt.execute("MERGE INTO users KEY(reg_no) VALUES ('55555555-5555-5555-5555-555555555555', 'STU202501', 'John Doe', 'john.doe@student.campusvoice.edu', '9876543214', '" + demoHash + "', 1, 1, 3, 6, TRUE, CURRENT_TIMESTAMP)");
            stmt.execute("MERGE INTO users KEY(reg_no) VALUES ('66666666-6666-6666-6666-666666666666', 'STU202502', 'Ananya Nair', 'ananya.nair@student.campusvoice.edu', '9876543215', '" + demoHash + "', 1, 1, 4, 8, TRUE, CURRENT_TIMESTAMP)");
            stmt.execute("MERGE INTO users KEY(reg_no) VALUES ('77777777-7777-7777-7777-777777777777', 'STU202503', 'Rahul Sharma', 'rahul.sharma@student.campusvoice.edu', '9876543216', '" + demoHash + "', 1, 2, 2, 4, TRUE, CURRENT_TIMESTAMP)");
            stmt.execute("MERGE INTO users KEY(reg_no) VALUES ('88888888-8888-8888-8888-888888888888', 'STU202504', 'Kavya Ramesh', 'kavya.ramesh@student.campusvoice.edu', '9876543217', '" + demoHash + "', 1, 3, 1, 2, TRUE, CURRENT_TIMESTAMP)");
            stmt.execute("MERGE INTO users KEY(reg_no) VALUES ('99999999-9999-9999-9999-999999999999', 'STU202505', 'Mohammed Ali', 'ali.m@student.campusvoice.edu', '9876543218', '" + demoHash + "', 1, 4, 3, 5, TRUE, CURRENT_TIMESTAMP)");

            // MERGED COMPLAINTS (Complete Institutional Grievances)
            stmt.execute("MERGE INTO complaints KEY(complaint_code) VALUES ('a1111111-1111-1111-1111-111111111111', '#CP001', '55555555-5555-5555-5555-555555555555', 'Exposed Electrical Wires in Lab 3', 'The electrical wire in the lab is exposed and students could get an electric shock.', 'The electrical wire in the lab is exposed and students could get an electric shock.', 'English', 'The electrical wire in the lab is exposed and students could get an electric shock.', 8, 'HIGH', 96.00, 'Potential safety hazard detected. Exposed electrical wiring can cause electric shock.', 'IN PROGRESS', FALSE, FALSE, '33333333-3333-3333-3333-333333333333', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL)");
            stmt.execute("MERGE INTO complaints KEY(complaint_code) VALUES ('a2222222-2222-2222-2222-222222222222', '#CP002', '66666666-6666-6666-6666-666666666666', 'Classroom Projector Not Working', 'ക്ലാസ്റൂമിലെ പ്രൊജക്ടർ പ്രവർത്തിക്കുന്നില്ല.', 'ക്ലാസ്റൂമിലെ പ്രൊജക്ടർ പ്രവർത്തിക്കുന്നില്ല.', 'Malayalam', 'The projector in the classroom is not working.', 2, 'MEDIUM', 88.50, 'Academic equipment malfunction affecting classroom lectures.', 'ASSIGNED', FALSE, FALSE, '33333333-3333-3333-3333-333333333333', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL)");
            stmt.execute("MERGE INTO complaints KEY(complaint_code) VALUES ('a3333333-3333-3333-3333-333333333333', '#CP003', '77777777-7777-7777-7777-777777777777', 'Need More Chairs in Central Library', 'The reading section on 2nd floor lacks sufficient seating during peak hours.', 'The reading section on 2nd floor lacks sufficient seating during peak hours.', 'English', 'The reading section on 2nd floor lacks sufficient seating during peak hours.', 15, 'LOW', 94.20, 'Minor furniture request for library study space enhancement.', 'RESOLVED', FALSE, FALSE, '44444444-4444-4444-4444-444444444444', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
            stmt.execute("MERGE INTO complaints KEY(complaint_code) VALUES ('a4444444-4444-4444-4444-444444444444', '#CP004', '55555555-5555-5555-5555-555555555555', 'Poor Hostel WiFi Connectivity', 'WiFi routers in Mens Hostel Block B have been down for 3 days.', 'WiFi routers in Mens Hostel Block B have been down for 3 days.', 'English', 'WiFi routers in Mens Hostel Block B have been down for 3 days.', 10, 'MEDIUM', 91.00, 'Network connectivity outage affecting hostel residents.', 'SUBMITTED', TRUE, TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL)");
            stmt.execute("MERGE INTO complaints KEY(complaint_code) VALUES ('a5555555-5555-5555-5555-555555555555', '#CP005', '88888888-8888-8888-8888-888888888888', 'Broken Street Light near Girls Hostel Gate', 'गर्ल्स हॉस्टल गेट के पास की स्ट्रीट लाइट खराब है, जिससे रात में काफी अंधेरा रहता है और सुरक्षा का खतरा है।', 'गर्ल्स हॉस्टल गेट के पास की स्ट्रीट लाइट खराब है, जिससे रात में काफी अंधेरा रहता है और सुरक्षा का खतरा है।', 'Hindi', 'The street light near the Girls Hostel gate is broken, causing severe darkness at night and posing a security risk.', 11, 'HIGH', 95.80, 'Security vulnerability and night hazard identified near residential gate.', 'ESCALATED', FALSE, FALSE, '22222222-2222-2222-2222-222222222222', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL)");

            // AI ANALYSIS RECORDS
            stmt.execute("MERGE INTO ai_analysis KEY(complaint_id) VALUES ('a1111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111', 'English', 'HIGH', 'HIGH', 96.00, 'Potential safety hazard detected.', '• Electrical hazard\n• Student safety risk\n• Multiple people affected', FALSE, 'v1.0.0', CURRENT_TIMESTAMP)");
            stmt.execute("MERGE INTO ai_analysis KEY(complaint_id) VALUES ('a2222222-2222-2222-2222-222222222222', 'a2222222-2222-2222-2222-222222222222', 'Malayalam', 'MEDIUM', 'MEDIUM', 88.50, 'Academic disruption reported.', '• IT/Equipment failure\n• Classroom impact', FALSE, 'v1.0.0', CURRENT_TIMESTAMP)");
            stmt.execute("MERGE INTO ai_analysis KEY(complaint_id) VALUES ('a5555555-5555-5555-5555-555555555555', 'a5555555-5555-5555-5555-555555555555', 'Hindi', 'HIGH', 'HIGH', 95.80, 'Campus safety hazard near residential area.', '• Poor lighting\n• Night security threat', FALSE, 'v1.0.0', CURRENT_TIMESTAMP)");

            // COMPLAINT COMMENTS & ACTION NOTES
            stmt.execute("MERGE INTO complaint_comments KEY(id) VALUES ('c1111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', 'Electrician team dispatched to Lab 3 for emergency cable rewiring.', FALSE, CURRENT_TIMESTAMP)");
            stmt.execute("MERGE INTO complaint_comments KEY(id) VALUES ('c2222222-2222-2222-2222-222222222222', 'a2222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', 'AV technician assigned to inspect HDMI and projector lamp.', FALSE, CURRENT_TIMESTAMP)");
            stmt.execute("MERGE INTO complaint_comments KEY(id) VALUES ('c3333333-3333-3333-3333-333333333333', 'a3333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444444', '20 additional cushioned study chairs installed in reading hall.', FALSE, CURRENT_TIMESTAMP)");
            stmt.execute("MERGE INTO complaint_comments KEY(id) VALUES ('c5555555-5555-5555-5555-555555555555', 'a5555555-5555-5555-5555-555555555555', '22222222-2222-2222-2222-222222222222', 'Principal office issued urgent maintenance priority notice for gate lamp repair.', FALSE, CURRENT_TIMESTAMP)");

            // GROUP COMPLAINTS
            stmt.execute("MERGE INTO complaint_groups KEY(group_code) VALUES ('b1111111-1111-1111-1111-111111111111', 'a4444444-4444-4444-4444-444444444444', 'GRP-WIFI-01', '55555555-5555-5555-5555-555555555555', 'Poor Hostel WiFi Connectivity', 32, CURRENT_TIMESTAMP)");

            logger.info("Database schema & complete complaints dataset successfully initialized.");
        } catch (SQLException e) {
            logger.error("Error initializing in-memory schema", e);
        }
    }

    private static synchronized void ensurePostgreSQLSeed(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            try {
                stmt.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"");
            } catch (Exception ignored) {}

            stmt.execute("CREATE TABLE IF NOT EXISTS roles (id SERIAL PRIMARY KEY, role_name VARCHAR(50) UNIQUE NOT NULL, description TEXT, created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS departments (id SERIAL PRIMARY KEY, dept_code VARCHAR(20) UNIQUE NOT NULL, dept_name VARCHAR(100) NOT NULL, head_of_dept_name VARCHAR(100), created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), reg_no VARCHAR(50) UNIQUE NOT NULL, full_name VARCHAR(100) NOT NULL, email VARCHAR(100) UNIQUE NOT NULL, phone VARCHAR(20), password_hash VARCHAR(255) NOT NULL, role_id INT NOT NULL REFERENCES roles(id) ON DELETE RESTRICT, department_id INT REFERENCES departments(id) ON DELETE SET NULL, year INT, semester INT, is_active BOOLEAN DEFAULT TRUE, created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS complaint_categories (id SERIAL PRIMARY KEY, category_name VARCHAR(100) UNIQUE NOT NULL, description TEXT, icon_name VARCHAR(50) DEFAULT 'folder', created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS complaints (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), complaint_code VARCHAR(20) UNIQUE NOT NULL, student_id UUID REFERENCES users(id) ON DELETE SET NULL, title VARCHAR(200) NOT NULL, description TEXT NOT NULL, original_text TEXT, original_language VARCHAR(50) DEFAULT 'English', translated_text TEXT, category_id INT NOT NULL REFERENCES complaint_categories(id) ON DELETE RESTRICT, priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', priority_confidence DECIMAL(5,2) DEFAULT 0.00, ai_reason TEXT, status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED', is_anonymous BOOLEAN DEFAULT FALSE, is_group_complaint BOOLEAN DEFAULT FALSE, assigned_to UUID REFERENCES users(id) ON DELETE SET NULL, created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, resolved_at TIMESTAMP WITH TIME ZONE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS complaint_attachments (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), complaint_id UUID NOT NULL REFERENCES complaints(id) ON DELETE CASCADE, file_name VARCHAR(255) NOT NULL, file_type VARCHAR(100) NOT NULL, file_size BIGINT NOT NULL, storage_path TEXT NOT NULL, uploaded_by UUID REFERENCES users(id) ON DELETE SET NULL, created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS complaint_comments (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), complaint_id UUID NOT NULL REFERENCES complaints(id) ON DELETE CASCADE, user_id UUID REFERENCES users(id) ON DELETE SET NULL, comment_text TEXT NOT NULL, is_internal BOOLEAN DEFAULT FALSE, created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS complaint_groups (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), complaint_id UUID NOT NULL REFERENCES complaints(id) ON DELETE CASCADE, group_code VARCHAR(20) UNIQUE NOT NULL, creator_student_id UUID REFERENCES users(id) ON DELETE SET NULL, title VARCHAR(200) NOT NULL, target_supporters_count INT DEFAULT 10, created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS complaint_group_members (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), group_id UUID NOT NULL REFERENCES complaint_groups(id) ON DELETE CASCADE, student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE, joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, UNIQUE(group_id, student_id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS notifications (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE, title VARCHAR(150) NOT NULL, message TEXT NOT NULL, type VARCHAR(50) DEFAULT 'INFO', entity_id UUID, is_read BOOLEAN DEFAULT FALSE, created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS ai_analysis (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), complaint_id UUID NOT NULL REFERENCES complaints(id) ON DELETE CASCADE, detected_language VARCHAR(50), original_priority VARCHAR(20), final_priority VARCHAR(20), confidence DECIMAL(5,2), reason TEXT, risk_factors TEXT, human_overridden BOOLEAN DEFAULT FALSE, model_version VARCHAR(30) DEFAULT 'v1.0.0', analyzed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS escalations (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), complaint_id UUID NOT NULL REFERENCES complaints(id) ON DELETE CASCADE, escalated_from_role VARCHAR(50) NOT NULL, escalated_to_role VARCHAR(50) NOT NULL, reason TEXT NOT NULL, sla_hours_exceeded INT DEFAULT 0, created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE IF NOT EXISTS audit_logs (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), user_id UUID REFERENCES users(id) ON DELETE SET NULL, action VARCHAR(100) NOT NULL, entity VARCHAR(100), entity_id VARCHAR(100), details TEXT, ip_address VARCHAR(50) DEFAULT '127.0.0.1', created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP)");

            // ROLES
            stmt.execute("INSERT INTO roles (id, role_name, description) VALUES " +
                         "(1, 'STUDENT', 'Enrolled student submitting complaints'), " +
                         "(2, 'HOD', 'Head of Department managing department issues'), " +
                         "(3, 'PRINCIPAL', 'Institutional Head handling escalations & oversight'), " +
                         "(4, 'MANAGEMENT', 'Administrator with executive control') " +
                         "ON CONFLICT (id) DO NOTHING");

            // DEPARTMENTS
            stmt.execute("INSERT INTO departments (id, dept_code, dept_name, head_of_dept_name) VALUES " +
                         "(1, 'CS', 'Computer Science & Engineering', 'Dr.Joby PP'), " +
                         "(2, 'EC', 'Electronics & Communication', 'Dr. Radhakrishnan'), " +
                         "(3, 'ME', 'Mechanical Engineering', 'Prof. Thomas'), " +
                         "(4, 'CE', 'Civil Engineering', 'Dr. Lakshmi'), " +
                         "(5, 'AD', 'General Administration', 'Mr. Suresh Kumar') " +
                         "ON CONFLICT (id) DO NOTHING");

            // CATEGORIES (All 17 categories)
            stmt.execute("INSERT INTO complaint_categories (id, category_name, description, icon_name) VALUES " +
                         "(1, 'Infrastructure', 'Physical buildings & safety', 'building'), " +
                         "(2, 'Academic', 'Lectures & lab equipment', 'book'), " +
                         "(3, 'Faculty', 'Teacher availability & feedback', 'user-check'), " +
                         "(4, 'Examination', 'Timetable, hall tickets & grades', 'file-text'), " +
                         "(5, 'Hostel', 'Rooms, food quality & amenities', 'home'), " +
                         "(6, 'Transport', 'College buses & routes', 'truck'), " +
                         "(7, 'Food/Canteen', 'Hygiene, quality & pricing', 'coffee'), " +
                         "(8, 'Electricity', 'Power cuts & exposed wiring', 'zap'), " +
                         "(9, 'Water', 'Drinking water & purifiers', 'droplet'), " +
                         "(10, 'Internet/WiFi', 'Campus WiFi speed & connectivity', 'wifi'), " +
                         "(11, 'Security', 'Campus security, gates & lighting', 'shield'), " +
                         "(12, 'Harassment', 'Physical/verbal misconduct & threats', 'alert-triangle'), " +
                         "(13, 'Ragging', 'Zero-tolerance anti-ragging complaints', 'alert-octagon'), " +
                         "(14, 'Sanitation', 'Restroom cleanliness & hygiene', 'trash-2'), " +
                         "(15, 'Library', 'Books, study hall & journal access', 'bookmark'), " +
                         "(16, 'Fees/Finance', 'Fee structure & scholarship disbursements', 'dollar-sign'), " +
                         "(17, 'Other', 'General campus grievances', 'help-circle') " +
                         "ON CONFLICT (id) DO NOTHING");

            String demoHash = PasswordHasher.hashPasswordWithSalt("Password123!", "cvsalt123");

            // USERS
            stmt.execute("INSERT INTO users (id, reg_no, full_name, email, phone, password_hash, role_id, department_id, is_active) VALUES ('11111111-1111-1111-1111-111111111111'::uuid, 'ADMIN001', 'Palai Diocese Management', 'admin@campusvoice.edu', '9876543210', '" + demoHash + "', 4, 5, TRUE) ON CONFLICT (reg_no) DO UPDATE SET full_name = EXCLUDED.full_name");
            stmt.execute("INSERT INTO users (id, reg_no, full_name, email, phone, password_hash, role_id, department_id, is_active) VALUES ('22222222-2222-2222-2222-222222222222'::uuid, 'PRIN001', 'Dr. VP Deavsia (Principal)', 'principal@campusvoice.edu', '9876543211', '" + demoHash + "', 3, 5, TRUE) ON CONFLICT (reg_no) DO UPDATE SET full_name = EXCLUDED.full_name");
            stmt.execute("INSERT INTO users (id, reg_no, full_name, email, phone, password_hash, role_id, department_id, is_active) VALUES ('33333333-3333-3333-3333-333333333333'::uuid, 'HODCS01', 'Dr.Joby PP (HOD CSE)', 'hod.cs@campusvoice.edu', '9876543212', '" + demoHash + "', 2, 1, TRUE) ON CONFLICT (reg_no) DO UPDATE SET full_name = EXCLUDED.full_name");
            try {
                stmt.execute("UPDATE users SET full_name = 'Dr.Joby PP (HOD CSE)' WHERE reg_no = 'HODCS01'");
                stmt.execute("UPDATE users SET full_name = 'Dr. VP Deavsia (Principal)' WHERE reg_no = 'PRIN001'");
                stmt.execute("UPDATE users SET full_name = 'Palai Diocese Management' WHERE reg_no = 'ADMIN001'");
                stmt.execute("UPDATE departments SET head_of_dept_name = 'Dr.Joby PP' WHERE id = 1");
            } catch (Exception ignored) {}
            stmt.execute("INSERT INTO users (id, reg_no, full_name, email, phone, password_hash, role_id, department_id, is_active) VALUES ('44444444-4444-4444-4444-444444444444'::uuid, 'HODEC01', 'Dr. Radhakrishnan (HOD ECE)', 'hod.ec@campusvoice.edu', '9876543213', '" + demoHash + "', 2, 2, TRUE) ON CONFLICT (reg_no) DO NOTHING");
            stmt.execute("INSERT INTO users (id, reg_no, full_name, email, phone, password_hash, role_id, department_id, year, semester, is_active) VALUES ('55555555-5555-5555-5555-555555555555'::uuid, 'STU202501', 'John Doe', 'john.doe@student.campusvoice.edu', '9876543214', '" + demoHash + "', 1, 1, 3, 6, TRUE) ON CONFLICT (reg_no) DO NOTHING");
            stmt.execute("INSERT INTO users (id, reg_no, full_name, email, phone, password_hash, role_id, department_id, year, semester, is_active) VALUES ('66666666-6666-6666-6666-666666666666'::uuid, 'STU202502', 'Ananya Nair', 'ananya.nair@student.campusvoice.edu', '9876543215', '" + demoHash + "', 1, 1, 4, 8, TRUE) ON CONFLICT (reg_no) DO NOTHING");
            stmt.execute("INSERT INTO users (id, reg_no, full_name, email, phone, password_hash, role_id, department_id, year, semester, is_active) VALUES ('77777777-7777-7777-7777-777777777777'::uuid, 'STU202503', 'Rahul Sharma', 'rahul.sharma@student.campusvoice.edu', '9876543216', '" + demoHash + "', 1, 2, 2, 4, TRUE) ON CONFLICT (reg_no) DO NOTHING");
            stmt.execute("INSERT INTO users (id, reg_no, full_name, email, phone, password_hash, role_id, department_id, year, semester, is_active) VALUES ('88888888-8888-8888-8888-888888888888'::uuid, 'STU202504', 'Kavya Ramesh', 'kavya.ramesh@student.campusvoice.edu', '9876543217', '" + demoHash + "', 1, 3, 1, 2, TRUE) ON CONFLICT (reg_no) DO NOTHING");
            stmt.execute("INSERT INTO users (id, reg_no, full_name, email, phone, password_hash, role_id, department_id, year, semester, is_active) VALUES ('99999999-9999-9999-9999-999999999999'::uuid, 'STU202505', 'Mohammed Ali', 'ali.m@student.campusvoice.edu', '9876543218', '" + demoHash + "', 1, 4, 3, 5, TRUE) ON CONFLICT (reg_no) DO NOTHING");

            // MERGED COMPLAINTS
            stmt.execute("INSERT INTO complaints (id, complaint_code, student_id, title, description, original_text, original_language, translated_text, category_id, priority, priority_confidence, ai_reason, status, is_anonymous, is_group_complaint, assigned_to) VALUES " +
                         "('a1111111-1111-1111-1111-111111111111'::uuid, '#CP001', '55555555-5555-5555-5555-555555555555'::uuid, 'Exposed Electrical Wires in Lab 3', 'The electrical wire in the lab is exposed and students could get an electric shock.', 'The electrical wire in the lab is exposed and students could get an electric shock.', 'English', 'The electrical wire in the lab is exposed and students could get an electric shock.', 8, 'HIGH', 96.00, 'Potential safety hazard detected. Exposed electrical wiring can cause electric shock.', 'IN PROGRESS', FALSE, FALSE, '33333333-3333-3333-3333-333333333333'::uuid) ON CONFLICT (complaint_code) DO NOTHING");
            stmt.execute("INSERT INTO complaints (id, complaint_code, student_id, title, description, original_text, original_language, translated_text, category_id, priority, priority_confidence, ai_reason, status, is_anonymous, is_group_complaint, assigned_to) VALUES " +
                         "('a2222222-2222-2222-2222-222222222222'::uuid, '#CP002', '66666666-6666-6666-6666-666666666666'::uuid, 'Classroom Projector Not Working', 'ക്ലാസ്റൂമിലെ പ്രൊജക്ടർ പ്രവർത്തിക്കുന്നില്ല.', 'ക്ലാസ്റൂമിലെ പ്രൊജക്ടർ പ്രവർത്തിക്കുന്നില്ല.', 'Malayalam', 'The projector in the classroom is not working.', 2, 'MEDIUM', 88.50, 'Academic equipment malfunction affecting classroom lectures.', 'ASSIGNED', FALSE, FALSE, '33333333-3333-3333-3333-333333333333'::uuid) ON CONFLICT (complaint_code) DO NOTHING");
            stmt.execute("INSERT INTO complaints (id, complaint_code, student_id, title, description, original_text, original_language, translated_text, category_id, priority, priority_confidence, ai_reason, status, is_anonymous, is_group_complaint, assigned_to, resolved_at) VALUES " +
                         "('a3333333-3333-3333-3333-333333333333'::uuid, '#CP003', '77777777-7777-7777-7777-777777777777'::uuid, 'Need More Chairs in Central Library', 'The reading section on 2nd floor lacks sufficient seating during peak hours.', 'The reading section on 2nd floor lacks sufficient seating during peak hours.', 'English', 'The reading section on 2nd floor lacks sufficient seating during peak hours.', 15, 'LOW', 94.20, 'Minor furniture request for library study space enhancement.', 'RESOLVED', FALSE, FALSE, '44444444-4444-4444-4444-444444444444'::uuid, CURRENT_TIMESTAMP) ON CONFLICT (complaint_code) DO NOTHING");
            stmt.execute("INSERT INTO complaints (id, complaint_code, student_id, title, description, original_text, original_language, translated_text, category_id, priority, priority_confidence, ai_reason, status, is_anonymous, is_group_complaint, assigned_to) VALUES " +
                         "('a4444444-4444-4444-4444-444444444444'::uuid, '#CP004', '55555555-5555-5555-5555-555555555555'::uuid, 'Poor Hostel WiFi Connectivity', 'WiFi routers in Mens Hostel Block B have been down for 3 days.', 'WiFi routers in Mens Hostel Block B have been down for 3 days.', 'English', 'WiFi routers in Mens Hostel Block B have been down for 3 days.', 10, 'MEDIUM', 91.00, 'Network connectivity outage affecting hostel residents.', 'SUBMITTED', TRUE, TRUE, NULL) ON CONFLICT (complaint_code) DO NOTHING");
            stmt.execute("INSERT INTO complaints (id, complaint_code, student_id, title, description, original_text, original_language, translated_text, category_id, priority, priority_confidence, ai_reason, status, is_anonymous, is_group_complaint, assigned_to) VALUES " +
                         "('a5555555-5555-5555-5555-555555555555'::uuid, '#CP005', '88888888-8888-8888-8888-888888888888'::uuid, 'Broken Street Light near Girls Hostel Gate', 'गर्ल्स हॉस्टल गेट के पास की स्ट्रीट लाइट खराब है, जिससे रात में काफी अंधेरा रहता है और सुरक्षा का खतरा है।', 'गर्ल्स हॉस्टल गेट के पास की स्ट्रीट लाइट खराब है, जिससे रात में काफी अंधेरा रहता है और सुरक्षा का खतरा है।', 'Hindi', 'The street light near the Girls Hostel gate is broken, causing severe darkness at night and posing a security risk.', 11, 'HIGH', 95.80, 'Security vulnerability and night hazard identified near residential gate.', 'ESCALATED', FALSE, FALSE, '22222222-2222-2222-2222-222222222222'::uuid) ON CONFLICT (complaint_code) DO NOTHING");

            // AI ANALYSIS RECORDS
            stmt.execute("INSERT INTO ai_analysis (complaint_id, detected_language, original_priority, final_priority, confidence, reason, risk_factors) VALUES " +
                         "('a1111111-1111-1111-1111-111111111111'::uuid, 'English', 'HIGH', 'HIGH', 96.00, 'Potential safety hazard detected.', '• Electrical hazard\\n• Student safety risk\\n• Multiple people affected') ON CONFLICT DO NOTHING");
            stmt.execute("INSERT INTO ai_analysis (complaint_id, detected_language, original_priority, final_priority, confidence, reason, risk_factors) VALUES " +
                         "('a2222222-2222-2222-2222-222222222222'::uuid, 'Malayalam', 'MEDIUM', 'MEDIUM', 88.50, 'Academic disruption reported.', '• IT/Equipment failure\\n• Classroom impact') ON CONFLICT DO NOTHING");
            stmt.execute("INSERT INTO ai_analysis (complaint_id, detected_language, original_priority, final_priority, confidence, reason, risk_factors) VALUES " +
                         "('a5555555-5555-5555-5555-555555555555'::uuid, 'Hindi', 'HIGH', 'HIGH', 95.80, 'Campus safety hazard near residential area.', '• Poor lighting\\n• Night security threat') ON CONFLICT DO NOTHING");

            // COMMENTS
            stmt.execute("INSERT INTO complaint_comments (complaint_id, user_id, comment_text) VALUES " +
                         "('a1111111-1111-1111-1111-111111111111'::uuid, '33333333-3333-3333-3333-333333333333'::uuid, 'Electrician team dispatched to Lab 3 for emergency cable rewiring.') ON CONFLICT DO NOTHING");
            stmt.execute("INSERT INTO complaint_comments (complaint_id, user_id, comment_text) VALUES " +
                         "('a2222222-2222-2222-2222-222222222222'::uuid, '33333333-3333-3333-3333-333333333333'::uuid, 'AV technician assigned to inspect HDMI and projector lamp.') ON CONFLICT DO NOTHING");
            stmt.execute("INSERT INTO complaint_comments (complaint_id, user_id, comment_text) VALUES " +
                         "('a3333333-3333-3333-3333-333333333333'::uuid, '44444444-4444-4444-4444-444444444444'::uuid, '20 additional cushioned study chairs installed in reading hall.') ON CONFLICT DO NOTHING");
            stmt.execute("INSERT INTO complaint_comments (complaint_id, user_id, comment_text) VALUES " +
                         "('a5555555-5555-5555-5555-555555555555'::uuid, '22222222-2222-2222-2222-222222222222'::uuid, 'Principal office issued urgent maintenance priority notice for gate lamp repair.') ON CONFLICT DO NOTHING");

            // GROUPS
            stmt.execute("INSERT INTO complaint_groups (id, complaint_id, group_code, creator_student_id, title, target_supporters_count) VALUES " +
                         "('b1111111-1111-1111-1111-111111111111'::uuid, 'a4444444-4444-4444-4444-444444444444'::uuid, 'GRP-WIFI-01', '55555555-5555-5555-5555-555555555555'::uuid, 'Poor Hostel WiFi Connectivity', 32) ON CONFLICT (group_code) DO NOTHING");

            logger.info("PostgreSQL / Supabase schema & complete complaints dataset verified.");
        } catch (Exception e) {
            logger.warn("Auto-seed note: {}", e.getMessage());
        }
    }

    public static boolean isH2Fallback() {
        return isH2Fallback;
    }
}
