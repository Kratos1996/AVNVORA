package com.aynvora.core.ai

import com.aynvora.core.AynvoraSdk
import com.aynvora.core.gita.GitaRepository
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.result.AynvoraResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Standard implementation of [AiToolRegistry].
 */
class DefaultAiToolRegistry : AiToolRegistry {
    private val tools = mutableMapOf<String, AiTool>()

    override fun registerTool(tool: AiTool) {
        tools[tool.name] = tool
    }

    override fun getTool(name: String): AiTool? = tools[name]

    override fun listTools(): List<AiTool> = tools.values.toList()
}

/**
 * Verified Astro Tool: calculateBirthChart
 * Interacts only with [AynvoraSdk] domain facade, returning verified facts with strict provenance.
 */
class CalculateBirthChartTool(
    private val sdk: AynvoraSdk,
) : AiTool {
    override val name: String = "calculateBirthChart"
    override val description: String =
        "Calculates verified planetary positions, Lagna, and houses using the deterministic Astro Engine."
    override val parametersJsonSchema: String =
        """{"type":"object","properties":{"year":{"type":"integer"},"month":{"type":"integer"},"day":{"type":"integer"},"hour":{"type":"integer"},"minute":{"type":"integer"},"latitude":{"type":"number"},"longitude":{"type":"number"}},"required":["year","month","day","hour","minute","latitude","longitude"]}"""

    override suspend fun execute(argumentsJson: String): AynvoraResult<AiToolResult> {
        return try {
            val json = Json.parseToJsonElement(argumentsJson) as? JsonObject
                ?: return AynvoraResult.Failure.InvalidInput(
                    "arguments",
                    "Arguments must be a valid JSON object."
                )

            val year =
                json["year"]?.jsonPrimitive?.intOrNull ?: return AynvoraResult.Failure.InvalidInput(
                    "year",
                    "year is required"
                )
            val month = json["month"]?.jsonPrimitive?.intOrNull
                ?: return AynvoraResult.Failure.InvalidInput("month", "month is required")
            val day = json["day"]?.jsonPrimitive?.intOrNull
                ?: return AynvoraResult.Failure.InvalidInput("day", "day is required")
            val hour =
                json["hour"]?.jsonPrimitive?.intOrNull ?: return AynvoraResult.Failure.InvalidInput(
                    "hour",
                    "hour is required"
                )
            val minute = json["minute"]?.jsonPrimitive?.intOrNull
                ?: return AynvoraResult.Failure.InvalidInput("minute", "minute is required")
            val latitude = json["latitude"]?.jsonPrimitive?.doubleOrNull
                ?: return AynvoraResult.Failure.InvalidInput("latitude", "latitude is required")
            val longitude = json["longitude"]?.jsonPrimitive?.doubleOrNull
                ?: return AynvoraResult.Failure.InvalidInput("longitude", "longitude is required")

            val birthData = BirthData(
                date = com.aynvora.core.models.BirthDate(year, month, day),
                time = com.aynvora.core.models.BirthTime(hour, minute),
                place = com.aynvora.core.models.BirthPlace(
                    name = "ToolLocation",
                    coordinates = com.aynvora.core.models.Coordinates(latitude, longitude),
                    timezoneId = "UTC",
                ),
            )

            val chartResult = sdk.calculateChart(ChartRequest(birthData = birthData))

            when (chartResult) {
                is AynvoraResult.Success -> {
                    val lagnaRashi =
                        chartResult.value.lagna?.rashiPosition?.rashi?.displayName ?: "UNKNOWN"
                    val sunRashi =
                        chartResult.value.planetaryPositions.find { it.body.name == "SUN" }?.rashiPosition?.rashi?.displayName
                            ?: "UNKNOWN"
                    val moonRashi =
                        chartResult.value.planetaryPositions.find { it.body.name == "MOON" }?.rashiPosition?.rashi?.displayName
                            ?: "UNKNOWN"
                    val moonNakshatra =
                        chartResult.value.planetaryPositions.find { it.body.name == "MOON" }?.nakshatraPosition?.nakshatra?.displayName
                            ?: "UNKNOWN"

                    val summaryJson =
                        """{"ascendant":"$lagnaRashi","sunSign":"$sunRashi","moonSign":"$moonRashi","moonNakshatra":"$moonNakshatra","planetsCalculated":${chartResult.value.planetaryPositions.size}}"""
                    AynvoraResult.Success(
                        AiToolResult(
                            callId = "call_${chartResult.value.julianDay.toLong()}",
                            toolName = name,
                            resultJson = summaryJson,
                            provenance = AiProvenance(
                                sourceDomain = "ASTROLOGY",
                                calculationRulesetOrEdition = "PARASHARA_CLASSICAL_V1",
                                verifiedTimestampEpochMs = 0L,
                                isRetrievedFact = true,
                            ),
                            isSuccess = true,
                            calculationMetadata = chartResult.value.calculationMetadata,
                        )
                    )
                }

                is AynvoraResult.Failure -> chartResult
            }
        } catch (e: Exception) {
            AynvoraResult.Failure.CalculationFailure(
                "AI_TOOL_EXECUTION_ERROR",
                e.message ?: "Failed to execute $name"
            )
        }
    }
}

