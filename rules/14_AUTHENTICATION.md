# AYNVORA Authentication & Identity
Version: 1.0

## Separation
Authentication, authorization, licensing and feature entitlements are separate concepts.

## Requirements
- short-lived access tokens where appropriate
- secure token storage
- refresh/revocation strategy
- device/session management
- rate limiting on server-side APIs
- MFA for administrative accounts
- least-privilege roles
- audit logging for security-sensitive admin actions

## Client Rule
Never store passwords directly in the client and never log credentials or tokens.
