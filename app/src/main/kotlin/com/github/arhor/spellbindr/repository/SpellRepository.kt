package com.github.arhor.spellbindr.repository

import android.content.Context
import com.github.arhor.spellbindr.data.model.Spell
import kotlinx.serialization.json.Json
import org.koin.dsl.module

class SpellRepository(
    private val context: Context
) {
    private val spells: List<Spell> by lazy {
        val jsonString = context.assets.open("spells.json").bufferedReader().use { it.readText() }
        Json { ignoreUnknownKeys = true }.decodeFromString<List<Spell>>(jsonString)
    }

    fun getAllSpells(): List<Spell> = spells

    fun searchSpells(query: String): List<Spell> = spells.filter {
        with(it) {
            name.contains(query, ignoreCase = true) || desc.contains(query, ignoreCase = true)
        }
    }

    fun getSpellsByLevel(level: Int): List<Spell> = spells.filter { it.level == level }
}
