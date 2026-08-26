package com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.skills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SkillUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SkillsTabState
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.github.arhor.spellbindr.utils.signed

@Composable
fun SkillsTab(
    state: SkillsTabState,
    modifier: Modifier = Modifier,
) {
    val skillsByAbility = remember(state.skills) {
        state.skills
            .groupBy { it.abilityAbbreviation }
            .filterValues { it.isNotEmpty() }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        skillsByAbility.forEach { (ability, skills) ->
            AbilitySkillsSection(
                title = ability,
                skills = skills,
            )
        }
    }
}

@Composable
private fun AbilitySkillsSection(
    title: String,
    skills: List<SkillUiModel>,
    modifier: Modifier = Modifier,
) {
    val (leftColumn, rightColumn) = remember(skills) {
        val splitIndex = (skills.size + 1) / 2
        skills.take(splitIndex) to skills.drop(splitIndex)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                SkillsColumn(
                    skills = leftColumn,
                    modifier = Modifier.weight(1f),
                )
                SkillsColumn(
                    skills = rightColumn,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SkillsColumn(
    skills: List<SkillUiModel>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        skills.forEach { skill ->
            SkillRow(skill = skill)
        }
    }
}

@Composable
private fun SkillRow(
    skill: SkillUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = skill.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkillProficiencyBadge(
                proficient = skill.proficient,
                expertise = skill.expertise,
            )
            Text(
                text = signed(skill.totalBonus),
                modifier = Modifier.widthIn(min = 26.dp),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun SkillProficiencyBadge(
    proficient: Boolean,
    expertise: Boolean,
    modifier: Modifier = Modifier,
) {
    val badgeText = when {
        expertise -> "E"
        proficient -> "P"
        else -> null
    } ?: return

    Surface(
        modifier = modifier.semantics {
            contentDescription = if (expertise) "Expertise" else "Proficient"
        },
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = badgeText,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@PreviewLightDark
@Composable
private fun SkillsTabPreview() {
    AppTheme {
        SkillsTab(
            state = CharacterSheetPreviewData.skills,
        )
    }
}
