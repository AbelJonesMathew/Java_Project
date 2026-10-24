param(
    [string]$Version = '1.0.3',
    [string]$SigningCertificateThumbprint,
    [ValidateSet('CurrentUser', 'LocalMachine')][string]$CertificateStoreLocation = 'CurrentUser',
    [switch]$RequireSigning
)
$ErrorActionPreference = 'Stop'
if ($Version -notmatch '^\d+\.\d+\.\d+$') { throw 'Version must be numeric, for example 1.0.0.' }
if ($RequireSigning -and -not $SigningCertificateThumbprint) {
    throw 'A trusted code-signing certificate is required. Set SigningCertificateThumbprint; this build will not publish an unsigned replacement.'
}
$root = Split-Path $PSScriptRoot -Parent
$project = Join-Path $root 'campusvoice-java'
$jdk = Split-Path (Split-Path (Get-Command javac.exe -ErrorAction Stop).Source)
$env:JAVA_HOME = $jdk
$maven = Join-Path $project 'mvn/apache-maven-3.9.6/bin/mvn.cmd'
if (-not (Test-Path $maven)) { $maven = (Get-Command mvn.cmd -ErrorAction Stop).Source }
$output = Join-Path $root ('dist/' + $Version + '-' + (Get-Date -Format 'yyyyMMddHHmmss'))
$inputDir = Join-Path $output 'input'
New-Item -ItemType Directory -Path $inputDir -Force | Out-Null
Push-Location $project
try {
    & $maven --batch-mode clean verify
    if ($LASTEXITCODE -ne 0) { throw 'Maven verification failed.' }
    & $maven --batch-mode org.apache.maven.plugins:maven-dependency-plugin:3.6.1:copy-dependencies '-DincludeScope=runtime' "-DoutputDirectory=$inputDir"
    if ($LASTEXITCODE -ne 0) { throw 'Dependency collection failed.' }
    [xml]$pom = Get-Content (Join-Path $project 'pom.xml')
    $jar = "$($pom.project.artifactId)-$($pom.project.version).jar"
    Copy-Item (Join-Path $project "target/$jar") $inputDir
    $packageArguments = @('--type', 'app-image', '--name', 'CampusVoice', '--app-version', $Version,
        '--vendor', 'CampusVoice', '--input', $inputDir, '--main-jar', $jar, '--main-class', 'com.campusvoice.Main',
        '--dest', $output, '--java-options', '-Dfile.encoding=UTF-8', '--add-modules', 'ALL-MODULE-PATH')
    if ($SigningCertificateThumbprint) {
        # Avoid extracting an unsigned optional FlatLaf DLL from its dependency JAR at runtime.
        $packageArguments += @('--java-options', '-Dflatlaf.useNativeLibrary=false')
    }
    & (Join-Path $jdk 'bin/jpackage.exe') @packageArguments
    if ($LASTEXITCODE -ne 0) { throw 'Windows packaging failed.' }
    if ($SigningCertificateThumbprint) {
        & (Join-Path $PSScriptRoot 'sign-windows.ps1') -ApplicationDirectory (Join-Path $output 'CampusVoice') `
            -CertificateThumbprint $SigningCertificateThumbprint -CertificateStoreLocation $CertificateStoreLocation
    }
    Copy-Item (Join-Path $root 'docs/WINDOWS-RELEASE.md') (Join-Path $output 'CampusVoice/READ-ME.md')
    Copy-Item (Join-Path $root 'docs/WINDOWS-SIGNING.md') (Join-Path $output 'CampusVoice/WINDOWS-SIGNING.md')
    Copy-Item (Join-Path $root 'docs/DATABASE.md') (Join-Path $output 'CampusVoice/DATABASE.md')
    $zip = Join-Path $output "CampusVoice-$Version-windows-x64.zip"
    Compress-Archive -Path (Join-Path $output 'CampusVoice') -DestinationPath $zip
    Write-Host "Release archive: $zip"
} finally { Pop-Location }
