package com.github.arhor.spellbindr.ui.feature.character.guided.model

import androidx.compose.runtime.Immutable

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
