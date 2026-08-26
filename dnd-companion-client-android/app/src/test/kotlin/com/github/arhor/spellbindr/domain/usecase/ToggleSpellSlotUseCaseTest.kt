package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.PactSlotState
import com.github.arhor.spellbindr.domain.model.SpellSlotState
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ToggleSpellSlotUseCaseTest {

    private val useCase = ToggleSpellSlotUseCase()

    @Test
    fun `invoke should reset all slots and concentration when action is long rest`() {
        // Given
        val sheet = CharacterSheet(
            id = "hero",
            concentrationSpellId = "hex",
            spellSlots = listOf(
                SpellSlotState(level = 1, total = 4, expended = 2),
                SpellSlotState(level = 2, total = 2, expended = 1),
            ),
            pactSlots = PactSlotState(
                slotLevel = 2,
                total = 2,
                expended = 2,
            ),
        )

        // When
        val updated = useCase(sheet, ToggleSpellSlotUseCase.Action.LongRest)

        // Then
        assertThat(updated.concentrationSpellId).isNull()
        assertThat(updated.spellSlots).containsExactly(
            SpellSlotState(level = 1, total = 4, expended = 0),
            SpellSlotState(level = 2, total = 2, expended = 0),
        ).inOrder()
        assertThat(updated.pactSlots).isEqualTo(
            PactSlotState(
                slotLevel = 2,
                total = 2,
                expended = 0,
            )
        )
    }

    @Test
    fun `invoke should reset only pact slots when action is short rest`() {
        // Given
        val sheet = CharacterSheet(
            id = "hero",
            concentrationSpellId = "hex",
            spellSlots = listOf(
                SpellSlotState(level = 1, total = 4, expended = 2),
            ),
            pactSlots = PactSlotState(
                slotLevel = 2,
                total = 2,
                expended = 1,
            ),
        )

        // When
        val updated = useCase(sheet, ToggleSpellSlotUseCase.Action.ShortRest)

        // Then
        assertThat(updated.concentrationSpellId).isEqualTo("hex")
        assertThat(updated.spellSlots).containsExactly(
            SpellSlotState(level = 1, total = 4, expended = 2),
        )
        assertThat(updated.pactSlots).isEqualTo(
            PactSlotState(
                slotLevel = 2,
                total = 2,
                expended = 0,
            )
        )
    }

    @Test
    fun `invoke should create pact slots when setting level without existing state`() {
        // Given
        val sheet = CharacterSheet(
            id = "hero",
            pactSlots = null,
        )

        // When
        val updated = useCase(sheet, ToggleSpellSlotUseCase.Action.SetPactSlotLevel(level = 5))

        // Then
        assertThat(updated.pactSlots).isEqualTo(
            PactSlotState(
                slotLevel = 5,
                total = 0,
                expended = 0,
            )
        )
    }

    @Test
    fun `invoke should retain pact slot usage when setting level with existing state`() {
        // Given
        val sheet = CharacterSheet(
            id = "hero",
            pactSlots = PactSlotState(
                slotLevel = 2,
                total = 3,
                expended = 1,
            ),
        )

        // When
        val updated = useCase(sheet, ToggleSpellSlotUseCase.Action.SetPactSlotLevel(level = 4))

        // Then
        assertThat(updated.pactSlots).isEqualTo(
            PactSlotState(
                slotLevel = 4,
                total = 3,
                expended = 1,
            )
        )
    }
}
