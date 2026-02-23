package com.github.arhor.spellbindr.ui.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.LocalSnackbarHostState
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState

@Composable
fun SettingsRoute(
    vm: SettingsViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(vm) {
        vm.effects.collect { effect ->
            when (effect) {
                is SettingsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = "Settings",
            ),
        ),
    ) {
        SettingsScreen(
            state = state,
            dispatch = vm::dispatch,
        )
    }
}
