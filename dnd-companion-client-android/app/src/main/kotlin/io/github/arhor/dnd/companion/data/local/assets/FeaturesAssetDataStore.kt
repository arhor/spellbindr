package io.github.arhor.dnd.companion.data.local.assets

import android.content.Context
import io.github.arhor.dnd.companion.domain.model.Feature
import io.github.arhor.dnd.companion.logging.LoggerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeaturesAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
    loggerFactory: LoggerFactory,
) : AssetDataStoreBase<Feature>(
    json = json,
    path = "data/features.json",
    context = context,
    serializer = Feature.serializer(),
    loggerFactory = loggerFactory,
    priority = AssetLoadingPriority.DEFERRED,
)
