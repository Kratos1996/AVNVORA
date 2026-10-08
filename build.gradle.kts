plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

val publishableProjects = setOf(
    "aynvora-contracts",
    "aynvora-core",
    "aynvora-sdk",
    "astro-engine",
    "palmistry-engine",
    "numerology-engine",
    "tarot-engine",
    "gemstone-engine",
    "gita-engine",
    "garuda-puran-engine",
    "rudraksha-engine",
    "jadi-engine",
    "yantra-engine",
    "guidance-engine",
    "ai-engine",
    "report-engine",
    "aynvora-data",
    "aynvora-localization",
    "design-system",
    "ui"
)

subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
            jvmToolchain(21)
        }
    }

    if (name in publishableProjects) {
        apply(plugin = "maven-publish")
        group = "com.aynvora"
        version = "10.46.0"

        plugins.withId("maven-publish") {
            configure<PublishingExtension> {
                publications.withType<MavenPublication> {
                    pom {
                        name.set(project.name)
                        description.set("AYNVORA Modular SDK - ${project.name}")
                        url.set("https://github.com/Kratos1996/AVNVORA")
                        licenses {
                            license {
                                name.set("Apache-2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                id.set("aynvora")
                                name.set("AYNVORA Engineering Team")
                                email.set("dev@aynvora.com")
                            }
                        }
                        scm {
                            connection.set("scm:git:git://github.com/Kratos1996/AVNVORA.git")
                            developerConnection.set("scm:git:ssh://github.com:Kratos1996/AVNVORA.git")
                            url.set("https://github.com/Kratos1996/AVNVORA")
                        }
                    }
                }
            }
        }
    }
}
