package com.github.arhor.spellbindr.ui.feature.diceRoller

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.diceRoller.model.DiceType
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadialDiceSelector(
    onDiceSelected: (DiceType) -> Unit,
) {
    val diceTypes = DiceType.entries.filter { it != DiceType.D100 }

    Layout(
        content = {
            Button(
                onClick = { onDiceSelected(DiceType.D100) },
                modifier = Modifier.size(100.dp)
            ) {
                Text(text = DiceType.D100.displayName)
            }
            diceTypes.forEach { diceType ->
                Button(
                    onClick = { onDiceSelected(diceType) },
                    modifier = Modifier.size(80.dp)
                ) {
                    Text(text = diceType.displayName)
                }
            }
        },
        measurePolicy = { measurables, constraints ->
            val mainButton = measurables.first()
            val diceButtons = measurables.subList(1, measurables.size)

            layout(constraints.maxWidth, constraints.maxHeight) {
                val centerPlaceable = mainButton.measure(constraints)

                centerPlaceable.place(
                    x = (constraints.maxWidth - centerPlaceable.width) / 2,
                    y = (constraints.maxHeight - centerPlaceable.height) / 2,
                )

                val radius = centerPlaceable.width * 1.2f
                val angleStep = 2 * Math.PI / diceButtons.size

                diceButtons.forEachIndexed { index, measurable ->
                    val placeable = measurable.measure(constraints)
                    val angle = angleStep * index - (Math.PI / 2)

                    placeable.place(
                        x = (constraints.maxWidth / 2) + (radius * cos(angle)).toInt() - (placeable.width / 2),
                        y = (constraints.maxHeight / 2) + (radius * sin(angle)).toInt() - (placeable.height / 2),
                    )
                }
            }
        },
    )
}

@Composable
@Preview(showBackground = true)
fun RadialTestPreview() {
    RadialDiceSelector {
        // Handle dice type selection
    }
}
