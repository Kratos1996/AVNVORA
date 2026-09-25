package com.aynvora.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.aynvora.data.database.AynvoraDatabase
import com.aynvora.ui.AynvoraApp
import com.aynvora.ui.di.aynvoraAppModules
import com.aynvora.ui.report.jvmReportModule
import kotlinx.coroutines.Dispatchers
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File

fun initDesktopSqlite() {
    val nativeDir = File(System.getProperty("user.home"), ".aynvora/natives/osx_x64")
    if (!nativeDir.exists()) nativeDir.mkdirs()
    val nativeLib = File(nativeDir, "libsqliteJni.dylib")

    // Copy bundled native binary from resources if missing
    val resourceStream =
        Thread.currentThread().contextClassLoader.getResourceAsStream("natives/osx_x64/libsqliteJni.dylib")
    if (resourceStream != null) {
        resourceStream.use { input ->
            nativeLib.outputStream().use { output -> input.copyTo(output) }
        }
    }

    if (nativeLib.exists()) {
        System.setProperty("androidx.sqlite.driver.bundled.path", nativeDir.absolutePath)
    }
}

val jvmDataModule = module {
    single<AynvoraDatabase> {
        val dbFile =
            File(System.getProperty("user.home"), ".aynvora/${AynvoraDatabase.DATABASE_NAME}")
        dbFile.parentFile?.mkdirs()
        Room.databaseBuilder<AynvoraDatabase>(
            name = dbFile.absolutePath,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}

fun main() {
    initDesktopSqlite()

    startKoin {
        modules(aynvoraAppModules + jvmReportModule + jvmDataModule)
    }

    application {
        Window(onCloseRequest = ::exitApplication, title = "AYNVORA") {
            AynvoraApp()
        }
    }
}
