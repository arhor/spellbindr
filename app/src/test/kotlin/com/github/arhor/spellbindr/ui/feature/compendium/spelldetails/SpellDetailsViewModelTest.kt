package com.github.arhor.spellbindr.ui.feature.compendium.spelldetails

import androidx.lifecycle.SavedStateHandle
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.SpellDetails
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellDetailsUseCase
import com.github.arhor.spellbindr.domain.usecase.ToggleFavoriteSpellUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SpellDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `dispatch should toggle favorite when current state is content`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val spell = buildSpell("magic_missile")
            val observeSpellDetails = mockk<ObserveSpellDetailsUseCase>()
            val toggleFavoriteSpell = mockk<ToggleFavoriteSpellUseCase>()
            every { observeSpellDetails("magic_missile") } returns flowOf(
                Loadable.Content(SpellDetails(spell = spell, isFavorite = false)),
            )
            coEvery { toggleFavoriteSpell("magic_missile") } returns Unit

            val vm = SpellDetailsViewModel(
                savedStateHandle = SavedStateHandle(mapOf("spellId" to "magic_missile")),
                observeSpellDetails = observeSpellDetails,
                toggleFavoriteSpell = toggleFavoriteSpell,
            )
            vm.uiState.first { it is SpellDetailsUiState.Content }

            // When
            vm.dispatch(SpellDetailsIntent.ToggleFavorite)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { toggleFavoriteSpell("magic_missile") }
        }

    @Test
    fun `dispatch should emit show message effect when toggle favorite fails`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val spell = buildSpell("shield")
            val observeSpellDetails = mockk<ObserveSpellDetailsUseCase>()
            val toggleFavoriteSpell = mockk<ToggleFavoriteSpellUseCase>()
            every { observeSpellDetails("shield") } returns flowOf(
                Loadable.Content(SpellDetails(spell = spell, isFavorite = false)),
            )
            coEvery { toggleFavoriteSpell("shield") } throws IllegalStateException("Write failed")

            val vm = SpellDetailsViewModel(
                savedStateHandle = SavedStateHandle(mapOf("spellId" to "shield")),
                observeSpellDetails = observeSpellDetails,
                toggleFavoriteSpell = toggleFavoriteSpell,
            )
            vm.uiState.first { it is SpellDetailsUiState.Content }

            // When
            val effect = async { vm.effects.first() }
            vm.dispatch(SpellDetailsIntent.ToggleFavorite)
            advanceUntilIdle()

            // Then
            assertThat(effect.await()).isEqualTo(SpellDetailsEffect.ShowMessage("Write failed"))
        }

    private fun buildSpell(id: String): Spell = Spell(
        id = id,
        name = id.replace('_', ' '),
        desc = listOf("desc"),
        level = 1,
        range = "60 ft",
        ritual = false,
        school = EntityRef("evocation"),
        duration = "Instant",
        castingTime = "1 action",
        classes = listOf(EntityRef("wizard")),
        components = listOf("V"),
        concentration = false,
        source = "PHB",
    )
}
