package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun DetailCard(
    title: String,
    lines: List<String>,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (lines.isEmpty()) "No information yet" else lines.joinToString("\n\n"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun EditableDetailCard(
    title: String,
    primaryLabel: String,
    primaryValue: String,
    onPrimaryChanged: (String) -> Unit,
    secondaryLabel: String,
    secondaryValue: String,
    onSecondaryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            InlineTextField(
                label = primaryLabel,
                value = primaryValue,
                onValueChanged = onPrimaryChanged,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            InlineTextField(
                label = secondaryLabel,
                value = secondaryValue,
                onValueChanged = onSecondaryChanged,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun DetailCardPreview() {
    AppTheme {
        DetailCard(
            title = "Proficiencies & Equipment",
            lines = listOf(
                "Proficiencies:\nArcana, History, Insight",
                "Equipment:\nQuarterstaff, Spellbook",
            ),
        )
    }
}

@Preview
@Composable
private fun EditableDetailCardPreview() {
    AppTheme {
        EditableDetailCard(
            title = "Senses & Languages",
            primaryLabel = "Senses",
            primaryValue = CharacterSheetPreviewData.overview.senses,
            onPrimaryChanged = {},
            secondaryLabel = "Languages",
            secondaryValue = CharacterSheetPreviewData.overview.languages,
            onSecondaryChanged = {},
        )
    }
}
