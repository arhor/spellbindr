package com.github.arhor.spellbindr.domain.model

data class CharacterEditorInput(
    val characterId: String? = null,
    val name: String = "",
    val level: String = "1",
    val className: String = "",
    val race: String = "",
    val background: String = "",
    val alignment: String = "",
    val experiencePoints: String = "",
    val abilities: List<AbilityScoreInput> = AbilityScoreInput.defaults(),
    val proficiencyBonus: String = "2",
    val inspiration: Boolean = false,
    val maxHitPoints: String = "1",
    val currentHitPoints: String = "1",
    val temporaryHitPoints: String = "",
    val armorClass: String = "",
    val initiative: String = "",
    val speed: String = "30 ft",
    val hitDice: String = "",
    val savingThrows: List<SavingThrowInput> = SavingThrowInput.defaults(),
    val skills: List<SkillProficiencyInput> = SkillProficiencyInput.defaults(),
    val senses: String = "",
    val languages: String = "",
    val proficiencies: String = "",
    val attacksAndCantrips: String = "",
    val featuresAndTraits: String = "",
    val equipment: String = "",
    val personalityTraits: String = "",
    val ideals: String = "",
    val bonds: String = "",
    val flaws: String = "",
    val notes: String = "",
)

data class AbilityScoreInput(
    val abilityId: String,
    val score: String = "10",
) {
    companion object {
        fun defaults(): List<AbilityScoreInput> = listOf("STR", "DEX", "CON", "INT", "WIS", "CHA").map { abilityId ->
            AbilityScoreInput(abilityId = abilityId, score = "10")
        }
    }
}

data class SavingThrowInput(
    val abilityId: String,
    val proficient: Boolean = false,
) {
    companion object {
        fun defaults(): List<SavingThrowInput> = listOf("STR", "DEX", "CON", "INT", "WIS", "CHA").map { abilityId ->
            SavingThrowInput(abilityId = abilityId)
        }
    }
}

data class SkillProficiencyInput(
    val skill: Skill,
    val proficient: Boolean = false,
    val expertise: Boolean = false,
) {
    companion object {
        fun defaults(): List<SkillProficiencyInput> = Skill.entries.map { skill ->
            SkillProficiencyInput(skill = skill)
        }
    }
}

data class CharacterEditorDerivedBonuses(
    val savingThrows: List<SavingThrowBonus>,
    val skills: List<SkillBonus>,
)

data class SavingThrowBonus(
    val abilityId: String,
    val bonus: Int,
)

data class SkillBonus(
    val skill: Skill,
    val bonus: Int,
)
