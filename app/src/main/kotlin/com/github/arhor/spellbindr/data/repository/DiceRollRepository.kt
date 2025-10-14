package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.model.RollHistoryEntry
import com.github.arhor.spellbindr.data.model.RollSet
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing dice roll data
 */
interface DiceRollRepository {
    suspend fun saveRoll(rollSet: RollSet)
    fun getRollHistory(): Flow<List<RollHistoryEntry>>
    suspend fun clearHistory()
    suspend fun deleteRoll(rollId: String)
}
