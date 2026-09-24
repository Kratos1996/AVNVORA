package com.aynvora.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.aynvora.ui.AynvoraApp
import com.aynvora.ui.di.aynvoraAppModules
import com.aynvora.ui.report.jvmReportModule
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(aynvoraAppModules + jvmReportModule)
    }

    application {
        Window(onCloseRequest = ::exitApplication, title = "AYNVORA") {
            AynvoraApp()
        }
    }
}
