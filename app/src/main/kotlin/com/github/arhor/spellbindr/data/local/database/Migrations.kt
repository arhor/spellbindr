package com.github.arhor.spellbindr.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `character_progressions` (
                `characterId` TEXT NOT NULL,
                `stateJson` TEXT NOT NULL,
                PRIMARY KEY(`characterId`),
                FOREIGN KEY(`characterId`) REFERENCES `characters`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_character_progressions_characterId`
            ON `character_progressions` (`characterId`)
            """.trimIndent()
        )
    }
}
