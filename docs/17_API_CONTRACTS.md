# AYNVORA API Contracts
Version: 1.0

## Public API
Public APIs must be small, documented and stable.

## Rules
- Prefer immutable request/result models.
- Use explicit versioning for breaking changes.
- Do not expose internal implementation types unnecessarily.
- Validate inputs at API boundaries.
- Define error/result states rather than throwing uncontrolled exceptions for expected domain conditions.

## SDK Entry Point (`aynvora-core`)

The public SDK boundary is formalized in `:aynvora-core`. Consumers (UI, application layer, external clients) must never interact directly with internal modules such as `:astro-engine`.

### Entry Point Contract
```kotlin
val sdk = Aynvora.create()

val result: AynvoraResult<ChartResult> = sdk.calculateChart(
    BirthData(
        date = BirthDate(year = 1995, month = 5, day = 21),
        time = BirthTime(hour = 14, minute = 30, second = 0),
        place = BirthPlace(
            name = "Varanasi, India",
            coordinates = Coordinates(latitude = 25.3176, longitude = 82.9739),
            timezoneId = "Asia/Kolkata"
        )
    )
)
```

### Divisional Chart Contracts (Phase 5.3)
```kotlin
// Calculate single divisional chart (e.g. Navamsa D9)
val d9Result: AynvoraResult<DivisionalChartResult> = sdk.calculateDivisionalChart(
    request = chartRequest,
    chart = DivisionalChart.D9,
)

// Calculate multiple divisional charts in a coordinated pass
val vargas: AynvoraResult<Map<DivisionalChart, DivisionalChartResult>> = sdk.calculateDivisionalCharts(
    request = chartRequest,
    charts = setOf(DivisionalChart.D1, DivisionalChart.D9, DivisionalChart.D10),
)
```

### Planetary Dignities & Relationships Contracts (Phase 5.4)
```kotlin
// Calculate planetary dignities for D1 or any divisional chart (e.g. D9 Navamsa)
val dignities: AynvoraResult<List<PlanetaryDignity>> = sdk.calculateDignities(
    request = chartRequest,
    chart = DivisionalChart.D9, // Defaults to DivisionalChart.D1
)

// Calculate directional planetary relationships for D1 or any divisional chart
val relationships: AynvoraResult<List<PlanetaryRelationship>> = sdk.calculateRelationships(
    request = chartRequest,
    chart = DivisionalChart.D1,
)
```

### Shadbala Engine Contracts (Phase 5.5 & 5.6 Complete)
```kotlin
// Calculate Shadbala strengths for all celestial bodies
val shadbala: AynvoraResult<List<PlanetaryShadbala>> = sdk.calculateShadbala(
    request = chartRequest,
)

// Shadbala is also included directly in the full ChartResult:
val chartResult = sdk.calculateChart(chartRequest)
if (chartResult is AynvoraResult.Success) {
    val strengths: List<PlanetaryShadbala> = chartResult.value.shadbala
    for (planet in strengths) {
        println("Body: ${planet.body}, Completeness: ${planet.completeness}")
        println("  Sthana Bala: ${planet.sthanaBala.totalVirupas} Virupas (${planet.sthanaBala.totalRupas} Rupas)")
        println("  Dig Bala: ${planet.digBala.virupas} Virupas (${planet.digBala.rupas} Rupas)")
        println("  Kala Bala: ${planet.kalaBala.totalVirupas} Virupas (${planet.kalaBala.totalRupas} Rupas)")
        println("  Chesta Bala: ${planet.chestaBala.virupas} Virupas (${planet.chestaBala.rupas} Rupas)")
        println("  Naisargika Bala: ${planet.naisargikaBala.virupas} Virupas (Rank ${planet.naisargikaBala.rank})")
        println("  Drik Bala: ${planet.drikBala.virupas} Virupas (${planet.drikBala.rupas} Rupas)")
        println("  Total Virupas: ${planet.totalVirupas}, Total Rupas: ${planet.totalRupas}")
        // For Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn:
        // completeness = COMPLETE, isComplete = true, totalVirupas != null
        // For Rahu, Ketu:
        // completeness = UNSUPPORTED, isComplete = false, totalVirupas = null
    }
}
```

### Ashtakavarga Engine Contracts (Phase 6.1)
```kotlin
// Calculate standalone Ashtakavarga (Bhinnashtakavarga and Sarvashtakavarga)
val ashtakavargaResult: AynvoraResult<AshtakavargaResult> = sdk.calculateAshtakavarga(chartRequest)
if (ashtakavargaResult is AynvoraResult.Success) {
    val av = ashtakavargaResult.value
    println("Ruleset: ${av.rulesetId}, Completeness: ${av.completeness}")
    println("SAV Grand Total Bindus: ${av.sarvashtakavarga.grandTotalBindus}") // strictly 337
    println("SAV Invariant Valid: ${av.sarvashtakavarga.isInvariantValid}") // true

    // Inspect individual planetary BAV:
    val sunBav = av.bhinnashtakavarga[CelestialBody.SUN]
    println("Sun BAV Total Bindus: ${sunBav?.totalBindus}") // 48
    println("Sun BAV Total Rekhas: ${sunBav?.totalRekhas}") // 48

    // Inspect Sarvashtakavarga sign scores (12 signs, Mesha to Meena):
    for (signScore in av.sarvashtakavarga.signScores) {
        println("Sign: ${signScore.rashi.displayName}, Bindus: ${signScore.totalBindus}, Rekhas: ${signScore.totalRekhas}")
    }
}

// Ashtakavarga is also available directly in the full ChartResult:
val chartResult = sdk.calculateChart(chartRequest)
if (chartResult is AynvoraResult.Success) {
    val av: AshtakavargaResult? = chartResult.value.ashtakavarga
    println("SAV Total from Chart: ${av?.sarvashtakavarga?.grandTotalBindus}")
    val shodhita: ShodhitaAshtakavargaResult? = chartResult.value.shodhitaAshtakavarga
    println("Shodhita SAV Total: ${shodhita?.shodhitaSarvashtakavarga?.grandTotalShodhitaBindus}")
}
```

