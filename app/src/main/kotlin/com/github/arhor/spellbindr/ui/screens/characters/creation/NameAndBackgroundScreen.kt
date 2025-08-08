package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.Choice
import com.github.arhor.spellbindr.ui.components.BaseScreenWithNavigation
import com.github.arhor.spellbindr.ui.components.GradientDivider
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationEvent.BackgroundEquipmentChanged
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationEvent.BondsChanged
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationEvent.FlawsChanged
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationEvent.IdealsChanged
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationEvent.LanguagesChanged
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationEvent.PersonalityTraitsChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameAndBackgroundScreen(
    onNext: () -> Unit,
    viewModel: CharacterCreationViewModel,
) {
    val state by viewModel.state.collectAsState()
    val background = state.background

    BaseScreenWithNavigation(
        onNext = onNext
    ) {
        Text("Step 1 of 7: Background", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = state.characterName,
            onValueChange = { viewModel.handleEvent(CharacterCreationEvent.NameChanged(it)) },
            label = { Text("Character Name") },
            modifier = Modifier.fillMaxWidth()
        )

        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = state.background?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Background") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                for (bg in state.backgrounds) {
                    DropdownMenuItem(
                        text = { Text(bg.name) },
                        onClick = {
                            viewModel.handleEvent(CharacterCreationEvent.BackgroundChanged(bg.name))
                            expanded = false
                        }
                    )
                }
            }
        }

        if (background != null) {

            Text(text = background.feature.name, style = MaterialTheme.typography.titleMedium)

            background.feature.desc.forEach { line ->
                Text(text = line, style = MaterialTheme.typography.bodyMedium)
            }

            if (background.languageChoice is Choice.ResourceListChoice) {
                GradientDivider()
                ChoiceSection(
                    title = "Languages",
                    choice = background.languageChoice,
                    selection = state.selectedLanguages,
                    onSelectionChanged = { viewModel.handleEvent(LanguagesChanged(it)) },
                    externalOptions = state.languages.map { it.id to it.name },
                )
            }

            if (background.equipmentChoice != null) {
                GradientDivider()
                ChoiceSection(
                    title = "Background Equipment",
                    choice = background.equipmentChoice,
                    selection = state.selectedBackgroundEquipment,
                    onSelectionChanged = { viewModel.handleEvent(BackgroundEquipmentChanged(it)) },
                    externalOptions = state.availableBackgroundEquipment.map { it.id to it.name },
                )
            }
            GradientDivider()

            ChoiceSection(
                title = "Personality Traits",
                choice = background.personalityTraits,
                selection = state.selectedPersonalityTraits,
                onSelectionChanged = { viewModel.handleEvent(PersonalityTraitsChanged(it)) }
            )
            GradientDivider()

            ChoiceSection(
                title = "Ideals",
                choice = background.ideals,
                selection = state.selectedIdeals,
                onSelectionChanged = { viewModel.handleEvent(IdealsChanged(it)) }
            )
            GradientDivider()

            ChoiceSection(
                title = "Bonds",
                choice = background.bonds,
                selection = state.selectedBonds,
                onSelectionChanged = { viewModel.handleEvent(BondsChanged(it)) }
            )
            GradientDivider()

            ChoiceSection(
                title = "Flaws",
                choice = background.flaws,
                selection = state.selectedFlaws,
                onSelectionChanged = { viewModel.handleEvent(FlawsChanged(it)) }
            )
        }
    }
}

