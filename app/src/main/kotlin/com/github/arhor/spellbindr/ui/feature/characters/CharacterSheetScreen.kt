@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.github.arhor.spellbindr.ui.feature.characters

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.data.model.predefined.Skill
import com.github.arhor.spellbindr.ui.AppTopBarConfig
import com.github.arhor.spellbindr.ui.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.WithAppTopBar
import com.github.arhor.spellbindr.ui.components.AbilityTokenData
import com.github.arhor.spellbindr.ui.components.AbilityTokensGrid
import com.github.arhor.spellbindr.ui.components.D20HpBar
import com.github.arhor.spellbindr.ui.theme.AppTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun CharacterSheetRoute(
    onBack: () -> Unit,
    onEditCharacter: (String) -> Unit,
    onOpenSpellDetail: (String) -> Unit,
    onAddSpells: (String) -> Unit,
    savedStateHandle: SavedStateHandle,
    modifier: Modifier = Modifier,
    viewModel: CharacterSheetViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(savedStateHandle) {
        savedStateHandle.getStateFlow<List<CharacterSpellAssignment>?>(CHARACTER_SPELL_SELECTION_RESULT_KEY, null)
            .collectLatest { assignments ->
                if (!assignments.isNullOrEmpty()) {
                    viewModel.addSpells(assignments)
                    savedStateHandle[CHARACTER_SPELL_SELECTION_RESULT_KEY] = null
                }
            }
    }

    CharacterSheetScreen(
        state = state,
        onBack = onBack,
        callbacks = CharacterSheetCallbacks(
            onTabSelected = viewModel::onTabSelected,
            onEnterEdit = viewModel::enterEditMode,
            onCancelEdit = viewModel::cancelEditMode,
            onSaveEdits = viewModel::saveInlineEdits,
            onAdjustHp = viewModel::adjustCurrentHp,
            onTempHpChanged = viewModel::setTemporaryHp,
            onMaxHpEdited = viewModel::onMaxHpEdited,
            onCurrentHpEdited = viewModel::onCurrentHpEdited,
            onTempHpEdited = viewModel::onTemporaryHpEdited,
            onSpeedEdited = viewModel::onSpeedEdited,
            onHitDiceEdited = viewModel::onHitDiceEdited,
            onSensesEdited = viewModel::onSensesEdited,
            onLanguagesEdited = viewModel::onLanguagesEdited,
            onProficienciesEdited = viewModel::onProficienciesEdited,
            onEquipmentEdited = viewModel::onEquipmentEdited,
            onDeathSaveSuccessesChanged = viewModel::setDeathSaveSuccesses,
            onDeathSaveFailuresChanged = viewModel::setDeathSaveFailures,
            onSpellSlotToggle = viewModel::toggleSpellSlot,
            onSpellSlotTotalChanged = viewModel::setSpellSlotTotal,
            onSpellRemoved = viewModel::removeSpell,
            onSpellSelected = onOpenSpellDetail,
            onAddSpellsClicked = { state.characterId?.let(onAddSpells) },
            onOpenFullEditor = { state.characterId?.let(onEditCharacter) },
        ),
        modifier = modifier,
    )
}

