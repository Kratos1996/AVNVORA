package com.aynvora.ui.report

import com.aynvora.core.report.ReportLanguage
import com.aynvora.core.report.ReportPdfGenerator
import com.aynvora.core.report.ReportShareService
import com.aynvora.localization.report.AynvoraReportTextResolver
import org.koin.core.module.Module
import org.koin.dsl.module

/** Desktop host bindings. Request a language-specific PDF renderer with `parametersOf(language)`. */
val jvmReportModule: Module = module {
    factory<ReportShareService> { parameters ->
        JvmReportShareService(
            AynvoraReportTextResolver(
                parameters.get<ReportLanguage>()
            )
        )
    }
    factory<ReportPdfGenerator> { parameters ->
        JvmReportPdfGenerator(
            AynvoraReportTextResolver(
                parameters.get<ReportLanguage>()
            )
        )
    }
}
