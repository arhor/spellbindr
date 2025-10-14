package com.github.arhor.spellbindr.ui.feature.characters.search

import androidx.lifecycle.ViewModel
import com.github.arhor.spellbindr.data.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CharacterListViewModel @Inject constructor(
    characterRepository: CharacterRepository
) : ViewModel() {
    val characters = characterRepository.getCharacters()
} 
