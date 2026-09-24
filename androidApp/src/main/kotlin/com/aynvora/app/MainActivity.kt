package com.aynvora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.aynvora.app.analytics.FirebaseAnalyticsTracker
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.ui.AynvoraApp

class MainActivity : ComponentActivity() {

    private lateinit var analyticsTracker: FirebaseAnalyticsTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analyticsTracker = FirebaseAnalyticsTracker(this)
        analyticsTracker.track(AnalyticsEvent.AppOpened)

        setContent { AynvoraApp() }
    }
}
