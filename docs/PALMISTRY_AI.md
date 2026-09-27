# AYNVORA Palmistry AI Explanation Architecture

Version: 1.0 (Phase 8.10)  
Status: Production Implemented  
Engines: `GroundedSlmPalmistryExplanationEngine`, `DeterministicPalmistryExplanationEngine`

---

## 1. Non-Vision SLM Principle & Separation of Concerns

AYNVORA's on-device SLM models (e.g. Qwen2.5-Instruct 0.5B/1.5B GGUF) are **text-only language
models**.

- **Strict Rule**: Raw palm images or binary pixel data are **never** passed directly to the
  text-only SLM under the pretense that the SLM "saw" the image.
- **Computer Vision Pipeline**: The deterministic `PalmImageAnalysisEngine` processes the image
  first, outputting structured, immutable `PalmFinding` and `PalmistryEvidence` objects.
- **Language Grounding**: The SLM receives **only** the approved structured observations and
  verified Samudrika Shastra traditional meanings.

---

## 2. Grounding Flow & Provenance

```
Palm Findings (Measured Line & Shape Data)
       │
       ▼
PalmistryFeatureDataConnector
       │  (Builds EvidenceGraph & AiEvidence nodes)
       ▼
DataSufficiencyValidator
       │  (Verifies sufficient quality & observations exist)
       ▼
AiContextBuilder
       │  (Constrains context strictly to approved evidence)
       ▼
PalmistryExplanationRequest
       │  (readingId, hand, language, evidence, approvedMeaning)
       ▼
GroundedSlmPalmistryExplanationEngine
   ├──> LocalNativeInferenceEngine (On-Device SLM)
   │       └──> AiOutputValidator (Checks for medical claims/fatalism)
   └──> DeterministicPalmistryExplanationEngine (Fallback on error/timeout)
       │
       ▼
PalmistryExplanationResult (Summary, Reflection, Themes, Provenance)
```

---

## 3. Strict Prohibitions on AI Authority

The AI layer is strictly an **interpretive and reflective synthesizer**:

1. **No Evidence Mutation**: AI cannot create palm lines, alter detected coordinates, or modify
   measurement confidence.
2. **No Hallucinated Sources**: AI cannot invent ancient texts or quote non-existent scriptures.
3. **No Direct Database Access**: AI engine has no DAO, Room, or filesystem handle.
4. **No Model Weight Modification**: User ratings and feedback generate immutable local telemetry (
   `AiImprovementSignal`). Model weights remain permanently immutable on-device.

---

## 4. Grounded Follow-up Question Flow (`PalmQuestionEngine`)

Users can ask interactive questions regarding their reading ("Ask about this reading"):

- **Evidence-Filtered Scope**: If a user asks "What does my fate line say?" and the fate line was
  `NOT_DETECTED`, the engine immediately returns an honest `INSUFFICIENT_EVIDENCE` status:
  > *"More information is needed. The fate line was faint or not detected in this image. Recapture
  with improved lighting or ask about a detected feature."*
- **No Hallucinated Cards or Lines**: Unlike Tarot which can draw a clarification card, Palmistry
  cannot invent new anatomical features.
