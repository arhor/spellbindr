@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.github.arhor.spellbindr.ui.feature.character.sheet.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.character.sheet.CharacterSheetUiState
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.overview.OverviewTab
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.skills.SkillsTab
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.spells.SpellsTab
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.weapons.WeaponsTab
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterHeaderUiState
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetTab
import com.github.arhor.spellbindr.ui.theme.AppTheme
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
internal fun CharacterSheetContent(
    state: CharacterSheetUiState.Content,
    header: CharacterHeaderUiState,
    onTabSelected: (CharacterSheetTab) -> Unit,
    onAddSpellsClick: () -> Unit,
    onSpellSelected: (String) -> Unit,
    onSpellRemoved: (String, String) -> Unit,
    onCastSpellClick: (String) -> Unit,
    onLongRestClick: () -> Unit,
    onShortRestClick: () -> Unit,
    onConfigureSlotsClick: () -> Unit,
    onSpellSlotToggle: (Int, Int) -> Unit,
    onSpellSlotTotalChanged: (Int, Int) -> Unit,
    onPactSlotToggle: (Int) -> Unit,
    onPactSlotTotalChanged: (Int) -> Unit,
    onPactSlotLevelChanged: (Int) -> Unit,
    onConcentrationClear: () -> Unit,
    onAddWeaponClick: () -> Unit,
    onWeaponSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = CharacterSheetTab.entries
    val pagerState = rememberPagerState(
        initialPage = state.selectedTab.ordinal,
        pageCount = { tabs.size },
    )
    val currentSelectedTab = rememberUpdatedState(state.selectedTab)
    val spellsListState = rememberSaveable(state.characterId, saver = LazyListState.Saver) {
        LazyListState()
    }

    LaunchedEffect(state.selectedTab) {
        val targetPage = state.selectedTab.ordinal
        if (pagerState.currentPage != targetPage || pagerState.currentPageOffsetFraction != 0f) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val pageTab = tabs[page]
                if (pageTab != currentSelectedTab.value) {
                    onTabSelected(pageTab)
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
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.testTag("CharacterSheetTab-${tab.name}"),
                    text = { Text(tab.name.lowercase().replaceFirstChar(Char::titlecase)) },
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (tabs[page]) {
                CharacterSheetTab.Overview -> OverviewTab(
                    header = header,
                    overview = state.overview,
                    editMode = state.editMode,
                    editingState = state.editingState,
                    modifier = Modifier.fillMaxSize(),
                )

                CharacterSheetTab.Skills -> SkillsTab(
                    state = state.skills,
                    modifier = Modifier.fillMaxSize(),
                )

                CharacterSheetTab.Spells -> SpellsTab(
                    spellsState = state.spells,
                    editMode = state.editMode,
                    onAddSpellsClick = onAddSpellsClick,
                    onCastSpellClick = onCastSpellClick,
                    onLongRestClick = onLongRestClick,
                    onShortRestClick = onShortRestClick,
                    onConfigureSlotsClick = onConfigureSlotsClick,
                    onSpellSlotToggle = onSpellSlotToggle,
                    onSpellSlotTotalChanged = onSpellSlotTotalChanged,
                    onPactSlotToggle = onPactSlotToggle,
                    onPactSlotTotalChanged = onPactSlotTotalChanged,
                    onPactSlotLevelChanged = onPactSlotLevelChanged,
                    onConcentrationClear = onConcentrationClear,
                    onSpellSelected = onSpellSelected,
                    onSpellRemoved = onSpellRemoved,
                    listState = spellsListState,
                    modifier = Modifier.fillMaxSize(),
                )

                CharacterSheetTab.Weapons -> WeaponsTab(
                    weapons = state.weapons,
                    onAddWeaponClick = onAddWeaponClick,
                    onWeaponSelected = onWeaponSelected,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CharacterSheetContentPreview() {
    AppTheme {
        CharacterSheetContent(
            state = CharacterSheetPreviewData.uiState,
            header = CharacterSheetPreviewData.header,
            onTabSelected = {},
            onAddSpellsClick = {},
            onSpellSelected = {},
            onSpellRemoved = { _, _ -> },
            onCastSpellClick = {},
            onLongRestClick = {},
            onShortRestClick = {},
            onConfigureSlotsClick = {},
            onSpellSlotToggle = { _, _ -> },
            onSpellSlotTotalChanged = { _, _ -> },
            onPactSlotToggle = {},
            onPactSlotTotalChanged = {},
            onPactSlotLevelChanged = {},
            onConcentrationClear = {},
            onAddWeaponClick = {},
            onWeaponSelected = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
