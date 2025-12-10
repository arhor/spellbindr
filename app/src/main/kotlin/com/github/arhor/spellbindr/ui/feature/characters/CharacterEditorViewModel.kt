package com.github.arhor.spellbindr.ui.feature.characters

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.AbilityScores
import com.github.arhor.spellbindr.data.model.CharacterSheet
import com.github.arhor.spellbindr.data.model.SavingThrowEntry
import com.github.arhor.spellbindr.data.model.SkillEntry
import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.data.model.predefined.Skill
import com.github.arhor.spellbindr.data.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CharacterEditorViewModel @Inject constructor(
    private val repository: CharacterRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialId: String? = savedStateHandle.get<String>("characterId")
    private var baseSheet: CharacterSheet? = null

    private val _uiState = MutableStateFlow(
        CharacterEditorUiState(
            characterId = initialId,
            mode = if (initialId == null) EditorMode.Create else EditorMode.Edit,
            isLoading = initialId != null,
        )
    )
    val uiState: StateFlow<CharacterEditorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CharacterEditorEvent>()
    val events: SharedFlow<CharacterEditorEvent> = _events.asSharedFlow()

    init {
        initialId?.let(::observeCharacter)
    }

    private fun observeCharacter(id: String) {
        repository.observeCharacterSheet(id)
            .onEach { sheet ->
                if (sheet != null) {
                    baseSheet = sheet
                    _uiState.value = sheet.toEditorState()
                } else {
                    _uiState.update { it.copy(characterId = id, isLoading = false) }
                }
            }
            .catch { throwable ->
                _uiState.update { it.copy(isLoading = false, saveError = throwable.message) }
            }
            .launchIn(viewModelScope)
    }

    fun onNameChanged(value: String) = _uiState.update {
        it.copy(name = value, nameError = null)
    }

    fun onClassChanged(value: String) = _uiState.update { it.copy(className = value) }

    fun onLevelChanged(value: String) = _uiState.update {
        it.copy(level = value, levelError = null)
    }

    fun onRaceChanged(value: String) = _uiState.update { it.copy(race = value) }

    fun onBackgroundChanged(value: String) = _uiState.update { it.copy(background = value) }

    fun onAlignmentChanged(value: String) = _uiState.update { it.copy(alignment = value) }

    fun onExperienceChanged(value: String) = _uiState.update { it.copy(experiencePoints = value) }

    fun onAbilityChanged(ability: Ability, value: String) = _uiState.update { state ->
        state.copy(
            abilities = state.abilities.map { field ->
                if (field.ability == ability) field.copy(score = value, error = null) else field
            }
        ).withUpdatedDerivedBonuses()
    }

    fun onProficiencyBonusChanged(value: String) = _uiState.update {
        it.copy(proficiencyBonus = value).withUpdatedDerivedBonuses()
    }

    fun onInspirationChanged(value: Boolean) = _uiState.update { it.copy(inspiration = value) }

    fun onMaxHpChanged(value: String) = _uiState.update { it.copy(maxHitPoints = value, maxHitPointsError = null) }

    fun onCurrentHpChanged(value: String) = _uiState.update { it.copy(currentHitPoints = value) }

    fun onTemporaryHpChanged(value: String) = _uiState.update { it.copy(temporaryHitPoints = value) }

    fun onArmorClassChanged(value: String) = _uiState.update { it.copy(armorClass = value) }

    fun onInitiativeChanged(value: String) = _uiState.update { it.copy(initiative = value) }

    fun onSpeedChanged(value: String) = _uiState.update { it.copy(speed = value) }

    fun onHitDiceChanged(value: String) = _uiState.update { it.copy(hitDice = value) }

    fun onSavingThrowProficiencyChanged(ability: Ability, value: Boolean) = _uiState.update { state ->
        state.copy(
            savingThrows = state.savingThrows.map { entry ->
                if (entry.ability == ability) entry.copy(proficient = value) else entry
            }
        ).withUpdatedDerivedBonuses()
    }

    fun onSkillProficiencyChanged(skill: Skill, value: Boolean) = _uiState.update { state ->
        state.copy(
            skills = state.skills.map { entry ->
                if (entry.skill == skill) entry.copy(proficient = value) else entry
            }
        ).withUpdatedDerivedBonuses()
    }

    fun onSkillExpertiseChanged(skill: Skill, value: Boolean) = _uiState.update { state ->
        state.copy(
            skills = state.skills.map { entry ->
                if (entry.skill == skill) entry.copy(expertise = value) else entry
            }
        ).withUpdatedDerivedBonuses()
    }

    fun onSensesChanged(value: String) = _uiState.update { it.copy(senses = value) }

    fun onLanguagesChanged(value: String) = _uiState.update { it.copy(languages = value) }

    fun onProficienciesChanged(value: String) = _uiState.update { it.copy(proficiencies = value) }

    fun onAttacksChanged(value: String) = _uiState.update { it.copy(attacksAndCantrips = value) }

    fun onFeaturesChanged(value: String) = _uiState.update { it.copy(featuresAndTraits = value) }

    fun onEquipmentChanged(value: String) = _uiState.update { it.copy(equipment = value) }

    fun onPersonalityTraitsChanged(value: String) = _uiState.update { it.copy(personalityTraits = value) }

    fun onIdealsChanged(value: String) = _uiState.update { it.copy(ideals = value) }

    fun onBondsChanged(value: String) = _uiState.update { it.copy(bonds = value) }

    fun onFlawsChanged(value: String) = _uiState.update { it.copy(flaws = value) }

    fun onNotesChanged(value: String) = _uiState.update { it.copy(notes = value) }

    fun onSaveClicked() {
        val validation = _uiState.value.validate()
        _uiState.update {
            it.copy(
                nameError = validation.nameError,
                levelError = validation.levelError,
                abilities = validation.abilityStates,
                maxHitPointsError = validation.maxHpError,
            )
        }
        if (validation.hasErrors) return

        viewModelScope.launch {
            val sheet = _uiState.value.toCharacterSheet(baseSheet)
            _uiState.update {
                it.copy(
                    isSaving = true,
                    saveError = null,
                    characterId = sheet.id,
                    mode = EditorMode.Edit,
                )
            }
            runCatching { repository.upsertCharacterSheet(sheet) }
                .onSuccess {
                    baseSheet = sheet
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(CharacterEditorEvent.Saved)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false, saveError = error.message) }
                    _events.emit(CharacterEditorEvent.Error("Unable to save character"))
                }
        }
    }
}

