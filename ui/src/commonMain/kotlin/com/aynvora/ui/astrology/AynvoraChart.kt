package com.aynvora.ui.astrology

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aynvora.core.models.AstroChart
import com.aynvora.core.models.AstroChartBuildResult
import com.aynvora.core.models.AstroChartBuilder
import com.aynvora.core.models.AstroChartHouseData
import com.aynvora.core.models.AstroChartValidator
import com.aynvora.core.models.PlanetInHouse

enum class ChartDetailMode { COMPACT, DETAILED }

data class ChartPlanetRenderModel(
    val planetId: String,
    val shortLabel: String,
    val fullLabel: String,
    val degreeText: String,
    val compactDegreeText: String,
    val markerText: String,
    val nakshatraText: String?,
    val preferredPosition: Int,
    val layoutPriority: Int,
    val semanticsDescription: String,
)

data class ChartHouseRenderModel(
    val houseNumber: Int,
    val signLabel: String,
    val signNumber: Int,
    val planetItems: List<ChartPlanetRenderModel>,
    val markers: List<String>,
    val ascendant: Boolean,
    val annotations: List<String>,
    val semanticsDescription: String,
)

data class AynvoraChartStyle(
    val lineColor: Color = Color(0xFFC89020),
    val lineWidth: Float = 1.5f,
    val background: Color = Color(0xFFFFFCF4),
    val houseTextColor: Color = Color(0xFF6F6550),
    val signTextColor: Color = Color(0xFF725A22),
    val planetTextColor: Color = Color(0xFF25233A),
    val markerTextColor: Color = Color(0xFF9C4B18),
    val padding: Float = 0f,
    val cornerRadius: Float = 12f,
    val selectedHouseColor: Color = Color(0xFFFFE6B0),
    val highlightedPlanetColor: Color = Color(0xFFC2410C),
    val darkTheme: Boolean = false,
    val lineStyle: ChartLineStyle = ChartLineStyle.SOLID,
    val houseTextSizeSp: Float = 8f,
    val signTextSizeSp: Float = 8f,
    val planetTextSizeSp: Float = 9f,
    val degreeTextSizeSp: Float = 7f,
    val spacingDp: Float = 1f,
    val selectedHouseNumber: Int? = null,
    val highlightedPlanetId: String? = null,
)

enum class ChartLineStyle { SOLID, DASHED }

/** Fixed North Indian geometry. Indices are house numbers 1..12, independent of signs. */
object NorthIndianChartGeometry {
    val houseCenters: List<Pair<Float, Float>> = listOf(
        .50f to .14f, .24f to .19f, .14f to .36f, .16f to .50f,
        .14f to .64f, .24f to .81f, .50f to .86f, .76f to .81f,
        .86f to .64f, .84f to .50f, .86f to .36f, .76f to .19f,
    )
    val collisionHalfWidth: Float = .12f
    val collisionHalfHeight: Float = .105f
}

object ChartPlanetLayoutEngine {
    /** Stable house-local grid. Every returned slot stays associated with its input planet. */
    fun positions(planets: List<ChartPlanetRenderModel>, spacingFactor: Float = 1f): List<Pair<ChartPlanetRenderModel, Pair<Float, Float>>> {
        val ordered = planets.sortedWith(compareByDescending<ChartPlanetRenderModel> { it.layoutPriority }.thenBy { it.planetId })
        if (ordered.isEmpty()) return emptyList()
        val columns = when {
            ordered.size >= 2 -> 2
            else -> 1
        }
        return ordered.mapIndexed { index, planet ->
            val col = index % columns
            val row = index / columns
            val rows = (ordered.size + columns - 1) / columns
            val x = when (columns) { 1 -> .5f; else -> .24f + col * .52f }
            val baseY = when (rows) {
                1 -> .5f
                2 -> .36f + row * .28f
                3 -> .24f + row * .26f
                else -> .12f + row * (.76f / (rows - 1))
            }
            val y = (.5f + (baseY - .5f) * spacingFactor.coerceIn(.5f, 1.5f)).coerceIn(.08f, .92f)
            planet to (x.coerceIn(.08f, .92f) to y.coerceIn(.08f, .92f))
        }
    }
}

