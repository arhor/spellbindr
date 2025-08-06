package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.github.arhor.spellbindr.ui.components.BaseScreenWithNavigation

@Composable
fun EquipmentScreen(
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    BaseScreenWithNavigation(
        onPrev = onPrev,
        onNext = onNext
    ) {
        Text("Step 6 of 9: Equipment", style = MaterialTheme.typography.titleLarge)

        // TODO: Implement equipment selection based on class and background


    }
}
