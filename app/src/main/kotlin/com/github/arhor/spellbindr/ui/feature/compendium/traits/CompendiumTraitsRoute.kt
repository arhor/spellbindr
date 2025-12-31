package com.github.arhor.spellbindr.ui.feature.compendium.traits

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.arhor.spellbindr.ui.feature.compendium.common.CompendiumPlaceholderRoute
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun CompendiumTraitsRoute(onBack: () -> Unit) {
    CompendiumPlaceholderRoute(title = "Traits", onBack = onBack)
}

@Preview
@Composable
private fun CompendiumTraitsRoutePreview() {
    AppTheme {
        CompendiumTraitsRoute(onBack = {})
    }
}
