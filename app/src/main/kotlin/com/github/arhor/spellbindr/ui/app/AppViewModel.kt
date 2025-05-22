package com.github.arhor.spellbindr.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.repository.SpellRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val spellRepository: SpellRepository,
) : ViewModel() {

    data class State(
        val isLoading: Boolean
    )

    private val _state = MutableStateFlow(State(isLoading = true))
    val state: StateFlow<State> = _state.asStateFlow()

    fun loadApplicationState() {
        viewModelScope.launch {
            spellRepository.loadDataIfNeeded()
            _state.update { it.copy(isLoading = false) }
        }
    }
}
