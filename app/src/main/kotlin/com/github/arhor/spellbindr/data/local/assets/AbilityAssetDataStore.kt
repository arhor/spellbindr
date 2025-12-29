package com.github.arhor.spellbindr.data.local.assets

import android.content.Context
import com.github.arhor.spellbindr.domain.model.Ability
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbilityAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
) : StaticAssetDataStoreBase<Ability>(
    json = json,
    path = "data/abilities.json",
    context = context,
    serializer = Ability.serializer(),
    loadingPriority = AssetLoadingPriority.DEFERRED,
)
