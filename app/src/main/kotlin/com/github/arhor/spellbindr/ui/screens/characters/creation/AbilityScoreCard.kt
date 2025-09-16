package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.arhor.spellbindr.ui.theme.Accent
import com.github.arhor.spellbindr.ui.theme.SpellbindrTheme
import com.github.arhor.spellbindr.utils.EllipseShape
import com.github.arhor.spellbindr.utils.calculateAbilityScoreModifier

@Composable
fun AbilityScoreCard(
    name: String,
    value: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val mainBoxShape = CutCornerShape(15.dp)

    Box(modifier = Modifier.padding(10.dp)) {
        Box(
            modifier = modifier
                .width(90.dp)
                .height(90.dp)
                .clip(mainBoxShape)
                .border(width = 1.dp, color = Color.Gray, shape = mainBoxShape)
                .background(color = MaterialTheme.colorScheme.surface)
                .clickable(enabled = onClick != null, onClick = { onClick?.invoke() }),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = name,
                    textAlign = TextAlign.Center,
                    color = Accent,
                )
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = value.toString(),
                        textAlign = TextAlign.Center,
                        color = Accent,
                        fontSize = 30.sp,
                    )
                }
            }

        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 10.dp)
                .width(45.dp)
                .height(30.dp)
                .clip(EllipseShape)
                .background(color = MaterialTheme.colorScheme.surface)
                .border(width = 1.dp, color = Color.Gray, shape = EllipseShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = calculateAbilityScoreModifier(value),
                textAlign = TextAlign.Center,
                color = Accent,
            )
        }
    }
}

@Preview
@Composable
private fun AbilityScoreCardPreview() {
    SpellbindrTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AbilityScoreCard(
                name = "STR",
                value = 13,
            )
        }
    }
}
