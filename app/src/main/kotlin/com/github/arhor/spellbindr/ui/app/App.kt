package com.github.arhor.spellbindr.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.arhor.spellbindr.ui.navigation.AppNavGraph
import com.github.arhor.spellbindr.ui.theme.SpellbindrTheme

@Composable
fun App(
    onLoaded: () -> Unit,
    viewModel: AppViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.ready) {
        if (state.ready) {
            onLoaded()
        }
    }

    SpellbindrTheme {
        AppNavGraph()
    }
}
