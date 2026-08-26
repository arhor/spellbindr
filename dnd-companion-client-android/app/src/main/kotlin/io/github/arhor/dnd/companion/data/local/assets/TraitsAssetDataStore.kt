package io.github.arhor.dnd.companion.data.local.assets

import android.content.Context
import io.github.arhor.dnd.companion.domain.model.Trait
import io.github.arhor.dnd.companion.logging.LoggerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraitsAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
    loggerFactory: LoggerFactory,
) : AssetDataStoreBase<Trait>(
    json = json,
    path = "data/traits.json",
    context = context,
    serializer = Trait.serializer(),
    loggerFactory = loggerFactory,
)
