package com.github.arhor.spellbindr.features.conditions.search

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.conditions.Condition
import com.github.arhor.spellbindr.data.conditions.ConditionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Stable
@HiltViewModel
class ConditionListViewModel @Inject constructor(
    conditionRepository: ConditionRepository,
) : ViewModel() {

    @Immutable
    data class State(
        val conditions: List<Condition> = emptyList(),
    )

    val state: StateFlow<State> = conditionRepository.allConditions
        .map(::State)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = State(),
        )
} 
