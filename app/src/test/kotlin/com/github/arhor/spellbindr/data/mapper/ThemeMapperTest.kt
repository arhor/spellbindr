package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.data.model.AppThemeMode
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ThemeMapperTest {

    @Test
    fun `toDomain should map each data theme mode when converting to domain`() {
        // Given
        val light = AppThemeMode.LIGHT
        val dark = AppThemeMode.DARK

        // When
        val lightDomain = light.toDomain()
        val darkDomain = dark.toDomain()

        // Then
        assertThat(lightDomain).isEqualTo(ThemeMode.LIGHT)
        assertThat(darkDomain).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun `toData should map each domain theme mode when converting to data`() {
        // Given
        val light = ThemeMode.LIGHT
        val dark = ThemeMode.DARK

        // When
        val lightData = light.toData()
        val darkData = dark.toData()

        // Then
        assertThat(lightData).isEqualTo(AppThemeMode.LIGHT)
        assertThat(darkData).isEqualTo(AppThemeMode.DARK)
    }
}
