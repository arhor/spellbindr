package com.github.arhor.spellbindr.ui.feature.characters.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.characters.list.model.CreateCharacterMode

@Composable
fun BoxScope.CreateCharacterDialog(
    onCreateCharacter: (CreateCharacterMode) -> Unit,
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    fun handleDialogAction(mode: CreateCharacterMode) {
        showCreateDialog = false
        onCreateCharacter(mode)
    }

    FloatingActionButton(
        onClick = { showCreateDialog = true },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp),
    ) {
        Icon(Icons.Default.Add, contentDescription = "Create character")
    }
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(text = "Create character") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Choose how you’d like to start your new hero.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    CreateCharacterOption(
                        title = "Guided setup",
                        description = "Follow step-by-step prompts to build a character with recommended defaults.",
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = { handleDialogAction(CreateCharacterMode.GuidedSetup) },
                    )
                    CreateCharacterOption(
                        title = "Manual entry",
                        description = "Fill in every detail yourself for full control over abilities, gear, and notes.",
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.EditNote,
                                contentDescription = null,
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = { handleDialogAction(CreateCharacterMode.ManualEntry) },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
