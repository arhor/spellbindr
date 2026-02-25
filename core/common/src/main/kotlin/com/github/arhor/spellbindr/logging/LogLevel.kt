package com.github.arhor.spellbindr.logging

/**
 * Represents the standard logging severity levels used within the application.
 */
enum class LogLevel {
    /**
     * Detailed information, typically useful only when diagnosing problems.
     */
    DEBUG,

    /**
     * Designates informational messages that highlight the progress of the application
     * at a coarse-grained level.
     */
    INFO,

    /**
     * Indicates potentially harmful situations or unexpected events that do not
     * interrupt the application's flow but may require attention.
     */
    WARN,

    /**
     * Designates error events that might still allow the application to continue running.
     */
    ERROR,
}
