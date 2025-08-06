package com.github.arhor.spellbindr.ui.navigation

import com.github.arhor.spellbindr.utils.AppRoute
import kotlinx.serialization.Serializable

@Serializable
data object Library : AppRoute(title = "Library") {
    @Serializable
    data object Main : AppRoute(title = "Library")
}

@Serializable
data object Conditions : AppRoute(title = "Conditions")

@Serializable
data object Alignments : AppRoute(title = "Alignments")

@Serializable
data object Races : AppRoute(title = "Races")

@Serializable
data object Spells : AppRoute(title = "Spells") {
    @Serializable
    data object Search : AppRoute(title = "Spell Book")

    @Serializable
    data class Details(val spellName: String) : AppRoute(title = "Spell Details")
}

@Serializable
data object Characters : AppRoute(title = "Characters") {

    @Serializable
    data object Search : AppRoute(title = "Characters List")

    @Serializable
    data class Details(val characterId: String) : AppRoute(title = "Character Details")

    @Serializable
    data object Create : AppRoute(title = "Create character") {

        @Serializable
        data object NameAndBackground : AppRoute(title = "Name and Background")

        @Serializable
        data object BackgroundDetails : AppRoute(title = "Background Details")

        @Serializable
        data object Race : AppRoute(title = "Race")

        @Serializable
        data object Class : AppRoute(title = "Class")

        @Serializable
        data object Abilities : AppRoute(title = "Abilities")

        @Serializable
        data object Skills : AppRoute(title = "Skills")

        @Serializable
        data object Spells : AppRoute(title = "Spells")

        @Serializable
        data object Summary : AppRoute(title = "Summary")
    }
}
