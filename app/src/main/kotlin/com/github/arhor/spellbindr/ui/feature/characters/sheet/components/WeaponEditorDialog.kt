package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.data.model.predefined.DamageType
import com.github.arhor.spellbindr.ui.feature.characters.sheet.WeaponEditorState
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun WeaponEditorDialog(
    editorState: WeaponEditorState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onAbilityChange: (Ability) -> Unit,
    onUseAbilityForDamageChange: (Boolean) -> Unit,
    onProficiencyChange: (Boolean) -> Unit,
    onDiceCountChange: (String) -> Unit,
    onDieSizeChange: (String) -> Unit,
    onDamageTypeChange: (DamageType) -> Unit,
    onDelete: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(if (editorState.id == null) "Add weapon" else "Edit weapon") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = editorState.name,
                    onValueChange = onNameChange,
                    label = { Text("Weapon name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ability", modifier = Modifier.padding(horizontal = 4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Ability.entries.forEach { ability ->
                            FilterChip(
                                selected = editorState.ability == ability,
                                onClick = { onAbilityChange(ability) },
                                label = { Text(ability.name) },
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editorState.damageDiceCount,
                        onValueChange = onDiceCountChange,
                        label = { Text("Dice count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = editorState.damageDieSize,
                        onValueChange = onDieSizeChange,
                        label = { Text("Die size") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                FilterChip(
                    selected = editorState.proficient,
                    onClick = { onProficiencyChange(!editorState.proficient) },
                    label = { Text("Proficient") },
                )
                FilterChip(
                    selected = editorState.useAbilityForDamage,
                    onClick = { onUseAbilityForDamageChange(!editorState.useAbilityForDamage) },
                    label = { Text("Add ability to damage") },
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Damage type", modifier = Modifier.padding(horizontal = 4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DamageType.entries.forEach { type ->
                            FilterChip(
                                selected = editorState.damageType == type,
                                onClick = { onDamageTypeChange(type) },
                                label = { Text(type.name) },
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (editorState.id != null) {
                    TextButton(onClick = { onDelete(editorState.id) }) {
                        Text("Delete")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = editorState.name.isNotBlank()) {
                Text("Save")
            }
        },
    )
}

@Preview
@Composable
private fun WeaponEditorDialogPreview() {
    AppTheme(isDarkTheme = false) {
        WeaponEditorDialog(
            editorState = WeaponEditorState(
                name = "Longsword",
                ability = Ability.STR,
                damageDiceCount = "1",
                damageDieSize = "8",
                proficient = true,
            ),
            onDismiss = {},
            onNameChange = {},
            onAbilityChange = {},
            onUseAbilityForDamageChange = {},
            onProficiencyChange = {},
            onDiceCountChange = {},
            onDieSizeChange = {},
            onDamageTypeChange = {},
            onDelete = {},
            onSave = {},
        )
    }
}
