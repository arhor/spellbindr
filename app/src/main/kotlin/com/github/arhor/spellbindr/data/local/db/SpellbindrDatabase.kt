package com.github.arhor.spellbindr.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.arhor.spellbindr.data.CharacterEntity

@Database(
    entities = [CharacterEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SpellbindrDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE characters ADD COLUMN manualSheet TEXT")
            }
        }
    }
} 
