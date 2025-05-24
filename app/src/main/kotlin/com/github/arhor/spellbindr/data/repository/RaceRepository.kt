package com.github.arhor.spellbindr.data.repository

import android.content.Context
import com.github.arhor.spellbindr.data.model.Race
import com.github.arhor.spellbindr.data.model.StaticAsset
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RaceRepository @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
) : StaticAssetLoaderBase<Race, Unit>(
    context = context,
    json = json,
    path = "races/data.json",
    serializer = StaticAsset.serializer(Race.serializer(), Unit.serializer())
) {

    suspend fun findRaces(): List<Race> = getAsset()
} 
