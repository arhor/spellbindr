package io.github.arhor.dnd.companion.ui.feature.settings.model

import io.github.arhor.dnd.companion.domain.model.ThemeMode

data class ThemeOption(
    val mode: ThemeMode?,
    val title: String,
    val description: String,
)
