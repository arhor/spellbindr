package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.characters.sheet.SkillsTabState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.github.arhor.spellbindr.utils.signed

@Composable
fun SkillsTab(
    skills: SkillsTabState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(skills.skills) { skill ->
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = skill.name,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = "${skill.abilityAbbreviation} • ${signed(skill.totalBonus)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (skill.expertise) {
                            AssistChip(onClick = {}, label = { Text("Expertise") })
                        } else if (skill.proficient) {
                            AssistChip(onClick = {}, label = { Text("Proficient") })
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SkillsTabLightPreview() {
    SkillsTabPreview(isDarkTheme = false)
}

@Preview
@Composable
private fun SkillsTabDarkPreview() {
    SkillsTabPreview(isDarkTheme = true)
}

@Composable
private fun SkillsTabPreview(isDarkTheme: Boolean) {
    AppTheme(isDarkTheme = isDarkTheme) {
        SkillsTab(skills = CharacterSheetPreviewData.skills)
    }
}
