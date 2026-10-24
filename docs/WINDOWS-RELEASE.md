# CampusVoice for Windows

Download the Windows x64 ZIP from this repository's GitHub Releases, extract the entire archive, and double-click `CampusVoice/CampusVoice.exe`. Keep the app and runtime folders beside the executable. Java and Maven are bundled or unnecessary for end users. This is a portable app, not an installer.

Starting with v1.0.1, without database configuration the app stores data in `%USERPROFILE%/.campusvoice/data/`. Student and HoD sessions running under the same Windows account share this persistent local database. Use Refresh in the HoD complaints queue after another session submits a complaint. Data from the v1.0.0 temporary database cannot be recovered after that process closes.

For student and HoD apps on different computers or Windows accounts, connect the SAME PostgreSQL database on every machine. In v1.0.2, click **Database connection** on the login screen, enter your administrator-provided JDBC URL, database username and password, then choose **Test and connect**. Sign in after the connection succeeds. Repeat on each computer. This option keeps credentials in memory only; reconnect after reopening the app. Existing local complaints are not migrated to PostgreSQL.

For administrator-managed persistent configuration, the app also supports SUPABASE_DB_URL, SUPABASE_DB_USER, and SUPABASE_DB_PASSWORD environment variables or an existing `%USERPROFILE%/.campusvoice/.env` file. Follow DATABASE.md to initialize the shared schema. Local databases do not synchronize between computers. The dashboard header identifies local versus shared data. A failed configured PostgreSQL connection no longer silently switches to a separate local demo database.

Example configuration (replace placeholders with your administrator-provided values):

```dotenv
SUPABASE_DB_URL=jdbc:postgresql://YOUR_HOST:5432/postgres?sslmode=require
SUPABASE_DB_USER=YOUR_USER
SUPABASE_DB_PASSWORD=YOUR_PASSWORD
```

Never upload your .env file or database credentials to GitHub or include them in a release. Errors are recorded in `%USERPROFILE%/.campusvoice/logs/campusvoice.log`.

The executable is unsigned. Windows may display a publisher warning.

## Build from source

On Windows x64 with JDK 17 and Maven available, run from the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/build-windows.ps1 -Version 1.0.2
```

The script runs the Maven tests, collects runtime dependencies, creates an executable with a bundled Java runtime using jpackage, and writes a ZIP under dist/. It packages only the app JAR and runtime dependency JARs.

## Publish on GitHub

Commit and push the project including scripts/ and .github/workflows/windows-release.yml. Then create and push a new version tag:

```powershell
git tag v1.0.2
git push origin v1.0.2
```

The Windows release workflow builds and tests the app and publishes the ZIP on GitHub Releases. Use a new numeric version tag for each release. You can also run the workflow manually in the Actions tab to download a build artifact without publishing a release.
