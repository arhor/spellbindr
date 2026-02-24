package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.PactSlotState
import com.github.arhor.spellbindr.domain.model.SpellSlotState
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ToggleSpellSlotUseCaseTest {

    private val useCase = ToggleSpellSlotUseCase()

    @Test
    fun longRest_resetsSharedAndPactSlots_andClearsConcentration() {
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

        val updated = useCase(sheet, ToggleSpellSlotUseCase.Action.LongRest)

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
    fun shortRest_resetsOnlyPactSlots() {
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

        val updated = useCase(sheet, ToggleSpellSlotUseCase.Action.ShortRest)

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
    fun setPactSlotLevel_createsPactSlotsWhenMissing() {
        val sheet = CharacterSheet(
            id = "hero",
            pactSlots = null,
        )

        val updated = useCase(sheet, ToggleSpellSlotUseCase.Action.SetPactSlotLevel(level = 5))

        assertThat(updated.pactSlots).isEqualTo(
            PactSlotState(
                slotLevel = 5,
                total = 0,
                expended = 0,
            )
        )
    }

    @Test
    fun setPactSlotLevel_updatesExistingPactSlots() {
        val sheet = CharacterSheet(
            id = "hero",
            pactSlots = PactSlotState(
                slotLevel = 2,
                total = 3,
                expended = 1,
            ),
        )

        val updated = useCase(sheet, ToggleSpellSlotUseCase.Action.SetPactSlotLevel(level = 4))

        assertThat(updated.pactSlots).isEqualTo(
            PactSlotState(
                slotLevel = 4,
                total = 3,
                expended = 1,
            )
        )
    }
}

