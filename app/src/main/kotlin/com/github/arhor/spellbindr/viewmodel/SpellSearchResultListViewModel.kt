package com.github.arhor.spellbindr.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SpellSearchResultListViewModel @Inject constructor() : ViewModel() {
    val expandedState = mutableStateMapOf<Int, Boolean>()
}
