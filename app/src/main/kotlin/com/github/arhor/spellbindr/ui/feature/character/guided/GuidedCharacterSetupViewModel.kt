package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.Equipment
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.Language
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.usecase.ObserveAllBackgroundsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllCharacterClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllEquipmentUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllFeaturesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllLanguagesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllRacesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllTraitsUseCase
import com.github.arhor.spellbindr.domain.usecase.SaveCharacterSheetUseCase
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedReferenceData
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedReferenceDataState
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedSpellsData
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.buildGuidedCharacterSheet
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.computeGuidedPreview
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.computeGuidedSetupSteps
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.defaultPointBuyScores
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.defaultStandardArrayAssignments
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.guidedPointBuyTotalCost
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.observeGuidedReferenceDataState
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.observeGuidedSpellsDataState
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.validateGuidedSetupContent
import com.github.arhor.spellbindr.ui.feature.character.guided.model.AbilityScoreMethod
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedCharacterPreview
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedStep
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class GuidedCharacterSetupViewModel @Inject constructor(
    observeClasses: ObserveAllCharacterClassesUseCase,
    observeRaces: ObserveAllRacesUseCase,
    observeTraits: ObserveAllTraitsUseCase,
    observeBackgrounds: ObserveAllBackgroundsUseCase,
    observeLanguages: ObserveAllLanguagesUseCase,
    observeFeatures: ObserveAllFeaturesUseCase,
    observeEquipment: ObserveAllEquipmentUseCase,
    private val observeSpells: ObserveAllSpellsUseCase,
    private val saveCharacterSheet: SaveCharacterSheetUseCase,
) : ViewModel() {

    @Immutable
    private data class State(
        val step: GuidedStep = GuidedStep.BASICS,
        val name: String = "",

        val classId: String? = null,
        val subclassId: String? = null,

        val raceId: String? = null,
        val subraceId: String? = null,

        val backgroundId: String? = null,

        val abilityMethod: AbilityScoreMethod? = null,
        val standardArrayAssignments: Map<AbilityId, Int?> = defaultStandardArrayAssignments(),
        val pointBuyScores: Map<AbilityId, Int> = defaultPointBuyScores(),

        val choiceSelections: Map<String, Set<String>> = emptyMap(),

        val isSaving: Boolean = false,
    )

    private val _state = MutableStateFlow(State())

    private val _effects = MutableSharedFlow<GuidedCharacterSetupEffect>()
    val effects: SharedFlow<GuidedCharacterSetupEffect> = _effects.asSharedFlow()

    private val referenceDataState: StateFlow<GuidedReferenceDataState> = observeGuidedReferenceDataState(
        scope = viewModelScope,
        observeClasses = observeClasses,
        observeRaces = observeRaces,
        observeTraits = observeTraits,
        observeBackgrounds = observeBackgrounds,
        observeLanguages = observeLanguages,
        observeFeatures = observeFeatures,
        observeEquipment = observeEquipment,
    )

    private val spellsData: StateFlow<GuidedSpellsData> = observeGuidedSpellsDataState(
        scope = viewModelScope,
        shouldLoadFlow = combine(_state, referenceDataState) { state, reference ->
            val referenceData = (reference as? GuidedReferenceDataState.Content)?.data
            shouldLoadSpells(state, referenceData)
        },
        observeSpells = observeSpells,
    )

    private fun shouldLoadSpells(state: State, referenceData: GuidedReferenceData?): Boolean {
        val hasSpellSelections = state.choiceSelections.keys.any { key ->
            key.startsWith(SPELL_CHOICE_PREFIX) || key.endsWith("/spell")
        }
        if (hasSpellSelections) return true
        if (state.step == GuidedStep.SPELLS) return true

        if (state.step != GuidedStep.RACE) return false

        val race = state.raceId?.let { id -> referenceData?.races?.firstOrNull { it.id == id } } ?: return false
        val traitIds = buildList {
            addAll(race.traits.map { it.id })
            val subrace = state.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
            if (subrace != null) addAll(subrace.traits.map { it.id })
        }
        val traitsById = referenceData?.traitsById ?: return false
        return traitIds.mapNotNull(traitsById::get).any { it.spellChoice != null }
    }

    val uiState: StateFlow<GuidedCharacterSetupUiState> = combine(
        _state,
        referenceDataState,
        spellsData,
    ) { state, referenceDataState, spellsData ->
        when (referenceDataState) {
            is GuidedReferenceDataState.Loading ->
                GuidedCharacterSetupUiState.Loading

            is GuidedReferenceDataState.Failure ->
                GuidedCharacterSetupUiState.Failure(referenceDataState.errorMessage)

            is GuidedReferenceDataState.Content -> {
                spellsData.errorMessage?.let { return@combine GuidedCharacterSetupUiState.Failure(it) }
                val referenceData = referenceDataState.data
                val selectedClass = state.classId?.let { id ->
                    referenceData.classes.firstOrNull { it.id == id }
                }

                val steps = computeSteps(
                    selectedClass = selectedClass,
                    featuresById = referenceData.featuresById,
                )

                val resolvedStep = state.step.takeIf { it in steps } ?: steps.first()
                val currentIndex = steps.indexOf(resolvedStep).coerceAtLeast(0)

                val selection = GuidedSelection(
                    classId = state.classId,
                    subclassId = state.subclassId,
                    raceId = state.raceId,
                    subraceId = state.subraceId,
                    backgroundId = state.backgroundId,
                    abilityMethod = state.abilityMethod,
                    standardArrayAssignments = state.standardArrayAssignments,
                    pointBuyScores = state.pointBuyScores,
                    choiceSelections = state.choiceSelections,
                )

                val preview = computePreview(
                    selection = selection,
                    selectedClass = selectedClass,
                    races = referenceData.races,
                    backgrounds = referenceData.backgrounds,
                    traitsById = referenceData.traitsById,
                    featuresById = referenceData.featuresById,
                )

                val content = GuidedCharacterSetupUiState.Content(
                    step = resolvedStep,
                    steps = steps,
                    currentStepIndex = currentIndex,
                    totalSteps = steps.size,
                    name = state.name,
                    classes = referenceData.classes,
                    races = referenceData.races,
                    backgrounds = referenceData.backgrounds,
                    languages = referenceData.languages,
                    equipment = referenceData.equipment,
                    traitsById = referenceData.traitsById,
                    featuresById = referenceData.featuresById,
                    languagesById = referenceData.languagesById,
                    equipmentById = referenceData.equipmentById,
                    spells = spellsData.spells,
                    spellsById = spellsData.spellsById,
                    referenceDataVersion = referenceData.version,
                    selection = selection,
                    preview = preview,
                    isSaving = state.isSaving,
                )
                content.copy(validation = validate(content))
            }
        }
    }.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        GuidedCharacterSetupUiState.Loading
    )

    fun dispatch(intent: GuidedCharacterSetupIntent) {
        when (intent) {
            is GuidedCharacterSetupIntent.NameChanged -> onNameChanged(intent.value)
            is GuidedCharacterSetupIntent.ClassSelected -> onClassSelected(intent.classId)
            is GuidedCharacterSetupIntent.SubclassSelected -> onSubclassSelected(intent.subclassId)
            is GuidedCharacterSetupIntent.RaceSelected -> onRaceSelected(intent.raceId)
            is GuidedCharacterSetupIntent.SubraceSelected -> onSubraceSelected(intent.subraceId)
            is GuidedCharacterSetupIntent.BackgroundSelected -> onBackgroundSelected(intent.backgroundId)
            is GuidedCharacterSetupIntent.AbilityMethodSelected -> onAbilityMethodSelected(intent.method)
            is GuidedCharacterSetupIntent.StandardArrayAssigned -> onStandardArrayAssigned(
                intent.abilityId,
                intent.score
            )

            is GuidedCharacterSetupIntent.PointBuyIncrement -> onPointBuyIncrement(intent.abilityId)
            is GuidedCharacterSetupIntent.PointBuyDecrement -> onPointBuyDecrement(intent.abilityId)
            is GuidedCharacterSetupIntent.ChoiceToggled -> onChoiceToggled(
                intent.key,
                intent.optionId,
                intent.maxSelected
            )

            GuidedCharacterSetupIntent.NextClicked -> onNext()
            GuidedCharacterSetupIntent.BackClicked -> onBack()
            GuidedCharacterSetupIntent.CreateClicked -> onCreateCharacter()
            is GuidedCharacterSetupIntent.GoToStep -> onGoToStep(intent.step)
        }
    }

    private fun onNameChanged(value: String) {
        _state.update { it.copy(name = value) }
    }

    private fun onClassSelected(classId: String) {
        _state.update {
            it.copy(
                classId = classId,
                subclassId = null,
                choiceSelections = it.choiceSelections.filterKeys { key ->
                    !key.startsWith(CLASS_CHOICE_PREFIX) &&
                        !key.startsWith(FEATURE_CHOICE_PREFIX) &&
                        !key.startsWith(SPELL_CHOICE_PREFIX)
                },
            )
        }
    }

    private fun onSubclassSelected(subclassId: String) {
        _state.update { it.copy(subclassId = subclassId) }
    }

    private fun onRaceSelected(raceId: String) {
        _state.update {
            it.copy(
                raceId = raceId,
                subraceId = null,
                choiceSelections = it.choiceSelections.filterKeys { key -> !key.startsWith(RACE_CHOICE_PREFIX) },
            )
        }
    }

    private fun onSubraceSelected(subraceId: String) {
        _state.update { it.copy(subraceId = subraceId) }
    }

    private fun onBackgroundSelected(backgroundId: String) {
        _state.update {
            it.copy(
                backgroundId = backgroundId,
                choiceSelections = it.choiceSelections.filterKeys { key -> !key.startsWith(BACKGROUND_CHOICE_PREFIX) },
            )
        }
    }

    private fun onAbilityMethodSelected(method: AbilityScoreMethod) {
        _state.update {
            it.copy(
                abilityMethod = method,
                standardArrayAssignments = defaultStandardArrayAssignments(),
                pointBuyScores = defaultPointBuyScores(),
            )
        }
    }

    private fun onStandardArrayAssigned(abilityId: AbilityId, score: Int?) {
        _state.update { state ->
            state.copy(
                standardArrayAssignments = state.standardArrayAssignments.toMutableMap().apply {
                    this[abilityId] = score
                },
            )
        }
    }

    private fun onPointBuyIncrement(abilityId: AbilityId) {
        _state.update { state ->
            val current = state.pointBuyScores[abilityId] ?: 8
            val next = (current + 1).coerceAtMost(15)
            if (next == current) return@update state

            val nextScores = state.pointBuyScores.toMutableMap().apply { put(abilityId, next) }.toMap()
            if (pointBuyTotalCost(nextScores) > POINT_BUY_BUDGET) return@update state

            state.copy(pointBuyScores = nextScores)
        }
    }

    private fun onPointBuyDecrement(abilityId: AbilityId) {
        _state.update { state ->
            val current = state.pointBuyScores[abilityId] ?: 8
            val next = (current - 1).coerceAtLeast(8)
            if (next == current) return@update state

            state.copy(pointBuyScores = state.pointBuyScores.toMutableMap().apply { put(abilityId, next) }.toMap())
        }
    }

    private fun onChoiceToggled(key: String, optionId: String, maxSelected: Int) {
        _state.update { state ->
            val current = state.choiceSelections[key].orEmpty()
            val next = if (optionId in current) {
                current - optionId
            } else {
                if (maxSelected == 1) {
                    setOf(optionId)
                } else if (current.size < maxSelected) {
                    current + optionId
                } else {
                    current
                }
            }
            state.copy(choiceSelections = state.choiceSelections + (key to next))
        }
    }

    private fun onNext() {
        val content = uiState.value as? GuidedCharacterSetupUiState.Content ?: return
        val steps = content.steps
        val index = steps.indexOf(content.step)
        if (index < 0) return
        val next = steps.getOrNull(index + 1) ?: return
        _state.update { it.copy(step = next) }
    }

    private fun onBack() {
        val content = uiState.value as? GuidedCharacterSetupUiState.Content ?: return
        val steps = content.steps
        val index = steps.indexOf(content.step)
        if (index <= 0) return
        val prev = steps[index - 1]
        _state.update { it.copy(step = prev) }
    }

    private fun onGoToStep(step: GuidedStep) {
        val content = uiState.value as? GuidedCharacterSetupUiState.Content ?: return
        val resolved = when {
            step in content.steps -> step
            step == GuidedStep.CLASS_CHOICES -> GuidedStep.CLASS
            else -> return
        }
        _state.update { it.copy(step = resolved) }
    }

    private fun onCreateCharacter() {
        val content = uiState.value as? GuidedCharacterSetupUiState.Content ?: return
        val validation = validate(content)
        if (validation.hasErrors) return

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val sheet = buildCharacterSheet(content)
                saveCharacterSheet(sheet)
                _effects.emit(GuidedCharacterSetupEffect.CharacterCreated(sheet.id))
            } catch (t: Throwable) {
                _effects.emit(GuidedCharacterSetupEffect.Error(t.message ?: "Failed to save character."))
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    internal fun buildCharacterSheet(content: GuidedCharacterSetupUiState.Content): CharacterSheet =
        buildGuidedCharacterSheet(content)

    private fun computePreview(
        selection: GuidedSelection,
        selectedClass: com.github.arhor.spellbindr.domain.model.CharacterClass?,
        races: List<Race>,
        backgrounds: List<com.github.arhor.spellbindr.domain.model.Background>,
        traitsById: Map<String, Trait>,
        featuresById: Map<String, Feature>,
    ): GuidedCharacterPreview = computeGuidedPreview(
        selection = selection,
        selectedClass = selectedClass,
        races = races,
        backgrounds = backgrounds,
        traitsById = traitsById,
        featuresById = featuresById,
    )

    fun validate(content: GuidedCharacterSetupUiState.Content): GuidedValidationResult =
        validateGuidedSetupContent(
            content = content,
            pointBuyBudget = POINT_BUY_BUDGET,
        )

    private fun pointBuyTotalCost(scores: Map<AbilityId, Int>): Int = guidedPointBuyTotalCost(scores)

    private fun computeSteps(
        selectedClass: com.github.arhor.spellbindr.domain.model.CharacterClass?,
        featuresById: Map<String, Feature>,
    ): List<GuidedStep> = computeGuidedSetupSteps(
        selectedClass = selectedClass,
        featuresById = featuresById,
    )

    companion object {
        private const val POINT_BUY_BUDGET = 27

        private const val CLASS_CHOICE_PREFIX = "class/"
        private const val FEATURE_CHOICE_PREFIX = "feature/"
        private const val RACE_CHOICE_PREFIX = "race/"
        private const val BACKGROUND_CHOICE_PREFIX = "background/"
        private const val SPELL_CHOICE_PREFIX = "spells/"

        fun classProficiencyChoiceKey(index: Int): String = "${CLASS_CHOICE_PREFIX}proficiency/$index"

        fun featureChoiceKey(featureId: String): String = "${FEATURE_CHOICE_PREFIX}$featureId"

        fun raceTraitAbilityBonusChoiceKey(traitId: String): String = "${RACE_CHOICE_PREFIX}trait/$traitId/abilityBonus"
        fun raceTraitLanguageChoiceKey(traitId: String): String = "${RACE_CHOICE_PREFIX}trait/$traitId/language"
        fun raceTraitProficiencyChoiceKey(traitId: String): String = "${RACE_CHOICE_PREFIX}trait/$traitId/proficiency"
        fun raceTraitSpellChoiceKey(traitId: String): String = "${RACE_CHOICE_PREFIX}trait/$traitId/spell"
        fun raceTraitDraconicAncestryChoiceKey(traitId: String): String =
            "${RACE_CHOICE_PREFIX}trait/$traitId/draconicAncestry"

        fun backgroundLanguageChoiceKey(): String = "${BACKGROUND_CHOICE_PREFIX}language"
        fun backgroundEquipmentChoiceKey(): String = "${BACKGROUND_CHOICE_PREFIX}equipment"

        fun spellCantripsChoiceKey(): String = "${SPELL_CHOICE_PREFIX}cantrips"
        fun spellLevel1ChoiceKey(): String = "${SPELL_CHOICE_PREFIX}level1"
    }
}

