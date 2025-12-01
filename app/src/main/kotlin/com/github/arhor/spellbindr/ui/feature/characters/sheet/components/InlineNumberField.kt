package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun InlineNumberField(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Preview
@Composable
private fun InlineNumberFieldLightPreview() {
    InlineNumberFieldPreview(isDarkTheme = false)
}

@Preview
@Composable
private fun InlineNumberFieldDarkPreview() {
    InlineNumberFieldPreview(isDarkTheme = true)
}

@Composable
private fun InlineNumberFieldPreview(isDarkTheme: Boolean) {
    AppTheme(isDarkTheme = isDarkTheme) {
        InlineNumberField(
            label = "Initiative",
            value = "2",
            onValueChanged = {},
        )
    }
}
