@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
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
import com.github.arhor.spellbindr.ui.feature.characters.CHARACTER_SPELL_SELECTION_RESULT_KEY
import com.github.arhor.spellbindr.ui.feature.characters.CharacterSpellAssignment
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
        savedStateHandle.getStateFlow<List<CharacterSpellAssignment>?>(
            CHARACTER_SPELL_SELECTION_RESULT_KEY, null
        )
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
    callbacks: CharacterSheetCallbacks = CharacterSheetCallbacks(),
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

@Preview
@Composable
private fun CharacterSheetPreview() {
    AppTheme {
        CharacterSheetScreen(
            state = CharacterSheetUiState(
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
            ),
            onBack = {},
        )
    }
}
