package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Background
import com.github.arhor.spellbindr.data.model.Character
import com.github.arhor.spellbindr.data.model.CharacterClass
import com.github.arhor.spellbindr.data.model.Choice.EquipmentCategoriesChoice
import com.github.arhor.spellbindr.data.model.Choice.EquipmentChoice
import com.github.arhor.spellbindr.data.model.EntityRef
import com.github.arhor.spellbindr.data.model.Equipment
import com.github.arhor.spellbindr.data.model.EquipmentCategory
import com.github.arhor.spellbindr.data.model.Language
import com.github.arhor.spellbindr.data.model.Race
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.model.Subrace
import com.github.arhor.spellbindr.data.model.predefined.Skill
import com.github.arhor.spellbindr.data.repository.BackgroundRepository
import com.github.arhor.spellbindr.data.repository.CharacterClassRepository
import com.github.arhor.spellbindr.data.repository.CharacterRepository
import com.github.arhor.spellbindr.data.repository.EquipmentRepository
import com.github.arhor.spellbindr.data.repository.LanguagesRepository
import com.github.arhor.spellbindr.data.repository.RacesRepository
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
    private val languagesRepository: LanguagesRepository,
    private val equipmentRepository: EquipmentRepository,
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
        val abilityScoresRolled: List<Int> = emptyList(),
        val abilityScoresInEdit: Map<String, Int> = emptyMap(),
        val pointBuyPoints: Int = 27,

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
        val languages: List<Language> = emptyList(),
        val allEquipment: List<Equipment> = emptyList(),
        val selectedSkills: List<Skill> = emptyList(),
        val selectedSpells: List<Spell> = emptyList(),
        val selectedPersonalityTraits: List<String> = emptyList(),
        val selectedIdeals: List<String> = emptyList(),
        val selectedBonds: List<String> = emptyList(),
        val selectedFlaws: List<String> = emptyList(),
        val selectedLanguages: List<String> = emptyList(),
        val selectedBackgroundEquipment: List<String> = emptyList(),
        val availableBackgroundEquipment: List<Equipment> = emptyList(),
    ) {
        val isCharacterComplete: Boolean
            get() = (background != null
                && race != null
                && subrace != null
                && characterClass != null
                && selectedSkills.isNotEmpty())

        val requiredSelections: List<String>
            get() {
                return buildList {
                    if (background == null) add("Background")
                    if (race == null) add("Race")
                    if (subrace == null) add("Subrace")
                    if (characterClass == null) add("Class")
                    if (selectedSkills.isEmpty()) add("Skills")
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
                val languages = languagesRepository.allLanguages.first()
                val equipment = equipmentRepository.allEquipment.first()
                _state.update {
                    it.copy(
                        isLoading = false,
                        backgrounds = backgrounds,
                        classes = characterClasses,
                        races = races,
                        languages = languages,
                        allEquipment = equipment,
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
                val availableEquipment = background?.equipmentChoice
                    ?.let { choice ->
                        when (choice) {
                            is EquipmentCategoriesChoice -> {
                                val allowedCategories: Set<EquipmentCategory> = choice.from.categories
                                    .map { it.replace('-', '_').uppercase() }
                                    .mapNotNull { value -> runCatching { EquipmentCategory.valueOf(value) }.getOrNull() }
                                    .toSet()
                                state.value.allEquipment.filter { equipment ->
                                    equipment.categories.any { category -> category in allowedCategories }
                                }
                            }

                            is EquipmentChoice -> {
                                val ids = choice.from.toSet()
                                state.value.allEquipment.filter { it.id in ids }
                            }

                            else -> emptyList()
                        }
                    } ?: emptyList()
                _state.update {
                    it.copy(
                        background = background,
                        selectedPersonalityTraits = emptyList(),
                        selectedIdeals = emptyList(),
                        selectedBonds = emptyList(),
                        selectedFlaws = emptyList(),
                        selectedLanguages = emptyList(),
                        selectedBackgroundEquipment = emptyList(),
                        availableBackgroundEquipment = availableEquipment,
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

    private fun onLanguagesChanged(languages: List<String>) {
        _state.update { it.copy(selectedLanguages = languages) }
    }

    private fun onBackgroundEquipmentChanged(equipmentIds: List<String>) {
        _state.update { it.copy(selectedBackgroundEquipment = equipmentIds) }
    }

    private fun onAbilityScoresChanged(scores: Map<String, Int>) {
        _state.update { it.copy(abilityScores = scores) }
    }

    private fun onSaveCharacter() {
        if (!state.value.isCharacterComplete) {
            return
        }

        viewModelScope.launch {
            val currentState = state.value
            val character = Character(
                id = UUID.randomUUID().toString(),
                name = currentState.characterName.ifBlank { "Unnamed Character" },
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
            is CharacterCreationEvent.SkillsSelected -> onSkillSelected(event.skill, event.isSelected)
            is CharacterCreationEvent.SpellsSelected -> onSpellSelected(event.spell, event.isSelected)
            is CharacterCreationEvent.SaveCharacter -> onSaveCharacter()
            is CharacterCreationEvent.PersonalityTraitsChanged -> onPersonalityTraitsChanged(event.traits)
            is CharacterCreationEvent.IdealsChanged -> onIdealsChanged(event.ideals)
            is CharacterCreationEvent.BondsChanged -> onBondsChanged(event.bonds)
            is CharacterCreationEvent.FlawsChanged -> onFlawsChanged(event.flaws)
            is CharacterCreationEvent.AbilityScoresChanged -> onAbilityScoresChanged(event.scores)
            is CharacterCreationEvent.LanguagesChanged -> onLanguagesChanged(event.languageIds)
            is CharacterCreationEvent.BackgroundEquipmentChanged -> onBackgroundEquipmentChanged(event.equipmentIds)
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
    data class SpellsSelected(val spell: Spell, val isSelected: Boolean) : CharacterCreationEvent
    data class PersonalityTraitsChanged(val traits: List<String>) : CharacterCreationEvent
    data class IdealsChanged(val ideals: List<String>) : CharacterCreationEvent
    data class BondsChanged(val bonds: List<String>) : CharacterCreationEvent
    data class FlawsChanged(val flaws: List<String>) : CharacterCreationEvent
    data class AbilityScoresChanged(val scores: Map<String, Int>) : CharacterCreationEvent
    data class LanguagesChanged(val languageIds: List<String>) : CharacterCreationEvent
    data class BackgroundEquipmentChanged(val equipmentIds: List<String>) : CharacterCreationEvent
    data object SaveCharacter : CharacterCreationEvent
}
