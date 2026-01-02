package com.github.arhor.spellbindr.ui.feature.compendium

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun CompendiumRoute(
    controller: NavHostController,
) {
    CompendiumScreen(
        onSectionClick = { controller.navigate(it) },
    )
}
