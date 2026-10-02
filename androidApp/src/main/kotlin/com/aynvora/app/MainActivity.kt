package com.aynvora.app

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.StatFs
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.aynvora.app.analytics.FirebaseAnalyticsTracker
import com.aynvora.app.storage.AndroidPreferencesStorageDriver
import com.aynvora.core.ai.ActualAndroidDeviceProfile
import com.aynvora.core.ai.AiAcceleratorType
import com.aynvora.core.ai.AiDeviceCapabilityDetector
import com.aynvora.core.ai.AiDeviceProfile
import com.aynvora.core.ai.AiRuntimeType
import com.aynvora.core.ai.CpuArchitecture
import com.aynvora.core.ai.OsPlatform
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.data.database.AynvoraDatabase
import com.aynvora.data.database.AynvoraDatabaseMigrations
import com.aynvora.core.models.OfflineLocationCatalog
import com.aynvora.data.gita.GitaDataSeeder
import com.aynvora.data.storage.StorageDriver
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
                single {
                    val context = this@MainActivity.applicationContext
                    val path = "composeResources/com.aynvora.designsystem.generated.resources/files/locations.tsv"
                    context.assets.open(path).bufferedReader().use { reader ->
                        OfflineLocationCatalog.parse(reader.readText())
                    }
                }
                single<StorageDriver> {
                    AndroidPreferencesStorageDriver(this@MainActivity.applicationContext)
                }
                single<AynvoraDatabase> {
                    val dbFile = applicationContext.getDatabasePath(AynvoraDatabase.DATABASE_NAME)
                    dbFile.parentFile?.mkdirs()
                    Room.databaseBuilder<AynvoraDatabase>(
                        context = applicationContext,
                        name = dbFile.absolutePath,
                    )
                        .addMigrations(*AynvoraDatabaseMigrations.ALL)
                        .setDriver(BundledSQLiteDriver())
                        .setQueryCoroutineContext(Dispatchers.IO)
                        .build()
                }
                single<AiDeviceCapabilityDetector> {
                    object : AiDeviceCapabilityDetector {
                        override suspend fun detectCapability(): AiDeviceProfile {
                            val context = this@MainActivity.applicationContext
                            val activityManager =
                                context.getSystemService(ACTIVITY_SERVICE) as? ActivityManager
                            val memInfo = ActivityManager.MemoryInfo()
                            val totalRam: Long
                            val availRam: Long
                            if (activityManager != null) {
                                activityManager.getMemoryInfo(memInfo)
                                totalRam = memInfo.totalMem
                                availRam = memInfo.availMem
                            } else {
                                totalRam =
                                    ActualAndroidDeviceProfile.TOTAL_RAM_BYTES
                                availRam =
                                    ActualAndroidDeviceProfile.AVAILABLE_RAM_BYTES
                            }

                            val filesDir = context.filesDir
                            val stat = StatFs(filesDir.absolutePath)
                            val freeStorage = stat.availableBytes
                            val totalStorage = stat.totalBytes

                            val primaryAbi =
                                Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
                            val cpuArch =
                                if (primaryAbi.contains("arm64") || primaryAbi.contains("aarch64")) {
                                    CpuArchitecture.ARM64
                                } else {
                                    CpuArchitecture.X86_64
                                }

                            return AiDeviceProfile(
                                totalRamBytes = totalRam,
                                availableRamBytes = availRam,
                                freeStorageBytes = freeStorage,
                                cpuArchitecture = cpuArch,
                                osPlatform = OsPlatform.ANDROID,
                                osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                                supportedRuntimes = setOf(
                                    AiRuntimeType.GGUF,
                                    AiRuntimeType.DETERMINISTIC_FALLBACK,
                                ),
                                supportedAccelerators = setOf(
                                    AiAcceleratorType.CPU,
                                    AiAcceleratorType.GPU,
                                ),
                                supportedLanguages = setOf("en", "hi", "ar"),
                                maxSafeRamAllocationBytes = AiDeviceProfile.calculateSafeRamAllocation(
                                    availRam
                                ),
                                manufacturer = Build.MANUFACTURER,
                                modelName = Build.MODEL,
                                cpuAbi = primaryAbi,
                                sdkInt = Build.VERSION.SDK_INT,
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
                    Log.i(
                        "AynvoraGita",
                        "Bhagavad Gita seeded: 701 verses + translations + commentaries."
                    )
                } else {
                    Log.d("AynvoraGita", "Bhagavad Gita already seeded — skipped.")
                }
            }.onFailure { e ->
                Log.e("AynvoraGita", "Gita seeding failed: ${e.message}", e)
            }
        }

        analyticsTracker = FirebaseAnalyticsTracker(this)
        analyticsTracker.track(AnalyticsEvent.AppOpened)

        // Register broadcast receiver for autonomous on-device AI testing (Phase 10.12)
        val filter = IntentFilter("com.aynvora.app.RUN_AI_TEST")
        ContextCompat.registerReceiver(
            this,
            aiTestReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )

        setContent { AynvoraApp() }
    }

    private val aiTestReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.aynvora.app.RUN_AI_TEST") {
                val testType = intent.getStringExtra("test") ?: "all"
                appScope.launch {
                    try {
                        Log.i("AynvoraAiTestResult", "Received broadcast to run AI tests: $testType")
                        val runner = AynvoraNativeAiTestRunner(this@MainActivity.applicationContext)
                        runner.runTests(testType)
                    } catch (t: Throwable) {
                        Log.e("AynvoraAiTestResult", "Error running AI tests: ${t.message}", t)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(aiTestReceiver)
        } catch (_: Throwable) {
        }
    }
}
