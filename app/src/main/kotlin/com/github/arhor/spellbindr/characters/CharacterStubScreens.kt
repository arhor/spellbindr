@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.characters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.AppTopBarConfig
import com.github.arhor.spellbindr.ui.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.ProvideTopBar
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun CharacterCreationScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StubScreen(
        title = "Create character",
        body = "TODO: Character creation flow coming soon.",
        modifier = modifier,
        onBack = onBack,
    )
}

@Composable
fun CharacterLevelUpScreen(
    characterId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StubScreen(
        title = "Level up",
        body = "TODO: Level-up planner for $characterId.",
        modifier = modifier,
        onBack = onBack,
    )
}

@Composable
private fun StubScreen(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        ProvideTopBar(
            AppTopBarConfig(
                visible = true,
                title = { Text(title) },
                navigation = AppTopBarNavigation.Back(onBack),
            )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = body,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Use the FAB/back action to return to the character list.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
        }
    }
}

@Preview
@Composable
private fun CharacterCreationPreview() {
    AppTheme {
        CharacterCreationScreen(onBack = {})
    }
}

@Preview
@Composable
private fun CharacterLevelUpPreview() {
    AppTheme {
        CharacterLevelUpScreen(
            characterId = "astra",
            onBack = {},
        )
    }
}
