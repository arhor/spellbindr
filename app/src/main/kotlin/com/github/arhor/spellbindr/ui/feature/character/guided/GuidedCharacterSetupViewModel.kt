package com.github.arhor.spellbindr.ui.feature.character.guided

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
import com.github.arhor.spellbindr.domain.model.PactSlotState
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.SpellSlotState
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.model.displayName
import com.github.arhor.spellbindr.domain.usecase.ObserveAllBackgroundsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllCharacterClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllEquipmentUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllFeaturesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllLanguagesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllRacesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllTraitsUseCase
import com.github.arhor.spellbindr.domain.usecase.SaveCharacterSheetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
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

    sealed interface GuidedCharacterSetupEvent {
        data class CharacterCreated(val characterId: String) : GuidedCharacterSetupEvent
        data class Error(val message: String) : GuidedCharacterSetupEvent
    }

    private val _state = MutableStateFlow(State())

    private val _events = MutableSharedFlow<GuidedCharacterSetupEvent>()
    val events: SharedFlow<GuidedCharacterSetupEvent> = _events.asSharedFlow()

    private data class ReferenceData(
        val version: Int,
        val classes: List<com.github.arhor.spellbindr.domain.model.CharacterClass>,
        val races: List<Race>,
        val backgrounds: List<com.github.arhor.spellbindr.domain.model.Background>,
        val languages: List<Language>,
        val equipment: List<Equipment>,
        val traitsById: Map<String, Trait>,
        val featuresById: Map<String, Feature>,
        val languagesById: Map<String, Language>,
        val equipmentById: Map<String, Equipment>,
    )

    private sealed interface ReferenceDataState {
        data object Loading : ReferenceDataState
        data class Failure(val errorMessage: String) : ReferenceDataState
        data class Content(val data: ReferenceData) : ReferenceDataState
    }

    private val referenceDataVersionCounter = AtomicInteger(0)

    private data class CoreReferenceLoadables(
        val classes: Loadable<List<com.github.arhor.spellbindr.domain.model.CharacterClass>>,
        val races: Loadable<List<Race>>,
        val traits: Loadable<List<Trait>>,
        val backgrounds: Loadable<List<com.github.arhor.spellbindr.domain.model.Background>>,
        val languages: Loadable<List<Language>>,
    )

    private data class ExtraReferenceLoadables(
        val features: Loadable<List<Feature>>,
        val equipment: Loadable<List<Equipment>>,
    )

    private val referenceDataState: StateFlow<ReferenceDataState> = combine(
        combine(
            observeClasses(),
            observeRaces(),
            observeTraits(),
            observeBackgrounds(),
            observeLanguages(),
        ) { classes, races, traits, backgrounds, languages ->
            CoreReferenceLoadables(
                classes = classes,
                races = races,
                traits = traits,
                backgrounds = backgrounds,
                languages = languages,
            )
        },
        combine(
            observeFeatures(),
            observeEquipment(),
        ) { features, equipment ->
            ExtraReferenceLoadables(
                features = features,
                equipment = equipment,
            )
        },
    ) { core, extra ->
        val allLoadables: List<Loadable<*>> = listOf(
            core.classes,
            core.races,
            core.traits,
            core.backgrounds,
            core.languages,
            extra.features,
            extra.equipment,
        )

        val firstFailure = allLoadables.filterIsInstance<Loadable.Failure>().firstOrNull()
        if (firstFailure != null) {
            return@combine ReferenceDataState.Failure(
                firstFailure.errorMessage ?: "Failed to load data.",
            )
        }

        val allReady = allLoadables.all { it is Loadable.Content<*> }
        if (!allReady) {
            return@combine ReferenceDataState.Loading
        }

        val classesContent =
            (core.classes as Loadable.Content<List<com.github.arhor.spellbindr.domain.model.CharacterClass>>).data
        val racesContent = (core.races as Loadable.Content<List<Race>>).data
        val traitsContent = (core.traits as Loadable.Content<List<Trait>>).data
        val backgroundsContent =
            (core.backgrounds as Loadable.Content<List<com.github.arhor.spellbindr.domain.model.Background>>).data
        val languagesContent = (core.languages as Loadable.Content<List<Language>>).data
        val featuresContent = (extra.features as Loadable.Content<List<Feature>>).data
        val equipmentContent = (extra.equipment as Loadable.Content<List<Equipment>>).data

        ReferenceDataState.Content(
            ReferenceData(
                version = referenceDataVersionCounter.incrementAndGet(),
                classes = classesContent,
                races = racesContent,
                backgrounds = backgroundsContent,
                languages = languagesContent,
                equipment = equipmentContent,
                traitsById = traitsContent.associateBy(Trait::id),
                featuresById = featuresContent.associateBy(Feature::id),
                languagesById = languagesContent.associateBy(Language::id),
                equipmentById = equipmentContent.associateBy(Equipment::id),
            ),
        )
    }.stateIn(
        viewModelScope,
        kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        ReferenceDataState.Loading,
    )

    private data class SpellsData(
        val spells: List<Spell>,
        val spellsById: Map<String, Spell>,
        val errorMessage: String? = null,
    )

    private val spellsData: StateFlow<SpellsData> = combine(_state, referenceDataState) { state, reference ->
        val referenceData = (reference as? ReferenceDataState.Content)?.data
        shouldLoadSpells(state, referenceData)
    }
        .distinctUntilChanged()
        .flatMapLatest { shouldLoad ->
            if (!shouldLoad) {
                kotlinx.coroutines.flow.flowOf(SpellsData(emptyList(), emptyMap()))
            } else {
                observeSpells()
                    .onStart { emit(Loadable.Loading) }
                    .map { spellsState ->
                        when (spellsState) {
                            is Loadable.Content -> SpellsData(
                                spells = spellsState.data,
                                spellsById = spellsState.data.associateBy(Spell::id),
                            )

                            is Loadable.Failure -> SpellsData(
                                spells = emptyList(),
                                spellsById = emptyMap(),
                                errorMessage = spellsState.errorMessage ?: "Failed to load spells.",
                            )

                            is Loadable.Loading -> SpellsData(
                                spells = emptyList(),
                                spellsById = emptyMap(),
                            )
                        }
                    }
            }
        }
        .stateIn(
            viewModelScope,
            kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
            SpellsData(emptyList(), emptyMap()),
        )

    private fun shouldLoadSpells(state: State, referenceData: ReferenceData?): Boolean {
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
            is ReferenceDataState.Loading ->
                GuidedCharacterSetupUiState.Loading

            is ReferenceDataState.Failure ->
                GuidedCharacterSetupUiState.Failure(referenceDataState.errorMessage)

            is ReferenceDataState.Content -> {
                spellsData.errorMessage?.let { return@combine GuidedCharacterSetupUiState.Failure(it) }
                val referenceData = referenceDataState.data
                val selectedClass = state.classId?.let { id ->
                    referenceData.classes.firstOrNull { it.id == id }
                }

                val steps = computeSteps(
                    state = state,
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
                    referenceData = referenceData,
                )

                GuidedCharacterSetupUiState.Content(
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
            }
        }
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
                    !key.startsWith(CLASS_CHOICE_PREFIX) &&
                        !key.startsWith(FEATURE_CHOICE_PREFIX) &&
                        !key.startsWith(SPELL_CHOICE_PREFIX)
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

    fun onGoToStep(step: GuidedStep) {
        val content = uiState.value as? GuidedCharacterSetupUiState.Content ?: return
        val resolved = when {
            step in content.steps -> step
            step == GuidedStep.CLASS_CHOICES -> GuidedStep.CLASS
            else -> return
        }
        _state.update { it.copy(step = resolved) }
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

    internal fun buildCharacterSheet(content: GuidedCharacterSetupUiState.Content): CharacterSheet {
        val selection = content.selection
        val clazz = content.classes.firstOrNull { it.id == selection.classId }
        val race = content.races.firstOrNull { it.id == selection.raceId }
        val background = content.backgrounds.firstOrNull { it.id == selection.backgroundId }

        val baseAbilityScores = resolveBaseAbilityScores(selection) ?: AbilityIds.standardOrder.associateWith { 10 }
        val effects = buildAllEffects(
            selection = selection,
            clazz = clazz,
            races = content.races,
            backgrounds = content.backgrounds,
            traitsById = content.traitsById,
            featuresById = content.featuresById,
        )

        val computed =
            effects.fold(Character.State(level = 1, abilityScores = baseAbilityScores.toEntityRefMap())) { s, e ->
                e.applyTo(s)
            }
        val finalAbilityScores = computed.abilityScores.toAbilityScores()

        val proficiencyBonus = 2
        val conMod = finalAbilityScores.modifierFor(AbilityIds.CON)
        val dexMod = finalAbilityScores.modifierFor(AbilityIds.DEX)
        val baseHp = ((clazz?.hitDie ?: 8) + conMod).coerceAtLeast(1)
        val maxHp = (baseHp + computed.maximumHitPoints).coerceAtLeast(1)

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
        val expertiseProficiencies = selection.choiceSelections[featureChoiceKey(ROGUE_EXPERTISE_FEATURE_ID)].orEmpty()
        val expertiseSkills = expertiseProficiencies.mapNotNull(::skillFromProficiencyId).toSet()

        val skillProficiencies = (computed.proficiencies.mapNotNull(::skillFromProficiencyId).toSet() + expertiseSkills)
        val skills = Skill.entries.map { skill ->
            val proficient = skill in skillProficiencies
            val expertise = skill in expertiseSkills
            val multiplier = when {
                expertise -> 2
                proficient -> 1
                else -> 0
            }
            val bonus = finalAbilityScores.modifierFor(skill.abilityId) + proficiencyBonus * multiplier
            com.github.arhor.spellbindr.domain.model.SkillEntry(
                skill = skill,
                bonus = bonus,
                proficient = proficient,
                expertise = expertise,
            )
        }

        val classSpells = buildList {
            val source = clazz?.name.orEmpty()
            val cantrips = selection.choiceSelections[spellCantripsChoiceKey()].orEmpty()
            val level1 = selection.choiceSelections[spellLevel1ChoiceKey()].orEmpty()
            cantrips.forEach { add(CharacterSpell(spellId = it, sourceClass = source)) }
            level1.forEach { add(CharacterSpell(spellId = it, sourceClass = source)) }
        }
        val racialSpells = buildList {
            val selectedRace = race ?: return@buildList
            val traitIds = buildList {
                addAll(selectedRace.traits.map { it.id })
                val subrace = selection.subraceId?.let { sid -> selectedRace.subraces.firstOrNull { it.id == sid } }
                if (subrace != null) {
                    addAll(subrace.traits.map { it.id })
                }
            }
            traitIds.mapNotNull(content.traitsById::get).forEach { trait ->
                trait.spellChoice ?: return@forEach
                val selected = selection.choiceSelections[raceTraitSpellChoiceKey(trait.id)].orEmpty()
                if (selected.isNotEmpty()) {
                    selected.forEach { spellId ->
                        add(CharacterSpell(spellId = spellId, sourceClass = trait.name))
                    }
                }
            }
        }
        val characterSpells = classSpells + racialSpells

        val (spellSlots, pactSlots) = computeInitialSlots(clazz)

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
            spellSlots = spellSlots,
            pactSlots = pactSlots,
            savingThrows = savingThrows,
            skills = skills,
            languages = languages,
            proficiencies = proficiencies,
            equipment = equipmentText,
            featuresAndTraits = buildFeaturesAndTraitsText(content, clazz, race, background),
            characterSpells = characterSpells,
        )
    }

    private fun computeInitialSlots(
        clazz: com.github.arhor.spellbindr.domain.model.CharacterClass?,
    ): Pair<List<SpellSlotState>, PactSlotState?> {
        val emptySharedSlots = (1..9).map { level -> SpellSlotState(level = level) }
        if (clazz?.spellcasting?.level != 1) return emptySharedSlots to null

        val level1Slots: Map<String, Int> =
            clazz.levels.firstOrNull { it.level == 1 }?.spellcasting?.spellSlots.orEmpty()
        val sharedSlots = (1..9).map { level ->
            SpellSlotState(
                level = level,
                total = level1Slots[level.toString()] ?: 0,
                expended = 0,
            )
        }

        if (clazz.id != "warlock") return sharedSlots to null

        val pactTotal = level1Slots["1"] ?: 0
        return emptySharedSlots to PactSlotState(
            slotLevel = 1,
            total = pactTotal,
            expended = 0,
        )
    }

    private data class SpellRequirements(
        val cantrips: Int,
        val level1Spells: Int,
        val level1Label: String,
    )

    private fun computeSpellRequirements(
        clazz: com.github.arhor.spellbindr.domain.model.CharacterClass,
        preview: GuidedCharacterPreview,
    ): SpellRequirements? {
        if (clazz.spellcasting?.level != 1) return null

        val level1 = clazz.levels.firstOrNull { it.level == 1 }?.spellcasting
        val cantrips = level1?.cantrips ?: 0
        val level1Spells = when {
            clazz.id == "wizard" -> 6
            level1?.spells != null -> level1.spells
            clazz.id == "cleric" || clazz.id == "druid" -> (preview.abilityScores.modifierFor(AbilityIds.WIS) + 1)
                .coerceAtLeast(1)

            else -> 0
        }
        val label = when (clazz.id) {
            "wizard" -> "spellbook spell(s)"
            "cleric", "druid" -> "prepared spell(s)"
            else -> "spell(s)"
        }

        return SpellRequirements(
            cantrips = cantrips,
            level1Spells = level1Spells,
            level1Label = label,
        )
    }

    private fun buildFeaturesAndTraitsText(
        content: GuidedCharacterSetupUiState.Content,
        clazz: com.github.arhor.spellbindr.domain.model.CharacterClass?,
        race: Race?,
        background: com.github.arhor.spellbindr.domain.model.Background?,
    ): String {
        val selection = content.selection
        val lines = mutableListOf<String>()

        fun header(title: String) {
            if (lines.isNotEmpty()) lines += ""
            lines += title
        }

        fun bullet(text: String) {
            lines += "• $text"
        }

        fun spellName(id: String): String =
            content.spellsById[id]?.name ?: id

        fun optionName(id: String): String {
            if (id.lowercase() in AbilityIds.standardOrder) return id.displayName()
            if (id.startsWith("skill-")) {
                val normalized = id.removePrefix("skill-").replace("-", "_").uppercase()
                val skill = Skill.entries.firstOrNull { it.name == normalized }
                if (skill != null) return skill.displayName
            }
            content.languagesById[id]?.let { return it.name }
            content.equipmentById[id]?.let { return it.name }
            content.featuresById[id]?.let { return it.name }
            content.traitsById[id]?.let { return it.name }
            content.spellsById[id]?.let { return it.name }
            return EntityRef(id).prettyString()
        }

        if (clazz != null) {
            header("Class")
            bullet(clazz.name)

            val subclass = selection.subclassId?.let { sid -> clazz.subclasses.firstOrNull { it.id == sid } }
            if (subclass != null) {
                bullet("Subclass: ${subclass.name}")
                val subclassLevel1Features = subclass.levels?.firstOrNull { it.level == 1 }?.features.orEmpty()
                subclassLevel1Features.mapNotNull(content.featuresById::get).forEach { feature ->
                    val summary = feature.desc.firstOrNull().orEmpty()
                    bullet(if (summary.isBlank()) feature.name else "${feature.name} — $summary")
                }
            }

            val level1Choices = findLevelOneFeatureChoices(clazz, content.featuresById)
            level1Choices.forEach { (featureId, _) ->
                val selected = selection.choiceSelections[featureChoiceKey(featureId)].orEmpty()
                if (selected.isEmpty()) return@forEach
                val title = content.featuresById[featureId]?.name ?: featureId
                val values = selected.map(::optionName).sorted().joinToString(", ")
                bullet("$title: $values")
            }

            val selectedCantrips = selection.choiceSelections[spellCantripsChoiceKey()].orEmpty()
            val selectedLevel1 = selection.choiceSelections[spellLevel1ChoiceKey()].orEmpty()
            if (selectedCantrips.isNotEmpty() || selectedLevel1.isNotEmpty()) {
                header("Spells")
                if (selectedCantrips.isNotEmpty()) {
                    bullet("Cantrips: ${selectedCantrips.map(::spellName).sorted().joinToString(", ")}")
                }
                if (selectedLevel1.isNotEmpty()) {
                    bullet("Level 1: ${selectedLevel1.map(::spellName).sorted().joinToString(", ")}")
                }
            }
        }

        if (race != null) {
            header("Race")
            val raceLabel = buildString {
                append(race.name)
                val subrace = selection.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
                if (subrace != null) {
                    append(" (")
                    append(subrace.name)
                    append(")")
                }
            }
            bullet(raceLabel)

            val traitIds = buildList {
                addAll(race.traits.map { it.id })
                val subrace = selection.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
                if (subrace != null) {
                    addAll(subrace.traits.map { it.id })
                }
            }
            val traits = traitIds.mapNotNull(content.traitsById::get)
            val traitNames = traits.map { it.name }.sorted()
            if (traitNames.isNotEmpty()) {
                bullet("Traits: ${traitNames.joinToString(", ")}")
            }

            traits.forEach { trait ->
                trait.abilityBonusChoice?.let {
                    val selected = selection.choiceSelections[raceTraitAbilityBonusChoiceKey(trait.id)].orEmpty()
                    if (selected.isNotEmpty()) {
                        bullet("${trait.name}: ${selected.joinToString(", ") { id -> "${id.displayName()} +1" }}")
                    }
                }
                trait.languageChoice?.let {
                    val selected = selection.choiceSelections[raceTraitLanguageChoiceKey(trait.id)].orEmpty()
                    if (selected.isNotEmpty()) {
                        bullet("${trait.name}: ${selected.map(::optionName).sorted().joinToString(", ")}")
                    }
                }
                trait.proficiencyChoice?.let {
                    val selected = selection.choiceSelections[raceTraitProficiencyChoiceKey(trait.id)].orEmpty()
                    if (selected.isNotEmpty()) {
                        bullet("${trait.name}: ${selected.map(::optionName).sorted().joinToString(", ")}")
                    }
                }
                trait.draconicAncestryChoice?.let {
                    val selected =
                        selection.choiceSelections[raceTraitDraconicAncestryChoiceKey(trait.id)].orEmpty()
                    if (selected.isNotEmpty()) {
                        bullet("${trait.name}: ${selected.map(::optionName).sorted().joinToString(", ")}")
                    }
                }
                trait.spellChoice?.let {
                    val selected = selection.choiceSelections[raceTraitSpellChoiceKey(trait.id)].orEmpty()
                    if (selected.isNotEmpty()) {
                        bullet("${trait.name}: ${selected.map(::spellName).sorted().joinToString(", ")}")
                    }
                }
            }
        }

        if (background != null) {
            header("Background")
            bullet(background.name)
            bullet("Feature: ${background.feature.name}")

            background.languageChoice?.let {
                val selected = selection.choiceSelections[backgroundLanguageChoiceKey()].orEmpty()
                if (selected.isNotEmpty()) {
                    bullet("Languages: ${selected.map(::optionName).sorted().joinToString(", ")}")
                }
            }
            background.equipmentChoice?.let {
                val selected = selection.choiceSelections[backgroundEquipmentChoiceKey()].orEmpty()
                if (selected.isNotEmpty()) {
                    bullet("Equipment: ${selected.map(::optionName).sorted().joinToString(", ")}")
                }
            }
        }

        return lines.joinToString("\n").trim()
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

    private fun computePreview(
        selection: GuidedSelection,
        selectedClass: com.github.arhor.spellbindr.domain.model.CharacterClass?,
        referenceData: ReferenceData,
    ): GuidedCharacterPreview {
        val baseAbilityScores = resolveBaseAbilityScores(selection) ?: AbilityIds.standardOrder.associateWith { 10 }
        val effects = buildAllEffects(
            selection = selection,
            clazz = selectedClass,
            races = referenceData.races,
            backgrounds = referenceData.backgrounds,
            traitsById = referenceData.traitsById,
            featuresById = referenceData.featuresById,
        )

        val computed =
            effects.fold(Character.State(level = 1, abilityScores = baseAbilityScores.toEntityRefMap())) { s, e ->
                e.applyTo(s)
            }
        val finalAbilityScores = computed.abilityScores.toAbilityScores()

        val conMod = finalAbilityScores.modifierFor(AbilityIds.CON)
        val dexMod = finalAbilityScores.modifierFor(AbilityIds.DEX)
        val baseHp = ((selectedClass?.hitDie ?: 8) + conMod).coerceAtLeast(1)
        val maxHp = (baseHp + computed.maximumHitPoints).coerceAtLeast(1)

        return GuidedCharacterPreview(
            abilityScores = finalAbilityScores,
            maxHitPoints = maxHp,
            armorClass = 10 + dexMod,
            speed = computed.speed,
            languagesCount = computed.languages.size,
            proficienciesCount = computed.proficiencies.size,
        )
    }

    private fun buildAllEffects(
        selection: GuidedSelection,
        clazz: com.github.arhor.spellbindr.domain.model.CharacterClass?,
        races: List<Race>,
        backgrounds: List<com.github.arhor.spellbindr.domain.model.Background>,
        traitsById: Map<String, Trait>,
        featuresById: Map<String, Feature>,
    ): List<Effect> {
        val effects = mutableListOf<Effect>()

        if (clazz != null) {
            effects += Effect.AddProficienciesEffect(clazz.proficiencies.toSet())
        }
        if (clazz?.startingEquipment != null) {
            effects += Effect.AddEquipmentEffect(
                clazz.startingEquipment.map { CountedEntityRef(it.id, it.quantity) },
            )
        }

        val background = backgrounds.firstOrNull { it.id == selection.backgroundId }
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

        val race = races.firstOrNull { it.id == selection.raceId }
        if (race != null) {
            val traitIds = buildList {
                addAll(race.traits.map { it.id })
                val subrace = selection.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
                if (subrace != null) {
                    addAll(subrace.traits.map { it.id })
                }
            }
            val traits = traitIds.mapNotNull { traitsById[it] }
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
                trait.draconicAncestryChoice?.let {
                    val selected =
                        selection.choiceSelections[raceTraitDraconicAncestryChoiceKey(trait.id)].orEmpty()
                    selected.mapNotNull(traitsById::get).forEach { selectedTrait ->
                        selectedTrait.effects?.let(effects::addAll)
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
        val featureChoices = findLevelOneFeatureChoices(clazz, featuresById)
        featureChoices.forEach { (featureId, choice) ->
            val selected = selection.choiceSelections[featureChoiceKey(featureId)].orEmpty()
            if (selected.isNotEmpty()) {
                when (choice) {
                    is com.github.arhor.spellbindr.domain.model.Choice.ProficiencyChoice ->
                        if (featureId != ROGUE_EXPERTISE_FEATURE_ID) {
                            effects += Effect.AddProficienciesEffect(selected)
                        }

                    else -> Unit
                }
            }
        }

        return effects
    }

    private fun findLevelOneFeatureChoices(
        clazz: com.github.arhor.spellbindr.domain.model.CharacterClass?,
        featuresById: Map<String, Feature>,
    ): List<Pair<String, com.github.arhor.spellbindr.domain.model.Choice>> {
        if (clazz == null) return emptyList()
        val level1Features = clazz.levels.firstOrNull { it.level == 1 }?.features.orEmpty()
        return level1Features.mapNotNull { featureId ->
            val choice = featuresById[featureId]?.choice ?: return@mapNotNull null
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
                trait.draconicAncestryChoice?.let { choice ->
                    val selected =
                        content.selection.choiceSelections[raceTraitDraconicAncestryChoiceKey(trait.id)].orEmpty()
                    if (selected.size != choice.choose) {
                        issues += err("Select ${choice.choose} option(s) for ${trait.name}.")
                    }
                }
                trait.spellChoice?.let { choice ->
                    val selected = content.selection.choiceSelections[raceTraitSpellChoiceKey(trait.id)].orEmpty()
                    if (selected.size != choice.choose) {
                        issues += err("Select ${choice.choose} spell option(s) for ${trait.name}.")
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

            findLevelOneFeatureChoices(clazz, content.featuresById).forEach { (featureId, choice) ->
                val selected = content.selection.choiceSelections[featureChoiceKey(featureId)].orEmpty()
                if (selected.size != choice.choose) {
                    issues += err("Select ${choice.choose} option(s) for ${content.featuresById[featureId]?.name ?: featureId}.")
                }
            }

            computeSpellRequirements(clazz, content.preview)?.let { req ->
                val selectedCantrips = content.selection.choiceSelections[spellCantripsChoiceKey()].orEmpty()
                if (req.cantrips > 0 && selectedCantrips.size != req.cantrips) {
                    issues += err("Select ${req.cantrips} cantrip(s).")
                }
                val selectedSpells = content.selection.choiceSelections[spellLevel1ChoiceKey()].orEmpty()
                if (req.level1Spells > 0 && selectedSpells.size != req.level1Spells) {
                    issues += err("Select ${req.level1Spells} ${req.level1Label}.")
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
        private const val SPELL_CHOICE_PREFIX = "spells/"

        private const val ROGUE_EXPERTISE_FEATURE_ID = "rogue-expertise-1"

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
    ) : GuidedCharacterSetupUiState
}
