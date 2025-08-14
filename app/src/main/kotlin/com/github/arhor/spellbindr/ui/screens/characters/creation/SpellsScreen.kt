package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.github.arhor.spellbindr.ui.components.BaseScreenWithNavigation

@Composable
fun SpellsScreen(
    onPrev: () -> Unit,
    onNext: () -> Unit,
    @Suppress("UNUSED_PARAMETER")
    viewModel: CharacterCreationViewModel
) {
    BaseScreenWithNavigation(
        onPrev = onPrev,
        onNext = onNext
    ) {
        Text("Step 6 of 7: Spells", style = MaterialTheme.typography.titleLarge)

        // TODO: Implement spell selection for spellcasting classes


    }
}
