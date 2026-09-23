# AYNVORA Security Specification
Version: 1.0

## Security Goals
Confidentiality, integrity, availability, tenant isolation, secure licensing, safe updates and protection of user data.

## Threat Areas
- reverse engineering
- tampering
- credential leakage
- license bypass
- malicious update
- insecure network
- local data theft
- excessive logging
- dependency compromise
- API abuse
- tenant/data isolation failure

## Controls
- platform secure storage/keystore/keychain
- encryption for sensitive data at rest where required
- TLS for network traffic
- certificate/public-key pinning where justified by threat model
- signed licenses/entitlements
- signed data/update packages
- secure secret management
- least privilege
- dependency scanning
- input validation
- secure logging
- integrity checks

## Rules
Never embed private signing keys or server secrets in the SDK/client.
Never treat client-side entitlement checks as the sole authority for high-value server-side actions.
Do not rely on obfuscation as the only security control.

## Storage and Persistence Security Boundaries
- **No Credentials at Rest**: Passwords, authentication tokens, API keys, and server secrets are strictly prohibited from local profile persistence tables and entities.
- **Privacy Minimization**: Stored profiles contain only data necessary for chart calculation (name, birth date, birth time, coordinates, timezone).
- **Encryption Abstraction**: `StorageCipher` abstraction decouples the storage engine from cryptography implementations, allowing pluggable integration with platform-native keystores (Android Keystore, Apple Keychain, JVM Keystore).
- **Error Sanitization**: Database and filesystem errors are sanitized into structured `AynvoraResult.Failure` categories (`NotFound`, `StorageFailure`, `CorruptedData`, `MigrationFailure`) to prevent leaking internal directory structures or system paths to calling layers.

## Verification
Security controls require tests and documented threat-model coverage.
