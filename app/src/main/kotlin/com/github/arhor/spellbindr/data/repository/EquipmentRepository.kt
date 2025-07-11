package com.github.arhor.spellbindr.data.repository

import androidx.compose.runtime.Stable
import com.github.arhor.spellbindr.data.local.assets.EquipmentAssetDataStore
import com.github.arhor.spellbindr.data.model.Equipment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class EquipmentRepository @Inject constructor(
    private val equipmentDataStore: EquipmentAssetDataStore,
) {
    val allEquipment: Flow<List<Equipment>>
        get() = equipmentDataStore.data.map { it ?: emptyList() }

    suspend fun findEquipmentById(id: String): Equipment? =
        allEquipment.firstOrNull()?.find { it.id == id }
} 
