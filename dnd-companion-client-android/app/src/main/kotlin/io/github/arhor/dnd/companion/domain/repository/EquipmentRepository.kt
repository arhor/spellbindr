package io.github.arhor.dnd.companion.domain.repository

import io.github.arhor.dnd.companion.domain.model.Equipment
import io.github.arhor.dnd.companion.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface EquipmentRepository {
    val allEquipmentState: Flow<Loadable<List<Equipment>>>

    suspend fun findEquipmentById(id: String): Equipment?
}
