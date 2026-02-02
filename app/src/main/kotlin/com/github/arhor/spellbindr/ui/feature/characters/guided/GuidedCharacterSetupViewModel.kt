package com.github.arhor.spellbindr.ui.feature.characters.guided

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.Character
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSpell
import com.github.arhor.spellbindr.domain.model.CountedEntityRef
import com.github.arhor.spellbindr.domain.model.Effect
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Equipment
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.Language
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.usecase.ObserveAllBackgroundsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllCharacterClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllEquipmentUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllFeaturesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllLanguagesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllRacesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllTraitsUseCase
import com.github.arhor.spellbindr.domain.usecase.SaveCharacterSheetUseCase
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
import java.util.UUID
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
        val selectedSpellIds: Set<String> = emptySet(),

        val isSaving: Boolean = false,
    )

    sealed interface GuidedCharacterSetupEvent {
        data class CharacterCreated(val characterId: String) : GuidedCharacterSetupEvent
        data class Error(val message: String) : GuidedCharacterSetupEvent
    }

    private val _state = MutableStateFlow(State())

    private val _events = MutableSharedFlow<GuidedCharacterSetupEvent>()
    val events: SharedFlow<GuidedCharacterSetupEvent> = _events.asSharedFlow()

    val uiState: StateFlow<GuidedCharacterSetupUiState> = combine(
        _state,
        observeClasses(),
        observeRaces(),
        observeTraits(),
        observeBackgrounds(),
        observeLanguages(),
        observeFeatures(),
        observeEquipment(),
    ) { args ->
        val state = args[0] as State
        val classes = args[1] as Loadable<List<com.github.arhor.spellbindr.domain.model.CharacterClass>>
        val races = args[2] as Loadable<List<Race>>
        val traits = args[3] as Loadable<List<Trait>>
        val backgrounds = args[4] as Loadable<List<com.github.arhor.spellbindr.domain.model.Background>>
        val languages = args[5] as Loadable<List<Language>>
        val features = args[6] as Loadable<List<Feature>>
        val equipment = args[7] as Loadable<List<Equipment>>

        val firstFailure = listOf(classes, races, traits, backgrounds, languages, features, equipment)
            .filterIsInstance<Loadable.Failure>()
            .firstOrNull()
        if (firstFailure != null) {
            return@combine GuidedCharacterSetupUiState.Failure(
                firstFailure.errorMessage ?: "Failed to load data.",
            )
        }

        val allReady = listOf(classes, races, traits, backgrounds, languages, features, equipment)
            .all { it is Loadable.Content }
        if (!allReady) {
            return@combine GuidedCharacterSetupUiState.Loading
        }

        val classesContent = (classes as Loadable.Content).data
        val racesContent = (races as Loadable.Content).data
        val traitsContent = (traits as Loadable.Content).data
        val backgroundsContent = (backgrounds as Loadable.Content).data
        val languagesContent = (languages as Loadable.Content).data
        val featuresContent = (features as Loadable.Content).data
        val equipmentContent = (equipment as Loadable.Content).data

        val traitsById = traitsContent.associateBy(Trait::id)
        val featuresById = featuresContent.associateBy(Feature::id)
        val languagesById = languagesContent.associateBy(Language::id)
        val equipmentById = equipmentContent.associateBy(Equipment::id)

        val selectedClass = state.classId?.let { id -> classesContent.firstOrNull { it.id == id } }

        val steps = computeSteps(
            state = state,
            selectedClass = selectedClass,
            featuresById = featuresById,
        )
        val currentIndex = steps.indexOf(state.step).coerceAtLeast(0)

        GuidedCharacterSetupUiState.Content(
            step = state.step.takeIf { it in steps } ?: steps.first(),
            steps = steps,
            currentStepIndex = currentIndex,
            totalSteps = steps.size,
            name = state.name,
            classes = classesContent,
            races = racesContent,
            backgrounds = backgroundsContent,
            languages = languagesContent,
            equipment = equipmentContent,
            traitsById = traitsById,
            featuresById = featuresById,
            languagesById = languagesById,
            equipmentById = equipmentById,
            selection = GuidedSelection(
                classId = state.classId,
                subclassId = state.subclassId,
                raceId = state.raceId,
                subraceId = state.subraceId,
                backgroundId = state.backgroundId,
                abilityMethod = state.abilityMethod,
                standardArrayAssignments = state.standardArrayAssignments,
                pointBuyScores = state.pointBuyScores,
                choiceSelections = state.choiceSelections,
                selectedSpellIds = state.selectedSpellIds,
            ),
            isSaving = state.isSaving,
        )
    }.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        GuidedCharacterSetupUiState.Loading
    )

    fun onNameChanged(value: String) {
        _state.update { it.copy(name = value) }
    }

    fun onClassSelected(classId: String) {
        _state.update {
            it.copy(
                classId = classId,
                subclassId = null,
                choiceSelections = it.choiceSelections.filterKeys { key ->
                    !key.startsWith(CLASS_CHOICE_PREFIX) && !key.startsWith(FEATURE_CHOICE_PREFIX)
                },
            )
        }
    }

    fun onSubclassSelected(subclassId: String) {
        _state.update { it.copy(subclassId = subclassId) }
    }

    fun onRaceSelected(raceId: String) {
        _state.update {
            it.copy(
                raceId = raceId,
                subraceId = null,
                choiceSelections = it.choiceSelections.filterKeys { key -> !key.startsWith(RACE_CHOICE_PREFIX) },
            )
        }
    }

    fun onSubraceSelected(subraceId: String) {
        _state.update { it.copy(subraceId = subraceId) }
    }

    fun onBackgroundSelected(backgroundId: String) {
        _state.update {
            it.copy(
                backgroundId = backgroundId,
                choiceSelections = it.choiceSelections.filterKeys { key -> !key.startsWith(BACKGROUND_CHOICE_PREFIX) },
            )
        }
    }

    fun onAbilityMethodSelected(method: AbilityScoreMethod) {
        _state.update {
            it.copy(
                abilityMethod = method,
                standardArrayAssignments = defaultStandardArrayAssignments(),
                pointBuyScores = defaultPointBuyScores(),
            )
        }
    }

    fun onStandardArrayAssigned(abilityId: AbilityId, score: Int?) {
        _state.update { state ->
            state.copy(
                standardArrayAssignments = state.standardArrayAssignments.toMutableMap().apply {
                    this[abilityId] = score
                },
            )
        }
    }

    fun onPointBuyIncrement(abilityId: AbilityId) {
        _state.update { state ->
            val current = state.pointBuyScores[abilityId] ?: 8
            val next = (current + 1).coerceAtMost(15)
            if (next == current) return@update state

            val nextScores = state.pointBuyScores.toMutableMap().apply { put(abilityId, next) }.toMap()
            if (pointBuyTotalCost(nextScores) > POINT_BUY_BUDGET) return@update state

            state.copy(pointBuyScores = nextScores)
        }
    }

    fun onPointBuyDecrement(abilityId: AbilityId) {
        _state.update { state ->
            val current = state.pointBuyScores[abilityId] ?: 8
            val next = (current - 1).coerceAtLeast(8)
            if (next == current) return@update state

            state.copy(pointBuyScores = state.pointBuyScores.toMutableMap().apply { put(abilityId, next) }.toMap())
        }
    }

    fun onChoiceToggled(key: String, optionId: String, maxSelected: Int) {
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

    fun onSpellToggled(spellId: String) {
        _state.update { state ->
            state.copy(
                selectedSpellIds = if (spellId in state.selectedSpellIds) {
                    state.selectedSpellIds - spellId
                } else {
                    state.selectedSpellIds + spellId
                },
            )
        }
    }

    fun onNext() {
        val content = uiState.value as? GuidedCharacterSetupUiState.Content ?: return
        val steps = content.steps
        val index = steps.indexOf(content.step)
        if (index < 0) return
        val next = steps.getOrNull(index + 1) ?: return
        _state.update { it.copy(step = next) }
    }

    fun onBack() {
        val content = uiState.value as? GuidedCharacterSetupUiState.Content ?: return
        val steps = content.steps
        val index = steps.indexOf(content.step)
        if (index <= 0) return
        val prev = steps[index - 1]
        _state.update { it.copy(step = prev) }
    }

    fun onCreateCharacter() {
        val content = uiState.value as? GuidedCharacterSetupUiState.Content ?: return
        val validation = validate(content)
        if (validation.hasErrors) return

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val sheet = buildCharacterSheet(content)
                saveCharacterSheet(sheet)
                _events.emit(GuidedCharacterSetupEvent.CharacterCreated(sheet.id))
            } catch (t: Throwable) {
                _events.emit(GuidedCharacterSetupEvent.Error(t.message ?: "Failed to save character."))
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun buildCharacterSheet(content: GuidedCharacterSetupUiState.Content): CharacterSheet {
        val selection = content.selection
        val clazz = content.classes.firstOrNull { it.id == selection.classId }
        val race = content.races.firstOrNull { it.id == selection.raceId }
        val background = content.backgrounds.firstOrNull { it.id == selection.backgroundId }

        val baseAbilityScores = resolveBaseAbilityScores(selection) ?: AbilityIds.standardOrder.associateWith { 10 }
        val effects = buildAllEffects(content)

        val computed =
            effects.fold(Character.State(level = 1, abilityScores = baseAbilityScores.toEntityRefMap())) { s, e ->
                e.applyTo(s)
            }
        val finalAbilityScores = computed.abilityScores.toAbilityScores()

        val proficiencyBonus = 2
        val conMod = finalAbilityScores.modifierFor(AbilityIds.CON)
        val dexMod = finalAbilityScores.modifierFor(AbilityIds.DEX)
        val maxHp = ((clazz?.hitDie ?: 8) + conMod).coerceAtLeast(1)

        val raceName = buildString {
            append(race?.name.orEmpty())
            val subraceName = selection.subraceId?.let { id ->
                race?.subraces?.firstOrNull { it.id == id }?.name
            }
            if (!subraceName.isNullOrBlank()) {
                append(" (")
                append(subraceName)
                append(')')
            }
        }.trim()

        val proficiencies = computed.proficiencies.map(EntityRef::prettyString).sorted().joinToString(", ")
        val languages = computed.languages.map { ref ->
            content.languagesById[ref.id]?.name ?: ref.prettyString()
        }.sorted().joinToString(", ")
        val equipmentText = computed.inventory.entries
            .sortedBy { it.key.id }
            .joinToString(separator = "\n") { (ref, count) ->
                val name = content.equipmentById[ref.id]?.name ?: ref.prettyString()
                if (count <= 1) name else "$name x$count"
            }

        val savingThrows = AbilityIds.standardOrder.map { abilityId ->
            val proficient = clazz?.savingThrows?.any { it.equals(abilityId, ignoreCase = true) } == true
            val bonus = finalAbilityScores.modifierFor(abilityId) + if (proficient) proficiencyBonus else 0
            com.github.arhor.spellbindr.domain.model.SavingThrowEntry(
                abilityId = abilityId,
                bonus = bonus,
                proficient = proficient,
            )
        }
        val skillProficiencies = computed.proficiencies.mapNotNull(::skillFromProficiencyId).toSet()
        val skills = Skill.entries.map { skill ->
            val proficient = skill in skillProficiencies
            val bonus = finalAbilityScores.modifierFor(skill.abilityId) + if (proficient) proficiencyBonus else 0
            com.github.arhor.spellbindr.domain.model.SkillEntry(
                skill = skill,
                bonus = bonus,
                proficient = proficient,
                expertise = false,
            )
        }

        val characterSpells = selection.selectedSpellIds.map { spellId ->
            CharacterSpell(spellId = spellId, sourceClass = clazz?.name.orEmpty())
        }

        return CharacterSheet(
            id = UUID.randomUUID().toString(),
            name = content.name.trim(),
            level = 1,
            className = clazz?.name.orEmpty(),
            race = raceName,
            background = background?.name.orEmpty(),
            abilityScores = finalAbilityScores,
            proficiencyBonus = proficiencyBonus,
            maxHitPoints = maxHp,
            currentHitPoints = maxHp,
            armorClass = 10 + dexMod,
            initiative = dexMod,
            speed = "${computed.speed} ft",
            hitDice = "1d${clazz?.hitDie ?: 8}",
            savingThrows = savingThrows,
            skills = skills,
            languages = languages,
            proficiencies = proficiencies,
            equipment = equipmentText,
            characterSpells = characterSpells,
        )
    }

    private fun resolveBaseAbilityScores(selection: GuidedSelection): Map<AbilityId, Int>? =
        when (selection.abilityMethod) {
            AbilityScoreMethod.STANDARD_ARRAY -> {
                if (!isStandardArrayValid(selection.standardArrayAssignments)) return null
                selection.standardArrayAssignments.mapValues { it.value ?: 10 }
            }

            AbilityScoreMethod.POINT_BUY -> selection.pointBuyScores

            null -> null
        }

    private fun buildAllEffects(content: GuidedCharacterSetupUiState.Content): List<Effect> {
        val selection = content.selection
        val effects = mutableListOf<Effect>()

        val clazz = content.classes.firstOrNull { it.id == selection.classId }
        if (clazz != null) {
            effects += Effect.AddProficienciesEffect(clazz.proficiencies.toSet())
        }
        if (clazz?.startingEquipment != null) {
            effects += Effect.AddEquipmentEffect(
                clazz.startingEquipment.map { CountedEntityRef(it.id, it.quantity) },
            )
        }

        val background = content.backgrounds.firstOrNull { it.id == selection.backgroundId }
        if (background != null) {
            effects += background.effects
            val langChoice = background.languageChoice
            if (langChoice != null) {
                val selected = selection.choiceSelections[backgroundLanguageChoiceKey()].orEmpty()
                if (selected.isNotEmpty()) {
                    effects += Effect.AddLanguagesEffect(selected)
                }
            }
            val equipmentChoice = background.equipmentChoice
            if (equipmentChoice != null) {
                val selected = selection.choiceSelections[backgroundEquipmentChoiceKey()].orEmpty()
                if (selected.isNotEmpty()) {
                    effects += Effect.AddEquipmentEffect(
                        selected.map { CountedEntityRef(it, 1) },
                    )
                }
            }
        }

        val race = content.races.firstOrNull { it.id == selection.raceId }
        if (race != null) {
            val traitIds = buildList {
                addAll(race.traits.map { it.id })
                val subrace = selection.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
                if (subrace != null) {
                    addAll(subrace.traits.map { it.id })
                }
            }
            val traits = traitIds.mapNotNull { content.traitsById[it] }
            traits.forEach { trait ->
                trait.effects?.let(effects::addAll)

                trait.abilityBonusChoice?.let { choice ->
                    val selected = selection.choiceSelections[raceTraitAbilityBonusChoiceKey(trait.id)].orEmpty()
                    if (selected.isNotEmpty()) {
                        val map = selected.associateWith { 1 }
                        effects += Effect.ModifyAbilityEffect(map)
                    }
                }
                trait.languageChoice?.let {
                    val selected = selection.choiceSelections[raceTraitLanguageChoiceKey(trait.id)].orEmpty()
                    if (selected.isNotEmpty()) {
                        effects += Effect.AddLanguagesEffect(selected)
                    }
                }
                trait.proficiencyChoice?.let {
                    val selected = selection.choiceSelections[raceTraitProficiencyChoiceKey(trait.id)].orEmpty()
                    if (selected.isNotEmpty()) {
                        effects += Effect.AddProficienciesEffect(selected)
                    }
                }
            }
        }

        // Class proficiency choices (mostly skills).
        if (clazz != null) {
            clazz.proficiencyChoices.forEachIndexed { index, choice ->
                val selected = selection.choiceSelections[classProficiencyChoiceKey(index)].orEmpty()
                if (selected.isNotEmpty()) {
                    effects += Effect.AddProficienciesEffect(selected)
                }
            }
        }

        // Feature choices (e.g. Fighting Style, Ranger choices, Rogue expertise, etc.).
        val featureChoices = findLevelOneFeatureChoices(content)
        featureChoices.forEach { (featureId, choice) ->
            val selected = selection.choiceSelections[featureChoiceKey(featureId)].orEmpty()
            if (selected.isNotEmpty()) {
                when (choice) {
                    is com.github.arhor.spellbindr.domain.model.Choice.ProficiencyChoice ->
                        effects += Effect.AddProficienciesEffect(selected)

                    else -> Unit
                }
            }
        }

        return effects
    }

    private fun findLevelOneFeatureChoices(
        content: GuidedCharacterSetupUiState.Content,
    ): List<Pair<String, com.github.arhor.spellbindr.domain.model.Choice>> {
        val clazz =
            content.selection.classId?.let { id -> content.classes.firstOrNull { it.id == id } } ?: return emptyList()
        val level1Features = clazz.levels.firstOrNull { it.level == 1 }?.features.orEmpty()
        return level1Features.mapNotNull { featureId ->
            val choice = content.featuresById[featureId]?.choice ?: return@mapNotNull null
            featureId to choice
        }
    }

    fun validate(content: GuidedCharacterSetupUiState.Content): GuidedValidationResult {
        val issues = mutableListOf<GuidedValidationIssue>()

        if (content.selection.classId == null) issues += err("Choose a class.")
        if (content.selection.raceId == null) issues += err("Choose a race.")
        if (content.selection.backgroundId == null) issues += err("Choose a background.")

        when (content.selection.abilityMethod) {
            null -> issues += err("Choose an ability score method.")
            AbilityScoreMethod.STANDARD_ARRAY -> if (!isStandardArrayValid(content.selection.standardArrayAssignments)) {
                issues += err("Assign all ability scores using the standard array (15, 14, 13, 12, 10, 8).")
            }

            AbilityScoreMethod.POINT_BUY -> if (pointBuyTotalCost(content.selection.pointBuyScores) > POINT_BUY_BUDGET) {
                issues += err("Point buy exceeds 27 points.")
            }
        }

        val background = content.selection.backgroundId?.let { id -> content.backgrounds.firstOrNull { it.id == id } }
        val bgLangChoice = background?.languageChoice
        if (bgLangChoice != null) {
            val selected = content.selection.choiceSelections[backgroundLanguageChoiceKey()].orEmpty()
            if (selected.size != bgLangChoice.choose) {
                issues += err("Select ${bgLangChoice.choose} background language(s).")
            }
        }

        val bgEquipChoice = background?.equipmentChoice
        if (bgEquipChoice != null) {
            val selected = content.selection.choiceSelections[backgroundEquipmentChoiceKey()].orEmpty()
            if (selected.size != bgEquipChoice.choose) {
                issues += err("Select ${bgEquipChoice.choose} background equipment item(s).")
            }
        }

        val race = content.selection.raceId?.let { id -> content.races.firstOrNull { it.id == id } }
        if (race != null) {
            val traitIds = buildList {
                addAll(race.traits.map { it.id })
                val subrace = content.selection.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
                if (subrace != null) {
                    addAll(subrace.traits.map { it.id })
                }
            }
            traitIds.mapNotNull { content.traitsById[it] }.forEach { trait ->
                trait.abilityBonusChoice?.let { choice ->
                    val selected =
                        content.selection.choiceSelections[raceTraitAbilityBonusChoiceKey(trait.id)].orEmpty()
                    if (selected.size != choice.choose) {
                        issues += err("Select ${choice.choose} race ability bonus option(s).")
                    }
                }
                trait.languageChoice?.let { choice ->
                    val selected = content.selection.choiceSelections[raceTraitLanguageChoiceKey(trait.id)].orEmpty()
                    if (selected.size != choice.choose) {
                        issues += err("Select ${choice.choose} race language option(s).")
                    }
                }
                trait.proficiencyChoice?.let { choice ->
                    val selected = content.selection.choiceSelections[raceTraitProficiencyChoiceKey(trait.id)].orEmpty()
                    if (selected.size != choice.choose) {
                        issues += err("Select ${choice.choose} race proficiency option(s).")
                    }
                }
            }
        }

        val clazz = content.selection.classId?.let { id -> content.classes.firstOrNull { it.id == id } }
        if (clazz != null) {
            val requiresSubclass = clazz.requiresLevelOneSubclass()
            if (requiresSubclass && content.selection.subclassId == null) {
                issues += err("Choose a subclass.")
            }

            clazz.proficiencyChoices.forEachIndexed { index, choice ->
                val selected = content.selection.choiceSelections[classProficiencyChoiceKey(index)].orEmpty()
                if (selected.size != choice.choose) {
                    issues += err("Select ${choice.choose} class proficiency option(s).")
                }
            }

            findLevelOneFeatureChoices(content).forEach { (featureId, choice) ->
                val selected = content.selection.choiceSelections[featureChoiceKey(featureId)].orEmpty()
                if (selected.size != choice.choose) {
                    issues += err("Select ${choice.choose} option(s) for ${content.featuresById[featureId]?.name ?: featureId}.")
                }
            }
        }

        if (content.name.isBlank()) {
            issues += warn("Name is empty (you can set it later).")
        }

        return GuidedValidationResult(issues = issues)
    }

    private fun isStandardArrayValid(assignments: Map<AbilityId, Int?>): Boolean {
        val values = assignments.values.filterNotNull()
        return values.size == AbilityIds.standardOrder.size && values.sorted() == StandardArray.sorted()
    }

    private fun pointBuyTotalCost(scores: Map<AbilityId, Int>): Int =
        scores.values.sumOf(::pointBuyCost)

    private fun computeSteps(
        state: State,
        selectedClass: com.github.arhor.spellbindr.domain.model.CharacterClass?,
        featuresById: Map<String, Feature>,
    ): List<GuidedStep> {
        val steps = mutableListOf(
            GuidedStep.BASICS,
            GuidedStep.CLASS,
        )

        val classChoicesNeeded = selectedClass?.let { clazz ->
            val requiresSubclass = clazz.requiresLevelOneSubclass()
            val level1FeatureChoiceCount = clazz.levels
                .firstOrNull { it.level == 1 }
                ?.features
                .orEmpty()
                .count { featuresById[it]?.choice != null }
            requiresSubclass || level1FeatureChoiceCount > 0
        } == true

        if (classChoicesNeeded) steps += GuidedStep.CLASS_CHOICES

        steps += listOf(
            GuidedStep.RACE,
            GuidedStep.BACKGROUND,
            GuidedStep.ABILITY_METHOD,
            GuidedStep.ABILITY_ASSIGN,
            GuidedStep.SKILLS_PROFICIENCIES,
            GuidedStep.EQUIPMENT,
        )

        val spellsStepNeeded = selectedClass?.spellcasting?.level == 1
        if (spellsStepNeeded) steps += GuidedStep.SPELLS

        steps += GuidedStep.REVIEW

        return steps
    }

    private fun com.github.arhor.spellbindr.domain.model.CharacterClass.requiresLevelOneSubclass(): Boolean {
        // SRD/PHB 2014: cleric, sorcerer, warlock pick subclass at level 1.
        // Keep this explicit for MVP (data doesn't model "subclass level" directly).
        return id in setOf("cleric", "sorcerer", "warlock")
    }

    private fun Map<AbilityId, Int>.toEntityRefMap(): Map<EntityRef, Int> =
        entries.associate { (abilityId, score) -> EntityRef(abilityId) to score }

    private fun Map<EntityRef, Int>.toAbilityScores(): com.github.arhor.spellbindr.domain.model.AbilityScores =
        com.github.arhor.spellbindr.domain.model.AbilityScores(
            strength = this[EntityRef(AbilityIds.STR)] ?: 10,
            dexterity = this[EntityRef(AbilityIds.DEX)] ?: 10,
            constitution = this[EntityRef(AbilityIds.CON)] ?: 10,
            intelligence = this[EntityRef(AbilityIds.INT)] ?: 10,
            wisdom = this[EntityRef(AbilityIds.WIS)] ?: 10,
            charisma = this[EntityRef(AbilityIds.CHA)] ?: 10,
        )

    private fun skillFromProficiencyId(id: EntityRef): Skill? = skillFromProficiencyId(id.id)

    private fun skillFromProficiencyId(id: String): Skill? {
        val normalized = id.removePrefix("skill-")
            .replace("-", "_")
            .uppercase()
        return Skill.entries.firstOrNull { it.name == normalized }
    }

    private fun err(message: String) =
        GuidedValidationIssue(GuidedValidationIssue.Severity.ERROR, message)

    private fun warn(message: String) =
        GuidedValidationIssue(GuidedValidationIssue.Severity.WARNING, message)

    companion object {
        private const val POINT_BUY_BUDGET = 27

        private const val CLASS_CHOICE_PREFIX = "class/"
        private const val FEATURE_CHOICE_PREFIX = "feature/"
        private const val RACE_CHOICE_PREFIX = "race/"
        private const val BACKGROUND_CHOICE_PREFIX = "background/"

        fun classProficiencyChoiceKey(index: Int): String = "${CLASS_CHOICE_PREFIX}proficiency/$index"

        fun featureChoiceKey(featureId: String): String = "${FEATURE_CHOICE_PREFIX}$featureId"

        fun raceTraitAbilityBonusChoiceKey(traitId: String): String = "${RACE_CHOICE_PREFIX}trait/$traitId/abilityBonus"
        fun raceTraitLanguageChoiceKey(traitId: String): String = "${RACE_CHOICE_PREFIX}trait/$traitId/language"
        fun raceTraitProficiencyChoiceKey(traitId: String): String = "${RACE_CHOICE_PREFIX}trait/$traitId/proficiency"

        fun backgroundLanguageChoiceKey(): String = "${BACKGROUND_CHOICE_PREFIX}language"
        fun backgroundEquipmentChoiceKey(): String = "${BACKGROUND_CHOICE_PREFIX}equipment"
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
    val selectedSpellIds: Set<String>,
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
        val selection: GuidedSelection,
        val isSaving: Boolean,
    ) : GuidedCharacterSetupUiState
}
