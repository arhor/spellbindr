package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.data.model.AppThemeMode
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ThemeMapperTest {

    @Test
    fun `toDomain maps each data theme mode`() {
        assertThat(AppThemeMode.LIGHT.toDomain()).isEqualTo(ThemeMode.LIGHT)
        assertThat(AppThemeMode.DARK.toDomain()).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun `toData maps each domain theme mode`() {
        assertThat(ThemeMode.LIGHT.toData()).isEqualTo(AppThemeMode.LIGHT)
        assertThat(ThemeMode.DARK.toData()).isEqualTo(AppThemeMode.DARK)
    }
}
