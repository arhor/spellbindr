@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarControllerProvider
import com.github.arhor.spellbindr.ui.navigation.AppDestination
import com.github.arhor.spellbindr.ui.navigation.BottomNavItems
import com.github.arhor.spellbindr.ui.navigation.SpellbindrAppNavGraph
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SpellbindrApp(
    onLoaded: () -> Unit,
    vm: SpellbindrAppViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val controller = rememberNavController()

    LaunchedEffect(state.ready) {
        if (state.ready) {
            onLoaded()
        }
    }

    AppTheme(isDarkTheme = state.isDarkTheme) {
        AppTopBarControllerProvider { config ->
            Scaffold(
                topBar = createTopBar(config),
                bottomBar = { AppBottomBar(controller) },
            ) { innerPadding ->
                SpellbindrAppNavGraph(
                    controller = controller,
                    innerPadding = innerPadding,
                )
            }
        }
    }
}

private fun createTopBar(config: AppTopBarConfig): @Composable () -> Unit = {
    if (config.visible) {
        TopAppBar(
            title = config.title,
            navigationIcon = config.navigation.asNavigationIcon(),
            actions = config.actions,
        )
    }
}

@Composable
private fun AppBottomBar(controller: NavHostController) {
    val backStackEntry by controller.currentBackStackEntryAsState()

    NavigationBar {
        BottomNavItems.forEach { item ->
            val isSelected = backStackEntry?.destination matches item.destination
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    controller.navigate(item.destination) {
                        popUpTo(controller.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                    )
                },
                label = { Text(item.label) },
            )
        }
    }
}

private infix fun NavDestination?.matches(destination: AppDestination): Boolean = when (this) {
    null -> false
    else -> hierarchy.any { it.hasRoute(destination::class) }
}
