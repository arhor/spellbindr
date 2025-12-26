package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.SpellSlotState
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class ToggleSpellSlotUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase = ToggleSpellSlotUseCase()

    @Test
    fun `invoke should update expended count when toggling spell slot`() {
        // Given
        val sheet = CharacterSheet(
            id = "hero",
            spellSlots = listOf(SpellSlotState(level = 1, total = 2, expended = 0)),
        )

        // When
        val toggled = useCase(sheet, ToggleSpellSlotUseCase.Action.Toggle(level = 1, slotIndex = 0))
        val reset = useCase(toggled, ToggleSpellSlotUseCase.Action.Toggle(level = 1, slotIndex = 0))

        // Then
        assertThat(toggled.spellSlots.first { it.level == 1 }.expended).isEqualTo(1)
        assertThat(reset.spellSlots.first { it.level == 1 }.expended).isEqualTo(0)
    }

    @Test
    fun `invoke should clamp expended and total when setting total below zero`() {
        // Given
        val sheet = CharacterSheet(
            id = "hero",
            spellSlots = listOf(SpellSlotState(level = 1, total = 3, expended = 3)),
        )

        // When
        val updated = useCase(sheet, ToggleSpellSlotUseCase.Action.SetTotal(level = 1, total = -1))

        // Then
        val slot = updated.spellSlots.first { it.level == 1 }
        assertThat(slot.total).isEqualTo(0)
        assertThat(slot.expended).isEqualTo(0)
    }

    @Test
    fun `invoke should leave expended unchanged when total is zero`() {
        // Given
        val sheet = CharacterSheet(
            id = "hero",
            spellSlots = listOf(SpellSlotState(level = 2, total = 0, expended = 0)),
        )

        // When
        val updated = useCase(sheet, ToggleSpellSlotUseCase.Action.Toggle(level = 2, slotIndex = 0))

        // Then
        assertThat(updated.spellSlots.first { it.level == 2 }.expended).isEqualTo(0)
    }
}
