package com.github.arhor.spellbindr.ui.feature.compendium.conditions

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Condition
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

    private val selectedItemIdState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ConditionsUiState> = combine(
        observeConditions(),
        selectedItemIdState
    ) { conditions, selectedItemId ->
        when (conditions) {
            is Loadable.Loading -> {
                ConditionsUiState.Loading
            }

            is Loadable.Ready -> {
                ConditionsUiState.Content(conditions.data, selectedItemId)
            }

            is Loadable.Error -> {
                ConditionsUiState.Error(conditions.errorMessage ?: "Failed to load conditions")
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConditionsUiState.Loading)

    fun onConditionClick(condition: Condition) {
        selectedItemIdState.update {
            if (it != condition.id) {
                condition.id
            } else {
                null
            }
        }
    }
}
