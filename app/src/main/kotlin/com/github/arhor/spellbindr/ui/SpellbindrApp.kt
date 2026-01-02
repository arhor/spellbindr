package com.github.arhor.spellbindr.ui

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.arhor.spellbindr.ui.components.AppBottomBar
import com.github.arhor.spellbindr.ui.components.AppTopBar
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.LocalTopBarState
import com.github.arhor.spellbindr.ui.components.rememberTopBarStateHolder
import com.github.arhor.spellbindr.ui.navigation.AppDestination
import com.github.arhor.spellbindr.ui.navigation.SpellbindrAppNavGraph
import com.github.arhor.spellbindr.ui.theme.AppTheme
import kotlin.reflect.KClass

@Composable
fun SpellbindrApp(onReady: () -> Unit) {
    val vm = hiltViewModel<SpellbindrAppViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    val controller = rememberNavController()
    val topBarStateHolder = rememberTopBarStateHolder()
    val snackbarHostState = remember { SnackbarHostState() }
    val lastErrorMessage = remember { mutableStateOf<String?>(null) }
    val backStackEntry by controller.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val topBarState by topBarStateHolder
    val defaultConfig by remember(destination) {
        derivedStateOf {
            defaultTopBarConfig(destination)
        }
    }
    val resolvedConfig by remember(topBarState.config, defaultConfig) {
        derivedStateOf {
            topBarState.config ?: defaultConfig
        }
    }

    LaunchedEffect(state.readyForInteraction) {
        if (state.readyForInteraction) {
            onReady()
        }
    }

    LaunchedEffect(state.criticalAssetsError, state.deferredAssetsError) {
        val messages = buildList {
            if (state.criticalAssetsError != null) {
                add("Failed to load critical app data. Please restart the app.")
            }
            if (state.deferredAssetsError != null) {
                add("Some data failed to load. Parts of the app may be missing.")
            }
        }
        if (messages.isEmpty()) {
            lastErrorMessage.value = null
            return@LaunchedEffect
        }
        for (message in messages) {
            if (lastErrorMessage.value != message) {
                lastErrorMessage.value = message
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    AppTheme(isDarkTheme = state.isDarkTheme) {
        CompositionLocalProvider(LocalTopBarState provides topBarStateHolder) {
            Scaffold(
                topBar = { AppTopBar(resolvedConfig) },
                bottomBar = { AppBottomBar(controller) },
                snackbarHost = { SnackbarHost(snackbarHostState) },
            ) { innerPadding ->
                SpellbindrAppNavGraph(
                    controller = controller,
                    innerPadding = innerPadding,
                )
                topBarState.overlays()
            }
        }
    }
}

private fun defaultTopBarConfig(destination: NavDestination?): AppTopBarConfig =
    when {
        destination matches AppDestination.CharactersHome::class -> AppTopBarConfig(
            title = { Text(text = "Characters") },
        )

        destination matches AppDestination.CompendiumSections::class -> AppTopBarConfig(
            title = { Text(text = "Compendium") },
        )

        destination matches AppDestination.Dice::class -> AppTopBarConfig(
            title = { Text(text = "Dice Roller") },
        )

        destination matches AppDestination.Settings::class -> AppTopBarConfig(
            title = { Text(text = "Settings") },
        )

        else -> AppTopBarConfig.None
    }

private infix fun NavDestination?.matches(destination: KClass<out AppDestination>): Boolean =
    when (this) {
        null -> false
        else -> hierarchy.any { it.hasRoute(destination) }
    }
