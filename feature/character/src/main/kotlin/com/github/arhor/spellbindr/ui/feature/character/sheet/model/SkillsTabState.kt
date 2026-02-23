package com.github.arhor.spellbindr.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
data class SkillsTabState(
    val skills: List<SkillUiModel>,
)
