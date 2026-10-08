pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AYNVORA-SDK"
include(
    ":aynvora-contracts",
    ":aynvora-core",
    ":aynvora-sdk",
    ":aynvora-data",
    ":design-system",
    ":astro-engine",
    ":palmistry-engine",
    ":numerology-engine",
    ":tarot-engine",
    ":gemstone-engine",
    ":gita-engine",
    ":garuda-puran-engine",
    ":rudraksha-engine",
    ":jadi-engine",
    ":yantra-engine",
    ":guidance-engine",
    ":ai-engine",
    ":report-engine",
    ":aynvora-localization",
    ":aynvora-navigation",
    ":ui",
    ":aynvora-qa-core",
    ":aynvora-qa-android",
    ":androidApp",
    ":desktopApp",
    ":samples:astrology-only-app",
    ":samples:palmistry-only-app",
    ":samples:multi-feature-app",
    ":samples:headless-json-app",
    ":samples:official-ui-app"
)

project(":astro-engine").projectDir = file("astro-engine")
project(":palmistry-engine").projectDir = file("engines/palmistry-engine")
project(":numerology-engine").projectDir = file("engines/numerology-engine")
project(":tarot-engine").projectDir = file("engines/tarot-engine")
project(":gemstone-engine").projectDir = file("engines/gemstone-engine")
project(":gita-engine").projectDir = file("engines/gita-engine")
project(":garuda-puran-engine").projectDir = file("engines/garuda-puran-engine")
project(":rudraksha-engine").projectDir = file("engines/rudraksha-engine")
project(":jadi-engine").projectDir = file("engines/jadi-engine")
project(":yantra-engine").projectDir = file("engines/yantra-engine")
project(":guidance-engine").projectDir = file("engines/guidance-engine")
project(":ai-engine").projectDir = file("engines/ai-engine")
project(":report-engine").projectDir = file("report-engine")
project(":aynvora-sdk").projectDir = file("aynvora-sdk")

project(":samples:astrology-only-app").projectDir = file("samples/astrology-only-app")
project(":samples:palmistry-only-app").projectDir = file("samples/palmistry-only-app")
project(":samples:multi-feature-app").projectDir = file("samples/multi-feature-app")
project(":samples:headless-json-app").projectDir = file("samples/headless-json-app")
project(":samples:official-ui-app").projectDir = file("samples/official-ui-app")
