package com.github.arhor.spellbindr.data.local.assets

import android.content.Context
import com.github.arhor.spellbindr.domain.model.Ability
import com.github.arhor.spellbindr.logging.LoggerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbilityAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
    loggerFactory: LoggerFactory,
) : AssetDataStoreBase<Ability>(
    json = json,
    path = "data/abilities.json",
    context = context,
    serializer = Ability.serializer(),
    loggerFactory = loggerFactory,
    priority = AssetLoadingPriority.DEFERRED,
)
