package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScores

@Immutable
enum class GuidedStep(val title: String) {
    BASICS("Basics"),
    CLASS("Class"),
    CLASS_CHOICES("Class choices"),
    RACE("Race"),
    BACKGROUND("Background"),
    ABILITY_METHOD("Ability scores"),
    ABILITY_ASSIGN("Assign ability scores"),
    SKILLS_PROFICIENCIES("Skills & proficiencies"),
    EQUIPMENT("Equipment"),
    SPELLS("Spells"),
    REVIEW("Review"),
}

@Immutable
enum class AbilityScoreMethod(val label: String) {
    STANDARD_ARRAY("Standard array"),
    POINT_BUY("Point buy"),
}

@Immutable
data class GuidedValidationIssue(
    val severity: Severity,
    val message: String,
) {
    enum class Severity { ERROR, WARNING }
}

@Immutable
data class GuidedValidationResult(
    val issues: List<GuidedValidationIssue>,
) {
    val hasErrors: Boolean = issues.any { it.severity == GuidedValidationIssue.Severity.ERROR }
}

@Immutable
data class GuidedCharacterPreview(
    val abilityScores: AbilityScores,
    val maxHitPoints: Int,
    val armorClass: Int,
    val speed: Int,
    val languagesCount: Int,
    val proficienciesCount: Int,
)

internal val StandardArray: List<Int> = listOf(15, 14, 13, 12, 10, 8)

internal fun defaultStandardArrayAssignments(): Map<AbilityId, Int?> =
    AbilityIds.standardOrder.associateWith { null }

internal fun defaultPointBuyScores(): Map<AbilityId, Int> =
    AbilityIds.standardOrder.associateWith { 8 }

internal fun pointBuyCost(score: Int): Int = when (score) {
    8 -> 0
    9 -> 1
    10 -> 2
    11 -> 3
    12 -> 4
    13 -> 5
    14 -> 7
    15 -> 9
    else -> Int.MAX_VALUE
}
