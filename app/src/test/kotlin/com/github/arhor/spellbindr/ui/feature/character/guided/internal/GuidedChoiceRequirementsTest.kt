package com.github.arhor.spellbindr.ui.feature.character.guided.internal

import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.Background
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.Effect
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.GenericInfo
import com.github.arhor.spellbindr.domain.model.Language
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedCharacterSetupViewModel
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedSelection
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GuidedChoiceRequirementsTest {

    @Test
    fun `deriveGuidedChoiceRequirements should resolve skills when class has proficiency choice`() {
        // Given
        val clazz = characterClass(
            id = "rogue",
            name = "Rogue",
            proficiencyChoices = listOf(
                Choice.ProficiencyChoice(
                    choose = 1,
                    from = listOf("skill-stealth", "skill-insight"),
                )
            ),
        )
        val selection = selection(
            classId = clazz.id,
            choices = mapOf(
                GuidedCharacterSetupViewModel.classProficiencyChoiceKey(0) to setOf("skill-stealth")
            ),
        )

        // When
        val result = deriveGuidedChoiceRequirements(
            GuidedChoiceContext(
                selection = selection,
                classes = listOf(clazz),
            )
        )

        // Then
        assertThat(result.requirements).hasSize(1)
        with(result.requirements.single()) {
            assertThat(key).isEqualTo("class/proficiency/0")
            assertThat(source).isEqualTo(GuidedChoiceSource.CLASS)
            assertThat(sourceId).isEqualTo("rogue")
            assertThat(sourceLabel).isEqualTo("Class: Rogue")
            assertThat(category).isEqualTo(GuidedChoiceCategory.PROFICIENCY)
            assertThat(options.map { it.id to it.displayName }).containsExactly(
                "skill-stealth" to "Stealth",
                "skill-insight" to "Insight",
            ).inOrder()
            assertThat(selectedOptionIds).containsExactly("skill-stealth")
        }
    }

    @Test
    fun `deriveGuidedChoiceRequirements should expose grants and conflicts when race trait has choices`() {
        // Given
        val trait = Trait(
            id = "elf-training",
            name = "Elf Training",
            desc = listOf("You have training passed down through elven tradition."),
            effects = listOf(
                Effect.AddProficienciesEffect(setOf("skill-perception")),
                Effect.AddLanguagesEffect(setOf("common")),
            ),
            proficiencyChoice = Choice.ProficiencyChoice(
                choose = 1,
                from = listOf("skill-perception", "skill-arcana"),
            ),
            languageChoice = Choice.FromAllChoice(choose = 1),
        )
        val race = race(
            id = "elf",
            name = "Elf",
            traitIds = listOf(trait.id),
        )

        // When
        val result = deriveGuidedChoiceRequirements(
            GuidedChoiceContext(
                selection = selection(raceId = race.id),
                races = listOf(race),
                traitsById = mapOf(trait.id to trait),
                languages = listOf(
                    language("common", "Common"),
                    language("elvish", "Elvish"),
                ),
            )
        )

        // Then
        assertThat(result.fixedGrants.map { it.optionId }).containsExactly(
            "skill-perception",
            "common",
        ).inOrder()
        assertThat(result.fixedGrants.map { it.sourceLabel }.distinct())
            .containsExactly("Race trait: Elf Training")
        assertThat(result.requirements.map { it.category }).containsExactly(
            GuidedChoiceCategory.PROFICIENCY,
            GuidedChoiceCategory.LANGUAGE,
        ).inOrder()

        val proficiency = result.requirements.first()
        assertThat(proficiency.disabledOptions["skill-perception"])
            .isEqualTo("Granted by Race trait: Elf Training")
        assertThat(proficiency.sourceDescription)
            .isEqualTo("You have training passed down through elven tradition.")

        val language = result.requirements.last()
        assertThat(language.options.map { it.displayName }).containsExactly("Common", "Elvish").inOrder()
        assertThat(language.disabledOptions["common"]).isEqualTo("Granted by Race trait: Elf Training")
    }

    @Test
    fun `deriveGuidedChoiceRequirements should expose grants and requirements when background has choices`() {
        // Given
        val background = background(
            id = "artisan",
            name = "Guild Artisan",
            effects = listOf(
                Effect.AddProficienciesEffect(setOf("skill-insight")),
                Effect.AddLanguagesEffect(setOf("common")),
            ),
            languageChoice = Choice.FromAllChoice(choose = 1),
            equipmentChoice = Choice.EquipmentChoice(
                choose = 1,
                from = listOf("artisans-tools"),
            ),
        )

        // When
        val result = deriveGuidedChoiceRequirements(
            GuidedChoiceContext(
                selection = selection(backgroundId = background.id),
                backgrounds = listOf(background),
                languages = listOf(
                    language("common", "Common"),
                    language("dwarvish", "Dwarvish"),
                ),
            )
        )

        // Then
        assertThat(result.fixedGrants.map { it.optionId }).containsExactly(
            "skill-insight",
            "common",
        ).inOrder()
        assertThat(result.requirements.map { it.category }).containsExactly(
            GuidedChoiceCategory.LANGUAGE,
            GuidedChoiceCategory.EQUIPMENT,
        ).inOrder()
        assertThat(result.requirements.first().disabledOptions["common"])
            .isEqualTo("Granted by Background: Guild Artisan")
        assertThat(result.requirements.last().options.single())
            .isEqualTo(GuidedChoiceOption("artisans-tools", "Artisans Tools"))
    }

    @Test
    fun `deriveGuidedChoiceRequirements should preserve child limits when background equipment choice is nested`() {
        // Given
        val background = background(
            id = "soldier",
            name = "Soldier",
            effects = emptyList(),
            equipmentChoice = Choice.NestedChoice(
                choose = 1,
                from = listOf(
                    Choice.EquipmentChoice(choose = 2, from = listOf("javelin", "handaxe")),
                    Choice.EquipmentChoice(choose = 1, from = listOf("longsword", "shortsword")),
                ),
            ),
        )

        // When
        val result = deriveGuidedChoiceRequirements(
            GuidedChoiceContext(
                selection = selection(
                    backgroundId = background.id,
                    choices = mapOf(backgroundNestedEquipmentChoiceKey(0) to setOf("javelin")),
                ),
                backgrounds = listOf(background),
            )
        )

        // Then
        assertThat(result.requirements.map { it.key }).containsExactly(
            "background/equipment/0",
            "background/equipment/1",
        ).inOrder()
        assertThat(result.requirements.map { it.choice.choose }).containsExactly(2, 1).inOrder()
        assertThat(result.requirements[0].options.map { it.id })
            .containsExactly("javelin", "handaxe").inOrder()
        assertThat(result.requirements[0].selectedOptionIds).containsExactly("javelin")
        assertThat(result.requirements[1].options.map { it.id })
            .containsExactly("longsword", "shortsword").inOrder()
    }

    @Test
    fun `deriveGuidedChoiceRequirements should retain deterministic sources when grants and selections overlap`() {
        // Given
        val clazz = characterClass(
            id = "ranger",
            name = "Ranger",
            proficiencies = listOf("skill-survival"),
            proficiencyChoices = listOf(
                Choice.ProficiencyChoice(choose = 1, from = listOf("skill-survival"))
            ),
        )
        val trait = Trait(
            id = "survivor",
            name = "Survivor",
            desc = emptyList(),
            effects = listOf(
                Effect.AddProficienciesEffect(setOf("skill-survival")),
                Effect.AddLanguagesEffect(setOf("common")),
            ),
            languageChoice = Choice.FromAllChoice(choose = 1),
        )
        val race = race(id = "human", name = "Human", traitIds = listOf(trait.id))
        val background = background(
            id = "outlander",
            name = "Outlander",
            effects = listOf(
                Effect.AddProficienciesEffect(setOf("skill-survival")),
                Effect.AddLanguagesEffect(setOf("common")),
            ),
            languageChoice = Choice.FromAllChoice(choose = 1),
        )
        val raceLanguageKey = GuidedCharacterSetupViewModel.raceTraitLanguageChoiceKey(trait.id)
        val backgroundLanguageKey = GuidedCharacterSetupViewModel.backgroundLanguageChoiceKey()

        // When
        val result = deriveGuidedChoiceRequirements(
            GuidedChoiceContext(
                selection = selection(
                    classId = clazz.id,
                    raceId = race.id,
                    backgroundId = background.id,
                    choices = mapOf(
                        raceLanguageKey to setOf("elvish"),
                        backgroundLanguageKey to setOf("elvish"),
                    ),
                ),
                classes = listOf(clazz),
                races = listOf(race),
                backgrounds = listOf(background),
                traitsById = mapOf(trait.id to trait),
                languages = listOf(
                    language("common", "Common"),
                    language("elvish", "Elvish"),
                ),
            )
        )

        // Then
        assertThat(result.fixedGrants.filter { it.optionId == "skill-survival" }.map { it.sourceLabel })
            .containsExactly(
                "Class: Ranger",
                "Race trait: Survivor",
                "Background: Outlander",
            ).inOrder()
        assertThat(result.fixedGrants.filter { it.optionId == "common" }.map { it.sourceLabel })
            .containsExactly(
                "Race trait: Survivor",
                "Background: Outlander",
            ).inOrder()

        val classProficiency = result.requirements.first { it.source == GuidedChoiceSource.CLASS }
        assertThat(classProficiency.disabledOptions["skill-survival"]).isEqualTo(
            "Granted by Class: Ranger, Race trait: Survivor, Background: Outlander"
        )
        val raceLanguage = result.requirements.first { it.key == raceLanguageKey }
        assertThat(raceLanguage.disabledOptions["elvish"])
            .isEqualTo("Selected for Background: Outlander")
    }

    @Test
    fun `deriveGuidedChoiceRequirements should include only active traits when subrace is selected`() {
        // Given
        val baseTrait = Trait(
            id = "base-choice",
            name = "Base Choice",
            desc = emptyList(),
            languageChoice = Choice.FromAllChoice(1),
        )
        val highTrait = Trait(
            id = "high-choice",
            name = "High Choice",
            desc = emptyList(),
            spellChoice = Choice.OptionsArrayChoice(1, listOf("light")),
        )
        val woodTrait = Trait(
            id = "wood-choice",
            name = "Wood Choice",
            desc = emptyList(),
            proficiencyChoice = Choice.ProficiencyChoice(1, listOf("skill-stealth")),
        )
        val race = Race(
            id = "elf",
            name = "Elf",
            traits = listOf(EntityRef(baseTrait.id)),
            subraces = listOf(
                Race.Subrace("high-elf", "High Elf", "", listOf(EntityRef(highTrait.id))),
                Race.Subrace("wood-elf", "Wood Elf", "", listOf(EntityRef(woodTrait.id))),
            ),
        )

        // When
        val result = deriveGuidedChoiceRequirements(
            GuidedChoiceContext(
                selection = selection(raceId = race.id, subraceId = "high-elf"),
                races = listOf(race),
                traitsById = listOf(baseTrait, highTrait, woodTrait).associateBy { it.id },
            )
        )

        // Then
        assertThat(result.requirements.map { it.sourceId }).containsExactly(
            baseTrait.id,
            highTrait.id,
        )
        assertThat(result.requirements.first { it.sourceId == highTrait.id }.source)
            .isEqualTo(GuidedChoiceSource.SUBRACE_TRAIT)
        assertThat(result.requirements.map { it.sourceId }).doesNotContain(woodTrait.id)
    }

    @Test
    fun `deriveGuidedChoiceRequirements should classify ancestry choices when race trait provides them`() {
        // Given
        val trait = Trait(
            id = "lineage-choices",
            name = "Lineage Choices",
            desc = listOf("Choose the expressions of your ancestry."),
            abilityBonusChoice = Choice.AbilityBonusChoice(
                choose = 1,
                from = listOf(mapOf(AbilityIds.DEX to 1), mapOf(AbilityIds.INT to 1)),
            ),
            draconicAncestryChoice = Choice.OptionsArrayChoice(
                choose = 1,
                from = listOf("draconic-ancestry-gold"),
            ),
            spellChoice = Choice.OptionsArrayChoice(
                choose = 1,
                from = listOf("thaumaturgy"),
            ),
        )
        val goldAncestry = Trait(
            id = "draconic-ancestry-gold",
            name = "Gold Dragon",
            desc = emptyList(),
        )
        val race = race("dragonborn", "Dragonborn", listOf(trait.id))

        // When
        val result = deriveGuidedChoiceRequirements(
            GuidedChoiceContext(
                selection = selection(raceId = race.id),
                races = listOf(race),
                traitsById = mapOf(
                    trait.id to trait,
                    goldAncestry.id to goldAncestry,
                ),
            )
        )

        // Then
        assertThat(result.requirements).hasSize(3)
        assertThat(result.requirements.map { it.category }.distinct())
            .containsExactly(GuidedChoiceCategory.ANCESTRY)
        assertThat(result.requirements.map { it.key }).containsExactly(
            GuidedCharacterSetupViewModel.raceTraitAbilityBonusChoiceKey(trait.id),
            GuidedCharacterSetupViewModel.raceTraitDraconicAncestryChoiceKey(trait.id),
            GuidedCharacterSetupViewModel.raceTraitSpellChoiceKey(trait.id),
        ).inOrder()
        val ability = result.requirements.first()
        assertThat(ability.options.map { it.displayName }).containsExactly(
            "Dexterity +1",
            "Intelligence +1",
        ).inOrder()
        val draconic = result.requirements[1]
        assertThat(draconic.options.single().displayName).isEqualTo("Gold Dragon")
    }

    private fun selection(
        classId: String? = null,
        raceId: String? = null,
        subraceId: String? = null,
        backgroundId: String? = null,
        choices: Map<String, Set<String>> = emptyMap(),
    ) = GuidedSelection(
        classId = classId,
        subclassId = null,
        raceId = raceId,
        subraceId = subraceId,
        backgroundId = backgroundId,
        abilityMethod = null,
        standardArrayAssignments = emptyMap(),
        pointBuyScores = emptyMap(),
        choiceSelections = choices,
    )

    private fun characterClass(
        id: String,
        name: String,
        proficiencies: List<String> = emptyList(),
        proficiencyChoices: List<Choice> = emptyList(),
    ) = CharacterClass(
        id = id,
        name = name,
        hitDie = 8,
        proficiencies = proficiencies,
        proficiencyChoices = proficiencyChoices,
        savingThrows = emptyList(),
        subclasses = emptyList(),
        levels = emptyList(),
    )

    private fun race(
        id: String,
        name: String,
        traitIds: List<String>,
    ) = Race(
        id = id,
        name = name,
        traits = traitIds.map(::EntityRef),
        subraces = emptyList(),
    )

    private fun background(
        id: String,
        name: String,
        effects: List<Effect>,
        languageChoice: Choice? = null,
        equipmentChoice: Choice? = null,
    ) = Background(
        id = id,
        name = name,
        feature = GenericInfo("Feature", emptyList()),
        effects = effects,
        languageChoice = languageChoice,
        equipmentChoice = equipmentChoice,
    )

    private fun language(
        id: String,
        name: String,
    ) = Language(
        id = id,
        name = name,
        type = "Standard",
        typicalSpeakers = emptyList(),
    )
}
