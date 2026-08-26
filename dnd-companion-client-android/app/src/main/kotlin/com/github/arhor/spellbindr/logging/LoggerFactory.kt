package com.github.arhor.spellbindr.logging

/**
 * A functional interface for creating [Logger] instances.
 *
 * This factory allows for abstraction over different logging implementations,
 * providing a consistent way to obtain a logger for a specific category or tag.
 */
fun interface LoggerFactory {
    /**
     * Provides a [Logger] instance associated with the specified [tag].
     *
     * @param tag the identifier for the logger, typically a class name or a specific component name
     * @return a [Logger] instance for the given [tag]
     */
    fun getLogger(tag: String): Logger
}
