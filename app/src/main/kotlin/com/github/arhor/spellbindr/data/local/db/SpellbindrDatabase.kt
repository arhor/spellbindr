package com.github.arhor.spellbindr.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.arhor.spellbindr.data.CharacterEntity

@Database(
    entities = [CharacterEntity::class, FavoriteEntity::class],
    version = 3,
    exportSchema = false
)
abstract class SpellbindrDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun favoritesDao(): FavoritesDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE characters ADD COLUMN manualSheet TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS favorites (
                        type TEXT NOT NULL,
                        entityId TEXT NOT NULL,
                        PRIMARY KEY(type, entityId)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_favorites_type ON favorites(type)")
            }
        }
    }
} 
