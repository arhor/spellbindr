package com.github.arhor.spellbindr.ui.feature.compendium.classes

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.arhor.spellbindr.ui.feature.compendium.common.CompendiumPlaceholderRoute
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun CompendiumClassesRoute(onBack: () -> Unit) {
    CompendiumPlaceholderRoute(title = "Classes", onBack = onBack)
}

@Preview
@Composable
private fun CompendiumClassesRoutePreview() {
    AppTheme {
        CompendiumClassesRoute(onBack = {})
    }
}
