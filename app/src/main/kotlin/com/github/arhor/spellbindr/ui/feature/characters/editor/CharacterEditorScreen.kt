@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui.feature.characters.editor

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.displayName
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun CharacterEditorRoute(
    vm: CharacterEditorViewModel,
    onBack: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val title = if (state.mode == EditorMode.Create) "New Character" else "Edit Character"

    val callbacks = remember(vm, onBack) {
        CharacterEditorCallbacks(
            onBack = onBack,
            onAction = vm::onAction,
            onSave = vm::onSaveClicked,
        )
    }

    LaunchedEffect(vm.events) {
        vm.events.collect { event ->
            when (event) {
                CharacterEditorEvent.Saved -> onFinished()
                is CharacterEditorEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = title,
                navigation = AppTopBarNavigation.Back(onBack),
                actions = {
                    TextButton(
                        onClick = callbacks.onSave,
                        enabled = !state.isSaving && !state.isLoading,
                    ) {
                        Text("Save")
                    }
                },
            ),
        ),
    ) {
        CharacterEditorScreen(
            state = state,
            callbacks = callbacks,
            snackbarHostState = snackbarHostState,
            modifier = modifier,
        )
    }
}

@Composable
private fun CharacterEditorScreen(
    state: CharacterEditorUiState,
    callbacks: CharacterEditorCallbacks,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
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
                onValueChange = { callbacks.onAction(CharacterEditorAction.NameChanged(it)) },
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
                    onValueChange = { callbacks.onAction(CharacterEditorAction.ClassChanged(it)) },
                    label = { Text("Class") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.level,
                    onValueChange = { callbacks.onAction(CharacterEditorAction.LevelChanged(it)) },
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
                    onValueChange = { callbacks.onAction(CharacterEditorAction.RaceChanged(it)) },
                    label = { Text("Race") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.background,
                    onValueChange = { callbacks.onAction(CharacterEditorAction.BackgroundChanged(it)) },
                    label = { Text("Background") },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.alignment,
                    onValueChange = { callbacks.onAction(CharacterEditorAction.AlignmentChanged(it)) },
                    label = { Text("Alignment") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.experiencePoints,
                    onValueChange = { callbacks.onAction(CharacterEditorAction.ExperienceChanged(it)) },
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
                onAbilityChanged = { ability, value ->
                    callbacks.onAction(CharacterEditorAction.AbilityChanged(ability, value))
                },
            )
        }
        SectionCard(title = "Proficiency & Inspiration") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.proficiencyBonus,
                    onValueChange = { callbacks.onAction(CharacterEditorAction.ProficiencyBonusChanged(it)) },
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
                        onCheckedChange = { callbacks.onAction(CharacterEditorAction.InspirationChanged(it)) },
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
                    onValueChange = { callbacks.onAction(CharacterEditorAction.MaxHpChanged(it)) },
                    label = { Text("Max HP") },
                    modifier = Modifier.weight(1f),
                    isError = state.maxHitPointsError != null,
                    supportingText = state.maxHitPointsError?.let { error -> { Text(error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = state.currentHitPoints,
                    onValueChange = { callbacks.onAction(CharacterEditorAction.CurrentHpChanged(it)) },
                    label = { Text("Current HP") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = state.temporaryHitPoints,
                    onValueChange = { callbacks.onAction(CharacterEditorAction.TemporaryHpChanged(it)) },
                    label = { Text("Temp HP") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.armorClass,
                    onValueChange = { callbacks.onAction(CharacterEditorAction.ArmorClassChanged(it)) },
                    label = { Text("Armor Class") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = state.initiative,
                    onValueChange = { callbacks.onAction(CharacterEditorAction.InitiativeChanged(it)) },
                    label = { Text("Initiative") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.speed,
                    onValueChange = { callbacks.onAction(CharacterEditorAction.SpeedChanged(it)) },
                    label = { Text("Speed") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.hitDice,
                    onValueChange = { callbacks.onAction(CharacterEditorAction.HitDiceChanged(it)) },
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
                        onProficiencyChanged = {
                            callbacks.onAction(
                                CharacterEditorAction.SavingThrowProficiencyChanged(entry.abilityId, it),
                            )
                        },
                    )
                }
            }
        }
        SectionCard(title = "Skills") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.skills.forEach { entry ->
                    SkillRow(
                        entry = entry,
                        onProficiencyChanged = {
                            callbacks.onAction(CharacterEditorAction.SkillProficiencyChanged(entry.skill, it))
                        },
                        onExpertiseChanged = {
                            callbacks.onAction(CharacterEditorAction.SkillExpertiseChanged(entry.skill, it))
                        },
                    )
                }
            }
        }
        SectionCard(title = "Senses, Languages & Proficiencies") {
            MultiLineField(
                value = state.senses,
                onValueChange = { callbacks.onAction(CharacterEditorAction.SensesChanged(it)) },
                label = "Senses",
            )
            Spacer(modifier = Modifier.height(12.dp))
            MultiLineField(
                value = state.languages,
                onValueChange = { callbacks.onAction(CharacterEditorAction.LanguagesChanged(it)) },
                label = "Languages",
            )
            Spacer(modifier = Modifier.height(12.dp))
            MultiLineField(
                value = state.proficiencies,
                onValueChange = { callbacks.onAction(CharacterEditorAction.ProficienciesChanged(it)) },
                label = "Proficiencies",
            )
        }
        SectionCard(title = "Attacks & Spellcasting") {
            MultiLineField(
                value = state.attacksAndCantrips,
                onValueChange = { callbacks.onAction(CharacterEditorAction.AttacksChanged(it)) },
                label = "Notes",
            )
        }
        SectionCard(title = "Features & Equipment") {
            MultiLineField(
                value = state.featuresAndTraits,
                onValueChange = { callbacks.onAction(CharacterEditorAction.FeaturesChanged(it)) },
                label = "Features & Traits",
            )
            Spacer(modifier = Modifier.height(12.dp))
            MultiLineField(
                value = state.equipment,
                onValueChange = { callbacks.onAction(CharacterEditorAction.EquipmentChanged(it)) },
                label = "Equipment",
            )
        }
        SectionCard(title = "Personality & Notes") {
            MultiLineField(
                value = state.personalityTraits,
                onValueChange = { callbacks.onAction(CharacterEditorAction.PersonalityTraitsChanged(it)) },
                label = "Personality traits",
            )
            Spacer(modifier = Modifier.height(12.dp))
            MultiLineField(
                value = state.ideals,
                onValueChange = { callbacks.onAction(CharacterEditorAction.IdealsChanged(it)) },
                label = "Ideals",
            )
            Spacer(modifier = Modifier.height(12.dp))
            MultiLineField(
                value = state.bonds,
                onValueChange = { callbacks.onAction(CharacterEditorAction.BondsChanged(it)) },
                label = "Bonds",
            )
            Spacer(modifier = Modifier.height(12.dp))
            MultiLineField(
                value = state.flaws,
                onValueChange = { callbacks.onAction(CharacterEditorAction.FlawsChanged(it)) },
                label = "Flaws",
            )
            Spacer(modifier = Modifier.height(12.dp))
            MultiLineField(
                value = state.notes,
                onValueChange = { callbacks.onAction(CharacterEditorAction.NotesChanged(it)) },
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
    onAbilityChanged: (AbilityId, String) -> Unit,
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
                        onAbilityChanged = { value -> onAbilityChanged(ability.abilityId, value) },
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
                text = state.abilityId.displayName(),
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
            Text(text = entry.abilityId.displayName(), fontWeight = FontWeight.Medium)
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
            text = "Linked to ${entry.skill.abilityAbbreviation}",
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
    val onAction: (CharacterEditorAction) -> Unit,
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
                onAction = {},
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
    savingThrows = SavingThrowInputState.defaults()
        .map { it.copy(bonus = 2, proficient = true) },
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
