package com.github.arhor.spellbindr.logging

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AndroidLoggerFactoryTest {

    @Test
    fun `getLogger should return same logger instance when tag is unchanged`() {
        // Given
        val factory = AndroidLoggerFactory()

        // When
        val first = factory.getLogger("Spellbindr")
        val second = factory.getLogger("Spellbindr")

        // Then
        assertThat(first).isSameInstanceAs(second)
    }

    @Test
    fun `getLogger should return same logger instance when tag is requested concurrently`() {
        // Given
        val factory = AndroidLoggerFactory()
        val executor = Executors.newFixedThreadPool(8)

        try {
            // When
            val futures = (1..128).map {
                executor.submit<Logger> { factory.getLogger("SharedTag") }
            }
            val loggers = futures.map { it.get(5, TimeUnit.SECONDS) }
            val first = loggers.first()

            // Then
            assertThat(loggers.all { it === first }).isTrue()
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `getLogger should return different logger instances when tags differ`() {
        // Given
        val factory = AndroidLoggerFactory()

        // When
        val first = factory.getLogger("A")
        val second = factory.getLogger("B")

        // Then
        assertThat(first).isNotSameInstanceAs(second)
    }
}
