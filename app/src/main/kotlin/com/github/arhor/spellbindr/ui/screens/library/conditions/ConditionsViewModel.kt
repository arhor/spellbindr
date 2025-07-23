package com.github.arhor.spellbindr.ui.screens.library.conditions

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Condition
import com.github.arhor.spellbindr.data.repository.ConditionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@Stable
@HiltViewModel
class ConditionsViewModel @Inject constructor(
    conditionRepository: ConditionRepository,
) : ViewModel() {

    @Immutable
    data class State(
        val conditions: List<Condition> = emptyList(),
        val expandedItemName: String? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            conditionRepository.allConditions.collect { data ->
                _state.update { it.copy(conditions = data) }
            }
        }
    }

    fun handleConditionClick(conditionName: String) {
        _state.update {
            it.copy(
                expandedItemName = if (it.expandedItemName == conditionName) {
                    null
                } else {
                    conditionName
                }
            )
        }
    }
} 
