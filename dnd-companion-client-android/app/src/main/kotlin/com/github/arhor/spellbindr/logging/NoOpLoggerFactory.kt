package com.github.arhor.spellbindr.logging

/**
 * A no-operation (No-Op) implementation of a logger factory.
 *
 * This implementation is typically used as a fallback or default when no
 * functional logger factory is provided, ensuring that logging calls do
 * not result in null pointer exceptions but instead perform no action.
 */
object NoOpLoggerFactory : LoggerFactory {
    override fun getLogger(tag: String): Logger = NoOpLogger
}
