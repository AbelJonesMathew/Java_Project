$ErrorActionPreference = "Stop"

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "         CAMPUSVOICE JAVA AUTOMATED LAUNCHER            " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

# 1. Detect & Configure JAVA_HOME (JDK check)
function Test-ValidJdk($path) {
    if (-not $path) { return $false }
    if ($path -match "javapath") { return $false }
    $hasJavac = Test-Path (Join-Path $path "bin\javac.exe")
    $hasJava  = Test-Path (Join-Path $path "bin\java.exe")
    return ($hasJavac -and $hasJava)
}

$detectedJdk = $null

# Check 1: Existing $env:JAVA_HOME
if (Test-ValidJdk $env:JAVA_HOME) {
    $detectedJdk = $env:JAVA_HOME
}

# Check 2: Check javac command if on PATH and not an Oracle javapath redirector
if (-not $detectedJdk) {
    $javacCmd = Get-Command javac -ErrorAction SilentlyContinue
    if ($javacCmd -and ($javacCmd.Source -notmatch "javapath")) {
        $candidate = Split-Path (Split-Path $javacCmd.Source)
        if (Test-ValidJdk $candidate) {
            $detectedJdk = $candidate
        }
    }
}

# Check 3: Standard JDK directories on Windows
if (-not $detectedJdk) {
    Write-Host "[INFO] Detecting installed JDK on system..." -ForegroundColor Yellow
    $searchRoots = @(
        "C:\Program Files\Java",
        "C:\Program Files\Java\latest",
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files (x86)\Java",
        "C:\Program Files\Amazon Corretto",
        "C:\Java",
        "$env:USERPROFILE\.jdk"
    )

    $validJdks = @()
    foreach ($root in $searchRoots) {
        if (Test-Path $root) {
            if (Test-ValidJdk $root) {
                $validJdks += $root
            }
            $subDirs = Get-ChildItem -Path $root -Directory -ErrorAction SilentlyContinue
            foreach ($dir in $subDirs) {
                if (Test-ValidJdk $dir.FullName) {
                    $validJdks += $dir.FullName
                }
            }
        }
    }

    if ($validJdks.Count -gt 0) {
        $exactJdks = $validJdks | Where-Object { $_ -notmatch "\\latest\\" }
        if ($exactJdks) {
            $detectedJdk = ($exactJdks | Sort-Object -Descending | Select-Object -First 1)
        } else {
            $detectedJdk = ($validJdks | Sort-Object -Descending | Select-Object -First 1)
        }
    }
}

if ($detectedJdk) {
    $env:JAVA_HOME = $detectedJdk
    [System.Environment]::SetEnvironmentVariable("JAVA_HOME", $detectedJdk, "Process")
    $env:PATH = "$detectedJdk\bin;$env:PATH"
    [System.Environment]::SetEnvironmentVariable("PATH", "$detectedJdk\bin;$env:PATH", "Process")
    Write-Host "[SUCCESS] Using JDK at: $env:JAVA_HOME" -ForegroundColor Green
} else {
    Write-Host "[WARNING] No valid JDK installation found with bin\javac.exe and bin\java.exe. Please ensure Java 17+ JDK is installed." -ForegroundColor Red
}

# 2. Check & Configure Maven
$mvnPath = Get-Command mvn -ErrorAction SilentlyContinue

if (-not $mvnPath) {
    $localMvnBin = Join-Path $PSScriptRoot "mvn\apache-maven-3.9.6\bin\mvn.cmd"
    
    if (-not (Test-Path $localMvnBin)) {
        Write-Host "[INFO] Downloading embedded Maven to project folder..." -ForegroundColor Yellow
        $zipPath = Join-Path $PSScriptRoot "maven_temp.zip"
        $mvnDir = Join-Path $PSScriptRoot "mvn"
        
        Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip" -OutFile $zipPath
        Write-Host "[INFO] Extracting Maven..." -ForegroundColor Yellow
        Expand-Archive -Path $zipPath -DestinationPath $mvnDir -Force
        Remove-Item $zipPath -Force
    }
    
    $mvnCmd = $localMvnBin
}
else {
    $mvnCmd = "mvn"
}

# 3. Build Project
Write-Host "[INFO] Verifying & Building CampusVoice Java Application..." -ForegroundColor Green
Set-Location $PSScriptRoot
& $mvnCmd compile -DskipTests

# 4. Launch Application
Write-Host "[INFO] Launching CampusVoice Swing GUI..." -ForegroundColor Green
& $mvnCmd exec:java "-Dexec.mainClass=com.campusvoice.Main" "-Dfile.encoding=UTF-8"
