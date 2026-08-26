package com.github.arhor.spellbindr.data.local.assets

import android.content.Context
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.logging.LoggerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRaceAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
    loggerFactory: LoggerFactory,
) : AssetDataStoreBase<Race>(
    json = json,
    path = "data/races.json",
    context = context,
    serializer = Race.serializer(),
    loggerFactory = loggerFactory,
)
