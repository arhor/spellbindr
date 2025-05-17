package com.github.arhor.spellbindr.ui.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoute(val title: String) {

    fun isCurrent(entry: NavBackStackEntry?): Boolean = when (entry) {
        null -> false
        else -> entry.destination.hasRoute(this::class)
    }

    @Serializable
    data object Spells : AppRoute(
        title = "Spell Book"
    )

    @Serializable
    data object SpellSearch : AppRoute(
        title = "Spell Book"
    )

    @Serializable
    data object FavoriteSpells : AppRoute(
        title = "Favorite Spells"
    )

    @Serializable
    data class SpellDetails(val spellName: String) : AppRoute(
        title = "Spell details"
    )

    @Serializable
    data object Characters : AppRoute(
        title = "Characters"
    )

    @Serializable
    data object CharacterSearch : AppRoute(
        title = "Characters List"
    )

    @Serializable
    data object CharacterCreate : AppRoute(
        title = "Create character"
    )
}
