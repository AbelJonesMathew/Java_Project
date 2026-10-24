param(
    [Parameter(Mandatory = $true)][string]$ApplicationDirectory,
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Fa-f0-9]{40}$')][string]$CertificateThumbprint,
    [ValidateSet('CurrentUser', 'LocalMachine')][string]$CertificateStoreLocation = 'CurrentUser',
    [string]$TimestampUrl = 'https://timestamp.digicert.com'
)
$ErrorActionPreference = 'Stop'
$application = (Resolve-Path -LiteralPath $ApplicationDirectory).Path
if (-not (Test-Path -LiteralPath (Join-Path $application 'CampusVoice.exe'))) { throw 'Application directory must contain CampusVoice.exe.' }
$certificate = Get-Item -LiteralPath "Cert:/$CertificateStoreLocation/My/$CertificateThumbprint" -ErrorAction Stop
if (-not $certificate.HasPrivateKey) { throw 'The certificate private key is unavailable.' }
if ($certificate.Subject -eq $certificate.Issuer) { throw 'A self-signed certificate cannot be used for this public release.' }
if ($certificate.NotAfter -le (Get-Date) -or $certificate.NotBefore -gt (Get-Date)) { throw 'The signing certificate is not currently valid.' }
$usage = $certificate.Extensions | Where-Object { $_ -is [System.Security.Cryptography.X509Certificates.X509EnhancedKeyUsageExtension] }
if ('1.3.6.1.5.5.7.3.3' -notin @($usage.EnhancedKeyUsages | ForEach-Object { $_.Value })) {
    throw 'The selected certificate is not a code-signing certificate.'
}
$chain = [System.Security.Cryptography.X509Certificates.X509Chain]::new()
try {
    if (-not $chain.Build($certificate)) { throw 'The certificate does not build a trusted certificate chain on this computer.' }
} finally { $chain.Dispose() }
$tool = Get-Command signtool.exe -ErrorAction SilentlyContinue
if ($tool) { $signTool = $tool.Source }
else {
    $sdk = Join-Path ${env:ProgramFiles(x86)} 'Windows Kits/10/bin'
    $candidate = Get-ChildItem -LiteralPath $sdk -Recurse -Filter signtool.exe -ErrorAction SilentlyContinue |
        Where-Object { $_.Directory.Name -eq 'x64' } | Sort-Object FullName -Descending | Select-Object -First 1
    if (-not $candidate) { throw 'Install Windows SDK Signing Tools (signtool.exe) first.' }
    $signTool = $candidate.FullName
}
$files = @(Get-ChildItem -LiteralPath $application -Recurse -File | Where-Object { $_.Extension -in '.exe', '.dll' })
foreach ($file in $files) {
    $signature = Get-AuthenticodeSignature -LiteralPath $file.FullName
    if ($signature.Status -eq 'Valid') { continue }
    if ($signature.Status -ne 'NotSigned') { throw "Existing signature is invalid on $($file.Name). Obtain a clean dependency before signing." }
    $arguments = @('sign', '/sha1', $CertificateThumbprint, '/fd', 'SHA256', '/tr', $TimestampUrl, '/td', 'SHA256')
    if ($CertificateStoreLocation -eq 'LocalMachine') { $arguments += '/sm' }
    & $signTool @arguments $file.FullName
    if ($LASTEXITCODE -ne 0) { throw "Signing failed for $($file.Name)." }
}
foreach ($file in $files) {
    & $signTool verify /pa /all $file.FullName
    if ($LASTEXITCODE -ne 0) { throw "Signature verification failed for $($file.Name)." }
}
Write-Host "Verified trusted signatures on $($files.Count) executable and DLL files."
