package com.github.arhor.spellbindr.ui.feature.compendium.features

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.arhor.spellbindr.ui.feature.compendium.common.CompendiumPlaceholderRoute
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun CompendiumFeaturesRoute(onBack: () -> Unit) {
    CompendiumPlaceholderRoute(title = "Features", onBack = onBack)
}

@Preview
@Composable
private fun CompendiumFeaturesRoutePreview() {
    AppTheme {
        CompendiumFeaturesRoute(onBack = {})
    }
}
