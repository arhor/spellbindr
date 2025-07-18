package com.github.arhor.spellbindr.features.spells

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.github.arhor.spellbindr.core.utils.AppRoute
import com.github.arhor.spellbindr.features.spells.details.SpellDetailScreen
import com.github.arhor.spellbindr.features.spells.search.SpellSearchScreen
import kotlinx.serialization.Serializable

@Serializable
data object Spells : AppRoute(title = "Spells") {
    @Serializable
    data object Search : AppRoute(title = "Spell Book")

    @Serializable
    data class Details(val spellName: String) : AppRoute(title = "Spell Details")
}

fun NavGraphBuilder.spellsNavGraph(controller: NavController) {
    navigation<Spells>(
        startDestination = Spells.Search
    ) {
        composable<Spells.Search> {
            SpellSearchScreen(
                onSpellClick = { controller navigateTo Spells.Details(it) },
            )
        }
        composable<Spells.Details> {
            SpellDetailScreen(
                spellName = it.toRoute<Spells.Details>().spellName,
                onBackClick = { controller.navigateUp() },
            )
        }
    }
}

// TODO: replace all duplications with a single function
private infix fun NavController.navigateTo(route: AppRoute) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
