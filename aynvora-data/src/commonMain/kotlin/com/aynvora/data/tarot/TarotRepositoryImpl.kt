package com.aynvora.data.tarot

import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.TarotArcana
import com.aynvora.core.tarot.TarotCard
import com.aynvora.core.tarot.TarotCardContent
import com.aynvora.core.tarot.TarotCardDraw
import com.aynvora.core.tarot.TarotCardOrientation
import com.aynvora.core.tarot.TarotReading
import com.aynvora.core.tarot.TarotRepository
import com.aynvora.core.tarot.TarotSpread
import com.aynvora.core.tarot.TarotSpreadPosition
import com.aynvora.core.tarot.TarotStandardDeck
import com.aynvora.core.tarot.TarotSuit
import com.aynvora.data.database.dao.TarotDao
import com.aynvora.data.database.entity.TarotCardContentRoomEntity
import com.aynvora.data.database.entity.TarotCardRoomEntity
import com.aynvora.data.database.entity.TarotDeckRoomEntity
import com.aynvora.data.database.entity.TarotReadingHistoryRoomEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * Production Room-backed implementation of [TarotRepository].
 *
 * Implements offline-first data access:
 * - Queries Room DAO first; if empty, bootstraps default canonical cards and starter content.
 * - Streams live updates via Coroutine Flow.
 * - Stores reading history purely locally (no PII or user-entered notes).
 */
