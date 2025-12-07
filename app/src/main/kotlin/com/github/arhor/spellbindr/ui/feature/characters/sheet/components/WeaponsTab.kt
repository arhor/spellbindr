package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.predefined.DamageType
import com.github.arhor.spellbindr.ui.feature.characters.sheet.WeaponUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.WeaponsTabState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetCallbacks
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun WeaponsTab(
    weapons: WeaponsTabState,
    callbacks: CharacterSheetCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Weapons",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = callbacks.onAddWeaponClicked) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add weapon")
            }
        }

        if (weapons.weapons.isEmpty()) {
            Text(
                text = "No weapons yet. Add your first weapon using the + button.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(weapons.weapons, key = { it.id }) { weapon ->
                    WeaponListItem(
                        weapon = weapon,
                        onClick = { callbacks.onWeaponSelected(weapon.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WeaponListItem(
    weapon: WeaponUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = weapon.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = weapon.attackBonusLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = weapon.damageLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = weapon.damageType.name.lowercase().replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun WeaponsTabPreview() {
    AppTheme(isDarkTheme = false) {
        WeaponsTab(
            weapons = WeaponsTabState(
                weapons = listOf(
                    WeaponUiModel(
                        id = "1",
                        name = "Longsword",
                        attackBonusLabel = "ATK +7",
                        damageLabel = "DMG 1d8+4",
                        damageType = DamageType.SLASHING,
                    ),
                ),
            ),
            callbacks = CharacterSheetCallbacks(),
        )
    }
}
