package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Equipment
import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface EquipmentRepository {
    val allEquipmentState: Flow<Loadable<List<Equipment>>>

    suspend fun findEquipmentById(id: String): Equipment?
}
