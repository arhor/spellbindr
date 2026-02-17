package com.github.arhor.spellbindr.ui.feature.compendium.conditions

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.usecase.ObserveAllConditionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Stable
@HiltViewModel
class ConditionsViewModel @Inject constructor(
    private val observeConditions: ObserveAllConditionsUseCase,
) : ViewModel() {

    private data class State(
        val selectedItemId: String? = null,
    )

    private val _state = MutableStateFlow(State())

    val uiState: StateFlow<ConditionsUiState> = combine(
        _state,
        observeConditions(),
    ) { state, conditions ->
        when (conditions) {
            is Loadable.Loading -> {
                ConditionsUiState.Loading
            }

            is Loadable.Content -> {
                ConditionsUiState.Content(conditions.data, state.selectedItemId)
            }

            is Loadable.Failure -> {
                ConditionsUiState.Failure(conditions.errorMessage ?: "Failed to load conditions")
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConditionsUiState.Loading)

    fun dispatch(intent: ConditionsIntent) {
        when (intent) {
            is ConditionsIntent.ConditionClicked -> toggleSelection(intent.conditionId)
        }
    }

    private fun toggleSelection(conditionId: String) {
        _state.update { state ->
            state.copy(
                selectedItemId = conditionId.takeIf { it != state.selectedItemId }
            )
        }
    }
}
