package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.classes.CharacterClass
import com.github.arhor.spellbindr.data.classes.CharacterClassRepository
import com.github.arhor.spellbindr.data.common.Background
import com.github.arhor.spellbindr.data.common.Character
import com.github.arhor.spellbindr.data.common.EntityRef
import com.github.arhor.spellbindr.data.common.Equipment
import com.github.arhor.spellbindr.data.common.Race
import com.github.arhor.spellbindr.data.common.Skill
import com.github.arhor.spellbindr.data.common.Subrace
import com.github.arhor.spellbindr.data.repository.BackgroundRepository
import com.github.arhor.spellbindr.data.repository.CharacterRepository
import com.github.arhor.spellbindr.data.repository.RacesRepository
import com.github.arhor.spellbindr.data.spells.Spell
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@Stable
@HiltViewModel
class CharacterCreationViewModel @Inject constructor(
    private val backgroundRepository: BackgroundRepository,
    private val characterClassRepository: CharacterClassRepository,
    private val racesRepository: RacesRepository,
    private val characterRepository: CharacterRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Immutable
    data class State(
        // Basic Info
        val characterName: String = "",
        val background: Background? = null,
        val race: Race? = null,
        val subrace: Subrace? = null,
        val characterClass: CharacterClass? = null,

        // Abilities
        val abilityScores: Map<String, Int> = emptyMap(),

        // Skills
        val skillProficiencies: List<String> = emptyList(),

        // Equipment
        val equipment: Set<EntityRef> = emptySet(),

        // Spells
        val knownSpells: Set<EntityRef> = emptySet(),

        // Data-loading states
        val isLoading: Boolean = true,
        val error: String? = null,

        // Selections
        val races: List<Race> = emptyList(),
        val classes: List<CharacterClass> = emptyList(),
        val backgrounds: List<Background> = emptyList(),
        val selectedSkills: List<Skill> = emptyList(),
        val selectedEquipment: List<Equipment> = emptyList(),
        val selectedSpells: List<Spell> = emptyList(),
        val selectedPersonalityTraits: List<String> = emptyList(),
        val selectedIdeals: List<String> = emptyList(),
        val selectedBonds: List<String> = emptyList(),
        val selectedFlaws: List<String> = emptyList(),
    ) {
        val isCharacterComplete: Boolean
            get() = (characterName.isNotBlank()
                && background != null
                && race != null
                && subrace != null
                && characterClass != null
                && selectedSkills.isNotEmpty()
                && selectedEquipment.isNotEmpty()
                && selectedPersonalityTraits.isNotEmpty()
                && selectedIdeals.isNotEmpty()
                && selectedBonds.isNotEmpty()
                && selectedFlaws.isNotEmpty())

        val requiredSelections: List<String>
            get() {
                return buildList {
                    if (characterName.isBlank()) add("Character Name")
                    if (background == null) add("Background")
                    if (race == null) add("Race")
                    if (subrace == null) add("Subrace")
                    if (characterClass == null) add("Class")
                    if (selectedSkills.isEmpty()) add("Skills")
                    if (selectedEquipment.isEmpty()) add("Starting Equipment")
                    if (selectedPersonalityTraits.isEmpty()) add("Personality Traits")
                    if (selectedIdeals.isEmpty()) add("Ideals")
                    if (selectedBonds.isEmpty()) add("Bonds")
                    if (selectedFlaws.isEmpty()) add("Flaws")
                }
            }
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val backgrounds = backgroundRepository.allBackgrounds.first()
                val characterClasses = characterClassRepository.allClasses.first()
                val races = racesRepository.allRaces.first()
                _state.update {
                    it.copy(
                        isLoading = false,
                        backgrounds = backgrounds,
                        classes = characterClasses,
                        races = races,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    private fun onNameChanged(name: String) {
        _state.update { it.copy(characterName = name) }
    }

    private fun onBackgroundChanged(backgroundName: String) {
        if (state.value.background?.name != backgroundName) {
            viewModelScope.launch {
                val background = state.value.backgrounds.find { it.name == backgroundName }
                _state.update {
                    it.copy(
                        background = background,
                        selectedPersonalityTraits = emptyList(),
                        selectedIdeals = emptyList(),
                        selectedBonds = emptyList(),
                        selectedFlaws = emptyList(),
                    )
                }
            }
        }
    }

    private fun onRaceChanged(raceId: String) {
        viewModelScope.launch {
            val race = state.value.races.find { it.id == raceId }
            _state.update { it.copy(race = race) }
        }
    }

    private fun onSubraceChanged(subraceId: String) {
        viewModelScope.launch {
            val subrace = state.value.race?.subraces?.find { it.id == subraceId }
            _state.update { it.copy(subrace = subrace) }
        }
    }

    private fun onClassChanged(characterClass: CharacterClass) {
        _state.update { it.copy(characterClass = characterClass) }
    }

    private fun onSkillSelected(skill: Skill, isSelected: Boolean) {
        val updatedSkills = if (isSelected) {
            state.value.selectedSkills + skill
        } else {
            state.value.selectedSkills - skill
        }
        _state.update { it.copy(selectedSkills = updatedSkills) }
    }

    private fun onEquipmentSelected(equipment: Equipment, isSelected: Boolean) {
        val updatedEquipment = if (isSelected) {
            state.value.selectedEquipment + equipment
        } else {
            state.value.selectedEquipment - equipment
        }
        _state.update { it.copy(selectedEquipment = updatedEquipment) }
    }

    private fun onSpellSelected(spell: Spell, isSelected: Boolean) {
        val updatedSpells = if (isSelected) {
            state.value.selectedSpells + spell
        } else {
            state.value.selectedSpells - spell
        }
        _state.update { it.copy(selectedSpells = updatedSpells) }
    }

    private fun onPersonalityTraitsChanged(traits: List<String>) {
        _state.update { it.copy(selectedPersonalityTraits = traits) }
    }

    private fun onIdealsChanged(ideals: List<String>) {
        _state.update { it.copy(selectedIdeals = ideals) }
    }

    private fun onBondsChanged(bonds: List<String>) {
        _state.update { it.copy(selectedBonds = bonds) }
    }

    private fun onFlawsChanged(flaws: List<String>) {
        _state.update { it.copy(selectedFlaws = flaws) }
    }

    private fun onSaveCharacter() {
        if (!state.value.isCharacterComplete) {
            return
        }

        viewModelScope.launch {
            val currentState = state.value
            val character = Character(
                id = UUID.randomUUID().toString(),
                name = currentState.characterName,
                race = EntityRef(currentState.race!!.id),
                subrace = currentState.subrace?.let { EntityRef(it.id) },
                classes = mapOf(EntityRef(currentState.characterClass!!.id) to 1),
                background = EntityRef(currentState.background!!.id),
                abilityScores = currentState.abilityScores.mapKeys { (key, _) -> EntityRef(key) },
                proficiencies = currentState.skillProficiencies.map { EntityRef(it) }.toSet(),
            )
            characterRepository.saveCharacter(character)
        }
    }

    fun handleEvent(event: CharacterCreationEvent) {
        when (event) {
            is CharacterCreationEvent.NameChanged -> onNameChanged(event.name)
            is CharacterCreationEvent.BackgroundChanged -> onBackgroundChanged(event.backgroundName)
            is CharacterCreationEvent.RaceChanged -> onRaceChanged(event.raceId)
            is CharacterCreationEvent.SubraceChanged -> onSubraceChanged(event.subraceId)
            is CharacterCreationEvent.ClassSelection -> onClassChanged(event.characterClass)
            is CharacterCreationEvent.EquipmentSelected -> onEquipmentSelected(event.equipment, event.isSelected)
            is CharacterCreationEvent.SkillsSelected -> onSkillSelected(event.skill, event.isSelected)
            is CharacterCreationEvent.SpellsSelected -> onSpellSelected(event.spell, event.isSelected)
            is CharacterCreationEvent.SaveCharacter -> onSaveCharacter()
            is CharacterCreationEvent.PersonalityTraitsChanged -> onPersonalityTraitsChanged(event.traits)
            is CharacterCreationEvent.IdealsChanged -> onIdealsChanged(event.ideals)
            is CharacterCreationEvent.BondsChanged -> onBondsChanged(event.bonds)
            is CharacterCreationEvent.FlawsChanged -> onFlawsChanged(event.flaws)
        }
    }
}

sealed interface CharacterCreationEvent {
    data class NameChanged(val name: String) : CharacterCreationEvent
    data class BackgroundChanged(val backgroundName: String) : CharacterCreationEvent
    data class RaceChanged(val raceId: String) : CharacterCreationEvent
    data class SubraceChanged(val subraceId: String) : CharacterCreationEvent
    data class ClassSelection(val characterClass: CharacterClass) : CharacterCreationEvent
    data class SkillsSelected(val skill: Skill, val isSelected: Boolean) : CharacterCreationEvent
    data class EquipmentSelected(val equipment: Equipment, val isSelected: Boolean) : CharacterCreationEvent
    data class SpellsSelected(val spell: Spell, val isSelected: Boolean) : CharacterCreationEvent
    data class PersonalityTraitsChanged(val traits: List<String>) : CharacterCreationEvent
    data class IdealsChanged(val ideals: List<String>) : CharacterCreationEvent
    data class BondsChanged(val bonds: List<String>) : CharacterCreationEvent
    data class FlawsChanged(val flaws: List<String>) : CharacterCreationEvent
    data object SaveCharacter : CharacterCreationEvent
}
