# CampusVoice Database Documentation

## Database Overview

**CampusVoice** uses PostgreSQL hosted on Supabase (or any standard PostgreSQL 13+ instance).

---

## Entity Relationship Diagram & Core Schema

```
[roles] 1 --- * [users] 1 --- * [complaints] 1 --- * [complaint_attachments]
                   |                 |        |
[departments] 1 ---+                 |        + --- * [complaint_comments]
                                     |        |
[complaint_categories] 1 ------------+        + --- * [complaint_status_history]
                                              |
                                              + --- 1 [ai_analysis]
                                              |
                                              + --- * [escalations]
                                              |
                                              + --- 1 [complaint_groups] 1 --- * [complaint_group_members]
```

---

## Key Tables

### 1. `users`
Stores student, faculty (HOD), Principal, and Management account profiles.
- `id` (UUID, Primary Key)
- `reg_no` (VARCHAR(50), Unique Registration Number / Username)
- `full_name` (VARCHAR(100))
- `email` (VARCHAR(100), Unique)
- `password_hash` (VARCHAR(255), Salted SHA-256 string)
- `role_id` (INT, Foreign Key to `roles.id`)
- `department_id` (INT, Foreign Key to `departments.id`)

### 2. `complaints`
Primary complaint ticket table.
- `id` (UUID, Primary Key)
- `complaint_code` (VARCHAR(20), Unique Code e.g. `#CP001`)
- `student_id` (UUID, Foreign Key to `users.id`)
- `title` (VARCHAR(200))
- `description` (TEXT)
- `original_text` (TEXT)
- `original_language` (VARCHAR(50))
- `translated_text` (TEXT)
- `category_id` (INT, Foreign Key to `complaint_categories.id`)
- `priority` (VARCHAR(20): `LOW`, `MEDIUM`, `HIGH`)
- `priority_confidence` (DECIMAL(5,2))
- `ai_reason` (TEXT)
- `status` (VARCHAR(30): `SUBMITTED`, `UNDER REVIEW`, `ASSIGNED`, `IN PROGRESS`, `ESCALATED`, `RESOLVED`, `REJECTED`, `CLOSED`)
- `is_anonymous` (BOOLEAN)
- `is_group_complaint` (BOOLEAN)
- `assigned_to` (UUID, Foreign Key to `users.id`)

### 3. `ai_analysis`
Detailed AI classification metadata & human review record.
- `id` (UUID, Primary Key)
- `complaint_id` (UUID, Foreign Key to `complaints.id`)
- `detected_language` (VARCHAR(50))
- `original_priority` (VARCHAR(20))
- `final_priority` (VARCHAR(20))
- `confidence` (DECIMAL(5,2))
- `reason` (TEXT)
- `risk_factors` (TEXT)
- `human_overridden` (BOOLEAN)
- `model_version` (VARCHAR(30))

### 4. `complaint_groups` & `complaint_group_members`
Enables multiple students to join and support a group complaint issue.

### 5. `escalations` & `audit_logs`
Tracks automatic SLA violations and detailed user audit trails.

---

## Migration & Setup
1. Run `database/schema.sql` to initialize tables, constraints, and indexes.
2. Run `database/seed.sql` to insert baseline roles, departments, complaint categories, default users, and sample complaints.
