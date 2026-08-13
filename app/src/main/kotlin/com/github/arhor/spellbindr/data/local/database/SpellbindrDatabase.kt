package com.github.arhor.spellbindr.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.arhor.spellbindr.data.local.database.converter.CharacterSheetConverter
import com.github.arhor.spellbindr.data.local.database.converter.EntityRefConverter
import com.github.arhor.spellbindr.data.local.database.dao.CharacterDao
import com.github.arhor.spellbindr.data.local.database.dao.FavoritesDao
import com.github.arhor.spellbindr.data.local.database.entity.CharacterEntity
import com.github.arhor.spellbindr.data.local.database.entity.CharacterProgressionEntity
import com.github.arhor.spellbindr.data.local.database.entity.FavoriteEntity

@Database(
    entities = [
        CharacterEntity::class,
        CharacterProgressionEntity::class,
        FavoriteEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    value = [
        CharacterSheetConverter::class,
        EntityRefConverter::class,
    ]
)
abstract class SpellbindrDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun favoritesDao(): FavoritesDao
} 
