package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.data.model.AbilityAssetModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AbilityMapperTest {

    @Test
    fun `toDomainAbilityOrNull should map asset model to domain ability when ability is valid`() {
        // Given
        val assetModel = AbilityAssetModel(
            id = "wis",
            name = "Wisdom",
            description = listOf("Perception and insight."),
        )

        // When
        val result = assetModel.toDomainAbilityOrNull()

        // Then
        requireNotNull(result)
        assertThat(result.id).isEqualTo(assetModel.id)
        assertThat(result.displayName).isEqualTo(assetModel.name)
        assertThat(result.description).isEqualTo(assetModel.description)
        assertThat(result.abbreviation).isEqualTo("WIS")
    }
}
