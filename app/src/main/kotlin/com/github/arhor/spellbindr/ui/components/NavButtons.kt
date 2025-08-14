package com.github.arhor.spellbindr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.SpellbindrTheme

private typealias NavHandler = () -> Unit

@Composable
fun NavButtons(
    modifier: Modifier = Modifier,
    onPrev: NavHandler? = null,
    onNext: NavHandler? = null,
    prevText: String = "Prev",
    nextText: String = "Next",
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        NavButton(text = prevText, onClick = onPrev)
        NavButton(text = nextText, onClick = onNext)
    }
}

@Composable
private fun NavButton(text: String, onClick: NavHandler?) {
    Button(
        onClick = onClick ?: {},
        enabled = onClick != null,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

@Preview
@Composable
private fun NavButtonsPreview() {
    SpellbindrTheme {
        NavButtons(
            onPrev = {},
            onNext = {},
        )
    }
}
