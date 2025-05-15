package com.github.arhor.spellbindr.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoute(val title: String) {

    @Serializable
    data object SpellSearch : AppRoute(
        title = "Spells"
    )

    @Serializable
    data object FavoriteSpells : AppRoute(
        title = "Favorites"
    )

    @Serializable
    data object Characters : AppRoute(
        title = "Characters"
    )

    @Serializable
    data object CharacterCreate : AppRoute(
        title = "Create character"
    )

    @Serializable
    data class SpellDetails(val spellName: String) : AppRoute(
        title = "Spell details"
    )
}
