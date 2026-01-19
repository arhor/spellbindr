package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun CharacterSheetTopBarActions(
    editMode: SheetEditMode,
    canEdit: Boolean,
    hasCharacter: Boolean,
    onEnterEdit: () -> Unit = {},
    onCancelEdit: () -> Unit = {},
    onSaveEdits: () -> Unit = {},
    onOverflowOpen: () -> Unit = {},
) {
    when (editMode) {
        SheetEditMode.View -> {
            TextButton(
                onClick = onEnterEdit,
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

        SheetEditMode.Edit -> {
            TextButton(onClick = onCancelEdit) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cancel")
            }
            TextButton(onClick = onSaveEdits) {
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

@Composable
@PreviewLightDark
private fun CharacterSheetTopBarActionsViewPreview() {
    AppTheme {
        CharacterSheetTopBarActions(
            editMode = SheetEditMode.View,
            canEdit = true,
            hasCharacter = true,
        )
    }
}

@Composable
@PreviewLightDark
private fun CharacterSheetTopBarActionsEditPreview() {
    AppTheme {
        CharacterSheetTopBarActions(
            editMode = SheetEditMode.Edit,
            canEdit = true,
            hasCharacter = true,
        )
    }
}
