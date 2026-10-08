plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "com.aynvora.sdk"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()
    }
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(project(":aynvora-contracts"))
            implementation(project(":aynvora-core"))
            compileOnly(project(":astro-engine"))
            compileOnly(project(":palmistry-engine"))
            compileOnly(project(":numerology-engine"))
            compileOnly(project(":tarot-engine"))
            compileOnly(project(":gemstone-engine"))
            compileOnly(project(":gita-engine"))
            compileOnly(project(":garuda-puran-engine"))
            compileOnly(project(":rudraksha-engine"))
            compileOnly(project(":jadi-engine"))
            compileOnly(project(":yantra-engine"))
            compileOnly(project(":guidance-engine"))
            compileOnly(project(":ai-engine"))
            compileOnly(project(":report-engine"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(project(":astro-engine"))
            implementation(project(":palmistry-engine"))
            implementation(project(":numerology-engine"))
            implementation(project(":tarot-engine"))
            implementation(project(":gemstone-engine"))
            implementation(project(":gita-engine"))
            implementation(project(":garuda-puran-engine"))
            implementation(project(":rudraksha-engine"))
            implementation(project(":jadi-engine"))
            implementation(project(":yantra-engine"))
            implementation(project(":guidance-engine"))
            implementation(project(":ai-engine"))
            implementation(project(":report-engine"))
        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(project(":astro-engine"))
            implementation(project(":palmistry-engine"))
            implementation(project(":numerology-engine"))
            implementation(project(":tarot-engine"))
            implementation(project(":gemstone-engine"))
            implementation(project(":gita-engine"))
            implementation(project(":garuda-puran-engine"))
            implementation(project(":rudraksha-engine"))
            implementation(project(":jadi-engine"))
            implementation(project(":yantra-engine"))
            implementation(project(":guidance-engine"))
            implementation(project(":ai-engine"))
            implementation(project(":report-engine"))
        }
    }
}
