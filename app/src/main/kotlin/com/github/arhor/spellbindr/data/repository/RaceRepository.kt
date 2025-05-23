package com.github.arhor.spellbindr.data.repository

import android.content.Context
import com.github.arhor.spellbindr.data.model.Race
import com.github.arhor.spellbindr.data.model.StaticAsset
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RaceRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val json: Json,
) : DataLoader {
    private lateinit var data: List<Race>
    private val mutex = Mutex()

    override suspend fun loadData() {
        if (!::data.isInitialized) {
            mutex.withLock {
                if (!::data.isInitialized) {
                    data = withContext(Dispatchers.IO) {
                        context.assets.open("races/data.json")
                            .bufferedReader()
                            .use { it.readText() }
                            .let { json.decodeFromString<StaticAsset<Race, Unit>>(it).data }
                    }
                }
            }
        }
    }

    override val resource: String
        get() = "Races"

    suspend fun findRaces(): List<Race> = races()

    private suspend fun races(): List<Race> {
        loadData()
        return data
    }
} 
