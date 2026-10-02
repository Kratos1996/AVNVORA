#!/usr/bin/env python3
"""Generate scoped grounded examples from the verified TAJIKA_V1 pack; this does not train a model."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PACK = ROOT / "knowledge/tajika/TAJIKA_V1.json"
OUT = Path(__file__).with_name("verified_sft.jsonl")
SOURCE_REGISTRY = ROOT / "training/verified_sources.json"


def main():
    pack = json.loads(PACK.read_text(encoding="utf-8"))
    source_ids = sorted({rule["sourceId"] for rule in pack["rules"]})
    SOURCE_REGISTRY.write_text(json.dumps({"sources": source_ids}, indent=2) + "\n", encoding="utf-8")
    examples = []
    for rule in pack["rules"]:
        ref = rule["sourceRefs"][0]
        user = f"According to the cited Tajika source, what does rule {rule['ruleId']} indicate?"
        tool = {
            "tool": "getMuntha" if "MUNTHA" in rule["ruleId"] else "getVarsheshaSun",
            "ruleId": rule["ruleId"],
            "result": rule["meaning"],
            "sourceRef": ref,
            "knowledgeVersion": pack["version"],
            "calculationProfile": "EXPLICIT_SOURCE_RULE_ONLY",
        }
        answer = f"{rule['meaning']} This is a traditional indication attributed to the cited source, not a factual prediction. Source: {ref}"
        examples.append({
            "messages": [
                {"role": "system", "content": "Use only supplied source evidence. Attribute traditional indications, avoid certainty, and do not infer missing chart calculations."},
                {"role": "user", "content": user},
                {"role": "tool", "content": json.dumps(tool, ensure_ascii=False, sort_keys=True)},
                {"role": "assistant", "content": answer},
            ],
            "metadata": {
                "tradition": "TAJIKA", "feature": rule["topic"], "sources": [rule["sourceId"]],
                "verified": True, "datasetVersion": "TAJIKA_SFT_V1", "ruleId": rule["ruleId"],
                "knowledgeVersion": pack["version"], "calculationProfile": "EXPLICIT_SOURCE_RULE_ONLY",
                "categories": ["SOURCE_GROUNDING", "EVIDENCE_CITATION", "ENGLISH", "UNCERTAINTY", "VARSHAPHAL" if rule["topic"] == "Varshesha" else "MUNTHA"],
                "sourceRef": ref,
            },
        })
    OUT.write_text("".join(json.dumps(item, ensure_ascii=False, sort_keys=True) + "\n" for item in examples), encoding="utf-8")
    print(f"Generated {len(examples)} examples from {pack['packId']} at {OUT}")


if __name__ == "__main__":
    main()
