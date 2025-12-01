@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui.feature.characters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.data.model.predefined.Skill
import com.github.arhor.spellbindr.ui.AppTopBarConfig
import com.github.arhor.spellbindr.ui.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.WithAppTopBar
import com.github.arhor.spellbindr.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow

@Composable
fun CharacterEditorRoute(
    state: CharacterEditorUiState,
    events: Flow<CharacterEditorEvent>,
    callbacks: CharacterEditorCallbacks,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                CharacterEditorEvent.Saved -> onFinished()
                is CharacterEditorEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    CharacterEditorScreen(
        state = state,
        callbacks = callbacks,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
private fun CharacterEditorScreen(
    state: CharacterEditorUiState,
    callbacks: CharacterEditorCallbacks,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    WithAppTopBar(
        AppTopBarConfig(
            visible = true,
            title = {
                Text(
                    text = if (state.mode == EditorMode.Create) "New Character" else "Edit Character",
                )
            },
            navigation = AppTopBarNavigation.Back(callbacks.onBack),
            actions = {
                TextButton(
                    onClick = callbacks.onSave,
                    enabled = !state.isSaving && !state.isLoading,
                ) {
                    Text("Save")
                }
            },
        )
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    CharacterEditorForm(
                        state = state,
                        callbacks = callbacks,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun CharacterEditorForm(
    state: CharacterEditorUiState,
    callbacks: CharacterEditorCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.isSaving) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        if (state.saveError != null) {
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.errorContainer,
            ) {
                Text(
                    text = state.saveError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
        SectionCard(title = "Identity") {
            OutlinedTextField(
                value = state.name,
                onValueChange = callbacks.onNameChanged,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.nameError != null,
                supportingText = state.nameError?.let { error ->
                    { Text(error) }
                },
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.className,
                    onValueChange = callbacks.onClassChanged,
                    label = { Text("Class") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.level,
                    onValueChange = callbacks.onLevelChanged,
                    label = { Text("Level") },
                    modifier = Modifier.weight(1f),
                    isError = state.levelError != null,
                    supportingText = state.levelError?.let { error -> { Text(error) } },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.race,
                    onValueChange = callbacks.onRaceChanged,
                    label = { Text("Race") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.background,
                    onValueChange = callbacks.onBackgroundChanged,
                    label = { Text("Background") },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.alignment,
                    onValueChange = callbacks.onAlignmentChanged,
                    label = { Text("Alignment") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.experiencePoints,
                    onValueChange = callbacks.onExperienceChanged,
                    label = { Text("Experience") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                )
            }
        }
        SectionCard(title = "Ability Scores") {
            AbilityGrid(
                abilities = state.abilities,
                onAbilityChanged = callbacks.onAbilityChanged,
            )
        }
        SectionCard(title = "Proficiency & Inspiration") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.proficiencyBonus,
                    onValueChange = callbacks.onProficiencyBonusChanged,
                    label = { Text("Proficiency bonus") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                )
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = state.inspiration,
                        onCheckedChange = callbacks.onInspirationChanged,
                    )
                    Text(
                        text = "Inspiration",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
        SectionCard(title = "Combat") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.maxHitPoints,
                    onValueChange = callbacks.onMaxHpChanged,
                    label = { Text("Max HP") },
                    modifier = Modifier.weight(1f),
                    isError = state.maxHitPointsError != null,
                    supportingText = state.maxHitPointsError?.let { error -> { Text(error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = state.currentHitPoints,
                    onValueChange = callbacks.onCurrentHpChanged,
                    label = { Text("Current HP") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = state.temporaryHitPoints,
                    onValueChange = callbacks.onTemporaryHpChanged,
                    label = { Text("Temp HP") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.armorClass,
                    onValueChange = callbacks.onArmorClassChanged,
                    label = { Text("Armor Class") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = state.initiative,
                    onValueChange = callbacks.onInitiativeChanged,
                    label = { Text("Initiative") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.speed,
                    onValueChange = callbacks.onSpeedChanged,
                    label = { Text("Speed") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.hitDice,
                    onValueChange = callbacks.onHitDiceChanged,
                    label = { Text("Hit Dice") },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        SectionCard(title = "Saving Throws") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.savingThrows.forEach { entry ->
                    SavingThrowRow(
                        entry = entry,
                        onProficiencyChanged = { callbacks.onSavingThrowProficiencyChanged(entry.ability, it) },
                    )
                }
            }
        }
        SectionCard(title = "Skills") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.skills.forEach { entry ->
                    SkillRow(
                        entry = entry,
                        onProficiencyChanged = { callbacks.onSkillProficiencyChanged(entry.skill, it) },
                        onExpertiseChanged = { callbacks.onSkillExpertiseChanged(entry.skill, it) },
                    )
                }
            }
        }
        SectionCard(title = "Senses, Languages & Proficiencies") {
            MultiLineField(
                value = state.senses,
                onValueChange = callbacks.onSensesChanged,
                label = "Senses",
            )
            Spacer(modifier = Modifier.height(12.dp))
            MultiLineField(
                value = state.languages,
                onValueChange = callbacks.onLanguagesChanged,
                label = "Languages",
            )
            Spacer(modifier = Modifier.height(12.dp))
            MultiLineField(
                value = state.proficiencies,
                onValueChange = callbacks.onProficienciesChanged,
                label = "Proficiencies",
            )
        }
        SectionCard(title = "Attacks & Spellcasting") {
            MultiLineField(
                value = state.attacksAndCantrips,
                onValueChange = callbacks.onAttacksChanged,
                label = "Notes",
            )
        }
        SectionCard(title = "Features & Equipment") {
            MultiLineField(
                value = state.featuresAndTraits,
                onValueChange = callbacks.onFeaturesChanged,
                label = "Features & Traits",
            )
            Spacer(modifier = Modifier.height(12.dp))
            MultiLineField(
                value = state.equipment,
                onValueChange = callbacks.onEquipmentChanged,
                label = "Equipment",
            )
        }
        SectionCard(title = "Personality & Notes") {
            MultiLineField(
                value = state.personalityTraits,
                onValueChange = callbacks.onPersonalityTraitsChanged,
                label = "Personality traits",
            )
            Spacer(modifier = Modifier.height(12.dp))
            MultiLineField(
                value = state.ideals,
                onValueChange = callbacks.onIdealsChanged,
                label = "Ideals",
            )
            Spacer(modifier = Modifier.height(12.dp))
            MultiLineField(
                value = state.bonds,
                onValueChange = callbacks.onBondsChanged,
                label = "Bonds",
            )
            Spacer(modifier = Modifier.height(12.dp))
            MultiLineField(
                value = state.flaws,
                onValueChange = callbacks.onFlawsChanged,
                label = "Flaws",
            )
            Spacer(modifier = Modifier.height(12.dp))
            MultiLineField(
                value = state.notes,
                onValueChange = callbacks.onNotesChanged,
                label = "Notes",
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit),
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun AbilityGrid(
    abilities: List<AbilityFieldState>,
    onAbilityChanged: (Ability, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        abilities.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                rowItems.forEach { ability ->
                    AbilityCard(
                        state = ability,
                        onAbilityChanged = { value -> onAbilityChanged(ability.ability, value) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AbilityCard(
    state: AbilityFieldState,
    onAbilityChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = state.ability.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            OutlinedTextField(
                value = state.score,
                onValueChange = onAbilityChanged,
                label = { Text("Score") },
                singleLine = true,
                isError = state.error != null,
                supportingText = state.error?.let { error -> { Text(error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Text(
                text = "Modifier ${formatModifier(state.score.toIntOrNull())}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SavingThrowRow(
    entry: SavingThrowInputState,
    onProficiencyChanged: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = entry.ability.displayName, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = entry.proficient,
                    onCheckedChange = onProficiencyChanged,
                )
                Text(text = "Proficient")
            }
        }
        Text(
            text = formatModifier(entry.bonus),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun SkillRow(
    entry: SkillInputState,
    onProficiencyChanged: (Boolean) -> Unit,
    onExpertiseChanged: (Boolean) -> Unit,
) {
    Column {
        Text(text = entry.skill.displayName, fontWeight = FontWeight.Medium)
        Text(
            text = "Linked to ${entry.skill.ability.displayName}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = entry.proficient,
                    onCheckedChange = onProficiencyChanged,
                )
                Text(text = "Proficient")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = entry.expertise,
                    onCheckedChange = onExpertiseChanged,
                )
                Text(text = "Expertise")
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = formatModifier(entry.bonus),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun MultiLineField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
    )
}

private fun formatModifier(value: Int?): String = when (value) {
    null -> "—"
    in Int.MIN_VALUE..-1 -> value.toString()
    else -> "+$value"
}

@Stable
data class CharacterEditorCallbacks(
    val onBack: () -> Unit,
    val onNameChanged: (String) -> Unit,
    val onClassChanged: (String) -> Unit,
    val onLevelChanged: (String) -> Unit,
    val onRaceChanged: (String) -> Unit,
    val onBackgroundChanged: (String) -> Unit,
    val onAlignmentChanged: (String) -> Unit,
    val onExperienceChanged: (String) -> Unit,
    val onAbilityChanged: (Ability, String) -> Unit,
    val onProficiencyBonusChanged: (String) -> Unit,
    val onInspirationChanged: (Boolean) -> Unit,
    val onMaxHpChanged: (String) -> Unit,
    val onCurrentHpChanged: (String) -> Unit,
    val onTemporaryHpChanged: (String) -> Unit,
    val onArmorClassChanged: (String) -> Unit,
    val onInitiativeChanged: (String) -> Unit,
    val onSpeedChanged: (String) -> Unit,
    val onHitDiceChanged: (String) -> Unit,
    val onSavingThrowProficiencyChanged: (Ability, Boolean) -> Unit,
    val onSkillProficiencyChanged: (Skill, Boolean) -> Unit,
    val onSkillExpertiseChanged: (Skill, Boolean) -> Unit,
    val onSensesChanged: (String) -> Unit,
    val onLanguagesChanged: (String) -> Unit,
    val onProficienciesChanged: (String) -> Unit,
    val onAttacksChanged: (String) -> Unit,
    val onFeaturesChanged: (String) -> Unit,
    val onEquipmentChanged: (String) -> Unit,
    val onPersonalityTraitsChanged: (String) -> Unit,
    val onIdealsChanged: (String) -> Unit,
    val onBondsChanged: (String) -> Unit,
    val onFlawsChanged: (String) -> Unit,
    val onNotesChanged: (String) -> Unit,
    val onSave: () -> Unit,
)

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun CharacterEditorScreenPreview() {
    AppTheme {
        CharacterEditorScreen(
            state = previewEditorState(),
            callbacks = CharacterEditorCallbacks(
                onBack = {},
                onNameChanged = {},
                onClassChanged = {},
                onLevelChanged = {},
                onRaceChanged = {},
                onBackgroundChanged = {},
                onAlignmentChanged = {},
                onExperienceChanged = {},
                onAbilityChanged = { _, _ -> },
                onProficiencyBonusChanged = {},
                onInspirationChanged = {},
                onMaxHpChanged = {},
                onCurrentHpChanged = {},
                onTemporaryHpChanged = {},
                onArmorClassChanged = {},
                onInitiativeChanged = {},
                onSpeedChanged = {},
                onHitDiceChanged = {},
                onSavingThrowProficiencyChanged = { _, _ -> },
                onSkillProficiencyChanged = { _, _ -> },
                onSkillExpertiseChanged = { _, _ -> },
                onSensesChanged = {},
                onLanguagesChanged = {},
                onProficienciesChanged = {},
                onAttacksChanged = {},
                onFeaturesChanged = {},
                onEquipmentChanged = {},
                onPersonalityTraitsChanged = {},
                onIdealsChanged = {},
                onBondsChanged = {},
                onFlawsChanged = {},
                onNotesChanged = {},
                onSave = {},
            ),
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Composable
private fun previewEditorState(): CharacterEditorUiState = CharacterEditorUiState(
    name = "Astra Moonshadow",
    className = "Wizard",
    level = "7",
    race = "Half-elf",
    background = "Sage",
    alignment = "CG",
    experiencePoints = "34000",
    abilities = AbilityFieldState.defaults().mapIndexed { index, field ->
        field.copy(score = (10 + index * 2).toString())
    },
    proficiencyBonus = "3",
    inspiration = true,
    maxHitPoints = "52",
    currentHitPoints = "40",
    temporaryHitPoints = "5",
    armorClass = "15",
    initiative = "2",
    speed = "30 ft",
    hitDice = "7d6",
    savingThrows = SavingThrowInputState.defaults().map { it.copy(bonus = 2, proficient = true) },
    skills = SkillInputState.defaults().map { it.copy(bonus = 4) },
    senses = "Darkvision 60 ft",
    languages = "Common, Elvish, Primordial",
    proficiencies = "Arcana, Investigation, Perception",
    attacksAndCantrips = "Fire Bolt +9 to hit, 2d10 fire",
    featuresAndTraits = "Arcane Recovery (3/day)",
    equipment = "Quarterstaff, Spellbook",
    personalityTraits = "Curious and driven",
    ideals = "Knowledge is the path to power",
    bonds = "Protect the Luna Conservatory",
    flaws = "Overly cautious",
    notes = "Keep an eye on the mysterious amulet.",
)
