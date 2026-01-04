package com.github.arhor.spellbindr.ui.feature.dice

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import com.github.arhor.spellbindr.ui.feature.dice.model.AmountResult
import com.github.arhor.spellbindr.ui.feature.dice.model.CheckMode
import com.github.arhor.spellbindr.ui.feature.dice.model.CheckResult
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceGroup
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceGroupResult
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceRollerUiState
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

    private var random: Random = Random.Default

    private val _uiState = MutableStateFlow<DiceRollerUiState>(DiceRollerUiState.Content())
    val uiState: StateFlow<DiceRollerUiState> = _uiState.asStateFlow()

    internal constructor(
        random: Random,
        initialState: DiceRollerUiState = DiceRollerUiState.Content(),
    ) : this() {
        this.random = random
        _uiState.value = initialState
    }

    fun toggleCheck() = updateContent { it.copy(hasCheck = !it.hasCheck) }

    fun setCheckMode(mode: CheckMode) = updateContent { it.copy(checkMode = mode) }

    fun incrementCheckModifier() = adjustCheckModifier(1)

    fun decrementCheckModifier() = adjustCheckModifier(-1)

    fun addAmountDie(sides: Int) {
        adjustAmountDie(sides, 1)
    }

    fun incrementAmountDie(sides: Int) {
        adjustAmountDie(sides, 1)
    }

    fun decrementAmountDie(sides: Int) {
        adjustAmountDie(sides, -1)
    }

    fun clearAll() = updateContent {
        it.copy(
            hasCheck = false,
            checkMode = CheckMode.NORMAL,
            checkModifier = 0,
            amountDice = emptyList(),
        )
    }

    fun rollMain() {
        val content = _uiState.value.asContentOrNull() ?: return
        val config = content.toRollConfiguration()
        if (!config.canRoll()) return
        rollWithConfig(config)
    }

    fun rollPercentile() {
        val value = randomDie(100)
        updateContent {
            it.copy(
                lastPercentileRoll = value,
                latestResult = RollResult.PercentileResult(value),
                latestResultToken = it.latestResultToken + 1,
            )
        }
    }

    fun rerollLast() {
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
        if (delta == 0) return
        _uiState.update { current ->
            val content = current.asContentOrNull() ?: return@update current
            val updated = if (delta > 0) {
                content.amountDice.incrementDie(sides, delta)
            } else {
                content.amountDice.decrementDie(sides, -delta)
            }
            content.copy(amountDice = updated)
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
        updateContent {
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
        _uiState.update { current ->
            val content = current.asContentOrNull() ?: return@update current
            transform(content)
        }
    }

    private fun DiceRollerUiState.asContentOrNull(): DiceRollerUiState.Content? {
        return this as? DiceRollerUiState.Content
    }
}
