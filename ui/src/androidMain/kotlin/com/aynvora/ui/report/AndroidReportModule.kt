package com.aynvora.ui.report

import com.aynvora.core.report.ReportLanguage
import com.aynvora.core.report.ReportPdfGenerator
import com.aynvora.core.report.ReportShareService
import com.aynvora.localization.report.AynvoraReportTextResolver
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/** Android host bindings. Request a language-specific PDF renderer with `parametersOf(language)`. */
val androidReportModule: Module = module {
    single<ReportShareService> { AndroidReportShareService(androidContext()) }
    factory<ReportPdfGenerator> { parameters ->
        AndroidReportPdfGenerator(
            AynvoraReportTextResolver(
                parameters.get<ReportLanguage>()
            )
        )
    }
}
