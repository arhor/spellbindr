package com.github.arhor.spellbindr.ui.feature.character.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.feature.character.list.model.CharacterListItem
import com.github.arhor.spellbindr.ui.feature.character.list.model.CreateCharacterMode

/**
 * Stateful entry point for the Characters List screen.
 *
 * Sets up the top bar and delegates UI rendering to [CharactersListScreen].
 */
@Composable
fun CharactersListRoute(
    vm: CharactersListViewModel,
    onCharacterSelected: (CharacterListItem) -> Unit,
    onCreateCharacter: (CreateCharacterMode) -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = "Characters",
            ),
        ),
    ) {
        CharactersListScreen(
            state = state,
            dispatch = { intent ->
                when (intent) {
                    is CharactersListIntent.CharacterSelected -> onCharacterSelected(intent.character)
                    is CharactersListIntent.CreateCharacterSelected -> onCreateCharacter(intent.mode)
                }
            },
        )
    }
}
