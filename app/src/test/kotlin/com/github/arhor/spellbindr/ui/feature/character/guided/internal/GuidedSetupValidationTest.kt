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
    fun `race completeness requires a valid subrace but ignores moved trait choices`() {
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

        assertThat(
            isGuidedRaceSelectionComplete(
                selection = selection(
                    raceId = race.id,
                    subraceId = "high-elf",
                    choiceSelections = emptyMap(),
                ),
                races = listOf(race),
            ),
        ).isTrue()
        assertThat(
            isGuidedRaceSelectionComplete(
                selection = selection(raceId = race.id, subraceId = "wood-elf"),
                races = listOf(race),
            ),
        ).isFalse()
    }

    @Test
    fun `requirement is complete only with exact legal non-disabled selections`() {
        val requirement = requirement(
            key = "class/proficiency/0",
            category = GuidedChoiceCategory.PROFICIENCY,
            choose = 1,
            options = listOf("arcana", "history"),
            disabledOptions = mapOf("history" to "Granted by Race trait: Training"),
        )

        assertThat(isRequirementComplete(requirement, mapOf(requirement.key to setOf("arcana")))).isTrue()
        assertThat(isRequirementComplete(requirement, emptyMap())).isFalse()
        assertThat(isRequirementComplete(requirement, mapOf(requirement.key to setOf("history")))).isFalse()
        assertThat(isRequirementComplete(requirement, mapOf(requirement.key to setOf("unknown")))).isFalse()
    }

    @Test
    fun `first incomplete requirement and owner step use canonical categories`() {
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

        assertThat(
            firstIncompleteRequirement(
                requirements = listOf(completed, incomplete),
                category = GuidedChoiceCategory.LANGUAGE,
                selections = selections,
            ),
        ).isEqualTo(incomplete)
        assertThat(guidedStepForChoiceCategory(GuidedChoiceCategory.LANGUAGE))
            .isEqualTo(GuidedStep.PROFICIENCIES_LANGUAGES)
        assertThat(guidedStepForChoiceCategory(GuidedChoiceCategory.ANCESTRY))
            .isEqualTo(GuidedStep.ANCESTRY_CHOICES)
        assertThat(guidedStepForChoiceCategory(GuidedChoiceCategory.EQUIPMENT))
            .isEqualTo(GuidedStep.EQUIPMENT)
    }

    @Test
    fun `reconciliation removes inactive keys but preserves legal active selections`() {
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

        val reconciled = reconcileGuidedChoiceSelections(
            choiceSelections = selections,
            choiceRequirements = GuidedChoiceRequirements(listOf(retained), emptyList()),
            additionalActiveKeys = setOf("spells/cantrips"),
        )

        assertThat(reconciled).containsExactly(
            retained.key, setOf("history"),
            "spells/cantrips", setOf("fire-bolt"),
        )
    }

    @Test
    fun `reconciliation removes illegal and fixed duplicate options deterministically`() {
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

        assertThat(reconciled[first.key]).containsExactly("arcana")
        assertThat(reconciled).doesNotContainKey(second.key)
    }

    @Test
    fun `reconciliation preserves active selection while options are not loaded yet`() {
        val racialSpell = requirement(
            key = "race/trait/high-elf-cantrip/spell",
            category = GuidedChoiceCategory.ANCESTRY,
            options = emptyList(),
        )

        val reconciled = reconcileGuidedChoiceSelections(
            choiceSelections = mapOf(racialSpell.key to setOf("minor-illusion")),
            choiceRequirements = GuidedChoiceRequirements(listOf(racialSpell), emptyList()),
        )

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
