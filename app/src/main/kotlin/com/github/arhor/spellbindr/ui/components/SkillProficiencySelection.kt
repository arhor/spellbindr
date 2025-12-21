package com.github.arhor.spellbindr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SkillProficiencySelection(
    skillsToChoose: Int = 2,
    skillsToChooseFrom: List<Skill> = emptyList(),
    skillProficiencies: List<Skill> = emptyList(),
) {
    val selectedSkills = remember { mutableStateListOf<Skill>() }
    skillsToChooseFrom.intersect(skillProficiencies)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Skill Proficiencies",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        GradientDivider(modifier = Modifier.padding(vertical = 16.dp))
        for (skill in Skill.entries) {
            SkillsListItem(
                skill = skill,
                selectedSkills = selectedSkills,
                skillProficiencies = skillProficiencies,
                skillsToChooseFrom = skillsToChooseFrom,
                skillsToChoose = skillsToChoose,
            )
        }

    }
}

@Composable
private fun SkillsListItem(
    skill: Skill,
    selectedSkills: SnapshotStateList<Skill>,
    skillProficiencies: List<Skill>,
    skillsToChooseFrom: List<Skill>,
    skillsToChoose: Int
) {
    val isSelected = skill in selectedSkills
    val hasProficiency = skill in skillProficiencies
    val canChooseSkill = skill in skillsToChooseFrom

    fun handleSkillClicked(isSelected: Boolean) {
        if (isSelected) {
            selectedSkills.remove(skill)
        } else {
            selectedSkills.add(skill)
        }
    }

    if (hasProficiency || canChooseSkill) {
        ListItem(
            headlineContent = {
                Text(
                    text = skill.displayName,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            },
            trailingContent = {
                Checkbox(
                    checked = isSelected || hasProficiency,
                    onCheckedChange = ::handleSkillClicked,
                    enabled = canChooseSkill && (isSelected || selectedSkills.size < skillsToChoose),
                )
            }
        )
    }
}

@Preview
@Composable
private fun SkillProficiencySelectionPreview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SkillProficiencySelection(
                skillProficiencies = listOf(
                    Skill.ANIMAL_HANDLING,
                    Skill.PERCEPTION,
                    Skill.SURVIVAL,
                ),
                skillsToChooseFrom = listOf(

                    Skill.ACROBATICS,
                    Skill.ATHLETICS,
                    Skill.HISTORY,
                    Skill.INSIGHT,
                    Skill.INTIMIDATION,
                    Skill.PERCEPTION,
                ),
                skillsToChoose = 2,
            )
        }
    }
}
