package com.github.arhor.spellbindr.ui.feature.compendium.classes

import androidx.compose.runtime.Composable
import com.github.arhor.spellbindr.ui.feature.compendium.common.CompendiumPlaceholderRoute

@Composable
fun CompendiumClassesRoute(onBack: () -> Unit) {
    CompendiumPlaceholderRoute(title = "Classes", onBack = onBack)
}
