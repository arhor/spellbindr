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
    fun `invoke should clamp current hit points to valid range when adjusting`() {
        // Given
        val sheet = CharacterSheet(id = "hero", maxHitPoints = 10, currentHitPoints = 9)

        // When
        val increased = useCase(sheet, UpdateHitPointsUseCase.Action.AdjustCurrentHp(5))
        val decreased = useCase(sheet, UpdateHitPointsUseCase.Action.AdjustCurrentHp(-20))

        // Then
        assertThat(increased.currentHitPoints).isEqualTo(10)
        assertThat(decreased.currentHitPoints).isEqualTo(0)
    }

    @Test
    fun `invoke should prevent negative temporary hit points when setting value`() {
        // Given
        val sheet = CharacterSheet(id = "hero", temporaryHitPoints = 4)

        // When
        val updated = useCase(sheet, UpdateHitPointsUseCase.Action.SetTemporaryHp(-3))

        // Then
        assertThat(updated.temporaryHitPoints).isEqualTo(0)
    }

    @Test
    fun `invoke should clamp death save counts between zero and three when setting values`() {
        // Given
        val sheet = CharacterSheet(id = "hero")

        // When
        val successes = useCase(sheet, UpdateHitPointsUseCase.Action.SetDeathSaveSuccesses(4))
        val failures = useCase(sheet, UpdateHitPointsUseCase.Action.SetDeathSaveFailures(-1))

        // Then
        assertThat(successes.deathSaves.successes).isEqualTo(3)
        assertThat(failures.deathSaves.failures).isEqualTo(0)
    }
}
