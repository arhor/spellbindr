package com.github.arhor.spellbindr.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.arhor.spellbindr.data.model.Race
import com.github.arhor.spellbindr.ui.theme.Accent
import com.github.arhor.spellbindr.viewmodel.CharacterCreationViewModel
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun CharacterCreationWizardScreen(
    viewModel: CharacterCreationViewModel = hiltViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val characterName by viewModel.characterName.collectAsState()
    val races by viewModel.races.collectAsState()
    val selectedRace by viewModel.selectedRace.collectAsState()

    // Local state for selected subrace name
    var selectedSubraceName by remember(selectedRace) { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        when (currentStep) {
            0 -> NameStep(
                name = characterName,
                onNameChange = viewModel::setCharacterName
            )

            1 -> RaceStep(
                races = races,
                selectedRace = selectedRace,
                selectedSubrace = selectedSubraceName,
                onRaceSelected = { race ->
                    viewModel.selectRace(race)
                    selectedSubraceName = null // reset subrace when race changes
                },
                onSubraceSelected = { subraceName ->
                    selectedSubraceName = subraceName
                }
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            if (currentStep > 0) {
                Button(onClick = { viewModel.goToStep(currentStep - 1) }) {
                    Text("Back")
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            if (currentStep < 1) {
                Button(
                    onClick = { viewModel.goToStep(currentStep + 1) },
                    enabled = when (currentStep) {
                        0 -> characterName.isNotBlank()
                        else -> true
                    }
                ) {
                    Text("Next")
                }
            }
        }
    }
}

@Composable
private fun NameStep(name: String, onNameChange: (String) -> Unit) {
    Text(
        text = "Enter Character Name",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun RaceStep(
    races: List<Race>,
    selectedRace: Race?,
    selectedSubrace: String?,
    onRaceSelected: (Race) -> Unit,
    onSubraceSelected: (String) -> Unit
) {
    if (races.isEmpty()) {
        Text("Loading races...", style = MaterialTheme.typography.bodyMedium)
        return
    }
    val virtualPageCount = 1000
    val startPage = virtualPageCount / 2
    val initialPage =
        startPage - (startPage % races.size) + (races.indexOfFirst { it == selectedRace }.coerceAtLeast(0) % races.size)
    val pagerState = rememberPagerState(initialPage = initialPage) { virtualPageCount }
    val coroutineScope = rememberCoroutineScope()

    // Map the current virtual page to the real race index
    val realIndex = pagerState.currentPage % races.size

    // Update selectedRace when page changes
    LaunchedEffect(realIndex) {
        if (races.isNotEmpty() && races.getOrNull(realIndex) != null) {
            onRaceSelected(races[realIndex])
        }
    }

    // If user scrolls near the ends, jump back to the middle
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage < 10 || pagerState.currentPage > virtualPageCount - 10) {
            coroutineScope.launch {
                pagerState.scrollToPage(initialPage)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Swipe to select a race",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 48.dp),
            pageSpacing = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 350.dp, max = 500.dp)
        ) { page ->
            val realPageIndex = page % races.size
            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                ).absoluteValue
            val scale = lerp(0.85f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
            val alpha = lerp(0.5f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
            val zIndex = if (page == pagerState.currentPage) 1f else 0f

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        this.scaleX = scale
                        this.scaleY = scale
                        this.alpha = alpha
                        this.shadowElevation = lerp(2f, 16f, 1f - pageOffset.coerceIn(0f, 1f))
                    }
                    .zIndex(zIndex)
            ) {
                RaceCard(
                    race = races[realPageIndex],
                    selected = realIndex == realPageIndex,
                    selectedSubrace = selectedSubrace,
                    onSubraceSelected = onSubraceSelected
                )
            }
        }
        // Pager indicators (show only for real races)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(races.size) { index ->
                val color =
                    if (realIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.3f
                    )
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(10.dp)
                        .background(color, shape = RoundedCornerShape(5.dp))
                )
            }
        }
    }
}

@Composable
private fun RaceCard(
    race: Race,
    selected: Boolean,
    selectedSubrace: String?,
    onSubraceSelected: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = race.name,
                style = MaterialTheme.typography.headlineSmall,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 2.dp,
                color = Color.Transparent
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .padding(bottom = 8.dp)
                    .shadow(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Accent.copy(alpha = 0.2f),
                                Accent,
                                Accent.copy(alpha = 0.2f)
                            )
                        )
                    )
            )
            Text("Source: ${race.source}", style = MaterialTheme.typography.labelMedium)
            if (race.traits.isNotEmpty()) {
                Text(
                    "Traits:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                race.traits.forEach { trait ->
                    Text("- ${trait.name}: ${trait.desc}", style = MaterialTheme.typography.bodySmall, fontSize = 13.sp)
                }
            }
            if (race.traits.isNotEmpty() && race.subraces.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            if (race.subraces.isNotEmpty()) {
                Text(
                    "Subraces:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    race.subraces.forEach { subrace ->
                        val isSelected = selectedSubrace == subrace.name
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSubraceSelected(subrace.name) },
                            label = { Text(subrace.name) },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
