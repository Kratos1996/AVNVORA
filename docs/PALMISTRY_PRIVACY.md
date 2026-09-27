# AYNVORA Palmistry Privacy & Security Architecture

Version: 1.0 (Phase 8.10)  
Status: Production Implemented  
Privacy Tier: `LOCAL_ONLY`

---

## 1. Local-Only Privacy Guarantee

Palm images are sensitive biometric/physical artifacts. AYNVORA enforces a zero-trust, local-only
architectural boundary:

```
[Camera / Gallery Input]
         │
         ▼
[In-Memory ByteArray / Temp Sandbox]
         │
         ▼
[Deterministic Feature Extraction]  ───(Raw Image Dropped from Memory)
         │
         ▼
[Structured Findings Only Saved to Session Storage]
```

### Prohibited Data Practices:

- **No Cloud Upload**: Palm images are never transmitted to cloud endpoints, remote servers, or
  external AI APIs.
- **No Analytics Leakage**: Raw images, filenames, pixel buffers, questions, or AI answers are never
  logged to analytics.
- **No Database Image Blobs**: Room DB and persistent session files store only typed metadata (
  `readingId`, `hand`, line observations, timestamps).

---

## 2. Image Retention Policy & User Deletion

- **Volatile Processing**: Image bytes passed into `PalmImageSource.ByteArraySource` or captured via
  camera are held in memory only for the duration of the analysis session.
- **Explicit Lifecycle**: If temporary sandbox files are used, they are deleted automatically upon
  session completion or user exit.
- **User-Initiated Purge**: When a user deletes a reading session from their history via
  `PalmSessionRepository.deleteSession()`, all associated metadata and temporary files are purged
  immediately.

---

## 3. Analytics Hygiene & Allowed Events

Only privacy-safe, non-PII telemetry events are recorded:

| Event Name                      | Allowed Metadata                                 | Strictly Forbidden Data            |
|---------------------------------|--------------------------------------------------|------------------------------------|
| `palmistry_opened`              | `language`                                       | User identity, device serials      |
| `palmistry_hand_selected`       | `hand`, `reading_id`                             | User name, demographic data        |
| `palmistry_image_selected`      | `source_type` (`CAMERA`/`GALLERY`)               | Image file path, image bytes, EXIF |
| `palmistry_analysis_started`    | `hand`, `analysis_version`                       | Raw image buffer                   |
| `palmistry_analysis_completed`  | `hand`, `detected_features_count`, `duration_ms` | Biometric markers, facial features |
| `palmistry_analysis_failed`     | `failure_code` (`TOO_DARK`, `BLURRY`, etc.)      | Image bytes                        |
| `palmistry_question_submitted`  | `feature_id`, `reading_id`                       | Raw question text, user notes      |
| `palmistry_ai_answer_generated` | `model_id`, `model_version`, `duration_ms`       | Raw response text                  |
| `palmistry_ai_fallback_used`    | `fallback_reason`                                | Raw response text                  |
| `palmistry_feedback_submitted`  | `rating`, `category`, `feature_id`               | Freeform text with personal data   |
| `palmistry_report_generated`    | `report_type`, `language`                        | Full report content                |
| `palmistry_pdf_generated`       | `language`                                       | PDF file bytes                     |
