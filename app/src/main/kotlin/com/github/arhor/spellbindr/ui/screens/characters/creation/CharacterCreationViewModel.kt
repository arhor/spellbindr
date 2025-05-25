package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.lifecycle.ViewModel
import com.github.arhor.spellbindr.data.repository.RaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CharacterCreationViewModel @Inject constructor(
    private val raceRepository: RaceRepository
) : ViewModel() {

    data class State(
        val name: String = "",
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()
}
