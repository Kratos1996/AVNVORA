# AYNVORA Palmistry Architecture & Clean Boundaries

Version: 1.0 (Phase 8.10)  
Status: Production Implemented  
Feature ID: `PALMISTRY`

---

## 1. Clean Architecture Stack

The Palmistry feature adheres strictly to AYNVORA's Clean Architecture standards:

```
Presentation Layer (:ui)
       │  (Compose Multiplatform, PalmistryRoute, PalmistryState)
       ▼
Application / Orchestration (:aynvora-core)
       │  (MultiFeatureOrchestrator, PalmQuestionEngine, PalmistryReportGenerator)
       ▼
Domain Layer (:aynvora-core)
       │  (PalmistryModels, PalmImageAnalysisEngine, PalmistryMeaning,
       │   PalmistryFeatureDataConnector, GroundedSlmPalmistryExplanationEngine)
       ▲
Data Layer (:aynvora-data)
          (PalmSessionRepositoryImpl, DriverPalmSessionStorage, StorageDriver)
```

### Module & Dependency Rules

1. **Domain Isolation (`:aynvora-core`)**:
    - Zero dependencies on Android SDK, iOS SDK, Jetpack Compose, Room DAOs, CameraX, or native
      image classes (`Bitmap`, `UIImage`).
    - Pure Kotlin Multiplatform definitions for `PalmImageSource`, `PalmFinding`,
      `PalmistryEvidence`, and `PalmReadingSession`.
2. **Presentation (`:ui`)**:
    - Consumes domain interfaces (`PalmImageAnalysisEngine`, `PalmSessionRepository`,
      `PalmQuestionEngine`).
    - Delegates state management to single-direction UDF (`PalmistryState`).
3. **Data (`:aynvora-data`)**:
    - Implements `PalmSessionRepository` and `PalmSessionStorage` via thread-safe mutex and KMP
      filesystem abstractions.
    - Raw image bytes are **never** persisted in database rows or entities.

---

## 2. Component Directory & Ownership

| Component                     | Package / Path                                                        | Responsibility                                                                   |
|-------------------------------|-----------------------------------------------------------------------|----------------------------------------------------------------------------------|
| Domain Models                 | `com.aynvora.core.palmistry.PalmistryModels.kt`                       | Immutable models for lines, findings, sessions, quality states, and timeline.    |
| AI Request/Response Contracts | `com.aynvora.core.palmistry.PalmistryAiModels.kt`                     | Typed contracts for grounded SLM inference and explanation synthesis.            |
| Deterministic Vision Engine   | `com.aynvora.core.palmistry.PalmImageAnalysisEngine.kt`               | Image decoding, quality assessment, gradient analysis, and feature detection.    |
| Samudrika Content Package     | `com.aynvora.core.palmistry.PalmistryContentPackage.kt`               | Authoritative classical texts in English and Hindi for supported palm lines.     |
| Evidence & Graph Connector    | `com.aynvora.core.palmistry.PalmistryFeatureDataConnector.kt`         | Converts palm findings into `AiEvidence` nodes preserving source provenance.     |
| Grounded SLM Engine           | `com.aynvora.core.palmistry.GroundedSlmPalmistryExplanationEngine.kt` | Orchestrates on-device SLM with deterministic fallback safeguards.               |
| Q&A Engine                    | `com.aynvora.core.palmistry.PalmQuestionEngine.kt`                    | Evidence-bounded follow-up questions with honest `INSUFFICIENT_EVIDENCE` checks. |
| Report Generator              | `com.aynvora.core.report.PalmistryReportGenerator.kt`                 | Generates standardized `ReportDocument` for viewer and PDF generator.            |
| Repository & Storage          | `com.aynvora.data.palmistry.*`                                        | Thread-safe persistence of reading metadata and timeline events.                 |
| Compose Presentation          | `com.aynvora.ui.palmistry.PalmistryRoute.kt`                          | 10-screen complete UI flow with top-right language switcher on every screen.     |

---

## 3. Dependency Injection Wiring (Koin KMP)

All components are registered cleanly in Koin without manual singletons:

- **Core Domain (`CoreDomainModule.kt`)**:
    - `single { PalmImageAnalysisEngine() }`
    - `single { PalmistryFeatureDataConnector() }`
    -
    `single<PalmistryExplanationEngine> { GroundedSlmPalmistryExplanationEngine(get(), get(), get()) }`
    - `single { PalmQuestionEngine(get(), get()) }`
- **Data Layer (`CoreDataModule.kt`)**:
    - `single<PalmSessionStorage> { DriverPalmSessionStorage(get()) }`
    - `single<PalmSessionRepository> { PalmSessionRepositoryImpl(get()) }`
    - `single<PalmistryRepository> { get<PalmSessionRepository>() }`
- **Presentation (`UiModule.kt` / `AynvoraAppModules.kt`)**:
    - Automatically resolves repositories and engines directly into Compose routes.
