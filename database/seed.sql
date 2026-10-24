-- ========================================================
-- CAMPUSVOICE SEED DATA (PostgreSQL / Supabase)
-- "Speak • Report • Get It Solved"
-- ========================================================

-- Insert Roles
INSERT INTO roles (id, role_name, description) VALUES
(1, 'STUDENT', 'Enrolled student submitting complaints'),
(2, 'HOD', 'Head of Department managing department issues'),
(3, 'PRINCIPAL', 'Institutional head handling escalations & executive oversight'),
(4, 'MANAGEMENT', 'System Administrator with overall system & user control')
ON CONFLICT (role_name) DO NOTHING;

-- Insert Departments
INSERT INTO departments (id, dept_code, dept_name, head_of_dept_name) VALUES
(1, 'CS', 'Computer Science & Engineering', 'Prof. Sharma'),
(2, 'EC', 'Electronics & Communication', 'Dr. Radhakrishnan'),
(3, 'ME', 'Mechanical Engineering', 'Prof. Thomas'),
(4, 'CE', 'Civil Engineering', 'Dr. Lakshmi'),
(5, 'AD', 'General Administration', 'Mr. Suresh Kumar')
ON CONFLICT (dept_code) DO NOTHING;

-- Insert Complaint Categories
INSERT INTO complaint_categories (id, category_name, description, icon_name) VALUES
(1, 'Infrastructure', 'Physical buildings, classrooms, furniture, structural safety', 'building'),
(2, 'Academic', 'Lectures, curriculum, lab equipment, course scheduling', 'book'),
(3, 'Faculty', 'Teacher availability, evaluation disputes, misconduct', 'user-check'),
(4, 'Examination', 'Hall tickets, exam timetable, revaluation, grade sheets', 'file-text'),
(5, 'Hostel', 'Room allotment, mess food quality, hostel maintenance', 'home'),
(6, 'Transport', 'College buses, bus routes, driver behavior, timing', 'truck'),
(7, 'Food/Canteen', 'Food hygiene, canteen pricing, water supply in canteen', 'coffee'),
(8, 'Electricity', 'Power cuts, exposed wiring, broken switches, generator backup', 'zap'),
(9, 'Water', 'Drinking water purifier, toilet water leakage, supply shortage', 'droplet'),
(10, 'Internet/WiFi', 'Campus WiFi connectivity, speed, router issues, portal errors', 'wifi'),
(11, 'Security', 'Gate security, CCTV cameras, trespassers, ID verification', 'shield'),
(12, 'Harassment', 'Physical/verbal harassment, discrimination, safety threats', 'alert-triangle'),
(13, 'Ragging', 'Zero-tolerance anti-ragging complaints and emergency reporting', 'alert-octagon'),
(14, 'Sanitation', 'Restroom cleanliness, waste disposal, pest control', 'trash-2'),
(15, 'Library', 'Book availability, digital library access, quiet study area', 'bookmark'),
(16, 'Fees/Finance', 'Fee structure, scholarship disbursement, payment gateway', 'dollar-sign'),
(17, 'Other', 'General complaints not covered above', 'help-circle')
ON CONFLICT (category_name) DO NOTHING;

-- Insert Base Users (Passwords are salted SHA-256 for 'Password123!')
-- Password Hash format: salt:sha256(salt + rawPassword)
-- Example pre-computed hash for 'Password123!' using salt 'cvsalt123': cvsalt123:9d7cea4adcaaf9e38ebd9ffe2e073366103ed9ad721141bb0a74ad05c267a0f8

INSERT INTO users (id, reg_no, full_name, email, phone, password_hash, role_id, department_id, year, semester) VALUES
-- Admin / Management
('11111111-1111-1111-1111-111111111111', 'ADMIN001', 'Admin Management', 'admin@campusvoice.edu', '9876543210', 'cvsalt123:9d7cea4adcaaf9e38ebd9ffe2e073366103ed9ad721141bb0a74ad05c267a0f8', 4, 5, NULL, NULL),

