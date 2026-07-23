package com.github.arhor.spellbindr.utils

import androidx.compose.foundation.lazy.LazyListState

suspend fun <T, K> LazyListState.scrollToItemIfNeeded(
    items: List<T>,
    selector: (T) -> K,
    selectedKey: K,
) {
    val index = items.indexOfFirst { selector(it) == selectedKey }
    if (index != -1) {
        val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }
        if (itemInfo == null || itemInfo.offset + itemInfo.size > layoutInfo.viewportSize.height) {
            animateScrollToItem(index)
        }
    }
}
