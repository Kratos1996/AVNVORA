# Optional grounded SFT preparation

Status: **TRAINING_PREPARED**. `dataset-v1/verified_sft.jsonl` contains eight examples generated from the source-verified rules in `TAJIKA_V1`; this is a narrow source-grounding dataset, not coverage of all requested categories. `dataset-v1/generate_tajika_examples.py` rebuilds it and updates `verified_sources.json`. The dataset validator passes. No AYNVORA SFT job, selected base model, training run, or evaluation artifact exists. The vendored generic llama.cpp training utilities do not make this an AYNVORA fine-tuning pipeline. No model is described as fine-tuned.

Keep examples separate from knowledge packs. Generated examples are sourced from structured pack rules and retain source page, rule ID, pack version, and calculation profile. The source transcription is marked unproofread by Wikisource but cross-checked against scan images; dataset coverage and independent editorial review remain limited. Do not synthesize examples from research-only Internet sources.

Run `python3 training/dataset-v1/generate_tajika_examples.py` to regenerate the scoped dataset, then `python3 training/validate_dataset.py training/dataset-v1/verified_sft.jsonl --sources training/verified_sources.json`. No training run is started by these scripts.

Before training, record the exact base model/revision, dataset version and SHA-256, example count, method, hardware, duration, output artifact checksum, and evaluation results. Preserve deterministic numbers and dates from tool output and include source uncertainty in target answers.
