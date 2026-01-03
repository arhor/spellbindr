package com.github.arhor.spellbindr.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.arhor.spellbindr.data.local.database.entity.CharacterEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the characters table.
 */
@Dao
interface CharacterDao {
    /**
     * Observes all characters in the database.
     */
    @Query("SELECT * FROM characters")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    /**
     * Observes a specific character by ID.
     */
    @Query("SELECT * FROM characters WHERE id = :id")
    fun getCharacterById(id: String): Flow<CharacterEntity?>

    /**
     * Inserts or replaces a character entity.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCharacter(character: CharacterEntity)

    /**
     * Deletes a character by ID.
     */
    @Query("DELETE FROM characters WHERE id = :id")
    suspend fun deleteCharacter(id: String)
}