-- Principal
('22222222-2222-2222-2222-222222222222', 'PRIN001', 'Dr. Meena (Principal)', 'principal@campusvoice.edu', '9876543211', 'cvsalt123:9d7cea4adcaaf9e38ebd9ffe2e073366103ed9ad721141bb0a74ad05c267a0f8', 3, 5, NULL, NULL),

-- HODs
('33333333-3333-3333-3333-333333333333', 'HODCS01', 'Prof. Sharma (HOD CSE)', 'hod.cs@campusvoice.edu', '9876543212', 'cvsalt123:9d7cea4adcaaf9e38ebd9ffe2e073366103ed9ad721141bb0a74ad05c267a0f8', 2, 1, NULL, NULL),
('44444444-4444-4444-4444-444444444444', 'HODEC01', 'Dr. Radhakrishnan (HOD ECE)', 'hod.ec@campusvoice.edu', '9876543213', 'cvsalt123:9d7cea4adcaaf9e38ebd9ffe2e073366103ed9ad721141bb0a74ad05c267a0f8', 2, 2, NULL, NULL),

-- Students
('55555555-5555-5555-5555-555555555555', 'STU202501', 'John Doe', 'john.doe@student.campusvoice.edu', '9876543214', 'cvsalt123:9d7cea4adcaaf9e38ebd9ffe2e073366103ed9ad721141bb0a74ad05c267a0f8', 1, 1, 3, 6),
('66666666-6666-6666-6666-666666666666', 'STU202502', 'Ananya Nair', 'ananya.nair@student.campusvoice.edu', '9876543215', 'cvsalt123:9d7cea4adcaaf9e38ebd9ffe2e073366103ed9ad721141bb0a74ad05c267a0f8', 1, 1, 4, 8),
('77777777-7777-7777-7777-777777777777', 'STU202503', 'Rahul Sharma', 'rahul.sharma@student.campusvoice.edu', '9876543216', 'cvsalt123:9d7cea4adcaaf9e38ebd9ffe2e073366103ed9ad721141bb0a74ad05c267a0f8', 1, 2, 2, 4),
('88888888-8888-8888-8888-888888888888', 'STU202504', 'Kavya Ramesh', 'kavya.ramesh@student.campusvoice.edu', '9876543217', 'cvsalt123:9d7cea4adcaaf9e38ebd9ffe2e073366103ed9ad721141bb0a74ad05c267a0f8', 1, 3, 1, 2),
('99999999-9999-9999-9999-999999999999', 'STU202505', 'Mohammed Ali', 'ali.m@student.campusvoice.edu', '9876543218', 'cvsalt123:9d7cea4adcaaf9e38ebd9ffe2e073366103ed9ad721141bb0a74ad05c267a0f8', 1, 4, 3, 5)
ON CONFLICT (reg_no) DO NOTHING;

-- Insert Sample Complaints
INSERT INTO complaints (id, complaint_code, student_id, title, description, original_text, original_language, translated_text, category_id, priority, priority_confidence, ai_reason, status, is_anonymous, is_group_complaint, assigned_to) VALUES
-- HIGH Priority 1 (Electrical Hazard - English)
('a1111111-1111-1111-1111-111111111111', '#CP001', '55555555-5555-5555-5555-555555555555',
 'Exposed Electrical Wires in Lab 3',
 'The electrical wire in the lab is exposed and students could get an electric shock.',
 'The electrical wire in the lab is exposed and students could get an electric shock.',
 'English',
 'The electrical wire in the lab is exposed and students could get an electric shock.',
 8, 'HIGH', 96.00,
 'Potential safety hazard detected. The complaint mentions exposed electrical wiring which can cause electric shock and affects multiple students.',
 'IN PROGRESS', FALSE, FALSE, '33333333-3333-3333-3333-333333333333'),

-- MEDIUM Priority 2 (Academic Projector - Malayalam)
('a2222222-2222-2222-2222-222222222222', '#CP002', '66666666-6666-6666-6666-666666666666',
 'Classroom Projector Not Working',
 'ക്ലാസ്റൂമിലെ പ്രൊജക്ടർ പ്രവർത്തിക്കുന്നില്ല.',
 'ക്ലാസ്റൂമിലെ പ്രൊജക്ടർ പ്രവർത്തിക്കുന്നില്ല.',
 'Malayalam',
 'The projector in the classroom is not working.',
 2, 'MEDIUM', 88.50,
 'Academic equipment malfunction affecting classroom lectures.',
 'ASSIGNED', FALSE, FALSE, '33333333-3333-3333-3333-333333333333'),

