package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetCallbacks
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun CharacterSheetTopBarTitle(
    name: String?,
    subtitle: String?,
) {
    if (name == null) {
        Text(
            text = "Character",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    } else {
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun CharacterSheetTopBarActions(
    editMode: SheetEditMode,
    canEdit: Boolean,
    hasCharacter: Boolean,
    callbacks: CharacterSheetCallbacks,
    onOverflowOpen: () -> Unit,
) {
    when (editMode) {
        SheetEditMode.View -> {
            TextButton(
                onClick = callbacks.onEnterEdit,
                enabled = canEdit,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit")
            }
            IconButton(onClick = onOverflowOpen, enabled = hasCharacter) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More")
            }
        }

        SheetEditMode.Editing -> {
            TextButton(onClick = callbacks.onCancelEdit) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cancel")
            }
            TextButton(onClick = callbacks.onSaveEdits) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save")
            }
        }
    }
}

@Preview
@Composable
private fun CharacterSheetTopBarTitlePreview() {
    AppTheme {
        CharacterSheetTopBarTitle(
            name = CharacterSheetPreviewData.header.name,
            subtitle = CharacterSheetPreviewData.header.subtitle,
        )
    }
}

@Preview
@Composable
private fun CharacterSheetTopBarTitleLoadingPreview() {
    AppTheme {
        CharacterSheetTopBarTitle(name = null, subtitle = null)
    }
}

@Preview
@Composable
private fun CharacterSheetTopBarActionsViewPreview() {
    AppTheme {
        CharacterSheetTopBarActions(
            editMode = SheetEditMode.View,
            canEdit = true,
            hasCharacter = true,
            callbacks = CharacterSheetCallbacks(),
            onOverflowOpen = {},
        )
    }
}

@Preview
@Composable
private fun CharacterSheetTopBarActionsEditingPreview() {
    AppTheme {
        CharacterSheetTopBarActions(
            editMode = SheetEditMode.Editing,
            canEdit = true,
            hasCharacter = true,
            callbacks = CharacterSheetCallbacks(),
            onOverflowOpen = {},
        )
    }
}
