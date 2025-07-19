package com.github.arhor.spellbindr.features.conditions

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.github.arhor.spellbindr.core.utils.AppRoute
import com.github.arhor.spellbindr.features.conditions.search.ConditionSearchScreen
import kotlinx.serialization.Serializable

@Serializable
data object Conditions : AppRoute(title = "Conditions") {
    @Serializable
    data object Search : AppRoute(title = "Conditions")
}

fun NavGraphBuilder.conditionsNavGraph(controller: NavController) {
    navigation<Conditions>(startDestination = Conditions.Search) {
        composable<Conditions.Search> {
            ConditionSearchScreen()
        }
    }
}
