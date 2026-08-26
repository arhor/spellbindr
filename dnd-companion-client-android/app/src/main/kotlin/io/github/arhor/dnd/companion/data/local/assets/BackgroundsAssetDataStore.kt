package io.github.arhor.dnd.companion.data.local.assets

import android.content.Context
import io.github.arhor.dnd.companion.domain.model.Background
import io.github.arhor.dnd.companion.logging.LoggerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundsAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
    loggerFactory: LoggerFactory,
) : AssetDataStoreBase<Background>(
    json = json,
    path = "data/backgrounds.json",
    context = context,
    serializer = Background.serializer(),
    loggerFactory = loggerFactory,
    priority = AssetLoadingPriority.DEFERRED,
)
