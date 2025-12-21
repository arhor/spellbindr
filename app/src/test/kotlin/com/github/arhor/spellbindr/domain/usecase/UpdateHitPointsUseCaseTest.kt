package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class UpdateHitPointsUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase = UpdateHitPointsUseCase()

    @Test
    fun `adjustCurrentHp clamps to valid range`() {
        val sheet = CharacterSheet(id = "hero", maxHitPoints = 10, currentHitPoints = 9)

        val increased = useCase(sheet, UpdateHitPointsUseCase.Action.AdjustCurrentHp(5))
        val decreased = useCase(sheet, UpdateHitPointsUseCase.Action.AdjustCurrentHp(-20))

        assertThat(increased.currentHitPoints).isEqualTo(10)
        assertThat(decreased.currentHitPoints).isEqualTo(0)
    }

    @Test
    fun `setTemporaryHp prevents negatives`() {
        val sheet = CharacterSheet(id = "hero", temporaryHitPoints = 4)

        val updated = useCase(sheet, UpdateHitPointsUseCase.Action.SetTemporaryHp(-3))

        assertThat(updated.temporaryHitPoints).isEqualTo(0)
    }

    @Test
    fun `setDeathSaves clamps values between zero and three`() {
        val sheet = CharacterSheet(id = "hero")

        val successes = useCase(sheet, UpdateHitPointsUseCase.Action.SetDeathSaveSuccesses(4))
        val failures = useCase(sheet, UpdateHitPointsUseCase.Action.SetDeathSaveFailures(-1))

        assertThat(successes.deathSaves.successes).isEqualTo(3)
        assertThat(failures.deathSaves.failures).isEqualTo(0)
    }
}
