# AYNVORA Palm Analysis & Computer Vision Engine

Version: 1.0 (Phase 8.10)  
Status: Production Implemented  
Engine: `PalmImageAnalysisEngine`

---

## 1. Real Analysis Mandate (Zero Simulation)

A core tenet of Phase 8.10 is that **no fake coordinates, random measurements, or simulated lines
may ever be generated**.

- If an image has insufficient contrast or blur, the engine returns an honest failure state.
- If a line is physically absent or below edge detection thresholds, it is classified as
  `NOT_DETECTED`.
- If an anatomical feature requires sensors or advanced neural models not currently bundled, it is
  marked `UNSUPPORTED`.

---

## 2. Capabilities Registry (`PalmistryAnalysisCapabilities`)

The engine formally reports its supported and unsupported capabilities:

| Feature Category              | Capability ID       | Status          | Implementation Details                                                        |
|-------------------------------|---------------------|-----------------|-------------------------------------------------------------------------------|
| Hand & Palm Detection         | `HAND_DETECTION`    | **SUPPORTED**   | Pixel bounds, contrast ratio, skin tone variance.                             |
| Palm Segmentation             | `PALM_SEGMENTATION` | **SUPPORTED**   | Coordinate grid mapping across 4 primary palm quadrants.                      |
| Major Line: Life Line         | `LIFE_LINE`         | **SUPPORTED**   | Radial thenar crease detection and curvature extraction.                      |
| Major Line: Head Line         | `HEAD_LINE`         | **SUPPORTED**   | Transverse thenar crease tracking from index mount across palm.               |
| Major Line: Heart Line        | `HEART_LINE`        | **SUPPORTED**   | Distal transverse crease tracking under digits 2-5.                           |
| Major Line: Fate Line         | `FATE_LINE`         | **SUPPORTED**   | Vertical median line tracking from carpal crease to Saturn mount.             |
| Palm Geometry                 | `PALM_SHAPE`        | **SUPPORTED**   | Aspect ratio of detected palm area (`LONG`, `WIDE`, `SQUARE`, `RECTANGULAR`). |
| Minor Lines: Sun Line         | `SUN_LINE`          | **UNSUPPORTED** | Explicitly flagged as unsupported in v1.0.                                    |
| Minor Lines: Mercury Line     | `MERCURY_LINE`      | **UNSUPPORTED** | Explicitly flagged as unsupported in v1.0.                                    |
| Minor Lines: Mars Line        | `MARS_LINE`         | **UNSUPPORTED** | Explicitly flagged as unsupported in v1.0.                                    |
| Mounts: Jupiter, Saturn, etc. | `MOUNTS`            | **UNSUPPORTED** | Requires 3D photometric depth; flagged as unsupported.                        |
| Digits & Phalanges            | `FINGER_ANALYSIS`   | **UNSUPPORTED** | Phalange measurement requires 21-point hand landmarking.                      |

---

## 3. Image Quality Assessment Algorithm

Before any line detection is attempted, the image is evaluated via
`PalmImageAnalysisEngine.evaluateQuality()`:

```
Raw Image Bytes
       │
       ▼
1. Dimension & Aspect Check (Width >= 200, Height >= 200, Ratio between 0.3 and 3.0)
       │ (Fail -> LOW_RESOLUTION / WRONG_ORIENTATION)
       ▼
2. Luminance Calculation (Mean sample intensity in [0, 255])
       │ (Intensity < 35 -> TOO_DARK, Intensity > 245 -> TOO_BRIGHT)
       ▼
3. Sharpness / Blur Metric (Laplacian kernel edge variance across 20x20 sample grid)
       │ (Variance < 8.0 -> BLURRY)
       ▼
4. Skin Contrast & Hand Boundary Verification
       │ (Zero hand boundary -> HAND_NOT_DETECTED)
       ▼
Quality State: GOOD
```

---

## 4. Palm Line Detection & Coordinate Measurement

For images passing quality evaluation, the engine conducts region-specific gradient scanning:

1. **Life Line (`LIFE_LINE`)**:
    - Region: `THENAR` (Radial arc surrounding the ball of the thumb).
    - Metrics: Evaluates continuity, arc curvature, and radial depth.
2. **Head Line (`HEAD_LINE`)**:
    - Region: `CENTRAL_PALM` (Middle traverse crossing towards hypothenar).
    - Metrics: Measures slope angle (horizontal vs sloping), relative span, and continuity.
3. **Heart Line (`HEART_LINE`)**:
    - Region: `INTERDIGITAL` (Upper traverse below knuckle pads).
    - Metrics: Evaluates termination point (between Jupiter/Saturn mounts) and curve upward.
4. **Fate Line (`FATE_LINE`)**:
    - Region: `HYPOTHENAR` to `CENTRAL_PALM` (Vertical central ascent).
    - Metrics: Evaluates continuity and strength. Often marked `NOT_DETECTED` in faint palms.

### Confidence Definition

Confidence represents **pure algorithmic confidence in image feature detection** (gradient strength
and edge continuity). It is **never** a probability of future life events.
