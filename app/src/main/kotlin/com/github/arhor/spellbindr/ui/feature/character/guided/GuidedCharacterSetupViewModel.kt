package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.CharacterCreationResult
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
import com.github.arhor.spellbindr.domain.usecase.SaveGuidedCharacterUseCase
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedReferenceData
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedReferenceDataState
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedSpellsData
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceCategory
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceContext
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceRequirement
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceRequirements
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedFixedGrant
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.buildGuidedCharacterCreationResult
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.computeGuidedPreview
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.computeGuidedSetupSteps
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.defaultPointBuyScores
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.defaultStandardArrayAssignments
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.deriveGuidedChoiceRequirements
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.guidedPointBuyTotalCost
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.observeGuidedReferenceDataState
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.observeGuidedSpellsDataState
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.reconcileGuidedChoiceSelections
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.resolveGuidedSetupStep
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
    private val saveGuidedCharacter: SaveGuidedCharacterUseCase,
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
        if (state.step == GuidedStep.ANCESTRY_CHOICES) return true

        val data = referenceData ?: return false
        return deriveChoiceRequirements(
            state = state,
            referenceData = data,
            spells = emptyList(),
        ).requirements.any { requirement ->
            requirement.category == GuidedChoiceCategory.ANCESTRY &&
                requirement.key.endsWith("/spell")
        }
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
                val selection = state.toSelection()
                val choiceRequirements = deriveChoiceRequirements(
                    state = state,
                    referenceData = referenceData,
                    spells = spellsData.spells,
                )

                val steps = computeSteps(
                    selectedClass = selectedClass,
                    featuresById = referenceData.featuresById,
                    choiceRequirements = choiceRequirements,
                )

                val resolvedStep = resolveGuidedSetupStep(state.step, steps)
                val currentIndex = steps.indexOf(resolvedStep).coerceAtLeast(0)

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
                    choiceRequirements = choiceRequirements.requirements,
                    fixedGrants = choiceRequirements.fixedGrants,
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
        updateUpstreamSelection(
            transform = {
                it.copy(
                    classId = classId,
                    subclassId = null,
                )
            },
            preserveClassOwnedSelections = false,
        )
    }

    private fun onSubclassSelected(subclassId: String) {
        _state.update { it.copy(subclassId = subclassId) }
    }

    private fun onRaceSelected(raceId: String) {
        updateUpstreamSelection(
            transform = { state ->
                state.copy(
                    raceId = raceId,
                    subraceId = null,
                )
            },
        )
    }

    private fun onSubraceSelected(subraceId: String) {
        updateUpstreamSelection(
            transform = { it.copy(subraceId = subraceId) },
        )
    }

    private fun onBackgroundSelected(backgroundId: String) {
        updateUpstreamSelection(
            transform = { it.copy(backgroundId = backgroundId) },
        )
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
        val resolved = resolveGuidedSetupStep(step, content.steps)
        _state.update { it.copy(step = resolved) }
    }

    private fun updateUpstreamSelection(
        transform: (State) -> State,
        preserveClassOwnedSelections: Boolean = true,
    ) {
        val referenceData = (referenceDataState.value as? GuidedReferenceDataState.Content)?.data
        _state.update { current ->
            val updated = transform(current)
            if (referenceData == null) {
                updated
            } else {
                val requirements = deriveChoiceRequirements(
                    state = updated,
                    referenceData = referenceData,
                    spells = spellsData.value.spells,
                )
                updated.copy(
                    choiceSelections = reconcileGuidedChoiceSelections(
                        choiceSelections = updated.choiceSelections,
                        choiceRequirements = requirements,
                        additionalActiveKeys = additionalActiveChoiceKeys(
                            state = updated,
                            referenceData = referenceData,
                            includeClassOwnedSelections = preserveClassOwnedSelections,
                        ),
                    ),
                )
            }
        }
    }

    private fun deriveChoiceRequirements(
        state: State,
        referenceData: GuidedReferenceData,
        spells: List<Spell>,
    ): GuidedChoiceRequirements = deriveGuidedChoiceRequirements(
        GuidedChoiceContext(
            selection = state.toSelection(),
            classes = referenceData.classes,
            races = referenceData.races,
            backgrounds = referenceData.backgrounds,
            traitsById = referenceData.traitsById,
            languages = referenceData.languages,
            equipment = referenceData.equipment,
            featuresById = referenceData.featuresById,
            spells = spells,
            referenceDataVersion = referenceData.version,
        ),
    )

    private fun additionalActiveChoiceKeys(
        state: State,
        referenceData: GuidedReferenceData,
        includeClassOwnedSelections: Boolean,
    ): Set<String> {
        val selectedClass = referenceData.classes.firstOrNull { it.id == state.classId } ?: return emptySet()
        return buildSet {
            if (includeClassOwnedSelections) {
                selectedClass.levels
                    .firstOrNull { it.level == 1 }
                    ?.features
                    .orEmpty()
                    .filter { referenceData.featuresById[it]?.choice != null }
                    .forEach { add(featureChoiceKey(it)) }
            }

            if (includeClassOwnedSelections && selectedClass.spellcasting?.level == 1) {
                add(spellCantripsChoiceKey())
                add(spellLevel1ChoiceKey())
            }
        }
    }

    private fun State.toSelection(): GuidedSelection = GuidedSelection(
        classId = classId,
        subclassId = subclassId,
        raceId = raceId,
        subraceId = subraceId,
        backgroundId = backgroundId,
        abilityMethod = abilityMethod,
        standardArrayAssignments = standardArrayAssignments,
        pointBuyScores = pointBuyScores,
        choiceSelections = choiceSelections,
    )

    private fun onCreateCharacter() {
        val content = uiState.value as? GuidedCharacterSetupUiState.Content ?: return
        val validation = validate(content)
        if (validation.hasErrors) return

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val result = buildCharacterCreationResult(content)
                saveGuidedCharacter(result)
                _effects.emit(GuidedCharacterSetupEffect.CharacterCreated(result.sheet.id))
            } catch (t: Throwable) {
                _effects.emit(GuidedCharacterSetupEffect.Error(t.message ?: "Failed to save character."))
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    internal fun buildCharacterCreationResult(
        content: GuidedCharacterSetupUiState.Content,
    ): CharacterCreationResult = buildGuidedCharacterCreationResult(content)

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
        choiceRequirements: GuidedChoiceRequirements,
    ): List<GuidedStep> = computeGuidedSetupSteps(
        selectedClass = selectedClass,
        featuresById = featuresById,
        choiceRequirements = choiceRequirements,
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
        val choiceRequirements: List<GuidedChoiceRequirement> = emptyList(),
        val fixedGrants: List<GuidedFixedGrant> = emptyList(),
        val preview: GuidedCharacterPreview,
        val isSaving: Boolean,
        val validation: GuidedValidationResult = GuidedValidationResult(emptyList()),
    ) : GuidedCharacterSetupUiState
}
