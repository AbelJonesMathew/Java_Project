# CampusVoice System Architecture

## Architecture Overview

**CampusVoice** is built using a modern decoupled micro-architecture combining a responsive **Java Swing desktop client** with a **Python FastAPI AI microservice** and a cloud **Supabase PostgreSQL / Storage** data tier.

```
+-------------------------------------------------------------------------+
|                        Java Swing Desktop Client                        |
|                                                                         |
|  +----------------+  +-------------------+  +------------------------+  |
|  | Student UI     |  | HOD UI            |  | Principal / Mgmt UI    |  |
|  +----------------+  +-------------------+  +------------------------+  |
|                                                                         |
|  +-------------------------------------------------------------------+  |
|  | Service Layer (Auth, Complaint, AI, Voice, Notification, SLA)     |  |
|  +-------------------------------------------------------------------+  |
|                                                                         |
|  +-------------------------------------------------------------------+  |
|  | DAO Layer (PreparedStatements, ConnectionManager)                 |  |
|  +-------------------------------------------------------------------+  |
+--------------------|---------------------------------|------------------+
                     | HTTP REST API                   | JDBC Connection
                     v                                 v
+-----------------------------------+   +---------------------------------+
|  Python FastAPI AI Service        |   | Supabase PostgreSQL Database    |
|                                   |   |                                 |
|  - NLP Priority Classification    |   | - Users & Roles                 |
|  - Rule-based Hazard Overrides    |   | - Complaints & AI History       |
|  - Multilingual Language Detect   |   | - Group Complaints & SLA Logs   |
|  - Real-time AI Translation       |   | - Notifications & Audit Logs    |
+-----------------------------------+   +---------------------------------+
                                                       ^
                                                       | File Uploads
                                        +---------------------------------+
                                        | Supabase Storage (Files/Images) |
                                        +---------------------------------+
```

---

## Technical Stack & Design Patterns

### 1. Java Swing Client (`campusvoice-java`)
- **Framework**: Java 17+, Swing with **FlatLaf Modern Light Look & Feel**.
- **Design Patterns**:
  - **MVC Pattern**: Model classes represent schema entities, View classes render FlatLaf Swing forms/panels, Controller/Service classes handle interactions.
  - **DAO Pattern**: Data Access Objects isolate direct JDBC SQL execution.
  - **Repository Pattern**: High-level data retrieval abstractions.
  - **Singleton Pattern**: Managed configuration objects, Connection pools, and Session state.
- **Security**:
  - Salting and Hashing via SHA-256 for passwords (`salt:sha256(salt + password)`).
  - Role-Based Access Control (RBAC) enforced at service and view levels.
  - Prepared statements everywhere to prevent SQL injection.

### 2. AI Microservice (`campusvoice-ai`)
- **Framework**: Python 3.11+, FastAPI, Uvicorn.
- **Core ML Engine**:
  - `TF-IDF Vectorizer` + `LogisticRegression` / `LinearSVM` for priority tag prediction (LOW, MEDIUM, HIGH).
  - Rule-Based Hazard Engine: Immediately elevates complaints containing critical keywords (e.g. `fire`, `electrical shock`, `gas leak`, `ragging`, `harassment`, `weapon`, `collapse`) to `HIGH` priority regardless of statistical score.
  - Confidence calculation: Reports confidence percentage (0-100%). Low confidence (< 75%) flags the complaint for Human Review.
- **Multilingual Pipeline**:
  - Language Detection: `langdetect` engine detecting English, Malayalam, Hindi, Tamil, Telugu, Kannada.
  - Translation: Automated translation into standard English prior to priority scoring.

### 3. Data Tier
- **Database**: PostgreSQL on Supabase.
- **File Storage**: Supabase Storage Buckets for complaint images, PDFs, and document attachments.
