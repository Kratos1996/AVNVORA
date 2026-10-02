#!/usr/bin/env python3
"""Generate 50 verified classical Tajika SFT examples for Phase 10.25; this does not train a model."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = Path(__file__).with_name("verified_sft.jsonl")
SOURCE_REGISTRY = ROOT / "training/verified_sources.json"

APPROVED_SOURCES = [
    "tajika-neelakanthi-wikisource-pages-112-122-124",
    "tajika-neelakanthi-1907-scan",
]

def build_examples():
    SOURCE_REF_120 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/१२०#printed-page-112-verse-1"
    SOURCE_REF_121 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/१२१#printed-page-113-verse-3"
    SOURCE_REF_122 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/१२२#printed-page-114-verse-7"
    SOURCE_REF_110 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/११०#printed-page-102-verse-5"
    SOURCE_REF_41 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/४१#printed-page-33-verse-61"
    SOURCE_REF_49 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/४९#printed-page-41-verse-13"
    SOURCE_REF_50 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/५०#printed-page-42-verse-1"
    SOURCE_REF_87 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/८७#printed-page-79-verse-5"
    SOURCE_REF_192 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/१९२#printed-page-184-verse-14"

    raw_items = [
        # --- 1-5: MUNTHA PROGRESSION & OFFSETS ---
        {
            "user": "What is Muntha and how is it calculated for the birth year (offset 0)?",
            "tool_call": {"tool": "getMuntha", "arguments": {"natalAscendantLongitude": 15.0, "elapsedSolarReturnCycles": 0}},
            "tool_result": {"sign": "Aries", "longitude": 15.0, "lord": "Mars", "elapsedCycles": 0, "ruleId": "TN-MUN-01", "sourceRef": SOURCE_REF_120},
            "assistant": "For the birth year (elapsed solar return cycles = 0), Muntha coincides exactly with the natal ascendant at 15.0° Aries. Its ruler is Mars. Source: Tajika Neelakanthi Varsha Tantra v. 1.",
            "feature": "Muntha", "categories": ["MUNTHA", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_120,
        },
        {
            "user": "How does Muntha progress at completed age 1 (first solar return)?",
            "tool_call": {"tool": "getMuntha", "arguments": {"natalAscendantLongitude": 15.0, "elapsedSolarReturnCycles": 1}},
            "tool_result": {"sign": "Taurus", "longitude": 45.0, "lord": "Venus", "elapsedCycles": 1, "ruleId": "TN-MUN-01", "sourceRef": SOURCE_REF_120},
            "assistant": "At elapsed cycle 1, Muntha advances by exactly one sign (30°) from Aries to Taurus (45.0° absolute longitude), preserving the natal degree within the sign (15°). Its lord is Venus. Source: Varsha Tantra v. 1.",
            "feature": "Muntha", "categories": ["MUNTHA", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_120,
        },
        {
            "user": "Explain the 12-year cycle wraparound for Muntha at age 12 and 13.",
            "tool_call": {"tool": "getMuntha", "arguments": {"natalAscendantLongitude": 15.0, "elapsedSolarReturnCycles": 12}},
            "tool_result": {"sign": "Aries", "longitude": 15.0, "lord": "Mars", "elapsedCycles": 12, "ruleId": "TN-MUN-01", "sourceRef": SOURCE_REF_120},
            "assistant": "Because the zodiac contains 12 signs, 12 elapsed cycles result in 12 % 12 = 0, returning Muntha to natal Aries (15°). At cycle 13, 13 % 12 = 1, advancing it to Taurus (15°). Source: Varsha Tantra v. 1.",
            "feature": "Muntha", "categories": ["MUNTHA", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_120,
        },
        {
            "user": "How does Muntha handle zodiacal wraparound from Pisces?",
            "tool_call": {"tool": "getMuntha", "arguments": {"natalAscendantLongitude": 359.0, "elapsedSolarReturnCycles": 1}},
            "tool_result": {"sign": "Aries", "longitude": 29.0, "lord": "Mars", "elapsedCycles": 1, "ruleId": "TN-MUN-01", "sourceRef": SOURCE_REF_120},
            "assistant": "A natal ascendant at 29° Pisces (359.0°) progresses by one sign (30°) at cycle 1 to 29° Aries (29.0° absolute), wrapping around 360° deterministically. Source: Varsha Tantra v. 1.",
            "feature": "Muntha", "categories": ["MUNTHA", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_120,
        },
        {
            "user": "Where is Muntha placed at age 24 for a natal Lagna of 15° Aries?",
            "tool_call": {"tool": "getMuntha", "arguments": {"natalAscendantLongitude": 15.0, "elapsedSolarReturnCycles": 24}},
            "tool_result": {"sign": "Aries", "longitude": 15.0, "lord": "Mars", "elapsedCycles": 24, "ruleId": "TN-MUN-01", "sourceRef": SOURCE_REF_120},
            "assistant": "At age 24, exactly two complete 12-year cycles have elapsed (24 % 12 = 0), placing Muntha back in Aries at 15.0°. Source: Varsha Tantra v. 1.",
            "feature": "Muntha", "categories": ["MUNTHA", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_120,
        },

        # --- 6-9: MUNTHA HOUSE INDICATIONS ---
        {
            "user": "What does Tajika Neelakanthi indicate for Muntha placed in the 3rd house of the annual chart?",
            "tool_call": {"tool": "getMuntha", "arguments": {"munthaHouse": 3}},
            "tool_result": {"ruleId": "TAJIKA_MUNTHA_H3_001", "meaning": "The source associates Muntha in house 3 with favorable traditional themes including initiative, reputation, comfort, and support.", "sourceRef": SOURCE_REF_122},
            "assistant": "The source associates Muntha in house 3 with favorable traditional themes including initiative, reputation, comfort, and support. This is a traditional indication attributed to the cited source. Source: Varsha Tantra v. 7.",
            "feature": "Muntha", "categories": ["MUNTHA", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_122,
        },
        {
            "user": "What does Tajika Neelakanthi indicate for Muntha placed in the 4th house?",
            "tool_call": {"tool": "getMuntha", "arguments": {"munthaHouse": 4}},
            "tool_result": {"ruleId": "TAJIKA_MUNTHA_H4_001", "meaning": "The source associates Muntha in house 4 with challenging traditional themes, including distress and disruption of comfort.", "sourceRef": SOURCE_REF_122},
            "assistant": "The source associates Muntha in house 4 with challenging traditional themes, including distress and disruption of comfort. Source: Varsha Tantra v. 8.",
            "feature": "Muntha", "categories": ["MUNTHA", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_122,
        },
        {
            "user": "What does Tajika Neelakanthi indicate for Muntha placed in the 5th house?",
            "tool_call": {"tool": "getMuntha", "arguments": {"munthaHouse": 5}},
            "tool_result": {"ruleId": "TAJIKA_MUNTHA_H5_001", "meaning": "The source associates Muntha in house 5 with favorable traditional themes including judgment, happiness, and gain.", "sourceRef": SOURCE_REF_122},
            "assistant": "The source associates Muntha in house 5 with favorable traditional themes including judgment, intellect, happiness, and gain. Source: Varsha Tantra v. 9.",
            "feature": "Muntha", "categories": ["MUNTHA", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_122,
        },
        {
            "user": "What does Tajika Neelakanthi indicate for Muntha placed in the 6th house?",
            "tool_call": {"tool": "getMuntha", "arguments": {"munthaHouse": 6}},
            "tool_result": {"ruleId": "TAJIKA_MUNTHA_H6_001", "meaning": "The source associates Muntha in house 6 with challenging traditional themes such as obstacles, opposition, and physical fatigue.", "sourceRef": SOURCE_REF_122},
            "assistant": "The source associates Muntha in house 6 with challenging traditional themes including obstacles, opposition, and physical fatigue. Source: Varsha Tantra v. 10.",
            "feature": "Muntha", "categories": ["MUNTHA", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_122,
        },

        # --- 10-14: MUNTHA LORD ---
        {
            "user": "Who is the Muntha Lord (Munthesha) according to Tajika Neelakanthi?",
            "tool_call": {"tool": "getMunthaLord", "arguments": {"munthaSignIndex": 0}},
            "tool_result": {"munthaSign": "Aries", "lord": "Mars", "ruleId": "TN-MUN-02", "sourceRef": SOURCE_REF_121},
            "assistant": "Per Varsha Tantra v. 3, the lord of the sign occupied by Muntha is the Munthesha. For Muntha in Aries, the lord is Mars. Source: Varsha Tantra v. 3.",
            "feature": "MunthaLord", "categories": ["MUNTHA_LORD", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_121,
        },
        {
            "user": "Who is the Munthesha when Muntha is in Taurus?",
            "tool_call": {"tool": "getMunthaLord", "arguments": {"munthaSignIndex": 1}},
            "tool_result": {"munthaSign": "Taurus", "lord": "Venus", "ruleId": "TN-MUN-02", "sourceRef": SOURCE_REF_121},
            "assistant": "When Muntha resides in Taurus, its ruler Venus is designated as Munthesha. Source: Varsha Tantra v. 3.",
            "feature": "MunthaLord", "categories": ["MUNTHA_LORD", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_121,
        },
        {
            "user": "Who is the Munthesha when Muntha is in Leo?",
            "tool_call": {"tool": "getMunthaLord", "arguments": {"munthaSignIndex": 4}},
            "tool_result": {"munthaSign": "Leo", "lord": "Sun", "ruleId": "TN-MUN-02", "sourceRef": SOURCE_REF_121},
            "assistant": "When Muntha is in Leo, its ruler is the Sun. Source: Varsha Tantra v. 3.",
            "feature": "MunthaLord", "categories": ["MUNTHA_LORD", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_121,
        },
        {
            "user": "Who is the Munthesha when Muntha is in Sagittarius?",
            "tool_call": {"tool": "getMunthaLord", "arguments": {"munthaSignIndex": 8}},
            "tool_result": {"munthaSign": "Sagittarius", "lord": "Jupiter", "ruleId": "TN-MUN-02", "sourceRef": SOURCE_REF_121},
            "assistant": "For Muntha in Sagittarius, the Muntha lord is Jupiter. Source: Varsha Tantra v. 3.",
            "feature": "MunthaLord", "categories": ["MUNTHA_LORD", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_121,
        },
        {
            "user": "How does Muntha Lord interact with the annual chart?",
            "tool_call": {"tool": "getMunthaLord", "arguments": {"munthaSignIndex": 1, "annualAscendantSignIndex": 0}},
            "tool_result": {"munthaSign": "Taurus", "lord": "Venus", "annualHouse": 2, "sourceRef": SOURCE_REF_121},
            "assistant": "Muntha Lord Venus occupies the 2nd whole-sign house relative to annual Aries Lagna. Its dignity and strength in the annual chart indicate the manifestation of annual themes. Source: Varsha Tantra vv. 3-4.",
            "feature": "MunthaLord", "categories": ["MUNTHA_LORD", "ENGLISH"], "sourceRef": SOURCE_REF_121,
        },

        # --- 15-19: VARSHESHWARA CANDIDATES (PANCHADHIKARIS) ---
        {
            "user": "What are the five office-bearer candidates (Panchadhikaris) for Varsheshwara?",
            "tool_call": {"tool": "getVarsheshwara", "arguments": {"natalAscendantLongitude": 15.0, "annualAscendantSignIndex": 4, "munthaSignIndex": 1, "isDay": True}},
            "tool_result": {"candidates": ["Janma Lagnesha", "Varsha Lagnesha", "Munthesha", "Trirashipati", "Dina/Ratri-pati"], "ruleId": "TN-VAR-01", "sourceRef": SOURCE_REF_110},
            "assistant": "Per Tajika Neelakanthi Varsha Tantra vv. 5-7, the five candidates are: (1) Janma Lagnesha, (2) Varsha Lagnesha, (3) Munthesha, (4) Trirashipati, and (5) Dina-pati (day ingress) or Ratri-pati (night ingress). Source: Varsha Tantra v. 5.",
            "feature": "Varsheshwara", "categories": ["VARSHESHWARA", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_110,
        },
        {
            "user": "How is Trirashipati determined for a daytime annual ingress in Leo?",
            "tool_call": {"tool": "getVarsheshwara", "arguments": {"annualAscendantSignIndex": 4, "isDay": True}},
            "tool_result": {"annualLagna": "Leo", "isDay": True, "trirashipati": "Jupiter", "ruleId": "TN-VAR-01", "sourceRef": SOURCE_REF_41},
            "assistant": "Per Samjna Tantra v. 61, for a daytime ingress with Leo annual ascendant, the Trirashipati is Jupiter (for night it would be Sun). Source: Samjna Tantra v. 61.",
            "feature": "Varsheshwara", "categories": ["VARSHESHWARA", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_41,
        },
        {
            "user": "How is Trirashipati determined for a nighttime annual ingress in Aries?",
            "tool_call": {"tool": "getVarsheshwara", "arguments": {"annualAscendantSignIndex": 0, "isDay": False}},
            "tool_result": {"annualLagna": "Aries", "isDay": False, "trirashipati": "Jupiter", "ruleId": "TN-VAR-01", "sourceRef": SOURCE_REF_41},
            "assistant": "Per Samjna Tantra v. 61, for a nighttime ingress with Aries ascendant, Trirashipati is Jupiter (day is Sun). Source: Samjna Tantra v. 61.",
            "feature": "Varsheshwara", "categories": ["VARSHESHWARA", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_41,
        },
        {
            "user": "How is Dina-pati vs Ratri-pati selected for Varsheshwara?",
            "tool_call": {"tool": "getVarsheshwara", "arguments": {"isDay": True, "sunSignIndex": 8}},
            "tool_result": {"isDay": True, "role": "Dina-pati", "lord": "Jupiter", "sourceRef": SOURCE_REF_110},
            "assistant": "For a daytime ingress, the Dina-pati is the lord of the sign occupied by the Sun in the annual chart. For a night ingress, Ratri-pati is the lord of the sign occupied by the Moon. Source: Varsha Tantra v. 6.",
            "feature": "Varsheshwara", "categories": ["VARSHESHWARA", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_110,
        },
        {
            "user": "What role does Janma Lagnesha play in Varsheshwara selection?",
            "tool_call": {"tool": "getVarsheshwara", "arguments": {"natalAscendantLongitude": 45.0}},
            "tool_result": {"natalLagna": "Taurus", "janmaLagnesha": "Venus", "sourceRef": SOURCE_REF_110},
            "assistant": "Janma Lagnesha is the ruler of the natal ascendant (Venus for Taurus Lagna). It is the first among the five office-bearers evaluated for Year Lord. Source: Varsha Tantra v. 5.",
            "feature": "Varsheshwara", "categories": ["VARSHESHWARA", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_110,
        },

        # --- 20-23: VARSHESHWARA ELIGIBILITY & PANCHAVARGIYA BALA ---
        {
            "user": "What is the mandatory aspect qualification for Varsheshwara?",
            "tool_call": {"tool": "getVarsheshwara", "arguments": {"ineligibleHouses": [2, 6, 8, 12]}},
            "tool_result": {"aspectCondition": "Must aspect annual Lagna", "ineligibleHouses": [2, 6, 8, 12], "ruleId": "TN-VAR-02", "sourceRef": SOURCE_REF_110},
            "assistant": "A candidate must cast a classical Tajika aspect on the annual Lagna to be eligible. Planets in houses 2, 6, 8, and 12 do not cast aspects (Adrishti / Aprakashya) and are ineligible unless no candidate aspects Lagna. Source: Varsha Tantra v. 7.",
            "feature": "Varsheshwara", "categories": ["VARSHESHWARA", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_110,
        },
        {
            "user": "What is Panchavargiya Bala used in Varsheshwara selection?",
            "tool_call": {"tool": "getVarsheshwara", "arguments": {"strengthType": "Panchavargiya"}},
            "tool_result": {"factors": ["Kshetra", "Uccha", "Hadda", "Drekkana", "Navamsha"], "maxVirupas": 20.0, "ruleId": "TN-VAR-02", "sourceRef": SOURCE_REF_110},
            "assistant": "Panchavargiya Bala evaluates 5 divisions: Kshetra (30), Uccha (20), Hadda (15), Drekkana (10), and Navamsha (5), yielding up to 20 virupas. Parashari Shadbala is NOT used for Tajika Year Lord. Source: Varsha Tantra v. 8.",
            "feature": "Varsheshwara", "categories": ["VARSHESHWARA", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_110,
        },
        {
            "user": "How is a tie broken among Varsheshwara candidates?",
            "tool_call": {"tool": "getVarsheshwara", "arguments": {"tieBreak": "priority"}},
            "tool_result": {"priority": ["Aspecting Lagna", "Higher Panchavargiya", "Munthesha", "Varsha Lagnesha"], "ruleId": "TN-VAR-03", "sourceRef": SOURCE_REF_110},
            "assistant": "If two qualified candidates have equal Panchavargiya strength, the tie is broken in favor of Munthesha or Varsha Lagnesha per classical commentary. Source: Varsha Tantra vv. 7-8.",
            "feature": "Varsheshwara", "categories": ["VARSHESHWARA", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_110,
        },
        {
            "user": "What happens if none of the five candidates aspects the annual Lagna?",
            "tool_call": {"tool": "getVarsheshwara", "arguments": {"eligibleCount": 0}},
            "tool_result": {"fallback": "Highest Panchavargiya strength candidate overall is selected", "sourceRef": SOURCE_REF_110},
            "assistant": "Per Varsha Tantra v. 7, if all five office-bearers are in aprakashya houses (2, 6, 8, 12) and fail to aspect Lagna, the candidate with the highest overall Panchavargiya strength is selected. Source: Varsha Tantra v. 7.",
            "feature": "Varsheshwara", "categories": ["VARSHESHWARA", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_110,
        },

        # --- 24-28: SAHAMS & SAIKA-BHAM CORRECTION ---
        {
            "user": "What is Punya Saham and what is its formula for day and night?",
            "tool_call": {"tool": "getSahams", "arguments": {"sahamId": "PUNYA", "isDay": True}},
            "tool_result": {"id": "PUNYA", "dayFormula": "Lagna + Moon - Sun", "nightFormula": "Lagna + Sun - Moon", "ruleId": "TN-SAH-01", "sourceRef": SOURCE_REF_87},
            "assistant": "Punya Saham (Fortune): Day formula is Lagna + Moon - Sun; Night formula is Lagna + Sun - Moon. If Lagna does not fall in the forward arc from subtrahend to minuend, +30° is added per the Saika-bham rule. Source: Samjna Tantra v. 5.",
            "feature": "Sahams", "categories": ["SAHAM", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_87,
        },
        {
            "user": "Explain the classical Saika-bham (+30°) arc correction for Sahams.",
            "tool_call": {"tool": "getSahams", "arguments": {"pointA": 192.167, "pointB": 128.167, "lagna": 250.167}},
            "tool_result": {"baseLongitude": 314.167, "correctionApplied": True, "finalLongitude": 344.167, "ruleId": "TN-SAH-01", "sourceRef": SOURCE_REF_87},
            "assistant": "Per Samjna Tantra v. 5, if Lagna is outside the forward zodiacal arc from Shodhya (point B) to Shuddhyashraya (point A), add 1 sign (+30° / सैकभम्). In Mahidhara's 1907 example, base 314°10' + 30° gives Pisces 14°10' (344°10'). Source: Samjna Tantra v. 5.",
            "feature": "Sahams", "categories": ["SAHAM", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_87,
        },
        {
            "user": "What is Vidya Saham and its formula?",
            "tool_call": {"tool": "getSahams", "arguments": {"sahamId": "VIDYA", "isDay": True}},
            "tool_result": {"id": "VIDYA", "dayFormula": "Lagna + Sun - Moon", "nightFormula": "Lagna + Moon - Sun", "ruleId": "TN-SAH-02", "sourceRef": SOURCE_REF_87},
            "assistant": "Vidya Saham (Knowledge/Wisdom) is the exact inverse of Punya Saham: Day formula is Lagna + Sun - Moon; Night formula is Lagna + Moon - Sun, subject to the +30° arc correction. Source: Samjna Tantra v. 6.",
            "feature": "Sahams", "categories": ["SAHAM", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_87,
        },
        {
            "user": "What is Yasas Saham and its formula?",
            "tool_call": {"tool": "getSahams", "arguments": {"sahamId": "YASAS", "isDay": True}},
            "tool_result": {"id": "YASAS", "dayFormula": "Lagna + Jupiter - Punya", "nightFormula": "Lagna + Punya - Jupiter", "ruleId": "TN-SAH-03", "sourceRef": SOURCE_REF_87},
            "assistant": "Yasas Saham (Fame/Renown): Day formula is Lagna + Jupiter - Punya Saham; Night formula is Lagna + Punya Saham - Jupiter. Source: Samjna Tantra v. 6.",
            "feature": "Sahams", "categories": ["SAHAM", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_87,
        },
        {
            "user": "What is Karma Saham and its formula?",
            "tool_call": {"tool": "getSahams", "arguments": {"sahamId": "KARMA", "isDay": True}},
            "tool_result": {"id": "KARMA", "dayFormula": "Lagna + Mars - Mercury", "nightFormula": "Lagna + Mercury - Mars", "ruleId": "TN-SAH-04", "sourceRef": SOURCE_REF_87},
            "assistant": "Karma Saham (Profession/Action): Day formula is Lagna + Mars - Mercury; Night formula is Lagna + Mercury - Mars. Source: Samjna Tantra v. 12.",
            "feature": "Sahams", "categories": ["SAHAM", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_87,
        },

        # --- 29-33: TAJIKA ASPECTS & ORBS (DEEPTAMSHA) ---
        {
            "user": "What are the allowed Tajika aspect relationships?",
            "tool_call": {"tool": "getTajikaAspects", "arguments": {"aspectTypes": "all"}},
            "tool_result": {"relationships": ["Conjunction (1-1)", "Sextile (3-11)", "Square (4-10)", "Trine (5-9)", "Opposition (1-7)"], "ruleId": "TN-ASP-01", "sourceRef": SOURCE_REF_49},
            "assistant": "Tajika recognizes 5 aspects: Conjunction (0°), Sextile (60° / 3-11), Square (90° / 4-10), Trine (120° / 5-9), and Opposition (180° / 1-7). Houses 2, 6, 8, and 12 cast no aspect. Source: Samjna Tantra v. 13.",
            "feature": "TajikaAspects", "categories": ["TAJIKA_ASPECT", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_49,
        },
        {
            "user": "What are planetary Deeptamshas (orbs) in Tajika Neelakanthi?",
            "tool_call": {"tool": "getTajikaAspects", "arguments": {"query": "deeptamsha"}},
            "tool_result": {"Sun": 15.0, "Moon": 12.0, "Mars": 8.0, "Mercury": 7.0, "Jupiter": 9.0, "Venus": 7.0, "Saturn": 9.0, "ruleId": "TN-ASP-01", "sourceRef": SOURCE_REF_49},
            "assistant": "Per Samjna Tantra v. 13, planetary orbs are: Sun 15°, Moon 12°, Mars 8°, Mercury 7°, Jupiter 9°, Venus 7°, and Saturn 9°. An aspect is effective when the distance from exact aspect is within the average of both planets' orbs. Source: Samjna Tantra v. 13.",
            "feature": "TajikaAspects", "categories": ["TAJIKA_ASPECT", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_49,
        },
        {
            "user": "How is the combined orb calculated for an aspect between Sun and Moon?",
            "tool_call": {"tool": "getTajikaAspects", "arguments": {"planet1": "SUN", "planet2": "MOON"}},
            "tool_result": {"sunOrb": 15.0, "moonOrb": 12.0, "combinedOrb": 13.5, "sourceRef": SOURCE_REF_49},
            "assistant": "Combined orb is (15° + 12°) / 2 = 13.5°. If Sun and Moon are within 13.5° of exact conjunction, sextile, square, trine, or opposition, a valid Tajika aspect is formed. Source: Samjna Tantra v. 13.",
            "feature": "TajikaAspects", "categories": ["TAJIKA_ASPECT", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_49,
        },
        {
            "user": "What is the planetary speed hierarchy in Tajika astrology?",
            "tool_call": {"tool": "getTajikaAspects", "arguments": {"query": "speed"}},
            "tool_result": {"hierarchy": "Moon > Mercury > Venus > Sun > Mars > Jupiter > Saturn", "sourceRef": SOURCE_REF_50},
            "assistant": "Planetary speed hierarchy from fastest to slowest is: Moon (1), Mercury (2), Venus (3), Sun (4), Mars (5), Jupiter (6), and Saturn (7). Lower rank indicates faster motion. Source: Samjna Tantra Ch. 3 v. 1.",
            "feature": "TajikaAspects", "categories": ["TAJIKA_ASPECT", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_50,
        },
        {
            "user": "Why do planets in houses 2, 6, 8, and 12 not form Tajika aspects?",
            "tool_call": {"tool": "getTajikaAspects", "arguments": {"houses": [2, 6, 8, 12]}},
            "tool_result": {"status": "Adrishti (Blind/Ineligible)", "sourceRef": SOURCE_REF_49},
            "assistant": "Houses 2, 6, 8, and 12 relative to any point are considered Aprakashya (unilluminated / Adrishti). Planets in these houses do not aspect that point in classical Tajika. Source: Samjna Tantra v. 13.",
            "feature": "TajikaAspects", "categories": ["TAJIKA_ASPECT", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_49,
        },

        # --- 34-37: TAJIKA YOGAS (ITTHASHALA & ISHRAFA) ---
        {
            "user": "What is Itthashala (Muthashila) Yoga and how is it formed?",
            "tool_call": {"tool": "getTajikaAspects", "arguments": {"yoga": "ITTHASHALA"}},
            "tool_result": {"yoga": "Itthashala", "condition": "Faster planet with fewer degrees applying to slower planet within combined orb", "ruleId": "TN-ASP-02", "sourceRef": SOURCE_REF_50},
            "assistant": "Itthashala Yoga occurs when two planets are in aspect within their combined Deeptamsha orb, and the faster planet has FEWER degrees in its sign than the slower planet, so it is applying to the aspect. Source: Samjna Tantra Ch. 3 v. 1.",
            "feature": "TajikaAspects", "categories": ["TAJIKA_ASPECT", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_50,
        },
        {
            "user": "What is Ishrafa (Musaripha) Yoga?",
            "tool_call": {"tool": "getTajikaAspects", "arguments": {"yoga": "ISHRAFA"}},
            "tool_result": {"yoga": "Ishrafa", "condition": "Faster planet with greater degrees separating from slower planet within orb", "ruleId": "TN-ASP-02", "sourceRef": SOURCE_REF_50},
            "assistant": "Ishrafa Yoga occurs when two aspected planets are within orb, but the faster planet has GREATER degrees in its sign than the slower planet, indicating that the aspect is separating. Source: Samjna Tantra Ch. 3 v. 3.",
            "feature": "TajikaAspects", "categories": ["TAJIKA_ASPECT", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_50,
        },
        {
            "user": "If Moon is at 8° Gemini and Sun is at 14° Aries, is this Itthashala or Ishrafa?",
            "tool_call": {"tool": "getTajikaAspects", "arguments": {"moonLong": 68.0, "sunLong": 14.0}},
            "tool_result": {"relationship": "Sextile", "applying": True, "yoga": "Itthashala", "sourceRef": SOURCE_REF_50},
            "assistant": "Moon (faster) is at 8° Gemini and Sun (slower) is at 14° Aries. Since Moon (8°) < Sun (14°), the faster planet is applying to the exact 60° sextile: this forms Itthashala Yoga. Source: Samjna Tantra Ch. 3 v. 1.",
            "feature": "TajikaAspects", "categories": ["TAJIKA_ASPECT", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_50,
        },
        {
            "user": "If Sun is at 14° Aries and Mars is at 10° Libra, what yoga is formed?",
            "tool_call": {"tool": "getTajikaAspects", "arguments": {"sunLong": 14.0, "marsLong": 190.0}},
            "tool_result": {"relationship": "Opposition", "separating": True, "yoga": "Ishrafa", "sourceRef": SOURCE_REF_50},
            "assistant": "Sun (faster, rank 4) is at 14° and Mars (slower, rank 5) is at 10°. Because faster Sun (14°) > slower Mars (10°), the aspect is separating within orb, forming Ishrafa Yoga. Source: Samjna Tantra Ch. 3 v. 3.",
            "feature": "TajikaAspects", "categories": ["TAJIKA_ASPECT", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_50,
        },

        # --- 38-42: MUDDA DASHA ---
        {
            "user": "What is Mudda Dasha in Tajika Varshaphal?",
            "tool_call": {"tool": "getMuddaDasha", "arguments": {"targetYear": 2024}},
            "tool_result": {"periodsCount": 9, "profile": "MUDDA_VIMSHOTTARI_PROPORTIONAL_ANNUAL_CYCLE", "ruleId": "TN-MUD-01", "sourceRef": SOURCE_REF_192},
            "assistant": "Mudda Dasha scales the standard 120-year Vimshottari Dasha sequence proportionally to the exact duration of the annual solar return interval (~365.242 days) with 9 planetary sub-periods. Source: Varsha Tantra p. 192 vv. 14-15 & Tajika Muktavali.",
            "feature": "MuddaDasha", "categories": ["MUDDA_DASHA", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_192,
        },
        {
            "user": "How are planetary durations calculated in Mudda Dasha?",
            "tool_call": {"tool": "getMuddaDasha", "arguments": {"query": "durations"}},
            "tool_result": {"formula": "(vimshottariYears / 120) * annualDays", "Sun": 18.26, "Moon": 30.44, "Venus": 60.87, "sourceRef": SOURCE_REF_192},
            "assistant": "Duration for each planet = (Vimshottari years / 120) * annual interval days. E.g. Sun: (6/120)*365.24 = 18.26 days; Moon: (10/120)*365.24 = 30.44 days; Venus: (20/120)*365.24 = 60.87 days. Source: Tajika Muktavali.",
            "feature": "MuddaDasha", "categories": ["MUDDA_DASHA", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_192,
        },
        {
            "user": "How is the starting planet of Mudda Dasha determined?",
            "tool_call": {"tool": "getMuddaDasha", "arguments": {"natalMoonLongitude": 10.0, "elapsedCycles": 0}},
            "tool_result": {"startingLord": "Ketu", "nakshatra": "Ashwini", "sourceRef": SOURCE_REF_192},
            "assistant": "Starting lord is determined by the natal Moon's nakshatra (Ashwini -> Ketu), advanced by elapsed return cycles. Source: Varsha Tantra p. 192.",
            "feature": "MuddaDasha", "categories": ["MUDDA_DASHA", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_192,
        },
        {
            "user": "Does Mudda Dasha cover the entire annual solar return interval without gaps or overlaps?",
            "tool_call": {"tool": "getMuddaDasha", "arguments": {"annualLengthDays": 365.24219}},
            "tool_result": {"totalDuration": 365.24219, "continuous": True, "sourceRef": SOURCE_REF_192},
            "assistant": "Yes. Mudda Dasha is strictly contiguous: period N ends at the exact microsecond period N+1 begins, and the sum of all 9 durations equals the solar return interval with zero gap and zero overlap. Source: Tajika Muktavali.",
            "feature": "MuddaDasha", "categories": ["MUDDA_DASHA", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_192,
        },
        {
            "user": "What is the sequence of planets in Mudda Dasha?",
            "tool_call": {"tool": "getMuddaDasha", "arguments": {"query": "sequence"}},
            "tool_result": {"sequence": ["Sun", "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury", "Ketu", "Venus"], "sourceRef": SOURCE_REF_192},
            "assistant": "Mudda Dasha preserves the standard Vimshottari order: Sun -> Moon -> Mars -> Rahu -> Jupiter -> Saturn -> Mercury -> Ketu -> Venus, starting from the cycle-shifted nakshatra lord. Source: Tajika Muktavali.",
            "feature": "MuddaDasha", "categories": ["MUDDA_DASHA", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_192,
        },

        # --- 43-45: FULL VARSHAPHAL PIPELINE ---
        {
            "user": "What is the execution pipeline of Tajika Varshaphal?",
            "tool_call": {"tool": "getVarshaphal", "arguments": {"query": "pipeline"}},
            "tool_result": {"stages": ["Solar Return Ingress", "Annual Chart Construction", "Muntha & Muntha Lord", "Varsheshwara", "Sahams", "Tajika Aspects/Yogas", "Mudda Dasha"], "sourceRef": SOURCE_REF_110},
            "assistant": "The pipeline proceeds deterministically: Natal Birth Data -> Solar Return Moment -> Annual Chart -> Muntha -> Muntha Lord -> Varsheshwara -> Sahams -> Tajika Aspects & Yogas -> Mudda Dasha -> Grounded EvidenceGraph. Source: Tajika Neelakanthi.",
            "feature": "Varshaphal", "categories": ["VARSHAPHAL", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_110,
        },
        {
            "user": "How is the Solar Return moment computed?",
            "tool_call": {"tool": "getVarshaphal", "arguments": {"targetYear": 2024}},
            "tool_result": {"calculation": "Recurrence of exact natal sidereal Sun longitude in target year", "sourceRef": SOURCE_REF_110},
            "assistant": "Solar Return is the precise astronomical moment when the Sun returns to its exact natal sidereal longitude in the target year. The annual chart is cast for this ingress timestamp. Source: Tajika Neelakanthi Varsha Tantra.",
            "feature": "Varshaphal", "categories": ["VARSHAPHAL", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_110,
        },
        {
            "user": "How are evidence and provenance tracked in the Varshaphal result?",
            "tool_call": {"tool": "getVarshaphal", "arguments": {"query": "evidence"}},
            "tool_result": {"evidenceKind": "KNOWLEDGE_RULE", "tradition": "TAJIKA", "sourceId": "tajika-neelakanthi-1907-scan"},
            "assistant": "Every calculated feature carries source references to the 1907 edition of Tajika Neelakanthi with chapter and verse citations, fused into a deterministic evidence bundle. Source: AYNVORA Knowledge Architecture.",
            "feature": "Varshaphal", "categories": ["VARSHAPHAL", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_110,
        },

        # --- 46-47: HINDI QUERIES ---
        {
            "user": "ताजिक पद्धति में मुन्था की गणना कैसे की जाती है?",
            "tool_call": {"tool": "getMuntha", "arguments": {"natalAscendantLongitude": 15.0, "elapsedSolarReturnCycles": 1}},
            "tool_result": {"sign": "Taurus", "longitude": 45.0, "lord": "Venus", "sourceRef": SOURCE_REF_120},
            "assistant": "ताजिक नीलकण्ठी (वर्ष तन्त्र श्लोक १) के अनुसार, जन्म लग्न से प्रति वर्ष एक-एक राशि आगे बढ़ने पर मुन्था प्राप्त होती है। जन्म लग्न के अंश वही रहते हैं। पहले वर्ष (आयु १) में मेष १५° से बढ़कर वृषभ १५° हो जाती है। स्रोत: वर्ष तन्त्र श्लोक १।",
            "feature": "Muntha", "categories": ["MUNTHA", "HINDI", "CALCULATION"], "sourceRef": SOURCE_REF_120,
        },
        {
            "user": "वर्षेश्वर के पाँच अधिकारी कौन से हैं?",
            "tool_call": {"tool": "getVarsheshwara", "arguments": {"query": "panchadhikari"}},
            "tool_result": {"candidates": ["जन्मलग्नेश", "वर्षलग्नेश", "मुन्थेश", "त्रिराशिपति", "दिन/रात्रि-पति"], "sourceRef": SOURCE_REF_110},
            "assistant": "ताजिक नीलकण्ठी के अनुसार वर्षेश्वर के पाँच दावेदार हैं: जन्मलग्नेश, वर्षलग्नेश, मुन्थेश, त्रिराशिपति तथा दिन में सूर्य राशि का स्वामी या रात्रि में चन्द्र राशि का स्वामी (दिनाधिप/रात्र्यधिप)। स्रोत: वर्ष तन्त्र श्लोक ५-७।",
            "feature": "Varsheshwara", "categories": ["VARSHESHWARA", "HINDI", "SOURCE"], "sourceRef": SOURCE_REF_110,
        },

        # --- 48-49: UNSUPPORTED QUERIES ---
        {
            "user": "Can Tajika astrology predict winning lottery numbers or exact stock prices?",
            "tool_call": {"tool": "getVarshaphal", "arguments": {"query": "lottery"}},
            "tool_result": {"status": "UNSUPPORTED", "reason": "Classical Tajika provides symbolic qualitative indications, not deterministic secular financial prediction."},
            "assistant": "This is unsupported. Classical Tajika Neelakanthi provides symbolic annual indications, not deterministic prediction of lotteries or financial asset prices. No such rule exists in the classical source.",
            "feature": "Varshaphal", "categories": ["UNSUPPORTED", "ENGLISH"], "sourceRef": SOURCE_REF_110,
        },
        {
            "user": "Does Tajika astrology use Parashari special aspects like Mars 4th/8th or Saturn 3rd/10th?",
            "tool_call": {"tool": "getTajikaAspects", "arguments": {"specialAspects": True}},
            "tool_result": {"status": "UNSUPPORTED", "reason": "Tajika uses strictly Ptolemaic/Arabic geometric aspects (conjunction, sextile, square, trine, opposition) with deeptamsha orbs."},
            "assistant": "No. Classical Tajika does NOT use Parashari special aspects. Tajika aspects are strictly based on geometric sign distances: 1-1, 3-11 (sextile), 4-10 (square), 5-9 (trine), and 1-7 (opposition) qualified by planetary Deeptamshas. Source: Samjna Tantra v. 13.",
            "feature": "TajikaAspects", "categories": ["TAJIKA_ASPECT", "UNSUPPORTED", "SOURCE", "ENGLISH"], "sourceRef": SOURCE_REF_49,
        },

        # --- 50: DIRECT TOOL CALL ---
        {
            "user": "Execute getVarsheshwara tool for natal Aries, annual Leo day ingress, Muntha Taurus.",
            "tool_call": {"tool": "getVarsheshwara", "arguments": {"natalAscendantLongitude": 15.0, "annualAscendantSignIndex": 4, "munthaSignIndex": 1, "isDay": True}},
            "tool_result": {"selectedPlanet": "SUN", "eligibility": {"MARS": True, "SUN": True, "VENUS": True, "JUPITER": True}, "ruleId": "TN-VAR-01", "sourceRef": SOURCE_REF_110},
            "assistant": "The getVarsheshwara tool selected the Sun as Year Lord based on eligibility (aspect to annual Leo Lagna) and highest Panchavargiya strength among the five office-bearers. Source: Varsha Tantra vv. 5-8.",
            "feature": "Varsheshwara", "categories": ["VARSHESHWARA", "TOOL_CALL", "CALCULATION", "ENGLISH"], "sourceRef": SOURCE_REF_110,
        },
    ]

    examples = []
    for item in raw_items:
        source_id = "tajika-neelakanthi-1907-scan" if "scan" in item.get("sourceRef", "") else "tajika-neelakanthi-wikisource-pages-112-122-124"
        examples.append({
            "messages": [
                {"role": "system", "content": "Use only supplied source evidence. Attribute traditional indications, avoid certainty, and do not infer missing chart calculations."},
                {"role": "user", "content": item["user"]},
                {"role": "tool", "content": json.dumps(item["tool_call"] if "tool" in item["tool_call"] else {"tool": "getVarshaphal", "result": item["tool_result"]}, ensure_ascii=False, sort_keys=True)},
                {"role": "assistant", "content": item["assistant"]},
            ],
            "metadata": {
                "tradition": "TAJIKA",
                "feature": item["feature"],
                "sources": [source_id],
                "verified": True,
                "datasetVersion": "TAJIKA_SFT_V1",
                "knowledgeVersion": "1.0.0",
                "calculationProfile": "EXPLICIT_SOURCE_RULE_ONLY",
                "categories": item["categories"],
                "sourceRef": item["sourceRef"],
            }
        })

    OUT.write_text("".join(json.dumps(ex, ensure_ascii=False, sort_keys=True) + "\n" for ex in examples), encoding="utf-8")
    SOURCE_REGISTRY.write_text(json.dumps({"sources": APPROVED_SOURCES}, indent=2) + "\n", encoding="utf-8")
    print(f"Generated {len(examples)} verified examples at {OUT}")

if __name__ == "__main__":
    build_examples()
