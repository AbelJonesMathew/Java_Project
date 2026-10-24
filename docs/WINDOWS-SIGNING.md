# Windows trusted signing setup

CampusVoice v1.0.3 is unsigned. Rebuilding or renaming it does not resolve Smart App Control. Do not label an unsigned replacement as a signing fix.

## Choose distribution and identity verification

- Microsoft Store MSIX distribution: Microsoft signs the package after certification. Register at https://storedeveloper.microsoft.com/ and reserve the app in Partner Center. MSIX packaging needs the assigned package identity and publisher identity. The existing ZIP/EXE is not an MSIX package; submitting an EXE through the Store does not make Microsoft sign it.
- Direct GitHub distribution: obtain a publicly trusted code-signing certificate from a provider that accepts your country and developer identity, or use an eligible cloud signing service. Complete the provider's identity verification. Keep the private key in its hardware token or cloud HSM, not in the repository.
- Qualifying open-source projects can apply to SignPath Foundation. A public repository alone does not establish eligibility; its license and the program requirements must be reviewed first.

Microsoft's current options and eligibility: https://learn.microsoft.com/en-us/windows/apps/package-and-deploy/code-signing-options

## Certificate-backed local build

Install the provider's key/token software and Windows SDK Signing Tools. The certificate must be available in the Windows Personal certificate store with its private key accessible. The following command uses its thumbprint, which is not the private key:

```powershell
./scripts/build-windows.ps1 -Version 1.0.3 -RequireSigning -SigningCertificateThumbprint YOUR_40_CHARACTER_THUMBPRINT
```

Use `-CertificateStoreLocation LocalMachine` if the provider installed the certificate there. The signing script validates certificate usage, validity and chain, signs unsigned EXE/DLL files with SHA-256 and a timestamp, retains valid dependency signatures, and verifies every executable/DLL before creating the ZIP. Optional FlatLaf native library extraction is disabled for this build because an embedded unsigned DLL would otherwise require separate signing.

The default GitHub workflow still builds unsigned artifacts until a signing provider is configured. A local certificate or hardware token is not automatically available on a GitHub-hosted runner. Cloud signing integration must be configured for the selected provider; use GitHub secrets/OIDC and never commit signing keys or credentials.

## Final validation

Test the completed package on a separate Windows machine with Smart App Control enabled. Trusted signing addresses publisher identification but is not a guarantee against every security or administrator policy. See https://learn.microsoft.com/en-us/windows/apps/develop/smart-app-control/code-signing-for-smart-app-control
