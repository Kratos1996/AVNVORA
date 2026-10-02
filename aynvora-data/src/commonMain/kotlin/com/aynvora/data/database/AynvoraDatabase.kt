package com.aynvora.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.aynvora.data.database.dao.BirthProfileDao
import com.aynvora.data.database.dao.ContentItemDao
import com.aynvora.data.database.dao.ContentPackDao
import com.aynvora.data.database.dao.ContentSyncMetadataDao
import com.aynvora.data.database.dao.GitaDao
import com.aynvora.data.database.dao.SavedChartDao
import com.aynvora.data.database.dao.UserPreferencesDao
import com.aynvora.data.database.dao.UserProfileDao
import com.aynvora.data.database.entity.BirthProfileRoomEntity
import com.aynvora.data.database.entity.ContentItemRoomEntity
import com.aynvora.data.database.entity.ContentPackRoomEntity
import com.aynvora.data.database.entity.ContentSyncMetadataRoomEntity
import com.aynvora.data.database.entity.GitaAuthorRoomEntity
import com.aynvora.data.database.entity.GitaChapterRoomEntity
import com.aynvora.data.database.entity.GitaCommentaryRoomEntity
import com.aynvora.data.database.entity.GitaSeedStateRoomEntity
import com.aynvora.data.database.entity.GitaTranslationRoomEntity
import com.aynvora.data.database.entity.GitaVerseRoomEntity
import com.aynvora.data.database.entity.SavedChartRoomEntity
import com.aynvora.data.database.entity.UserPreferencesRoomEntity
import com.aynvora.data.database.entity.UserProfileRoomEntity

/**
 * Versioned Room KMP local database.
 *
 * Serves as the authoritative offline source of truth for user profiles,
 * birth records, persisted charts, user preferences, downloaded knowledge packs,
 * and Bhagavad Gita scripture data.
 *
 * Version history:
 *  v1 — initial schema (profiles, charts, tarot)
 *  v2 — Phase 8.3: Bhagavad Gita scripture tables (gita/gita, Public Domain)
 *  v3 — Phase 10.16F: complete saved-chart snapshot compatibility metadata
 */
@Database(
    entities = [
        UserProfileRoomEntity::class,
        BirthProfileRoomEntity::class,
        SavedChartRoomEntity::class,
        UserPreferencesRoomEntity::class,
        ContentPackRoomEntity::class,
        ContentItemRoomEntity::class,
        ContentSyncMetadataRoomEntity::class,
        com.aynvora.data.database.entity.TarotDeckRoomEntity::class,
        com.aynvora.data.database.entity.TarotCardRoomEntity::class,
        com.aynvora.data.database.entity.TarotCardContentRoomEntity::class,
        com.aynvora.data.database.entity.TarotReadingHistoryRoomEntity::class,
        // ── Phase 8.3: Bhagavad Gita ─────────────────────────────────────────
        GitaChapterRoomEntity::class,
        GitaVerseRoomEntity::class,
        GitaTranslationRoomEntity::class,
        GitaCommentaryRoomEntity::class,
        GitaAuthorRoomEntity::class,
        GitaSeedStateRoomEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@ConstructedBy(AynvoraDatabaseConstructor::class)
abstract class AynvoraDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun birthProfileDao(): BirthProfileDao
    abstract fun savedChartDao(): SavedChartDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun contentPackDao(): ContentPackDao
    abstract fun contentItemDao(): ContentItemDao
    abstract fun contentSyncMetadataDao(): ContentSyncMetadataDao
    abstract fun tarotDao(): com.aynvora.data.database.dao.TarotDao
    abstract fun gitaDao(): GitaDao

    companion object {
        const val DATABASE_NAME = "aynvora.db"
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AynvoraDatabaseConstructor : RoomDatabaseConstructor<AynvoraDatabase> {
    override fun initialize(): AynvoraDatabase
}
