package com.github.arhor.spellbindr.ui.feature.compendium.spells.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.github.arhor.spellbindr.data.model.EntityRef
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SearchFilterDialog(
    showFilterDialog: Boolean,
    castingClasses: List<EntityRef>,
    currentClasses: Set<EntityRef>,
    onSubmit: (Set<EntityRef>) -> Unit = { },
    onCancel: (Set<EntityRef>) -> Unit = { },
) {
    var classesExpanded by remember { mutableStateOf(false) }
    var selectedClasses by remember { mutableStateOf(currentClasses) }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { onCancel(currentClasses) },
            title = { Text("Filters") },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { classesExpanded = !classesExpanded }
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Classes:", style = MaterialTheme.typography.titleMedium)
                        if (selectedClasses.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedClasses.size.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(
                            imageVector = if (classesExpanded) Icons.Default.ExpandMore else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (classesExpanded) "Collapse" else "Expand"
                        )
                    }
                    AnimatedVisibility(
                        visible = classesExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            castingClasses.forEach { spellClass ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedClasses = if (spellClass in selectedClasses) {
                                                selectedClasses - spellClass
                                            } else {
                                                selectedClasses + spellClass
                                            }
                                        }
                                ) {
                                    Checkbox(
                                        checked = spellClass in selectedClasses,
                                        onCheckedChange = {
                                            selectedClasses = if (it) {
                                                selectedClasses + spellClass
                                            } else {
                                                selectedClasses - spellClass
                                            }
                                        }
                                    )
                                    Text(spellClass.prettyString())
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    OutlinedButton(
                        onClick = { onCancel(emptySet()) }
                    ) {
                        Text("Clear")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSubmit(selectedClasses) }
                    ) {
                        Text("Apply")
                    }
                }
            },
        )
    }
}

@Preview
@Composable
private fun SearchFilterDialogLightPreview() {
    SearchFilterDialogPreview(isDarkTheme = false)
}

@Preview
@Composable
private fun SearchFilterDialogDarkPreview() {
    SearchFilterDialogPreview(isDarkTheme = true)
}

@Composable
private fun SearchFilterDialogPreview(isDarkTheme: Boolean) {
    AppTheme(isDarkTheme = isDarkTheme) {
        SearchFilterDialog(
            showFilterDialog = true,
            castingClasses = listOf(
                EntityRef(id = "wizard"),
                EntityRef(id = "sorcerer"),
            ),
            currentClasses = setOf(EntityRef(id = "wizard")),
            onSubmit = {},
            onCancel = {},
        )
    }
}
