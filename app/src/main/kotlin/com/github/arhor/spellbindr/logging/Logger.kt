package com.github.arhor.spellbindr.logging

/**
 * Defines the contract for a logging component that supports different severity levels.
 *
 * This interface provides basic methods for checking log level status and performing
 * the actual logging of messages and exceptions.
 */
interface Logger {
    /**
     * Checks if logging is enabled for the specified [level].
     *
     * @param level the log level to check
     * @return true if logging is enabled for the given level, false otherwise
     */
    fun isEnabled(level: LogLevel): Boolean

    /**
     * Logs a message at the specified [level] with an optional [throwable].
     *
     * @param level the severity level of the log message
     * @param message the message to be logged
     * @param throwable an optional exception to be logged along with the message
     */
    fun log(level: LogLevel, message: String, throwable: Throwable? = null)
}
