@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.EquipmentCategory
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.displayName
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ErrorMessage
import com.github.arhor.spellbindr.ui.components.LoadingIndicator
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import kotlinx.coroutines.flow.collectLatest

private fun com.github.arhor.spellbindr.domain.model.CharacterClass.requiresLevelOneSubclass(): Boolean =
    id in setOf("cleric", "sorcerer", "warlock")

private fun com.github.arhor.spellbindr.domain.model.AbilityScores.scoreFor(abilityId: AbilityId): Int =
    when (abilityId.lowercase()) {
        AbilityIds.STR -> strength
        AbilityIds.DEX -> dexterity
        AbilityIds.CON -> constitution
        AbilityIds.INT -> intelligence
        AbilityIds.WIS -> wisdom
        AbilityIds.CHA -> charisma
        else -> 0
    }

@Composable
fun GuidedCharacterSetupRoute(
    onBack: () -> Unit,
    onFinished: (String) -> Unit,
    modifier: Modifier = Modifier,
    vm: GuidedCharacterSetupViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vm.events) {
        vm.events.collectLatest { event ->
            when (event) {
                is GuidedCharacterSetupViewModel.GuidedCharacterSetupEvent.CharacterCreated -> onFinished(event.characterId)
                is GuidedCharacterSetupViewModel.GuidedCharacterSetupEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val title = when (val uiState = state) {
        is GuidedCharacterSetupUiState.Content ->
            "Guided setup · ${uiState.step.title} (${uiState.currentStepIndex + 1}/${uiState.totalSteps})"

        else -> "Guided setup"
    }

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = title,
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        GuidedCharacterSetupScreen(
            state = state,
            snackbarHostState = snackbarHostState,
            onNameChanged = vm::onNameChanged,
            onClassSelected = vm::onClassSelected,
            onSubclassSelected = vm::onSubclassSelected,
            onRaceSelected = vm::onRaceSelected,
            onSubraceSelected = vm::onSubraceSelected,
            onBackgroundSelected = vm::onBackgroundSelected,
            onAbilityMethodSelected = vm::onAbilityMethodSelected,
            onStandardArrayAssigned = vm::onStandardArrayAssigned,
            onPointBuyIncrement = vm::onPointBuyIncrement,
            onPointBuyDecrement = vm::onPointBuyDecrement,
            onChoiceToggled = vm::onChoiceToggled,
            onNext = vm::onNext,
            onPrev = vm::onBack,
            onCreate = vm::onCreateCharacter,
            onGoToStep = vm::onGoToStep,
            validate = vm::validate,
            modifier = modifier,
        )
    }
}

@Composable
private fun GuidedCharacterSetupScreen(
    state: GuidedCharacterSetupUiState,
    snackbarHostState: SnackbarHostState,
    onNameChanged: (String) -> Unit,
    onClassSelected: (String) -> Unit,
    onSubclassSelected: (String) -> Unit,
    onRaceSelected: (String) -> Unit,
    onSubraceSelected: (String) -> Unit,
    onBackgroundSelected: (String) -> Unit,
    onAbilityMethodSelected: (AbilityScoreMethod) -> Unit,
    onStandardArrayAssigned: (AbilityId, Int?) -> Unit,
    onPointBuyIncrement: (AbilityId) -> Unit,
    onPointBuyDecrement: (AbilityId) -> Unit,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onCreate: () -> Unit,
    onGoToStep: (GuidedStep) -> Unit,
    validate: (GuidedCharacterSetupUiState.Content) -> GuidedValidationResult,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            GuidedCharacterSetupUiState.Loading -> LoadingIndicator()
            is GuidedCharacterSetupUiState.Failure -> ErrorMessage(state.errorMessage)
            is GuidedCharacterSetupUiState.Content -> GuidedCharacterSetupContent(
                state = state,
                onNameChanged = onNameChanged,
                onClassSelected = onClassSelected,
                onSubclassSelected = onSubclassSelected,
                onRaceSelected = onRaceSelected,
                onSubraceSelected = onSubraceSelected,
                onBackgroundSelected = onBackgroundSelected,
                onAbilityMethodSelected = onAbilityMethodSelected,
                onStandardArrayAssigned = onStandardArrayAssigned,
                onPointBuyIncrement = onPointBuyIncrement,
                onPointBuyDecrement = onPointBuyDecrement,
                onChoiceToggled = onChoiceToggled,
                onNext = onNext,
                onPrev = onPrev,
                onCreate = onCreate,
                onGoToStep = onGoToStep,
                validate = validate,
            )
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
private fun GuidedCharacterSetupContent(
    state: GuidedCharacterSetupUiState.Content,
    onNameChanged: (String) -> Unit,
    onClassSelected: (String) -> Unit,
    onSubclassSelected: (String) -> Unit,
    onRaceSelected: (String) -> Unit,
    onSubraceSelected: (String) -> Unit,
    onBackgroundSelected: (String) -> Unit,
    onAbilityMethodSelected: (AbilityScoreMethod) -> Unit,
    onStandardArrayAssigned: (AbilityId, Int?) -> Unit,
    onPointBuyIncrement: (AbilityId) -> Unit,
    onPointBuyDecrement: (AbilityId) -> Unit,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onCreate: () -> Unit,
    onGoToStep: (GuidedStep) -> Unit,
    validate: (GuidedCharacterSetupUiState.Content) -> GuidedValidationResult,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(state.step) {
        listState.scrollToItem(0)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        LinearProgressIndicator(
            progress = { (state.currentStepIndex + 1).toFloat() / state.totalSteps.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )

        Box(modifier = Modifier.weight(1f)) {
            when (state.step) {
                GuidedStep.BASICS -> BasicsStep(state, onNameChanged, listState)
                GuidedStep.CLASS -> ClassStep(state, onClassSelected, listState)
                GuidedStep.CLASS_CHOICES -> ClassChoicesStep(state, onSubclassSelected, onChoiceToggled, listState)
                GuidedStep.RACE -> RaceStep(state, onRaceSelected, onSubraceSelected, onChoiceToggled, listState)
                GuidedStep.BACKGROUND -> BackgroundStep(state, onBackgroundSelected, onChoiceToggled, listState)
                GuidedStep.ABILITY_METHOD -> AbilityMethodStep(state, onAbilityMethodSelected, listState)
                GuidedStep.ABILITY_ASSIGN -> AbilityAssignStep(
                    state = state,
                    onStandardArrayAssigned = onStandardArrayAssigned,
                    onPointBuyIncrement = onPointBuyIncrement,
                    onPointBuyDecrement = onPointBuyDecrement,
                    listState = listState,
                )

                GuidedStep.SKILLS_PROFICIENCIES -> SkillsStep(state, onChoiceToggled, listState)
                GuidedStep.EQUIPMENT -> EquipmentStep(state, onChoiceToggled, listState)
                GuidedStep.SPELLS -> SpellsStep(state, onChoiceToggled, listState)
                GuidedStep.REVIEW -> ReviewStep(state, validate(state), onGoToStep, listState)
            }
        }

        HorizontalDivider()

        val isReview = state.step == GuidedStep.REVIEW
        val canGoPrev = state.currentStepIndex > 0 && !state.isSaving
        val validation = if (isReview) validate(state) else null
        val canContinue = if (isReview) !validation!!.hasErrors && !state.isSaving else canProceedFromStep(state)
        val blockingReason = if (!canContinue) {
            if (isReview) {
                validation?.issues
                    ?.firstOrNull { it.severity == GuidedValidationIssue.Severity.ERROR }
                    ?.message
                    ?: "Fix the required fields to create the character."
            } else {
                stepBlockingReason(state)
            }
        } else {
            null
        }

        if (blockingReason != null) {
            Text(
                text = blockingReason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onPrev,
                enabled = canGoPrev,
            ) {
                Text("Back")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (isReview) onCreate() else onNext()
                },
                enabled = canContinue,
            ) {
                Text(if (isReview) "Create character" else "Next")
            }
        }
    }
}

@Composable
private fun BasicsStep(
    state: GuidedCharacterSetupUiState.Content,
    onNameChanged: (String) -> Unit,
    listState: LazyListState,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Level 1 (2014 rules)",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        item {
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChanged,
                label = { Text("Name (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            Text(
                text = "You can fill more details later in the full editor.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ClassStep(
    state: GuidedCharacterSetupUiState.Content,
    onClassSelected: (String) -> Unit,
    listState: LazyListState,
) {
    var query by remember { mutableStateOf("") }
    val classes = remember(state.classes, query) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            state.classes
        } else {
            state.classes.filter { it.name.contains(trimmed, ignoreCase = true) }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Choose a class",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (state.classes.size >= 8) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search classes") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        items(classes, key = { it.id }) { clazz ->
            val selected = state.selection.classId == clazz.id
            Card(
                onClick = { onClassSelected(clazz.id) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RadioButton(selected = selected, onClick = null)
                        Text(text = clazz.name, style = MaterialTheme.typography.bodyLarge)
                    }
                    if (selected) {
                        val savingThrows = clazz.savingThrows.joinToString(", ") { it.displayName() }
                        val skillPicks = clazz.proficiencyChoices.sumOf { choice ->
                            when (choice) {
                                is Choice.ProficiencyChoice ->
                                    if (choice.from.any { it.startsWith("skill-") }) choice.choose else 0

                                else -> 0
                            }
                        }
                        val spellcastingNote = if (clazz.spellcasting?.level == 1) {
                            "Spellcasting at level 1"
                        } else {
                            null
                        }
                        Text(
                            text = buildString {
                                append("Hit die: d")
                                append(clazz.hitDie)
                                append(" • Saves: ")
                                append(savingThrows)
                                if (skillPicks > 0) {
                                    append(" • Choose ")
                                    append(skillPicks)
                                    append(" skill")
                                    if (skillPicks != 1) append('s')
                                }
                                if (spellcastingNote != null) {
                                    append(" • ")
                                    append(spellcastingNote)
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (state.classes.isNotEmpty() && classes.isEmpty()) {
            item {
                Text(
                    text = "No matches.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ClassChoicesStep(
    state: GuidedCharacterSetupUiState.Content,
    onSubclassSelected: (String) -> Unit,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
    listState: LazyListState,
) {
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Level 1 choices",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        if (clazz == null) {
            item { Text("Choose a class first.") }
            return@LazyColumn
        }

        if (clazz.requiresLevelOneSubclass()) {
            item {
                Text(
                    text = "Subclass",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            items(clazz.subclasses, key = { it.id }) { subclass ->
                SelectRow(
                    title = subclass.name,
                    selected = state.selection.subclassId == subclass.id,
                    onClick = { onSubclassSelected(subclass.id) },
                )
            }
        }

        val level1FeatureIds = clazz.levels.firstOrNull { it.level == 1 }?.features.orEmpty()
        val featureChoices = level1FeatureIds.mapNotNull { featureId ->
            val feature = state.featuresById[featureId] ?: return@mapNotNull null
            val choice = feature.choice ?: return@mapNotNull null
            Triple(featureId, feature, choice)
        }

        featureChoices.forEach { (featureId, feature, choice) ->
            item(key = "feature/$featureId") {
                val selected =
                    state.selection.choiceSelections[GuidedCharacterSetupViewModel.featureChoiceKey(featureId)].orEmpty()
                val options = remember(featureId, choice, state.referenceDataVersion) {
                    resolveOptions(choice, state)
                }
                ChoiceSection(
                    title = feature.name,
                    description = null,
                    choice = choice,
                    selected = selected,
                    options = options,
                    onToggle = { optionId ->
                        onChoiceToggled(
                            GuidedCharacterSetupViewModel.featureChoiceKey(featureId),
                            optionId,
                            choice.choose,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun RaceStep(
    state: GuidedCharacterSetupUiState.Content,
    onRaceSelected: (String) -> Unit,
    onSubraceSelected: (String) -> Unit,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
    listState: LazyListState,
) {
    var query by remember { mutableStateOf("") }
    val races = remember(state.races, query) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            state.races
        } else {
            state.races.filter { it.name.contains(trimmed, ignoreCase = true) }
        }
    }

    val selectedRace = state.selection.raceId?.let { id -> state.races.firstOrNull { it.id == id } }

    val traitIds = selectedRace?.let { race ->
        buildList {
            addAll(race.traits.map { it.id })
            val subrace = state.selection.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
            if (subrace != null) addAll(subrace.traits.map { it.id })
        }
    }.orEmpty()
    val traits = traitIds.mapNotNull { state.traitsById[it] }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Choose a race",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (state.races.size >= 8) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search races") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }

        items(races, key = { it.id }) { race ->
            val selected = state.selection.raceId == race.id
            Card(
                onClick = { onRaceSelected(race.id) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RadioButton(selected = selected, onClick = null)
                        Text(text = race.name, style = MaterialTheme.typography.bodyLarge)
                    }
                    if (selected) {
                        Text(
                            text = "Traits: ${race.traits.size} • Subraces: ${race.subraces.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        if (state.races.isNotEmpty() && races.isEmpty()) {
            item {
                Text(
                    text = "No matches.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (selectedRace == null) return@LazyColumn

        if (selectedRace.subraces.isNotEmpty()) {
            item { Text(text = "Subrace", style = MaterialTheme.typography.titleSmall) }
            items(selectedRace.subraces, key = { it.id }) { subrace ->
                SelectRow(
                    title = subrace.name,
                    selected = state.selection.subraceId == subrace.id,
                    onClick = { onSubraceSelected(subrace.id) },
                )
            }
        }

        if (traits.isNotEmpty()) {
            item { Text(text = "Traits", style = MaterialTheme.typography.titleSmall) }
        }

        items(traits, key = { it.id }) { trait ->
            var expanded by remember(trait.id) { mutableStateOf(false) }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = trait.name, style = MaterialTheme.typography.titleSmall)
                    val visibleDesc = if (expanded) trait.desc else trait.desc.take(1)
                    visibleDesc.forEach { paragraph ->
                        Text(
                            text = paragraph,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (trait.desc.size > 1) {
                        OutlinedButton(onClick = { expanded = !expanded }) {
                            Text(if (expanded) "Hide details" else "Show details")
                        }
                    }

                    (trait.abilityBonusChoice as? Choice.AbilityBonusChoice)?.let { choice ->
                        val choiceKey = GuidedCharacterSetupViewModel.raceTraitAbilityBonusChoiceKey(trait.id)
                        val selected = state.selection.choiceSelections[choiceKey].orEmpty()
                        val options = remember(trait.id, state.referenceDataVersion) {
                            choice.from.flatMap { it.keys }.distinct().associateWith { id -> "${id.displayName()} +1" }
                        }
                        ChoiceSection(
                            title = "Ability score increase",
                            description = "Choose ${choice.choose}",
                            choice = choice,
                            selected = selected,
                            options = options,
                            onToggle = { optionId -> onChoiceToggled(choiceKey, optionId, choice.choose) },
                        )
                    }

                    trait.languageChoice?.let { choice ->
                        val choiceKey = GuidedCharacterSetupViewModel.raceTraitLanguageChoiceKey(trait.id)
                        val selected = state.selection.choiceSelections[choiceKey].orEmpty()
                        val options =
                            remember(trait.id, choice, state.referenceDataVersion) { resolveOptions(choice, state) }
                        ChoiceSection(
                            title = "Languages",
                            description = "Choose ${choice.choose}",
                            choice = choice,
                            selected = selected,
                            options = options,
                            disabledOptions = computeAlreadySelectedLanguageReasons(state, choiceKey),
                            onToggle = { optionId -> onChoiceToggled(choiceKey, optionId, choice.choose) },
                        )
                    }

                    trait.proficiencyChoice?.let { choice ->
                        val choiceKey = GuidedCharacterSetupViewModel.raceTraitProficiencyChoiceKey(trait.id)
                        val selected = state.selection.choiceSelections[choiceKey].orEmpty()
                        val options =
                            remember(trait.id, choice, state.referenceDataVersion) { resolveOptions(choice, state) }
                        ChoiceSection(
                            title = "Proficiencies",
                            description = "Choose ${choice.choose}",
                            choice = choice,
                            selected = selected,
                            options = options,
                            disabledOptions = computeAlreadySelectedProficiencyReasons(state, choiceKey),
                            onToggle = { optionId -> onChoiceToggled(choiceKey, optionId, choice.choose) },
                        )
                    }

                    trait.draconicAncestryChoice?.let { choice ->
                        val choiceKey = GuidedCharacterSetupViewModel.raceTraitDraconicAncestryChoiceKey(trait.id)
                        val selected = state.selection.choiceSelections[choiceKey].orEmpty()
                        val options =
                            remember(trait.id, choice, state.referenceDataVersion) { resolveOptions(choice, state) }
                        ChoiceSection(
                            title = "Draconic ancestry",
                            description = "Choose ${choice.choose}",
                            choice = choice,
                            selected = selected,
                            options = options,
                            onToggle = { optionId -> onChoiceToggled(choiceKey, optionId, choice.choose) },
                        )
                    }

                    trait.spellChoice?.let { choice ->
                        val choiceKey = GuidedCharacterSetupViewModel.raceTraitSpellChoiceKey(trait.id)
                        val selected = state.selection.choiceSelections[choiceKey].orEmpty()
                        val options = remember(trait.id, choice, state.spells) { resolveOptions(choice, state) }
                        ChoiceSection(
                            title = "Spell",
                            description = "Choose ${choice.choose}",
                            choice = choice,
                            selected = selected,
                            options = options,
                            onToggle = { optionId -> onChoiceToggled(choiceKey, optionId, choice.choose) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackgroundStep(
    state: GuidedCharacterSetupUiState.Content,
    onBackgroundSelected: (String) -> Unit,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
    listState: LazyListState,
) {
    var query by remember { mutableStateOf("") }
    val backgrounds = remember(state.backgrounds, query) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            state.backgrounds
        } else {
            state.backgrounds.filter { it.name.contains(trimmed, ignoreCase = true) }
        }
    }

    val bg = state.selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id } }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Choose a background",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (state.backgrounds.size >= 10) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search backgrounds") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        items(backgrounds, key = { it.id }) { entry ->
            val selected = state.selection.backgroundId == entry.id
            Card(
                onClick = { onBackgroundSelected(entry.id) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RadioButton(selected = selected, onClick = null)
                        Text(text = entry.name, style = MaterialTheme.typography.bodyLarge)
                    }
                    if (selected) {
                        val featureSummary = entry.feature.desc.firstOrNull().orEmpty()
                        Text(
                            text = "Feature: ${entry.feature.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (featureSummary.isNotBlank()) {
                            Text(
                                text = featureSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        if (state.backgrounds.isNotEmpty() && backgrounds.isEmpty()) {
            item {
                Text(
                    text = "No matches.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        val languageChoice = bg?.languageChoice
        if (bg != null && languageChoice != null) {
            item {
                val choiceKey = GuidedCharacterSetupViewModel.backgroundLanguageChoiceKey()
                val selected = state.selection.choiceSelections[choiceKey].orEmpty()
                val options =
                    remember(languageChoice, state.referenceDataVersion) { resolveOptions(languageChoice, state) }
                ChoiceSection(
                    title = "Languages",
                    description = "Choose ${languageChoice.choose}",
                    choice = languageChoice,
                    selected = selected,
                    options = options,
                    disabledOptions = computeAlreadySelectedLanguageReasons(state, choiceKey),
                    onToggle = { optionId -> onChoiceToggled(choiceKey, optionId, languageChoice.choose) },
                )
            }
        }
    }
}

@Composable
private fun AbilityMethodStep(
    state: GuidedCharacterSetupUiState.Content,
    onAbilityMethodSelected: (AbilityScoreMethod) -> Unit,
    listState: LazyListState,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(text = "Ability score method", style = MaterialTheme.typography.titleMedium) }
        items(AbilityScoreMethod.entries, key = { it.name }) { method ->
            SelectRow(
                title = method.label,
                selected = state.selection.abilityMethod == method,
                onClick = { onAbilityMethodSelected(method) },
            )
        }
        item {
            Text(
                text = "Racial ability score increases are applied after this step.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AbilityAssignStep(
    state: GuidedCharacterSetupUiState.Content,
    onStandardArrayAssigned: (AbilityId, Int?) -> Unit,
    onPointBuyIncrement: (AbilityId) -> Unit,
    onPointBuyDecrement: (AbilityId) -> Unit,
    listState: LazyListState,
) {
    val method = state.selection.abilityMethod
    if (method == null) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text("Choose a method first.") }
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(text = "Assign scores", style = MaterialTheme.typography.titleMedium) }
        item {
            when (method) {
                AbilityScoreMethod.STANDARD_ARRAY -> StandardArrayAssign(state, onStandardArrayAssigned)
                AbilityScoreMethod.POINT_BUY -> PointBuyAssign(state, onPointBuyIncrement, onPointBuyDecrement)
            }
        }
    }
}

@Composable
private fun StandardArrayAssign(
    state: GuidedCharacterSetupUiState.Content,
    onStandardArrayAssigned: (AbilityId, Int?) -> Unit,
) {
    Text(
        text = "Use values: ${StandardArray.joinToString()}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    AbilityIds.standardOrder.forEach { abilityId ->
        val assigned = state.selection.standardArrayAssignments[abilityId]
        val takenByOthers = state.selection.standardArrayAssignments
            .filterKeys { it != abilityId }
            .values
            .filterNotNull()
            .toSet()

        val available = StandardArray.filter { it !in takenByOthers || it == assigned }

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = abilityId.displayName(), style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    available.forEach { value ->
                        SelectRowCompact(
                            label = value.toString(),
                            selected = assigned == value,
                            onClick = { onStandardArrayAssigned(abilityId, value) },
                        )
                    }
                    if (assigned != null) {
                        OutlinedButton(onClick = { onStandardArrayAssigned(abilityId, null) }) {
                            Text("Clear")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PointBuyAssign(
    state: GuidedCharacterSetupUiState.Content,
    onPointBuyIncrement: (AbilityId) -> Unit,
    onPointBuyDecrement: (AbilityId) -> Unit,
) {
    val totalCost = state.selection.pointBuyScores.values.sumOf(::pointBuyCost)
    val remaining = (27 - totalCost).coerceAtLeast(0)

    Text(
        text = "Points remaining: $remaining (spent $totalCost / 27)",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    AbilityIds.standardOrder.forEach { abilityId ->
        val value = state.selection.pointBuyScores[abilityId] ?: 8
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = abilityId.displayName(), style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "Cost: ${pointBuyCost(value)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(
                    onClick = { onPointBuyDecrement(abilityId) },
                    enabled = value > 8,
                ) {
                    Text("-")
                }
                Text(text = value.toString(), style = MaterialTheme.typography.titleMedium)
                OutlinedButton(
                    onClick = { onPointBuyIncrement(abilityId) },
                    enabled = value < 15,
                ) {
                    Text("+")
                }
            }
        }
    }
}

@Composable
private fun SkillsStep(
    state: GuidedCharacterSetupUiState.Content,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
    listState: LazyListState,
) {
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(text = "Skills & proficiencies", style = MaterialTheme.typography.titleMedium) }

        if (clazz == null) {
            item { Text("Choose a class first.") }
            return@LazyColumn
        }

        if (clazz.proficiencyChoices.isEmpty()) {
            item {
                Text(
                    text = "No additional choices for this class.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@LazyColumn
        }

        clazz.proficiencyChoices.forEachIndexed { index, choice ->
            item(key = "class/proficiency/$index") {
                val choiceKey = GuidedCharacterSetupViewModel.classProficiencyChoiceKey(index)
                val selected = state.selection.choiceSelections[choiceKey].orEmpty()
                val options = remember(choiceKey, choice, state.referenceDataVersion) { resolveOptions(choice, state) }
                ChoiceSection(
                    title = "Class choice ${index + 1}",
                    description = (choice as? Choice.OptionsArrayChoice)?.desc ?: "Choose ${choice.choose}",
                    choice = choice,
                    selected = selected,
                    options = options,
                    disabledOptions = computeAlreadySelectedProficiencyReasons(state, choiceKey),
                    onToggle = { optionId -> onChoiceToggled(choiceKey, optionId, choice.choose) },
                )
            }
        }
    }
}

@Composable
private fun EquipmentStep(
    state: GuidedCharacterSetupUiState.Content,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
    listState: LazyListState,
) {
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } }
    val bg = state.selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id } }
    val fixed = remember(clazz?.startingEquipment, bg?.effects, state.referenceDataVersion) {
        buildList<String> {
            clazz?.startingEquipment?.forEach { ref ->
                val name = state.equipmentById[ref.id]?.name ?: EntityRef(ref.id).prettyString()
                add(if (ref.quantity <= 1) name else "$name x${ref.quantity}")
            }
            bg?.effects?.forEach { effect ->
                if (effect is com.github.arhor.spellbindr.domain.model.Effect.AddEquipmentEffect) {
                    effect.equipment.forEach { counted ->
                        val name = state.equipmentById[counted.id]?.name ?: EntityRef(counted.id).prettyString()
                        add(if (counted.quantity <= 1) name else "$name x${counted.quantity}")
                    }
                }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(text = "Starting equipment", style = MaterialTheme.typography.titleMedium) }
        item {
            Text(
                text = "This MVP supports fixed starting equipment and simple background equipment choices. "
                    + "Some class equipment options may be missing.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (fixed.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Included", style = MaterialTheme.typography.titleSmall)
                        fixed.forEach { Text(text = "• $it", style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
        }

        val equipmentChoice = bg?.equipmentChoice
        if (equipmentChoice != null) {
            item {
                val choiceKey = GuidedCharacterSetupViewModel.backgroundEquipmentChoiceKey()
                val selected = state.selection.choiceSelections[choiceKey].orEmpty()
                val options =
                    remember(equipmentChoice, state.referenceDataVersion) { resolveOptions(equipmentChoice, state) }
                ChoiceSection(
                    title = "Background equipment",
                    description = "Choose ${equipmentChoice.choose}",
                    choice = equipmentChoice,
                    selected = selected,
                    options = options,
                    onToggle = { optionId -> onChoiceToggled(choiceKey, optionId, equipmentChoice.choose) },
                )
            }
        }
    }
}

@Composable
private fun SpellsStep(
    state: GuidedCharacterSetupUiState.Content,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
    listState: LazyListState,
) {
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } }
    val requirements = clazz?.let { computeSpellRequirements(it, state.preview) }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(text = "Spells", style = MaterialTheme.typography.titleMedium) }
        item {
            Text(
                text = "MVP: only count limits are enforced. You can still edit spells later on the sheet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (clazz == null || requirements == null) {
            item {
                Text(
                    text = "This class doesn’t cast spells at level 1.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@LazyColumn
        }

        if (state.spells.isEmpty()) {
            item {
                Text(
                    text = "Loading spells…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@LazyColumn
        }

        val cantripsKey = GuidedCharacterSetupViewModel.spellCantripsChoiceKey()
        val spellsKey = GuidedCharacterSetupViewModel.spellLevel1ChoiceKey()

        val selectedCantrips = state.selection.choiceSelections[cantripsKey].orEmpty()
        val selectedSpells = state.selection.choiceSelections[spellsKey].orEmpty()

        if (requirements.cantrips > 0) {
            item {
                Text(
                    text = "Cantrips: ${selectedCantrips.size} / ${requirements.cantrips}",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            item {
                val options = remember(clazz.id, state.spells) {
                    state.spells
                        .asSequence()
                        .filter { spell -> spell.level == 0 && spell.classes.any { it.id == clazz.id } }
                        .sortedBy { it.name }
                        .associate { it.id to it.name }
                }
                ChoiceSection(
                    title = "Cantrips",
                    description = null,
                    choice = Choice.OptionsArrayChoice(
                        choose = requirements.cantrips,
                        from = options.keys.toList(),
                    ),
                    selected = selectedCantrips,
                    options = options,
                    onToggle = { spellId -> onChoiceToggled(cantripsKey, spellId, requirements.cantrips) },
                )
            }
        }

        if (requirements.level1Spells > 0) {
            item {
                Text(
                    text = "${requirements.level1Label.replaceFirstChar { it.uppercase() }}: "
                        + "${selectedSpells.size} / ${requirements.level1Spells}",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            item {
                val options = remember(clazz.id, state.spells) {
                    state.spells
                        .asSequence()
                        .filter { spell -> spell.level == 1 && spell.classes.any { it.id == clazz.id } }
                        .sortedBy { it.name }
                        .associate { it.id to it.name }
                }
                ChoiceSection(
                    title = requirements.level1Label.replaceFirstChar { it.uppercase() },
                    description = null,
                    choice = Choice.OptionsArrayChoice(
                        choose = requirements.level1Spells,
                        from = options.keys.toList(),
                    ),
                    selected = selectedSpells,
                    options = options,
                    onToggle = { spellId -> onChoiceToggled(spellsKey, spellId, requirements.level1Spells) },
                )
            }
        }
    }
}

@Composable
private fun ReviewStep(
    state: GuidedCharacterSetupUiState.Content,
    validation: GuidedValidationResult,
    onGoToStep: (GuidedStep) -> Unit,
    listState: LazyListState,
) {
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } }
    val subclassName = state.selection.subclassId?.let { id -> clazz?.subclasses?.firstOrNull { it.id == id }?.name }
    val race = state.selection.raceId?.let { id -> state.races.firstOrNull { it.id == id } }
    val subraceName = state.selection.subraceId?.let { id -> race?.subraces?.firstOrNull { it.id == id }?.name }
    val background = state.selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id } }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(text = "Review", style = MaterialTheme.typography.titleMedium) }

        item {
            val preview = state.preview
            val abilitiesLine = AbilityIds.standardOrder.joinToString(" • ") { abilityId ->
                val score = preview.abilityScores.scoreFor(abilityId)
                val mod = preview.abilityScores.modifierFor(abilityId)
                val modLabel = if (mod >= 0) "+$mod" else mod.toString()
                "${abilityId.uppercase()} $score ($modLabel)"
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(text = "Quick stats", style = MaterialTheme.typography.titleSmall)
                    Text(text = abilitiesLine, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "HP ${preview.maxHitPoints} • AC ${preview.armorClass} • Speed ${preview.speed} ft",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Languages: ${preview.languagesCount} • Proficiencies: ${preview.proficienciesCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            Text(text = "Jump back to edit", style = MaterialTheme.typography.titleSmall)
        }

        item {
            SummaryRow(
                label = "Name",
                value = state.name.ifBlank { "—" },
                onClick = { onGoToStep(GuidedStep.BASICS) },
            )
        }
        item {
            SummaryRow(
                label = "Class",
                value = clazz?.name ?: "—",
                onClick = { onGoToStep(GuidedStep.CLASS) },
            )
        }
        if (GuidedStep.CLASS_CHOICES in state.steps) {
            item {
                SummaryRow(
                    label = "Subclass & choices",
                    value = subclassName ?: "—",
                    onClick = { onGoToStep(GuidedStep.CLASS_CHOICES) },
                )
            }
        }
        item {
            SummaryRow(
                label = "Race",
                value = race?.name ?: "—",
                onClick = { onGoToStep(GuidedStep.RACE) },
            )
        }
        if (!subraceName.isNullOrBlank()) {
            item {
                SummaryRow(
                    label = "Subrace",
                    value = subraceName,
                    onClick = { onGoToStep(GuidedStep.RACE) },
                )
            }
        }
        item {
            SummaryRow(
                label = "Background",
                value = background?.name ?: "—",
                onClick = { onGoToStep(GuidedStep.BACKGROUND) },
            )
        }
        item {
            SummaryRow(
                label = "Ability scores",
                value = state.selection.abilityMethod?.label ?: "—",
                onClick = { onGoToStep(GuidedStep.ABILITY_ASSIGN) },
            )
        }
        item {
            SummaryRow(
                label = "Skills & proficiencies",
                value = "Tap to edit",
                onClick = { onGoToStep(GuidedStep.SKILLS_PROFICIENCIES) },
            )
        }
        item {
            SummaryRow(
                label = "Equipment",
                value = "Tap to edit",
                onClick = { onGoToStep(GuidedStep.EQUIPMENT) },
            )
        }
        if (GuidedStep.SPELLS in state.steps) {
            item {
                SummaryRow(
                    label = "Spells",
                    value = "Tap to edit",
                    onClick = { onGoToStep(GuidedStep.SPELLS) },
                )
            }
        }

        if (validation.issues.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(text = "Checks", style = MaterialTheme.typography.titleSmall)
                        validation.issues.forEach { issue ->
                            val prefix = if (issue.severity == GuidedValidationIssue.Severity.ERROR) "Error" else "Note"
                            Text(text = "$prefix: ${issue.message}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "All set.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SelectCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RadioButton(selected = selected, onClick = null)
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun SelectRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RadioButton(selected = selected, onClick = null)
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun SelectRowCompact(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(onClick = onClick, enabled = !selected) {
        Text(label)
    }
}

@Composable
private fun ChoiceSection(
    title: String,
    description: String?,
    choice: Choice,
    selected: Set<String>,
    options: Map<String, String>,
    disabledOptions: Map<String, String> = emptyMap(),
    onToggle: (String) -> Unit,
) {
    val isSingleSelect = choice.choose == 1
    var query by remember(title, options.size) { mutableStateOf("") }
    val showSearch = options.size >= 12
    val filteredOptions = remember(options, query) {
        if (query.isBlank()) {
            options.entries.toList()
        } else {
            options.entries.filter { (_, label) ->
                label.contains(query, ignoreCase = true)
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "Selected: ${selected.size} / ${choice.choose}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (disabledOptions.isNotEmpty()) {
                val alreadyHaveCount = options.keys.count { it !in selected && it in disabledOptions }
                if (alreadyHaveCount > 0) {
                    Text(
                        text = "Already have: $alreadyHaveCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (options.isEmpty()) {
                Text(
                    text = "No options available (MVP limitation).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }
            if (showSearch) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search options") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            if (filteredOptions.isEmpty()) {
                Text(
                    text = "No matches.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }
            val useLazyOptions = filteredOptions.size >= 30

            @Composable
            fun OptionRow(id: String, label: String) {
                val alreadySelectedReason = disabledOptions[id]
                val isSelectedHere = id in selected
                val isAlreadyHave = alreadySelectedReason != null && !isSelectedHere
                val enabled = when {
                    isSelectedHere -> true
                    alreadySelectedReason != null -> false
                    isSingleSelect -> true
                    selected.size >= choice.choose -> false
                    else -> true
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (isSingleSelect) {
                        RadioButton(
                            selected = isSelectedHere,
                            onClick = { onToggle(id) },
                            enabled = enabled,
                        )
                    } else {
                        Checkbox(
                            checked = isSelectedHere || isAlreadyHave,
                            onCheckedChange = { onToggle(id) },
                            enabled = enabled,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (!isSelectedHere && alreadySelectedReason != null) {
                            Text(
                                text = "Already have — $alreadySelectedReason",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            if (useLazyOptions) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                ) {
                    items(filteredOptions, key = { it.key }) { entry ->
                        OptionRow(id = entry.key, label = entry.value)
                    }
                }
            } else {
                filteredOptions.forEach { entry ->
                    OptionRow(id = entry.key, label = entry.value)
                }
            }
        }
    }
}

private fun resolveOptions(choice: Choice, state: GuidedCharacterSetupUiState.Content): Map<String, String> =
    when (choice) {
        is Choice.FromAllChoice ->
            state.languages.associate { it.id to it.name }

        is Choice.OptionsArrayChoice ->
            choice.from.associateWith { optionId -> displayNameForOption(optionId, state) }

        is Choice.ProficiencyChoice ->
            choice.from.associateWith { optionId -> displayNameForOption(optionId, state) }

        is Choice.EquipmentChoice ->
            choice.from.associateWith { optionId -> displayNameForOption(optionId, state) }

        is Choice.EquipmentCategoriesChoice -> {
            val categoryIds = choice.from.categories
            val categories = categoryIds.mapNotNull(::equipmentCategoryFromId).toSet()
            state.equipment
                .asSequence()
                .filter { categories.all { c -> c in it.categories } }
                .sortedBy { it.name }
                .associate { it.id to it.name }
        }

        is Choice.FeatureChoice ->
            choice.from.associateWith { optionId ->
                state.featuresById[optionId]?.name ?: EntityRef(optionId).prettyString()
            }

        is Choice.FavoredEnemyChoice ->
            choice.from.associateWith { it }

        is Choice.TerrainTypeChoice ->
            choice.from.associateWith { it }

        is Choice.AbilityBonusChoice ->
            choice.from.flatMap { it.keys }.distinct().associateWith { "${it.displayName()} +1" }

        is Choice.ResourceListChoice ->
            when (choice.from.lowercase()) {
                "languages" -> state.languages.associate { it.id to it.name }
                "spells" -> {
                    var seq = state.spells.asSequence()
                    choice.where?.forEach { (key, value) ->
                        when (key.lowercase()) {
                            "classes" -> {
                                seq = seq.filter { spell -> spell.classes.any { it.id == value } }
                            }

                            "level" -> {
                                val level = value.toIntOrNull()
                                if (level != null) {
                                    seq = seq.filter { spell -> spell.level == level }
                                }
                            }
                        }
                    }
                    seq.sortedBy { it.name }.associate { it.id to it.name }
                }
                else -> emptyMap()
            }

        is Choice.IdealChoice ->
            emptyMap()

        is Choice.NestedChoice ->
            emptyMap()
    }

private fun computeAlreadySelectedLanguageReasons(
    state: GuidedCharacterSetupUiState.Content,
    currentChoiceKey: String,
): Map<String, String> {
    val result = linkedMapOf<String, String>()

    fun record(languageId: String, reason: String) {
        result.putIfAbsent(languageId, reason)
    }

    val selectedRace = state.selection.raceId?.let { rid -> state.races.firstOrNull { it.id == rid } }
    if (selectedRace != null) {
        val traitIds = buildList {
            addAll(selectedRace.traits.map { it.id })
            val subrace = state.selection.subraceId?.let { sid -> selectedRace.subraces.firstOrNull { it.id == sid } }
            if (subrace != null) {
                addAll(subrace.traits.map { it.id })
            }
        }
        traitIds.mapNotNull { state.traitsById[it] }.forEach { trait ->
            trait.effects.orEmpty().forEach { effect ->
                if (effect is com.github.arhor.spellbindr.domain.model.Effect.AddLanguagesEffect) {
                    effect.languages.forEach { languageId ->
                        record(languageId, "Race trait: ${trait.name}")
                    }
                }
            }
        }
    }

    val selectedBackground = state.selection.backgroundId?.let { bid -> state.backgrounds.firstOrNull { it.id == bid } }
    if (selectedBackground != null) {
        selectedBackground.effects.forEach { effect ->
            if (effect is com.github.arhor.spellbindr.domain.model.Effect.AddLanguagesEffect) {
                effect.languages.forEach { languageId ->
                    record(languageId, "Background: ${selectedBackground.name}")
                }
            }
        }
    }

    state.selection.choiceSelections.forEach { (key, selectedIds) ->
        if (key == currentChoiceKey) return@forEach
        val reason = when {
            key.startsWith("race/trait/") && key.endsWith("/language") -> {
                val parts = key.split('/')
                val traitId = parts.getOrNull(2)
                val traitName = traitId?.let { state.traitsById[it]?.name }
                if (!traitName.isNullOrBlank()) "Race choice: $traitName" else "Race choice"
            }

            key == GuidedCharacterSetupViewModel.backgroundLanguageChoiceKey() -> {
                selectedBackground?.let { "Background choice: ${it.name}" } ?: "Background choice"
            }

            else -> null
        }
        if (reason != null) {
            selectedIds.forEach { record(it, reason) }
        }
    }

    return result
}

private fun computeAlreadySelectedProficiencyReasons(
    state: GuidedCharacterSetupUiState.Content,
    currentChoiceKey: String,
): Map<String, String> {
    val result = linkedMapOf<String, String>()

    fun record(proficiencyId: String, reason: String) {
        result.putIfAbsent(proficiencyId, reason)
    }

    val selectedClass = state.selection.classId?.let { cid -> state.classes.firstOrNull { it.id == cid } }
    if (selectedClass != null) {
        selectedClass.proficiencies.forEach { proficiencyId ->
            record(proficiencyId, "Class: ${selectedClass.name}")
        }
    }

    val selectedRace = state.selection.raceId?.let { rid -> state.races.firstOrNull { it.id == rid } }
    if (selectedRace != null) {
        val traitIds = buildList {
            addAll(selectedRace.traits.map { it.id })
            val subrace = state.selection.subraceId?.let { sid -> selectedRace.subraces.firstOrNull { it.id == sid } }
            if (subrace != null) {
                addAll(subrace.traits.map { it.id })
            }
        }
        traitIds.mapNotNull { state.traitsById[it] }.forEach { trait ->
            trait.effects.orEmpty().forEach { effect ->
                if (effect is com.github.arhor.spellbindr.domain.model.Effect.AddProficienciesEffect) {
                    effect.proficiencies.forEach { proficiencyId ->
                        record(proficiencyId, "Race trait: ${trait.name}")
                    }
                }
            }
        }
    }

    val selectedBackground = state.selection.backgroundId?.let { bid -> state.backgrounds.firstOrNull { it.id == bid } }
    if (selectedBackground != null) {
        selectedBackground.effects.forEach { effect ->
            if (effect is com.github.arhor.spellbindr.domain.model.Effect.AddProficienciesEffect) {
                effect.proficiencies.forEach { proficiencyId ->
                    record(proficiencyId, "Background: ${selectedBackground.name}")
                }
            }
        }
    }

    state.selection.choiceSelections.forEach { (key, selectedIds) ->
        if (key == currentChoiceKey) return@forEach
        val reason = when {
            key.startsWith("class/proficiency/") -> {
                val index = key.substringAfterLast('/').toIntOrNull()
                if (index != null) "Class choice ${index + 1}" else "Class choice"
            }

            key.startsWith("race/trait/") && key.endsWith("/proficiency") -> {
                val parts = key.split('/')
                val traitId = parts.getOrNull(2)
                val traitName = traitId?.let { state.traitsById[it]?.name }
                if (!traitName.isNullOrBlank()) "Race choice: $traitName" else "Race choice"
            }

            key.startsWith("feature/") -> {
                val featureId = key.removePrefix("feature/")
                val featureName = state.featuresById[featureId]?.name
                if (!featureName.isNullOrBlank()) "Feature choice: $featureName" else "Feature choice"
            }

            else -> null
        }
        if (reason != null) {
            selectedIds.forEach { record(it, reason) }
        }
    }

    return result
}

private fun displayNameForOption(id: String, state: GuidedCharacterSetupUiState.Content): String {
    if (id.startsWith("skill-")) {
        val normalized = id.removePrefix("skill-").replace("-", "_").uppercase()
        val skill = Skill.entries.firstOrNull { it.name == normalized }
        if (skill != null) return skill.displayName
    }
    state.languagesById[id]?.let { return it.name }
    state.equipmentById[id]?.let { return it.name }
    state.featuresById[id]?.let { return it.name }
    state.traitsById[id]?.let { return it.name }
    state.spellsById[id]?.let { return it.name }
    return EntityRef(id).prettyString()
}

private data class SpellRequirements(
    val cantrips: Int,
    val level1Spells: Int,
    val level1Label: String,
)

private fun computeSpellRequirements(
    clazz: com.github.arhor.spellbindr.domain.model.CharacterClass,
    preview: GuidedCharacterPreview,
): SpellRequirements? {
    if (clazz.spellcasting?.level != 1) return null

    val level1 = clazz.levels.firstOrNull { it.level == 1 }?.spellcasting
    val cantrips = level1?.cantrips ?: 0
    val level1Spells = when {
        clazz.id == "wizard" -> 6
        level1?.spells != null -> level1.spells
        clazz.id == "cleric" || clazz.id == "druid" -> (preview.abilityScores.modifierFor(AbilityIds.WIS) + 1)
            .coerceAtLeast(1)

        else -> 0
    }
    val label = when (clazz.id) {
        "wizard" -> "spellbook spell(s)"
        "cleric", "druid" -> "prepared spell(s)"
        else -> "spell(s)"
    }

    return SpellRequirements(
        cantrips = cantrips,
        level1Spells = level1Spells,
        level1Label = label,
    )
}

private fun canProceedFromStep(state: GuidedCharacterSetupUiState.Content): Boolean {
    val selection = state.selection
    return when (state.step) {
        GuidedStep.BASICS -> true
        GuidedStep.CLASS -> selection.classId != null
        GuidedStep.CLASS_CHOICES -> classChoicesComplete(state)
        GuidedStep.RACE -> raceComplete(state)
        GuidedStep.BACKGROUND -> backgroundComplete(state)
        GuidedStep.ABILITY_METHOD -> selection.abilityMethod != null
        GuidedStep.ABILITY_ASSIGN -> abilityAssignComplete(state)
        GuidedStep.SKILLS_PROFICIENCIES -> classProficiencyChoicesComplete(state)
        GuidedStep.EQUIPMENT -> backgroundEquipmentComplete(state)
        GuidedStep.SPELLS -> spellsComplete(state)
        GuidedStep.REVIEW -> false
    }
}

private fun stepBlockingReason(state: GuidedCharacterSetupUiState.Content): String? {
    val selection = state.selection
    return when (state.step) {
        GuidedStep.BASICS -> null

        GuidedStep.CLASS ->
            if (selection.classId == null) "Select a class to continue." else null

        GuidedStep.CLASS_CHOICES -> {
            val clazz = selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } }
                ?: return "Select a class first."

            if (clazz.requiresLevelOneSubclass() && selection.subclassId == null) {
                return "Select a subclass to continue."
            }

            val level1Features = clazz.levels.firstOrNull { it.level == 1 }?.features.orEmpty()
            for (featureId in level1Features) {
                val feature = state.featuresById[featureId] ?: continue
                val choice = feature.choice ?: continue
                val selected =
                    selection.choiceSelections[GuidedCharacterSetupViewModel.featureChoiceKey(featureId)].orEmpty()
                if (selected.size != choice.choose) {
                    return "Select ${choice.choose} option(s) for ${feature.name}."
                }
            }
            null
        }

        GuidedStep.RACE -> {
            val race = selection.raceId?.let { id -> state.races.firstOrNull { it.id == id } }
                ?: return "Select a race to continue."
            if (race.subraces.isNotEmpty() && selection.subraceId == null) return "Select a subrace to continue."

            val traitIds = buildList {
                addAll(race.traits.map { it.id })
                val subrace = selection.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
                if (subrace != null) addAll(subrace.traits.map { it.id })
            }
            val traits = traitIds.mapNotNull { state.traitsById[it] }
            for (trait in traits) {
                val requiredChoices = listOfNotNull(
                    trait.abilityBonusChoice?.let { GuidedCharacterSetupViewModel.raceTraitAbilityBonusChoiceKey(trait.id) to it.choose },
                    trait.languageChoice?.let { GuidedCharacterSetupViewModel.raceTraitLanguageChoiceKey(trait.id) to it.choose },
                    trait.proficiencyChoice?.let { GuidedCharacterSetupViewModel.raceTraitProficiencyChoiceKey(trait.id) to it.choose },
                    trait.draconicAncestryChoice?.let {
                        GuidedCharacterSetupViewModel.raceTraitDraconicAncestryChoiceKey(trait.id) to it.choose
                    },
                    trait.spellChoice?.let { GuidedCharacterSetupViewModel.raceTraitSpellChoiceKey(trait.id) to it.choose },
                )
                for ((key, choose) in requiredChoices) {
                    if (selection.choiceSelections[key].orEmpty().size != choose) {
                        return "Select $choose option(s) for ${trait.name}."
                    }
                }
            }
            null
        }

        GuidedStep.BACKGROUND -> {
            val bg = selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id } }
                ?: return "Select a background to continue."
            val languageChoice = bg.languageChoice ?: return null
            val selected =
                selection.choiceSelections[GuidedCharacterSetupViewModel.backgroundLanguageChoiceKey()].orEmpty()
            if (selected.size != languageChoice.choose) {
                "Select ${languageChoice.choose} language(s) to continue."
            } else {
                null
            }
        }

        GuidedStep.ABILITY_METHOD ->
            if (selection.abilityMethod == null) "Choose an ability score method to continue." else null

        GuidedStep.ABILITY_ASSIGN -> {
            when (selection.abilityMethod) {
                AbilityScoreMethod.STANDARD_ARRAY ->
                    if (!abilityAssignComplete(state)) "Assign all six standard array values." else null

                AbilityScoreMethod.POINT_BUY -> {
                    val cost = selection.pointBuyScores.values.sumOf(::pointBuyCost)
                    if (cost > 27) "Point buy exceeds 27 points." else null
                }

                null -> "Choose an ability score method first."
            }
        }

        GuidedStep.SKILLS_PROFICIENCIES -> {
            val clazz = selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } } ?: return null
            clazz.proficiencyChoices.withIndex().firstOrNull { (index, choice) ->
                selection.choiceSelections[GuidedCharacterSetupViewModel.classProficiencyChoiceKey(index)].orEmpty().size != choice.choose
            }?.let { (index, choice) ->
                "Select ${choice.choose} option(s) for class choice ${index + 1}."
            }
        }

        GuidedStep.EQUIPMENT -> {
            val bg = selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id } } ?: return null
            val equipmentChoice = bg.equipmentChoice ?: return null
            val selected =
                selection.choiceSelections[GuidedCharacterSetupViewModel.backgroundEquipmentChoiceKey()].orEmpty()
            if (selected.size != equipmentChoice.choose) {
                "Select ${equipmentChoice.choose} equipment option(s) to continue."
            } else {
                null
            }
        }

        GuidedStep.SPELLS -> {
            val clazz = selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } } ?: return null
            val requirements = computeSpellRequirements(clazz, state.preview) ?: return null
            val selectedCantrips =
                selection.choiceSelections[GuidedCharacterSetupViewModel.spellCantripsChoiceKey()].orEmpty()
            if (requirements.cantrips > 0 && selectedCantrips.size != requirements.cantrips) {
                return "Select ${requirements.cantrips} cantrip(s) to continue."
            }
            val selectedSpells =
                selection.choiceSelections[GuidedCharacterSetupViewModel.spellLevel1ChoiceKey()].orEmpty()
            if (requirements.level1Spells > 0 && selectedSpells.size != requirements.level1Spells) {
                return "Select ${requirements.level1Spells} ${requirements.level1Label} to continue."
            }
            null
        }

        GuidedStep.REVIEW -> null
    }
}

private fun classChoicesComplete(state: GuidedCharacterSetupUiState.Content): Boolean {
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } } ?: return false
    val requiresSubclass = clazz.id in setOf("cleric", "sorcerer", "warlock")
    if (requiresSubclass && state.selection.subclassId == null) return false

    val level1Features = clazz.levels.firstOrNull { it.level == 1 }?.features.orEmpty()
    val requiredFeatureChoices = level1Features.mapNotNull { featureId ->
        val choice = state.featuresById[featureId]?.choice ?: return@mapNotNull null
        featureId to choice.choose
    }
    return !requiredFeatureChoices.any { (featureId, choose) ->
        state.selection.choiceSelections[GuidedCharacterSetupViewModel.featureChoiceKey(featureId)].orEmpty().size != choose
    }
}

private fun raceComplete(state: GuidedCharacterSetupUiState.Content): Boolean {
    val race = state.selection.raceId?.let { id -> state.races.firstOrNull { it.id == id } } ?: return false
    if (race.subraces.isNotEmpty() && state.selection.subraceId == null) return false

    val traitIds = buildList {
        addAll(race.traits.map { it.id })
        val subrace = state.selection.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
        if (subrace != null) {
            addAll(subrace.traits.map { it.id })
        }
    }
    val traits = traitIds.mapNotNull { state.traitsById[it] }
    return traits.all { trait ->
        listOfNotNull(
            trait.abilityBonusChoice?.let { GuidedCharacterSetupViewModel.raceTraitAbilityBonusChoiceKey(trait.id) to it.choose },
            trait.languageChoice?.let { GuidedCharacterSetupViewModel.raceTraitLanguageChoiceKey(trait.id) to it.choose },
            trait.proficiencyChoice?.let { GuidedCharacterSetupViewModel.raceTraitProficiencyChoiceKey(trait.id) to it.choose },
            trait.draconicAncestryChoice?.let { GuidedCharacterSetupViewModel.raceTraitDraconicAncestryChoiceKey(trait.id) to it.choose },
            trait.spellChoice?.let { GuidedCharacterSetupViewModel.raceTraitSpellChoiceKey(trait.id) to it.choose },
        ).all { (key, choose) ->
            state.selection.choiceSelections[key].orEmpty().size == choose
        }
    }
}

private fun backgroundComplete(state: GuidedCharacterSetupUiState.Content): Boolean {
    val bg = state.selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id } } ?: return false
    val languageChoice = bg.languageChoice ?: return true
    return state.selection.choiceSelections[GuidedCharacterSetupViewModel.backgroundLanguageChoiceKey()].orEmpty().size == languageChoice.choose
}

private fun backgroundEquipmentComplete(state: GuidedCharacterSetupUiState.Content): Boolean {
    val bg = state.selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id } } ?: return true
    val equipmentChoice = bg.equipmentChoice ?: return true
    return state.selection.choiceSelections[GuidedCharacterSetupViewModel.backgroundEquipmentChoiceKey()].orEmpty().size == equipmentChoice.choose
}

private fun abilityAssignComplete(state: GuidedCharacterSetupUiState.Content): Boolean {
    return when (state.selection.abilityMethod) {
        AbilityScoreMethod.STANDARD_ARRAY -> {
            val values = state.selection.standardArrayAssignments.values.filterNotNull()
            values.size == 6 && values.sorted() == StandardArray.sorted()
        }

        AbilityScoreMethod.POINT_BUY -> state.selection.pointBuyScores.values.sumOf(::pointBuyCost) <= 27
        null -> false
    }
}

private fun classProficiencyChoicesComplete(state: GuidedCharacterSetupUiState.Content): Boolean {
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } } ?: return true
    return clazz.proficiencyChoices.withIndex().all { (index, choice) ->
        state.selection.choiceSelections[GuidedCharacterSetupViewModel.classProficiencyChoiceKey(index)]
            .orEmpty()
            .size == choice.choose
    }
}

private fun spellsComplete(state: GuidedCharacterSetupUiState.Content): Boolean {
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } } ?: return true
    val requirements = computeSpellRequirements(clazz, state.preview) ?: return true

    val selectedCantrips =
        state.selection.choiceSelections[GuidedCharacterSetupViewModel.spellCantripsChoiceKey()].orEmpty()
    if (requirements.cantrips > 0 && selectedCantrips.size != requirements.cantrips) return false

    val selectedSpells =
        state.selection.choiceSelections[GuidedCharacterSetupViewModel.spellLevel1ChoiceKey()].orEmpty()
    return !(requirements.level1Spells > 0 && selectedSpells.size != requirements.level1Spells)
}

private fun equipmentCategoryFromId(id: String): EquipmentCategory? = when (id) {
    "weapon" -> EquipmentCategory.WEAPON
    "armor" -> EquipmentCategory.ARMOR
    "tool" -> EquipmentCategory.TOOL
    "gear" -> EquipmentCategory.GEAR
    "holy-symbol" -> EquipmentCategory.HOLY_SYMBOL
    "standard" -> EquipmentCategory.STANDARD
    "musical-instrument" -> EquipmentCategory.MUSICAL_INSTRUMENT
    "gaming-set" -> EquipmentCategory.GAMING_SET
    "other" -> EquipmentCategory.OTHER
    "arcane-focus" -> EquipmentCategory.ARCANE_FOCUS
    "druidic-focus" -> EquipmentCategory.DRUIDIC_FOCUS
    "kit" -> EquipmentCategory.KIT
    "simple" -> EquipmentCategory.SIMPLE
    "martial" -> EquipmentCategory.MARTIAL
    "ranged" -> EquipmentCategory.RANGED
    "melee" -> EquipmentCategory.MELEE
    "shield" -> EquipmentCategory.SHIELD
    "light" -> EquipmentCategory.LIGHT
    "heavy" -> EquipmentCategory.HEAVY
    "medium" -> EquipmentCategory.MEDIUM
    "ammunition" -> EquipmentCategory.AMMUNITION
    "equipment-pack" -> EquipmentCategory.EQUIPMENT_PACK
    "artisans-tool" -> EquipmentCategory.ARTISANS_TOOL
    "gaming-sets" -> EquipmentCategory.GAMING_SETS
    "mounts-and-other-animals" -> EquipmentCategory.MOUNTS_AND_OTHER_ANIMALS
    "vehicle" -> EquipmentCategory.VEHICLE
    "tack-harness-and-drawn-vehicle" -> EquipmentCategory.TACK_HARNESS_AND_DRAWN_VEHICLE
    "waterborne-vehicle" -> EquipmentCategory.WATERBORNE_VEHICLE
    else -> null
}
