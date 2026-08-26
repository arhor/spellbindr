package com.github.arhor.spellbindr.ui.feature.character.spellpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.domain.model.CharacterSpellAssignment
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState

@Composable
fun CharacterSpellPickerRoute(
    vm: CharacterSpellPickerViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSpellSelected: (CharacterSpellAssignment) -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = "Add Spells",
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        CharacterSpellPickerScreen(
            state = state,
            dispatch = { intent ->
                when (intent) {
                    is CharacterSpellPickerIntent.SpellClicked -> onSpellSelected(intent.assignment)
                    else -> vm.dispatch(intent)
                }
            },
        )
    }
}
