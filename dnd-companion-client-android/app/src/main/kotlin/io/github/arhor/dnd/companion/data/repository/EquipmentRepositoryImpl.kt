package io.github.arhor.dnd.companion.data.repository

import io.github.arhor.dnd.companion.data.local.assets.EquipmentAssetDataStore
import io.github.arhor.dnd.companion.domain.model.Equipment
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.EquipmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EquipmentRepositoryImpl @Inject constructor(
    private val equipmentDataStore: EquipmentAssetDataStore,
) : EquipmentRepository {

    override val allEquipmentState: Flow<Loadable<List<Equipment>>>
        get() = equipmentDataStore.data

    override suspend fun findEquipmentById(id: String): Equipment? =
        when (val state = equipmentDataStore.data.first { it !is Loadable.Loading }) {
            is Loadable.Content -> state.data.find { it.id == id }
            is Loadable.Failure -> null
            is Loadable.Loading -> null
        }
}
