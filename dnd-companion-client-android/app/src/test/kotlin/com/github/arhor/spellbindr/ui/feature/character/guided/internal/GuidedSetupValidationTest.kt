package com.github.arhor.spellbindr.ui.feature.character.guided.internal

import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedSelection
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedStep
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GuidedSetupValidationTest {

    @Test
    fun `isGuidedRaceSelectionComplete should require valid subrace when race has subraces`() {
        // Given
        val race = Race(
            id = "elf",
            name = "Elf",
            traits = listOf(EntityRef("elf-cantrip")),
            subraces = listOf(
                Race.Subrace(
                    id = "high-elf",
                    name = "High Elf",
                    desc = "",
                    traits = emptyList(),
                ),
            ),
        )

        // When
        val validResult = isGuidedRaceSelectionComplete(
            selection = selection(
                raceId = race.id,
                subraceId = "high-elf",
                choiceSelections = emptyMap(),
            ),
            races = listOf(race),
        )
        val invalidResult = isGuidedRaceSelectionComplete(
            selection = selection(raceId = race.id, subraceId = "wood-elf"),
            races = listOf(race),
        )

        // Then
        assertThat(validResult).isTrue()
        assertThat(invalidResult).isFalse()
    }

    @Test
    fun `isRequirementComplete should accept only exact legal selections when options can be disabled`() {
        // Given
        val requirement = requirement(
            key = "class/proficiency/0",
            category = GuidedChoiceCategory.PROFICIENCY,
            choose = 1,
            options = listOf("arcana", "history"),
            disabledOptions = mapOf("history" to "Granted by Race trait: Training"),
        )

        // When
        val legalResult = isRequirementComplete(requirement, mapOf(requirement.key to setOf("arcana")))
        val emptyResult = isRequirementComplete(requirement, emptyMap())
        val disabledResult = isRequirementComplete(requirement, mapOf(requirement.key to setOf("history")))
        val unknownResult = isRequirementComplete(requirement, mapOf(requirement.key to setOf("unknown")))

        // Then
        assertThat(legalResult).isTrue()
        assertThat(emptyResult).isFalse()
        assertThat(disabledResult).isFalse()
        assertThat(unknownResult).isFalse()
    }

    @Test
    fun `firstIncompleteRequirement should return matching incomplete requirement when earlier choice is complete`() {
        // Given
        val completed = requirement(
            key = "race/trait/training/proficiency",
            category = GuidedChoiceCategory.PROFICIENCY,
            options = listOf("arcana"),
        )
        val incomplete = requirement(
            key = "background/language",
            category = GuidedChoiceCategory.LANGUAGE,
            options = listOf("elvish"),
        )
        val selections = mapOf(completed.key to setOf("arcana"))

        // When
        val result = firstIncompleteRequirement(
            requirements = listOf(completed, incomplete),
            category = GuidedChoiceCategory.LANGUAGE,
            selections = selections,
        )

        // Then
        assertThat(result).isEqualTo(incomplete)
    }

    @Test
    fun `guidedStepForChoiceCategory should return owner step when category is canonical`() {
        // Given
        val categories = listOf(
            GuidedChoiceCategory.LANGUAGE,
            GuidedChoiceCategory.ANCESTRY,
            GuidedChoiceCategory.EQUIPMENT,
        )

        // When
        val results = categories.map(::guidedStepForChoiceCategory)

        // Then
        assertThat(results).containsExactly(
            GuidedStep.PROFICIENCIES_LANGUAGES,
            GuidedStep.ANCESTRY_CHOICES,
            GuidedStep.EQUIPMENT,
        ).inOrder()
    }

    @Test
    fun `reconcileGuidedChoiceSelections should remove inactive keys when active selections are legal`() {
        // Given
        val retained = requirement(
            key = "race/trait/training/proficiency",
            category = GuidedChoiceCategory.PROFICIENCY,
            options = listOf("arcana", "history"),
        )
        val selections = linkedMapOf(
            retained.key to setOf("history"),
            "race/trait/old/language" to setOf("elvish"),
            "spells/cantrips" to setOf("fire-bolt"),
        )

        // When
        val reconciled = reconcileGuidedChoiceSelections(
            choiceSelections = selections,
            choiceRequirements = GuidedChoiceRequirements(listOf(retained), emptyList()),
            additionalActiveKeys = setOf("spells/cantrips"),
        )

        // Then
        assertThat(reconciled).containsExactly(
            retained.key, setOf("history"),
            "spells/cantrips", setOf("fire-bolt"),
        )
    }

    @Test
    fun `reconcileGuidedChoiceSelections should remove illegal duplicates when fixed grants conflict`() {
        // Given
        val first = requirement(
            key = "class/proficiency/0",
            category = GuidedChoiceCategory.PROFICIENCY,
            choose = 2,
            options = listOf("arcana", "history", "perception"),
        )
        val second = requirement(
            key = "race/trait/training/proficiency",
            category = GuidedChoiceCategory.PROFICIENCY,
            options = listOf("arcana", "stealth"),
        )
        val fixed = GuidedFixedGrant(
            optionId = "history",
            displayName = "History",
            category = GuidedChoiceCategory.PROFICIENCY,
            source = GuidedChoiceSource.BACKGROUND,
            sourceId = "sage",
            sourceLabel = "Background: Sage",
        )

        // When
        val reconciled = reconcileGuidedChoiceSelections(
            choiceSelections = mapOf(
                first.key to setOf("arcana", "history", "not-an-option"),
                second.key to setOf("arcana"),
            ),
            choiceRequirements = GuidedChoiceRequirements(
                requirements = listOf(first, second),
                fixedGrants = listOf(fixed),
            ),
        )

        // Then
        assertThat(reconciled[first.key]).containsExactly("arcana")
        assertThat(reconciled).doesNotContainKey(second.key)
    }

    @Test
    fun `reconcileGuidedChoiceSelections should preserve active selection when options are not loaded`() {
        // Given
        val racialSpell = requirement(
            key = "race/trait/high-elf-cantrip/spell",
            category = GuidedChoiceCategory.ANCESTRY,
            options = emptyList(),
        )

        // When
        val reconciled = reconcileGuidedChoiceSelections(
            choiceSelections = mapOf(racialSpell.key to setOf("minor-illusion")),
            choiceRequirements = GuidedChoiceRequirements(listOf(racialSpell), emptyList()),
        )

        // Then
        assertThat(reconciled[racialSpell.key]).containsExactly("minor-illusion")
    }

    private fun requirement(
        key: String,
        category: GuidedChoiceCategory,
        choose: Int = 1,
        options: List<String>,
        disabledOptions: Map<String, String> = emptyMap(),
    ) = GuidedChoiceRequirement(
        key = key,
        source = GuidedChoiceSource.RACE_TRAIT,
        sourceId = "source",
        sourceLabel = "Race trait: Source",
        sourceDescription = null,
        category = category,
        choice = Choice.OptionsArrayChoice(choose = choose, from = options),
        options = options.map { GuidedChoiceOption(it, it) },
        selectedOptionIds = emptySet(),
        disabledOptions = disabledOptions,
    )

    private fun selection(
        raceId: String? = null,
        subraceId: String? = null,
        choiceSelections: Map<String, Set<String>> = emptyMap(),
    ) = GuidedSelection(
        classId = null,
        subclassId = null,
        raceId = raceId,
        subraceId = subraceId,
        backgroundId = null,
        abilityMethod = null,
        standardArrayAssignments = emptyMap(),
        pointBuyScores = emptyMap(),
        choiceSelections = choiceSelections,
    )
}
