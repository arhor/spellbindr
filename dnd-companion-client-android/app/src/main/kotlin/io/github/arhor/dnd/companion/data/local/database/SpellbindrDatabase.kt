package io.github.arhor.dnd.companion.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.arhor.dnd.companion.data.local.database.converter.CharacterSheetConverter
import io.github.arhor.dnd.companion.data.local.database.converter.EntityRefConverter
import io.github.arhor.dnd.companion.data.local.database.dao.CharacterDao
import io.github.arhor.dnd.companion.data.local.database.dao.FavoritesDao
import io.github.arhor.dnd.companion.data.local.database.entity.CharacterEntity
import io.github.arhor.dnd.companion.data.local.database.entity.CharacterProgressionEntity
import io.github.arhor.dnd.companion.data.local.database.entity.FavoriteEntity

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
