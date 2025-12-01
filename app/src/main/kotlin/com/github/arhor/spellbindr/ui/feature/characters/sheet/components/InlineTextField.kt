package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun InlineTextField(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
    )
}

@Preview
@Composable
private fun InlineTextFieldLightPreview() {
    InlineTextFieldPreview(isDarkTheme = false)
}

@Preview
@Composable
private fun InlineTextFieldDarkPreview() {
    InlineTextFieldPreview(isDarkTheme = true)
}

@Composable
private fun InlineTextFieldPreview(isDarkTheme: Boolean) {
    AppTheme(isDarkTheme = isDarkTheme) {
        InlineTextField(
            label = "Notes",
            value = "Mage of the Arcane Circle",
            onValueChanged = {},
        )
    }
}
