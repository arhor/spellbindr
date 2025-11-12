@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.library.model.SampleLibraryContent
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SpellDetailScreen(
    spellId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spell = remember(spellId) { SampleLibraryContent.spell(spellId) }
    DetailScreenContent(
        title = spell.name,
        subtitle = "Level ${spell.level} • ${spell.school}",
        body = "Future versions will load the full spell description from local JSON.",
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun MonsterDetailScreen(
    monsterId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val monster = remember(monsterId) { SampleLibraryContent.monster(monsterId) }
    DetailScreenContent(
        title = monster.name,
        subtitle = "${monster.creatureType} • ${monster.challengeRating}",
        body = "Add stat blocks, actions, and traits here once offline data is wired in.",
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun RuleDetailScreen(
    ruleId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rule = remember(ruleId) { SampleLibraryContent.rule(ruleId) }
    DetailScreenContent(
        title = rule.name,
        subtitle = rule.snippet,
        body = "Use this screen for longer-form rule text, clarifications, and quick references.",
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun DetailScreenContent(
    title: String,
    subtitle: String,
    body: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
                .padding(24.dp),
        ) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun SpellDetailPreview() {
    AppTheme {
        SpellDetailScreen(spellId = "embershield", onBack = {})
    }
}

@Preview
@Composable
private fun MonsterDetailPreview() {
    AppTheme {
        MonsterDetailScreen(monsterId = "starving-mimic", onBack = {})
    }
}

@Preview
@Composable
private fun RuleDetailPreview() {
    AppTheme {
        RuleDetailScreen(ruleId = "dodge", onBack = {})
    }
}
