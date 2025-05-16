package com.github.arhor.spellbindr.data.repository

import android.content.Context
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.model.SpellcastingClass
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SpellRepository @Inject constructor(
    private val context: Context,
) {
    val json = Json { ignoreUnknownKeys = true }

    private val spells by lazy {
        context.assets.open("spells/data.json")
            .bufferedReader()
            .use { it.readText() }
            .let { json.decodeFromString<List<Spell>>(it) }
            .sortedWith(compareBy<Spell> { it.level }.thenBy { it.name })
    }

    fun findSpellByName(name: String): Spell? = spells.find { it.name == name }

    fun findSpells(
        query: String? = null,
        classes: Set<SpellcastingClass>? = null,
    ): List<Spell> = findSpells(queries = query?.takeIf(String::isNotBlank)?.let(::listOf), classes = classes)

    fun findSpells(
        queries: List<String>? = null,
        classes: Set<SpellcastingClass>? = null,
    ): List<Spell> = spells.filter { it.shouldBeIncluded(queries, classes) }

    private fun Spell.shouldBeIncluded(
        queries: List<String>?,
        classes: Set<SpellcastingClass>?
    ): Boolean {
        return (queries.isNullOrEmpty() || queries.any { this.name.contains(it, ignoreCase = true) })
            && (classes.isNullOrEmpty() || classes.all { this.classes.contains(it) })
    }
}