### Ashtakavarga Shodhana Contracts (Phase 6.2)
```kotlin
// Calculate standalone Shodhita Ashtakavarga (Trikona & Ekadhipatya Shodhana + Reduced SAV)
val shodhitaResult: AynvoraResult<ShodhitaAshtakavargaResult> = sdk.calculateShodhitaAshtakavarga(chartRequest)
if (shodhitaResult is AynvoraResult.Success) {
    val shodhita = shodhitaResult.value
    println("Ruleset: ${shodhita.rulesetId}, Completeness: ${shodhita.completeness}")

    // Inspect individual planetary Shodhita BAV:
    val sunShodhita = shodhita.shodhitaBhinnashtakavarga[CelestialBody.SUN]
    println("Sun Raw Bindus: ${sunShodhita?.rawTotalBindus}") // 48
    println("Sun Trikona Bindus: ${sunShodhita?.trikonaTotalBindus}")
    println("Sun Shodhita Bindus: ${sunShodhita?.shodhitaTotalBindus}")

    // Inspect Shodhita Sarvashtakavarga:
    val sav = shodhita.shodhitaSarvashtakavarga
    println("SAV Raw Grand Total: ${sav.grandTotalRawBindus}") // 337
    println("SAV Trikona Grand Total: ${sav.grandTotalTrikonaBindus}")
    println("SAV Shodhita Grand Total: ${sav.grandTotalShodhitaBindus}")

    // Sign-by-sign scores:
    for (score in sav.signScores) {
        println("${score.rashi.displayName}: Raw=${score.rawTotalBindus}, Trikona=${score.trikonaTotalBindus}, Shodhita=${score.shodhitaTotalBindus}")
    }
}
```

### Ashtakavarga Pinda Contracts (Phase 6.3)
```kotlin
// Calculate standalone Ashtakavarga Pindas (Rashi Pinda, Graha Pinda, Shodhya Pinda)
val pindaResult: AynvoraResult<AshtakavargaPinda> = sdk.calculateAshtakavargaPinda(chartRequest)
if (pindaResult is AynvoraResult.Success) {
    val pinda = pindaResult.value
    println("Ruleset: ${pinda.rulesetId}, Completeness: ${pinda.completeness}")
    println("Total Rasi Pinda: ${pinda.totalRasiPinda}")
    println("Total Graha Pinda: ${pinda.totalGrahaPinda}")
    println("Total Shodhya Pinda: ${pinda.totalShodhyaPinda}")

    // Inspect individual planetary Pinda:
    val sunPinda = pinda.planetaryPindas[CelestialBody.SUN]
    println("Sun Rasi Pinda: ${sunPinda?.rasiPinda}")
    println("Sun Graha Pinda: ${sunPinda?.grahaPinda}")
    println("Sun Shodhya Pinda: ${sunPinda?.shodhyaPinda}")

    // Sign contributions (bindus in sign * sign multiplier):
    for ((rashi, contrib) in sunPinda?.rasiContributions.orEmpty()) {
        println("${rashi.displayName}: $contrib")
    }

    // Graha contributions (bindus in occupied sign * planet multiplier):
    for ((graha, contrib) in sunPinda?.grahaContributions.orEmpty()) {
        println("${graha.name}: $contrib")
    }
}

// Ashtakavarga Pinda is also available directly in the full ChartResult:
val chartResult = sdk.calculateChart(chartRequest)
if (chartResult is AynvoraResult.Success) {
    val pindaDirect: AshtakavargaPinda? = chartResult.value.ashtakavargaPinda
    val pindaFromAv: AshtakavargaPinda? = chartResult.value.ashtakavarga?.pinda
    println("Total Shodhya Pinda: ${pindaDirect?.totalShodhyaPinda}")
}
```

### Capability Discovery
```kotlin
val metadata: EngineMetadata = sdk.getMetadata()
```


### Deterministic Result Contract
All operations return `AynvoraResult<T>`:
- `AynvoraResult.Success(value, metadata)`
- `AynvoraResult.Failure`:
  - `InvalidInput(field, message)`
  - `UnsupportedConfiguration(message)`
  - `CalculationFailure(code, message)`
  - `InternalFailure(message)`

No unhandled exceptions are thrown across the public SDK boundary for expected domain conditions.

