package com.github.arhor.spellbindr.ui.feature.compendium

import androidx.compose.runtime.Composable

@Composable
fun CompendiumRoute(
    onSectionClick: (CompendiumSections) -> Unit,
) {
    CompendiumScreen(
        dispatch = { intent ->
            when (intent) {
                is CompendiumIntent.SectionClicked -> onSectionClick(intent.section)
            }
        },
    )
}
