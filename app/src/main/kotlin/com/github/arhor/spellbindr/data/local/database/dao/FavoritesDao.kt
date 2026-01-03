package com.github.arhor.spellbindr.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.arhor.spellbindr.data.local.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Query("SELECT entityId FROM favorites WHERE type = :type")
    fun observeFavoriteIds(type: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM favorites WHERE type = :type AND entityId = :entityId")
    suspend fun isFavorite(type: String, entityId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)
}
