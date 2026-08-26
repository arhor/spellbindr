package com.github.arhor.spellbindr.logging

/**
 * A null-object implementation of the [Logger] interface that performs no operations.
 * All log levels are disabled, and all logging calls are ignored.
 */
object NoOpLogger : Logger {
    override fun isEnabled(level: LogLevel): Boolean = false
    override fun log(level: LogLevel, message: String, throwable: Throwable?) = Unit
}
