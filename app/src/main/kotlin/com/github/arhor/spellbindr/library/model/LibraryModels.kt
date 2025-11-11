package com.github.arhor.spellbindr.library.model

data class SpellSummary(
    val id: String,
    val name: String,
    val level: Int,
    val school: String,
    val classes: List<String>,
)

data class MonsterSummary(
    val id: String,
    val name: String,
    val creatureType: String,
    val challengeRating: String,
    val disposition: String,
)

data class RuleSummary(
    val id: String,
    val name: String,
    val snippet: String,
)

object SampleLibraryContent {
    val spells = listOf(
        SpellSummary(
            id = "embershield",
            name = "Embershield",
            level = 3,
            school = "Abjuration",
            classes = listOf("Wizard", "Artificer"),
        ),
        SpellSummary(
            id = "tidepull",
            name = "Tidepull",
            level = 2,
            school = "Evocation",
            classes = listOf("Druid", "Cleric"),
        ),
        SpellSummary(
            id = "luminous-step",
            name = "Luminous Step",
            level = 4,
            school = "Conjuration",
            classes = listOf("Sorcerer", "Warlock", "Wizard"),
        ),
    )

    val monsters = listOf(
        MonsterSummary(
            id = "starving-mimic",
            name = "Starving Mimic",
            creatureType = "Monstrosity",
            challengeRating = "CR 4",
            disposition = "Chaotic hungry",
        ),
        MonsterSummary(
            id = "gilded-golem",
            name = "Gilded Golem",
            creatureType = "Construct",
            challengeRating = "CR 8",
            disposition = "Programmed guardian",
        ),
        MonsterSummary(
            id = "emberdrake",
            name = "Emberdrake",
            creatureType = "Dragon",
            challengeRating = "CR 10",
            disposition = "Territorial predator",
        ),
    )

    val rules = listOf(
        RuleSummary(
            id = "dodge",
            name = "Dodge Action",
            snippet = "Until the start of your next turn, attack rolls against you have disadvantage if you can see the attacker.",
        ),
        RuleSummary(
            id = "concentration",
            name = "Concentration",
            snippet = "Whenever you take damage while concentrating on a spell, roll a Constitution saving throw to maintain it.",
        ),
        RuleSummary(
            id = "exhaustion",
            name = "Exhaustion",
            snippet = "Track tiers of fatigue to represent environmental strain or relentless travel.",
        ),
    )

    fun spell(id: String): SpellSummary = spells.find { it.id == id } ?: spells.first()
    fun monster(id: String): MonsterSummary = monsters.find { it.id == id } ?: monsters.first()
    fun rule(id: String): RuleSummary = rules.find { it.id == id } ?: rules.first()
}
