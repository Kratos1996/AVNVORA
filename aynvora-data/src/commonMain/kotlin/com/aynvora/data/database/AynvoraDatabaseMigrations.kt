package com.aynvora.data.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection

private fun SQLiteConnection.execute(sql: String) {
    prepare(sql).use { statement -> statement.step() }
}

/** Explicit, additive migrations. No migration path deletes user data. */
object AynvoraDatabaseMigrations {
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execute("""CREATE TABLE IF NOT EXISTS `gita_chapters` (`chapterId` INTEGER NOT NULL, `chapterNumber` INTEGER NOT NULL, `nameSanskrit` TEXT NOT NULL, `nameTranslation` TEXT NOT NULL, `nameTransliterated` TEXT NOT NULL, `nameMeaning` TEXT NOT NULL, `chapterSummaryEnglish` TEXT NOT NULL, `chapterSummaryHindi` TEXT NOT NULL, `versesCount` INTEGER NOT NULL, `imageName` TEXT NOT NULL, PRIMARY KEY(`chapterId`))""")
            connection.execute("""CREATE TABLE IF NOT EXISTS `gita_verses` (`verseId` INTEGER NOT NULL, `chapterNumber` INTEGER NOT NULL, `verseNumber` INTEGER NOT NULL, `verseOrder` INTEGER NOT NULL, `title` TEXT NOT NULL, `sanskritDevanagari` TEXT NOT NULL, `transliteration` TEXT NOT NULL, `wordMeanings` TEXT NOT NULL, PRIMARY KEY(`verseId`))""")
            connection.execute("CREATE INDEX IF NOT EXISTS `index_gita_verses_chapterNumber` ON `gita_verses` (`chapterNumber`)")
            connection.execute("CREATE UNIQUE INDEX IF NOT EXISTS `index_gita_verses_chapterNumber_verseNumber` ON `gita_verses` (`chapterNumber`, `verseNumber`)")
            connection.execute("""CREATE TABLE IF NOT EXISTS `gita_translations` (`translationId` INTEGER NOT NULL, `verseId` INTEGER NOT NULL, `authorId` INTEGER NOT NULL, `authorName` TEXT NOT NULL, `language` TEXT NOT NULL, `description` TEXT NOT NULL, PRIMARY KEY(`translationId`))""")
            connection.execute("CREATE INDEX IF NOT EXISTS `index_gita_translations_verseId` ON `gita_translations` (`verseId`)")
            connection.execute("CREATE INDEX IF NOT EXISTS `index_gita_translations_authorId` ON `gita_translations` (`authorId`)")
            connection.execute("CREATE INDEX IF NOT EXISTS `index_gita_translations_language` ON `gita_translations` (`language`)")
            connection.execute("CREATE UNIQUE INDEX IF NOT EXISTS `index_gita_translations_verseId_authorId_language` ON `gita_translations` (`verseId`, `authorId`, `language`)")
            connection.execute("""CREATE TABLE IF NOT EXISTS `gita_commentaries` (`commentaryId` INTEGER NOT NULL, `verseId` INTEGER NOT NULL, `authorId` INTEGER NOT NULL, `authorName` TEXT NOT NULL, `language` TEXT NOT NULL, `description` TEXT NOT NULL, PRIMARY KEY(`commentaryId`))""")
            connection.execute("CREATE INDEX IF NOT EXISTS `index_gita_commentaries_verseId` ON `gita_commentaries` (`verseId`)")
            connection.execute("CREATE INDEX IF NOT EXISTS `index_gita_commentaries_authorId` ON `gita_commentaries` (`authorId`)")
            connection.execute("CREATE INDEX IF NOT EXISTS `index_gita_commentaries_language` ON `gita_commentaries` (`language`)")
            connection.execute("CREATE UNIQUE INDEX IF NOT EXISTS `index_gita_commentaries_verseId_authorId_language` ON `gita_commentaries` (`verseId`, `authorId`, `language`)")
            connection.execute("CREATE TABLE IF NOT EXISTS `gita_authors` (`authorId` INTEGER NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`authorId`))")
            connection.execute("""CREATE TABLE IF NOT EXISTS `gita_seed_state` (`id` TEXT NOT NULL, `isSeeded` INTEGER NOT NULL, `seededVerseCount` INTEGER NOT NULL, `seededTranslationCount` INTEGER NOT NULL, `seededCommentaryCount` INTEGER NOT NULL, `sourceCommitSha` TEXT NOT NULL, `schemaVersion` INTEGER NOT NULL, `seededAtEpochMs` INTEGER NOT NULL, PRIMARY KEY(`id`))""")
        }
    }

    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execute("ALTER TABLE `birth_profiles` ADD COLUMN `locationDatasetVersion` TEXT")
            connection.execute("ALTER TABLE `birth_profiles` ADD COLUMN `locationProvenance` TEXT")
            connection.execute("ALTER TABLE `saved_charts` ADD COLUMN `snapshotSchemaVersion` TEXT")
            connection.execute("ALTER TABLE `saved_charts` ADD COLUMN `calculationContractVersion` TEXT")
            connection.execute("ALTER TABLE `saved_charts` ADD COLUMN `createdAtEpochMs` INTEGER NOT NULL DEFAULT 0")
            connection.execute("ALTER TABLE `saved_charts` ADD COLUMN `updatedAtEpochMs` INTEGER NOT NULL DEFAULT 0")
            connection.execute("ALTER TABLE `saved_charts` ADD COLUMN `lastOpenedAtEpochMs` INTEGER")
            connection.execute("ALTER TABLE `saved_charts` ADD COLUMN `identityFingerprint` TEXT")
            connection.execute("UPDATE `saved_charts` SET `createdAtEpochMs` = `calculationTimestampEpochMs`, `updatedAtEpochMs` = `calculationTimestampEpochMs` WHERE `createdAtEpochMs` = 0")
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}
