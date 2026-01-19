package com.github.arhor.spellbindr.ui.feature.characters.sheet.model

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.Skill

@Immutable
data class SkillUiModel(
    val id: Skill,
    val name: String,
    val abilityAbbreviation: String,
    val totalBonus: Int,
    val proficient: Boolean,
    val expertise: Boolean,
)
