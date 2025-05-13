package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable
import kotlin.String

@Serializable
data class SpellList(
    val name: String,
    val spellNames: List<String>
) {
    companion object {
        val EMPTY = SpellList(
            name = "Empty",
            spellNames = emptyList(),
        )
    }
}
