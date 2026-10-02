# Varshaphal calculation and knowledge status

Status: PARTIAL.

The SDK now calculates a solar return with the configured supported ayanamsa, recalculates an annual chart at that UTC instant, and adapts the result to the shared `AstroChart` model. The solar coordinates use the existing Meeus Sun implementation, which documents approximately 0.01 degree accuracy near J2000; reported timestamp seconds are formatting precision.

The source-backed knowledge rules remain the scoped `TAJIKA_V1` pack under `knowledge/tajika/`. It contains selected Muntha house indications and a selected Sun/Varshesha interpretation, not the formulas needed to calculate Muntha progression, select Varsheshwara, calculate Sahams, Tajika aspects, or Mudda Dasha. Those components remain unsupported or not verified. Caller-supplied Muntha facts are not treated as engine calculations.

Do not promote additional rules until the source edition, exact page or verse, rights, structured conditions, and independent verification are recorded. See [the knowledge acquisition report](../../docs/KNOWLEDGE_ACQUISITION_REPORT.md) and [the Phase 10.24 status](../../docs/PHASE_10_24_STATUS.md).