enum class EditorMode {
    Create,
    Edit,
}

sealed interface CharacterEditorEvent {
    data object Saved : CharacterEditorEvent
    data class Error(val message: String) : CharacterEditorEvent
}

@Immutable
data class CharacterEditorUiState(
    val characterId: String? = null,
    val mode: EditorMode = EditorMode.Create,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val nameError: String? = null,
    val className: String = "",
    val level: String = "1",
    val levelError: String? = null,
    val race: String = "",
    val background: String = "",
    val alignment: String = "",
    val experiencePoints: String = "",
    val abilities: List<AbilityFieldState> = AbilityFieldState.defaults(),
    val proficiencyBonus: String = "2",
    val inspiration: Boolean = false,
    val maxHitPoints: String = "1",
    val maxHitPointsError: String? = null,
    val currentHitPoints: String = "1",
    val temporaryHitPoints: String = "",
    val armorClass: String = "",
    val initiative: String = "",
    val speed: String = "30 ft",
    val hitDice: String = "",
    val savingThrows: List<SavingThrowInputState> = SavingThrowInputState.defaults(),
    val skills: List<SkillInputState> = SkillInputState.defaults(),
    val senses: String = "",
    val languages: String = "",
    val proficiencies: String = "",
    val attacksAndCantrips: String = "",
    val featuresAndTraits: String = "",
    val equipment: String = "",
    val personalityTraits: String = "",
    val ideals: String = "",
    val bonds: String = "",
    val flaws: String = "",
    val notes: String = "",
    val saveError: String? = null,
)

@Immutable
data class AbilityFieldState(
    val ability: Ability,
    val score: String = "10",
    val error: String? = null,
) {
    companion object {
        fun defaults(): List<AbilityFieldState> = Ability.entries.map { ability ->
            AbilityFieldState(ability = ability, score = "10")
        }
    }
}

