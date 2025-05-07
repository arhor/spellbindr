package com.github.arhor.spellbindr.data.repository

import android.content.Context
import com.github.arhor.spellbindr.data.model.Spell
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SpellRepository @Inject constructor(
    private val context: Context,
) {
    val json = Json { ignoreUnknownKeys = true }

    private val spells by lazy {
        context.assets.open("spells.json")
            .bufferedReader()
            .use { it.readText() }
            .let { json.decodeFromString<List<Spell>>(it) }
    }

    fun searchSpells(query: String): List<Spell> = spells.filter {
        with(it) {
            name.contains(query, ignoreCase = true)
        }
    }

    fun getAllSpells(): List<Spell> = spells
}