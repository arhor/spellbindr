@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.github.arhor.spellbindr.ui.components.AppBottomBar
import com.github.arhor.spellbindr.ui.components.AppTopBarControllerProvider
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
                topBar = {
                    if (config.visible) {
                        TopAppBar(
                            title = config.title,
                            navigationIcon = config.navigation.asNavigationIcon(),
                            actions = config.actions,
                        )
                    }
                },
                bottomBar = {
                    AppBottomBar(controller)
                },
            ) { innerPadding ->
                SpellbindrAppNavGraph(
                    controller = controller,
                    innerPadding = innerPadding,
                )
            }
        }
    }
}
