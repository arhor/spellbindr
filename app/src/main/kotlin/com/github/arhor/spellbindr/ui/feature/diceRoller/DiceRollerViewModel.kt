package com.github.arhor.spellbindr.ui.feature.diceRoller

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@Stable
@HiltViewModel
class DiceRollerViewModel @Inject constructor() : ViewModel() {

    @Immutable
    class State()

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()
}
