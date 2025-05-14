package com.github.arhor.spellbindr.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.github.arhor.spellbindr.viewmodel.SpellDetailViewModel
import com.github.arhor.spellbindr.viewmodel.SpellListViewModel

@Composable
fun SpellDetailScreen(
    spellName: String?,
    onBackClicked: () -> Unit = {},
    onLikeClicked: (String?) -> Unit = {},
    viewModel: SpellDetailViewModel = hiltViewModel(),
    spellListViewModel: SpellListViewModel,
) {
    viewModel.loadSpellByName(spellName)
    val spell by viewModel.state.collectAsState()
    val favorites by spellListViewModel.state.collectAsState()
    val isFavorite = spell?.name?.let { favorites?.spellNames?.contains(it) } == true

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
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
            IconButton(onClick = { onLikeClicked(spell?.name) }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                )
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
                            TableRow(label = "Level", value = buildString {
                                append(it.level)
                                if (it.level == 0) {
                                    append(" (Cantrip)")
                                }
                            })
                            TableRow(label = "School", value = it.school.toString())
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    GradientDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    TableRow(label = "Casting Time", value = it.castingTime)
                    TableRow(label = "Range", value = it.range.toString())
                    TableRow(label = "Components", value = it.components.joinToString())
                    TableRow(label = "Duration", value = it.duration)
                    TableRow(label = "Classes", value = it.classes.joinToString())
                    TableRow(label = "Ritual", value = it.ritual.toString())
                    TableRow(label = "Concentration", value = it.concentration.toString())

                    it.damage?.let {
                        TableRow(label = "Damage", value = it.toString())
                    }
                    TableRow(
                        label = "Source",
                        value = "${it.source.book}${it.source.page?.let { ", p.$it" } ?: ""}")
                }
            }

            DescriptionRow(it.desc)

            if (!it.higherLevel.isNullOrBlank()) {
                DescriptionRow(it.higherLevel)

            }
        } ?: run {
            Text("Loading or spell not found")
        }
    }
}

@Composable
private fun DescriptionRow(text: String) {
    Text(
        text = text,
        color = DescriptionText,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontFamily = FontFamily.Serif,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
    )
}

@Composable
private fun TableRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = HeaderText, fontFamily = FontFamily.Serif)
        Text(value, color = HeaderText, fontFamily = FontFamily.Serif)
    }
}