@Immutable
data class SavingThrowInputState(
    val ability: Ability,
    val bonus: Int = 0,
    val proficient: Boolean = false,
) {
    companion object {
        fun defaults(): List<SavingThrowInputState> = Ability.entries.map { ability ->
            SavingThrowInputState(ability = ability)
        }
    }
}

@Immutable
data class SkillInputState(
    val skill: Skill,
    val bonus: Int = 0,
    val proficient: Boolean = false,
    val expertise: Boolean = false,
) {
    companion object {
        fun defaults(): List<SkillInputState> = Skill.entries.map { skill ->
            SkillInputState(skill = skill)
        }
    }
}

private data class ValidationResult(
    val nameError: String?,
    val levelError: String?,
    val abilityStates: List<AbilityFieldState>,
    val maxHpError: String?,
) {
    val hasErrors: Boolean =
        nameError != null || levelError != null || maxHpError != null || abilityStates.any { it.error != null }
}

private fun CharacterEditorUiState.validate(): ValidationResult {
    val updatedAbilities = abilities.map { ability ->
        val value = ability.score.toIntOrNull()
        if (value == null) ability.copy(error = "Required") else ability.copy(error = null)
    }
    val nameError = if (name.isBlank()) "Required" else null
    val levelValue = level.toIntOrNull()
    val levelError = if (levelValue == null || levelValue < 1) "Level must be ≥ 1" else null
    val maxHpValue = maxHitPoints.toIntOrNull()
    val maxHpError = if (maxHpValue == null || maxHpValue <= 0) "Required" else null
    return ValidationResult(
        nameError = nameError,
        levelError = levelError,
        abilityStates = updatedAbilities,
        maxHpError = maxHpError,
    )
}

private fun CharacterEditorUiState.toCharacterSheet(base: CharacterSheet?): CharacterSheet {
    val ensuredId = characterId ?: base?.id ?: UUID.randomUUID().toString()
    val abilityLookup = abilities.associateBy { it.ability }
    val abilityModifiers = abilityModifiers()
    val proficiencyValue = proficiencyBonus.toIntOrNull() ?: 0
    val baseline = base ?: CharacterSheet(id = ensuredId)
    return baseline.copy(
        id = ensuredId,
        name = name.trim(),
        level = level.toIntOrNull() ?: 1,
        className = className.trim(),
        race = race.trim(),
        background = background.trim(),
        alignment = alignment.trim(),
        experiencePoints = experiencePoints.toIntOrNull(),
        abilityScores = AbilityScores(
            strength = abilityLookup[Ability.STR]?.score?.toIntOrNull() ?: 10,
            dexterity = abilityLookup[Ability.DEX]?.score?.toIntOrNull() ?: 10,
            constitution = abilityLookup[Ability.CON]?.score?.toIntOrNull() ?: 10,
            intelligence = abilityLookup[Ability.INT]?.score?.toIntOrNull() ?: 10,
            wisdom = abilityLookup[Ability.WIS]?.score?.toIntOrNull() ?: 10,
            charisma = abilityLookup[Ability.CHA]?.score?.toIntOrNull() ?: 10,
        ),
        proficiencyBonus = proficiencyBonus.toIntOrNull() ?: 2,
        inspiration = inspiration,
        maxHitPoints = maxHitPoints.toIntOrNull() ?: baseline.maxHitPoints,
        currentHitPoints = currentHitPoints.toIntOrNull() ?: baseline.currentHitPoints,
        temporaryHitPoints = temporaryHitPoints.toIntOrNull() ?: baseline.temporaryHitPoints,
        armorClass = armorClass.toIntOrNull() ?: 10,
        initiative = initiative.toIntOrNull() ?: 0,
        speed = speed.trim(),
        hitDice = hitDice.trim(),
        savingThrows = savingThrows.map { entry ->
            SavingThrowEntry(
                ability = entry.ability,
                bonus = abilityModifiers[entry.ability].orZero() + entry.proficiencyBonus(proficiencyValue),
                proficient = entry.proficient,
            )
        },
        skills = skills.map { entry ->
            SkillEntry(
                skill = entry.skill,
                bonus = abilityModifiers[entry.skill.ability].orZero() + entry.proficiencyBonus(proficiencyValue),
                proficient = entry.proficient,
                expertise = entry.expertise,
            )
        },
        senses = senses,
        languages = languages,
        proficiencies = proficiencies,
        attacksAndCantrips = attacksAndCantrips,
        featuresAndTraits = featuresAndTraits,
        equipment = equipment,
        personalityTraits = personalityTraits,
        ideals = ideals,
        bonds = bonds,
        flaws = flaws,
        notes = notes,
    )
}

