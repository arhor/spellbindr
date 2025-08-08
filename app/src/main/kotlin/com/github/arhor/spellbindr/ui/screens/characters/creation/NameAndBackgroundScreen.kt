package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
                LanguageSelection(
                    choose = background.languageChoice.choose,
                    selected = state.selectedLanguages,
                    allLanguages = state.languages.map { it.id to it.name },
                    onSelectionChanged = { viewModel.handleEvent(LanguagesChanged(it)) }
                )
            }

            if (background.equipmentChoice != null) {
                GradientDivider()
                EquipmentSelection(
                    choose = background.equipmentChoice.choose,
                    items = state.availableBackgroundEquipment.map { it.id to it.name },
                    selected = state.selectedBackgroundEquipment,
                    onSelectionChanged = { viewModel.handleEvent(BackgroundEquipmentChanged(it)) }
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
    onSelectionChanged: (List<String>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)

        when (choice) {
            is Choice.OptionsArrayChoice -> {
                val options = choice.from
                if (choice.choose == 1) {
                    SingleSelectList(
                        options = options,
                        selected = selection.firstOrNull(),
                        onSelected = { value -> onSelectionChanged(if (value == null) emptyList() else listOf(value)) }
                    )
                } else {
                    MultiSelectList(
                        options = options,
                        selected = selection,
                        limit = choice.choose,
                        onSelectionChanged = onSelectionChanged,
                    )
                }
            }

            is Choice.IdealChoice -> {
                val options = choice.from.map { it.desc }
                SingleSelectList(
                    options = options,
                    selected = selection.firstOrNull(),
                    onSelected = { value -> onSelectionChanged(if (value == null) emptyList() else listOf(value)) }
                )
            }

            else -> {
                Text(text = "Unsupported choice type")
            }
        }
    }
}

@Composable
private fun MultiSelectList(
    options: List<String>,
    selected: List<String>,
    limit: Int,
    onSelectionChanged: (List<String>) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { option ->
            val checked = option in selected
            Row(modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { isChecked ->
                        val current = selected.toMutableList()
                        if (isChecked) {
                            if (current.size < limit && option !in current) current.add(option)
                        } else {
                            current.remove(option)
                        }
                        onSelectionChanged(current)
                    }
                )
                Text(option)
            }
        }
    }
}

@Composable
private fun SingleSelectList(
    options: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { option ->
            Row(modifier = Modifier.fillMaxWidth()) {
                RadioButton(
                    selected = selected == option,
                    onClick = {
                        onSelected(if (selected == option) null else option)
                    }
                )
                Text(option)
            }
        }
    }
}

@Composable
private fun LanguageSelection(
    choose: Int,
    selected: List<String>,
    allLanguages: List<Pair<String, String>>,
    onSelectionChanged: (List<String>) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Languages", style = MaterialTheme.typography.titleMedium)
        val currentSelections: List<String?> = (0 until choose).map { idx -> selected.getOrNull(idx) }
        for (index in 0 until choose) {
            val selectedId = currentSelections[index]
            // Prevent duplicates across dropdowns; allow the currently selected id
            val excluded = selected.toSet() - setOfNotNull(selectedId)
            LanguageDropdown(
                index = index,
                selectedId = selectedId,
                options = allLanguages,
                excludedIds = excluded,
                onSelected = { idx, id ->
                    val mutable = selected.toMutableList()
                    while (mutable.size <= idx) mutable.add("")
                    mutable[idx] = id
                    onSelectionChanged(mutable.filter { it.isNotBlank() })
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    index: Int,
    selectedId: String?,
    options: List<Pair<String, String>>, // id to name
    excludedIds: Set<String>,
    onSelected: (index: Int, id: String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.firstOrNull { it.first == selectedId }?.second ?: ""
    val available = options.filter { (id, _) -> id == selectedId || id !in excludedIds }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Language ${index + 1}") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            available.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelected(index, id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EquipmentSelection(
    choose: Int,
    items: List<Pair<String, String>>,
    selected: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Background Equipment", style = MaterialTheme.typography.titleMedium)
        if (choose == 1) {
            SingleSelectList(
                options = items.map { it.first },
                selected = selected.firstOrNull(),
                onSelected = { value -> onSelectionChanged(value?.let { listOf(it) } ?: emptyList()) }
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items.forEach { (id, name) ->
                    val checked = id in selected
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                val current = selected.toMutableList()
                                if (isChecked) {
                                    if (current.size < choose && id !in current) current.add(id)
                                } else {
                                    current.remove(id)
                                }
                                onSelectionChanged(current)
                            }
                        )
                        Text(name)
                    }
                }
            }
        }
    }
}
