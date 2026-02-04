package com.github.arhor.spellbindr.ui.feature.character.editor

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSheetInputError
import com.github.arhor.spellbindr.domain.model.SavingThrowEntry
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.SkillEntry
import com.github.arhor.spellbindr.domain.usecase.BuildCharacterSheetFromInputsUseCase
import com.github.arhor.spellbindr.domain.usecase.ComputeDerivedBonusesUseCase
import com.github.arhor.spellbindr.domain.usecase.LoadCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.SaveCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ValidateCharacterSheetUseCase
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
import javax.inject.Inject

@HiltViewModel
class CharacterEditorViewModel @Inject constructor(
    private val loadCharacterSheetUseCase: LoadCharacterSheetUseCase,
    private val saveCharacterSheetUseCase: SaveCharacterSheetUseCase,
    private val validateCharacterSheetUseCase: ValidateCharacterSheetUseCase,
    private val computeDerivedBonusesUseCase: ComputeDerivedBonusesUseCase,
    private val buildCharacterSheetFromInputsUseCase: BuildCharacterSheetFromInputsUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialId: String? = savedStateHandle.get<String>("characterId")
    private var baseSheet: CharacterSheet? = null

    private val _uiState = MutableStateFlow<CharacterEditorUiState>(
        if (initialId == null) {
            CharacterEditorUiState.Content()
        } else {
            CharacterEditorUiState.Loading
        },
    )
    val uiState: StateFlow<CharacterEditorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CharacterEditorEvent>()
    val events: SharedFlow<CharacterEditorEvent> = _events.asSharedFlow()

    init {
        initialId?.let(::observeCharacter)
    }

    private fun observeCharacter(id: String) {
        loadCharacterSheetUseCase(id)
            .onEach { sheet ->
                _uiState.value = if (sheet != null) {
                    baseSheet = sheet
                    sheet.toEditorState(computeDerivedBonusesUseCase)
                } else {
                    CharacterEditorUiState.Content(
                        characterId = id,
                        mode = EditorMode.Edit,
                    )
                }
            }
            .catch { throwable ->
                _uiState.value = CharacterEditorUiState.Error(
                    throwable.message ?: "Unable to load character",
                )
            }
            .launchIn(viewModelScope)
    }

    fun onNameChanged(value: String) {
        updateContent { it.copy(name = value, nameError = null) }
    }

    fun onClassChanged(value: String) {
        updateContent { it.copy(className = value) }
    }

    fun onLevelChanged(value: String) {
        updateContent { it.copy(level = value, levelError = null) }
    }

    fun onRaceChanged(value: String) {
        updateContent { it.copy(race = value) }
    }

    fun onBackgroundChanged(value: String) {
        updateContent { it.copy(background = value) }
    }

    fun onAlignmentChanged(value: String) {
        updateContent { it.copy(alignment = value) }
    }

    fun onExperienceChanged(value: String) {
        updateContent { it.copy(experiencePoints = value) }
    }

    fun onAbilityChanged(abilityId: AbilityId, value: String) {
        updateContent(recomputeDerivedBonuses = true) { state ->
            state.copy(
                abilities = state.abilities.map { field ->
                    if (field.abilityId == abilityId) field.copy(score = value, error = null) else field
                },
            )
        }
    }

    fun onProficiencyBonusChanged(value: String) {
        updateContent(recomputeDerivedBonuses = true) { it.copy(proficiencyBonus = value) }
    }

    fun onInspirationChanged(value: Boolean) {
        updateContent { it.copy(inspiration = value) }
    }

    fun onMaxHpChanged(value: String) {
        updateContent { it.copy(maxHitPoints = value, maxHitPointsError = null) }
    }

    fun onCurrentHpChanged(value: String) {
        updateContent { it.copy(currentHitPoints = value) }
    }

    fun onTemporaryHpChanged(value: String) {
        updateContent { it.copy(temporaryHitPoints = value) }
    }

    fun onArmorClassChanged(value: String) {
        updateContent { it.copy(armorClass = value) }
    }

    fun onInitiativeChanged(value: String) {
        updateContent { it.copy(initiative = value) }
    }

    fun onSpeedChanged(value: String) {
        updateContent { it.copy(speed = value) }
    }

    fun onHitDiceChanged(value: String) {
        updateContent { it.copy(hitDice = value) }
    }

    fun onSavingThrowProficiencyChanged(abilityId: AbilityId, value: Boolean) {
        updateContent(recomputeDerivedBonuses = true) { state ->
            state.copy(
                savingThrows = state.savingThrows.map { entry ->
                    if (entry.abilityId == abilityId) entry.copy(proficient = value) else entry
                },
            )
        }
    }

    fun onSkillProficiencyChanged(skill: Skill, value: Boolean) {
        updateContent(recomputeDerivedBonuses = true) { state ->
            state.copy(
                skills = state.skills.map { entry ->
                    if (entry.skill == skill) entry.copy(proficient = value) else entry
                },
            )
        }
    }

    fun onSkillExpertiseChanged(skill: Skill, value: Boolean) {
        updateContent(recomputeDerivedBonuses = true) { state ->
            state.copy(
                skills = state.skills.map { entry ->
                    if (entry.skill == skill) entry.copy(expertise = value) else entry
                },
            )
        }
    }

    fun onSensesChanged(value: String) {
        updateContent { it.copy(senses = value) }
    }

    fun onLanguagesChanged(value: String) {
        updateContent { it.copy(languages = value) }
    }

    fun onProficienciesChanged(value: String) {
        updateContent { it.copy(proficiencies = value) }
    }

    fun onAttacksChanged(value: String) {
        updateContent { it.copy(attacksAndCantrips = value) }
    }

    fun onFeaturesChanged(value: String) {
        updateContent { it.copy(featuresAndTraits = value) }
    }

    fun onEquipmentChanged(value: String) {
        updateContent { it.copy(equipment = value) }
    }

    fun onPersonalityTraitsChanged(value: String) {
        updateContent { it.copy(personalityTraits = value) }
    }

    fun onIdealsChanged(value: String) {
        updateContent { it.copy(ideals = value) }
    }

    fun onBondsChanged(value: String) {
        updateContent { it.copy(bonds = value) }
    }

    fun onFlawsChanged(value: String) {
        updateContent { it.copy(flaws = value) }
    }

    fun onNotesChanged(value: String) {
        updateContent { it.copy(notes = value) }
    }

    fun onSaveClicked() {
        val currentState = _uiState.value as? CharacterEditorUiState.Content ?: return
        val validation = validateCharacterSheetUseCase(currentState.toDomainInput())
        val validatedState = currentState.copy(
            nameError = validation.nameError?.toRequiredMessage(),
            levelError = validation.levelError?.toLevelMessage(),
            abilities = currentState.abilities.map { ability ->
                val error = validation.abilityErrors[ability.abilityId]?.toRequiredMessage()
                ability.copy(error = error)
            },
            maxHitPointsError = validation.maxHpError?.toRequiredMessage(),
        )
        _uiState.value = validatedState

        if (validation.hasErrors) {
            return
        }

        viewModelScope.launch {
            val sheet = buildCharacterSheetFromInputsUseCase(validatedState.toDomainInput(), baseSheet)
            _uiState.update { state ->
                if (state is CharacterEditorUiState.Content) {
                    state.copy(
                        isSaving = true,
                        saveError = null,
                        characterId = sheet.id,
                        mode = EditorMode.Edit,
                    )
                } else {
                    state
                }
            }
            runCatching { saveCharacterSheetUseCase(sheet) }
                .onSuccess {
                    baseSheet = sheet
                    _uiState.update { state ->
                        if (state is CharacterEditorUiState.Content) {
                            state.copy(isSaving = false)
                        } else {
                            state
                        }
                    }
                    _events.emit(CharacterEditorEvent.Saved)
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        if (state is CharacterEditorUiState.Content) {
                            state.copy(isSaving = false, saveError = error.message)
                        } else {
                            state
                        }
                    }
                    _events.emit(CharacterEditorEvent.Error("Unable to save character"))
                }
        }
    }

    private fun updateContent(
        recomputeDerivedBonuses: Boolean = false,
        transform: (CharacterEditorUiState.Content) -> CharacterEditorUiState.Content,
    ) {
        _uiState.update { state ->
            if (state is CharacterEditorUiState.Content) {
                val updated = transform(state)
                if (recomputeDerivedBonuses) {
                    val derived = computeDerivedBonusesUseCase(updated.toDomainInput())
                    updated.withDerivedBonuses(derived)
                } else {
                    updated
                }
            } else {
                state
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

sealed interface CharacterEditorUiState {
    @Immutable
    data object Loading : CharacterEditorUiState

    @Immutable
    data class Content(
        val characterId: String? = null,
        val mode: EditorMode = EditorMode.Create,
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
    ) : CharacterEditorUiState

    @Immutable
    data class Error(
        val message: String,
    ) : CharacterEditorUiState
}

@Immutable
data class AbilityFieldState(
    val abilityId: AbilityId,
    val score: String = "10",
    val error: String? = null,
) {
    val label: String
        get() = abilityId.uppercase()

    companion object {
        fun defaults(): List<AbilityFieldState> =
            AbilityIds.standardOrder.map { abilityId -> AbilityFieldState(abilityId = abilityId, score = "10") }
    }
}

@Immutable
data class SavingThrowInputState(
    val abilityId: AbilityId,
    val bonus: Int = 0,
    val proficient: Boolean = false,
) {
    companion object {
        fun defaults(): List<SavingThrowInputState> =
            AbilityIds.standardOrder.map { abilityId -> SavingThrowInputState(abilityId = abilityId) }
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

private fun CharacterSheet.toEditorState(
    computeDerivedBonusesUseCase: ComputeDerivedBonusesUseCase,
): CharacterEditorUiState.Content {
    val baseState = CharacterEditorUiState.Content(
        characterId = id,
        mode = EditorMode.Edit,
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
    )
    val derived = computeDerivedBonusesUseCase(baseState.toDomainInput())
    return baseState.withDerivedBonuses(derived)
}

private fun CharacterSheetInputError.toRequiredMessage(): String = "Required"

private fun CharacterSheetInputError.toLevelMessage(): String = when (this) {
    is CharacterSheetInputError.MinValue -> "Level must be ≥ $min"
    CharacterSheetInputError.Required -> "Level must be ≥ 1"
}

private fun AbilityScores.toFieldStates(): List<AbilityFieldState> = listOf(
    AbilityFieldState(AbilityIds.STR, strength.toString()),
    AbilityFieldState(AbilityIds.DEX, dexterity.toString()),
    AbilityFieldState(AbilityIds.CON, constitution.toString()),
    AbilityFieldState(AbilityIds.INT, intelligence.toString()),
    AbilityFieldState(AbilityIds.WIS, wisdom.toString()),
    AbilityFieldState(AbilityIds.CHA, charisma.toString()),
)

private fun List<SavingThrowEntry>.savingThrowsToInputStates(): List<SavingThrowInputState> =
    AbilityIds.standardOrder.map { abilityId ->
        val entry = firstOrNull { it.abilityId == abilityId }
        SavingThrowInputState(
            abilityId = abilityId,
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
