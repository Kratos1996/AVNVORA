# AYNVORA Security Checklist
Version: 1.0

Before release:
- [ ] No secrets in repository/build artifacts
- [ ] Signing keys are outside source control
- [ ] Sensitive local data uses approved secure storage
- [ ] Network uses TLS
- [ ] Certificate pinning considered/documented where applicable
- [ ] Input validation exists at trust boundaries
- [ ] Sensitive logs reviewed
- [ ] Dependencies reviewed/scanned
- [ ] License/update signatures verified
- [ ] Entitlement bypass scenarios tested
- [ ] Admin authentication/MFA and least privilege verified
- [ ] Tenant isolation tested where applicable
- [ ] Data retention/deletion behavior documented
- [ ] Threat model reviewed
