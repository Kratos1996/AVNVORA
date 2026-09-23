plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()
    sourceSets {
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(project(":ui"))
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.aynvora.desktop.MainKt"
    }
}
