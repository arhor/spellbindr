package io.github.arhor.dnd.companion.data.local.assets

import android.content.Context
import io.github.arhor.dnd.companion.domain.model.Feat
import io.github.arhor.dnd.companion.logging.LoggerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatsAssetDataStore @Inject constructor(
    @ApplicationContext context: Context,
    json: Json,
    loggerFactory: LoggerFactory,
) : AssetDataStoreBase<Feat>(
    json = json,
    path = "data/feats.json",
    context = context,
    serializer = Feat.serializer(),
    loggerFactory = loggerFactory,
)
