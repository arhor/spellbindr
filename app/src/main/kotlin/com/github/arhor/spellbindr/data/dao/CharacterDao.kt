package com.github.arhor.spellbindr.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.arhor.spellbindr.data.model.Character
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters")
    fun getAll(): Flow<List<Character>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(character: Character)
}