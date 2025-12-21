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
    fun `toggle spell slot updates expended count`() {
        val sheet = CharacterSheet(
            id = "hero",
            spellSlots = listOf(SpellSlotState(level = 1, total = 2, expended = 0)),
        )

        val toggled = useCase(sheet, ToggleSpellSlotUseCase.Action.Toggle(level = 1, slotIndex = 0))
        val reset = useCase(toggled, ToggleSpellSlotUseCase.Action.Toggle(level = 1, slotIndex = 0))

        assertThat(toggled.spellSlots.first { it.level == 1 }.expended).isEqualTo(1)
        assertThat(reset.spellSlots.first { it.level == 1 }.expended).isEqualTo(0)
    }

    @Test
    fun `set total clamps expended and total`() {
        val sheet = CharacterSheet(
            id = "hero",
            spellSlots = listOf(SpellSlotState(level = 1, total = 3, expended = 3)),
        )

        val updated = useCase(sheet, ToggleSpellSlotUseCase.Action.SetTotal(level = 1, total = -1))

        val slot = updated.spellSlots.first { it.level == 1 }
        assertThat(slot.total).isEqualTo(0)
        assertThat(slot.expended).isEqualTo(0)
    }

    @Test
    fun `toggle does nothing when total is zero`() {
        val sheet = CharacterSheet(
            id = "hero",
            spellSlots = listOf(SpellSlotState(level = 2, total = 0, expended = 0)),
        )

        val updated = useCase(sheet, ToggleSpellSlotUseCase.Action.Toggle(level = 2, slotIndex = 0))

        assertThat(updated.spellSlots.first { it.level == 2 }.expended).isEqualTo(0)
    }
}
