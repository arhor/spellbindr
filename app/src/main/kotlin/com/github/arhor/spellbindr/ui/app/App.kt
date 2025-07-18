package com.github.arhor.spellbindr.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.arhor.spellbindr.core.theme.SpellbindrTheme
import com.github.arhor.spellbindr.ui.navigation.AppNavGraph

@Composable
fun App(onLoaded: () -> Unit) {
    val appViewModel = hiltViewModel<AppViewModel>()
    val appViewState by appViewModel.state.collectAsState()

    LaunchedEffect(appViewState.ready) {
        if (appViewState.ready) {
            onLoaded()
        }
    }

    SpellbindrTheme {
        AppNavGraph()
    }
}