@Immutable
data class GuidedSelection(
    val classId: String?,
    val subclassId: String?,
    val raceId: String?,
    val subraceId: String?,
    val backgroundId: String?,
    val abilityMethod: AbilityScoreMethod?,
    val standardArrayAssignments: Map<AbilityId, Int?>,
    val pointBuyScores: Map<AbilityId, Int>,
    val choiceSelections: Map<String, Set<String>>,
)

sealed interface GuidedCharacterSetupUiState {
    data object Loading : GuidedCharacterSetupUiState
    data class Failure(val errorMessage: String) : GuidedCharacterSetupUiState

    @Immutable
    data class Content(
        val step: GuidedStep,
        val steps: List<GuidedStep>,
        val currentStepIndex: Int,
        val totalSteps: Int,
        val name: String,
        val classes: List<com.github.arhor.spellbindr.domain.model.CharacterClass>,
        val races: List<Race>,
        val backgrounds: List<com.github.arhor.spellbindr.domain.model.Background>,
        val languages: List<Language>,
        val equipment: List<Equipment>,
        val traitsById: Map<String, Trait>,
        val featuresById: Map<String, Feature>,
        val languagesById: Map<String, Language>,
        val equipmentById: Map<String, Equipment>,
        val spells: List<Spell>,
        val spellsById: Map<String, Spell>,
        val referenceDataVersion: Int,
        val selection: GuidedSelection,
        val preview: GuidedCharacterPreview,
        val isSaving: Boolean,
        val validation: GuidedValidationResult = GuidedValidationResult(emptyList()),
    ) : GuidedCharacterSetupUiState
}
