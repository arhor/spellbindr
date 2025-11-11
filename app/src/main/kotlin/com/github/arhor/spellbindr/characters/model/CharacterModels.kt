package com.github.arhor.spellbindr.characters.model

data class CharacterSummary(
    val id: String,
    val name: String,
    val className: String,
    val level: Int,
    val race: String,
    val ancestry: String,
)

data class CharacterVitals(
    val hitPoints: String,
    val armorClass: Int,
    val initiative: Int,
)

data class AbilityScore(
    val label: String,
    val value: Int,
) {
    val modifier: Int
        get() = (value - 10) / 2
}

data class CharacterDetails(
    val summary: CharacterSummary,
    val vitals: CharacterVitals,
    val abilityScores: List<AbilityScore>,
    val preparedSpells: List<String>,
    val notes: List<String>,
)

object SampleCharacterRepository {

    private val characters = listOf(
        CharacterDetails(
            summary = CharacterSummary(
                id = "astra",
                name = "Astra Moonshadow",
                className = "Wizard",
                level = 7,
                race = "Half-elf",
                ancestry = "Luna Conservatory",
            ),
            vitals = CharacterVitals(
                hitPoints = "38 / 38",
                armorClass = 15,
                initiative = 2,
            ),
            abilityScores = listOf(
                AbilityScore("STR", 10),
                AbilityScore("DEX", 14),
                AbilityScore("CON", 12),
                AbilityScore("INT", 18),
                AbilityScore("WIS", 13),
                AbilityScore("CHA", 11),
            ),
            preparedSpells = listOf(
                "Arcane Eye",
                "Counterspell",
                "Fireball",
                "Shield",
                "Wall of Force",
            ),
            notes = listOf(
                "Keeps a pocket atlas filled with constellations.",
                "Bargained with the Starweaver to protect the party.",
            ),
        ),
        CharacterDetails(
            summary = CharacterSummary(
                id = "bronn",
                name = "Bronn Blackbriar",
                className = "Fighter",
                level = 5,
                race = "Human",
                ancestry = "Knight of the Autumn Guard",
            ),
            vitals = CharacterVitals(
                hitPoints = "52 / 52",
                armorClass = 18,
                initiative = 1,
            ),
            abilityScores = listOf(
                AbilityScore("STR", 17),
                AbilityScore("DEX", 12),
                AbilityScore("CON", 16),
                AbilityScore("INT", 11),
                AbilityScore("WIS", 12),
                AbilityScore("CHA", 14),
            ),
            preparedSpells = emptyList(),
            notes = listOf(
                "Sworn to defend the village of Stillmere.",
                "Keeps meticulous maintenance logs for his gear.",
            ),
        ),
        CharacterDetails(
            summary = CharacterSummary(
                id = "kel",
                name = "Kel Whisperwind",
                className = "Druid",
                level = 4,
                race = "Wood Elf",
                ancestry = "Circle of Seeds",
            ),
            vitals = CharacterVitals(
                hitPoints = "29 / 29",
                armorClass = 16,
                initiative = 3,
            ),
            abilityScores = listOf(
                AbilityScore("STR", 9),
                AbilityScore("DEX", 16),
                AbilityScore("CON", 14),
                AbilityScore("INT", 12),
                AbilityScore("WIS", 17),
                AbilityScore("CHA", 13),
            ),
            preparedSpells = listOf(
                "Call Lightning",
                "Goodberry",
                "Moonbeam",
                "Pass Without Trace",
            ),
            notes = listOf(
                "Can always identify the nearest grove or spring.",
                "Collects pressed leaves from every journey.",
            ),
        ),
    )

    fun summaries(): List<CharacterSummary> = characters.map(CharacterDetails::summary)

    fun details(characterId: String): CharacterDetails =
        characters.find { it.summary.id == characterId } ?: characters.first()
}

val EmptyCharacterList = emptyList<CharacterSummary>()
