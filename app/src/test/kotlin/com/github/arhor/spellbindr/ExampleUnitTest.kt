package com.github.arhor.spellbindr

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun `addition should return sum when integers are added`() {
        // Given
        val first = 2
        val second = 2

        // When
        val result = first + second

        // Then
        assertThat(result).isEqualTo(4)
    }
}
