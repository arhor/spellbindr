package com.github.arhor.spellbindr.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.arhor.spellbindr.data.local.database.converter.Converters
import com.github.arhor.spellbindr.data.local.database.dao.CharacterDao
import com.github.arhor.spellbindr.data.local.database.dao.FavoritesDao
import com.github.arhor.spellbindr.data.local.database.entity.CharacterEntity
import com.github.arhor.spellbindr.data.local.database.entity.FavoriteEntity

@Database(
    entities = [
        CharacterEntity::class,
        FavoriteEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class SpellbindrDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun favoritesDao(): FavoritesDao

    companion object {
        val allMigrations: Array<Migration>
            get() = arrayOf(MIGRATION_1_2, MIGRATION_2_3)

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE characters
                        ADD COLUMN manualSheet TEXT NULL
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS favorites (
                        type     TEXT NOT NULL,
                        entityId TEXT NOT NULL,
                        PRIMARY KEY (type, entityId)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_favorites_type
                        ON favorites (type)
                    """.trimIndent()
                )
            }
        }
    }
} 
