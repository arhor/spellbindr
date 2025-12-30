package com.github.arhor.spellbindr.ui.feature.compendium.equipment

import androidx.compose.runtime.Composable
import com.github.arhor.spellbindr.ui.feature.compendium.common.CompendiumPlaceholderRoute

@Composable
fun CompendiumEquipmentRoute(onBack: () -> Unit) {
    CompendiumPlaceholderRoute(title = "Equipment", onBack = onBack)
}
