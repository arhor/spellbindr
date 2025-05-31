package com.github.arhor.spellbindr.data.datasource.local

import android.content.Context
import com.github.arhor.spellbindr.data.model.Equipment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EquipmentAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
) : StaticAssetDataStoreBase<Equipment>(
    json = json,
    path = "data/equipment.json",
    context = context,
    serializer = Equipment.serializer(),
) 