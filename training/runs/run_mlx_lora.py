#!/usr/bin/env python3
"""
Official MLX-LM LoRA SFT Fine-Tuning Execution Script for AYNVORA (Dataset v4).
Designed for Apple Silicon (arm64, macOS 14+ with unified memory and Metal 3).
"""

import os
import sys
import subprocess
from pathlib import Path

RUN_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = RUN_DIR.parents[1]
DATASET_DIR = PROJECT_ROOT / "training" / "dataset-v4"

def verify_environment():
    import platform
    machine = platform.machine()
    if machine != "arm64":
        print(f"ERROR: Host architecture is {machine}. MLX strictly requires Apple Silicon (arm64).", file=sys.stderr)
        return False
    try:
        import mlx.core as mx
        import mlx_lm
        print(f"MLX verified. Device: {mx.default_device()}")
        return True
    except ImportError as e:
        print(f"ERROR: MLX / MLX-LM not installed: {e}", file=sys.stderr)
        return False

def main():
    print("=== AYNVORA Apple Silicon MLX LoRA Fine-Tuning Launcher ===")
    if not verify_environment():
        print("Pre-flight check failed. Halting to avoid invalid execution.")
        sys.exit(1)

    cmd = [
        sys.executable, "-m", "mlx_lm.lora",
        "--model", "Qwen/Qwen2.5-1.5B-Instruct",
        "--train",
        "--data", str(DATASET_DIR),
        "--batch-size", "4",
        "--lora-layers", "16",
        "--lora-parameters", '{"rank": 16, "alpha": 32, "dropout": 0.05}',
        "--learning-rate", "1e-4",
        "--iters", "600",
        "--val-batches", "25",
        "--steps-per-eval", "50",
        "--save-every", "100",
        "--adapter-path", str(RUN_DIR / "adapters"),
        "--seed", "42"
    ]

    print(f"Executing: {' '.join(cmd)}")
    result = subprocess.run(cmd)
    sys.exit(result.returncode)

if __name__ == "__main__":
    main()
