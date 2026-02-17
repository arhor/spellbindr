package com.github.arhor.spellbindr.ui.feature.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.ui.components.ErrorMessage
import com.github.arhor.spellbindr.ui.components.LoadingIndicator
import com.github.arhor.spellbindr.ui.feature.settings.model.ThemeOption
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    dispatch: SettingsDispatch = {},
    snackbarHostState: SnackbarHostState,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (state) {
            is SettingsUiState.Loading -> {
                LoadingIndicator()
            }

            is SettingsUiState.Content -> {
                SettingsContent(
                    state = state,
                    dispatch = dispatch,
                )
            }

            is SettingsUiState.Error -> {
                ErrorMessage(state.errorMessage)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState.Content,
    dispatch: SettingsDispatch,
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
                                onClick = { dispatch(SettingsIntent.ThemeModeSelected(option.mode)) },
                                role = Role.RadioButton,
                            ),
                        headlineContent = { Text(option.title) },
                        supportingContent = { Text(option.description) },
                        trailingContent = {
                            RadioButton(
                                selected = state.themeMode == option.mode,
                                onClick = { dispatch(SettingsIntent.ThemeModeSelected(option.mode)) },
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            state = SettingsUiState.Content(
                themeMode = null,
            ),
            snackbarHostState = SnackbarHostState(),
        )
    }
}
