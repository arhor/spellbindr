package com.github.arhor.spellbindr.ui.screens.library.conditions

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import com.github.arhor.spellbindr.data.model.predefined.Condition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Stable
@HiltViewModel
class ConditionsViewModel @Inject constructor() : ViewModel() {

    @Immutable
    data class State(
        val expandedItem: Condition? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun handleConditionClick(condition: Condition) {
        _state.update {
            it.copy(
                expandedItem = if (it.expandedItem == condition) {
                    null
                } else {
                    condition
                }
            )
        }
    }
} 
