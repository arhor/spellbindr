package com.github.arhor.spellbindr.ui.feature.dice

import com.github.arhor.spellbindr.ui.feature.dice.model.CheckMode
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceGroup
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceGroupResult
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceRollerUiState
import com.github.arhor.spellbindr.ui.feature.dice.model.RollResult
import com.google.common.truth.Truth.assertThat
import kotlin.random.Random
import org.junit.Test

class DiceRollerViewModelTest {

    @Test
    fun `toggle check and modifiers update state`() {
        val viewModel = DiceRollerViewModel(FakeRandom())

        assertThat(viewModel.content().hasCheck).isFalse()

        viewModel.toggleCheck()
        viewModel.setCheckMode(CheckMode.ADVANTAGE)
        viewModel.incrementCheckModifier()
        viewModel.incrementCheckModifier()
        viewModel.decrementCheckModifier()

        val state = viewModel.content()
        assertThat(state.hasCheck).isTrue()
        assertThat(state.checkMode).isEqualTo(CheckMode.ADVANTAGE)
        assertThat(state.checkModifier).isEqualTo(1)
    }

    @Test
    fun `amount dice adjustments maintain sorted groups`() {
        val viewModel = DiceRollerViewModel(FakeRandom())

        viewModel.addAmountDie(8)
        viewModel.incrementAmountDie(6)
        viewModel.incrementAmountDie(6)
        viewModel.decrementAmountDie(8)

        val state = viewModel.content()
        assertThat(state.amountDice).containsExactly(
            DiceGroup(sides = 6, count = 2),
        ).inOrder()
    }

    @Test
    fun `rolling main configuration records latest result`() {
        val initialState = DiceRollerUiState.Content(
            hasCheck = true,
            checkMode = CheckMode.ADVANTAGE,
            checkModifier = 2,
            amountDice = listOf(
                DiceGroup(sides = 6, count = 2),
                DiceGroup(sides = 8, count = 1),
            ),
        )
        val random = FakeRandom(17, 12, 3, 5, 7)
        val viewModel = DiceRollerViewModel(random, initialState)

        viewModel.rollMain()

        val state = viewModel.content()
        val result = state.latestResult as RollResult.CheckAmountResult

        val check = result.check
        requireNotNull(check)
        assertThat(check.keptRoll).isEqualTo(17)
        assertThat(check.rolls).containsExactly(17, 12).inOrder()
        assertThat(check.total).isEqualTo(19)

        val amount = result.amount
        requireNotNull(amount)
        assertThat(amount.total).isEqualTo(15)
        assertThat(amount.groups).containsExactly(
            DiceGroupResult(sides = 6, rolls = listOf(3, 5)),
            DiceGroupResult(sides = 8, rolls = listOf(7)),
        ).inOrder()

        assertThat(state.lastRollConfig?.amountDice).containsExactly(
            DiceGroup(sides = 6, count = 2),
            DiceGroup(sides = 8, count = 1),
        ).inOrder()
        assertThat(state.latestResultToken).isEqualTo(1)
    }

    @Test
    fun `rolling percentile updates last roll`() {
        val random = FakeRandom(42)
        val viewModel = DiceRollerViewModel(random)

        viewModel.rollPercentile()

        val state = viewModel.content()
        val result = state.latestResult as RollResult.PercentileResult
        assertThat(result.value).isEqualTo(42)
        assertThat(state.lastPercentileRoll).isEqualTo(42)
        assertThat(state.latestResultToken).isEqualTo(1)
    }

    private fun DiceRollerViewModel.content(): DiceRollerUiState.Content {
        return uiState.value as DiceRollerUiState.Content
    }

    private class FakeRandom(
        vararg values: Int,
    ) : Random() {

        private val queue = ArrayDeque(values.toList())

        override fun nextInt(from: Int, until: Int): Int {
            val next = queue.removeFirstOrNull() ?: error("No more values in FakeRandom queue")
            require(next >= from && next < until) { "Value $next is not in range [$from, $until)" }
            return next
        }

        override fun nextBits(bitCount: Int): Int {
            error("nextBits should not be called in FakeRandom")
        }
    }
}
