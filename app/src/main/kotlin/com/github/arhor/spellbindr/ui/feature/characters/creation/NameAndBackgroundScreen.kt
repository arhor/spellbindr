package com.github.arhor.spellbindr.ui.feature.characters.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
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
import com.github.arhor.spellbindr.ui.feature.characters.creation.CharacterCreationEvent.BackgroundEquipmentChanged
import com.github.arhor.spellbindr.ui.feature.characters.creation.CharacterCreationEvent.BondsChanged
import com.github.arhor.spellbindr.ui.feature.characters.creation.CharacterCreationEvent.FlawsChanged
import com.github.arhor.spellbindr.ui.feature.characters.creation.CharacterCreationEvent.IdealsChanged
import com.github.arhor.spellbindr.ui.feature.characters.creation.CharacterCreationEvent.LanguagesChanged
import com.github.arhor.spellbindr.ui.feature.characters.creation.CharacterCreationEvent.PersonalityTraitsChanged

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
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true)
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

            if (background.languageChoice is Choice.FromAllChoice) {
                GradientDivider()
                ChoiceSection(
                    title = "Languages",
                    choose = background.languageChoice.choose,
                    options = state.languages.map { LabeledOption(it.id, it.name) },
                    selection = state.selectedLanguages,
                    onSelectionChanged = { viewModel.handleEvent(LanguagesChanged(it)) },
                )
            }

            if (background.equipmentChoice != null) {
                GradientDivider()
                ChoiceSection(
                    title = "Background Equipment",
                    choose = background.equipmentChoice.choose,
                    options = state.availableBackgroundEquipment.map { LabeledOption(it.id, it.name) },
                    selection = state.selectedBackgroundEquipment,
                    onSelectionChanged = { viewModel.handleEvent(BackgroundEquipmentChanged(it)) },
                )
            }
            GradientDivider()

            if (background.personalityTraits != null) {
                ChoiceSection(
                    title = "Personality Traits",
                    choice = background.personalityTraits,
                    selection = state.selectedPersonalityTraits,
                    onSelectionChanged = { viewModel.handleEvent(PersonalityTraitsChanged(it)) }
                )
                GradientDivider()
            }

            if (background.ideals != null) {
                ChoiceSection(
                    title = "Ideals",
                    choice = background.ideals,
                    selection = state.selectedIdeals,
                    onSelectionChanged = { viewModel.handleEvent(IdealsChanged(it)) }
                )
                GradientDivider()
            }

            if (background.bonds != null) {
                ChoiceSection(
                    title = "Bonds",
                    choice = background.bonds,
                    selection = state.selectedBonds,
                    onSelectionChanged = { viewModel.handleEvent(BondsChanged(it)) }
                )
                GradientDivider()
            }

            if (background.flaws != null) {
                ChoiceSection(
                    title = "Flaws",
                    choice = background.flaws,
                    selection = state.selectedFlaws,
                    onSelectionChanged = { viewModel.handleEvent(FlawsChanged(it)) }
                )
            }
        }
    }
}

@Composable
private fun ChoiceSection(
    title: String,
    choice: Choice,
    selection: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
) {
    ChoiceSection(
        title = title,
        choose = choice.choose,
        options = choiceToOptions(choice),
        selection = selection,
        onSelectionChanged = onSelectionChanged,
    )
}

@Composable
private fun ChoiceSection(
    title: String,
    choose: Int,
    options: List<LabeledOption<String>>,
    selection: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)

        val labelPrefix = when {
            title.endsWith("s") -> title.dropLast(1)
            else -> title
        }
        PairOptionsSelection(
            choose = choose,
            options = options,
            selected = selection,
            labelPrefix = labelPrefix,
            onSelectionChanged = onSelectionChanged,
        )
    }
}

private fun choiceToOptions(choice: Choice) = when (choice) {
    is Choice.OptionsArrayChoice -> choice.from.map { LabeledOption(it, it) }
    is Choice.IdealChoice -> choice.from.map { LabeledOption(it.desc, it.desc) }
    else -> emptyList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IdLabelDropdown(
    index: Int,
    label: String,
    selectedValue: String?,
    options: List<LabeledOption<String>>,
    excludedValues: Set<String>,
    onSelected: (index: Int, value: String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.value == selectedValue }?.label ?: ""
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
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true)
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
    options: List<LabeledOption<String>>,
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

/**
 * Represents an option with a value and a corresponding label.
 *
 * @param T The type of the value.
 * @property value The actual value of the option.
 * @property label The display label for the option.
 */
data class LabeledOption<T>(
    val value: T,
    val label: String,
)
