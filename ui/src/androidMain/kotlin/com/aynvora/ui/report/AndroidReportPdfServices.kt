package com.aynvora.ui.report

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.aynvora.core.report.ReportDocument
import com.aynvora.core.report.ReportPdfArtifact
import com.aynvora.core.report.ReportPdfError
import com.aynvora.core.report.ReportPdfGenerator
import com.aynvora.core.report.ReportPdfResult
import com.aynvora.core.report.ReportShareResult
import com.aynvora.core.report.ReportShareService
import com.aynvora.core.report.ReportTextResolver
import java.io.ByteArrayOutputStream
import java.io.File

/** Android-native PDF renderer. It only draws the existing report model; it performs no calculations. */
class AndroidReportPdfGenerator(
    private val resolver: ReportTextResolver,
) : ReportPdfGenerator {
    override fun generate(document: ReportDocument): ReportPdfResult = try {
        val pdf = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 42f
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(34, 39, 52); textSize = 11f; typeface =
            Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(85, 92, 105); textSize = 9f; typeface =
            Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(138, 99, 25); textSize = 16f; typeface =
            Typeface.create("sans-serif-medium", Typeface.BOLD)
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(138, 99, 25); textSize = 22f; typeface =
            Typeface.create("sans-serif-medium", Typeface.BOLD)
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(220, 224, 230)
            strokeWidth = 0.8f
        }
        val goldLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(180, 140, 45)
            strokeWidth = 1.2f
        }

        var pageNumber = 0
        var page: PdfDocument.Page? = null
        var canvas: android.graphics.Canvas? = null
        var y = margin

        fun drawFooter() {
            canvas?.let { c ->
                c.drawLine(margin, pageHeight - margin + 8f, pageWidth - margin, pageHeight - margin + 8f, linePaint)
                c.drawText("AYNVORA • Page $pageNumber", margin, pageHeight - margin + 22f, metaPaint)
            }
        }

        fun startPage() {
            drawFooter()
            page?.let(pdf::finishPage)
            pageNumber += 1
            page = pdf.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            )
            canvas = page!!.canvas
            canvas!!.drawColor(android.graphics.Color.WHITE)
            y = margin

            if (pageNumber > 1) {
                canvas!!.drawText(document.title.value, margin, margin - 14f, metaPaint)
                canvas!!.drawLine(margin, margin - 8f, pageWidth - margin, margin - 8f, linePaint)
                y = margin + 10f
            }
        }

        fun draw(line: ReportPdfLine) {
            val paint = when (line.kind) {
                ReportPdfLineKind.TITLE -> titlePaint
                ReportPdfLineKind.HEADING, ReportPdfLineKind.SUBHEADING, ReportPdfLineKind.TABLE_HEADER -> headingPaint
                ReportPdfLineKind.METADATA -> metaPaint
                ReportPdfLineKind.BODY -> bodyPaint
            }
            val lineHeight = when (line.kind) {
                ReportPdfLineKind.TITLE -> 30f
                ReportPdfLineKind.HEADING -> 24f
                ReportPdfLineKind.SUBHEADING -> 20f
                ReportPdfLineKind.TABLE_HEADER -> 18f
                else -> 16f
            }
            val textLines = wrap(line.text, paint, pageWidth - margin * 2)
            textLines.forEach { text ->
                if (y + lineHeight > pageHeight - margin - 16f) startPage()
                canvas!!.drawText(text, margin, y, paint)
                y += lineHeight
            }
            if (line.kind == ReportPdfLineKind.HEADING || line.kind == ReportPdfLineKind.TITLE) {
                y += 2f
                canvas!!.drawLine(margin, y, margin + 60f, y, goldLinePaint)
                y += 6f
            }
        }

        startPage()
        document.toReportPdfLines(resolver).forEach(::draw)
        drawFooter()
        page?.let(pdf::finishPage)
        val output = ByteArrayOutputStream()
        pdf.writeTo(output)
        pdf.close()
        val bytes = output.toByteArray()
        if (bytes.isEmpty()) ReportPdfResult.Failed(ReportPdfError.Failed("empty_pdf"))
        else ReportPdfResult.Generated(
            ReportPdfArtifact(
                fileName = "${safeSlug(document.metadata.reportTypeId)}-${document.metadata.generatedAtEpochMs}.pdf",
                bytes = bytes
            )
        )
    } catch (_: Exception) {
        ReportPdfResult.Failed(ReportPdfError.Failed("android_pdf_render_failed"))
    }

    private fun wrap(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return listOf("")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        text.split(Regex("\\s+")).forEach { word ->
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (current.isNotEmpty() && paint.measureText(candidate) > maxWidth) {
                lines += current.toString()
                current = StringBuilder(word)
            } else current = StringBuilder(candidate)
        }
        if (current.isNotEmpty()) lines += current.toString()
        return lines
    }

    private fun safeSlug(value: String) =
        value.lowercase().replace(Regex("[^a-z0-9_-]"), "-").trim('-').ifBlank { "report" }
}

/** Uses a cache-scoped FileProvider URI so report files are not exposed through file:// paths. */
class AndroidReportShareService(
    private val context: Context,
    private val authority: String = "${context.packageName}.report-files",
) : ReportShareService {
    override fun share(artifact: ReportPdfArtifact): ReportShareResult = try {
        val directory = File(context.cacheDir, "shared-reports").apply { mkdirs() }
        val name =
            artifact.fileName.substringAfterLast('/').replace(Regex("[^A-Za-z0-9._-]"), "_").let {
                if (it.endsWith(".pdf", ignoreCase = true)) it else "$it.pdf"
            }
        val file = File(directory, name)
        file.writeBytes(artifact.bytes)
        val uri = FileProvider.getUriForFile(context, authority, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = artifact.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (context !is android.app.Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, null))
        ReportShareResult.Shared
    } catch (_: Exception) {
        ReportShareResult.Failed("android_share_failed")
    }
}
