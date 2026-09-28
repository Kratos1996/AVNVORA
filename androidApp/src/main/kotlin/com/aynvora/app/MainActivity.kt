package com.aynvora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.aynvora.app.analytics.FirebaseAnalyticsTracker
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.data.database.AynvoraDatabase
import com.aynvora.data.gita.GitaDataSeeder
import com.aynvora.qa.android.AndroidInteractionSentinelRuntime
import com.aynvora.ui.AynvoraApp
import com.aynvora.ui.di.aynvoraAppModules
import com.aynvora.ui.report.androidReportModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MainActivity : ComponentActivity() {

    private lateinit var analyticsTracker: AnalyticsTracker

    /** App-scoped coroutine scope for background seeding tasks. Cancelled with the process. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Koin if not already started
        if (GlobalContext.getOrNull() == null) {
            val androidModule = module {
                single<AnalyticsTracker> { FirebaseAnalyticsTracker(this@MainActivity) }
                single<com.aynvora.data.storage.StorageDriver> {
                    com.aynvora.app.storage.AndroidPreferencesStorageDriver(this@MainActivity.applicationContext)
                }
                single<AynvoraDatabase> {
                    val dbFile = applicationContext.getDatabasePath(AynvoraDatabase.DATABASE_NAME)
                    dbFile.parentFile?.mkdirs()
                    Room.databaseBuilder<AynvoraDatabase>(
                        context = applicationContext,
                        name = dbFile.absolutePath,
                    )
                        .fallbackToDestructiveMigration(true)
                        .setDriver(BundledSQLiteDriver())
                        .setQueryCoroutineContext(Dispatchers.IO)
                        .build()
                }
                single<com.aynvora.core.ai.AiDeviceCapabilityDetector> {
                    object : com.aynvora.core.ai.AiDeviceCapabilityDetector {
                        override suspend fun detectCapability(): com.aynvora.core.ai.AiDeviceProfile {
                            val context = this@MainActivity.applicationContext
                            val activityManager =
                                context.getSystemService(android.content.Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
                            val memInfo = android.app.ActivityManager.MemoryInfo()
                            val totalRam: Long
                            val availRam: Long
                            if (activityManager != null) {
                                activityManager.getMemoryInfo(memInfo)
                                totalRam = memInfo.totalMem
                                availRam = memInfo.availMem
                            } else {
                                totalRam =
                                    com.aynvora.core.ai.ActualAndroidDeviceProfile.TOTAL_RAM_BYTES
                                availRam =
                                    com.aynvora.core.ai.ActualAndroidDeviceProfile.AVAILABLE_RAM_BYTES
                            }

                            val filesDir = context.filesDir
                            val stat = android.os.StatFs(filesDir.absolutePath)
                            val freeStorage = stat.availableBytes
                            val totalStorage = stat.totalBytes

                            val primaryAbi =
                                android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
                            val cpuArch =
                                if (primaryAbi.contains("arm64") || primaryAbi.contains("aarch64")) {
                                    com.aynvora.core.ai.CpuArchitecture.ARM64
                                } else {
                                    com.aynvora.core.ai.CpuArchitecture.X86_64
                                }

                            return com.aynvora.core.ai.AiDeviceProfile(
                                totalRamBytes = totalRam,
                                availableRamBytes = availRam,
                                freeStorageBytes = freeStorage,
                                cpuArchitecture = cpuArch,
                                osPlatform = com.aynvora.core.ai.OsPlatform.ANDROID,
                                osVersion = "Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})",
                                supportedRuntimes = setOf(
                                    com.aynvora.core.ai.AiRuntimeType.GGUF,
                                    com.aynvora.core.ai.AiRuntimeType.DETERMINISTIC_FALLBACK,
                                ),
                                supportedAccelerators = setOf(
                                    com.aynvora.core.ai.AiAcceleratorType.CPU,
                                    com.aynvora.core.ai.AiAcceleratorType.GPU,
                                ),
                                supportedLanguages = setOf("en", "hi", "ar"),
                                maxSafeRamAllocationBytes = com.aynvora.core.ai.AiDeviceProfile.calculateSafeRamAllocation(
                                    availRam
                                ),
                                manufacturer = android.os.Build.MANUFACTURER,
                                modelName = android.os.Build.MODEL,
                                cpuAbi = primaryAbi,
                                sdkInt = android.os.Build.VERSION.SDK_INT,
                                totalStorageBytes = totalStorage,
                            )
                        }
                    }
                }
            }
            startKoin {
                androidContext(this@MainActivity.applicationContext)
                modules(aynvoraAppModules + androidModule + androidReportModule)
            }
        }

        // Initialize Interaction Sentinel runtime
        AndroidInteractionSentinelRuntime.initialize()

        // Phase 8.3 — Seed Bhagavad Gita data into Room on first launch (background, idempotent)
        appScope.launch {
            runCatching {
                val database = GlobalContext.get().get<AynvoraDatabase>()
                val seeder = GitaDataSeeder(
                    context = applicationContext,
                    dao = database.gitaDao(),
                )
                val seeded = seeder.seedIfNeeded()
                if (seeded) {
                    android.util.Log.i(
                        "AynvoraGita",
                        "Bhagavad Gita seeded: 701 verses + translations + commentaries."
                    )
                } else {
                    android.util.Log.d("AynvoraGita", "Bhagavad Gita already seeded — skipped.")
                }
            }.onFailure { e ->
                android.util.Log.e("AynvoraGita", "Gita seeding failed: ${e.message}", e)
            }
        }

        analyticsTracker = FirebaseAnalyticsTracker(this)
        analyticsTracker.track(AnalyticsEvent.AppOpened)

        setContent { AynvoraApp() }
    }
}
