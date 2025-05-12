package com.github.arhor.spellbindr.data.repository

import android.content.Context
import com.github.arhor.spellbindr.data.model.Race
import com.github.arhor.spellbindr.data.model.RaceList
import kotlinx.serialization.json.Json
import javax.inject.Inject

class RaceRepository @Inject constructor(
    private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val races by lazy {
        context.assets.open("races.json")
            .bufferedReader()
            .use { it.readText() }
            .let { json.decodeFromString(RaceList.serializer(), it).data }
    }

    fun getAllRaces(): List<Race> = races
} 