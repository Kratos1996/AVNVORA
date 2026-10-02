#!/usr/bin/env python3
"""Rebuild the narrow, sourced Tajika pack from retained licensed page transcriptions."""
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).parent
SOURCE_PAGES = ROOT / "source" / "source_pages.json"
OUT = ROOT / "TAJIKA_V1.json"
SCAN_SHA256 = "a6968d0f22a277eca7d64649490baad1c1cd08b71dde4adacccd267420e5f989"


def sha(value):
    return hashlib.sha256(value.encode("utf-8")).hexdigest()


def canonical(value):
    return json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(",", ":"))


def main():
    acquired = json.loads(SOURCE_PAGES.read_text(encoding="utf-8"))
    pages = {p["pdfPage"]: p for p in acquired["pages"]}
    required = {112, 122, 124}
    if set(pages) != required:
        raise SystemExit(f"Expected source pages {sorted(required)}, got {sorted(pages)}")
    for page in pages.values():
        if sha(page["wikitext"]) != page["wikitextSha256"]:
            raise SystemExit(f"Wikisource page {page['pdfPage']} checksum mismatch")
        if page["transcriptionStatus"] != "UNPROOFREAD_TRANSCRIPTION_VISUALLY_CROSSCHECKED_AGAINST_SCAN":
            raise SystemExit(f"Page {page['pdfPage']} verification status is not accepted")

    transcript_hash = sha("\n".join(f"{n}:{pages[n]['wikitextSha256']}" for n in sorted(pages)))
    scan_source = {
        "sourceId": "tajika-neelakanthi-1907-scan",
        "title": "Tājika Nīlakaṇṭhī (Mahidhar Hindi commentary edition)",
        "author": "Nīlakaṇṭha Daivajña; Hindi commentary attributed in the edition to Mahidhar",
        "edition": "Khemraj Shri Venkateshwar Steam Press edition; Samvat 1964 / Śaka 1829 (1907 CE)",
        "publicationYear": 1907,
        "language": "Sanskrit with Hindi commentary",
        "sourceType": "PUBLIC_DOMAIN_SCAN",
        "sourceClass": "PUBLIC_DOMAIN",
        "license": "Public domain in India and the United States per Wikimedia Commons source record; territorial status may vary elsewhere",
        "rightsStatus": "VERIFIED",
        "rightsTerritories": ["India", "United States"],
        "sourceUrl": "https://commons.wikimedia.org/wiki/File:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf",
        "publisher": "Khemraj Shri Venkateshwar Steam Press, Mumbai",
        "repository": "Wikimedia Commons; scanned under Digitalized Sanskrit Corps",
        "retrievedAt": acquired["retrievedAt"],
        "contentHash": SCAN_SHA256,
        "version": "1907-scan-sha256:a6968d0f",
        "inspectedFiles": ["280-page PDF; folios 112, 122, 124 visually reviewed"],
    }
    transcription_source = {
        "sourceId": "tajika-neelakanthi-wikisource-pages-112-122-124",
        "title": "Wikisource transcription of selected Tājika Nīlakaṇṭhī pages",
        "author": "Wikisource contributors",
        "edition": "Transcription revisions 378434, 378444, 378446 of the 1907 edition",
        "publicationYear": 2023,
        "language": "Sanskrit with Hindi commentary",
        "sourceType": "WEB_PAGE",
        "sourceClass": "LICENSED",
        "license": "CC BY-SA 4.0; attribution and ShareAlike apply to transcribed text and adaptations",
        "rightsStatus": "VERIFIED",
        "rightsTerritories": ["Worldwide under CC BY-SA 4.0 terms"],
        "sourceUrl": "https://sa.wikisource.org/wiki/अनुक्रमणिका:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf",
        "publisher": "Wikisource contributors",
        "repository": "Sanskrit Wikisource; https://sa.wikisource.org/",
        "retrievedAt": acquired["retrievedAt"],
        "contentHash": transcript_hash,
        "version": "revisions:378434,378444,378446",
        "transcriptionStatus": "The three source pages are marked unproofread on Wikisource; each retained page was manually cross-checked against the scan images.",
        "attribution": "Wikisource contributors, Tājika Nīlakaṇṭhī (Mahidhar Hindi commentary), Sanskrit Wikisource, revisions 378434, 378444, 378446, CC BY-SA 4.0. AYNVORA adaptations are distributed under CC BY-SA 4.0.",
    }
    source_ref = lambda n, verse: f"{pages[n]['url']}#printed-page-{pages[n]['printedPage']}-verse-{verse}"
    source_pages = {
        112: ("Chapter/section label not established; Varshesha indications", 104),
        122: ("Muntha phala", 114),
        124: ("Muntha phala and qualifications", 116),
    }
    summaries = {
        112: "The 1907 Hindi commentary describes annual indications for the Sun when it is Varshesha (year lord), distinguishing strong, middling, and weak conditions. It also says natal strength modifies how fully the annual indication manifests.",
        122: "The commentary associates Muntha in the 3rd and 5th houses with favorable traditional indications, and in the 4th and 6th with challenging indications. Its house-specific descriptions are preserved as tradition-attributed themes, not factual forecasts.",
        124: "The commentary qualifies house-based Muntha indications: malefic occupation or hostile malefic aspect can suppress favorable indications and strengthen adverse ones; benefic association/aspect and a strong lord can support favorable indications.",
    }
    chunks = []
    for n in sorted(required):
        text = summaries[n]
        ref = source_ref(n, "11-13" if n == 112 else "7-11" if n == 122 else "17-20")
        checksum = sha(text)
        chunk_id = "tajika:%s:%s" % (n, checksum[:16])
        chunks.append({
            "chunkId": chunk_id, "sourceId": transcription_source["sourceId"], "traditionId": "TAJIKA",
            "topic": source_pages[n][0], "text": text, "structuredFacts": {}, "ruleIds": [],
            "tags": ["VARSHAPHAL", "TAJIKA"] + (["MUNTHA"] if n != 112 else ["VARS HESHA".replace(" ", "")]),
            "language": "en", "page": n, "printedPage": source_pages[n][1], "chapter": None,
            "licenseStatus": "VERIFIED_CC_BY_SA_4_0", "attribution": transcription_source["attribution"],
            "version": "TAJIKA_V1", "checksum": checksum, "sourceRef": ref,
            "sourcePageProofreadStatus": pages[n]["transcriptionStatus"],
        })

    defs = [
        ("TAJIKA_MUNTHA_H3_001", 122, "7", "muntha_house", "3", "TRADITIONAL_INDICATION", "tajika.muntha.house.3", "The source associates Muntha in house 3 with favorable traditional themes including initiative, reputation, comfort, and support.", ["muntha_house"]),
        ("TAJIKA_MUNTHA_H4_001", 122, "8", "muntha_house", "4", "TRADITIONAL_INDICATION", "tajika.muntha.house.4", "The source associates Muntha in house 4 with challenging traditional themes, including distress and disruption of comfort.", ["muntha_house"]),
        ("TAJIKA_MUNTHA_H5_001", 122, "9", "muntha_house", "5", "TRADITIONAL_INDICATION", "tajika.muntha.house.5", "The source associates Muntha in house 5 with favorable traditional themes including judgment, happiness, and gain.", ["muntha_house"]),
        ("TAJIKA_MUNTHA_H6_001", 122, "10", "muntha_house", "6", "TRADITIONAL_INDICATION", "tajika.muntha.house.6", "The source associates Muntha in house 6 with challenging traditional themes.", ["muntha_house"]),
        ("TAJIKA_MUNTHA_AFFLICTION_001", 124, "17", "muntha_bhava_malefic_afflicted", "true", "QUALIFIER", "tajika.muntha.affliction", "The source says malefic occupation or hostile malefic aspect can suppress favorable results for the occupied Muntha house and increase adverse indications.", ["muntha_house", "malefic_occupation", "hostile_malefic_aspect"]),
        ("TAJIKA_MUNTHA_BENEFIC_001", 124, "18", "muntha_lord_strength", "strong", "QUALIFIER", "tajika.muntha.support", "The source says benefic association or aspect and a strong lord can support favorable indications for the Muntha house.", ["muntha_house", "muntha_lord_strength", "benefic_association", "benefic_aspect"]),
        ("TAJIKA_VARS HESHA_SUN_STRONG_001".replace(" ", ""), 112, "11", "varshesha_planet", "SUN", "ANNUAL_PLANETARY_INDICATION", "tajika.varshesha.sun.strong", "The source associates a strong Sun acting as Varshesha with favorable traditional themes; natal strength is stated to modify how completely these indications manifest.", ["varshesha_planet", "varshesha_strength", "natal_sun_strength"]),
        ("TAJIKA_VARS HESHA_SUN_MID_001".replace(" ", ""), 112, "12", "varshesha_planet", "SUN", "ANNUAL_PLANETARY_INDICATION", "tajika.varshesha.sun.middling", "The source describes a middling Sun as Varshesha as giving moderated or mixed traditional indications.", ["varshesha_planet", "varshesha_strength"]),
    ]
    rules = []
    for rid, n, verse, fact, value, result_type, key, meaning, inputs in defs:
        rule = {
            "ruleId": rid, "traditionId": "TAJIKA", "topic": "Muntha" if n != 112 else "Varshesha",
            "tags": ["VARSHAPHAL", "TAJIKA", "MUNTHA" if n != 112 else "VARS HESHA".replace(" ", "")],
            "conditions": [{"factKey": fact, "comparison": "EQUALS", "expectedValue": value, "evidenceRequirementId": f"input:{fact}"}],
            "inputs": inputs, "evidenceRequirements": [f"input:{v}" for v in inputs],
            "resultType": result_type, "interpretationKey": key,
            "sourceId": transcription_source["sourceId"], "sourceRefs": [source_ref(n, verse)],
            "chapter": source_pages[n][0], "page": n, "printedPage": source_pages[n][1], "verse": verse,
            "calculationReference": "Consumes explicit annual chart facts; does not calculate the solar return or Muntha position.",
            "knowledgeVersion": "TAJIKA_V1", "license": "CC BY-SA 4.0", "rightsStatus": "VERIFIED",
            "meaning": meaning,
        }
        rule["checksum"] = sha(canonical(rule))
        rules.append(rule)

    glossary = [
        {"term":"Muntha","traditionId":"TAJIKA","definition":"A Tajika annual-chart factor named by the cited source; this pack records source-specific house indications and does not assert equivalence with other traditions.","sourceRef":source_ref(122,"7-11"),"license":"CC BY-SA 4.0"},
        {"term":"Muntha phala","traditionId":"TAJIKA","definition":"The source section's term for Muntha-related indications.","sourceRef":source_ref(124,"17-20"),"license":"CC BY-SA 4.0"},
        {"term":"Bhava","traditionId":"TAJIKA","definition":"House, as used in the cited Hindi commentary.","sourceRef":source_ref(124,"17"),"license":"CC BY-SA 4.0"},
        {"term":"Krura graha","traditionId":"TAJIKA","definition":"A malefic or adverse planet in the terminology of the cited commentary.","sourceRef":source_ref(124,"17-18"),"license":"CC BY-SA 4.0"},
        {"term":"Shubha graha","traditionId":"TAJIKA","definition":"A benefic planet in the terminology of the cited commentary.","sourceRef":source_ref(124,"18"),"license":"CC BY-SA 4.0"},
        {"term":"Varshesha","traditionId":"TAJIKA","definition":"The annual year lord referred to in the cited commentary; this selected material includes only a short Sun-as-year-lord subsection.","sourceRef":source_ref(112,"11-13"),"license":"CC BY-SA 4.0"},
    ]
    pack = {
        "packId":"TAJIKA_V1", "version":"1.0.0", "locale":"en", "language":"en",
        "tradition":"TAJIKA", "status":"VERIFIED_SCOPED_CONTENT", "rightsStatus":"VERIFIED",
        "license":"CC BY-SA 4.0 for adaptations of the cited Wikisource transcription; source scan marked public domain in India and the United States",
        "attribution":transcription_source["attribution"], "sources":[scan_source,transcription_source],
        "sourceVersions":{scan_source["sourceId"]:scan_source["version"],transcription_source["sourceId"]:transcription_source["version"]},
        "sourceSections":["Muntha phala; source section title not established", "Varshesha Sun indications; heading not established"],
        "chapters":[], "pagesInspected":[112,122,124], "chunks":chunks, "rules":rules, "glossary":glossary,
        "buildTimestamp":"2026-10-01T13:16:48.087874Z",
    }
    pack["checksum"] = sha(canonical(pack))
    OUT.write_text(json.dumps(pack, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Built {OUT.name}: checksum={pack['checksum']} sources={len(pack['sources'])} chunks={len(chunks)} rules={len(rules)} glossary={len(glossary)}")


if __name__ == "__main__":
    main()