object ChartRenderMapper {
    fun map(chart: AstroChart, mode: ChartDetailMode = ChartDetailMode.COMPACT): List<ChartHouseRenderModel> =
        chart.houses.sortedBy { it.houseNumber }.map { house -> mapHouse(house, mode) }

    private fun mapHouse(house: AstroChartHouseData, mode: ChartDetailMode) = ChartHouseRenderModel(
        houseNumber = house.houseNumber,
        signLabel = house.sign.displayName,
        signNumber = house.sign.index + 1,
        planetItems = house.planets.mapIndexed { i, p -> mapPlanet(p, i, mode) },
        markers = house.markers,
        ascendant = house.ascendant,
        annotations = house.annotations,
        semanticsDescription = buildString {
            append("House ${house.houseNumber}, ${house.sign.displayName}")
            if (house.ascendant) append(", Ascendant")
            house.planets.forEach { append("; ${mapPlanet(it, 0, mode).semanticsDescription}") }
            house.annotations.forEach { append("; $it") }
        },
    )

    private fun mapPlanet(p: PlanetInHouse, order: Int, mode: ChartDetailMode): ChartPlanetRenderModel {
        val degree = formatDms(p.degrees, p.minutes, p.seconds)
        val markers = buildList {
            if (p.retrograde) add("℞")
            if (p.combust) add("☼")
            if (p.exalted == true) add("↑")
            if (p.debilitated == true) add("↓")
            if (p.vargottama == true) add("◆")
        }.joinToString("")
        val name = p.planetId.lowercase().replaceFirstChar(Char::uppercase)
        val nak = p.nakshatra?.let { "$it${p.pada?.let { pada -> " · Pada $pada" } ?: ""}" }
        return ChartPlanetRenderModel(
            p.planetId, shortPlanet(p.planetId), name, degree, "%02d°%02d′".format(p.degrees, p.minutes), markers, nak,
            order, 0,
            listOfNotNull(name, degree, nak, markers.takeIf(String::isNotEmpty)).joinToString(", "),
        )
    }

    private fun shortPlanet(id: String) = when (id) {
        "SUN" -> "☉"; "MOON" -> "☽"; "MERCURY" -> "☿"; "VENUS" -> "♀"; "MARS" -> "♂"
        "JUPITER" -> "♃"; "SATURN" -> "♄"; "RAHU" -> "☊"; "KETU" -> "☋"; else -> id.take(2)
    }

    private fun formatDms(degrees: Int, minutes: Int, seconds: Double): String {
        val secondsText = if (kotlin.math.abs(seconds - seconds.toInt()) < 0.0001) "%02d".format(seconds.toInt()) else "%04.1f".format(seconds)
        return "%02d°%02d′%s″".format(degrees, minutes, secondsText)
    }
}

/** Optional slots let SDK consumers replace individual visual pieces while retaining one renderer. */
interface AynvoraComponentProvider {
    @Composable fun ChartContainer(modifier: Modifier, content: @Composable () -> Unit)
    @Composable fun ChartHouse(
        model: ChartHouseRenderModel,
        style: AynvoraChartStyle,
        detailMode: ChartDetailMode,
        modifier: Modifier,
        renderPlanet: @Composable (ChartPlanetRenderModel, ChartDetailMode, AynvoraChartStyle, Modifier) -> Unit,
        renderSign: @Composable (String, Int, AynvoraChartStyle, Modifier) -> Unit,
        renderMarker: @Composable (List<String>, AynvoraChartStyle, Modifier) -> Unit,
    )
    @Composable fun ChartPlanet(model: ChartPlanetRenderModel, mode: ChartDetailMode, style: AynvoraChartStyle, modifier: Modifier)
    @Composable fun ChartSign(label: String, number: Int, style: AynvoraChartStyle, modifier: Modifier)
    @Composable fun ChartMarker(markers: List<String>, style: AynvoraChartStyle, modifier: Modifier)
    @Composable fun ChartLegend(markers: List<String>, modifier: Modifier)
}

