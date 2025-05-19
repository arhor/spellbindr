package com.github.arhor.spellbindr.data.repository

import android.content.Context
import com.github.arhor.spellbindr.data.model.Race
import com.github.arhor.spellbindr.data.model.StaticAsset
import kotlinx.serialization.json.Json
import javax.inject.Inject

class RaceRepository @Inject constructor(
    private val context: Context,
    private val json: Json,
) {
    private val races by lazy {
        context.assets.open("races/data.json")
            .bufferedReader()
            .use { it.readText() }
            .let { json.decodeFromString<StaticAsset<Race, Unit>>(it).data }
    }

    fun getAllRaces(): List<Race> = races
} 
