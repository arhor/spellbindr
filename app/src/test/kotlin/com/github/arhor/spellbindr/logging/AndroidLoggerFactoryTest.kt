package com.github.arhor.spellbindr.logging

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AndroidLoggerFactoryTest {

    @Test
    fun `create should return same logger instance for the same tag`() {
        // Given
        val factory = AndroidLoggerFactory()

        // When
        val first = factory.getLogger("Spellbindr")
        val second = factory.getLogger("Spellbindr")

        // Then
        assertThat(first).isSameInstanceAs(second)
        assertThat(factory.cacheSize()).isEqualTo(1)
    }

    @Test
    fun `create should return same logger instance for the same tag under concurrent load`() {
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
            assertThat(factory.cacheSize()).isEqualTo(1)
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `create should return different logger instances for different tags`() {
        // Given
        val factory = AndroidLoggerFactory()

        // When
        val first = factory.getLogger("A")
        val second = factory.getLogger("B")

        // Then
        assertThat(first).isNotSameInstanceAs(second)
        assertThat(factory.cacheSize()).isEqualTo(2)
    }

    private fun AndroidLoggerFactory.cacheSize(): Int {
        val field = AndroidLoggerFactory::class.java.getDeclaredField("loggersByTag")
        field.isAccessible = true
        return (field.get(this) as Map<*, *>).size
    }
}
