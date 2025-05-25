package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.lifecycle.ViewModel
import com.github.arhor.spellbindr.data.repository.RaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CharacterCreationViewModel @Inject constructor(
    private val raceRepository: RaceRepository
) : ViewModel() {

    data class State(
        val name: String = "",
        val background: String = "",
        val alignment: String? = null,
        val backstory: String? = null,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun onNameChanged(newName: String) {
        _state.update { it.copy(name = newName) }
    }

    fun onBackgroundChanged(newBackground: String) {
        _state.update { it.copy(background = newBackground) }
    }

    fun onAlignmentChanged(newAlignment: String?) {
        _state.update { it.copy(alignment = newAlignment) }
    }

    fun onBackstoryChanged(newBackstory: String?) {
        _state.update { it.copy(backstory = newBackstory) }
    }
}
