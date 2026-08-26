package io.github.arhor.dnd.companion.ui.feature.character.guided

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.arhor.dnd.companion.ui.components.AppTopBarConfig
import io.github.arhor.dnd.companion.ui.components.AppTopBarNavigation
import io.github.arhor.dnd.companion.ui.components.LocalSnackbarHostState
import io.github.arhor.dnd.companion.ui.components.ProvideTopBarState
import io.github.arhor.dnd.companion.ui.components.TopBarState
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GuidedCharacterSetupRoute(
    onBack: () -> Unit,
    onFinished: (String) -> Unit,
    vm: GuidedCharacterSetupViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(vm.effects) {
        vm.effects.collectLatest {
            when (it) {
                is GuidedCharacterSetupEffect.CharacterCreated -> onFinished(it.characterId)
                is GuidedCharacterSetupEffect.Error -> snackbarHostState.showSnackbar(it.message)
            }
        }
    }

    val title = when (val uiState = state) {
        is GuidedCharacterSetupUiState.Content ->
            "Guided setup · ${uiState.step.title} (${uiState.currentStepIndex + 1}/${uiState.totalSteps})"

        else -> "Guided setup"
    }

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = title,
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        GuidedCharacterSetupScreen(
            state = state,
            dispatch = vm::dispatch,
        )
    }
}
