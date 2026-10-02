#!/usr/bin/env python3
"""Validate curated AYNVORA grounded SFT JSONL; performs no model training."""
import argparse
import json
import re
import sys


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("dataset")
    parser.add_argument("--sources", required=True)
    args = parser.parse_args()
    try:
        with open(args.sources, encoding="utf-8") as stream:
            source_doc = json.load(stream)
        approved = set(source_doc.get("sources", []))
        count = 0
        with open(args.dataset, encoding="utf-8") as stream:
            for line_number, line in enumerate(stream, 1):
                if not line.strip():
                    continue
                try:
                    item = json.loads(line)
                except json.JSONDecodeError as error:
                    raise ValueError(f"line {line_number}: invalid JSON: {error.msg}") from error
                messages, metadata = item.get("messages"), item.get("metadata", {})
                if not isinstance(messages, list) or len(messages) < 3:
                    raise ValueError(f"line {line_number}: messages must contain at least three turns")
                if metadata.get("verified") is not True:
                    raise ValueError(f"line {line_number}: example is not verified")
                if not metadata.get("tradition") or not metadata.get("feature"):
                    raise ValueError(f"line {line_number}: tradition and feature are required")
                refs = metadata.get("sources")
                if not isinstance(refs, list) or not refs or not set(refs).issubset(approved):
                    raise ValueError(f"line {line_number}: every source must appear in the approved source registry")
                for message in messages:
                    if message.get("role") not in {"system", "user", "tool", "assistant"} or not isinstance(message.get("content"), str):
                        raise ValueError(f"line {line_number}: malformed message")
                count += 1
        print(f"Validated {count} grounded examples; this did not train a model.")
        return 0
    except (OSError, ValueError, json.JSONDecodeError) as error:
        print(error, file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
