package com.github.arhor.spellbindr.ui.feature.dice

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import com.github.arhor.spellbindr.ui.feature.dice.model.AmountResult
import com.github.arhor.spellbindr.ui.feature.dice.model.CheckMode
import com.github.arhor.spellbindr.ui.feature.dice.model.CheckResult
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceGroup
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceGroupResult
import com.github.arhor.spellbindr.ui.feature.dice.model.RollConfiguration
import com.github.arhor.spellbindr.ui.feature.dice.model.RollResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.random.Random

@Stable
@HiltViewModel
class DiceRollerViewModel @Inject constructor() : ViewModel() {

    private val random: Random = Random.Default
    private val _uiState: MutableStateFlow<DiceRollerUiState> = MutableStateFlow(DiceRollerUiState.Content())

    val uiState: StateFlow<DiceRollerUiState> = _uiState.asStateFlow()

    fun dispatch(intent: DiceRollerIntent) {
        when (intent) {
            DiceRollerIntent.ToggleCheck -> toggleCheck()
            is DiceRollerIntent.CheckModeSelected -> setCheckMode(intent.mode)
            DiceRollerIntent.IncrementCheckModifier -> incrementCheckModifier()
            DiceRollerIntent.DecrementCheckModifier -> decrementCheckModifier()
            is DiceRollerIntent.AddAmountDie -> addAmountDie(intent.sides)
            is DiceRollerIntent.IncrementAmountDie -> incrementAmountDie(intent.sides)
            is DiceRollerIntent.DecrementAmountDie -> decrementAmountDie(intent.sides)
            DiceRollerIntent.ClearAll -> clearAll()
            DiceRollerIntent.RollMain -> rollMain()
            DiceRollerIntent.RollPercentile -> rollPercentile()
            DiceRollerIntent.RerollLast -> rerollLast()
        }
    }

    private fun toggleCheck() = updateContent { it.copy(hasCheck = !it.hasCheck) }

    private fun setCheckMode(mode: CheckMode) = updateContent { it.copy(checkMode = mode) }

    private fun incrementCheckModifier() = adjustCheckModifier(1)

    private fun decrementCheckModifier() = adjustCheckModifier(-1)

    private fun addAmountDie(sides: Int) {
        adjustAmountDie(sides, 1)
    }

    private fun incrementAmountDie(sides: Int) {
        adjustAmountDie(sides, 1)
    }

    private fun decrementAmountDie(sides: Int) {
        adjustAmountDie(sides, -1)
    }

    private fun clearAll() = updateContent {
        it.copy(
            hasCheck = false,
            checkMode = CheckMode.NORMAL,
            checkModifier = 0,
            amountDice = emptyList(),
        )
    }

    private fun rollMain() {
        val content = _uiState.value.asContentOrNull() ?: return
        val config = content.toRollConfiguration()
        if (!config.canRoll()) return
        rollWithConfig(config)
    }

    private fun rollPercentile() {
        val value = randomDie(100)
        updateContent {
            it.copy(
                lastPercentileRoll = value,
                latestResult = RollResult.PercentileResult(value),
                latestResultToken = it.latestResultToken + 1,
            )
        }
    }

    private fun rerollLast() {
        val state = _uiState.value.asContentOrNull() ?: return
        when (state.latestResult) {
            is RollResult.CheckAmountResult -> {
                val lastConfig = state.lastRollConfig ?: return
                rollWithConfig(lastConfig)
            }

            is RollResult.PercentileResult -> rollPercentile()
            null -> Unit
        }
    }

    private fun adjustCheckModifier(delta: Int) = updateContent {
        it.copy(checkModifier = it.checkModifier + delta)
    }

    private fun adjustAmountDie(sides: Int, delta: Int) {
        if (delta == 0) {
            return
        }
        updateContent {
            it.copy(
                amountDice = if (delta > 0) {
                    it.amountDice.incrementDie(sides, delta)
                } else {
                    it.amountDice.decrementDie(sides, -delta)
                }
            )
        }
    }

    private fun rollWithConfig(config: RollConfiguration) {
        val checkResult = if (config.hasCheck) rollCheck(config.checkMode, config.checkModifier) else null
        val amountResult = config.amountDice.takeIf { it.isNotEmpty() }?.let(::rollAmount)

        updateContent {
            it.copy(
                latestResult = RollResult.CheckAmountResult(
                    check = checkResult,
                    amount = amountResult,
                ),
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

    private fun DiceRollerUiState.Content.toRollConfiguration(): RollConfiguration {
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

    private inline fun updateContent(
        crossinline transform: (DiceRollerUiState.Content) -> DiceRollerUiState.Content,
    ) {
        _uiState.update {
            when (it) {
                is DiceRollerUiState.Content -> transform(it)
                else -> it
            }
        }
    }

    private fun DiceRollerUiState.asContentOrNull(): DiceRollerUiState.Content? {
        return this as? DiceRollerUiState.Content
    }
}
