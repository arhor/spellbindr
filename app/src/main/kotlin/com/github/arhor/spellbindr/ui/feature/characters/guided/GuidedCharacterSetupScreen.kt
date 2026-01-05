package com.github.arhor.spellbindr.ui.feature.characters.guided

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun GuidedCharacterSetupRoute(
    onBack: () -> Unit,
) {
    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = "Guided setup",
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        GuidedCharacterSetupScreen(
            onBack = onBack,
        )
    }
}

@Composable
fun GuidedCharacterSetupScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Guided setup coming soon",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "We’re building a step-by-step character creator. In the meantime, you can go back and choose " +
                "manual entry to start crafting your hero.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onBack,
        ) {
            Text("Back to characters")
        }
    }
}

@Preview
@Composable
private fun GuidedCharacterSetupPreview() {
    AppTheme {
        GuidedCharacterSetupScreen(
            onBack = {},
        )
    }
}
