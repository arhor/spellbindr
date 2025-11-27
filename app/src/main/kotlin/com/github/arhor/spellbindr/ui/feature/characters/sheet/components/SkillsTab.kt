package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.characters.sheet.SkillsTabState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.theme.AppTheme

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
            SkillRow(skill)
        }
    }
}

@Preview
@Composable
fun SkillsTabPreview() {
    AppTheme {
        SkillsTab(
            skills = CharacterSheetPreviewData.skills,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
