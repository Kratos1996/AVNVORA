# AYNVORA Threat Model
Version: 1.0

## Assets
- user birth/profile data
- authentication credentials/tokens
- license/entitlement state
- signing keys
- astrology rule/data packages
- customer configuration
- source/IP
- telemetry

## Threat Actors
- malicious client user
- reverse engineer
- compromised device
- network attacker
- malicious dependency/update
- compromised administrator
- abusive API client

## Security Priorities
1. protect signing/private keys
2. protect user data
3. protect update integrity
4. protect licensing/tenant boundaries
5. minimize exposed attack surface

## Process
Threat models must be revisited when architecture, trust boundaries, external services or sensitive assets change.
