package com.aynvora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.aynvora.app.analytics.FirebaseAnalyticsTracker
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.ui.AynvoraApp
import com.aynvora.ui.di.aynvoraAppModules
import com.aynvora.ui.report.androidReportModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MainActivity : ComponentActivity() {

    private lateinit var analyticsTracker: AnalyticsTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Koin if not already started
        if (GlobalContext.getOrNull() == null) {
            val androidModule = module {
                single<AnalyticsTracker> { FirebaseAnalyticsTracker(this@MainActivity) }
            }
            startKoin {
                androidContext(this@MainActivity.applicationContext)
                modules(aynvoraAppModules + androidModule + androidReportModule)
            }
        }

        analyticsTracker = FirebaseAnalyticsTracker(this)
        analyticsTracker.track(AnalyticsEvent.AppOpened)

        setContent { AynvoraApp() }
    }
}
