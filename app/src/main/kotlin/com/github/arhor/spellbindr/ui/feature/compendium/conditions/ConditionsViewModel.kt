package com.github.arhor.spellbindr.ui.feature.compendium.conditions

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Condition
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.usecase.ObserveConditionsUseCase
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
    private val observeConditions: ObserveConditionsUseCase,
) : ViewModel() {

    private val selectedItemIdState = MutableStateFlow<String?>(null)
    private val conditionsState = observeConditions()
        .stateIn(viewModelScope, sharingStrategy, Loadable.Loading)

    val uiState: StateFlow<ConditionsUiState> = combine(conditionsState, selectedItemIdState, ::toUiState)
        .stateIn(viewModelScope, sharingStrategy, ConditionsUiState.Loading)

    fun onConditionClick(condition: Condition) {
        selectedItemIdState.update {
            if (it != condition.id) {
                condition.id
            } else {
                null
            }
        }
    }

    private fun toUiState(
        conditions: Loadable<List<Condition>>,
        selectedId: String?
    ): ConditionsUiState = when (conditions) {
        is Loadable.Loading -> {
            ConditionsUiState.Loading
        }

        is Loadable.Ready -> {
            ConditionsUiState.Content(conditions.data, selectedId)
        }

        is Loadable.Error -> {
            ConditionsUiState.Error(conditions.cause?.message ?: "Failed to load conditions")
        }
    }

    companion object {
        private val sharingStrategy = SharingStarted.WhileSubscribed(5_000)
    }
}
