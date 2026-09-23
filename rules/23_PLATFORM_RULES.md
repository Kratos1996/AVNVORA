# AYNVORA Platform Rules
Version: 1.1

## Android
Follow Android lifecycle, background execution, secure storage and accessibility guidance.

## iOS
Use platform conventions where needed while preserving shared domain logic and AYNVORA visual tokens.

## Desktop
Support keyboard/mouse interaction, focus, resizable layouts, and wide window bounds (>= 1200dp).

## Foldables & Large Screens
- Automatically arrange layouts based on window width size class (Compact, Medium, Expanded).
- Foldable devices in unfolded posture (600dp - 840dp) must present dual-pane or two-column arrangements rather than stretched mobile views.
- Tablet devices (840dp+) must utilize master-detail layouts, side navigation, and centered content constraints.
- Use `sdp` and `ssp` scalable units for harmonious density scaling across form factors.

## Multi-Device Verification
Every new or modified screen must be tested across mobile, foldable, tablet, and desktop form factors before approval.

## KMP
Use common code for domain/business logic. Platform source sets contain adapters only where platform behavior is genuinely different.