@Composable
private fun ChoiceSection(
    title: String,
    choice: Choice,
    selection: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    externalOptions: List<Pair<String, String>>? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)

        if (externalOptions != null) {
            val labelPrefix = when {
                title.endsWith("s") -> title.dropLast(1)
                else -> title
            }
            PairOptionsSelection(
                choose = choice.choose,
                options = externalOptions,
                selected = selection,
                labelPrefix = labelPrefix,
                onSelectionChanged = onSelectionChanged,
            )
        } else when (choice) {
            is Choice.OptionsArrayChoice -> {
                val options = choice.from
                val labelPrefix = when {
                    title == "Personality Traits" -> "Personality Trait"
                    title.endsWith("s") -> title.dropLast(1)
                    else -> title
                }
                OptionsSelection(
                    choose = choice.choose,
                    options = options,
                    selected = selection,
                    labelPrefix = labelPrefix,
                    onSelectionChanged = onSelectionChanged,
                )
            }

            is Choice.IdealChoice -> {
                val options = choice.from.map { it.desc }
                OptionsSelection(
                    choose = choice.choose,
                    options = options,
                    selected = selection,
                    labelPrefix = "Ideal",
                    onSelectionChanged = onSelectionChanged,
                )
            }

            else -> {
                Text(text = "Unsupported choice type")
            }
        }
    }
}

// LanguageSelection and LanguageDropdown were redundant with ChoiceSection and
// are removed in favor of a generic IdLabelDropdown used by PairOptionsSelection.

// EquipmentSelection and EquipmentDropdown were redundant with ChoiceSection and
// are removed in favor of a generic IdLabelDropdown used by PairOptionsSelection.

@Composable
private fun OptionsSelection(
    choose: Int,
    options: List<String>,
    selected: List<String>,
    labelPrefix: String,
    onSelectionChanged: (List<String>) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val currentSelections: List<String?> = (0 until choose).map { idx -> selected.getOrNull(idx) }
        for (index in 0 until choose) {
            val selectedValue = currentSelections[index]
            val excluded = selected.toSet() - setOfNotNull(selectedValue)
            OptionDropdown(
                index = index,
                label = "$labelPrefix ${index + 1}",
                selected = selectedValue,
                options = options,
                excluded = excluded,
                onSelected = { idx, value ->
                    val mutable = selected.toMutableList()
                    while (mutable.size <= idx) mutable.add("")
                    mutable[idx] = value
                    onSelectionChanged(mutable.filter { it.isNotBlank() })
                }
            )
            if (index < choose - 1) Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IdLabelDropdown(
    index: Int,
    label: String,
    selectedValue: String?,
    options: List<Pair<String, String>>, // value to label
    excludedValues: Set<String>,
    onSelected: (index: Int, value: String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedValue }?.second ?: ""
    val available = options.filter { (value, _) -> value == selectedValue || value !in excludedValues }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            available.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelected(index, value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PairOptionsSelection(
    choose: Int,
    options: List<Pair<String, String>>, // value to label
    selected: List<String>,
    labelPrefix: String,
    onSelectionChanged: (List<String>) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val currentSelections: List<String?> = (0 until choose).map { idx -> selected.getOrNull(idx) }
        for (index in 0 until choose) {
            val selectedValue = currentSelections[index]
            val excludedValues = selected.toSet() - setOfNotNull(selectedValue)
            IdLabelDropdown(
                index = index,
                label = "$labelPrefix ${index + 1}",
                selectedValue = selectedValue,
                options = options,
                excludedValues = excludedValues,
                onSelected = { idx, value ->
                    val mutable = selected.toMutableList()
                    while (mutable.size <= idx) mutable.add("")
                    mutable[idx] = value
                    onSelectionChanged(mutable.filter { it.isNotBlank() })
                }
            )
            if (index < choose - 1) Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionDropdown(
    index: Int,
    label: String,
    selected: String?,
    options: List<String>,
    excluded: Set<String>,
    onSelected: (index: Int, value: String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val available = options.filter { it == selected || it !in excluded }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            available.forEach { value ->
                DropdownMenuItem(
                    text = { Text(value) },
                    onClick = {
                        onSelected(index, value)
                        expanded = false
                    }
                )
            }
        }
    }
}

// EquipmentDropdown was redundant with IdLabelDropdown; removed.
