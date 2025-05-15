package com.github.arhor.spellbindr.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Destination(val route: String) {

    object SpellSearch : Destination("spell-search")

    object FavoriteSpells : Destination("spell-lists")

    object Characters : Destination("characters")

    object CharacterCreate : Destination("characters/create")

    object SpellDetail : Destination("spell-detail/{spellName}") {
        fun createRoute(spellName: String) = "spell-detail/$spellName"
        const val ARG_SPELL_NAME = "spellName"
    }
}
