package com.github.arhor.spellbindr.ui.spells.search

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.arhor.spellbindr.data.model.SpellcastingClass
import com.github.arhor.spellbindr.viewmodel.SpellSearchViewModel

@Composable
fun SearchFilterDialog(
    showFilterDialog: Boolean,
    onSubmit: () -> Unit = { },
    onCancel: () -> Unit = { },
    spellSearchViewModel: SpellSearchViewModel = hiltViewModel(),
) {
    var classesExpanded by remember { mutableStateOf(false) }
    val spellSearchViewState by spellSearchViewModel.state.collectAsState()
    var selectedClassFilters by remember { mutableStateOf(spellSearchViewState.selectedClasses) }


    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { onCancel() },
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
                        if (selectedClassFilters.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedClassFilters.size.toString(),
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
                            SpellcastingClass.entries.forEach { spellClass ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedClassFilters = if (spellClass in selectedClassFilters)
                                                selectedClassFilters - spellClass
                                            else
                                                selectedClassFilters + spellClass
                                        }
                                ) {
                                    Checkbox(
                                        checked = spellClass in selectedClassFilters,
                                        onCheckedChange = {
                                            selectedClassFilters = if (it)
                                                selectedClassFilters + spellClass
                                            else
                                                selectedClassFilters - spellClass
                                        }
                                    )
                                    Text(spellClass.toString())
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    OutlinedButton(
                        onClick = {
                            selectedClassFilters = emptySet()
                            spellSearchViewModel.onClassFilterChanged(emptySet())
                            onCancel()
                        }
                    ) {
                        Text("Clear")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            spellSearchViewModel.onClassFilterChanged(selectedClassFilters)
                            onSubmit()
                        }
                    ) {
                        Text("Apply")
                    }
                }
            },
        )
    }
}
