package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.LocalSnackbarHostState
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CharacterLevelUpRoute(
    onBack: () -> Unit,
    onFinished: () -> Unit,
    vm: CharacterLevelUpViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    LaunchedEffect(vm) {
        vm.effects.collectLatest { effect ->
            when (effect) {
                CharacterLevelUpEffect.Cancelled -> onBack()
                CharacterLevelUpEffect.Completed -> onFinished()
                is CharacterLevelUpEffect.Message -> snackbar.showSnackbar(effect.text)
            }
        }
    }
    val title = (state as? CharacterLevelUpUiState.Content)?.let {
        "Level up · ${it.step.title} (${it.currentStepIndex + 1}/${it.steps.size})"
    } ?: "Level up"
    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = title,
                navigation = AppTopBarNavigation.Back {
                    val content = state as? CharacterLevelUpUiState.Content
                    if (content != null && content.currentStepIndex > 0) vm.dispatch(CharacterLevelUpIntent.BackClicked)
                    else vm.dispatch(CharacterLevelUpIntent.CancelClicked)
                },
            ),
        ),
    ) {
        CharacterLevelUpRouteContent(state = state, dispatch = vm::dispatch)
    }
}

@Composable
internal fun CharacterLevelUpRouteContent(
    state: CharacterLevelUpUiState,
    dispatch: CharacterLevelUpDispatch,
    modifier: Modifier = Modifier,
) {
    if (state is CharacterLevelUpUiState.Failure && state.canRetry) {
        CharacterLevelUpFailurePane(
            message = state.message,
            onRetry = { dispatch(CharacterLevelUpIntent.RetryClicked) },
            modifier = modifier,
        )
    } else {
        CharacterLevelUpScreen(state = state, dispatch = dispatch, modifier = modifier)
    }
}
