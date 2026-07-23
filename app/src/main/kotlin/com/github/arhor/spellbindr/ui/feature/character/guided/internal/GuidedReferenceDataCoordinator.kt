package com.github.arhor.spellbindr.ui.feature.character.guided.internal

import com.github.arhor.spellbindr.domain.model.Background
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.Equipment
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.Language
import com.github.arhor.spellbindr.domain.model.Loadable
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.atomic.AtomicInteger

internal data class GuidedReferenceData(
    val version: Int,
    val classes: List<CharacterClass>,
    val races: List<Race>,
    val backgrounds: List<Background>,
    val languages: List<Language>,
    val equipment: List<Equipment>,
    val traitsById: Map<String, Trait>,
    val featuresById: Map<String, Feature>,
    val languagesById: Map<String, Language>,
    val equipmentById: Map<String, Equipment>,
)

internal sealed interface GuidedReferenceDataState {
    data object Loading : GuidedReferenceDataState
    data class Failure(val errorMessage: String) : GuidedReferenceDataState
    data class Content(val data: GuidedReferenceData) : GuidedReferenceDataState
}

internal data class GuidedSpellsData(
    val spells: List<Spell>,
    val spellsById: Map<String, Spell>,
    val errorMessage: String? = null,
)

private data class GuidedCoreReferenceLoadables(
    val classes: Loadable<List<CharacterClass>>,
    val races: Loadable<List<Race>>,
    val traits: Loadable<List<Trait>>,
    val backgrounds: Loadable<List<Background>>,
    val languages: Loadable<List<Language>>,
)

private data class GuidedExtraReferenceLoadables(
    val features: Loadable<List<Feature>>,
    val equipment: Loadable<List<Equipment>>,
)

internal fun observeGuidedReferenceDataState(
    scope: CoroutineScope,
    observeClasses: ObserveAllCharacterClassesUseCase,
    observeRaces: ObserveAllRacesUseCase,
    observeTraits: ObserveAllTraitsUseCase,
    observeBackgrounds: ObserveAllBackgroundsUseCase,
    observeLanguages: ObserveAllLanguagesUseCase,
    observeFeatures: ObserveAllFeaturesUseCase,
    observeEquipment: ObserveAllEquipmentUseCase,
): StateFlow<GuidedReferenceDataState> {
    val versionCounter = AtomicInteger(0)

    return combine(
        combine(
            observeClasses(),
            observeRaces(),
            observeTraits(),
            observeBackgrounds(),
            observeLanguages(),
        ) { classes, races, traits, backgrounds, languages ->
            GuidedCoreReferenceLoadables(
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
            GuidedExtraReferenceLoadables(
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

        val firstFailure = firstLoadableFailure(allLoadables)
        if (firstFailure != null) {
            return@combine GuidedReferenceDataState.Failure(
                firstFailure.errorMessage ?: "Failed to load data.",
            )
        }

        if (!allLoadablesReady(allLoadables)) {
            return@combine GuidedReferenceDataState.Loading
        }

        val classesContent = (core.classes as Loadable.Content<List<CharacterClass>>).data
        val racesContent = (core.races as Loadable.Content<List<Race>>).data
        val traitsContent = (core.traits as Loadable.Content<List<Trait>>).data
        val backgroundsContent = (core.backgrounds as Loadable.Content<List<Background>>).data
        val languagesContent = (core.languages as Loadable.Content<List<Language>>).data
        val featuresContent = (extra.features as Loadable.Content<List<Feature>>).data
        val equipmentContent = (extra.equipment as Loadable.Content<List<Equipment>>).data

        GuidedReferenceDataState.Content(
            GuidedReferenceData(
                version = versionCounter.incrementAndGet(),
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
        scope,
        SharingStarted.WhileSubscribed(5_000),
        GuidedReferenceDataState.Loading,
    )
}

internal fun observeGuidedSpellsDataState(
    scope: CoroutineScope,
    shouldLoadFlow: Flow<Boolean>,
    observeSpells: ObserveAllSpellsUseCase,
): StateFlow<GuidedSpellsData> {
    return shouldLoadFlow
        .distinctUntilChanged()
        .flatMapLatest { shouldLoad ->
            if (!shouldLoad) {
                flowOf(GuidedSpellsData(emptyList(), emptyMap()))
            } else {
                observeSpells()
                    .onStart { emit(Loadable.Loading) }
                    .map { spellsState ->
                        when (spellsState) {
                            is Loadable.Content -> GuidedSpellsData(
                                spells = spellsState.data,
                                spellsById = spellsState.data.associateBy(Spell::id),
                            )

                            is Loadable.Failure -> GuidedSpellsData(
                                spells = emptyList(),
                                spellsById = emptyMap(),
                                errorMessage = spellsState.errorMessage ?: "Failed to load spells.",
                            )

                            is Loadable.Loading -> GuidedSpellsData(
                                spells = emptyList(),
                                spellsById = emptyMap(),
                            )
                        }
                    }
            }
        }
        .stateIn(
            scope,
            SharingStarted.WhileSubscribed(5_000),
            GuidedSpellsData(emptyList(), emptyMap()),
        )
}

internal fun firstLoadableFailure(loadables: List<Loadable<*>>): Loadable.Failure? =
    loadables.filterIsInstance<Loadable.Failure>().firstOrNull()

internal fun allLoadablesReady(loadables: List<Loadable<*>>): Boolean =
    loadables.all { it is Loadable.Content<*> }
