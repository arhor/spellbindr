package com.github.arhor.spellbindr.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.arhor.spellbindr.data.CharacterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE id = :id")
    fun getCharacterById(id: String): Flow<CharacterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCharacter(character: CharacterEntity)

    @Query("DELETE FROM characters WHERE id = :id")
    suspend fun deleteCharacter(id: String)
} 