/**
 * Verified Knowledge Tool: searchGita
 * Retrieves authentic Bhagavad Gita verses without AI hallucination.
 */
class SearchGitaTool(
    private val gitaRepository: GitaRepository,
) : AiTool {
    override val name: String = "searchGita"
    override val description: String =
        "Retrieves authentic Bhagavad Gita verse, Sanskrit text, and translation by chapter and verse number."
    override val parametersJsonSchema: String =
        """{"type":"object","properties":{"chapter":{"type":"integer"},"verse":{"type":"integer"},"language":{"type":"string"}},"required":["chapter","verse"]}"""

    override suspend fun execute(argumentsJson: String): AynvoraResult<AiToolResult> {
        return try {
            val json = Json.parseToJsonElement(argumentsJson) as? JsonObject
                ?: return AynvoraResult.Failure.InvalidInput(
                    "arguments",
                    "Arguments must be a valid JSON object."
                )

            val chapter = json["chapter"]?.jsonPrimitive?.intOrNull
                ?: return AynvoraResult.Failure.InvalidInput("chapter", "chapter is required")
            val verse = json["verse"]?.jsonPrimitive?.intOrNull
                ?: return AynvoraResult.Failure.InvalidInput("verse", "verse is required")
            val language = json["language"]?.jsonPrimitive?.content ?: "en"

            when (val verseResult = gitaRepository.getVerse(chapter, verse, language)) {
                is AynvoraResult.Success -> {
                    val item = verseResult.value
                    val resultJson =
                        """{"chapter":${item.chapterNumber},"verse":${item.verseNumber},"sanskrit":"${
                            item.sanskritDevenagari.replace(
                                "\"",
                                "\\\""
                            )
                        }","translation":"${
                            item.translation.replace(
                                "\"",
                                "\\\""
                            )
                        }","sourceEdition":"${item.sourceEdition.title}"}"""
                    AynvoraResult.Success(
                        AiToolResult(
                            callId = "gita_${chapter}_${verse}",
                            toolName = name,
                            resultJson = resultJson,
                            provenance = AiProvenance(
                                sourceDomain = "GITA",
                                calculationRulesetOrEdition = item.sourceEdition.editionId,
                                verifiedTimestampEpochMs = 0L,
                                isRetrievedFact = true,
                            ),
                            isSuccess = true,
                        )
                    )
                }

                is AynvoraResult.Failure -> verseResult
            }
        } catch (e: Exception) {
            AynvoraResult.Failure.CalculationFailure(
                "AI_TOOL_EXECUTION_ERROR",
                e.message ?: "Failed to execute $name"
            )
        }
    }
}