class TarotRepositoryImpl(
    private val tarotDao: TarotDao,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : TarotRepository {

    override suspend fun getDeckCards(deckId: String): AynvoraResult<List<TarotCard>> {
        return try {
            val entities = tarotDao.getCardsForDeck(deckId)
            if (entities.isEmpty()) {
                // Seed standard cards into Room
                seedDefaultDeckAndCards()
                AynvoraResult.Success(TarotStandardDeck.AllCards)
            } else {
                AynvoraResult.Success(entities.map { it.toDomain() })
            }
        } catch (e: Exception) {
            // Fallback to in-memory standard deck to guarantee offline functionality
            AynvoraResult.Success(TarotStandardDeck.AllCards)
        }
    }

    override suspend fun getCardContent(
        cardId: String,
        language: String
    ): AynvoraResult<TarotCardContent> {
        return try {
            val entity = tarotDao.getContentByCardAndLanguage(cardId, language)
            if (entity != null) {
                AynvoraResult.Success(entity.toDomain())
            } else {
                // Seed or fallback to starter content
                val fallback = TarotStarterContent.AllStarterContent.firstOrNull {
                    it.cardId == cardId && it.language == language
                } ?: TarotStarterContent.EnglishCards.firstOrNull { it.cardId == cardId }
                ?: TarotCardContent(
                    cardId = cardId,
                    language = language,
                    title = cardId.replace("_", " ").replaceFirstChar { it.uppercase() },
                    shortDescription = "Mindful reflection and thematic contemplation.",
                    keywords = listOf("Reflection", "Insight", "Mindfulness"),
                    uprightMeaning = "A constructive aspect to consider in your current perspective.",
                    reversedMeaning = "An opportunity to pause and consider alternate viewpoints.",
                )
                AynvoraResult.Success(fallback)
            }
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                operation = "getCardContent",
                message = "Failed to load Tarot card content: ${e.message}",
            )
        }
    }

    override fun observeAllCardContent(language: String): Flow<AynvoraResult<List<TarotCardContent>>> {
        return tarotDao.observeContentByLanguage(language).map { entities ->
            if (entities.isEmpty()) {
                // Emit starter content if database is not yet seeded
                val starter =
                    TarotStarterContent.AllStarterContent.filter { it.language == language }
                AynvoraResult.Success(starter.ifEmpty { TarotStarterContent.EnglishCards })
            } else {
                AynvoraResult.Success(entities.map { it.toDomain() })
            }
        }
    }

    override suspend fun getSupportedSpreads(): AynvoraResult<List<TarotSpread>> {
        return AynvoraResult.Success(TarotSpread.StandardSpreads)
    }

    override suspend fun saveReading(reading: TarotReading): AynvoraResult<Unit> {
        return try {
            val drawsDto = reading.draws.map { draw ->
                SerializedDrawDto(
                    cardId = draw.card.id,
                    orientation = draw.orientation.name,
                    positionId = draw.position.id,
                    positionName = draw.position.name,
                    positionDescription = draw.position.description,
                    orderIndex = draw.position.orderIndex,
                )
            }
            val jsonPayload = json.encodeToString(drawsDto)

            val entity = TarotReadingHistoryRoomEntity(
                id = reading.id,
                spreadId = reading.spreadId,
                deckId = reading.deckId,
                serializedDrawsJson = jsonPayload,
                timestampEpochMs = reading.timestampEpochMs,
            )
            tarotDao.insertReading(entity)
            AynvoraResult.Success(Unit)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                operation = "saveReading",
                message = "Failed to save Tarot reading: ${e.message}",
            )
        }
    }

    override fun observeRecentReadings(limit: Int): Flow<List<TarotReading>> {
        return tarotDao.observeRecentReadings(limit).map { entities ->
            entities.mapNotNull { entity ->
                try {
                    val drawsDto =
                        json.decodeFromString<List<SerializedDrawDto>>(entity.serializedDrawsJson)
                    val draws = drawsDto.map { dto ->
                        val card = TarotStandardDeck.AllCards.firstOrNull { it.id == dto.cardId }
                            ?: TarotCard(
                                id = dto.cardId,
                                number = 0,
                                name = dto.cardId,
                                arcana = TarotArcana.MAJOR,
                            )
                        TarotCardDraw(
                            card = card,
                            orientation = runCatching { TarotCardOrientation.valueOf(dto.orientation) }.getOrDefault(
                                TarotCardOrientation.UPRIGHT
                            ),
                            position = TarotSpreadPosition(
                                id = dto.positionId,
                                orderIndex = dto.orderIndex,
                                name = dto.positionName,
                                description = dto.positionDescription,
                            ),
                        )
                    }
                    TarotReading(
                        id = entity.id,
                        spreadId = entity.spreadId,
                        deckId = entity.deckId,
                        draws = draws,
                        timestampEpochMs = entity.timestampEpochMs,
                    )
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    override suspend fun deleteReading(id: String): AynvoraResult<Unit> {
        return try {
            tarotDao.deleteReadingById(id)
            AynvoraResult.Success(Unit)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                operation = "deleteReading",
                message = "Failed to delete reading '$id': ${e.message}",
            )
        }
    }

    override suspend fun clearReadingHistory(): AynvoraResult<Unit> {
        return try {
            tarotDao.clearHistory()
            AynvoraResult.Success(Unit)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                operation = "clearReadingHistory",
                message = "Failed to clear reading history: ${e.message}",
            )
        }
    }

    override suspend fun updateContentPack(
        language: String,
        newVersion: Int,
        contentList: List<TarotCardContent>,
    ): AynvoraResult<Unit> {
        return try {
            if (contentList.isEmpty()) {
                return AynvoraResult.Failure.InvalidInput(
                    field = "contentList",
                    message = "Incoming content list cannot be empty",
                )
            }
            // Check existing version in Room for this language
            val existingSample =
                tarotDao.getContentByCardAndLanguage(contentList.first().cardId, language)
            val currentVersion = existingSample?.contentVersion ?: 0
            if (newVersion <= currentVersion) {
                return AynvoraResult.Failure.SyncFailure(
                    code = "VERSION_STALE",
                    message = "Incoming version $newVersion is not newer than installed version $currentVersion",
                    isRetryable = false,
                )
            }

            // Transactional update: prepare entities
            val entities = contentList.map { content ->
                TarotCardContentRoomEntity(
                    id = "${content.cardId}_${content.language}",
                    cardId = content.cardId,
                    language = content.language,
                    title = content.title,
                    shortDescription = content.shortDescription,
                    keywordsCsv = content.keywords.joinToString(","),
                    uprightMeaning = content.uprightMeaning,
                    reversedMeaning = content.reversedMeaning,
                    sourceAttribution = content.sourceAttribution,
                    contentVersion = newVersion,
                )
            }
            tarotDao.upsertCardContentList(entities)
            AynvoraResult.Success(Unit)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                operation = "updateContentPack",
                message = "Failed to apply content pack update: ${e.message}",
            )
        }
    }

    private suspend fun seedDefaultDeckAndCards() {
        val existingDeck = tarotDao.getDeckById(TarotStandardDeck.Deck.id)
        if (existingDeck == null) {
            tarotDao.upsertDeck(
                TarotDeckRoomEntity(
                    id = TarotStandardDeck.Deck.id,
                    name = TarotStandardDeck.Deck.name,
                    description = TarotStandardDeck.Deck.description,
                    cardCount = TarotStandardDeck.Deck.cardCount,
                ),
            )
        }

        val cardCount = tarotDao.getCardCount(TarotStandardDeck.Deck.id)
        if (cardCount < TarotStandardDeck.AllCards.size) {
            val cardEntities = TarotStandardDeck.AllCards.map { card ->
                TarotCardRoomEntity(
                    id = card.id,
                    deckId = TarotStandardDeck.Deck.id,
                    number = card.number,
                    name = card.name,
                    arcana = card.arcana.name,
                    suit = card.suit?.name,
                )
            }
            tarotDao.upsertCards(cardEntities)
        }

        val enCount = tarotDao.getContentCount("en")
        val hiCount = tarotDao.getContentCount("hi")
        if (enCount < TarotCompleteContentPack.EnglishCards.size || hiCount < TarotCompleteContentPack.HindiCards.size) {
            val contentEntities = TarotCompleteContentPack.AllCards.map { content ->
                TarotCardContentRoomEntity(
                    id = "${content.cardId}_${content.language}",
                    cardId = content.cardId,
                    language = content.language,
                    title = content.title,
                    shortDescription = content.shortDescription,
                    keywordsCsv = content.keywords.joinToString(","),
                    uprightMeaning = content.uprightMeaning,
                    reversedMeaning = content.reversedMeaning,
                    sourceAttribution = content.sourceAttribution,
                    contentVersion = content.contentVersion,
                )
            }
            tarotDao.upsertCardContentList(contentEntities)
        }
    }

    private fun TarotCardRoomEntity.toDomain(): TarotCard = TarotCard(
        id = id,
        number = number,
        name = name,
        arcana = runCatching { TarotArcana.valueOf(arcana) }.getOrDefault(TarotArcana.MAJOR),
        suit = suit?.let { runCatching { TarotSuit.valueOf(it) }.getOrNull() },
    )

    private fun TarotCardContentRoomEntity.toDomain(): TarotCardContent = TarotCardContent(
        cardId = cardId,
        language = language,
        title = title,
        shortDescription = shortDescription,
        keywords = if (keywordsCsv.isBlank()) emptyList() else keywordsCsv.split(",")
            .map { it.trim() },
        uprightMeaning = uprightMeaning,
        reversedMeaning = reversedMeaning,
        sourceAttribution = sourceAttribution,
        contentVersion = contentVersion,
    )
}

@kotlinx.serialization.Serializable
private data class SerializedDrawDto(
    val cardId: String,
    val orientation: String,
    val positionId: String,
    val positionName: String,
    val positionDescription: String,
    val orderIndex: Int,
)
