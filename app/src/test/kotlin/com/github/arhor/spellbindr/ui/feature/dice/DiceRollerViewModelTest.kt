package com.github.arhor.spellbindr.ui.feature.dice

import com.github.arhor.spellbindr.ui.feature.dice.model.RollResult
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DiceRollerViewModelTest {

    @Test
    fun `dispatch should toggle check section`() {
        // Given
        val vm = DiceRollerViewModel()

        // When
        vm.dispatch(DiceRollerIntent.ToggleCheck)

        // Then
        val content = vm.uiState.value as DiceRollerUiState.Content
        assertThat(content.hasCheck).isTrue()
    }

    @Test
    fun `dispatch should produce percentile result when roll percentile requested`() {
        // Given
        val vm = DiceRollerViewModel()

        // When
        vm.dispatch(DiceRollerIntent.RollPercentile)

        // Then
        val content = vm.uiState.value as DiceRollerUiState.Content
        assertThat(content.latestResult).isInstanceOf(RollResult.PercentileResult::class.java)
        assertThat(content.lastPercentileRoll).isNotNull()
    }
}
