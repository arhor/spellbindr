package com.github.arhor.spellbindr.data.local.assets

import android.content.Context
import com.github.arhor.spellbindr.domain.model.Feat
import com.github.arhor.spellbindr.logging.LoggerFactory
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
