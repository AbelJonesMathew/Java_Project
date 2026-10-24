# CampusVoice Installation & Setup Guide

## Prerequisites

- **Java JDK**: 17 or higher (`java -version`)
- **Apache Maven**: 3.8+ (`mvn -version`)
- **Python**: 3.11+ (`python --version`)
- **PostgreSQL Database**: Supabase account or local PostgreSQL instance.

---

## 1. Database Setup (Supabase / PostgreSQL)

1. Log into your Supabase Dashboard and create a new project.
2. Open the **SQL Editor**.
3. Run the SQL script located in `database/schema.sql` to create tables and indexes.
4. Run the SQL script located in `database/seed.sql` to insert sample data (Students, HODs, Principal, Admin, Categories, Complaints).
5. Copy your database connection credentials (Host, User, Password, Port).

---

## 2. Environment Configuration

Copy `.env.example` to `.env` in the root folder and populate your database and AI API settings:

```env
SUPABASE_DB_URL=jdbc:postgresql://db.xxx.supabase.co:5432/postgres
SUPABASE_DB_USER=postgres
SUPABASE_DB_PASSWORD=your_supabase_db_password
AI_SERVICE_URL=http://localhost:8000
```

---

## 3. Python AI Microservice Setup

```bash
cd campusvoice-ai

# Create virtual environment
python -m venv venv

# Activate environment (Windows)
venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Train baseline ML model
python training/train_model.py

# Start FastAPI service
uvicorn app:app --reload --host 0.0.0.0 --port 8000
```

Verify service is running by navigating to `http://localhost:8000/ai/health`.

---

## 4. Java Swing Client Setup & Execution

```bash
cd campusvoice-java

# Build Maven project
mvn clean install

# Run application
mvn exec:java -Dexec.mainClass="com.campusvoice.Main"
```

---

## 5. Sample Login Credentials

| Role | Username / Reg No | Password |
|---|---|---|
| Student | `STU202501` | `Password123!` |
| Student | `STU202502` | `Password123!` |
| HOD (CSE) | `HODCS01` | `Password123!` |
| Principal | `PRIN001` | `Password123!` |
| Admin / Management | `ADMIN001` | `Password123!` |
