package com.github.arhor.spellbindr.data.datasource.local

import android.content.Context
import com.github.arhor.spellbindr.data.model.Race
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RacesAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
) : StaticAssetDataStoreBase<Race>(
    json = json,
    path = "data/races.json",
    context = context,
    serializer = Race.serializer(),
) 
