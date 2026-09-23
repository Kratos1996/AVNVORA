# AYNVORA Project Structure
Version: 1.0

## Repository
- apps/: host/reference applications
- modules/: reusable SDK modules
- design-system/: tokens/components/assets
- docs/: authoritative specifications
- build-logic/: convention plugins when justified
- gradle/: version catalog/wrapper
- scripts/: reproducible tooling
- security/: threat models and security test artifacts where appropriate

## KMP
Use commonMain/commonTest for shared behavior. Android/iOS/Desktop source sets contain only genuinely platform-specific code.

## Rule
Do not duplicate domain logic in androidMain, iosMain or desktopMain.
