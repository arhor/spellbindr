package com.github.arhor.spellbindr.ui.feature.compendium.conditions

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Condition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Stable
@HiltViewModel
class ConditionsViewModel @Inject constructor() : ViewModel() {

    @Immutable
    data class ConditionsState(
        val expandedItem: Condition? = null,
    )

    private val conditionSelection = MutableStateFlow<Condition?>(null)

    val state = conditionSelection
        .map { ConditionsState(expandedItem = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConditionsState())

    fun onConditionClick(condition: Condition) {
        conditionSelection.update { current ->
            if (current == condition) {
                null
            } else {
                condition
            }
        }
    }
}
