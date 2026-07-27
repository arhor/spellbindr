package com.github.arhor.spellbindr.ui.feature.character.guided.components.race

import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.Effect
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RaceSummaryUiModelTest {

    @Test
    fun `summary resolves structured mechanics and counts deferred choices`() {
        val race = race()
        val traits = mapOf(
            "ability-score-increase-test" to trait(
                id = "ability-score-increase-test",
                name = "Ability Score Increase",
                effects = listOf(Effect.ModifyAbilityEffect(mapOf("dex" to 2))),
            ),
            "size-test" to trait(
                id = "size-test",
                name = "Size",
                effects = listOf(Effect.ModifySizeEffect("medium")),
            ),
            "speed-test" to trait(
                id = "speed-test",
                name = "Speed",
                effects = listOf(Effect.ModifySpeedEffect(30)),
            ),
            "keen-senses-test" to trait(
                id = "keen-senses-test",
                name = "Keen Senses",
                proficiencyChoice = Choice.OptionsArrayChoice(
                    choose = 2,
                    from = listOf("perception", "insight"),
                ),
            ),
            "extra-language-test" to trait(
                id = "extra-language-test",
                name = "Extra Language",
                languageChoice = Choice.FromAllChoice(choose = 1),
            ),
            "subrace-magic-test" to trait(
                id = "subrace-magic-test",
                name = "Innate Magic",
                spellChoice = Choice.OptionsArrayChoice(
                    choose = 1,
                    from = listOf("light"),
                ),
                effects = listOf(Effect.ModifyAbilityEffect(mapOf("int" to 1))),
            ),
        )

        val result = raceSummaryUiModel(
            race = race,
            traitsById = traits,
            selectedSubraceId = "scholar-test",
        )

        assertThat(result.name).isEqualTo("Testfolk")
        assertThat(result.selectedSubraceName).isEqualTo("Scholar Testfolk")
        assertThat(result.size).isEqualTo("Medium")
        assertThat(result.speedFeet).isEqualTo(30)
        assertThat(result.abilityBonuses).containsExactly("Dexterity +2", "Intelligence +1").inOrder()
        assertThat(result.definingTraits).containsExactly("Keen Senses", "Extra Language", "Innate Magic").inOrder()
        assertThat(result.deferredChoices.map { it.label }).containsExactly(
            "2 proficiency choices later",
            "1 language choice later",
            "1 ancestry choice later",
        ).inOrder()
    }

    @Test
    fun `summary excludes subrace traits until that subrace is selected`() {
        val race = race()
        val subraceTrait = trait(
            id = "subrace-magic-test",
            name = "Innate Magic",
            spellChoice = Choice.OptionsArrayChoice(choose = 1, from = listOf("light")),
        )

        val result = raceSummaryUiModel(
            race = race,
            traitsById = mapOf("subrace-magic-test" to subraceTrait),
            selectedSubraceId = null,
        )

        assertThat(result.selectedSubraceName).isNull()
        assertThat(result.traitDetails).isEmpty()
        assertThat(result.deferredChoices).isEmpty()
    }

    @Test
    fun `summary ignores missing trait references without inventing mechanics`() {
        val result = raceSummaryUiModel(
            race = race(),
            traitsById = emptyMap(),
            selectedSubraceId = "unknown-subrace",
        )

        assertThat(result.size).isNull()
        assertThat(result.speedFeet).isNull()
        assertThat(result.abilityBonuses).isEmpty()
        assertThat(result.definingTraits).isEmpty()
    }

    private fun race() = Race(
        id = "testfolk",
        name = "Testfolk",
        traits = listOf(
            EntityRef("ability-score-increase-test"),
            EntityRef("size-test"),
            EntityRef("speed-test"),
            EntityRef("keen-senses-test"),
            EntityRef("extra-language-test"),
        ),
        subraces = listOf(
            Race.Subrace(
                id = "scholar-test",
                name = "Scholar Testfolk",
                desc = "Curious and magically gifted.",
                traits = listOf(EntityRef("subrace-magic-test")),
            ),
        ),
    )

    private fun trait(
        id: String,
        name: String,
        effects: List<Effect>? = null,
        spellChoice: Choice? = null,
        languageChoice: Choice? = null,
        proficiencyChoice: Choice? = null,
    ) = Trait(
        id = id,
        name = name,
        desc = listOf("$name description."),
        effects = effects,
        spellChoice = spellChoice,
        languageChoice = languageChoice,
        proficiencyChoice = proficiencyChoice,
    )
}
