# AYNVORA Palmistry (Hastrekha) Experience Specification

Version: 1.0 (Phase 8.10)  
Status: Production Implemented  
Feature ID: `PALMISTRY`

---

## 1. Product Boundary & Ethical Framing

Palmistry (Hastrekha) in AYNVORA is presented strictly as a **traditional, symbolic, and reflective
contemplative tool** rooted in classical Indian Samudrika Shastra.

### Strict Ethical Guardrails

- **No Lifespan / Medical Predictions**: Does not predict longevity, death, illness, medical
  diagnostics, or biological certainty.
- **No Absolute Financial / Legal Outcomes**: Does not guarantee wealth, bankruptcy, lawsuits, or
  definite legal victories.
- **No Deterministic Fatalism**: Reframes line traits (length, curvature, branching, depth) as
  archetypal tendencies, cognitive habits, and reflective touchpoints.
- **Mandatory Disclaimer Gate**: Users must explicitly review and acknowledge the philosophical
  disclaimer before accessing capture, analysis, or interpretations.

---

## 2. Complete User Journey Flow

```
Palmistry Home
       │
       ▼
   Disclaimer (Acknowledge)
       │
       ▼
Hand Selection (LEFT / RIGHT)
       │
       ▼
Image Input (Camera Capture or Gallery Photo Picker)
       │
       ▼
Image Quality Validation (Resolution, Sharpness, Lighting, Bounds)
       │
       ▼
Palm Analysis (Deterministic Gradient/Contour Feature Extraction)
       │
       ▼
Structured Evidence & Traditional Interpretation (Samudrika Shastra)
       │
       ▼
On-Device AI Explanation (SLM Grounding with Deterministic Fallback)
       │
       ▼
Interactive Questions ("Ask About This Reading")
       │
       ▼
Reading Timeline (Chronological Audit Trail)
       │
       ▼
Multi-Tier Feedback (1-5 Stars, Per-Answer, Per-Feature)
       │
       ▼
Palmistry Report & PDF Export (Structured Document Engine)
       │
       ▼
Session History (Local Storage with Explicit Image Retention Controls)
```

---

## 3. Screen Inventory & Component Breakdown

### 1. Palmistry Home (`PalmScreen.Home`)

- **Hero Display**: Gold/Cosmic celestial aesthetic displaying Hastrekha overview and active session
  statistics.
- **Action**: "Begin Palm Reading" launches the flow; links to historical readings and capabilities
  directory.
- **Top-Right**: Global Language Selector (`EN` / `HI`).

### 2. Disclaimer (`PalmScreen.Disclaimer`)

- **Framing**: Transparent statement that Hastrekha is a contemplative art of self-reflection and
  classical symbolism, not a substitute for medical, psychological, or financial counsel.
- **Acknowledge CTA**: Advances to Hand Selection.

### 3. Hand Selection (`PalmScreen.HandSelection`)

- **Supported Hands**: `LEFT` (receptive, latent potentials, inner tendencies) and `RIGHT` (active,
  manifested paths, current expression).
- **Rule**: Hand selection is explicitly user-declared; never guessed from image bytes without user
  confirmation.

### 4. Image Capture & Gallery Picker (`PalmScreen.Capture`)

- **Dual Mode**: Direct live camera capture or local gallery image selection via `PalmImageSource`.
- **Capture Guidance**:
    - Open palm facing camera squarely.
    - Fingers gently separated and visible.
    - Uniform ambient lighting without harsh directional shadows.
    - Plain, high-contrast background with wrist visible.
- **Synthetic Test Image Generator**: Built-in developer/testing tool generates standard test palm
  buffers to verify vision algorithms offline.

### 5. Quality Validation Gate (`PalmScreen.QualityCheck`)

- **Evaluation Criteria**: Computes luminance, edge variance (Laplacian sharpness), and dimensional
  bounds.
- **Typed States**: `GOOD`, `LOW_RESOLUTION`, `BLURRY`, `TOO_DARK`, `TOO_BRIGHT`,
  `HAND_NOT_DETECTED`, `PALM_NOT_VISIBLE`, `OBSTRUCTED`, `WRONG_ORIENTATION`, `UNSUPPORTED`.
- **Action**: Blocks progression if quality is unviable, presenting specific troubleshooting
  recommendations and a "Recapture" button.

### 6. Palm Analysis Progress (`PalmScreen.Analyzing`)

- **Real Processing**: Executes deterministic structural gradient analysis over anatomical palm
  zones (`HYPOTHENAR`, `THENAR`, `INTERDIGITAL`, `CENTRAL_PALM`).
- **Zero Simulation**: Does not generate synthetic coordinates or fake lines. Features not visible
  or unmeasurable are honestly marked `NOT_DETECTED` or `UNSUPPORTED`.

### 7. Reading Results (`PalmScreen.Result`)

- **Three-Tier Separation**:
    1. **Real Observations**: Measured line continuity, relative length, curvature, and clarity.
    2. **Traditional Interpretation**: Verified classical texts from Samudrika Shastra.
    3. **AI Explanation**: Grounded synthesis explaining how traditional symbolism invites personal
       reflection.
- **Feature Cards**: Interactive cards for detected lines (`LIFE_LINE`, `HEAD_LINE`, `HEART_LINE`,
  `FATE_LINE`) and overall `PalmShape`.

### 8. Feature Detail Screen (`PalmScreen.FeatureDetail`)

- Deep dive into an individual feature with confidence score, source provenance, traditional
  interpretation, and dedicated feedback controls.

### 9. Interactive Follow-up Questions

- Natural language query input ("Ask about this reading").
- Context-filtered evidence query; returns `INSUFFICIENT_EVIDENCE` honestly when queried lines are
  absent.

### 10. Timeline (`PalmScreen.Timeline`)

- Chronological list of events (`READING_STARTED`, `IMAGE_SELECTED`, `IMAGE_VALIDATED`,
  `ANALYSIS_COMPLETED`, `AI_EXPLANATION_GENERATED`, `QUESTION_ASKED`, etc.) linked to a persistent
  `readingId`.

### 11. Multi-Tier Feedback

- Overall 1-5 star rating and optional text review.
- Per-answer "Helpful / Not Helpful" toggle.
- Per-feature "Clear / Confusing / Need More Context" triage.
- Generates anonymous, privacy-safe `AiImprovementSignal` records without transmitting PII or raw
  images.

### 12. Report & PDF Export (`PalmScreen.Report`)

- Integration with AYNVORA Report Engine (`ReportType.PALMISTRY`).
- Renders structured sections: Overview, Findings, Traditional Meanings, Grounded Explanation, Q&A,
  and Ethical Disclaimer.
- Direct PDF generation via `ReportPdfGenerator`.

---

## 4. Multilanguage Integration & Global Header

- **Supported Languages**: English (`EN`) and Hindi (`HI`) fully translated across all 40+ UI
  tokens, feature titles, descriptions, warnings, and reports.
- **Top-Right Language Switcher**: Mounted in the top-right app bar of **every single Palmistry
  screen**.
- **State Preservation**: Switching language dynamically recomposes localized strings while
  preserving active `readingId`, analysis findings, timeline, and input state.
