package com.github.arhor.spellbindr.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.next.CharacterRace
import com.github.arhor.spellbindr.utils.PreviewScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CharacterRaceSelection(
    races: List<CharacterRace>,
    onRaceSelected: (CharacterRace, CharacterRace.Subrace?) -> Unit,
) {
    val pagerState = rememberPagerState { races.size }
    val selectedRace by remember { derivedStateOf { races[pagerState.currentPage] } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Character Race",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        GradientDivider(modifier = Modifier.padding(vertical = 16.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val race = races[page]
            RacePlaceholder(race = race)
        }

        RaceDetails(
            race = selectedRace,
            onSubraceSelected = { subrace ->
                onRaceSelected(selectedRace, subrace)
            }
        )
    }
}

@Composable
private fun RaceDetails(
    race: CharacterRace,
    onSubraceSelected: (CharacterRace.Subrace?) -> Unit,
) {
    var selectedSubrace by remember { mutableStateOf<CharacterRace.Subrace?>(null) }

    Text(
        text = race.name,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 16.dp)
    )

    if (race.subraces.isNotEmpty()) {
        race.subraces.forEach { subrace ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                RadioButton(
                    selected = subrace == selectedSubrace,
                    onClick = {
                        selectedSubrace = subrace
                        onSubraceSelected(subrace)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = subrace.name, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
private fun RacePlaceholder(race: CharacterRace) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(Color.Gray),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = race.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
private fun CharacterRaceSelectionPreview() {
    PreviewScope {
        CharacterRaceSelection(
            races = listOf(
                CharacterRace(
                    id = "human",
                    name = "Human",
                    traits = emptyList(),
                    subraces = listOf(
                        CharacterRace.Subrace("human-1", "Variant 1", "Human", emptyList()),
                        CharacterRace.Subrace("human-2", "Variant 2", "Human", emptyList()),
                    ),
                ),
                CharacterRace(
                    id = "elf",
                    name = "Elf",
                    traits = emptyList(),
                    subraces = listOf(
                        CharacterRace.Subrace("elf-1", "High Elf", "High Elf", emptyList()),
                        CharacterRace.Subrace("elf-2", "Drow", "Drow", emptyList()),
                    ),
                ),
                CharacterRace(
                    id = "tiefling",
                    name = "Tiefling",
                    traits = emptyList(),
                    subraces = emptyList(),
                ),
            ),
            onRaceSelected = { _, _ -> }
        )
    }
}
