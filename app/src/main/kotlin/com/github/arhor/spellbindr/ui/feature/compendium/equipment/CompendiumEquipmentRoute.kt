package com.github.arhor.spellbindr.ui.feature.compendium.equipment

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.arhor.spellbindr.ui.feature.compendium.common.CompendiumPlaceholderRoute
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun CompendiumEquipmentRoute(onBack: () -> Unit) {
    CompendiumPlaceholderRoute(title = "Equipment", onBack = onBack)
}

@Preview
@Composable
private fun CompendiumEquipmentRoutePreview() {
    AppTheme {
        CompendiumEquipmentRoute(onBack = {})
    }
}
