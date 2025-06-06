package com.github.arhor.spellbindr.ui.screens.spells.details

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.arhor.spellbindr.ui.components.GradientDivider
import com.github.arhor.spellbindr.ui.components.SpellIcon
import com.github.arhor.spellbindr.ui.theme.Accent
import com.github.arhor.spellbindr.ui.theme.CardBg
import com.github.arhor.spellbindr.ui.theme.DescriptionText
import com.github.arhor.spellbindr.ui.theme.HeaderText

@Composable
fun SpellDetailScreen(
    spellName: String?,
    onBackClick: () -> Unit = {},
    spellDetailsVM: SpellDetailsViewModel = hiltViewModel(),
) {
    spellDetailsVM.loadSpellByName(spellName)
    val spellDetailState by spellDetailsVM.state.collectAsState()
    val spell = spellDetailState.spell
    val isFavorite = spellDetailState.isFavorite

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
            IconButton(
                onClick = { spellDetailsVM.toggleFavorite() }
            ) {
                if (isFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Remove from favorites",
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Add to favorites",
                    )
                }
            }
        }

        spell?.let {
            Text(
                text = it.name,
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
                colors = CardDefaults.cardColors(containerColor = CardBg.copy(alpha = 0.2f)),
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
                    Row(modifier = Modifier.padding(start = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                        SpellIcon(
                            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                            spellName = it.name,
                            size = 60.dp,
                            iconSize = 60.dp,
                        )
                        Column {
                            TableRow(label = "Level", text = buildString {
                                append(it.level)
                                if (it.level == 0) {
                                    append(" (Cantrip)")
                                }
                            })
                            TableRow(label = "School", text = it.school.prettyString())
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    GradientDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    TableRow(label = "Casting Time", text = it.castingTime)
                    TableRow(label = "Range", text = it.range.toString())
                    TableRow(label = "Components", text = it.components.joinToString())
                    TableRow(label = "Duration", text = it.duration)
                    TableRow(label = "Classes") {
                        FlowRow(horizontalArrangement = Arrangement.End) {
                            it.classes.forEachIndexed { i, classRef ->
                                Text(
                                    text = " [${classRef.prettyString()}]",
                                    color = HeaderText,
                                    fontStyle = FontStyle.Italic,
                                    fontFamily = FontFamily.Serif,
                                )

                            }
                        }
                    }
                    TableRow(label = "Ritual", text = it.ritual.toString())
                    TableRow(label = "Concentration", text = it.concentration.toString())
                    TableRow(label = "Source", text = it.source)
                }
            }

            DescriptionRow(it.desc)

            if (!it.higherLevel.isNullOrEmpty()) {
                DescriptionRow(it.higherLevel)

            }
        } ?: run {
            Text("Loading or spell not found")
        }
    }
}

@Composable
private fun DescriptionRow(text: Iterable<String>) {
    for (paragraph in text) {
        Text(
            text = paragraph,
            color = DescriptionText,
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
            color = HeaderText,
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
                color = HeaderText,
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
