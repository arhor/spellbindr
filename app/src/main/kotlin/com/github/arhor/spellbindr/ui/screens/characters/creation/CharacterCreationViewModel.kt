package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import com.github.arhor.spellbindr.data.model.EntityRef
import com.github.arhor.spellbindr.data.model.Race
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class CharacterCreationViewModel @Inject constructor(
) : ViewModel() {

    @Immutable
    data class State(
        val name: String = "",
        val background: String = "",
        val alignment: String? = null,
        val backstory: String? = null,
        val race: Race? = null,
        val subrace: EntityRef? = null,
        val isLoadingRaces: Boolean = false,
        val races: List<Race> = emptyList(),
        val raceLoadError: String? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadRaces()
    }

    private fun loadRaces() {
        _state.update { it.copy(isLoadingRaces = true, raceLoadError = null) }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val races = emptyList<Race>()
                _state.update { it.copy(races = races, isLoadingRaces = false) }
            } catch (e: Exception) {
                _state.update { it.copy(raceLoadError = e.message, isLoadingRaces = false) }
            }
        }
    }

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

    fun onRaceSelected(race: Race) {
        _state.update { it.copy(race = race, subrace = null) }
    }

    fun onSubraceSelected(subrace: EntityRef) {
        _state.update { it.copy(subrace = subrace) }
    }
}
