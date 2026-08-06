package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.ApplyLevelUpResult
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterWithProgression
import com.github.arhor.spellbindr.domain.model.Feat
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.Language
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceCategory
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceRules
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.ProgressionState
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.github.arhor.spellbindr.domain.usecase.ApplyLevelUpUseCase
import com.github.arhor.spellbindr.domain.usecase.CreateLevelUpPlanUseCase
import com.github.arhor.spellbindr.domain.usecase.LoadCharacterWithProgressionUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllCharacterClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllFeatsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllFeaturesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllLanguagesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.RebuildLevelUpPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class CharacterLevelUpViewModel @Inject constructor(
    loadCharacter: LoadCharacterWithProgressionUseCase,
    observeClasses: ObserveAllCharacterClassesUseCase,
    observeFeatures: ObserveAllFeaturesUseCase,
    observeFeats: ObserveAllFeatsUseCase,
    observeSpells: ObserveAllSpellsUseCase,
    observeLanguages: ObserveAllLanguagesUseCase,
    private val createPlan: CreateLevelUpPlanUseCase,
    private val rebuildPlan: RebuildLevelUpPlanUseCase,
    private val applyLevelUp: ApplyLevelUpUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val characterId: String? = savedStateHandle["characterId"]
    private val draft = MutableStateFlow(restoredDraft())
    private var latestReady: SourceState.Ready? = null
    private var confirmationInFlight = false
    private val _effects = MutableSharedFlow<CharacterLevelUpEffect>()
    val effects = _effects.asSharedFlow()

    private data class ReferenceState(
        val classes: List<CharacterClass>,
        val features: List<com.github.arhor.spellbindr.domain.model.Feature>,
        val feats: List<Feat>,
        val spells: List<Spell>,
        val languages: List<Language>,
    )

    private data class LoadedReferences(
        val classes: Loadable<List<CharacterClass>>,
        val features: Loadable<List<com.github.arhor.spellbindr.domain.model.Feature>>,
        val feats: Loadable<List<Feat>>,
        val spells: Loadable<List<Spell>>,
        val languages: Loadable<List<Language>>,
    )

    private val references = combine(
        observeClasses(),
        observeFeatures(),
        observeFeats(),
        observeSpells(),
        observeLanguages(),
        ::LoadedReferences,
    )

    private val source = if (characterId == null) {
        kotlinx.coroutines.flow.flowOf<SourceState>(SourceState.Failure("Missing character id"))
    } else {
        combine(loadCharacter(characterId), references) { character, references ->
            val classes = references.classes
            val features = references.features
            val feats = references.feats
            val spells = references.spells
            val languages = references.languages
            when {
                character == null -> SourceState.Failure("Character not found")
                classes is Loadable.Failure || features is Loadable.Failure ||
                    feats is Loadable.Failure || spells is Loadable.Failure || languages is Loadable.Failure ->
                    SourceState.Failure("Unable to load level-up reference data")
                classes is Loadable.Content && features is Loadable.Content &&
                    feats is Loadable.Content && spells is Loadable.Content && languages is Loadable.Content -> SourceState.Ready(
                    character,
                    ReferenceState(classes.data, features.data, feats.data, spells.data, languages.data),
                )
                else -> SourceState.Loading
            }
        }
    }

    val uiState = combine(source, draft) { source, draft -> render(source, draft) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CharacterLevelUpUiState.Loading)

    fun dispatch(intent: CharacterLevelUpIntent) {
        when (intent) {
            is CharacterLevelUpIntent.ClassSelected -> updatePlan { it.copy(selectedClassId = intent.classId, selections = it.selections.copy(subclassId = null, featureChoices = emptyMap(), proficiencyChoices = emptyMap(), hitPointGain = null, abilityScoreDecision = null, featChoices = emptyMap(), spellChanges = SpellChanges())) }
            is CharacterLevelUpIntent.SubclassSelected -> updatePlan { it.copy(selections = it.selections.copy(subclassId = intent.subclassId)) }
            is CharacterLevelUpIntent.ChoiceToggled -> toggleChoice(intent)
            is CharacterLevelUpIntent.HitPointsSelected -> updatePlan { it.copy(selections = it.selections.copy(hitPointGain = intent.gain)) }
            CharacterLevelUpIntent.HitPointsCleared -> updatePlan {
                it.copy(selections = it.selections.copy(hitPointGain = null))
            }
            is CharacterLevelUpIntent.AbilityScoreDecisionSelected -> updatePlan { it.copy(selections = it.selections.copy(abilityScoreDecision = intent.decision, featChoices = emptyMap())) }
            is CharacterLevelUpIntent.SpellChangesSelected -> updatePlan { it.applySpellChangesSelection(intent) }
            is CharacterLevelUpIntent.AcknowledgementChanged -> updatePlan { plan ->
                val codes = plan.selections.acknowledgedIssueCodes.toMutableSet().apply {
                    if (intent.acknowledged) add(intent.issueCode) else remove(intent.issueCode)
                }
                plan.copy(selections = plan.selections.copy(acknowledgedIssueCodes = codes))
            }
            CharacterLevelUpIntent.NextClicked -> move(1)
            CharacterLevelUpIntent.BackClicked -> move(-1)
            CharacterLevelUpIntent.CancelClicked -> cancel()
            CharacterLevelUpIntent.ConfirmClicked -> confirm()
            CharacterLevelUpIntent.ReloadClicked -> reloadDraft()
        }
    }

    private fun render(source: SourceState, draft: SavedDraft?): CharacterLevelUpUiState {
        latestReady = source as? SourceState.Ready
        return when (source) {
        SourceState.Loading -> CharacterLevelUpUiState.Loading
        is SourceState.Failure -> CharacterLevelUpUiState.Failure(source.message)
        is SourceState.Ready -> {
            val managed = source.character.progressionState as? ProgressionState.Managed
                ?: return CharacterLevelUpUiState.Unavailable("Set up level progression", "Reconciliation is not yet available for this character.")
            val progression = managed.progression
            if (progression.totalLevel >= LevelUpReferenceRules.maximumCharacterLevel) {
                return CharacterLevelUpUiState.Unavailable("Maximum level reached", "This character is already level 20.")
            }
            val reference = LevelUpReferenceData(
                classes = source.reference.classes,
                features = source.reference.features,
                feats = source.reference.feats,
                referenceDataVersion = LevelUpReferenceRules.referenceDataVersion,
                spells = source.reference.spells,
                languages = source.reference.languages,
            )
            val restoredDraft = draft?.takeIf {
                it.plan.expectedTotalLevel == progression.totalLevel &&
                    it.plan.rulesetId == progression.rulesetId &&
                    it.plan.referenceDataVersion == reference.referenceDataVersion
            }
            val plan = restoredDraft?.plan ?: createPlan(progression)
            if (restoredDraft == null && draft != null) clearSavedDraft()
            val preview = rebuildPlan(source.character.sheet, progression, plan, reference)
            val steps = characterLevelUpSteps(preview.requirements)
            val requested = restoredDraft?.step ?: CharacterLevelUpStep.Class
            val step = requested.takeIf(steps::contains) ?: steps.first()
            CharacterLevelUpUiState.Content(
                source.character.sheet.name,
                plan,
                preview,
                source.reference.classes,
                source.reference.feats,
                source.reference.spells,
                steps,
                step,
                steps.indexOf(step),
                restoredDraft?.isSaving == true,
                restoredDraft?.staleMessage,
                restoredDraft?.persistenceMessage,
            )
        }
        }
    }

    private fun updatePlan(transform: (LevelUpPlan) -> LevelUpPlan) {
        if (confirmationInFlight) return
        val content = uiState.value as? CharacterLevelUpUiState.Content ?: return
        if (content.isSaving) return
        setDraft(SavedDraft(content.step, transform(content.plan)))
    }

    private fun toggleChoice(intent: CharacterLevelUpIntent.ChoiceToggled) {
        val content = uiState.value as? CharacterLevelUpUiState.Content ?: return
        val requirement = content.preview.requirements.filterIsInstance<LevelUpRequirement.ChoiceSelection>().firstOrNull { it.id == intent.requirementId } ?: return
        updatePlan { plan ->
            val target = when (requirement.category) {
                LevelUpChoiceCategory.Feature -> plan.selections.featureChoices
                LevelUpChoiceCategory.Proficiency -> plan.selections.proficiencyChoices
                LevelUpChoiceCategory.Feat -> plan.selections.featChoices
            }.toMutableMap()
            val selected = target[intent.requirementId].orEmpty().toMutableSet()
            if (!selected.remove(intent.optionId)) {
                if (selected.size >= intent.maximum) selected.remove(selected.first())
                selected.add(intent.optionId)
            }
            target[intent.requirementId] = selected
            val selections = when (requirement.category) {
                LevelUpChoiceCategory.Feature -> plan.selections.copy(featureChoices = target)
                LevelUpChoiceCategory.Proficiency -> plan.selections.copy(proficiencyChoices = target)
                LevelUpChoiceCategory.Feat -> plan.selections.copy(featChoices = target)
            }
            plan.copy(selections = selections)
        }
    }

    private fun move(delta: Int) {
        if (confirmationInFlight) return
        val content = uiState.value as? CharacterLevelUpUiState.Content ?: return
        if (content.isSaving) return
        if (delta > 0 && !content.canAdvance) return
        val next = (content.currentStepIndex + delta).coerceIn(0, content.steps.lastIndex)
        setDraft(SavedDraft(content.steps[next], content.plan))
    }

    private fun reloadDraft() {
        if (confirmationInFlight) return
        clearSavedDraft()
        draft.value = null
    }

    private fun cancel() {
        if (confirmationInFlight) return
        viewModelScope.launch { _effects.emit(CharacterLevelUpEffect.Cancelled) }
    }

    private fun confirm() {
        val content = uiState.value as? CharacterLevelUpUiState.Content ?: return
        if (!content.canConfirm || characterId == null || confirmationInFlight) return
        val ready = latestReady ?: return
        val reference = LevelUpReferenceData(
            classes = ready.reference.classes,
            features = ready.reference.features,
            feats = ready.reference.feats,
            referenceDataVersion = LevelUpReferenceRules.referenceDataVersion,
            spells = ready.reference.spells,
            languages = ready.reference.languages,
        )
        confirmationInFlight = true
        setDraft(SavedDraft(content.step, content.plan, isSaving = true))
        viewModelScope.launch {
            when (val result = applyLevelUp(characterId, content.plan.expectedTotalLevel, content.plan, reference)) {
                is ApplyLevelUpResult.Success -> { clearSavedDraft(); _effects.emit(CharacterLevelUpEffect.Completed) }
                ApplyLevelUpResult.StaleState -> retainDraftAfterFailure(
                    content.plan,
                    staleMessage = "The character changed. Reload this draft before confirming.",
                )
                is ApplyLevelUpResult.PersistenceFailure -> retainDraftAfterFailure(
                    content.plan,
                    persistenceMessage = result.message.ifBlank {
                        "Unable to save. Your reviewed choices are ready to retry."
                    },
                )
                is ApplyLevelUpResult.ValidationFailure -> retainDraftAfterFailure(
                    content.plan,
                    persistenceMessage = result.issues.joinToString("\n") { it.message },
                )
                ApplyLevelUpResult.MissingCharacter -> retainDraftAfterFailure(
                    content.plan,
                    persistenceMessage = "Character not found",
                )
                ApplyLevelUpResult.UnmanagedCharacter -> retainDraftAfterFailure(
                    content.plan,
                    persistenceMessage = "This character is not managed",
                )
            }
        }
    }

    private fun retainDraftAfterFailure(
        plan: LevelUpPlan,
        staleMessage: String? = null,
        persistenceMessage: String? = null,
    ) {
        confirmationInFlight = false
        setDraft(SavedDraft(
            step = CharacterLevelUpStep.Review,
            plan = plan,
            staleMessage = staleMessage,
            persistenceMessage = persistenceMessage,
        ))
    }

    private fun setDraft(value: SavedDraft) { draft.value = value; savedStateHandle[DRAFT_KEY] = JSON.encodeToString(SavedDraft.serializer(), value) }
    private fun clearSavedDraft() { savedStateHandle.remove<String>(DRAFT_KEY) }
    private fun restoredDraft(): SavedDraft? = savedStateHandle.get<String>(DRAFT_KEY)?.let {
        runCatching { JSON.decodeFromString(SavedDraft.serializer(), it) }
            .getOrNull()
            ?.copy(isSaving = false)
    }

    @Serializable
    private data class SavedDraft(val step: CharacterLevelUpStep, val plan: LevelUpPlan, val isSaving: Boolean = false, val staleMessage: String? = null, val persistenceMessage: String? = null)
    private sealed interface SourceState { data object Loading : SourceState; data class Failure(val message: String) : SourceState; data class Ready(val character: CharacterWithProgression, val reference: ReferenceState) : SourceState }
    companion object { private const val DRAFT_KEY = "character-level-up-draft"; private val JSON = Json { ignoreUnknownKeys = true } }
}
