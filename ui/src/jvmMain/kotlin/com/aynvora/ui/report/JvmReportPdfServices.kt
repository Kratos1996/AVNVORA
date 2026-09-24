package com.aynvora.ui.report

import com.aynvora.core.report.ReportDocument
import com.aynvora.core.report.ReportPdfArtifact
import com.aynvora.core.report.ReportPdfError
import com.aynvora.core.report.ReportPdfGenerator
import com.aynvora.core.report.ReportPdfResult
import com.aynvora.core.report.ReportShareResult
import com.aynvora.core.report.ReportShareService
import com.aynvora.core.report.ReportTextKey
import com.aynvora.core.report.ReportTextResolver
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/** Dependency-free JVM PDF renderer. Pages are rasterized to retain Hindi glyph shaping via desktop fonts. */
class JvmReportPdfGenerator(
    private val resolver: ReportTextResolver,
) : ReportPdfGenerator {
    override fun generate(document: ReportDocument): ReportPdfResult = try {
        val lines = document.toReportPdfLines(resolver)
        val textForFont = lines.joinToString("\n") { it.text }
        val baseFont = loadSupportedFont(textForFont)
        val pages = renderPages(lines, baseFont)
        val imageBytes = pages.map { image ->
            ByteArrayOutputStream().use { output ->
                if (!ImageIO.write(image, "jpg", output)) error("jpeg_writer_unavailable")
                output.toByteArray()
            }
        }
        val bytes = createPdf(imageBytes)
        if (bytes.isEmpty()) ReportPdfResult.Failed(ReportPdfError.Failed("empty_pdf"))
        else ReportPdfResult.Generated(
            ReportPdfArtifact(
                fileName = "${safeSlug(document.metadata.reportTypeId)}-${document.metadata.generatedAtEpochMs}.pdf",
                bytes = bytes,
            ),
        )
    } catch (_: Exception) {
        ReportPdfResult.Failed(ReportPdfError.Failed("jvm_pdf_render_failed"))
    }

    private fun loadSupportedFont(text: String): Font {
        val embedded = JvmReportPdfGenerator::class.java.classLoader
            .getResourceAsStream("composeResources/aynvora_sdk.design_system.generated.resources/font/notosans_regular.ttf")
            ?.use { Font.createFont(Font.TRUETYPE_FONT, it) }
        if (embedded != null && embedded.canDisplayUpTo(text) == -1) return embedded
        val installed = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().allFonts
            .firstOrNull { it.canDisplayUpTo(text) == -1 }
        return installed ?: Font(Font.SANS_SERIF, Font.PLAIN, 12)
    }

    private fun renderPages(lines: List<ReportPdfLine>, baseFont: Font): List<BufferedImage> {
        val pageWidth = 1190
        val pageHeight = 1684
        val margin = 92
        val output = mutableListOf<BufferedImage>()
        var image: BufferedImage? = null
        var graphics: Graphics2D? = null
        var y = margin
        fun newPage() {
            graphics?.dispose()
            image = BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_RGB)
            graphics = image!!.createGraphics().apply {
                setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                )
                setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                color = Color.WHITE
                fillRect(0, 0, pageWidth, pageHeight)
            }
            y = margin
        }
        newPage()
        lines.forEach { line ->
            val style = if (line.kind in setOf(
                    ReportPdfLineKind.TITLE,
                    ReportPdfLineKind.HEADING,
                    ReportPdfLineKind.SUBHEADING,
                    ReportPdfLineKind.TABLE_HEADER
                )
            ) Font.BOLD else Font.PLAIN
            val size = when (line.kind) {
                ReportPdfLineKind.TITLE -> 44
                ReportPdfLineKind.HEADING -> 32
                ReportPdfLineKind.SUBHEADING -> 27
                ReportPdfLineKind.BODY, ReportPdfLineKind.TABLE_HEADER -> 24
                ReportPdfLineKind.METADATA -> 20
            }
            val textFont = baseFont.deriveFont(style, size.toFloat())
            val g = graphics!!
            g.font = textFont
            g.color = when (line.kind) {
                ReportPdfLineKind.TITLE, ReportPdfLineKind.HEADING, ReportPdfLineKind.SUBHEADING, ReportPdfLineKind.TABLE_HEADER -> Color(
                    133,
                    94,
                    28
                )

                ReportPdfLineKind.METADATA -> Color(89, 96, 108)
                ReportPdfLineKind.BODY -> Color(34, 39, 52)
            }
            val metrics = g.fontMetrics
            val wrapped = wrap(line.text, metrics, pageWidth - margin * 2)
            val lineHeight = if (line.kind == ReportPdfLineKind.TITLE) 58 else size + 13
            wrapped.forEach { text ->
                if (y + lineHeight > pageHeight - margin) {
                    output += image!!
                    newPage()
                    graphics!!.font = textFont
                    graphics!!.color = g.color
                }
                graphics!!.drawString(text, margin, y)
                y += lineHeight
            }
            if (line.kind == ReportPdfLineKind.TITLE || line.kind == ReportPdfLineKind.HEADING) y += 7
        }
        graphics?.dispose()
        output += image!!
        return output
    }

    private fun wrap(text: String, metrics: FontMetrics, maxWidth: Int): List<String> {
        if (text.isBlank()) return listOf("")
        val result = mutableListOf<String>()
        var current = StringBuilder()
        text.split(Regex("\\s+")).forEach { word ->
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (current.isNotEmpty() && metrics.stringWidth(candidate) > maxWidth) {
                result += current.toString()
                current = StringBuilder(word)
            } else current = StringBuilder(candidate)
        }
        if (current.isNotEmpty()) result += current.toString()
        return result
    }

    private fun createPdf(jpegs: List<ByteArray>): ByteArray {
        require(jpegs.isNotEmpty())
        val objectBodies = mutableListOf<ByteArray>()
        objectBodies += ascii("<< /Type /Catalog /Pages 2 0 R >>")
        val pageRefs = jpegs.indices.joinToString(" ") { "${3 + it * 3} 0 R" }
        objectBodies += ascii("<< /Type /Pages /Kids [$pageRefs] /Count ${jpegs.size} >>")
        jpegs.forEachIndexed { index, jpeg ->
            val pageObject = 3 + index * 3
            val imageObject = pageObject + 1
            val contentObject = pageObject + 2
            objectBodies += ascii("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /XObject << /Im0 $imageObject 0 R >> >> /Contents $contentObject 0 R >>")
            objectBodies += streamObject(
                "/Type /XObject /Subtype /Image /Width 1190 /Height 1684 /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode",
                jpeg,
            )
            val content = ascii("q\n595 0 0 842 0 0 cm\n/Im0 Do\nQ\n")
            objectBodies += streamObject("", content)
        }
        val output = ByteArrayOutputStream()
        output.write(ascii("%PDF-1.4\n"))
        val offsets = mutableListOf(0)
        objectBodies.forEachIndexed { index, body ->
            offsets += output.size()
            output.write(ascii("${index + 1} 0 obj\n"))
            output.write(body)
            output.write(ascii("\nendobj\n"))
        }
        val xrefOffset = output.size()
        output.write(ascii("xref\n0 ${objectBodies.size + 1}\n0000000000 65535 f \n"))
        offsets.drop(1).forEach { offset ->
            output.write(
                ascii(
                    "${
                        offset.toString().padStart(10, '0')
                    } 00000 n \n"
                )
            )
        }
        output.write(ascii("trailer\n<< /Size ${objectBodies.size + 1} /Root 1 0 R >>\nstartxref\n$xrefOffset\n%%EOF\n"))
        return output.toByteArray()
    }

    private fun streamObject(dictionary: String, bytes: ByteArray): ByteArray =
        ByteArrayOutputStream().use { output ->
            val extra = if (dictionary.isBlank()) "" else "$dictionary "
            output.write(ascii("<< $extra/Length ${bytes.size} >>\nstream\n"))
            output.write(bytes)
            output.write(ascii("\nendstream"))
            output.toByteArray()
        }

    private fun ascii(value: String) = value.encodeToByteArray()
    private fun safeSlug(value: String) =
        value.lowercase().replace(Regex("[^a-z0-9_-]"), "-").trim('-').ifBlank { "report" }
}

/** Desktop sharing is an explicit save-as action, leaving the user with a portable PDF file. */
class JvmReportShareService(private val resolver: ReportTextResolver) : ReportShareService {
    override fun share(artifact: ReportPdfArtifact): ReportShareResult = try {
        val chooser = JFileChooser().apply {
            dialogTitle = resolver.text(ReportTextKey.SAVE_PDF_DIALOG).value
            fileFilter = FileNameExtensionFilter("PDF", "pdf")
            selectedFile = File(artifact.fileName)
        }
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return ReportShareResult.Cancelled
        val selected = chooser.selectedFile.let {
            if (it.extension.equals(
                    "pdf",
                    ignoreCase = true
                )
            ) it else File(it.parentFile, "${it.name}.pdf")
        }
        selected.writeBytes(artifact.bytes)
        ReportShareResult.Shared
    } catch (_: Exception) {
        ReportShareResult.Failed("desktop_save_failed")
    }
}
