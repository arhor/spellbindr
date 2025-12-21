package com.github.arhor.spellbindr.ui.feature.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SettingsRoute(
    vm: SettingsViewModel,
) {
    val state by vm.state.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Settings") },
            ),
        ),
    ) {
        SettingsScreen(
            state = state,
            onThemeModeSelected = vm::onThemeModeSelected,
        )
    }
}

@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
) {
    val state by vm.state.collectAsStateWithLifecycle()

    SettingsScreen(
        state = state,
        onThemeModeSelected = vm::onThemeModeSelected,
    )
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onThemeModeSelected: (ThemeMode?) -> Unit,
) {
    val effectiveIsDarkTheme = state.themeMode?.isDark ?: isSystemInDarkTheme()
    val themeOptions = listOf(
        ThemeOption(
            mode = null,
            title = "System default",
            description = if (effectiveIsDarkTheme) {
                "Follows system (currently dark)"
            } else {
                "Follows system (currently light)"
            },
        ),
        ThemeOption(
            mode = ThemeMode.LIGHT,
            title = "Light",
            description = "Always use the light palette",
        ),
        ThemeOption(
            mode = ThemeMode.DARK,
            title = "Dark",
            description = "Always use the dark palette",
        ),
    )

    Column(
        modifier = Modifier
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
            Column {
                themeOptions.forEach { option ->
                    ListItem(
                        modifier = Modifier
                            .selectable(
                                selected = state.themeMode == option.mode,
                                enabled = state.loaded,
                                onClick = { onThemeModeSelected(option.mode) },
                                role = Role.RadioButton,
                            ),
                        headlineContent = { Text(option.title) },
                        supportingContent = { Text(option.description) },
                        trailingContent = {
                            RadioButton(
                                selected = state.themeMode == option.mode,
                                onClick = { onThemeModeSelected(option.mode) },
                                enabled = state.loaded,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    val state = SettingsUiState(themeMode = ThemeMode.DARK, loaded = true)
    AppTheme {
        SettingsScreen(
            state = state,
            onThemeModeSelected = {},
        )
    }
}

private data class ThemeOption(
    val mode: ThemeMode?,
    val title: String,
    val description: String,
)