object DefaultAynvoraComponentProvider : AynvoraComponentProvider {
    @Composable override fun ChartContainer(modifier: Modifier, content: @Composable () -> Unit) = Box(modifier, contentAlignment = Alignment.Center) { content() }
    @Composable override fun ChartHouse(
        model: ChartHouseRenderModel,
        style: AynvoraChartStyle,
        detailMode: ChartDetailMode,
        modifier: Modifier,
        renderPlanet: @Composable (ChartPlanetRenderModel, ChartDetailMode, AynvoraChartStyle, Modifier) -> Unit,
        renderSign: @Composable (String, Int, AynvoraChartStyle, Modifier) -> Unit,
        renderMarker: @Composable (List<String>, AynvoraChartStyle, Modifier) -> Unit,
    ) {
        BoxWithConstraints(modifier) {
            val w = maxWidth; val h = maxHeight
            Row(Modifier.align(Alignment.TopCenter).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${model.houseNumber.toString().padStart(2, '0')}${if (model.ascendant) " ↑" else ""}", color = if (model.ascendant) Color(0xFFC89020) else style.houseTextColor, fontSize = style.houseTextSizeSp.sp, maxLines = 1)
                renderSign(model.signLabel, model.signNumber, style, Modifier)
            }
            ChartPlanetLayoutEngine.positions(model.planetItems, style.spacingDp.coerceAtLeast(.5f)).forEach { (planet, position) ->
                val itemWidth = when { model.planetItems.size == 1 -> 40.dp; else -> 36.dp }
                val denseStyle = if (model.planetItems.size >= 5) style.copy(planetTextSizeSp = style.planetTextSizeSp.coerceAtMost(7f), degreeTextSizeSp = style.degreeTextSizeSp.coerceAtMost(6f)) else style
                renderPlanet(planet, detailMode, denseStyle, Modifier.offset(x = w * position.first - itemWidth / 2f, y = h * position.second - 5.dp).width(itemWidth))
            }
            if (model.markers.isNotEmpty()) renderMarker(model.markers, style, Modifier.align(Alignment.BottomCenter))
        }
    }
    @Composable override fun ChartPlanet(model: ChartPlanetRenderModel, mode: ChartDetailMode, style: AynvoraChartStyle, modifier: Modifier) {
        if (mode == ChartDetailMode.COMPACT) {
            Text("${model.shortLabel}${model.markerText} ${model.compactDegreeText}", modifier, color = if (style.highlightedPlanetId == model.planetId) style.highlightedPlanetColor else style.planetTextColor, fontSize = style.planetTextSizeSp.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
        } else {
            Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${model.fullLabel}${model.markerText}", color = if (style.highlightedPlanetId == model.planetId) style.highlightedPlanetColor else style.planetTextColor, fontSize = style.planetTextSizeSp.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(model.degreeText, color = style.signTextColor, fontSize = style.degreeTextSizeSp.sp, maxLines = 1)
                model.nakshatraText?.let { Text(it, fontSize = 6.sp, maxLines = 1) }
            }
        }
    }
    @Composable override fun ChartSign(label: String, number: Int, style: AynvoraChartStyle, modifier: Modifier) { Text("$label · $number", modifier, color = style.signTextColor, fontSize = style.signTextSizeSp.sp) }
    @Composable override fun ChartMarker(markers: List<String>, style: AynvoraChartStyle, modifier: Modifier) { Text(markers.distinct().joinToString(" "), modifier, color = style.markerTextColor, fontSize = style.degreeTextSizeSp.sp) }
    @Composable override fun ChartLegend(markers: List<String>, modifier: Modifier) { if (markers.isNotEmpty()) Text(markers.distinct().joinToString("   "), modifier, fontSize = 10.sp) }
}