@Composable
fun CharacterSheetScreen(
    state: CharacterSheetUiState,
    onBack: () -> Unit,
    callbacks: CharacterSheetCallbacks,
    modifier: Modifier = Modifier,
) {
    var overflowExpanded by remember { mutableStateOf(false) }
    val headerState = state.header

    WithAppTopBar(
        AppTopBarConfig(
            visible = true,
            title = { CharacterSheetTopBarTitle(header = headerState) },
            navigation = AppTopBarNavigation.Back(onBack),
            actions = {
                CharacterSheetTopBarActions(
                    state = state,
                    callbacks = callbacks,
                    onOverflowOpen = { overflowExpanded = true },
                )
                DropdownMenu(
                    expanded = overflowExpanded,
                    onDismissRequest = { overflowExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Open full editor") },
                        onClick = {
                            overflowExpanded = false
                            callbacks.onOpenFullEditor()
                        },
                        enabled = state.characterId != null,
                    )
                }
            },
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                state.errorMessage != null -> {
                    CharacterSheetError(
                        message = state.errorMessage,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp),
                    )
                }

                state.header != null && state.overview != null && state.skills != null && state.spells != null -> {
                    CharacterSheetContent(
                        header = state.header,
                        state = state,
                        callbacks = callbacks,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterSheetTopBarTitle(
    header: CharacterHeaderUiState?,
) {
    if (header == null) {
        Text(
            text = "Character",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    } else {
        Column {
            Text(
                text = header.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (header.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = header.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CharacterSheetTopBarActions(
    state: CharacterSheetUiState,
    callbacks: CharacterSheetCallbacks,
    onOverflowOpen: () -> Unit,
) {
    when (state.editMode) {
        SheetEditMode.View -> {
            TextButton(
                onClick = callbacks.onEnterEdit,
                enabled = state.header != null,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit")
            }
            IconButton(onClick = onOverflowOpen, enabled = state.characterId != null) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More")
            }
        }

        SheetEditMode.Editing -> {
            TextButton(onClick = callbacks.onCancelEdit) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cancel")
            }
            TextButton(onClick = callbacks.onSaveEdits) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save")
            }
        }
    }
}

@Composable
private fun CharacterSheetContent(
    state: CharacterSheetUiState,
    header: CharacterHeaderUiState,
    callbacks: CharacterSheetCallbacks,
    modifier: Modifier = Modifier,
) {
    val tabs = CharacterSheetTab.entries
    val pagerState = rememberPagerState { tabs.size }
    val currentSelectedTab by rememberUpdatedState(state.selectedTab)

    LaunchedEffect(state.selectedTab) {
        val targetPage = state.selectedTab.ordinal
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                val pageTab = tabs[page]
                if (pageTab != currentSelectedTab) {
                    callbacks.onTabSelected(pageTab)
                }
            }
    }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        PrimaryTabRow(selectedTabIndex = state.selectedTab.ordinal) {
            tabs.forEach { tab ->
                Tab(
                    selected = state.selectedTab == tab,
                    onClick = { callbacks.onTabSelected(tab) },
                    text = { Text(tab.name.lowercase().replaceFirstChar(Char::titlecase)) },
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (tabs[page]) {
                CharacterSheetTab.Overview -> OverviewTab(
                    header = header,
                    overview = requireNotNull(state.overview),
                    editMode = state.editMode,
                    editingState = state.editingState,
                    callbacks = callbacks,
                    modifier = Modifier.fillMaxSize(),
                )

                CharacterSheetTab.Skills -> SkillsTab(
                    skills = requireNotNull(state.skills),
                    modifier = Modifier.fillMaxSize(),
                )

                CharacterSheetTab.Spells -> SpellsTab(
                    spellsState = requireNotNull(state.spells),
                    editMode = state.editMode,
                    callbacks = callbacks,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun HitPointBlock(
    hitPoints: HitPointSummary,
    editMode: SheetEditMode,
    editingState: CharacterSheetEditingState?,
    callbacks: CharacterSheetCallbacks,
    onHitPointsClick: (() -> Unit)?,
) {
    when {
        editMode == SheetEditMode.Editing && editingState != null -> {
            Column {
                Text(
                    text = "Hit Points",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InlineNumberField(
                        label = "Current",
                        value = editingState.currentHp,
                        onValueChanged = callbacks.onCurrentHpEdited,
                        modifier = Modifier.weight(1f),
                    )
                    InlineNumberField(
                        label = "Max",
                        value = editingState.maxHp,
                        onValueChanged = callbacks.onMaxHpEdited,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                InlineNumberField(
                    label = "Temporary",
                    value = editingState.tempHp,
                    onValueChanged = callbacks.onTempHpEdited,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        else -> {
            val hpModifier = if (onHitPointsClick != null) {
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(onClick = onHitPointsClick)
            } else {
                Modifier.fillMaxWidth()
            }
            Column(
                modifier = hpModifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                D20HpBar(
                    currentHp = hitPoints.current,
                    maxHp = hitPoints.max,
                )

                Text(
                    text = "HP",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CombatOverviewCard(
    header: CharacterHeaderUiState,
    editMode: SheetEditMode,
    editingState: CharacterSheetEditingState?,
    callbacks: CharacterSheetCallbacks,
    onHitPointsClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        if (editMode == SheetEditMode.Editing && editingState != null) {
            Column(modifier = Modifier.padding(16.dp)) {
                HitPointBlock(
                    hitPoints = header.hitPoints,
                    editMode = editMode,
                    editingState = editingState,
                    callbacks = callbacks,
                    onHitPointsClick = onHitPointsClick,
                )
                Spacer(modifier = Modifier.height(20.dp))
                StatsRow(
                    header = header,
                    editMode = editMode,
                    editingState = editingState,
                    callbacks = callbacks,
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    HitPointBlock(
                        hitPoints = header.hitPoints,
                        editMode = editMode,
                        editingState = editingState,
                        callbacks = callbacks,
                        onHitPointsClick = onHitPointsClick,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CombatStatRow(
                        label = "AC",
                        value = header.armorClass.toString(),
                    )
                    CombatStatRow(
                        label = "Initiative",
                        value = formatBonus(header.initiative),
                    )
                    CombatStatRow(
                        label = "Speed",
                        value = header.speed,
                    )
                }
            }
        }
    }
}

@Composable
private fun HitPointAdjustDialog(
    hitPoints: HitPointSummary,
    onAdjustHp: (Int) -> Unit,
    onTempHpChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        title = { Text("Adjust hit points") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${hitPoints.current} / ${hitPoints.max}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Column {
                    Text(
                        text = "Modify HP",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(-5, -1, 1, 5).forEach { delta ->
                            val label = if (delta > 0) "+$delta" else delta.toString()
                            HpAdjustButton(
                                label = label,
                                onClick = { onAdjustHp(delta) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                Column {
                    Text(
                        text = "Temporary HP ${hitPoints.temporary}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AssistChip(
                            onClick = { onTempHpChanged((hitPoints.temporary - 1).coerceAtLeast(0)) },
                            label = { Text("-1") },
                            enabled = hitPoints.temporary > 0,
                        )
                        AssistChip(
                            onClick = { onTempHpChanged(hitPoints.temporary + 1) },
                            label = { Text("+1") },
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun StatsRow(
    header: CharacterHeaderUiState,
    editMode: SheetEditMode,
    editingState: CharacterSheetEditingState?,
    callbacks: CharacterSheetCallbacks,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatChip(label = "AC", value = header.armorClass.toString(), modifier = Modifier.weight(1f))
        StatChip(label = "Initiative", value = formatBonus(header.initiative), modifier = Modifier.weight(1f))
        if (editMode == SheetEditMode.Editing && editingState != null) {
            InlineTextField(
                label = "Speed",
                value = editingState.speed,
                onValueChanged = callbacks.onSpeedEdited,
                modifier = Modifier.weight(1f),
            )
        } else {
            StatChip(label = "Speed", value = header.speed, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun HpAdjustButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 12.dp,
            top = ButtonDefaults.ContentPadding.calculateTopPadding(),
            end = 12.dp,
            bottom = ButtonDefaults.ContentPadding.calculateBottomPadding(),
        ),
    ) {
        Text(label)
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CombatStatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun OverviewTab(
    header: CharacterHeaderUiState,
    overview: OverviewTabState,
    editMode: SheetEditMode,
    editingState: CharacterSheetEditingState?,
    callbacks: CharacterSheetCallbacks,
    modifier: Modifier = Modifier,
) {
    var showHpAdjustDialog by remember { mutableStateOf(false) }
    val onHitPointsClick = if (editMode == SheetEditMode.View) {
        { showHpAdjustDialog = true }
    } else {
        null
    }

    if (showHpAdjustDialog) {
        HitPointAdjustDialog(
            hitPoints = header.hitPoints,
            onAdjustHp = callbacks.onAdjustHp,
            onTempHpChanged = callbacks.onTempHpChanged,
            onDismiss = { showHpAdjustDialog = false },
        )
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            val isAtOrBelowZeroHp = header.hitPoints.current <= 0
            if (isAtOrBelowZeroHp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CombatOverviewCard(
                        header = header,
                        editMode = editMode,
                        editingState = editingState,
                        callbacks = callbacks,
                        onHitPointsClick = onHitPointsClick,
                        modifier = Modifier.weight(1f),
                    )
                    DeathSavesCard(
                        state = overview.deathSaves,
                        onSuccessChanged = callbacks.onDeathSaveSuccessesChanged,
                        onFailureChanged = callbacks.onDeathSaveFailuresChanged,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                CombatOverviewCard(
                    header = header,
                    editMode = editMode,
                    editingState = editingState,
                    callbacks = callbacks,
                    onHitPointsClick = onHitPointsClick,
                )
            }
        }
        item {
            AbilityTokensGrid(
                abilities = overview.abilities.map { ability ->
                    AbilityTokenData(
                        abbreviation = ability.label,
                        score = ability.score,
                        modifier = ability.modifier,
                        savingThrowBonus = ability.savingThrowBonus,
                        proficient = ability.savingThrowProficient,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            DetailCard(
                title = "Core details",
                lines = listOfNotBlank(
                    overview.race.takeIf { it.isNotBlank() }?.let { "Race: $it" },
                    overview.background.takeIf { it.isNotBlank() }?.let { "Background: $it" },
                    overview.alignment.takeIf { it.isNotBlank() }?.let { "Alignment: $it" },
                ),
            )
        }
        item {
            if (editMode == SheetEditMode.Editing && editingState != null) {
                EditableDetailCard(
                    title = "Senses & Languages",
                    primaryLabel = "Senses",
                    primaryValue = editingState.senses,
                    onPrimaryChanged = callbacks.onSensesEdited,
                    secondaryLabel = "Languages",
                    secondaryValue = editingState.languages,
                    onSecondaryChanged = callbacks.onLanguagesEdited,
                )
            } else {
                DetailCard(
                    title = "Senses & Languages",
                    lines = listOfNotBlank(
                        overview.senses.takeIf { it.isNotBlank() }?.let { "Senses: $it" },
                        overview.languages.takeIf { it.isNotBlank() }?.let { "Languages: $it" },
                    ),
                )
            }
        }
        item {
            if (editMode == SheetEditMode.Editing && editingState != null) {
                EditableDetailCard(
                    title = "Proficiencies & Equipment",
                    primaryLabel = "Proficiencies",
                    primaryValue = editingState.proficiencies,
                    onPrimaryChanged = callbacks.onProficienciesEdited,
                    secondaryLabel = "Equipment",
                    secondaryValue = editingState.equipment,
                    onSecondaryChanged = callbacks.onEquipmentEdited,
                )
            } else {
                DetailCard(
                    title = "Proficiencies & Equipment",
                    lines = listOfNotBlank(
                        overview.proficiencies.takeIf { it.isNotBlank() }?.let { "Proficiencies:\n$it" },
                        overview.equipment.takeIf { it.isNotBlank() }?.let { "Equipment:\n$it" },
                    ),
                )
            }
        }
        item {
            if (editMode == SheetEditMode.Editing && editingState != null) {
                InlineTextField(
                    label = "Hit Dice",
                    value = editingState.hitDice,
                    onValueChanged = callbacks.onHitDiceEdited,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                DetailCard(
                    title = "Hit Dice",
                    lines = listOf(overview.hitDice.ifBlank { "—" }),
                )
            }
        }
    }
}

@Composable
private fun DeathSavesCard(
    state: DeathSaveUiState,
    onSuccessChanged: (Int) -> Unit,
    onFailureChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Death Saves",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            DeathSaveTrack(
                label = "Successes",
                count = state.successes,
                color = MaterialTheme.colorScheme.primary,
                onCountChanged = onSuccessChanged,
            )
            Spacer(modifier = Modifier.height(8.dp))
            DeathSaveTrack(
                label = "Failures",
                count = state.failures,
                color = MaterialTheme.colorScheme.error,
                onCountChanged = onFailureChanged,
            )
        }
    }
}

@Composable
private fun DeathSaveTrack(
    label: String,
    count: Int,
    color: Color,
    onCountChanged: (Int) -> Unit,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { index ->
                val isFilled = index < count
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            color = if (isFilled) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 1.dp,
                            color = if (isFilled) color else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape,
                        )
                        .padding(6.dp)
                        .clickable { onCountChanged(if (isFilled) index else index + 1) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isFilled) {
                        Icon(
                            imageVector = if (label == "Failures") Icons.Rounded.Close else Icons.Rounded.Check,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    lines: List<String>,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (lines.isEmpty()) "No information yet" else lines.joinToString("\n\n"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EditableDetailCard(
    title: String,
    primaryLabel: String,
    primaryValue: String,
    onPrimaryChanged: (String) -> Unit,
    secondaryLabel: String,
    secondaryValue: String,
    onSecondaryChanged: (String) -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            InlineTextField(
                label = primaryLabel,
                value = primaryValue,
                onValueChanged = onPrimaryChanged,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            InlineTextField(
                label = secondaryLabel,
                value = secondaryValue,
                onValueChanged = onSecondaryChanged,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun InlineNumberField(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
private fun InlineTextField(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
    )
}

@Composable
private fun SkillsTab(
    skills: SkillsTabState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(skills.skills) { skill ->
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = skill.name,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = "${skill.abilityAbbreviation} • ${formatBonus(skill.totalBonus)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (skill.expertise) {
                            AssistChip(onClick = {}, label = { Text("Expertise") })
                        } else if (skill.proficient) {
                            AssistChip(onClick = {}, label = { Text("Proficient") })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpellsTab(
    spellsState: SpellsTabState,
    editMode: SheetEditMode,
    callbacks: CharacterSheetCallbacks,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            FilledTonalButton(
                onClick = callbacks.onAddSpellsClicked,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add spells")
            }
        }
        item {
            SpellSlotsCard(
                slots = spellsState.spellSlots,
                editMode = editMode,
                callbacks = callbacks,
            )
        }
        if (spellsState.spellcastingGroups.isEmpty()) {
            item {
                Text(
                    text = "No spells linked to this character yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            }
        } else {
            items(spellsState.spellcastingGroups) { group ->
                SpellGroupCard(
                    group = group,
                    editMode = editMode,
                    callbacks = callbacks,
                )
            }
        }
    }
}

@Composable
private fun SpellSlotsCard(
    slots: List<SpellSlotUiModel>,
    editMode: SheetEditMode,
    callbacks: CharacterSheetCallbacks,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Spell Slots",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                slots.forEach { slot ->
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "Level ${slot.level}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            if (editMode == SheetEditMode.Editing) {
                                Row {
                                    IconButton(
                                        onClick = { callbacks.onSpellSlotTotalChanged(slot.level, slot.total - 1) },
                                        enabled = slot.total > 0,
                                    ) {
                                        Text("-")
                                    }
                                    IconButton(
                                        onClick = { callbacks.onSpellSlotTotalChanged(slot.level, slot.total + 1) },
                                    ) {
                                        Text("+")
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (slot.total == 0) {
                            Text(
                                text = "No slots configured for this level",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                repeat(slot.total) { index ->
                                    val used = index < slot.expended
                                    SpellSlotChip(
                                        used = used,
                                        onClick = { callbacks.onSpellSlotToggle(slot.level, index) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpellSlotChip(
    used: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        tonalElevation = if (used) 4.dp else 0.dp,
        color = if (used) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (used) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun SpellGroupCard(
    group: SpellcastingGroupUiModel,
    editMode: SheetEditMode,
    callbacks: CharacterSheetCallbacks,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = group.sourceLabel,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            group.spells.forEach { spell ->
                SpellRow(
                    spell = spell,
                    editMode = editMode,
                    onClick = { callbacks.onSpellSelected(spell.spellId) },
                    onRemove = { callbacks.onSpellRemoved(spell.spellId, group.sourceKey) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SpellRow(
    spell: CharacterSpellUiModel,
    editMode: SheetEditMode,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = spell.name,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Level ${spell.level} • ${spell.school}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (spell.castingTime.isNotBlank()) {
                    Text(
                        text = spell.castingTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (editMode == SheetEditMode.Editing) {
                IconButton(onClick = onRemove) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "Remove spell")
                }
            }
        }
    }
}

@Composable
private fun CharacterSheetError(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Unable to load character",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

data class CharacterSheetCallbacks(
    val onTabSelected: (CharacterSheetTab) -> Unit,
    val onEnterEdit: () -> Unit,
    val onCancelEdit: () -> Unit,
    val onSaveEdits: () -> Unit,
    val onAdjustHp: (Int) -> Unit,
    val onTempHpChanged: (Int) -> Unit,
    val onMaxHpEdited: (String) -> Unit,
    val onCurrentHpEdited: (String) -> Unit,
    val onTempHpEdited: (String) -> Unit,
    val onSpeedEdited: (String) -> Unit,
    val onHitDiceEdited: (String) -> Unit,
    val onSensesEdited: (String) -> Unit,
    val onLanguagesEdited: (String) -> Unit,
    val onProficienciesEdited: (String) -> Unit,
    val onEquipmentEdited: (String) -> Unit,
    val onDeathSaveSuccessesChanged: (Int) -> Unit,
    val onDeathSaveFailuresChanged: (Int) -> Unit,
    val onSpellSlotToggle: (Int, Int) -> Unit,
    val onSpellSlotTotalChanged: (Int, Int) -> Unit,
    val onSpellRemoved: (String, String) -> Unit,
    val onSpellSelected: (String) -> Unit,
    val onAddSpellsClicked: () -> Unit,
    val onOpenFullEditor: () -> Unit,
)

private fun listOfNotBlank(vararg values: String?): List<String> =
    values.mapNotNull { value -> value?.takeIf { it.isNotBlank() }?.trim() }

private fun formatBonus(value: Int): String = if (value >= 0) "+$value" else value.toString()

@Preview
@Composable
private fun CharacterSheetPreview() {
    AppTheme {
        CharacterSheetScreen(
            state = previewUiState(),
            onBack = {},
            callbacks = CharacterSheetCallbacks(
                onTabSelected = {},
                onEnterEdit = {},
                onCancelEdit = {},
                onSaveEdits = {},
                onAdjustHp = {},
                onTempHpChanged = {},
                onMaxHpEdited = {},
                onCurrentHpEdited = {},
                onTempHpEdited = {},
                onSpeedEdited = {},
                onHitDiceEdited = {},
                onSensesEdited = {},
                onLanguagesEdited = {},
                onProficienciesEdited = {},
                onEquipmentEdited = {},
                onDeathSaveSuccessesChanged = {},
                onDeathSaveFailuresChanged = {},
                onSpellSlotToggle = { _, _ -> },
                onSpellSlotTotalChanged = { _, _ -> },
                onSpellRemoved = { _, _ -> },
                onSpellSelected = {},
                onAddSpellsClicked = {},
                onOpenFullEditor = {},
            ),
        )
    }
}

private fun previewUiState(): CharacterSheetUiState = CharacterSheetUiState(
    characterId = "preview",
    selectedTab = CharacterSheetTab.Overview,
    header = CharacterHeaderUiState(
        name = "Astra Moonshadow",
        subtitle = "Level 7 Wizard • Half-elf",
        hitPoints = HitPointSummary(max = 38, current = 13, temporary = 5),
        armorClass = 16,
        initiative = 2,
        speed = "30 ft",
        proficiencyBonus = 3,
        inspiration = true,
    ),
    overview = OverviewTabState(
        abilities = Ability.entries.mapIndexed { index, ability ->
            AbilityUiModel(
                ability = ability,
                label = ability.name,
                score = 10 + index * 2,
                modifier = index - 1,
                savingThrowBonus = index + 2,
                savingThrowProficient = index % 2 == 0,
            )
        },
        hitDice = "7d6",
        senses = "Darkvision 60 ft",
        languages = "Common, Elvish",
        proficiencies = "Arcana, History, Insight",
        equipment = "Quarterstaff, Spellbook",
        background = "Sage",
        race = "Half-elf",
        alignment = "Chaotic Good",
        deathSaves = DeathSaveUiState(successes = 1, failures = 0),
    ),
    skills = SkillsTabState(
        skills = Skill.entries.take(6).mapIndexed { index, skill ->
            SkillUiModel(
                id = skill,
                name = skill.displayName,
                abilityAbbreviation = skill.ability.name,
                totalBonus = index,
                proficient = index % 2 == 0,
                expertise = index == 0,
            )
        }
    ),
    spells = SpellsTabState(
        spellcastingGroups = listOf(
            SpellcastingGroupUiModel(
                sourceKey = "Wizard",
                sourceLabel = "Wizard",
                spells = listOf(
                    CharacterSpellUiModel(
                        spellId = "fireball",
                        name = "Fireball",
                        level = 3,
                        school = "Evocation",
                        castingTime = "1 action",
                    )
                )
            )
        ),
        spellSlots = listOf(
            SpellSlotUiModel(level = 1, total = 4, expended = 1),
            SpellSlotUiModel(level = 2, total = 3, expended = 2),
        ),
        canAddSpells = true,
    ),
)
