plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidLibrary {
        namespace = "com.aynvora.ui"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()
    }
    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(project(":design-system"))
            implementation(project(":aynvora-core"))
            implementation(project(":aynvora-data"))
            implementation(project(":aynvora-localization"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.cottonsheet)
            implementation(libs.popbox)
        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
