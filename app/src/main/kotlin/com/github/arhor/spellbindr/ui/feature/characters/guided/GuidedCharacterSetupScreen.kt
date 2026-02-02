@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.github.arhor.spellbindr.ui.feature.characters.guided

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.remember
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
            onSpellToggled = vm::onSpellToggled,
            onNext = vm::onNext,
            onPrev = vm::onBack,
            onCreate = vm::onCreateCharacter,
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
    onSpellToggled: (String) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onCreate: () -> Unit,
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
                onSpellToggled = onSpellToggled,
                onNext = onNext,
                onPrev = onPrev,
                onCreate = onCreate,
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
    onSpellToggled: (String) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onCreate: () -> Unit,
    validate: (GuidedCharacterSetupUiState.Content) -> GuidedValidationResult,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        LinearProgressIndicator(
            progress = { (state.currentStepIndex + 1).toFloat() / state.totalSteps.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (state.step) {
                GuidedStep.BASICS -> BasicsStep(state, onNameChanged)
                GuidedStep.CLASS -> ClassStep(state, onClassSelected)
                GuidedStep.CLASS_CHOICES -> ClassChoicesStep(state, onSubclassSelected, onChoiceToggled)
                GuidedStep.RACE -> RaceStep(state, onRaceSelected, onSubraceSelected, onChoiceToggled)
                GuidedStep.BACKGROUND -> BackgroundStep(state, onBackgroundSelected, onChoiceToggled)
                GuidedStep.ABILITY_METHOD -> AbilityMethodStep(state, onAbilityMethodSelected)
                GuidedStep.ABILITY_ASSIGN -> AbilityAssignStep(
                    state = state,
                    onStandardArrayAssigned = onStandardArrayAssigned,
                    onPointBuyIncrement = onPointBuyIncrement,
                    onPointBuyDecrement = onPointBuyDecrement,
                )

                GuidedStep.SKILLS_PROFICIENCIES -> SkillsStep(state, onChoiceToggled)
                GuidedStep.EQUIPMENT -> EquipmentStep(state, onChoiceToggled)
                GuidedStep.SPELLS -> SpellsStep(state, onSpellToggled)
                GuidedStep.REVIEW -> ReviewStep(state, validate(state))
            }
        }

        HorizontalDivider()

        val isReview = state.step == GuidedStep.REVIEW
        val canGoPrev = state.currentStepIndex > 0 && !state.isSaving
        val validation = if (isReview) validate(state) else null
        val canContinue = if (isReview) !validation!!.hasErrors && !state.isSaving else canProceedFromStep(state)

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
) {
    Text(
        text = "Level 1 (2014 rules)",
        style = MaterialTheme.typography.titleMedium,
    )
    OutlinedTextField(
        value = state.name,
        onValueChange = onNameChanged,
        label = { Text("Name (optional)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    Text(
        text = "You can fill more details later in the full editor.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ClassStep(
    state: GuidedCharacterSetupUiState.Content,
    onClassSelected: (String) -> Unit,
) {
    Text(
        text = "Choose a class",
        style = MaterialTheme.typography.titleMedium,
    )
    state.classes.forEach { clazz ->
        SelectCard(
            title = clazz.name,
            selected = state.selection.classId == clazz.id,
            onClick = { onClassSelected(clazz.id) },
        )
    }
}

@Composable
private fun ClassChoicesStep(
    state: GuidedCharacterSetupUiState.Content,
    onSubclassSelected: (String) -> Unit,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
) {
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } }
    if (clazz == null) {
        Text("Choose a class first.")
        return
    }

    Text(
        text = "Level 1 choices",
        style = MaterialTheme.typography.titleMedium,
    )

    if (clazz.id in setOf("cleric", "sorcerer", "warlock")) {
        Text(
            text = "Subclass",
            style = MaterialTheme.typography.titleSmall,
        )
        clazz.subclasses.forEach { subclass ->
            SelectRow(
                title = subclass.name,
                selected = state.selection.subclassId == subclass.id,
                onClick = { onSubclassSelected(subclass.id) },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    val level1FeatureIds = clazz.levels.firstOrNull { it.level == 1 }?.features.orEmpty()
    val featureChoices = level1FeatureIds.mapNotNull { featureId ->
        val feature = state.featuresById[featureId] ?: return@mapNotNull null
        val choice = feature.choice ?: return@mapNotNull null
        Triple(featureId, feature, choice)
    }

    featureChoices.forEach { (featureId, feature, choice) ->
        ChoiceSection(
            title = feature.name,
            description = null,
            choice = choice,
            selected = state.selection.choiceSelections[GuidedCharacterSetupViewModel.featureChoiceKey(featureId)].orEmpty(),
            options = resolveOptions(choice, state),
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

@Composable
private fun RaceStep(
    state: GuidedCharacterSetupUiState.Content,
    onRaceSelected: (String) -> Unit,
    onSubraceSelected: (String) -> Unit,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
) {
    Text(
        text = "Choose a race",
        style = MaterialTheme.typography.titleMedium,
    )
    state.races.forEach { race ->
        SelectCard(
            title = race.name,
            selected = state.selection.raceId == race.id,
            onClick = { onRaceSelected(race.id) },
        )
    }

    val race = state.selection.raceId?.let { id -> state.races.firstOrNull { it.id == id } } ?: return

    if (race.subraces.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Subrace",
            style = MaterialTheme.typography.titleSmall,
        )
        race.subraces.forEach { subrace ->
            SelectRow(
                title = subrace.name,
                selected = state.selection.subraceId == subrace.id,
                onClick = { onSubraceSelected(subrace.id) },
            )
        }
    }

    val traitIds = buildList {
        addAll(race.traits.map { it.id })
        val subrace = state.selection.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
        if (subrace != null) {
            addAll(subrace.traits.map { it.id })
        }
    }
    val traits = traitIds.mapNotNull { state.traitsById[it] }
    val abilityBonusChoices = traits.mapNotNull { trait ->
        val choice = trait.abilityBonusChoice as? Choice.AbilityBonusChoice ?: return@mapNotNull null
        trait to choice
    }
    val languageChoices = traits.mapNotNull { trait ->
        val choice = trait.languageChoice ?: return@mapNotNull null
        trait to choice
    }
    val proficiencyChoices = traits.mapNotNull { trait ->
        val choice = trait.proficiencyChoice ?: return@mapNotNull null
        trait to choice
    }

    if (abilityBonusChoices.isNotEmpty() || languageChoices.isNotEmpty() || proficiencyChoices.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Race choices",
            style = MaterialTheme.typography.titleSmall,
        )
    }

    abilityBonusChoices.forEach { (trait, choice) ->
        val options: List<String> = choice.from.flatMap { it.keys }.distinct()
        ChoiceSection(
            title = trait.name,
            description = "Choose ${choice.choose}",
            choice = choice,
            selected = state.selection.choiceSelections[GuidedCharacterSetupViewModel.raceTraitAbilityBonusChoiceKey(
                trait.id
            )].orEmpty(),
            options = options.associateWith { id -> "${id.displayName()} +1" },
            onToggle = { optionId ->
                onChoiceToggled(
                    GuidedCharacterSetupViewModel.raceTraitAbilityBonusChoiceKey(trait.id),
                    optionId,
                    choice.choose,
                )
            },
        )
    }

    languageChoices.forEach { (trait, choice) ->
        val choiceKey = GuidedCharacterSetupViewModel.raceTraitLanguageChoiceKey(trait.id)
        ChoiceSection(
            title = trait.name,
            description = "Choose ${choice.choose}",
            choice = choice,
            selected = state.selection.choiceSelections[choiceKey].orEmpty(),
            options = resolveOptions(choice, state),
            disabledOptions = computeAlreadySelectedLanguageReasons(state, choiceKey),
            onToggle = { optionId ->
                onChoiceToggled(
                    choiceKey,
                    optionId,
                    choice.choose,
                )
            },
        )
    }

    proficiencyChoices.forEach { (trait, choice) ->
        ChoiceSection(
            title = trait.name,
            description = "Choose ${choice.choose}",
            choice = choice,
            selected = state.selection.choiceSelections[GuidedCharacterSetupViewModel.raceTraitProficiencyChoiceKey(
                trait.id
            )].orEmpty(),
            options = resolveOptions(choice, state),
            onToggle = { optionId ->
                onChoiceToggled(
                    GuidedCharacterSetupViewModel.raceTraitProficiencyChoiceKey(trait.id),
                    optionId,
                    choice.choose,
                )
            },
        )
    }
}

@Composable
private fun BackgroundStep(
    state: GuidedCharacterSetupUiState.Content,
    onBackgroundSelected: (String) -> Unit,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
) {
    Text(
        text = "Choose a background",
        style = MaterialTheme.typography.titleMedium,
    )
    state.backgrounds.forEach { bg ->
        SelectCard(
            title = bg.name,
            selected = state.selection.backgroundId == bg.id,
            onClick = { onBackgroundSelected(bg.id) },
        )
    }

    val bg = state.selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id } } ?: return
    val languageChoice = bg.languageChoice ?: return

    Spacer(modifier = Modifier.height(8.dp))
    val choiceKey = GuidedCharacterSetupViewModel.backgroundLanguageChoiceKey()
    ChoiceSection(
        title = "Languages",
        description = "Choose ${languageChoice.choose}",
        choice = languageChoice,
        selected = state.selection.choiceSelections[choiceKey].orEmpty(),
        options = resolveOptions(languageChoice, state),
        disabledOptions = computeAlreadySelectedLanguageReasons(state, choiceKey),
        onToggle = { optionId ->
            onChoiceToggled(
                choiceKey,
                optionId,
                languageChoice.choose,
            )
        },
    )
}

@Composable
private fun AbilityMethodStep(
    state: GuidedCharacterSetupUiState.Content,
    onAbilityMethodSelected: (AbilityScoreMethod) -> Unit,
) {
    Text(
        text = "Ability score method",
        style = MaterialTheme.typography.titleMedium,
    )
    AbilityScoreMethod.entries.forEach { method ->
        SelectRow(
            title = method.label,
            selected = state.selection.abilityMethod == method,
            onClick = { onAbilityMethodSelected(method) },
        )
    }
    Text(
        text = "Racial ability score increases are applied after this step.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun AbilityAssignStep(
    state: GuidedCharacterSetupUiState.Content,
    onStandardArrayAssigned: (AbilityId, Int?) -> Unit,
    onPointBuyIncrement: (AbilityId) -> Unit,
    onPointBuyDecrement: (AbilityId) -> Unit,
) {
    val method = state.selection.abilityMethod
    if (method == null) {
        Text("Choose a method first.")
        return
    }

    Text(
        text = "Assign scores",
        style = MaterialTheme.typography.titleMedium,
    )

    when (method) {
        AbilityScoreMethod.STANDARD_ARRAY -> StandardArrayAssign(state, onStandardArrayAssigned)
        AbilityScoreMethod.POINT_BUY -> PointBuyAssign(state, onPointBuyIncrement, onPointBuyDecrement)
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
) {
    Text(
        text = "Skills & proficiencies",
        style = MaterialTheme.typography.titleMedium,
    )
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } }
    if (clazz == null) {
        Text("Choose a class first.")
        return
    }

    if (clazz.proficiencyChoices.isEmpty()) {
        Text(
            text = "No additional choices for this class.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    clazz.proficiencyChoices.forEachIndexed { index, choice ->
        ChoiceSection(
            title = "Class choice ${index + 1}",
            description = (choice as? Choice.OptionsArrayChoice)?.desc ?: "Choose ${choice.choose}",
            choice = choice,
            selected = state.selection.choiceSelections[GuidedCharacterSetupViewModel.classProficiencyChoiceKey(index)].orEmpty(),
            options = resolveOptions(choice, state),
            onToggle = { optionId ->
                onChoiceToggled(
                    GuidedCharacterSetupViewModel.classProficiencyChoiceKey(index),
                    optionId,
                    choice.choose,
                )
            },
        )
    }
}

@Composable
private fun EquipmentStep(
    state: GuidedCharacterSetupUiState.Content,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
) {
    Text(
        text = "Starting equipment",
        style = MaterialTheme.typography.titleMedium,
    )

    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } }
    val bg = state.selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id } }

    Text(
        text = "This MVP supports fixed starting equipment and simple background equipment choices. Some class equipment options may be missing.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    val fixed = buildList<String> {
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

    if (fixed.isNotEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Included", style = MaterialTheme.typography.titleSmall)
                fixed.forEach { Text(text = "• $it", style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }

    val equipmentChoice = bg?.equipmentChoice
    if (equipmentChoice != null) {
        ChoiceSection(
            title = "Background equipment",
            description = "Choose ${equipmentChoice.choose}",
            choice = equipmentChoice,
            selected = state.selection.choiceSelections[GuidedCharacterSetupViewModel.backgroundEquipmentChoiceKey()].orEmpty(),
            options = resolveOptions(equipmentChoice, state),
            onToggle = { optionId ->
                onChoiceToggled(
                    GuidedCharacterSetupViewModel.backgroundEquipmentChoiceKey(),
                    optionId,
                    equipmentChoice.choose,
                )
            },
        )
    }
}

@Composable
private fun SpellsStep(
    state: GuidedCharacterSetupUiState.Content,
    onSpellToggled: (String) -> Unit,
) {
    Text(
        text = "Spells",
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = "MVP note: spell limits/preparation rules aren’t enforced yet. You can also add spells later from the Character Sheet.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } }
    val isSpellcaster = clazz?.spellcasting?.level == 1
    if (!isSpellcaster) {
        Text(
            text = "This class doesn’t cast spells at level 1.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    // Keep it lightweight for MVP: allow a small curated pick list (empty by default).
    Text(
        text = "Spell selection UI will be added next. For now, continue and add spells on the sheet.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ReviewStep(
    state: GuidedCharacterSetupUiState.Content,
    validation: GuidedValidationResult,
) {
    Text(
        text = "Review",
        style = MaterialTheme.typography.titleMedium,
    )

    SummaryCard(
        title = "Summary",
        lines = listOfNotNull(
            "Name: ${state.name.ifBlank { "—" }}",
            "Class: ${state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id }?.name } ?: "—"}",
            "Subclass: ${state.selection.subclassId ?: "—"}",
            "Race: ${state.selection.raceId?.let { id -> state.races.firstOrNull { it.id == id }?.name } ?: "—"}",
            "Subrace: ${state.selection.subraceId ?: "—"}",
            "Background: ${state.selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id }?.name } ?: "—"}",
            "Ability method: ${state.selection.abilityMethod?.label ?: "—"}",
        ),
    )

    if (validation.issues.isEmpty()) {
        Text(
            text = "All set.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    SummaryCard(
        title = "Checks",
        lines = validation.issues.map { issue ->
            val prefix = if (issue.severity == GuidedValidationIssue.Severity.ERROR) "Error" else "Note"
            "$prefix: ${issue.message}"
        },
    )
}

@Composable
private fun SummaryCard(
    title: String,
    lines: List<String>,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            lines.forEach { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
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
            options.forEach { (id, label) ->
                val alreadySelectedReason = disabledOptions[id]
                val isSelectedHere = id in selected
                val isAlreadyHave = alreadySelectedReason != null && !isSelectedHere
                val enabled = when {
                    isSelectedHere -> true
                    alreadySelectedReason != null -> false
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
                    Checkbox(
                        checked = isSelectedHere || isAlreadyHave,
                        onCheckedChange = { onToggle(id) },
                        enabled = enabled,
                    )
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
                                text = "Already selected — $alreadySelectedReason",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
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

private fun displayNameForOption(id: String, state: GuidedCharacterSetupUiState.Content): String {
    if (id.startsWith("skill-")) {
        val normalized = id.removePrefix("skill-").replace("-", "_").uppercase()
        val skill = Skill.entries.firstOrNull { it.name == normalized }
        if (skill != null) return skill.displayName
    }
    state.languagesById[id]?.let { return it.name }
    state.equipmentById[id]?.let { return it.name }
    state.featuresById[id]?.let { return it.name }
    return EntityRef(id).prettyString()
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
        GuidedStep.SPELLS -> true
        GuidedStep.REVIEW -> false
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
