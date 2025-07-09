package com.github.arhor.spellbindr.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.arhor.spellbindr.data.model.CharacterEntity

@Database(
    entities = [CharacterEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SpellbindrDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
} 
