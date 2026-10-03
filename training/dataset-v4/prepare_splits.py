#!/usr/bin/env python3
"""
Prepares deterministic splits (train.jsonl, valid.jsonl, test.jsonl) from dataset-v4/verified_sft.jsonl,
ensures zero leakage with holdout_validation_v5.jsonl, and calculates cryptographic SHA-256 checksums.
"""

import hashlib
import json
import random
import sys
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent
SFT_FILE = BASE_DIR / "verified_sft.jsonl"
HOLDOUT_FILE = BASE_DIR / "holdout_validation_v5.jsonl"
SOURCES_FILE = BASE_DIR.parent / "verified_sources.json"

TRAIN_FILE = BASE_DIR / "train.jsonl"
VALID_FILE = BASE_DIR / "valid.jsonl"
TEST_FILE = BASE_DIR / "test.jsonl"
MANIFEST_FILE = BASE_DIR / "dataset_manifest.json"

def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with open(path, "rb") as f:
        while chunk := f.read(65536):
            h.update(chunk)
    return h.hexdigest()

def main():
    with open(SOURCES_FILE, encoding="utf-8") as f:
        approved_sources = set(json.load(f).get("sources", []))

    # 1. Load holdout questions
    holdout_questions = set()
    with open(HOLDOUT_FILE, encoding="utf-8") as f:
        for line in f:
            if line.strip():
                item = json.loads(line)
                holdout_questions.add(item["question"].strip().lower())
    print(f"Loaded {len(holdout_questions)} holdout questions from {HOLDOUT_FILE.name}")

    # 2. Audit and load verified SFT examples
    sft_examples = []
    leakage_count = 0
    with open(SFT_FILE, encoding="utf-8") as f:
        for idx, line in enumerate(f, 1):
            if not line.strip():
                continue
            item = json.loads(line)
            messages = item.get("messages", [])
            metadata = item.get("metadata", {})

            # Extract user question
            user_msg = next((m for m in messages if m.get("role") == "user"), None)
            tool_msg = next((m for m in messages if m.get("role") == "tool"), None)
            asst_msg = next((m for m in messages if m.get("role") == "assistant"), None)

            if not user_msg or not tool_msg or not asst_msg:
                raise ValueError(f"Line {idx}: missing user, tool, or assistant turn")

            question = user_msg["content"].strip()
            if question.lower() in holdout_questions:
                print(f"LEAKAGE DETECTED at line {idx}: {question}")
                leakage_count += 1

            # Verify sources
            sources = metadata.get("sources", [])
            if not sources or not set(sources).issubset(approved_sources):
                raise ValueError(f"Line {idx}: unapproved sources {sources}")

            sft_examples.append(item)

    if leakage_count > 0:
        raise ValueError(f"CRITICAL: Found {leakage_count} leaked questions in training data!")

    print(f"Audited {len(sft_examples)} SFT examples. 0% leakage detected against holdout.")

    # 3. Deterministic split
    rng = random.Random(42)
    shuffled = list(sft_examples)
    rng.shuffle(shuffled)

    total = len(shuffled)
    train_count = int(total * 0.80)  # 406
    valid_count = int(total * 0.10)  # 50 or 51
    test_count = total - train_count - valid_count

    train_data = shuffled[:train_count]
    valid_data = shuffled[train_count:train_count + valid_count]
    test_data = shuffled[train_count + valid_count:]

    print(f"Split counts: train={len(train_data)}, valid={len(valid_data)}, test={len(test_data)}, holdout={len(holdout_questions)}")

    # 4. Write split files
    for path, data in [(TRAIN_FILE, train_data), (VALID_FILE, valid_data), (TEST_FILE, test_data)]:
        with open(path, "w", encoding="utf-8") as f:
            for item in data:
                f.write(json.dumps(item, ensure_ascii=False) + "\n")

    # 5. Compute SHA-256
    hashes = {
        "datasetVersion": "v4",
        "splitSeed": 42,
        "counts": {
            "total_verified_sft": total,
            "train": len(train_data),
            "valid": len(valid_data),
            "test": len(test_data),
            "holdout_v5": len(holdout_questions)
        },
        "sha256": {
            "verified_sft.jsonl": sha256_file(SFT_FILE),
            "train.jsonl": sha256_file(TRAIN_FILE),
            "valid.jsonl": sha256_file(VALID_FILE),
            "test.jsonl": sha256_file(TEST_FILE),
            "holdout_validation_v5.jsonl": sha256_file(HOLDOUT_FILE)
        }
    }

    with open(MANIFEST_FILE, "w", encoding="utf-8") as f:
        json.dump(hashes, f, indent=2)

    print("Dataset manifest created:")
    print(json.dumps(hashes, indent=2))

if __name__ == "__main__":
    main()
