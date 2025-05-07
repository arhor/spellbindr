package com.github.arhor.spellbindr.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.arhor.spellbindr.ui.theme.SpellbindrTheme
import com.github.arhor.spellbindr.viewmodel.SpellSearchViewModel
import kotlinx.coroutines.delay

@Composable
fun App(onLoaded: () -> Unit) {
    val spellsViewModel = hiltViewModel<SpellSearchViewModel>()
    val spellsViewState by spellsViewModel.state.collectAsState()

    var initialDelayPassed by remember { mutableStateOf(false) }
    var initialLoadingDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1500)
        initialDelayPassed = true
    }
    LaunchedEffect(initialDelayPassed, spellsViewState.isLoading) {
        if (initialDelayPassed && !initialLoadingDone && !spellsViewState.isLoading) {
            initialLoadingDone = true
            onLoaded()
        }
    }
    SpellbindrTheme {
        AppNavGraph()
    }
}
