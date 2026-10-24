# CampusVoice
### *"Speak • Report • Get It Solved"*

**CampusVoice** is a Smart College Complaint Management System featuring AI-driven automatic priority detection, multilingual support, student tracking, and dedicated dashboards for HODs, Principals, and Management.

---

## Key Features

- **AI-Based Automatic Priority Detection**: Evaluates complaint text, category, and keywords using TF-IDF + Machine Learning combined with safety overrides.
- **Multilingual Support**: Submit complaints in Malayalam, Hindi, Tamil, Telugu, Kannada, or English with automated language detection & translation.
- **Anonymous & Group Complaints**: Keep identities protected or unite multiple student supporters under a single issue.
- **Role-Based Access Control (RBAC)**: Custom operational dashboards for **Student**, **HOD**, **Principal**, and **Management / Admin**.
- **Automated Escalation Rules & SLA Tracking**: Automatically elevates unresolved high/medium priority complaints up the chain of command.
- **Analytics & Reports**: Visual charts, AI accuracy tracking, and exportable PDF/CSV reports.

---

## Project Structure

```
CampusVoice/
├── campusvoice-java/       # 100% Native Java 17+ Application
│   ├── src/main/java/com/campusvoice/
│   │   ├── ai/             # Native Java AI Suite (TF-IDF, Classifier, Translation, Server)
│   │   ├── ui/             # Java Swing Desktop UI with FlatLaf Theme (Student, HOD, Principal, Management)
│   │   ├── service/        # Business Logic & In-Process AI Integration
│   │   ├── dao/            # Data Access Layer (PostgreSQL & H2 In-Memory Fallback)
│   │   └── security/       # Role-Based Access Control & Password Hashing
│   └── src/test/java/      # Comprehensive JUnit 5 AI Test Suite
├── database/               # PostgreSQL / Supabase Schema and Seed scripts
├── docs/                   # System documentation (Architecture, API, Database, AI, Setup)
├── .env.example            # Environment properties template
└── README.md
```

---

## Quick Start

### Windows executable

See [Windows releases](docs/WINDOWS-RELEASE.md) for portable `.exe` downloads, local packaging, and automatic GitHub Releases. Extract the full ZIP before launching `CampusVoice.exe`; no Java installation is required.

### 1. Database Setup (Optional)
Execute `database/schema.sql` followed by `database/seed.sql` on your PostgreSQL / Supabase database instance. Configure the same database for every student and HoD installation to share complaints across computers. Without PostgreSQL configuration, CampusVoice uses a persistent H2 database under the current Windows account's `.campusvoice/data` directory, initialized with demo users and complaints.

### 2. Launch Java Application (1-Click Run)
Simply double-click `run.bat` in the root folder or execute:
```cmd
run.bat
```
*(The launcher automatically detects your Java 17+ JDK, configures embedded offline Maven, verifies classes, and launches the FlatLaf Swing GUI with embedded native AI.)*

### 3. AI Tools & Test Suite (Pure Java - Zero Python)
- **Run AI Test Suite**: `cd campusvoice-java && powershell -Command "& .\mvn\apache-maven-3.9.6\bin\mvn.cmd exec:java '-Dexec.mainClass=com.campusvoice.ai.TestRunner' '-Dexec.classpathScope=test'"`
- **Train AI Model**: `cd campusvoice-java && powershell -Command "& .\mvn\apache-maven-3.9.6\bin\mvn.cmd exec:java '-Dexec.mainClass=com.campusvoice.ai.TrainModel'"`
- **Run Standalone AI Server**: `cd campusvoice-java && powershell -Command "& .\mvn\apache-maven-3.9.6\bin\mvn.cmd exec:java '-Dexec.mainClass=com.campusvoice.ai.AIServer'"`

---

## Default Credentials (Demo Quick-Select Enabled)

Click any of the quick-select role buttons on the login screen to automatically fill demo credentials:

- 🎓 **Student**: `STU202501` / `Password123!`
- 👤 **HOD (CSE)**: `HODCS01` / `Password123!`
- 🏛️ **Principal**: `PRIN001` / `Password123!`
- ⛪ **Management**: `ADMIN001` / `Password123!`

For detailed documentation, see the [docs/](file:///c:/Users/Abel/Desktop/Projects/Java%20Project/docs) folder.
