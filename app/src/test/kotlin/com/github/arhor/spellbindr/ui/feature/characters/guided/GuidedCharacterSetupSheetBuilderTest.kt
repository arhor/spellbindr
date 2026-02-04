package com.github.arhor.spellbindr.ui.feature.characters.guided

import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.Background
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.ClassLevel
import com.github.arhor.spellbindr.domain.model.Effect
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.GenericInfo
import com.github.arhor.spellbindr.domain.model.Language
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Skill
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
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class GuidedCharacterSetupSheetBuilderTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `buildCharacterSheet should apply per-level hp effects`() = runTest(mainDispatcherRule.dispatcher) {
        val vm = buildViewModel()

        val toughTrait = Trait(
            id = "dwarven-toughness",
            name = "Dwarven Toughness",
            desc = listOf("Your hit point maximum increases by 1, and it increases by 1 every time you gain a level."),
            effects = listOf(Effect.AddHpEffect(value = 1, perLevel = true)),
        )
        val dwarf = Race(
            id = "dwarf",
            name = "Dwarf",
            traits = listOf(EntityRef(toughTrait.id)),
            subraces = emptyList(),
        )
        val fighter = CharacterClass(
            id = "fighter",
            name = "Fighter",
            hitDie = 8,
            proficiencies = emptyList(),
            proficiencyChoices = emptyList(),
            savingThrows = emptyList(),
            spellcasting = null,
            startingEquipment = null,
            subclasses = emptyList(),
            levels = listOf(
                ClassLevel(
                    id = "fighter-1",
                    level = 1,
                    features = emptyList(),
                ),
            ),
        )
        val background = Background(
            id = "acolyte",
            name = "Acolyte",
            feature = GenericInfo(name = "Shelter of the Faithful", desc = emptyList()),
            effects = emptyList(),
        )

        val selection = GuidedSelection(
            classId = fighter.id,
            subclassId = null,
            raceId = dwarf.id,
            subraceId = null,
            backgroundId = background.id,
            abilityMethod = AbilityScoreMethod.POINT_BUY,
            standardArrayAssignments = defaultStandardArrayAssignments(),
            pointBuyScores = AbilityIds.standardOrder.associateWith { 10 },
            choiceSelections = emptyMap(),
        )
        val content = buildContent(
            name = "Test",
            classes = listOf(fighter),
            races = listOf(dwarf),
            backgrounds = listOf(background),
            traits = listOf(toughTrait),
            features = emptyList(),
            spells = emptyList(),
            selection = selection,
        )

        val sheet = vm.buildCharacterSheet(content)

        // Base HP at level 1: hit die + CON mod (0) = 8, plus racial bonus +1.
        assertThat(sheet.maxHitPoints).isEqualTo(9)
    }

    @Test
    fun `buildCharacterSheet should mark expertise on selected skills`() = runTest(mainDispatcherRule.dispatcher) {
        val vm = buildViewModel()

        val rogue = CharacterClass(
            id = "rogue",
            name = "Rogue",
            hitDie = 8,
            proficiencies = emptyList(),
            proficiencyChoices = emptyList(),
            savingThrows = emptyList(),
            spellcasting = null,
            startingEquipment = null,
            subclasses = emptyList(),
            levels = listOf(
                ClassLevel(
                    id = "rogue-1",
                    level = 1,
                    features = emptyList(),
                ),
            ),
        )
        val human = Race(
            id = "human",
            name = "Human",
            traits = emptyList(),
            subraces = emptyList(),
        )
        val background = Background(
            id = "criminal",
            name = "Criminal",
            feature = GenericInfo(name = "Criminal Contact", desc = emptyList()),
            effects = emptyList(),
        )

        val choiceKey = GuidedCharacterSetupViewModel.featureChoiceKey("rogue-expertise-1")
        val selection = GuidedSelection(
            classId = rogue.id,
            subclassId = null,
            raceId = human.id,
            subraceId = null,
            backgroundId = background.id,
            abilityMethod = AbilityScoreMethod.POINT_BUY,
            standardArrayAssignments = defaultStandardArrayAssignments(),
            pointBuyScores = AbilityIds.standardOrder.associateWith { 10 },
            choiceSelections = mapOf(
                choiceKey to setOf("skill-stealth", "skill-perception"),
            ),
        )

        val content = buildContent(
            name = "Sneaky",
            classes = listOf(rogue),
            races = listOf(human),
            backgrounds = listOf(background),
            traits = emptyList(),
            features = listOf(
                Feature(
                    id = "rogue-expertise-1",
                    name = "Expertise",
                    desc = emptyList(),
                    choice = Choice.ProficiencyChoice(
                        choose = 2,
                        from = listOf("skill-stealth", "skill-perception"),
                    ),
                ),
            ),
            spells = emptyList(),
            selection = selection,
        )

        val sheet = vm.buildCharacterSheet(content)

        val stealth = sheet.skills.first { it.skill == Skill.STEALTH }
        assertThat(stealth.expertise).isTrue()
        assertThat(stealth.proficient).isTrue()
        assertThat(stealth.bonus).isEqualTo(4)

        val perception = sheet.skills.first { it.skill == Skill.PERCEPTION }
        assertThat(perception.expertise).isTrue()
        assertThat(perception.proficient).isTrue()
        assertThat(perception.bonus).isEqualTo(4)
    }

    @Test
    fun `validate should surface required selections and ability method`() = runTest(mainDispatcherRule.dispatcher) {
        val vm = buildViewModel()

        val content = buildContent(
            name = "",
            classes = emptyList(),
            races = emptyList(),
            backgrounds = emptyList(),
            traits = emptyList(),
            features = emptyList(),
            spells = emptyList(),
            selection = GuidedSelection(
                classId = null,
                subclassId = null,
                raceId = null,
                subraceId = null,
                backgroundId = null,
                abilityMethod = null,
                standardArrayAssignments = defaultStandardArrayAssignments(),
                pointBuyScores = defaultPointBuyScores(),
                choiceSelections = emptyMap(),
            ),
        )

        val result = vm.validate(content)

        assertThat(result.hasErrors).isTrue()
        assertThat(result.issues.map { it.message }).containsAtLeast(
            "Choose a class.",
            "Choose a race.",
            "Choose a background.",
            "Choose an ability score method.",
        )
    }

    @Test
    fun `validate should fail when point buy exceeds budget`() = runTest(mainDispatcherRule.dispatcher) {
        val vm = buildViewModel()

        val fighter = CharacterClass(
            id = "fighter",
            name = "Fighter",
            hitDie = 10,
            proficiencies = emptyList(),
            proficiencyChoices = emptyList(),
            savingThrows = emptyList(),
            spellcasting = null,
            startingEquipment = null,
            subclasses = emptyList(),
            levels = listOf(ClassLevel(id = "fighter-1", level = 1, features = emptyList())),
        )
        val human = Race(id = "human", name = "Human", traits = emptyList(), subraces = emptyList())
        val background = Background(
            id = "acolyte",
            name = "Acolyte",
            feature = GenericInfo(name = "Shelter of the Faithful", desc = emptyList()),
            effects = emptyList(),
        )

        val content = buildContent(
            name = "Overbudget",
            classes = listOf(fighter),
            races = listOf(human),
            backgrounds = listOf(background),
            traits = emptyList(),
            features = emptyList(),
            spells = emptyList(),
            selection = GuidedSelection(
                classId = fighter.id,
                subclassId = null,
                raceId = human.id,
                subraceId = null,
                backgroundId = background.id,
                abilityMethod = AbilityScoreMethod.POINT_BUY,
                standardArrayAssignments = defaultStandardArrayAssignments(),
                pointBuyScores = AbilityIds.standardOrder.associateWith { 15 },
                choiceSelections = emptyMap(),
            ),
        )

        val result = vm.validate(content)

        assertThat(result.hasErrors).isTrue()
        assertThat(result.issues.map { it.message }).contains("Point buy exceeds 27 points.")
    }

    @Test
    fun `validate should fail when standard array assignments are invalid`() = runTest(mainDispatcherRule.dispatcher) {
        val vm = buildViewModel()

        val fighter = CharacterClass(
            id = "fighter",
            name = "Fighter",
            hitDie = 10,
            proficiencies = emptyList(),
            proficiencyChoices = emptyList(),
            savingThrows = emptyList(),
            spellcasting = null,
            startingEquipment = null,
            subclasses = emptyList(),
            levels = listOf(ClassLevel(id = "fighter-1", level = 1, features = emptyList())),
        )
        val human = Race(id = "human", name = "Human", traits = emptyList(), subraces = emptyList())
        val background = Background(
            id = "acolyte",
            name = "Acolyte",
            feature = GenericInfo(name = "Shelter of the Faithful", desc = emptyList()),
            effects = emptyList(),
        )

        val invalidAssignments = defaultStandardArrayAssignments()
            .toMutableMap()
            .apply { put(AbilityIds.STR, 15) }
            .toMap()
        val content = buildContent(
            name = "Array",
            classes = listOf(fighter),
            races = listOf(human),
            backgrounds = listOf(background),
            traits = emptyList(),
            features = emptyList(),
            spells = emptyList(),
            selection = GuidedSelection(
                classId = fighter.id,
                subclassId = null,
                raceId = human.id,
                subraceId = null,
                backgroundId = background.id,
                abilityMethod = AbilityScoreMethod.STANDARD_ARRAY,
                standardArrayAssignments = invalidAssignments,
                pointBuyScores = defaultPointBuyScores(),
                choiceSelections = emptyMap(),
            ),
        )

        val result = vm.validate(content)

        assertThat(result.hasErrors).isTrue()
        assertThat(result.issues.map { it.message }).contains(
            "Assign all ability scores using the standard array (15, 14, 13, 12, 10, 8).",
        )
    }

    private fun buildViewModel(): GuidedCharacterSetupViewModel {
        val observeClasses = mockk<ObserveAllCharacterClassesUseCase>()
        val observeRaces = mockk<ObserveAllRacesUseCase>()
        val observeTraits = mockk<ObserveAllTraitsUseCase>()
        val observeBackgrounds = mockk<ObserveAllBackgroundsUseCase>()
        val observeLanguages = mockk<ObserveAllLanguagesUseCase>()
        val observeFeatures = mockk<ObserveAllFeaturesUseCase>()
        val observeEquipment = mockk<ObserveAllEquipmentUseCase>()
        val observeSpells = mockk<ObserveAllSpellsUseCase>()
        val saveCharacterSheet = mockk<SaveCharacterSheetUseCase>(relaxed = true)

        every { observeClasses() } returns flowOf(Loadable.Content(emptyList()))
        every { observeRaces() } returns flowOf(Loadable.Content(emptyList()))
        every { observeTraits() } returns flowOf(Loadable.Content(emptyList()))
        every { observeBackgrounds() } returns flowOf(Loadable.Content(emptyList()))
        every { observeLanguages() } returns flowOf(Loadable.Content(emptyList()))
        every { observeFeatures() } returns flowOf(Loadable.Content(emptyList()))
        every { observeEquipment() } returns flowOf(Loadable.Content(emptyList()))
        every { observeSpells() } returns flowOf(emptyList())

        return GuidedCharacterSetupViewModel(
            observeClasses = observeClasses,
            observeRaces = observeRaces,
            observeTraits = observeTraits,
            observeBackgrounds = observeBackgrounds,
            observeLanguages = observeLanguages,
            observeFeatures = observeFeatures,
            observeEquipment = observeEquipment,
            observeSpells = observeSpells,
            saveCharacterSheet = saveCharacterSheet,
        )
    }

    private fun buildContent(
        name: String,
        classes: List<CharacterClass>,
        races: List<Race>,
        backgrounds: List<Background>,
        traits: List<Trait>,
        features: List<Feature>,
        spells: List<Spell>,
        selection: GuidedSelection,
    ): GuidedCharacterSetupUiState.Content {
        return GuidedCharacterSetupUiState.Content(
            step = GuidedStep.REVIEW,
            steps = listOf(GuidedStep.REVIEW),
            currentStepIndex = 0,
            totalSteps = 1,
            name = name,
            classes = classes,
            races = races,
            backgrounds = backgrounds,
            languages = emptyList(),
            equipment = emptyList(),
            traitsById = traits.associateBy { it.id },
            featuresById = features.associateBy { it.id },
            languagesById = emptyMap<String, Language>(),
            equipmentById = emptyMap(),
            spells = spells,
            spellsById = spells.associateBy { it.id },
            referenceDataVersion = 1,
            selection = selection,
            preview = GuidedCharacterPreview(
                abilityScores = AbilityScores(),
                maxHitPoints = 1,
                armorClass = 10,
                speed = 30,
                languagesCount = 0,
                proficienciesCount = 0,
            ),
            isSaving = false,
        )
    }
}
