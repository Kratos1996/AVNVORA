package com.aynvora.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.aynvora.ui.AynvoraApp

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "AYNVORA") {
        AynvoraApp()
    }
}
