package com.github.arhor.spellbindr.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.AppThemeMode
import com.github.arhor.spellbindr.ui.AppTopBarConfig
import com.github.arhor.spellbindr.ui.WithAppTopBar
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    WithAppTopBar(
        AppTopBarConfig(
            visible = true,
            title = { Text(text = "Settings") },
        ),
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Card {
                ListItem(
                    headlineContent = { Text("Dark theme") },
                    supportingContent = {
                        val description = if (isDarkTheme) {
                            "Currently using the dark palette"
                        } else {
                            "Currently using the light palette"
                        }
                        Text(description)
                    },
                    trailingContent = {
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { onThemeToggle(it) },
                            enabled = state.loaded,
                        )
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val state = SettingsUiState(themeMode = AppThemeMode.DARK, loaded = true)
    AppTheme {
        SettingsScreen(
            state = state,
            isDarkTheme = true,
            onThemeToggle = {},
        )
    }
}
