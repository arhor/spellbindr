package io.github.arhor.dnd.companion.data.local.assets

import android.content.Context
import io.github.arhor.dnd.companion.domain.model.Equipment
import io.github.arhor.dnd.companion.logging.LoggerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EquipmentAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
    loggerFactory: LoggerFactory,
) : AssetDataStoreBase<Equipment>(
    json = json,
    path = "data/equipment.json",
    context = context,
    serializer = Equipment.serializer(),
    loggerFactory = loggerFactory,
    priority = AssetLoadingPriority.DEFERRED,
)