-- LOW Priority 3 (Library Chairs - English)
('a3333333-3333-3333-3333-333333333333', '#CP003', '77777777-7777-7777-7777-777777777777',
 'Need More Chairs in Central Library',
 'The reading section on 2nd floor lacks sufficient seating during peak hours.',
 'The reading section on 2nd floor lacks sufficient seating during peak hours.',
 'English',
 'The reading section on 2nd floor lacks sufficient seating during peak hours.',
 15, 'LOW', 94.20,
 'Minor furniture request for library study space enhancement.',
 'RESOLVED', FALSE, FALSE, '44444444-4444-4444-4444-444444444444'),

-- MEDIUM Priority 4 (Hostel WiFi - English - Group Complaint)
('a4444444-4444-4444-4444-444444444444', '#CP004', '55555555-5555-5555-5555-555555555555',
 'Poor Hostel WiFi Connectivity',
 'WiFi routers in Mens Hostel Block B have been down for 3 days.',
 'WiFi routers in Mens Hostel Block B have been down for 3 days.',
 'English',
 'WiFi routers in Mens Hostel Block B have been down for 3 days.',
 10, 'MEDIUM', 91.00,
 'Network connectivity outage affecting hostel residents.',
 'SUBMITTED', TRUE, TRUE, NULL),

-- HIGH Priority 5 (Security / Gate - Hindi)
('a5555555-5555-5555-5555-555555555555', '#CP005', '88888888-8888-8888-8888-888888888888',
 'Broken Street Light near Girls Hostel Gate',
 'गर्ल्स हॉस्टल गेट के पास की स्ट्रीट लाइट खराब है, जिससे रात में काफी अंधेरा रहता है और सुरक्षा का खतरा है।',
 'गर्ल्स हॉस्टल गेट के पास की स्ट्रीट लाइट खराब है, जिससे रात में काफी अंधेरा रहता है और सुरक्षा का खतरा है।',
 'Hindi',
 'The street light near the Girls Hostel gate is broken, causing severe darkness at night and posing a security risk.',
 11, 'HIGH', 95.80,
 'Security vulnerability and night hazard identified near hostel entry.',
 'IN PROGRESS', FALSE, FALSE, '22222222-2222-2222-2222-222222222222')
ON CONFLICT (complaint_code) DO NOTHING;

-- AI Analysis records for sample complaints
INSERT INTO ai_analysis (complaint_id, detected_language, original_priority, final_priority, confidence, reason, risk_factors) VALUES
('a1111111-1111-1111-1111-111111111111', 'English', 'HIGH', 'HIGH', 96.00, 'Potential safety hazard detected.', '• Electrical hazard\n• Student safety risk\n• Multiple people affected'),
('a2222222-2222-2222-2222-222222222222', 'Malayalam', 'MEDIUM', 'MEDIUM', 88.50, 'Academic disruption reported.', '• IT/Equipment failure\n• Classroom impact'),
('a5555555-5555-5555-5555-555555555555', 'Hindi', 'HIGH', 'HIGH', 95.80, 'Campus safety hazard near residential area.', '• Poor lighting\n• Night security threat')
ON CONFLICT DO NOTHING;

-- Insert Group Complaint details
INSERT INTO complaint_groups (id, complaint_id, group_code, creator_student_id, title, target_supporters_count) VALUES
('b1111111-1111-1111-1111-111111111111', 'a4444444-4444-4444-4444-444444444444', 'GRP-WIFI-01', '55555555-5555-5555-5555-555555555555', 'Poor Hostel WiFi Connectivity', 32)
ON CONFLICT (group_code) DO NOTHING;

INSERT INTO complaint_group_members (group_id, student_id) VALUES
('b1111111-1111-1111-1111-111111111111', '55555555-5555-5555-5555-555555555555'),
('b1111111-1111-1111-1111-111111111111', '66666666-6666-6666-6666-666666666666')
ON CONFLICT DO NOTHING;
