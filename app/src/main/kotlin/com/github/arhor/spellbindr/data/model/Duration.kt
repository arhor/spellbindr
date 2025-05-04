package com.github.arhor.spellbindr.data.model

sealed class Duration {

    data object Instantaneous : Duration()

    data object UntilDispelled : Duration()

    data class Timed(
        val amount: Int,
        val unit: TimeUnit,
        val isConcentration: Boolean = false,
    ) : Duration()

    data class Special(
        val description: String,
    ) : Duration()
}
