package com.github.arhor.spellbindr.features.conditions

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.github.arhor.spellbindr.core.utils.AppRoute
import com.github.arhor.spellbindr.features.conditions.details.ConditionDetailsScreen
import com.github.arhor.spellbindr.features.conditions.search.ConditionListScreen
import kotlinx.serialization.Serializable

@Serializable
data object Conditions : AppRoute(title = "Conditions") {
    @Serializable
    data object Search : AppRoute(title = "Conditions")

    @Serializable
    data class Details(val conditionName: String) : AppRoute(title = "Condition Details")
}

fun NavGraphBuilder.conditionsNavGraph(controller: NavController) {
    navigation<Conditions>(startDestination = Conditions.Search) {
        composable<Conditions.Search> {
            ConditionListScreen()
        }
        composable<Conditions.Details> {
            val details = it.toRoute<Conditions.Details>()
            ConditionDetailsScreen(
                conditionName = details.conditionName,
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
