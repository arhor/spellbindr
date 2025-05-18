package com.github.arhor.spellbindr.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoute(val title: String) {

    infix fun inSameHierarchyWith(destination: NavDestination?): Boolean = when (destination) {
        null -> false
        else -> destination.hierarchy.any { it.hasRoute(this::class) }
    }

    @Serializable
    data object Spells : AppRoute(title = "Spell Book") {
        @Serializable
        data object Search : AppRoute(title = "Spell Book")

        @Serializable
        data object Favorite : AppRoute(title = "Favorite Spells")

        @Serializable
        data class Details(val spellName: String) : AppRoute(title = "Spell Details")
    }

    @Serializable
    data object Characters : AppRoute(title = "Characters") {

        @Serializable
        data object Search : AppRoute(title = "Characters List")

        @Serializable
        data object Create : AppRoute(title = "Create character") {

            @Serializable
            data object NameAndBackground : AppRoute(title = "Name and Background")

            @Serializable
            data object Race : AppRoute(title = "Race")

            @Serializable
            data object Class : AppRoute(title = "Class")

            @Serializable
            data object Abilities : AppRoute(title = "Abilities")

            @Serializable
            data object Skills : AppRoute(title = "Skills")

            @Serializable
            data object Equipment : AppRoute(title = "Equipment")

            @Serializable
            data object Spells : AppRoute(title = "Spells")

            @Serializable
            data object Appearance : AppRoute(title = "Appearance")

            @Serializable
            data object Summary : AppRoute(title = "Summary")
        }
    }
}
