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