@Composable
fun AynvoraChart(
    chart: AstroChart,
    modifier: Modifier = Modifier,
    style: AynvoraChartStyle = AynvoraChartStyle(),
    detailMode: ChartDetailMode = ChartDetailMode.COMPACT,
    components: AynvoraComponentProvider = DefaultAynvoraComponentProvider,
) {
    val diagnostics = AstroChartValidator.validate(chart)
    if (diagnostics.isNotEmpty()) {
        Text("CHART_DATA_INVALID · ${diagnostics.joinToString { it.code }}", modifier.semantics { contentDescription = "Invalid chart data: ${diagnostics.joinToString { it.message }}" })
        return
    }
    val houseModels = ChartRenderMapper.map(chart, detailMode)
    val description = houseModels.joinToString(". ") { it.semanticsDescription }
    val renderStyle = if (style.darkTheme) style.copy(
        background = Color(0xFF171923), houseTextColor = Color(0xFFE4DCC8),
        signTextColor = Color(0xFFE6BC65), planetTextColor = Color(0xFFF3EEE2),
        markerTextColor = Color(0xFFFFA26D),
    ) else style
    components.ChartContainer(
        modifier.fillMaxWidth().aspectRatio(1f).padding(style.padding.dp).clip(RoundedCornerShape(style.cornerRadius.dp))
            .background(renderStyle.background).semantics { contentDescription = description },
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val w = maxWidth
            val h = maxHeight
            Canvas(Modifier.fillMaxSize()) {
                val cx = size.width / 2f; val cy = size.height / 2f
                val gold = renderStyle.lineColor
                val stroke = renderStyle.lineWidth.dp.toPx()
                val pathEffect = if (renderStyle.lineStyle == ChartLineStyle.DASHED) androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(stroke * 4, stroke * 3)) else null
                drawRect(gold, style = Stroke(stroke))
                drawLine(gold, Offset(cx, 0f), Offset(size.width, cy), strokeWidth = stroke, pathEffect = pathEffect)
                drawLine(gold, Offset(size.width, cy), Offset(cx, size.height), strokeWidth = stroke, pathEffect = pathEffect)
                drawLine(gold, Offset(cx, size.height), Offset(0f, cy), strokeWidth = stroke, pathEffect = pathEffect)
                drawLine(gold, Offset(0f, cy), Offset(cx, 0f), strokeWidth = stroke, pathEffect = pathEffect)
                drawLine(gold, Offset(0f, 0f), Offset(cx, cy), strokeWidth = stroke, pathEffect = pathEffect)
                drawLine(gold, Offset(size.width, 0f), Offset(cx, cy), strokeWidth = stroke, pathEffect = pathEffect)
                drawLine(gold, Offset(size.width, size.height), Offset(cx, cy), strokeWidth = stroke, pathEffect = pathEffect)
                drawLine(gold, Offset(0f, size.height), Offset(cx, cy), strokeWidth = stroke, pathEffect = pathEffect)
            }
            houseModels.forEach { house ->
                val (x, y) = NorthIndianChartGeometry.houseCenters[house.houseNumber - 1]
                val houseModifier = Modifier.offset(x = w * x - 38.dp, y = h * y - 34.dp).size(76.dp, 68.dp)
                components.ChartHouse(
                    house,
                    renderStyle,
                    detailMode,
                    houseModifier.background(if (house.houseNumber == renderStyle.selectedHouseNumber) renderStyle.selectedHouseColor else Color.Transparent)
                        .semantics { contentDescription = house.semanticsDescription },
                    renderPlanet = { planet, mode, childStyle, childModifier -> components.ChartPlanet(planet, mode, childStyle, childModifier) },
                    renderSign = { label, number, childStyle, childModifier -> components.ChartSign(label, number, childStyle, childModifier) },
                    renderMarker = { markers, childStyle, childModifier -> components.ChartMarker(markers, childStyle, childModifier) },
                )
            }
        }
    }
}

@Composable
fun AynvoraChart(chartResult: AstroChartBuildResult, modifier: Modifier = Modifier, style: AynvoraChartStyle = AynvoraChartStyle()) {
    if (chartResult is AstroChartBuildResult.Valid) AynvoraChart(chartResult.chart, modifier, style)
    else {
        val diagnostics = (chartResult as AstroChartBuildResult.Invalid).diagnostics
        Text("CHART_DATA_INVALID · ${diagnostics.joinToString { it.code }}", modifier.semantics { contentDescription = "Invalid chart data: ${diagnostics.joinToString { it.message }}" })
    }
}

@Composable
fun AynvoraChartLegend(chart: AstroChart, modifier: Modifier = Modifier, components: AynvoraComponentProvider = DefaultAynvoraComponentProvider) {
    val markers = chart.houses.flatMap { house -> house.planets.flatMap { p -> buildList {
        if (p.retrograde) add("℞ Retrograde")
        if (p.combust) add("☼ Combust")
        if (p.exalted == true) add("↑ Exalted")
        if (p.debilitated == true) add("↓ Debilitated")
        if (p.vargottama == true) add("◆ Vargottama")
    } } }
    components.ChartLegend(markers.distinct(), modifier)
}
