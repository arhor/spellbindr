package com.github.arhor.spellbindr.ui.feature.characters.creation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.github.arhor.spellbindr.ui.components.BaseScreenWithNavigation

@Composable
fun SkillsScreen(
    onPrev: () -> Unit,
    onNext: () -> Unit,
    @Suppress("UNUSED_PARAMETER")
    viewModel: CharacterCreationViewModel
) {
    BaseScreenWithNavigation(
        onPrev = onPrev,
        onNext = onNext
    ) {
        Text("Step 5 of 7: Skills", style = MaterialTheme.typography.titleLarge)

        // TODO: Implement skill selection based on class and background


    }
}
