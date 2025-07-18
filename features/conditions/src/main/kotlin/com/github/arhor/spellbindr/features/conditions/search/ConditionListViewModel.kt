package com.github.arhor.spellbindr.features.conditions.search

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.conditions.Condition
import com.github.arhor.spellbindr.data.conditions.ConditionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@Stable
@HiltViewModel
class ConditionListViewModel @Inject constructor(
    private val conditionRepository: ConditionRepository,
) : ViewModel() {

    @Immutable
    data class State(
        val query: String = "",
        val conditions: List<Condition> = emptyList(),
        val isLoading: Boolean = false,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        observeStateChanges()
    }

    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
    }

    @OptIn(FlowPreview::class)
    private fun observeStateChanges() {
        _state
            .debounce(350.milliseconds)
            .distinctUntilChanged()
            .onEach { state ->
                _state.update { it.copy(isLoading = true) }
                val conditions = conditionRepository.findConditions(state.query)
                _state.update { it.copy(conditions = conditions, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }
} 
