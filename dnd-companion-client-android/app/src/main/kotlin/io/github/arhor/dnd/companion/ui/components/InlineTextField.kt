package io.github.arhor.dnd.companion.ui.components

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.github.arhor.dnd.companion.ui.theme.AppTheme

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

@PreviewLightDark
@Composable
private fun InlineTextFieldPreview() {
    AppTheme {
        InlineTextField(
            label = "Notes",
            value = "Mage of the Arcane Circle",
            onValueChanged = {},
        )
    }
}
