# AYNVORA Event Security & Guard System

Version: 1.0 (Phase 9.0)

## 1. Principles

Events are interactive user triggers, but they must **never** be trusted implicitly or used to
bypass business domain boundaries:

1. **Never Trust UI Payloads Blindly**: All payload fields are subjected to `AynvoraEventGuard`
   validation before reaching handlers.
2. **Forbidden Namespaces**: Internal/admin namespaces (e.g. `admin.*`, `system.*`) originating from
   UI are rejected with `Unauthorized`.
3. **Bounded Validation**:
    - Feedback ratings outside `1..5` return `InvalidPayload`.
    - Empty or blank IDs for features, models, spreads, or texts return `InvalidPayload`.
    - Unknown feature IDs not in `CanonicalCoreFeatures` return `InvalidPayload`.
4. **Idempotency Protection**: Destructive or costly operations (`ai.delete.clicked`,
   `ai.download.clicked`) enforce `AynvoraDeduplicationPolicy.IDEMPOTENT`, preventing accidental
   double-deletion or duplicate downloads.
5. **Short-Window Debounce**: Critical submission actions (`tarot.question.submit_clicked`, rapid
   navigation taps) enforce `AynvoraDeduplicationPolicy.DEDUP_SHORT_WINDOW` (350-400ms threshold).
6. **Graceful Failures**: Malformed or unauthorized events return structured results (`Invalid`,
   `Unauthorized`, `Rejected`) and never crash the host application.
