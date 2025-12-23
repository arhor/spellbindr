package com.github.arhor.spellbindr.ui.feature.characters

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Ability
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSheetInputError
import com.github.arhor.spellbindr.domain.model.SavingThrowEntry
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.SkillEntry
import com.github.arhor.spellbindr.domain.repository.AbilityRepository
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
    private val abilityRepository: AbilityRepository,
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
        observeAbilities()
        initialId?.let(::observeCharacter)
    }

    private fun observeAbilities() {
        abilityRepository.getAbilities()
            .onEach { abilities ->
                val metadata = abilities.associateBy { it.id }
                _uiState.update { it.copy(abilityMetadata = metadata) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeCharacter(id: String) {
        loadCharacterSheetUseCase(id)
            .onEach { sheet ->
                if (sheet != null) {
                    baseSheet = sheet
                    _uiState.update { currentState ->
                        sheet.toEditorState(computeDerivedBonusesUseCase, currentState.abilityMetadata)
                    }
                } else {
                    _uiState.update { it.copy(characterId = id, isLoading = false) }
                }
            }
            .catch { throwable ->
                _uiState.update { it.copy(isLoading = false, saveError = throwable.message) }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: CharacterEditorAction) {
        _uiState.update { reduceCharacterEditorState(it, action, computeDerivedBonusesUseCase) }
    }

    fun onSaveClicked() {
        val validation = validateCharacterSheetUseCase(_uiState.value.toDomainInput())
        _uiState.update { state ->
            state.copy(
                nameError = validation.nameError?.toRequiredMessage(),
                levelError = validation.levelError?.toLevelMessage(),
                abilities = state.abilities.map { ability ->
                    val error = validation.abilityErrors[ability.abilityId]?.toRequiredMessage()
                    ability.copy(error = error)
                },
                maxHitPointsError = validation.maxHpError?.toRequiredMessage(),
            )
        }
        if (validation.hasErrors) return

        viewModelScope.launch {
            val sheet = buildCharacterSheetFromInputsUseCase(_uiState.value.toDomainInput(), baseSheet)
            _uiState.update {
                it.copy(
                    isSaving = true,
                    saveError = null,
                    characterId = sheet.id,
                    mode = EditorMode.Edit,
                )
            }
            runCatching { saveCharacterSheetUseCase(sheet) }
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
    val abilityMetadata: Map<String, Ability> = emptyMap(),
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
    val abilityId: String,
    val score: String = "10",
    val error: String? = null,
) {
    companion object {
        fun defaults(): List<AbilityFieldState> = listOf("STR", "DEX", "CON", "INT", "WIS", "CHA").map { abilityId ->
            AbilityFieldState(abilityId = abilityId, score = "10")
        }
    }
}

@Immutable
data class SavingThrowInputState(
    val abilityId: String,
    val bonus: Int = 0,
    val proficient: Boolean = false,
) {
    companion object {
        fun defaults(): List<SavingThrowInputState> =
            listOf("STR", "DEX", "CON", "INT", "WIS", "CHA").map { abilityId ->
                SavingThrowInputState(abilityId = abilityId)
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

private fun CharacterSheet.toEditorState(
    computeDerivedBonusesUseCase: ComputeDerivedBonusesUseCase,
    abilityMetadata: Map<String, Ability>,
): CharacterEditorUiState {
    val baseState = CharacterEditorUiState(
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
        abilityMetadata = abilityMetadata,
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
    AbilityFieldState("STR", strength.toString()),
    AbilityFieldState("DEX", dexterity.toString()),
    AbilityFieldState("CON", constitution.toString()),
    AbilityFieldState("INT", intelligence.toString()),
    AbilityFieldState("WIS", wisdom.toString()),
    AbilityFieldState("CHA", charisma.toString()),
)

private fun List<SavingThrowEntry>.savingThrowsToInputStates(): List<SavingThrowInputState> =
    listOf("STR", "DEX", "CON", "INT", "WIS", "CHA").map { abilityId ->
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
