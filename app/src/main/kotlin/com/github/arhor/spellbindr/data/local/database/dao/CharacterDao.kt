package com.github.arhor.spellbindr.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.github.arhor.spellbindr.data.local.database.entity.CharacterEntity
import com.github.arhor.spellbindr.data.local.database.entity.CharacterProgressionEntity
import com.github.arhor.spellbindr.data.local.database.model.CharacterWithProgressionEntity
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

    @Transaction
    @Query("SELECT * FROM characters WHERE id = :id")
    fun observeCharacterWithProgression(id: String): Flow<CharacterWithProgressionEntity?>

    /**
     * Inserts a new character or updates the existing row without deleting it.
     *
     * A delete-and-insert replacement would trigger the progression foreign key's delete cascade.
     */
    @Upsert
    suspend fun saveCharacter(character: CharacterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgression(progression: CharacterProgressionEntity)

    @Transaction
    suspend fun saveCharacterWithProgression(
        character: CharacterEntity,
        progression: CharacterProgressionEntity,
    ) {
        saveCharacter(character)
        saveProgression(progression)
    }

    /**
     * Deletes a character by ID.
     */
    @Query("DELETE FROM characters WHERE id = :id")
    suspend fun deleteCharacter(id: String)
}
