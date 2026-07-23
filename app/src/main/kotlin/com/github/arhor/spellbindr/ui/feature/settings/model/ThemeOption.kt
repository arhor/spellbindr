package com.github.arhor.spellbindr.ui.feature.settings.model

import com.github.arhor.spellbindr.domain.model.ThemeMode

data class ThemeOption(
    val mode: ThemeMode?,
    val title: String,
    val description: String,
)
