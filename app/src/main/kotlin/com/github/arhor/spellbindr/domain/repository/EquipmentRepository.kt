package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.Equipment
import kotlinx.coroutines.flow.Flow

interface EquipmentRepository {
    val allEquipmentState: Flow<AssetState<List<Equipment>>>

    suspend fun findEquipmentById(id: String): Equipment?
}
