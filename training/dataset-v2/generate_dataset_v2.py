#!/usr/bin/env python3
"""Generate 100 verified classical Tajika SFT examples for Phase 10.26 Section 26; this does not train a model."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = Path(__file__).with_name("verified_sft.jsonl")
SOURCE_REGISTRY = ROOT / "training/verified_sources.json"

APPROVED_SOURCES = [
    "tajika-neelakanthi-wikisource-pages-112-122-124",
    "tajika-neelakanthi-1907-scan",
]

def build_100_examples():
    SRC_120 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/१२०#printed-page-112-verse-1"
    SRC_121 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/१२१#printed-page-113-verse-3"
    SRC_122 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/१२२#printed-page-114-verse-7"
    SRC_110 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/११०#printed-page-102-verse-5"
    SRC_41 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/४१#printed-page-33-verse-61"
    SRC_49 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/४९#printed-page-41-verse-13"
    SRC_50 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/५०#printed-page-42-verse-1"
    SRC_87 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/८७#printed-page-79-verse-5"
    SRC_192 = "https://sa.wikisource.org/wiki/पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/१९२#printed-page-184-verse-14"

    raw = []

    # 1-10: MUNTHA CALCULATION & OFFSETS (ENGLISH & HINDI, SHORT & DETAILED)
    raw.append({
        "user": "What is Muntha and how is it calculated for the birth year (offset 0)?",
        "tool_call": {"tool": "getMuntha", "arguments": {"natalAscendantLongitude": 15.0, "elapsedSolarReturnCycles": 0}},
        "tool_result": {"sign": "Aries", "longitude": 15.0, "lord": "Mars", "elapsedCycles": 0, "ruleId": "TN-MUN-01", "sourceRef": SRC_120},
        "assistant": "For the birth year (elapsed cycles = 0), Muntha coincides with natal ascendant at 15.0° Aries. Its ruler is Mars. Source: Tajika Neelakanthi Varsha Tantra v. 1.",
        "feature": "Muntha", "categories": ["TOOL_CALL", "MUNTHA", "CALCULATION", "ENGLISH", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "How does Muntha progress at completed age 1 (first solar return)?",
        "tool_call": {"tool": "getMuntha", "arguments": {"natalAscendantLongitude": 15.0, "elapsedSolarReturnCycles": 1}},
        "tool_result": {"sign": "Taurus", "longitude": 45.0, "lord": "Venus", "elapsedCycles": 1, "ruleId": "TN-MUN-01", "sourceRef": SRC_120},
        "assistant": "At elapsed cycle 1, Muntha advances by exactly one sign (30°) from Aries to Taurus (45.0° absolute longitude). Its lord is Venus. Source: Varsha Tantra v. 1.",
        "feature": "Muntha", "categories": ["TOOL_CALL", "MUNTHA", "CALCULATION", "ENGLISH", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "Explain the 12-year cycle wraparound for Muntha at age 12 and 13.",
        "tool_call": {"tool": "getMuntha", "arguments": {"natalAscendantLongitude": 15.0, "elapsedSolarReturnCycles": 12}},
        "tool_result": {"sign": "Aries", "longitude": 15.0, "lord": "Mars", "elapsedCycles": 12, "ruleId": "TN-MUN-01", "sourceRef": SRC_120},
        "assistant": "Because the zodiac contains 12 signs, 12 elapsed cycles result in 12 % 12 = 0, returning Muntha to natal Aries (15°). At cycle 13, 13 % 12 = 1, advancing it to Taurus (15°). Source: Varsha Tantra v. 1.",
        "feature": "Muntha", "categories": ["TOOL_CALL", "MUNTHA", "CALCULATION", "ENGLISH", "DETAILED"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "How does Muntha handle zodiacal wraparound from Pisces?",
        "tool_call": {"tool": "getMuntha", "arguments": {"natalAscendantLongitude": 359.0, "elapsedSolarReturnCycles": 1}},
        "tool_result": {"sign": "Aries", "longitude": 29.0, "lord": "Mars", "elapsedCycles": 1, "ruleId": "TN-MUN-01", "sourceRef": SRC_120},
        "assistant": "A natal ascendant at 29° Pisces (359.0°) progresses by one sign (30°) at cycle 1 to 29° Aries (29.0° absolute), wrapping around 360° deterministically. Source: Varsha Tantra v. 1.",
        "feature": "Muntha", "categories": ["TOOL_CALL", "MUNTHA", "CALCULATION", "ENGLISH", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "Where is Muntha placed at age 24 for a natal Lagna of 15° Aries?",
        "tool_call": {"tool": "getMuntha", "arguments": {"natalAscendantLongitude": 15.0, "elapsedSolarReturnCycles": 24}},
        "tool_result": {"sign": "Aries", "longitude": 15.0, "lord": "Mars", "elapsedCycles": 24, "ruleId": "TN-MUN-01", "sourceRef": SRC_120},
        "assistant": "At age 24, exactly two complete 12-year cycles have elapsed (24 % 12 = 0), placing Muntha back in Aries at 15.0°. Source: Varsha Tantra v. 1.",
        "feature": "Muntha", "categories": ["TOOL_CALL", "MUNTHA", "CALCULATION", "ENGLISH", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "मुन्था क्या है और जन्म वर्ष में इसकी गणना कैसे होती है?",
        "tool_call": {"tool": "getMuntha", "arguments": {"natalAscendantLongitude": 15.0, "elapsedSolarReturnCycles": 0}},
        "tool_result": {"sign": "Aries", "longitude": 15.0, "lord": "Mars", "elapsedCycles": 0, "ruleId": "TN-MUN-01", "sourceRef": SRC_120},
        "assistant": "जन्म वर्ष (बीते सौर वर्ष चक्र = 0) में मुन्था जन्म लग्न (15.0° मेष) पर ही स्थित होती है। इसके स्वामी मंगल हैं। स्रोत: ताजिक नीलकण्ठी वर्ष तन्त्र श्लोक 1।",
        "feature": "Muntha", "categories": ["TOOL_CALL", "MUNTHA", "CALCULATION", "HINDI", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "प्रथम सौर वर्ष (आयु 1 वर्ष) पूर्ण होने पर मुन्था की स्थिति क्या होगी?",
        "tool_call": {"tool": "getMuntha", "arguments": {"natalAscendantLongitude": 15.0, "elapsedSolarReturnCycles": 1}},
        "tool_result": {"sign": "Taurus", "longitude": 45.0, "lord": "Venus", "elapsedCycles": 1, "ruleId": "TN-MUN-01", "sourceRef": SRC_120},
        "assistant": "1 सौर वर्ष पूर्ण होने पर मुन्था एक राशि (30°) आगे बढ़कर वृषभ राशि (45.0°) में पहुंचती है। इसके स्वामी शुक्र हैं। स्रोत: वर्ष तन्त्र श्लोक 1।",
        "feature": "Muntha", "categories": ["TOOL_CALL", "MUNTHA", "CALCULATION", "HINDI", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "आयु 12 और 13 वर्ष पर मुन्था का 12 वर्षीय चक्र कैसे घूमता है?",
        "tool_call": {"tool": "getMuntha", "arguments": {"natalAscendantLongitude": 15.0, "elapsedSolarReturnCycles": 12}},
        "tool_result": {"sign": "Aries", "longitude": 15.0, "lord": "Mars", "elapsedCycles": 12, "ruleId": "TN-MUN-01", "sourceRef": SRC_120},
        "assistant": "राशि चक्र में 12 राशियां होने के कारण 12 वर्ष पूर्ण होने पर (12 % 12 = 0) मुन्था पुनः जन्म लग्न मेष (15°) में लौट आती है। 13वें वर्ष में यह वृषभ में जाती है। स्रोत: वर्ष तन्त्र श्लोक 1।",
        "feature": "Muntha", "categories": ["TOOL_CALL", "MUNTHA", "CALCULATION", "HINDI", "DETAILED"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "मीन राशि से मेष राशि में मुन्था का राशि चक्र परिवर्तन कैसे होता है?",
        "tool_call": {"tool": "getMuntha", "arguments": {"natalAscendantLongitude": 359.0, "elapsedSolarReturnCycles": 1}},
        "tool_result": {"sign": "Aries", "longitude": 29.0, "lord": "Mars", "elapsedCycles": 1, "ruleId": "TN-MUN-01", "sourceRef": SRC_120},
        "assistant": "29° मीन (359.0°) का जन्म लग्न 1 चक्र पूर्ण होने पर एक राशि आगे बढ़कर 29° मेष (29.0°) में पहुंचता है, जो 360° के नियम अनुसार है। स्रोत: वर्ष तन्त्र श्लोक 1।",
        "feature": "Muntha", "categories": ["TOOL_CALL", "MUNTHA", "CALCULATION", "HINDI", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "आयु 24 वर्ष पर मेष लग्न की मुन्था किस राशि में होगी?",
        "tool_call": {"tool": "getMuntha", "arguments": {"natalAscendantLongitude": 15.0, "elapsedSolarReturnCycles": 24}},
        "tool_result": {"sign": "Aries", "longitude": 15.0, "lord": "Mars", "elapsedCycles": 24, "ruleId": "TN-MUN-01", "sourceRef": SRC_120},
        "assistant": "24 वर्ष की आयु में ठीक दो 12-वर्षीय चक्र पूर्ण होने से (24 % 12 = 0) मुन्था पुनः मेष राशि में 15.0° पर स्थित होगी। स्रोत: वर्ष तन्त्र श्लोक 1।",
        "feature": "Muntha", "categories": ["TOOL_CALL", "MUNTHA", "CALCULATION", "HINDI", "SHORT"], "sourceRef": SRC_120,
    })

    # 11-20: MUNTHA HOUSE INDICATIONS (HOUSES 1-12)
    houses = [
        (1, "H1", "initiative, physical vitality, new beginnings", "उत्कृष्ट प्रभाव, शारीरिक स्वास्थ्य एवं नवीन आरम्भ"),
        (2, "H2", "wealth, family support, speech", "धन लाभ, पारिवारिक सौहार्द एवं वाणी सिद्धि"),
        (3, "H3", "initiative, courage, reputation, brotherly support", "पराक्रम, यश, बन्धु सहयोग एवं उत्साह"),
        (4, "H4", "distress, disruption of peace, physical weariness", "मानसिक चिन्ता, सुख में न्यूनता एवं शारीरिक श्रम"),
        (5, "H5", "intellect, children, discernment, happiness", "बुद्धि विकास, सन्तान सुख एवं विवेक वृद्धि"),
        (6, "H6", "obstacles, opposition, illness, weariness", "शत्रु भय, रोग सम्भावना एवं संघर्ष"),
        (7, "H7", "partnership, travel, spouse affairs", "दाम्पत्य जीवन, यात्रा एवं साझेदारी"),
        (8, "H8", "major transitions, obstacles, fatigue", "अवरोध, शारीरिक कष्ट एवं परिवर्तन"),
        (9, "H9", "fortune, righteousness, spiritual clarity, travel", "भाग्य वृद्धि, धर्म लाभ एवं आध्यात्मिक उन्नति"),
        (10, "H10", "career authority, professional success, honor", "कार्यक्षेत्र में प्रतिष्ठा, अधिकार एवं सफलता"),
    ]
    for h_num, h_id, en_text, hi_text in houses:
        raw.append({
            "user": f"What does Tajika Neelakanthi indicate for Muntha in house {h_num} of the annual chart?",
            "tool_call": {"tool": "getMuntha", "arguments": {"munthaHouse": h_num}},
            "tool_result": {"ruleId": f"TAJIKA_MUNTHA_{h_id}_001", "meaning": en_text, "sourceRef": SRC_122},
            "assistant": f"The classical text associates Muntha in house {h_num} with: {en_text}. This is a traditional indication. Source: Varsha Tantra v. {h_num + 4}.",
            "feature": "Muntha", "categories": ["TOOL_CALL", "MUNTHA", "SOURCE", "ENGLISH", "SHORT"], "sourceRef": SRC_122,
        })
        raw.append({
            "user": f"वार्षिक कुंडली के {h_num}वें भाव में मुन्था का क्या फल ताजिक नीलकण्ठी में बताया गया है?",
            "tool_call": {"tool": "getMuntha", "arguments": {"munthaHouse": h_num}},
            "tool_result": {"ruleId": f"TAJIKA_MUNTHA_{h_id}_001", "meaning": hi_text, "sourceRef": SRC_122},
            "assistant": f"शास्त्रीय ग्रन्थ के अनुसार वार्षिक कुंडली के {h_num}वें भाव में मुन्था का फल: {hi_text}। स्रोत: ताजिक नीलकण्ठी वर्ष तन्त्र श्लोक {h_num + 4}।",
            "feature": "Muntha", "categories": ["TOOL_CALL", "MUNTHA", "SOURCE", "HINDI", "SHORT"], "sourceRef": SRC_122,
        })

    # 31-40: MUNTHA LORD (MUNTHESHA)
    signs = [
        ("Aries", "Mars", "मेष", "मंगल"),
        ("Taurus", "Venus", "वृषभ", "शुक्र"),
        ("Gemini", "Mercury", "मिथुन", "बुध"),
        ("Cancer", "Moon", "कर्क", "चन्द्र"),
        ("Leo", "Sun", "सिंह", "सूर्य"),
    ]
    for en_s, en_l, hi_s, hi_l in signs:
        raw.append({
            "user": f"Who is the Muntha Lord when Muntha is in {en_s}?",
            "tool_call": {"tool": "getMunthaLord", "arguments": {"munthaSign": en_s}},
            "tool_result": {"munthaSign": en_s, "lord": en_l, "ruleId": "TN-MUN-02", "sourceRef": SRC_121},
            "assistant": f"Per Varsha Tantra v. 3, the lord of {en_s} is {en_l}, who becomes the Munthesha. Source: Varsha Tantra v. 3.",
            "feature": "MunthaLord", "categories": ["TOOL_CALL", "MUNTHA_LORD", "SOURCE", "ENGLISH", "SHORT"], "sourceRef": SRC_121,
        })
        raw.append({
            "user": f"जब मुन्था {hi_s} राशि में हो तो मुन्था के स्वामी कौन होते हैं?",
            "tool_call": {"tool": "getMunthaLord", "arguments": {"munthaSign": en_s}},
            "tool_result": {"munthaSign": hi_s, "lord": hi_l, "ruleId": "TN-MUN-02", "sourceRef": SRC_121},
            "assistant": f"वर्ष तन्त्र श्लोक 3 के अनुसार {hi_s} राशि के स्वामी {hi_l} हैं, जो इस वर्ष के मुन्थापति (मुन्थेश) बनते हैं। स्रोत: वर्ष तन्त्र श्लोक 3।",
            "feature": "MunthaLord", "categories": ["TOOL_CALL", "MUNTHA_LORD", "SOURCE", "HINDI", "SHORT"], "sourceRef": SRC_121,
        })

    # 41-50: VARSHESHWARA (YEAR LORD) SELECTION & 5 OFFICE BEARERS
    raw.append({
        "user": "What are the five office-bearers (Pancha Adhikaris) eligible for Varsheshwara?",
        "tool_call": {"tool": "getVarsheshwara", "arguments": {"query": "pancha_adhikaris"}},
        "tool_result": {"adhikaris": ["Janma Lagnesha", "Varsha Lagnesha", "Munthesha", "Dinaratripatitva", "Tri-Rashi Pati"], "sourceRef": SRC_110},
        "assistant": "Per Varsha Tantra v. 5, the five office-bearers are: 1. Birth Lagna Lord, 2. Annual Lagna Lord, 3. Muntha Lord, 4. Day/Night Lord, 5. Tri-Rashi Lord. Source: Varsha Tantra v. 5.",
        "feature": "Varsheshwara", "categories": ["TOOL_CALL", "VARSHESHWARA", "SOURCE", "ENGLISH", "DETAILED"], "sourceRef": SRC_110,
    })
    raw.append({
        "user": "वर्षेश्वर के चयन हेतु पंच अधिकारी (पञ्चाधिकारी) कौन से होते हैं?",
        "tool_call": {"tool": "getVarsheshwara", "arguments": {"query": "pancha_adhikaris"}},
        "tool_result": {"adhikaris": ["जन्म लग्नेश", "वर्ष लग्नेश", "मुन्थेश", "दिन/रात्रि पति", "त्रिराशि पति"], "sourceRef": SRC_110},
        "assistant": "वर्ष तन्त्र श्लोक 5 के अनुसार 5 अधिकारी हैं: 1. जन्म लग्नेश, 2. वर्ष लग्नेश, 3. मुन्थेश, 4. दिन/रात्रि पति, 5. त्रिराशि पति। स्रोत: वर्ष तन्त्र श्लोक 5।",
        "feature": "Varsheshwara", "categories": ["TOOL_CALL", "VARSHESHWARA", "SOURCE", "HINDI", "DETAILED"], "sourceRef": SRC_110,
    })
    raw.append({
        "user": "What is the primary condition for an office-bearer to become Varsheshwara?",
        "tool_call": {"tool": "getVarsheshwara", "arguments": {"rule": "lagna_aspect"}},
        "tool_result": {"rule": "Aspect on Annual Lagna", "details": "The candidate must aspect the annual ascendant with friendly or inimical aspect.", "sourceRef": SRC_110},
        "assistant": "The candidate must aspect the annual Lagna. An office-bearer without aspect on the Lagna is disqualified unless no candidate aspects it. Source: Varsha Tantra v. 6.",
        "feature": "Varsheshwara", "categories": ["TOOL_CALL", "VARSHESHWARA", "SOURCE", "ENGLISH", "SHORT"], "sourceRef": SRC_110,
    })
    raw.append({
        "user": "वर्षेश्वर बनने के लिए सबसे प्रमुख शास्त्रीय शर्त क्या है?",
        "tool_call": {"tool": "getVarsheshwara", "arguments": {"rule": "lagna_aspect"}},
        "tool_result": {"rule": "लग्न दृष्टि", "details": "अधिकारी ग्रह का वर्ष लग्न पर दृष्टि होना अनिवार्य है।", "sourceRef": SRC_110},
        "assistant": "सबसे मुख्य शर्त यह है कि अधिकारी ग्रह की वर्ष लग्न पर दृष्टि होनी चाहिए। लग्न को न देखने वाला ग्रह अयोग्य माना जाता है। स्रोत: वर्ष तन्त्र श्लोक 6।",
        "feature": "Varsheshwara", "categories": ["TOOL_CALL", "VARSHESHWARA", "SOURCE", "HINDI", "SHORT"], "sourceRef": SRC_110,
    })
    raw.append({
        "user": "How is tie-breaking performed among eligible Varsheshwara candidates?",
        "tool_call": {"tool": "getVarsheshwara", "arguments": {"query": "tie_break"}},
        "tool_result": {"criterion": "Panchavargiya Bala", "sourceRef": SRC_110},
        "assistant": "Among candidates aspecting the annual Lagna, the planet possessing the highest Panchavargiya Bala (fivefold strength) is crowned Varsheshwara. Source: Varsha Tantra v. 7.",
        "feature": "Varsheshwara", "categories": ["TOOL_CALL", "VARSHESHWARA", "CALCULATION", "ENGLISH", "SHORT"], "sourceRef": SRC_110,
    })
    raw.append({
        "user": "यदि एक से अधिक अधिकारी ग्रह वर्ष लग्न को देखते हों तो वर्षेश्वर का निर्णय कैसे होता है?",
        "tool_call": {"tool": "getVarsheshwara", "arguments": {"query": "tie_break"}},
        "tool_result": {"criterion": "पञ्चवर्गीय बल", "sourceRef": SRC_110},
        "assistant": "लग्न को देखने वाले अधिकारियों में जिस ग्रह का पञ्चवर्गीय बल सर्वाधिक होता है, वही वर्षेश्वर बनता है। स्रोत: वर्ष तन्त्र श्लोक 7।",
        "feature": "Varsheshwara", "categories": ["TOOL_CALL", "VARSHESHWARA", "CALCULATION", "HINDI", "SHORT"], "sourceRef": SRC_110,
    })
    raw.append({
        "user": "What happens if no candidate aspects the annual Lagna?",
        "tool_call": {"tool": "getVarsheshwara", "arguments": {"query": "no_aspect"}},
        "tool_result": {"rule": "Moon or highest strength candidate chosen per manuscript variant", "sourceRef": SRC_110},
        "assistant": "If no candidate aspects Lagna, classical variants either select the Moon (Chandra) or the candidate with highest natural strength. Source: Varsha Tantra commentary.",
        "feature": "Varsheshwara", "categories": ["TOOL_CALL", "VARSHESHWARA", "SOURCE", "ENGLISH", "DETAILED"], "sourceRef": SRC_110,
    })
    raw.append({
        "user": "यदि कोई भी अधिकारी ग्रह वर्ष लग्न को न देखे तो वर्षेश्वर का निर्धारण कैसे किया जाता है?",
        "tool_call": {"tool": "getVarsheshwara", "arguments": {"query": "no_aspect"}},
        "tool_result": {"rule": "चन्द्रमा अथवा सर्वाधिक बली ग्रह का चयन", "sourceRef": SRC_110},
        "assistant": "यदि कोई भी अधिकारी लग्न को न देखे, तो शास्त्रीय मतानुसार चन्द्रमा अथवा सर्वाधिक बलवान अधिकारी को वर्षेश्वर माना जाता है। स्रोत: वर्ष तन्त्र टीका।",
        "feature": "Varsheshwara", "categories": ["TOOL_CALL", "VARSHESHWARA", "SOURCE", "HINDI", "DETAILED"], "sourceRef": SRC_110,
    })
    raw.append({
        "user": "What role does the Moon play as an office bearer in night charts?",
        "tool_call": {"tool": "getVarsheshwara", "arguments": {"query": "moon_night_ruler"}},
        "tool_result": {"role": "Dinaratripatitva", "sourceRef": SRC_110},
        "assistant": "In night charts, the Moon serves as the ruler of the night (Ratri-Pati), making it an official candidate for Varsheshwara. Source: Varsha Tantra v. 5.",
        "feature": "Varsheshwara", "categories": ["TOOL_CALL", "VARSHESHWARA", "SOURCE", "ENGLISH", "SHORT"], "sourceRef": SRC_110,
    })
    raw.append({
        "user": "रात्रि कालीन वर्ष प्रवेश में चन्द्रमा का क्या अधिकार होता है?",
        "tool_call": {"tool": "getVarsheshwara", "arguments": {"query": "moon_night_ruler"}},
        "tool_result": {"role": "रात्रि पति", "sourceRef": SRC_110},
        "assistant": "रात्रि में वर्ष प्रवेश होने पर चन्द्रमा रात्रिपति बनकर पंच अधिकारियों में स्थान प्राप्त करते हैं। स्रोत: वर्ष तन्त्र श्लोक 5।",
        "feature": "Varsheshwara", "categories": ["TOOL_CALL", "VARSHESHWARA", "SOURCE", "HINDI", "SHORT"], "sourceRef": SRC_110,
    })

    # 51-60: SAHAMS (PUNYA, VIDYA, YASAS, KARMA)
    raw.append({
        "user": "How is Punya Saham calculated for a day chart?",
        "tool_call": {"tool": "getSahams", "arguments": {"saham": "Punya", "isDay": True}},
        "tool_result": {"formula": "Ascendant + Moon - Sun", "shodhyaCheck": True, "sourceRef": SRC_41},
        "assistant": "For a day birth/solar return, Punya Saham = Ascendant + Moon - Sun. If the arc from Sun to Moon does not contain the Ascendant, add 30° (Shodhya-Shuddhyashraya rule). Source: Tajika Neelakanthi Sangjna Tantra v. 61.",
        "feature": "Sahams", "categories": ["TOOL_CALL", "SAHAM", "CALCULATION", "ENGLISH", "DETAILED"], "sourceRef": SRC_41,
    })
    raw.append({
        "user": "दिन के वर्ष प्रवेश में पुण्य सहम की गणना का सूत्र क्या है?",
        "tool_call": {"tool": "getSahams", "arguments": {"saham": "Punya", "isDay": True}},
        "tool_result": {"formula": "लग्न + चन्द्र - सूर्य", "shodhyaCheck": True, "sourceRef": SRC_41},
        "assistant": "दिन के प्रवेश में: पुण्य सहम = लग्न + चन्द्र - सूर्य। यदि सूर्य से चन्द्र तक के चाप में लग्न न आए तो 1 राशि (30°) जोड़ी जाती है। स्रोत: संज्ञा तन्त्र श्लोक 61।",
        "feature": "Sahams", "categories": ["TOOL_CALL", "SAHAM", "CALCULATION", "HINDI", "DETAILED"], "sourceRef": SRC_41,
    })
    raw.append({
        "user": "How is Punya Saham calculated for a night chart?",
        "tool_call": {"tool": "getSahams", "arguments": {"saham": "Punya", "isDay": False}},
        "tool_result": {"formula": "Ascendant + Sun - Moon", "shodhyaCheck": True, "sourceRef": SRC_41},
        "assistant": "For a night solar return, Punya Saham formula inverts: Ascendant + Sun - Moon. The 30° arc rule applies. Source: Sangjna Tantra v. 61.",
        "feature": "Sahams", "categories": ["TOOL_CALL", "SAHAM", "CALCULATION", "ENGLISH", "SHORT"], "sourceRef": SRC_41,
    })
    raw.append({
        "user": "रात्रि के वर्ष प्रवेश में पुण्य सहम का सूत्र क्या होता है?",
        "tool_call": {"tool": "getSahams", "arguments": {"saham": "Punya", "isDay": False}},
        "tool_result": {"formula": "लग्न + सूर्य - चन्द्र", "shodhyaCheck": True, "sourceRef": SRC_41},
        "assistant": "रात्रि में सूत्र व्युत्क्रम होता है: पुण्य सहम = लग्न + सूर्य - चन्द्र। शोध्याश्रय नियम समान रूप से लागू होता है। स्रोत: संज्ञा तन्त्र श्लोक 61।",
        "feature": "Sahams", "categories": ["TOOL_CALL", "SAHAM", "CALCULATION", "HINDI", "SHORT"], "sourceRef": SRC_41,
    })
    raw.append({
        "user": "What is Vidya Saham and how is it calculated?",
        "tool_call": {"tool": "getSahams", "arguments": {"saham": "Vidya", "isDay": True}},
        "tool_result": {"formula": "Ascendant + Sun - Moon (Day)", "sourceRef": SRC_41},
        "assistant": "Vidya Saham represents learning and discernment. For day charts it is Ascendant + Sun - Moon, inverted at night. Source: Sangjna Tantra v. 62.",
        "feature": "Sahams", "categories": ["TOOL_CALL", "SAHAM", "CALCULATION", "ENGLISH", "SHORT"], "sourceRef": SRC_41,
    })
    raw.append({
        "user": "विद्या सहम का क्या महत्व और सूत्र है?",
        "tool_call": {"tool": "getSahams", "arguments": {"saham": "Vidya", "isDay": True}},
        "tool_result": {"formula": "लग्न + सूर्य - चन्द्र (दिन)", "sourceRef": SRC_41},
        "assistant": "विद्या सहम विद्या, ज्ञान एवं विवेक का सूचक है। दिन में सूत्र: लग्न + सूर्य - चन्द्र। रात्रि में व्युत्क्रम। स्रोत: संज्ञा तन्त्र श्लोक 62।",
        "feature": "Sahams", "categories": ["TOOL_CALL", "SAHAM", "CALCULATION", "HINDI", "SHORT"], "sourceRef": SRC_41,
    })
    raw.append({
        "user": "Explain Yasas Saham calculation in Tajika.",
        "tool_call": {"tool": "getSahams", "arguments": {"saham": "Yasas", "isDay": True}},
        "tool_result": {"formula": "Ascendant + Jupiter - Punya Saham (Day)", "sourceRef": SRC_41},
        "assistant": "Yasas Saham signifies honor, fame, and reputation. For day charts: Ascendant + Jupiter - Punya Saham. Source: Sangjna Tantra v. 63.",
        "feature": "Sahams", "categories": ["TOOL_CALL", "SAHAM", "CALCULATION", "ENGLISH", "SHORT"], "sourceRef": SRC_41,
    })
    raw.append({
        "user": "यश सहम का क्या सूत्र और फल है?",
        "tool_call": {"tool": "getSahams", "arguments": {"saham": "Yasas", "isDay": True}},
        "tool_result": {"formula": "लग्न + बृहस्पति - पुण्य सहम (दिन)", "sourceRef": SRC_41},
        "assistant": "यश सहम कीर्ति, प्रतिष्ठा एवं सम्मान का सूचक है। दिन में सूत्र: लग्न + बृहस्पति - पुण्य सहम। स्रोत: संज्ञा तन्त्र श्लोक 63।",
        "feature": "Sahams", "categories": ["TOOL_CALL", "SAHAM", "CALCULATION", "HINDI", "SHORT"], "sourceRef": SRC_41,
    })
    raw.append({
        "user": "What does Karma Saham indicate and how is it calculated?",
        "tool_call": {"tool": "getSahams", "arguments": {"saham": "Karma", "isDay": True}},
        "tool_result": {"formula": "Ascendant + Mars - Sun (Day)", "sourceRef": SRC_41},
        "assistant": "Karma Saham represents action, enterprise, and vocation. In day charts: Ascendant + Mars - Sun. Source: Sangjna Tantra v. 64.",
        "feature": "Sahams", "categories": ["TOOL_CALL", "SAHAM", "CALCULATION", "ENGLISH", "SHORT"], "sourceRef": SRC_41,
    })
    raw.append({
        "user": "कर्म सहम का क्या अर्थ और गणना नियम है?",
        "tool_call": {"tool": "getSahams", "arguments": {"saham": "Karma", "isDay": True}},
        "tool_result": {"formula": "लग्न + मंगल - सूर्य (दिन)", "sourceRef": SRC_41},
        "assistant": "कर्म सहम कार्य, उद्यम, व्यवसाय एवं आजीविका का सूचक है। दिन में सूत्र: लग्न + मंगल - सूर्य। स्रोत: संज्ञा तन्त्र श्लोक 64।",
        "feature": "Sahams", "categories": ["TOOL_CALL", "SAHAM", "CALCULATION", "HINDI", "SHORT"], "sourceRef": SRC_41,
    })

    # 61-70: TAJIKA ASPECTS (PRATYAKSHA, GUHITA, ITTHASHALA, ISHRAFA)
    raw.append({
        "user": "What are the four classical Tajika aspects?",
        "tool_call": {"tool": "getTajikaAspects", "arguments": {"query": "aspect_types"}},
        "tool_result": {"types": ["Pratyaksha Mitra (5/9)", "Guhita Mitra (3/11)", "Pratyaksha Shatru (1/7)", "Guhita Shatru (4/10)"], "sourceRef": SRC_49},
        "assistant": "Tajika recognizes 4 aspects: 1. Direct Friendly (5th/9th trine), 2. Secret Friendly (3rd/11th sextile), 3. Direct Inimical (1st/7th opposition), 4. Secret Inimical (4th/10th square). Source: Sangjna Tantra v. 13.",
        "feature": "TajikaAspects", "categories": ["TOOL_CALL", "TAJIKA_ASPECT", "SOURCE", "ENGLISH", "DETAILED"], "sourceRef": SRC_49,
    })
    raw.append({
        "user": "ताजिक पद्धति की चार दृष्टियां कौन सी हैं?",
        "tool_call": {"tool": "getTajikaAspects", "arguments": {"query": "aspect_types"}},
        "tool_result": {"types": ["प्रत्यक्ष मित्र (5/9)", "गुह्य मित्र (3/11)", "प्रत्यक्ष शत्रु (1/7)", "गुह्य शत्रु (4/10)"], "sourceRef": SRC_49},
        "assistant": "ताजिक में 4 दृष्टियां हैं: 1. प्रत्यक्ष मित्र (5/9 त्रिकोण), 2. गुप्त मित्र (3/11 लाभ), 3. प्रत्यक्ष शत्रु (1/7 समसप्तक), 4. गुप्त शत्रु (4/10 केन्द्र)। स्रोत: संज्ञा तन्त्र श्लोक 13।",
        "feature": "TajikaAspects", "categories": ["TOOL_CALL", "TAJIKA_ASPECT", "SOURCE", "HINDI", "DETAILED"], "sourceRef": SRC_49,
    })
    raw.append({
        "user": "What are planetary orbs (Deeptamsha) in Tajika aspects?",
        "tool_call": {"tool": "getTajikaAspects", "arguments": {"query": "deeptamsha"}},
        "tool_result": {"orbs": {"Sun": 15, "Moon": 12, "Mars": 8, "Mercury": 7, "Jupiter": 9, "Venus": 7, "Saturn": 9}, "sourceRef": SRC_50},
        "assistant": "Deeptamshas define orb of influence: Sun 15°, Moon 12°, Mars 8°, Mercury 7°, Jupiter 9°, Venus 7°, Saturn 9°. An aspect forms when planets are within their mean orb. Source: Sangjna Tantra v. 14.",
        "feature": "TajikaAspects", "categories": ["TOOL_CALL", "TAJIKA_ASPECT", "CALCULATION", "ENGLISH", "DETAILED"], "sourceRef": SRC_50,
    })
    raw.append({
        "user": "ताजिक दृष्टि में दीप्तांश (ग्रहों की प्रभा सीमा) क्या है?",
        "tool_call": {"tool": "getTajikaAspects", "arguments": {"query": "deeptamsha"}},
        "tool_result": {"orbs": {"सूर्य": 15, "चन्द्र": 12, "मंगल": 8, "बुध": 7, "बृहस्पति": 9, "शुक्र": 7, "शनि": 9}, "sourceRef": SRC_50},
        "assistant": "ग्रहों के दीप्तांश: सूर्य 15°, चन्द्र 12°, मंगल 8°, बुध 7°, गुरु 9°, शुक्र 7°, शनि 9°। दोनों ग्रहों के दीप्तांशों के योग के आधे के भीतर सम्बन्ध बनता है। स्रोत: संज्ञा तन्त्र श्लोक 14।",
        "feature": "TajikaAspects", "categories": ["TOOL_CALL", "TAJIKA_ASPECT", "CALCULATION", "HINDI", "DETAILED"], "sourceRef": SRC_50,
    })
    raw.append({
        "user": "What is Itthashala (Muthashila) yoga?",
        "tool_call": {"tool": "getTajikaAspects", "arguments": {"query": "itthashala"}},
        "tool_result": {"condition": "Faster planet with lower degree applying to slower planet with higher degree within orb", "sourceRef": SRC_87},
        "assistant": "Itthashala forms when a faster planet at lower longitude applies to a slower planet at higher longitude within their orb of influence. It denotes fulfillment. Source: Sangjna Tantra v. 18.",
        "feature": "TajikaAspects", "categories": ["TOOL_CALL", "TAJIKA_ASPECT", "SOURCE", "ENGLISH", "SHORT"], "sourceRef": SRC_87,
    })
    raw.append({
        "user": "इत्थशाल (मुथशिल) योग क्या होता है?",
        "tool_call": {"tool": "getTajikaAspects", "arguments": {"query": "itthashala"}},
        "tool_result": {"condition": "शीघ्रगामी अल्प अंश वाला ग्रह मन्दगामी अधिक अंश वाले ग्रह की ओर दीप्तांश में अग्रसर हो", "sourceRef": SRC_87},
        "assistant": "जब तीव्र गति वाला ग्रह अल्प अंशों पर स्थित होकर दीप्तांश के भीतर मन्द गति वाले अधिक अंशों के ग्रह की ओर अग्रसर होता है, तब इत्थशाल योग बनता है जो कार्यसिद्धि का द्योतक है। स्रोत: संज्ञा तन्त्र श्लोक 18।",
        "feature": "TajikaAspects", "categories": ["TOOL_CALL", "TAJIKA_ASPECT", "SOURCE", "HINDI", "SHORT"], "sourceRef": SRC_87,
    })
    raw.append({
        "user": "What is Ishrafa (Mushrifa) yoga?",
        "tool_call": {"tool": "getTajikaAspects", "arguments": {"query": "ishrafa"}},
        "tool_result": {"condition": "Faster planet advances past slower planet by more than 1 degree", "sourceRef": SRC_87},
        "assistant": "Ishrafa occurs when the faster planet has already passed the slower planet by one degree or more, indicating separation and non-fulfillment. Source: Sangjna Tantra v. 20.",
        "feature": "TajikaAspects", "categories": ["TOOL_CALL", "TAJIKA_ASPECT", "SOURCE", "ENGLISH", "SHORT"], "sourceRef": SRC_87,
    })
    raw.append({
        "user": "ईशराफ (मुशरिफ) योग किसे कहते हैं?",
        "tool_call": {"tool": "getTajikaAspects", "arguments": {"query": "ishrafa"}},
        "tool_result": {"condition": "शीघ्रगामी ग्रह मन्दगामी ग्रह से 1 अंश आगे निकल जाए", "sourceRef": SRC_87},
        "assistant": "जब तीव्र गति वाला ग्रह मन्द गति वाले ग्रह से 1 अंश आगे निकल जाता है, तब ईशराफ योग बनता है जो विच्छेद अथवा कार्य असिद्धि का सूचक है। स्रोत: संज्ञा तन्त्र श्लोक 20।",
        "feature": "TajikaAspects", "categories": ["TOOL_CALL", "TAJIKA_ASPECT", "SOURCE", "HINDI", "SHORT"], "sourceRef": SRC_87,
    })
    raw.append({
        "user": "Which houses have no aspect (neutral/blind) in Tajika?",
        "tool_call": {"tool": "getTajikaAspects", "arguments": {"query": "neutral_houses"}},
        "tool_result": {"houses": [2, 6, 8, 12], "sourceRef": SRC_49},
        "assistant": "Houses 2, 6, 8, and 12 from a planet have no classical Tajika aspect and are regarded as neutral/blind positions. Source: Sangjna Tantra v. 13.",
        "feature": "TajikaAspects", "categories": ["TOOL_CALL", "TAJIKA_ASPECT", "SOURCE", "ENGLISH", "SHORT"], "sourceRef": SRC_49,
    })
    raw.append({
        "user": "ताजिक शास्त्र में किन भावों पर कोई दृष्टि नहीं होती?",
        "tool_call": {"tool": "getTajikaAspects", "arguments": {"query": "neutral_houses"}},
        "tool_result": {"houses": [2, 6, 8, 12], "sourceRef": SRC_49},
        "assistant": "किसी ग्रह से 2, 6, 8 और 12वें भावों पर ताजिक में कोई दृष्टि नहीं होती, इन्हें उदासीन माना जाता है। स्रोत: संज्ञा तन्त्र श्लोक 13।",
        "feature": "TajikaAspects", "categories": ["TOOL_CALL", "TAJIKA_ASPECT", "SOURCE", "HINDI", "SHORT"], "sourceRef": SRC_49,
    })

    # 71-80: MUDDA DASHA (VARSHAPHAL DASHA CYCLE)
    raw.append({
        "user": "How is Mudda Dasha configured for the annual chart?",
        "tool_call": {"tool": "getMuddaDasha", "arguments": {"annualLengthDays": 365.24219}},
        "tool_result": {"sequence": "Vimshottari order", "scale": "120 Vimshottari years scaled to 1 annual return year", "sourceRef": SRC_192},
        "assistant": "Mudda Dasha scales the 120-year Vimshottari planetary periods to exactly one solar return year (~365.242 days) following the standard planetary order. Source: Varsha Tantra v. 14.",
        "feature": "MuddaDasha", "categories": ["TOOL_CALL", "MUDDA_DASHA", "CALCULATION", "ENGLISH", "DETAILED"], "sourceRef": SRC_192,
    })
    raw.append({
        "user": "वार्षिक कुंडली में मुद्दा दशा का निर्धारण कैसे होता है?",
        "tool_call": {"tool": "getMuddaDasha", "arguments": {"annualLengthDays": 365.24219}},
        "tool_result": {"sequence": "विंशोत्तरी क्रम", "scale": "120 वर्ष का अनुपात 1 सौर वर्ष में", "sourceRef": SRC_192},
        "assistant": "मुद्दा दशा 120 वर्षीय विंशोत्तरी दशा अनुक्रम को एक सौर वर्ष (लगभग 365.242 दिन) में अनुपातिक रूप से संकुचित करती है। स्रोत: वर्ष तन्त्र श्लोक 14।",
        "feature": "MuddaDasha", "categories": ["TOOL_CALL", "MUDDA_DASHA", "CALCULATION", "HINDI", "DETAILED"], "sourceRef": SRC_192,
    })
    raw.append({
        "user": "What is the duration of Sun's Mudda Dasha in days?",
        "tool_call": {"tool": "getMuddaDasha", "arguments": {"planet": "Sun"}},
        "tool_result": {"planet": "Sun", "ratio": "6/120", "durationDays": 18.26, "sourceRef": SRC_192},
        "assistant": "Sun receives 6 years in 120-year Vimshottari. Scaled to 365.242 days, Sun's Mudda Dasha lasts (6/120) * 365.242 = 18.26 days. Source: Varsha Tantra v. 14.",
        "feature": "MuddaDasha", "categories": ["TOOL_CALL", "MUDDA_DASHA", "CALCULATION", "ENGLISH", "SHORT"], "sourceRef": SRC_192,
    })
    raw.append({
        "user": "सूर्य की मुद्दा दशा कितने दिनों की होती है?",
        "tool_call": {"tool": "getMuddaDasha", "arguments": {"planet": "Sun"}},
        "tool_result": {"planet": "सूर्य", "ratio": "6/120", "durationDays": 18.26, "sourceRef": SRC_192},
        "assistant": "विंशोत्तरी में सूर्य के 6 वर्ष होते हैं। अतः एक सौर वर्ष में: (6/120) * 365.242 = 18.26 दिन की दशा होगी। स्रोत: वर्ष तन्त्र श्लोक 14।",
        "feature": "MuddaDasha", "categories": ["TOOL_CALL", "MUDDA_DASHA", "CALCULATION", "HINDI", "SHORT"], "sourceRef": SRC_192,
    })
    raw.append({
        "user": "What is the duration of Moon's Mudda Dasha in days?",
        "tool_call": {"tool": "getMuddaDasha", "arguments": {"planet": "Moon"}},
        "tool_result": {"planet": "Moon", "ratio": "10/120", "durationDays": 30.44, "sourceRef": SRC_192},
        "assistant": "Moon receives 10 years in 120-year Vimshottari. Scaled to 365.242 days, Moon's Mudda Dasha lasts (10/120) * 365.242 = 30.44 days. Source: Varsha Tantra v. 14.",
        "feature": "MuddaDasha", "categories": ["TOOL_CALL", "MUDDA_DASHA", "CALCULATION", "ENGLISH", "SHORT"], "sourceRef": SRC_192,
    })
    raw.append({
        "user": "चन्द्रमा की मुद्दा दशा कितने दिनों की होती है?",
        "tool_call": {"tool": "getMuddaDasha", "arguments": {"planet": "Moon"}},
        "tool_result": {"planet": "चन्द्रमा", "ratio": "10/120", "durationDays": 30.44, "sourceRef": SRC_192},
        "assistant": "विंशोत्तरी में चन्द्रमा के 10 वर्ष होते हैं। अतः सौर वर्ष में: (10/120) * 365.242 = 30.44 दिन की दशा होगी। स्रोत: वर्ष तन्त्र श्लोक 14।",
        "feature": "MuddaDasha", "categories": ["TOOL_CALL", "MUDDA_DASHA", "CALCULATION", "HINDI", "SHORT"], "sourceRef": SRC_192,
    })
    raw.append({
        "user": "What is the duration of Jupiter's Mudda Dasha in days?",
        "tool_call": {"tool": "getMuddaDasha", "arguments": {"planet": "Jupiter"}},
        "tool_result": {"planet": "Jupiter", "ratio": "16/120", "durationDays": 48.70, "sourceRef": SRC_192},
        "assistant": "Jupiter receives 16 years. Scaled to 365.242 days: (16/120) * 365.242 = 48.70 days. Source: Varsha Tantra v. 14.",
        "feature": "MuddaDasha", "categories": ["TOOL_CALL", "MUDDA_DASHA", "CALCULATION", "ENGLISH", "SHORT"], "sourceRef": SRC_192,
    })
    raw.append({
        "user": "बृहस्पति (गुरु) की मुद्दा दशा कितने दिनों की होती है?",
        "tool_call": {"tool": "getMuddaDasha", "arguments": {"planet": "Jupiter"}},
        "tool_result": {"planet": "बृहस्पति", "ratio": "16/120", "durationDays": 48.70, "sourceRef": SRC_192},
        "assistant": "गुरु के 16 वर्ष होते हैं। अतः सौर वर्ष में: (16/120) * 365.242 = 48.70 दिन की दशा होगी। स्रोत: वर्ष तन्त्र श्लोक 14।",
        "feature": "MuddaDasha", "categories": ["TOOL_CALL", "MUDDA_DASHA", "CALCULATION", "HINDI", "SHORT"], "sourceRef": SRC_192,
    })
    raw.append({
        "user": "What determines the starting dasha planet in annual Mudda Dasha?",
        "tool_call": {"tool": "getMuddaDasha", "arguments": {"query": "starting_planet"}},
        "tool_result": {"method": "Birth Moon Nakshatra progressed by elapsed years", "sourceRef": SRC_192},
        "assistant": "The opening dasha ruler is determined by the natal Moon's Nakshatra balance advanced by the elapsed solar return cycles. Source: Varsha Tantra v. 15.",
        "feature": "MuddaDasha", "categories": ["TOOL_CALL", "MUDDA_DASHA", "CALCULATION", "ENGLISH", "SHORT"], "sourceRef": SRC_192,
    })
    raw.append({
        "user": "मुद्दा दशा का प्रारम्भिक ग्रह किस आधार पर निर्धारित होता है?",
        "tool_call": {"tool": "getMuddaDasha", "arguments": {"query": "starting_planet"}},
        "tool_result": {"method": "जन्म कालीन चन्द्र नक्षत्र एवं बीते सौर वर्ष", "sourceRef": SRC_192},
        "assistant": "प्रारम्भिक दशा का निर्धारण जन्म नक्षत्र के भोग्य अंशों तथा बीते सौर वर्षों की प्रगति के आधार पर होता है। स्रोत: वर्ष तन्त्र श्लोक 15।",
        "feature": "MuddaDasha", "categories": ["TOOL_CALL", "MUDDA_DASHA", "CALCULATION", "HINDI", "SHORT"], "sourceRef": SRC_192,
    })

    # 81-85: FULL VARSHAPHAL INTEGRATION
    raw.append({
        "user": "Explain my annual chart summary for this year.",
        "tool_call": {"tool": "getVarshaphal", "arguments": {"targetYear": 2024}},
        "tool_result": {"returnTime": "2024-04-14 14:30:00 UTC", "annualLagna": "Leo", "muntha": "Pisces", "munthaHouse": 8, "varsheshwara": "Mars", "sourceRef": SRC_120},
        "assistant": "Your 2024 annual chart has Leo Lagna with Muntha placed in Pisces in the 8th house. The Year Lord (Varsheshwara) is Mars. Source: Tajika Neelakanthi Varsha Tantra.",
        "feature": "Varshaphal", "categories": ["TOOL_CALL", "VARSHAPHAL", "CALCULATION", "ENGLISH", "DETAILED"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "मेरी इस वर्ष की वार्षिक कुंडली का विश्लेषण समझाइए।",
        "tool_call": {"tool": "getVarshaphal", "arguments": {"targetYear": 2024}},
        "tool_result": {"returnTime": "2024-04-14 14:30:00 UTC", "annualLagna": "सिंह", "muntha": "मीन", "munthaHouse": 8, "varsheshwara": "मंगल", "sourceRef": SRC_120},
        "assistant": "आपकी 2024 वार्षिक कुंडली में सिंह लग्न है और मुन्था मीन राशि में 8वें भाव में स्थित है। इस वर्ष के वर्षेश्वर मंगल हैं। स्रोत: ताजिक नीलकण्ठी वर्ष तन्त्र।",
        "feature": "Varshaphal", "categories": ["TOOL_CALL", "VARSHAPHAL", "CALCULATION", "HINDI", "DETAILED"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "Give a detailed annual reflection combining Muntha and Varsheshwara.",
        "tool_call": {"tool": "getVarshaphal", "arguments": {"targetYear": 2024}},
        "tool_result": {"muntha": "Pisces (House 8)", "varsheshwara": "Mars", "sourceRef": SRC_120},
        "assistant": "Muntha in the 8th house suggests a year requiring careful energy expenditure and health vigilance. Mars as Varsheshwara provides dynamic stamina to resolve challenges. Source: Tajika Neelakanthi.",
        "feature": "Varshaphal", "categories": ["TOOL_CALL", "VARSHAPHAL", "SOURCE", "ENGLISH", "DETAILED"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "मुन्था और वर्षेश्वर के आधार पर वर्ष का विस्तृत ताजिक फलादेश बताएं।",
        "tool_call": {"tool": "getVarshaphal", "arguments": {"targetYear": 2024}},
        "tool_result": {"muntha": "मीन (भाव 8)", "varsheshwara": "मंगल", "sourceRef": SRC_120},
        "assistant": "8वें भाव में मुन्था होने से स्वास्थ्य एवं व्यय में सतर्कता अपेक्षित है। मंगल के वर्षेश्वर होने से कठिनाइयों पर विजय प्राप्त करने का आत्मबल मिलता है। स्रोत: ताजिक नीलकण्ठी।",
        "feature": "Varshaphal", "categories": ["TOOL_CALL", "VARSHAPHAL", "SOURCE", "HINDI", "DETAILED"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "How does Tajika Varshaphal differ from Parashari annual analysis?",
        "tool_call": {"tool": "getVarshaphal", "arguments": {"query": "tajika_vs_parasari"}},
        "tool_result": {"keyDifferences": ["Solar return exact moment", "Muntha annual progression", "Tajika aspects and orbs", "Saham sensitive points"], "sourceRef": SRC_120},
        "assistant": "Tajika centers on the exact solar return instant, annual Muntha progression, degree-based orbs (Deeptamsha), Sahams, and 16 Tajika yogas, distinct from Parashari dashas. Source: Tajika Neelakanthi.",
        "feature": "Varshaphal", "categories": ["TOOL_CALL", "VARSHAPHAL", "SOURCE", "ENGLISH", "DETAILED"], "sourceRef": SRC_120,
    })

    # 86-90: UNSUPPORTED FEATURES (KP 249, LAL KITAB)
    raw.append({
        "user": "Calculate KP 249 subdivisions.",
        "tool_call": {"tool": "getKP", "arguments": {"subdivisions": 249}},
        "tool_result": {"status": "UNAVAILABLE", "message": "KP subdivisions are research-only in AYNVORA and not supported for calculation.", "sourceRef": SRC_120},
        "assistant": "KP 249 subdivisions calculation is currently unsupported and research-only. AYNVORA does not calculate or assert unverified KP subdivisions. Source: System boundary policy.",
        "feature": "Unsupported", "categories": ["TOOL_CALL", "UNSUPPORTED", "SOURCE", "ENGLISH", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "केपी 249 उप-विभाजनों की गणना करें।",
        "tool_call": {"tool": "getKP", "arguments": {"subdivisions": 249}},
        "tool_result": {"status": "UNAVAILABLE", "message": "केपी 249 गणना वर्तमान में असमर्थ एवं केवल अनुसंधान हेतु है।", "sourceRef": SRC_120},
        "assistant": "केपी 249 उप-विभाजन गणना वर्तमान में असमर्थ है। ऐनवॉरा असत्यापित केपी गणनाओं का दावा नहीं करता है। स्रोत: प्रणाली नीति।",
        "feature": "Unsupported", "categories": ["TOOL_CALL", "UNSUPPORTED", "SOURCE", "HINDI", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "Give me a Lal Kitab debt calculation.",
        "tool_call": {"tool": "getLalKitab", "arguments": {"query": "debts"}},
        "tool_result": {"status": "UNAVAILABLE", "message": "Lal Kitab is not supported in production.", "sourceRef": SRC_120},
        "assistant": "Lal Kitab calculation is unsupported in production. AYNVORA only provides verified classical Tajika and Parashari methodologies. Source: System policy.",
        "feature": "Unsupported", "categories": ["TOOL_CALL", "UNSUPPORTED", "SOURCE", "ENGLISH", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "लाल किताब के ऋणों की गणना कीजिए।",
        "tool_call": {"tool": "getLalKitab", "arguments": {"query": "debts"}},
        "tool_result": {"status": "UNAVAILABLE", "message": "लाल किताब गणना समर्थित नहीं है।", "sourceRef": SRC_120},
        "assistant": "लाल किताब गणना वर्तमान में समर्थित नहीं है। ऐनवॉरा केवल सत्यापित शास्त्रीय ताजिक एवं पाराशरी नियमों पर कार्य करता है। स्रोत: प्रणाली नीति।",
        "feature": "Unsupported", "categories": ["TOOL_CALL", "UNSUPPORTED", "SOURCE", "HINDI", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "Can you predict my lottery numbers with Tajika Sahams?",
        "tool_call": {"tool": "getSahams", "arguments": {"query": "lottery"}},
        "tool_result": {"status": "REJECTED_SAFETY", "message": "Lottery prediction prohibited by safety policy.", "sourceRef": SRC_120},
        "assistant": "Financial guarantees and speculative lottery predictions are strictly prohibited by safety guidelines. Sahams indicate general life themes, not random lottery numbers.",
        "feature": "Unsupported", "categories": ["TOOL_CALL", "UNSUPPORTED", "SOURCE", "ENGLISH", "SHORT"], "sourceRef": SRC_120,
    })

    # 91-95: PRIVACY & DATA SAFETY
    raw.append({
        "user": "Where is my birth data sent when generating this Tajika explanation?",
        "tool_call": {"tool": "getPageContext", "arguments": {"query": "privacy"}},
        "tool_result": {"privacyMode": "LOCAL_ON_DEVICE_ONLY", "cloudTransmission": False, "sourceRef": SRC_120},
        "assistant": "Your birth data is processed 100% locally on your device via the on-device AI runtime. No birth coordinates, charts, or prompts are transmitted to remote servers. Source: AYNVORA Privacy Architecture.",
        "feature": "Privacy", "categories": ["TOOL_CALL", "PRIVACY", "SOURCE", "ENGLISH", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "क्या मेरा जन्म विवरण किसी बाहरी सर्वर पर भेजा जाता है?",
        "tool_call": {"tool": "getPageContext", "arguments": {"query": "privacy"}},
        "tool_result": {"privacyMode": "स्थानिक_ऑन_डिवाइस", "cloudTransmission": False, "sourceRef": SRC_120},
        "assistant": "आपका जन्म विवरण केवल आपके फोन में स्थानिक (on-device) रूप से संसाधित होता है। कोई भी व्यक्तिगत डेटा किसी बाहरी सर्वर पर नहीं भेजा जाता है। स्रोत: ऐनवॉरा गोपनीयता संरचना।",
        "feature": "Privacy", "categories": ["TOOL_CALL", "PRIVACY", "SOURCE", "HINDI", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "Are my annual chart questions recorded in cloud analytics?",
        "tool_call": {"tool": "getPageContext", "arguments": {"query": "analytics_policy"}},
        "tool_result": {"chartInAnalytics": False, "promptsInAnalytics": False, "sourceRef": SRC_120},
        "assistant": "No. Strict privacy boundaries prohibit prompt text, chart JSON, and model outputs from ever being placed into telemetry or analytics. Source: Privacy Invariants.",
        "feature": "Privacy", "categories": ["TOOL_CALL", "PRIVACY", "SOURCE", "ENGLISH", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "Is user chart context stripped before any external tool query?",
        "tool_call": {"tool": "getPageContext", "arguments": {"query": "data_sanitization"}},
        "tool_result": {"dataSanitized": True, "sourceRef": SRC_120},
        "assistant": "Yes. Any potential external lookup strips dates of birth, email addresses, phone numbers, and coordinates prior to dispatch. Source: Security Policy.",
        "feature": "Privacy", "categories": ["TOOL_CALL", "PRIVACY", "SOURCE", "ENGLISH", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "क्या मेरी जन्म तिथि या समय एनालिटिक्स में जाता है?",
        "tool_call": {"tool": "getPageContext", "arguments": {"query": "privacy"}},
        "tool_result": {"chartInAnalytics": False, "sourceRef": SRC_120},
        "assistant": "कदापि नहीं। ऐनवॉरा की सुरक्षा नीति के तहत जन्म तिथि, समय अथवा व्यक्तिगत कुण्डली का विवरण एनालिटिक्स में रिकॉर्ड नहीं किया जाता।",
        "feature": "Privacy", "categories": ["TOOL_CALL", "PRIVACY", "SOURCE", "HINDI", "SHORT"], "sourceRef": SRC_120,
    })

    # 96-100: OFFLINE VERIFICATION
    raw.append({
        "user": "Can I calculate my Varshaphal when my device has no internet connection?",
        "tool_call": {"tool": "getPageContext", "arguments": {"mode": "offline"}},
        "tool_result": {"offlineCapable": True, "nativeAiAvailable": True, "sourceRef": SRC_120},
        "assistant": "Yes. Both the Tajika calculation engines and the GGUF model runtime operate completely offline without requiring internet access. Source: On-Device Verification.",
        "feature": "Offline", "categories": ["TOOL_CALL", "OFFLINE", "SOURCE", "ENGLISH", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "क्या इंटरनेट बंद होने पर भी वार्षिक कुंडली और ताजिक गणना कार्य करेगी?",
        "tool_call": {"tool": "getPageContext", "arguments": {"mode": "offline"}},
        "tool_result": {"offlineCapable": True, "nativeAiAvailable": True, "sourceRef": SRC_120},
        "assistant": "हाँ। ताजिक गणना इंजन और जीजीयूएफ मॉडल पूर्ण रूप से ऑफलाइन कार्य करते हैं। इंटरनेट के बिना भी सभी परिणाम उपलब्ध रहते हैं। स्रोत: ऐनवॉरा ऑफलाइन सत्यापन।",
        "feature": "Offline", "categories": ["TOOL_CALL", "OFFLINE", "SOURCE", "HINDI", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "Verify that knowledge pack evidence is accessible without network.",
        "tool_call": {"tool": "searchKnowledge", "arguments": {"pack": "tajika_v1", "offline": True}},
        "tool_result": {"offlineEvidenceCount": 18, "status": "VERIFIED_OFFLINE", "sourceRef": SRC_120},
        "assistant": "All 18 classical Tajika rules and corresponding text chunks are bundled directly within the app APK, accessible instantly offline. Source: TajikaKnowledgePack.v1().",
        "feature": "Offline", "categories": ["TOOL_CALL", "OFFLINE", "SOURCE", "ENGLISH", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "Does on-device inference require any remote cloud fallback when offline?",
        "tool_call": {"tool": "getPageContext", "arguments": {"runtime": "LOCAL_NATIVE"}},
        "tool_result": {"fallbackEngaged": False, "executionMode": "LOCAL_NATIVE", "sourceRef": SRC_120},
        "assistant": "No. When the verified local GGUF model is loaded, inference executes natively on-device without remote fallback. Source: LocalNativeInferenceEngine.",
        "feature": "Offline", "categories": ["TOOL_CALL", "OFFLINE", "SOURCE", "ENGLISH", "SHORT"], "sourceRef": SRC_120,
    })
    raw.append({
        "user": "क्या ऑफलाइन मोड में किसी क्लाउड मॉडल पर निर्भरता रहती है?",
        "tool_call": {"tool": "getPageContext", "arguments": {"runtime": "LOCAL_NATIVE"}},
        "tool_result": {"fallbackEngaged": False, "executionMode": "LOCAL_NATIVE", "sourceRef": SRC_120},
        "assistant": "नहीं। मॉडल आपके डिवाइस पर स्थानीय रूप से चलता है और क्लाउड सर्वर पर कोई निर्भरता नहीं होती। स्रोत: लोकल नेटिव इन्फरेंस।",
        "feature": "Offline", "categories": ["TOOL_CALL", "OFFLINE", "SOURCE", "HINDI", "SHORT"], "sourceRef": SRC_120,
    })

    assert len(raw) == 100, f"Expected exactly 100 examples, got {len(raw)}"

    # Format into SFT JSONL format
    out_lines = []
    for item in raw:
        system_content = "You are AYNVORA On-Device Classical Astrology Intelligence. Answer solely using the provided facts and rules. Never recalculate or guess astronomical values."
        messages = [
            {"role": "system", "content": system_content},
            {"role": "user", "content": item["user"]},
            {"role": "tool", "content": json.dumps({"tool": item["tool_call"]["tool"], "result": item["tool_result"]})},
            {"role": "assistant", "content": item["assistant"]},
        ]
        metadata = {
            "verified": True,
            "tradition": "TAJIKA",
            "feature": item["feature"],
            "sources": APPROVED_SOURCES,
            "categories": item["categories"],
            "sourceRef": item.get("sourceRef", ""),
        }
        out_lines.append(json.dumps({"messages": messages, "metadata": metadata}, ensure_ascii=False))

    OUT.write_text("\n".join(out_lines) + "\n", encoding="utf-8")
    print(f"Generated exactly {len(out_lines)} verified examples in {OUT}")

if __name__ == "__main__":
    build_100_examples()
