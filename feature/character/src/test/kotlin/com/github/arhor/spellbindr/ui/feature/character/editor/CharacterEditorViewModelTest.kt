package com.github.arhor.spellbindr.ui.feature.character.editor

import androidx.lifecycle.SavedStateHandle
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.usecase.BuildCharacterSheetFromInputsUseCase
import com.github.arhor.spellbindr.domain.usecase.ComputeDerivedBonusesUseCase
import com.github.arhor.spellbindr.domain.usecase.ValidateCharacterSheetUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class CharacterEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `dispatch should update name field in content state`() {
        // Given
        val vm = CharacterEditorViewModel(
            loadCharacterSheetUseCase = mockk(relaxed = true),
            saveCharacterSheetUseCase = mockk(relaxed = true),
            validateCharacterSheetUseCase = mockk<ValidateCharacterSheetUseCase>(relaxed = true),
            computeDerivedBonusesUseCase = mockk<ComputeDerivedBonusesUseCase>(relaxed = true),
            buildCharacterSheetFromInputsUseCase = mockk<BuildCharacterSheetFromInputsUseCase>(relaxed = true),
            savedStateHandle = SavedStateHandle(),
        )

        // When
        vm.dispatch(CharacterEditorIntent.NameChanged("Astra"))

        // Then
        val state = vm.uiState.value as CharacterEditorUiState.Content
        assertThat(state.name).isEqualTo("Astra")
    }
}
