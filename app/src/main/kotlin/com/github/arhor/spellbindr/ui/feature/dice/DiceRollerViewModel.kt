package com.github.arhor.spellbindr.ui.feature.dice

import androidx.lifecycle.ViewModel
import com.github.arhor.spellbindr.ui.feature.dice.model.AmountResult
import com.github.arhor.spellbindr.ui.feature.dice.model.CheckMode
import com.github.arhor.spellbindr.ui.feature.dice.model.CheckResult
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceGroup
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceGroupResult
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceRollerIntent
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceRollerState
import com.github.arhor.spellbindr.ui.feature.dice.model.RollConfiguration
import com.github.arhor.spellbindr.ui.feature.dice.model.RollResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class DiceRollerViewModel @Inject constructor() : ViewModel() {

    private val random = Random.Default

    private val _state = MutableStateFlow(DiceRollerState())
    val state: StateFlow<DiceRollerState> = _state.asStateFlow()

    fun onIntent(intent: DiceRollerIntent) {
        when (intent) {
            DiceRollerIntent.ToggleCheck -> toggleCheck()
            is DiceRollerIntent.SetCheckMode -> setCheckMode(intent.mode)
            DiceRollerIntent.IncrementCheckModifier -> adjustCheckModifier(1)
            DiceRollerIntent.DecrementCheckModifier -> adjustCheckModifier(-1)
            is DiceRollerIntent.AddAmountDie -> addAmountDie(intent.sides)
            is DiceRollerIntent.IncrementAmountDie -> adjustAmountDie(intent.sides, 1)
            is DiceRollerIntent.DecrementAmountDie -> adjustAmountDie(intent.sides, -1)
            DiceRollerIntent.ClearAll -> clearAll()
            DiceRollerIntent.RollMain -> rollMain()
            DiceRollerIntent.RollPercentile -> rollPercentile()
            DiceRollerIntent.ReRollLast -> rerollLast()
        }
    }

    private fun toggleCheck() {
        _state.update { it.copy(hasCheck = !it.hasCheck) }
    }

    private fun setCheckMode(mode: CheckMode) {
        _state.update { it.copy(checkMode = mode) }
    }

    private fun adjustCheckModifier(delta: Int) {
        _state.update { it.copy(checkModifier = it.checkModifier + delta) }
    }

    private fun addAmountDie(sides: Int) {
        adjustAmountDie(sides, 1)
    }

    private fun adjustAmountDie(sides: Int, delta: Int) {
        if (delta == 0) return
        _state.update { current ->
            val updated = if (delta > 0) {
                current.amountDice.incrementDie(sides, delta)
            } else {
                current.amountDice.decrementDie(sides, -delta)
            }
            current.copy(amountDice = updated)
        }
    }

    private fun clearAll() {
        _state.update {
            it.copy(
                hasCheck = false,
                checkMode = CheckMode.NORMAL,
                checkModifier = 0,
                amountDice = emptyList(),
            )
        }
    }

    private fun rollMain() {
        val config = _state.value.toRollConfiguration()
        if (!config.canRoll()) return
        rollWithConfig(config)
    }

    private fun rollPercentile() {
        val value = randomDie(100)
        _state.update {
            it.copy(
                lastPercentileRoll = value,
                latestResult = RollResult.PercentileResult(value),
                latestResultToken = it.latestResultToken + 1,
            )
        }
    }

    private fun rerollLast() {
        when (_state.value.latestResult) {
            is RollResult.CheckAmountResult -> {
                val lastConfig = _state.value.lastRollConfig ?: return
                rollWithConfig(lastConfig)
            }

            is RollResult.PercentileResult -> rollPercentile()
            null -> Unit
        }
    }

    private fun rollWithConfig(config: RollConfiguration) {
        val checkResult = if (config.hasCheck) {
            rollCheck(config.checkMode, config.checkModifier)
        } else {
            null
        }

        val amountResult = config.amountDice.takeIf { it.isNotEmpty() }?.let(::rollAmount)
        val latest = RollResult.CheckAmountResult(
            check = checkResult,
            amount = amountResult,
        )
        _state.update {
            it.copy(
                latestResult = latest,
                lastRollConfig = config,
                latestResultToken = it.latestResultToken + 1,
            )
        }
    }

    private fun rollCheck(mode: CheckMode, modifier: Int): CheckResult {
        val rolls = when (mode) {
            CheckMode.NORMAL -> listOf(randomDie(20))
            CheckMode.ADVANTAGE, CheckMode.DISADVANTAGE -> listOf(randomDie(20), randomDie(20))
        }
        val kept = when (mode) {
            CheckMode.NORMAL -> rolls.first()
            CheckMode.ADVANTAGE -> rolls.maxOrNull() ?: rolls.first()
            CheckMode.DISADVANTAGE -> rolls.minOrNull() ?: rolls.first()
        }
        val total = kept + modifier
        return CheckResult(
            mode = mode,
            rolls = rolls,
            modifier = modifier,
            keptRoll = kept,
            total = total,
        )
    }

    private fun rollAmount(groups: List<DiceGroup>): AmountResult {
        val groupResults = groups.map { group ->
            val rolls = List(group.count) { randomDie(group.sides) }
            DiceGroupResult(
                sides = group.sides,
                rolls = rolls,
            )
        }
        val subtotal = groupResults.sumOf { it.rolls.sum() }
        return AmountResult(
            groups = groupResults,
            total = subtotal,
        )
    }

    private fun randomDie(sides: Int): Int = random.nextInt(1, sides + 1)

    private fun DiceRollerState.toRollConfiguration(): RollConfiguration {
        val diceCopy = amountDice.map { it.copy() }
        return RollConfiguration(
            hasCheck = hasCheck,
            checkMode = checkMode,
            checkModifier = checkModifier,
            amountDice = diceCopy,
        )
    }

    private fun RollConfiguration.canRoll(): Boolean {
        return hasCheck || amountDice.isNotEmpty()
    }

    private fun List<DiceGroup>.incrementDie(sides: Int, count: Int): List<DiceGroup> {
        var remaining = count
        var result = this
        while (remaining > 0) {
            result = result.incrementOnce(sides)
            remaining--
        }
        return result
    }

    private fun List<DiceGroup>.decrementDie(sides: Int, count: Int): List<DiceGroup> {
        var remaining = count
        var result = this
        while (remaining > 0) {
            result = result.decrementOnce(sides)
            remaining--
        }
        return result
    }

    private fun List<DiceGroup>.incrementOnce(sides: Int): List<DiceGroup> {
        val index = indexOfFirst { it.sides == sides }
        return if (index >= 0) {
            toMutableList().also { list ->
                val group = list[index]
                list[index] = group.copy(count = group.count + 1)
            }
        } else {
            (this + DiceGroup(sides = sides, count = 1)).sortedBy { it.sides }
        }
    }

    private fun List<DiceGroup>.decrementOnce(sides: Int): List<DiceGroup> {
        val index = indexOfFirst { it.sides == sides }
        if (index < 0) return this
        val list = toMutableList()
        val group = list[index]
        val newCount = group.count - 1
        if (newCount <= 0) {
            list.removeAt(index)
        } else {
            list[index] = group.copy(count = newCount)
        }
        return list
    }
}
