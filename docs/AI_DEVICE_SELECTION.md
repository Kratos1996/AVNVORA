# AYNVORA AI Device-Adaptive Selection Matrix

## 1. Zero-Configuration Philosophy

The user is never burdened with choosing models, quantizations, or parameter counts. The user-facing
experience is strictly **"Download AYNVORA AI"**.

AYNVORA's internal `AiModelSelector` and `AiDeviceCapabilityDetector` evaluate hardware capabilities
deterministically and pick the optimal model variant.

## 2. Hardware Profiles & Selection Matrix

| Device Profile Tier   | Hardware Criteria (RAM & Storage)                    | Recommended Variant            | Quantization | Disk Footprint | Expected Context       |
|:----------------------|:-----------------------------------------------------|:-------------------------------|:-------------|:---------------|:-----------------------|
| **HIGH_RESOURCE**     | $\ge 8\text{ GB}$ RAM, $\ge 4\text{ GB}$ Free Disk   | `qwen2.5-1.5b-instruct-q5_k_m` | Q5_K_M       | 1.28 GB        | 2048                   |
| **BALANCED**          | $\ge 6\text{ GB}$ RAM, $\ge 3\text{ GB}$ Free Disk   | `qwen2.5-1.5b-instruct-q4_k_m` | Q4_K_M       | 1.11 GB        | 2048                   |
| **LOW_RESOURCE**      | $\ge 4\text{ GB}$ RAM, $\ge 2\text{ GB}$ Free Disk   | `qwen2.5-0.5b-instruct-q5_k_m` | Q5_K_M       | 522 MB         | 2048                   |
| **VERY_LOW_RESOURCE** | $\ge 3\text{ GB}$ RAM, $\ge 1.5\text{ GB}$ Free Disk | `qwen2.5-0.5b-instruct-q4_k_m` | Q4_K_M       | 491 MB         | 2048                   |
| **UNSUPPORTED**       | $< 3\text{ GB}$ RAM or $< 1.5\text{ GB}$ Free Disk   | *None*                         | *None*       | 0 MB           | Deterministic Fallback |

## 3. UI Boundary & Diagnostic Screen

- **Consumer UI**: `AiSetupCard` displays only:
    - `"AI optimized for your device"`
    - `"100% Private & On-Device"`
    - High-level status: Ready / Downloading / Not Installed.
- **Developer Diagnostics**: Accessible via `AiDiagnosticScreen` (hidden from normal consumer
  navigation). Exposes full hardware specs, candidate evaluation rankings, and latency metrics for
  debugging.
