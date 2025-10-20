package com.github.arhor.spellbindr.ui.feature.diceRoller

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.repository.DiceRollRepository
import com.github.arhor.spellbindr.ui.feature.diceRoller.model.DiceRoll
import com.github.arhor.spellbindr.ui.feature.diceRoller.model.DiceType
import com.github.arhor.spellbindr.ui.feature.diceRoller.model.RollHistoryEntry
import com.github.arhor.spellbindr.ui.feature.diceRoller.model.RollSet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class DiceRollerViewModel @Inject constructor(
    private val diceRollRepository: DiceRollRepository
) : ViewModel() {

    @Immutable
    data class State(
        val selectedDiceType: DiceType = DiceType.D20,
        val selectedQuantity: Int = 1,
        val currentRoll: RollSet? = null,
        val rollHistory: List<RollHistoryEntry> = emptyList(),
        val isRolling: Boolean = false,
        val showHistory: Boolean = false
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                diceRollRepository.getRollHistory(),
                _state
            ) { history, currentState ->
                currentState.copy(rollHistory = history)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun selectDiceType(diceType: DiceType) {
        _state.value = _state.value.copy(selectedDiceType = diceType)
    }

    fun updateQuantity(quantity: Int) {
        if (quantity > 0 && quantity <= 100) { // Reasonable limits
            _state.value = _state.value.copy(selectedQuantity = quantity)
        }
    }

    fun rollDice() {
        val currentState = _state.value
        if (currentState.isRolling) return

        viewModelScope.launch {
            _state.value = currentState.copy(isRolling = true)

            // Simulate rolling animation
            delay(1000)

            val rolls = generateRolls(
                diceType = currentState.selectedDiceType,
                quantity = currentState.selectedQuantity
            )

            val total = rolls.sumOf { it.result }
            val rollSet = RollSet(
                diceType = currentState.selectedDiceType,
                quantity = currentState.selectedQuantity,
                rolls = rolls,
                total = total
            )

            _state.value = currentState.copy(
                currentRoll = rollSet,
                isRolling = false
            )

            // Save to history
            diceRollRepository.saveRoll(rollSet)
        }
    }

    fun toggleHistory() {
        _state.value = _state.value.copy(showHistory = !_state.value.showHistory)
    }

    fun clearHistory() {
        viewModelScope.launch {
            diceRollRepository.clearHistory()
        }
    }

    fun deleteRoll(rollId: String) {
        viewModelScope.launch {
            diceRollRepository.deleteRoll(rollId)
        }
    }

    private fun generateRolls(
        diceType: DiceType,
        quantity: Int
    ): List<DiceRoll> {
        return (1..quantity).map { _ ->
            val result = (1..diceType.sides).random()
            DiceRoll(
                diceType = diceType,
                result = result,
            )
        }
    }
}
