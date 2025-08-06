package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.components.BaseScreenWithNavigation

@Composable
fun SpellsScreen(
    onPrev: () -> Unit,
    onNext: () -> Unit,
    @Suppress("UNUSED_PARAMETER")
    viewModel: CharacterCreationViewModel
) {
    BaseScreenWithNavigation(
        padding = 24.dp,
        onPrev = onPrev,
        onNext = onNext
    ) {
        Text("Step 7 of 9: Spells", style = MaterialTheme.typography.titleLarge)

        // TODO: Implement spell selection for spellcasting classes


    }
}
