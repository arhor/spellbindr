package com.github.arhor.spellbindr.ui.feature.characters.spellpicker

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
            onSourceClassChanged = vm::onSourceClassChanged,
            onQueryChanged = vm::onQueryChanged,
            onFavoriteClick = vm::onFavoritesToggled,
            onSpellClick = onSpellSelected,
        )
    }
}