private fun CharacterSheet.toEditorState(): CharacterEditorUiState = CharacterEditorUiState(
    characterId = id,
    mode = EditorMode.Edit,
    isLoading = false,
    name = name,
    className = className,
    level = level.toString(),
    race = race,
    background = background,
    alignment = alignment,
    experiencePoints = experiencePoints?.toString() ?: "",
    abilities = abilityScores.toFieldStates(),
    proficiencyBonus = proficiencyBonus.toString(),
    inspiration = inspiration,
    maxHitPoints = maxHitPoints.toString(),
    currentHitPoints = currentHitPoints.toString(),
    temporaryHitPoints = temporaryHitPoints.toString(),
    armorClass = armorClass.toString(),
    initiative = initiative.toString(),
    speed = speed,
    hitDice = hitDice,
    savingThrows = savingThrows.savingThrowsToInputStates(),
    skills = skills.skillsToInputStates(),
    senses = senses,
    languages = languages,
    proficiencies = proficiencies,
    attacksAndCantrips = attacksAndCantrips,
    featuresAndTraits = featuresAndTraits,
    equipment = equipment,
    personalityTraits = personalityTraits,
    ideals = ideals,
    bonds = bonds,
    flaws = flaws,
    notes = notes,
).withUpdatedDerivedBonuses()

private fun AbilityScores.toFieldStates(): List<AbilityFieldState> = listOf(
    AbilityFieldState(Ability.STR, strength.toString()),
    AbilityFieldState(Ability.DEX, dexterity.toString()),
    AbilityFieldState(Ability.CON, constitution.toString()),
    AbilityFieldState(Ability.INT, intelligence.toString()),
    AbilityFieldState(Ability.WIS, wisdom.toString()),
    AbilityFieldState(Ability.CHA, charisma.toString()),
)

private fun List<SavingThrowEntry>.savingThrowsToInputStates(): List<SavingThrowInputState> =
    Ability.entries.map { ability ->
        val entry = firstOrNull { it.ability == ability }
        SavingThrowInputState(
            ability = ability,
            bonus = entry?.bonus ?: 0,
            proficient = entry?.proficient ?: false,
        )
    }

private fun List<SkillEntry>.skillsToInputStates(): List<SkillInputState> =
    Skill.entries.map { skill ->
        val entry = firstOrNull { it.skill == skill }
        SkillInputState(
            skill = skill,
            bonus = entry?.bonus ?: 0,
            proficient = entry?.proficient ?: false,
            expertise = entry?.expertise ?: false,
        )
    }

private fun CharacterEditorUiState.withUpdatedDerivedBonuses(): CharacterEditorUiState {
    val abilityModifiers = abilityModifiers()
    val proficiencyValue = proficiencyBonus.toIntOrNull() ?: 0

    return copy(
        savingThrows = savingThrows.map { entry ->
            entry.copy(
                bonus = abilityModifiers[entry.ability].orZero() + entry.proficiencyBonus(proficiencyValue),
            )
        },
        skills = skills.map { entry ->
            entry.copy(
                bonus = abilityModifiers[entry.skill.ability].orZero() + entry.proficiencyBonus(proficiencyValue),
            )
        },
    )
}

private fun CharacterEditorUiState.abilityModifiers(): Map<Ability, Int> = abilities.associate { field ->
    val score = field.score.toIntOrNull() ?: 10
    field.ability to ((score - 10) / 2)
}

private fun SavingThrowInputState.proficiencyBonus(proficiencyValue: Int): Int =
    if (proficient) proficiencyValue else 0

private fun SkillInputState.proficiencyBonus(proficiencyValue: Int): Int = when {
    expertise -> proficiencyValue * 2
    proficient -> proficiencyValue
    else -> 0
}

private fun Int?.orZero(): Int = this ?: 0
