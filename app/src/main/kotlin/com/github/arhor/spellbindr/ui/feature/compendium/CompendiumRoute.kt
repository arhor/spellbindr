package com.github.arhor.spellbindr.ui.feature.compendium

import androidx.compose.runtime.Composable

@Composable
fun CompendiumRoute(
    onSectionClick: (CompendiumSections) -> Unit,
) {
    CompendiumScreen(
        onSectionClick = onSectionClick,
    )
}
