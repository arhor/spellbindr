package com.github.arhor.spellbindr.data.local.assets

import android.content.Context
import com.github.arhor.spellbindr.data.model.CharacterRace
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRaceAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
) : StaticAssetDataStoreBase<CharacterRace>(
    json = json,
    path = "data/races.json",
    context = context,
    serializer = CharacterRace.serializer(),
) 
