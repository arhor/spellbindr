package com.github.arhor.spellbindr.logging

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LoggerTest {

    @Test
    fun `debug should not evaluate message when debug level is disabled`() {
        // Given
        val logger = CapturingLogger(enabledLevels = emptySet())
        var isMessageEvaluated = false

        // When
        logger.debug {
            isMessageEvaluated = true
            "Message"
        }

        // Then
        assertThat(isMessageEvaluated).isFalse()
        assertThat(logger.entries).isEmpty()
    }

    @Test
    fun `error should pass throwable and message to logger backend`() {
        // Given
        val logger = CapturingLogger(enabledLevels = setOf(LogLevel.ERROR))
        val throwable = IllegalStateException("failed")

        // When
        logger.error(throwable) { "Operation failed" }

        // Then
        assertThat(logger.entries).hasSize(1)
        assertThat(logger.entries.single()).isEqualTo(
            CapturedLogEntry(
                level = LogLevel.ERROR,
                message = "Operation failed",
                throwable = throwable,
            ),
        )
    }

    @Test
    fun `warn should pass throwable and message to logger backend`() {
        // Given
        val logger = CapturingLogger(enabledLevels = setOf(LogLevel.WARN))
        val throwable = IllegalArgumentException("invalid")

        // When
        logger.warn(throwable) { "Validation warning" }

        // Then
        assertThat(logger.entries).hasSize(1)
        assertThat(logger.entries.single()).isEqualTo(
            CapturedLogEntry(
                level = LogLevel.WARN,
                message = "Validation warning",
                throwable = throwable,
            ),
        )
    }

    @Test
    fun `tagOf should return companion owner class name for companion type`() {
        // Given
        val type = CompanionHost.Companion::class.java

        // When
        val tag = tagOf(type = type)

        // Then
        assertThat(tag).isEqualTo("CompanionHost")
    }

    @Test
    fun `tagOf should return simple class name for regular type`() {
        // Given
        val type = RegularType::class.java

        // When
        val tag = tagOf(type = type)

        // Then
        assertThat(tag).isEqualTo("RegularType")
    }

    @Test
    fun `tagOf should return non-empty fallback tag for anonymous type`() {
        // Given
        val anonymousObject = object {}

        // When
        val tag = tagOf(type = anonymousObject.javaClass)

        // Then
        assertThat(tag).isNotEmpty()
    }

    private class RegularType

    private class CompanionHost {
        companion object
    }
}
