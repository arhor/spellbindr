package com.github.arhor.spellbindr.data.local.assets

import android.content.Context
import com.github.arhor.spellbindr.domain.model.Feature
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeaturesAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
) : StaticAssetDataStoreBase<Feature>(
    json = json,
    path = "data/features.json",
    context = context,
    serializer = Feature.serializer(),
    loadingPriority = AssetLoadingPriority.DEFERRED,
)

