# CampusVoice for Windows

Download the Windows x64 ZIP from this repository's GitHub Releases, extract the entire archive, and double-click `CampusVoice/CampusVoice.exe`. Keep the app and runtime folders beside the executable. Java and Maven are bundled or unnecessary for end users. This is a portable app, not an installer.

Without database configuration, the app uses its existing in-memory demo database. Changes in demo mode are lost when the app closes. For persistent storage, follow DATABASE.md and configure SUPABASE_DB_URL, SUPABASE_DB_USER, and SUPABASE_DB_PASSWORD as user environment variables before starting the app. Never upload your .env file or database credentials to GitHub or include them in a release.

The executable is unsigned. Windows may display a publisher warning.

## Build from source

On Windows x64 with JDK 17 and Maven available, run from the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/build-windows.ps1 -Version 1.0.0
```

The script runs the Maven tests, collects runtime dependencies, creates an executable with a bundled Java runtime using jpackage, and writes a ZIP under dist/. It packages only the app JAR and runtime dependency JARs.

## Publish on GitHub

Commit and push the project including scripts/ and .github/workflows/windows-release.yml. Then create and push a new version tag:

```powershell
git tag v1.0.0
git push origin v1.0.0
```

The Windows release workflow builds and tests the app and publishes the ZIP on GitHub Releases. Use a new numeric version tag for each release. You can also run the workflow manually in the Actions tab to download a build artifact without publishing a release.
