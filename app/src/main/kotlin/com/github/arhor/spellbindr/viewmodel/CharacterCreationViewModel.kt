package com.github.arhor.spellbindr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Race
import com.github.arhor.spellbindr.data.repository.RaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterCreationViewModel @Inject constructor(
    private val raceRepository: RaceRepository
) : ViewModel() {

    private val _races = MutableStateFlow<List<Race>>(emptyList())
    val races: StateFlow<List<Race>> = _races

    private val _selectedRace = MutableStateFlow<Race?>(null)
    val selectedRace: StateFlow<Race?> = _selectedRace

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep

    private val _characterName = MutableStateFlow("")
    val characterName: StateFlow<String> = _characterName

    init {
        viewModelScope.launch {
            _races.value = raceRepository.getAllRaces()
        }
    }

    fun selectRace(race: Race) {
        _selectedRace.value = race
    }

    fun goToStep(step: Int) {
        _currentStep.value = step
    }

    fun setCharacterName(name: String) {
        _characterName.value = name
    }
} 