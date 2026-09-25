# AYNVORA AI Model Lifecycle: Download, Install, Update, Rollback & Deletion

## 1. Lifecycle State Machine

Model state transitions follow a deterministic state machine managed by `AiModelLifecycleManager`:

```
               [ NotInstalled ]
                      │
            downloadModel(variant)
                      │
                      ▼
               [ Downloading ]
                      │ (progress: 0.0 -> 1.0)
                      ▼
               [ Installing ]
                      │
       ┌──────────────┴──────────────┐
  [Checksum Match]            [Checksum Mismatch / Error]
       │                             │
       ▼                             ▼
    [ Ready ] <───────────┐      [ Error ]
       │                  │          │
   loadModel()            │    rollbackToPrevious()
       │                  │          │
       ▼                  │          ▼
   [ Loaded ] ────────────┴───> [ Ready (Previous) ]
       │
  deleteInstalledModel()
       │
       ▼
 [ NotInstalled ]
```

## 2. Staging & Atomic Installation

1. **Download Phase**: Bytes stream into temporary storage (`staging/${variant.id}.tmp`).
2. **Checksum Phase**: SHA-256 hash is computed over the downloaded bytes and verified against
   `variant.sha256Checksum`. If mismatched, the temporary file is deleted, and state transitions to
   `Error(CHECKSUM_MISMATCH, previousReadyState)`.
3. **Storage Check**: Ensures available storage $\ge$ `variant.minimumStorageBytes` (plus old model
   size if updating).
4. **Atomic Promotion**: The verified temporary file is moved to the active model location (
   `models/${variant.id}.gguf`).

## 3. Atomic Updates & Rollback Guarantee

- When updating an installed model to a new variant, the currently active model is **not deleted**
  during download or checksum verification.
- If the new model download fails, is cancelled, or fails checksum verification,
  `rollbackToPrevious()` restores the previous model to the `Ready` state without interruption.
- The previous model is purged only after the new model is successfully verified and loaded.

## 4. Deletion Contract

Invoking `deleteInstalledModel()`:

- Cancels active inferences.
- Unloads native tensor handles from memory.
- Deletes installed model files and staging artifacts from disk.
- Sets lifecycle state to `NotInstalled`.
- Consumers (such as Tarot) automatically fall back to deterministic explanations without triggering
  re-downloads.
