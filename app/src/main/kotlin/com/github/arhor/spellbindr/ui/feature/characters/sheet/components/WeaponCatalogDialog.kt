package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.DamageType
import com.github.arhor.spellbindr.domain.model.EquipmentCategory
import com.github.arhor.spellbindr.ui.feature.characters.sheet.WeaponCatalogUiModel
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun WeaponCatalogDialog(
    catalog: List<WeaponCatalogUiModel>,
    onDismiss: () -> Unit,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    AlertDialog(
        modifier = modifier.testTag("WeaponCatalogDialog"),
        onDismissRequest = onDismiss,
        title = { Text("Weapon catalog") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.testTag("WeaponCatalogLoading"))
                    }
                } else if (catalog.isEmpty()) {
                    Text(
                        text = "No weapons available.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(catalog, key = { it.id }) { entry ->
                            val categoryLabel = entry.category?.toDisplayLabel() ?: "Weapon"
                            val damageLabel =
                                "${entry.damageDiceCount}d${entry.damageDieSize} ${entry.damageType.displayLabel()}"
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = entry.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                supportingContent = {
                                    Text("$categoryLabel • $damageLabel")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onItemSelected(entry.id) }
                                    .testTag("WeaponCatalogItem-${entry.id}"),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}

private fun EquipmentCategory.toDisplayLabel(): String =
    name.lowercase()
        .split("_")
        .joinToString(" ") { part -> part.replaceFirstChar(Char::titlecase) }

private fun DamageType.displayLabel(): String =
    name.lowercase().replaceFirstChar(Char::titlecase)

@PreviewLightDark
@Composable
private fun WeaponCatalogDialogPreview() {
    AppTheme {
        WeaponCatalogDialog(
            catalog = emptyList(),
            onDismiss = {},
            onItemSelected = {},
            isLoading = true,
        )
    }
}

@PreviewLightDark
@Composable
private fun WeaponCatalogDialogLoadedPreview() {
    AppTheme {
        WeaponCatalogDialog(
            catalog = listOf(
                WeaponCatalogUiModel(
                    id = "longsword",
                    name = "Longsword",
                    category = EquipmentCategory.MARTIAL,
                    categories = setOf(EquipmentCategory.MARTIAL, EquipmentCategory.MELEE),
                    damageDiceCount = 1,
                    damageDieSize = 8,
                    damageType = DamageType.SLASHING,
                ),
                WeaponCatalogUiModel(
                    id = "shortbow",
                    name = "Shortbow",
                    category = EquipmentCategory.SIMPLE,
                    categories = setOf(EquipmentCategory.SIMPLE, EquipmentCategory.RANGED),
                    damageDiceCount = 1,
                    damageDieSize = 6,
                    damageType = DamageType.PIERCING,
                ),
            ),
            onDismiss = {},
            onItemSelected = {},
        )
    }
}
