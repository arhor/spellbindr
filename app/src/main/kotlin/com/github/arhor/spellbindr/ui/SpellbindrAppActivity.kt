package com.github.arhor.spellbindr.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpellbindrAppActivity : ComponentActivity() {
    private var isSplashVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                isSplashVisible
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm = hiltViewModel<SpellbindrAppViewModel>()
            val state by vm.state.collectAsStateWithLifecycle()
            val controller = rememberNavController()
            val topBarStateHolder = rememberTopBarStateHolder()
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
                    isSplashVisible = false
                }
            }

            AppTheme(isDarkTheme = state.isDarkTheme) {
                CompositionLocalProvider(LocalTopBarState provides topBarStateHolder) {
                    Scaffold(
                        topBar = { AppTopBar(resolvedConfig) },
                        bottomBar = { AppBottomBar(controller) },
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
    }
}

private fun defaultTopBarConfig(destination: NavDestination?): AppTopBarConfig =
    when {
        destination matches AppDestination.CharactersHome::class -> AppTopBarConfig(
            visible = true,
            title = { Text(text = "Characters") },
        )

        destination matches AppDestination.Compendium::class -> AppTopBarConfig(
            visible = true,
            title = { Text(text = "Compendium") },
        )

        destination matches AppDestination.Dice::class -> AppTopBarConfig(
            visible = true,
            title = { Text(text = "Dice Roller") },
        )

        destination matches AppDestination.Settings::class -> AppTopBarConfig(
            visible = true,
            title = { Text(text = "Settings") },
        )

        else -> AppTopBarConfig.None
    }

private infix fun NavDestination?.matches(destination: kotlin.reflect.KClass<out AppDestination>): Boolean =
    when (this) {
        null -> false
        else -> hierarchy.any { it.hasRoute(destination) }
    }
