package com.aynvora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aynvora.app.analytics.FirebaseAnalyticsTracker
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.ui.AynvoraApp
import com.aynvora.ui.di.aynvoraAppModules
import com.aynvora.ui.report.androidReportModule
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.aynvora.data.database.AynvoraDatabase
import com.aynvora.data.gita.GitaDataSeeder
import com.aynvora.qa.android.AndroidInteractionSentinelRuntime
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
                        .setDriver(BundledSQLiteDriver())
                        .setQueryCoroutineContext(Dispatchers.IO)
                        .build()
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
