package com.github.arhor.spellbindr.ui.feature.compendium.spells.details

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.components.ErrorMessage
import com.github.arhor.spellbindr.ui.components.GradientDivider
import com.github.arhor.spellbindr.ui.components.LoadingIndicator
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellIcon
import com.github.arhor.spellbindr.ui.theme.Accent
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun SpellDetailScreen(
    state: SpellDetailsUiState,
) {
    when (state) {
        is SpellDetailsUiState.Loading -> LoadingIndicator()
        is SpellDetailsUiState.Content -> SpellDetailsContent(state.spell)
        is SpellDetailsUiState.Error -> ErrorMessage(state.errorMessage)
    }
}

@Composable
private fun SpellDetailsContent(spell: Spell) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = spell.name,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            style = TextStyle(
                shadow = Shadow(
                    color = Accent.copy(alpha = 0.5f),
                    offset = Offset(2f, 2f),
                    blurRadius = 32f
                )
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Accent.copy(alpha = 0.2f),
                            Accent,
                            Accent.copy(alpha = 0.2f),
                        ),
                    )
                )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                Row(
                    modifier = Modifier.padding(start = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpellIcon(
                        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                        spellName = spell.name,
                        size = 60.dp,
                        iconSize = 60.dp,
                    )
                    Column {
                        TableRow(label = "Level", text = buildString {
                            append(spell.level)
                            if (spell.level == 0) {
                                append(" (Cantrip)")
                            }
                        })
                        TableRow(label = "School", text = spell.school.prettyString())
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                GradientDivider()
                Spacer(modifier = Modifier.height(12.dp))

                TableRow(label = "Casting Time", text = spell.castingTime)
                TableRow(label = "Range", text = spell.range)
                TableRow(label = "Components", text = spell.components.joinToString())
                TableRow(label = "Duration", text = spell.duration)
                TableRow(label = "Classes") {
                    FlowRow(horizontalArrangement = Arrangement.End) {
                        spell.classes.forEachIndexed { _, classRef ->
                            Text(
                                text = " [${classRef.prettyString()}]",
                                fontStyle = FontStyle.Italic,
                                fontFamily = FontFamily.Serif,
                            )

                        }
                    }
                }
                TableRow(label = "Ritual", text = spell.ritual.toString())
                TableRow(label = "Concentration", text = spell.concentration.toString())
                TableRow(label = "Source", text = spell.source)
            }
        }

        DescriptionRow(spell.desc)

        spell.higherLevel?.takeIf(List<*>::isNotEmpty)?.let { higherLevelText ->
            DescriptionRow(higherLevelText)
        }
    }
}

@Composable
private fun DescriptionRow(text: Iterable<String>) {
    for (paragraph in text) {
        Text(
            text = paragraph,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
        )
    }
}

@Composable
private fun TableRow(label: String, text: String) {
    TableRow(label) {
        Text(
            text = text,
            fontFamily = FontFamily.Serif,
            maxLines = Int.MAX_VALUE,
            softWrap = true
        )
    }
}

@Composable
private fun TableRow(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontFamily = FontFamily.Serif,
                maxLines = Int.MAX_VALUE,
                softWrap = true
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            content()
        }
    }
}

@PreviewLightDark
@Composable
private fun SpellDetailsPreview() {
    AppTheme {
        val spell = Spell(
            id = "arcane_blast",
            name = "Arcane Blast",
            desc = listOf(
                "A surge of arcane energy leaps from your hands to strike a creature.",
                "On a hit, the target takes 2d8 force damage.",
            ),
            level = 2,
            range = "60 ft",
            ritual = false,
            school = EntityRef(id = "evocation"),
            duration = "Instant",
            castingTime = "1 action",
            classes = listOf(EntityRef(id = "wizard")),
            components = listOf("V", "S"),
            concentration = false,
            higherLevel = listOf("Damage increases by 1d8 for each slot above 2nd."),
            source = "Homebrew",
        )
        SpellDetailScreen(
            state = SpellDetailsUiState.Content(
                spell = spell,
                isFavorite = false,
            ),
        )
    }
}
