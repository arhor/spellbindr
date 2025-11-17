@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
internal fun CharacterSheetContent(
    state: CharacterSheetUiState,
    header: CharacterHeaderUiState,
    callbacks: CharacterSheetCallbacks,
    modifier: Modifier = Modifier,
) {
    val tabs = CharacterSheetTab.entries
    val pagerState = rememberPagerState { tabs.size }
    val currentSelectedTab = rememberUpdatedState(state.selectedTab)

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
                if (pageTab != currentSelectedTab.value) {
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
private fun CharacterSheetContentPreview() {
    AppTheme {
        CharacterSheetContent(
            state = CharacterSheetPreviewData.uiState,
            header = CharacterSheetPreviewData.header,
            callbacks = CharacterSheetCallbacks(),
            modifier = Modifier.fillMaxSize(),
        )
    }
}
