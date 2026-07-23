package com.github.arhor.spellbindr.logging

/**
 * Represents a single log entry captured during the execution of a process.
 * Typically used for verifying logging behavior in unit or integration tests.
 */
data class CapturedLogEntry(
    val level: LogLevel,
    val message: String,
    val throwable: Throwable?,
)
