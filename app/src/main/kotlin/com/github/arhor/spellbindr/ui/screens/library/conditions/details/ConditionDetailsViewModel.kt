package com.github.arhor.spellbindr.ui.screens.library.conditions.details

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.conditions.Condition
import com.github.arhor.spellbindr.data.conditions.ConditionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class ConditionDetailsViewModel @Inject constructor(
    private val conditionRepository: ConditionRepository,
) : ViewModel() {

    @Immutable
    data class State(
        val condition: Condition? = null,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun loadConditionByName(name: String) {
        viewModelScope.launch {
            val condition = conditionRepository.findConditionByName(name)
            _state.update { it.copy(condition = condition) }
        }
    }
} 
