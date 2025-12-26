package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.data.model.AbilityAssetModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AbilityMapperTest {

    @Test
    fun `toDomainAbilityOrNull maps asset model to domain ability`() {
        val assetModel = AbilityAssetModel(
            id = "wis",
            name = "Wisdom",
            description = listOf("Perception and insight."),
        )

        val result = assetModel.toDomainAbilityOrNull()

        requireNotNull(result)
        assertThat(result.id).isEqualTo(assetModel.id)
        assertThat(result.displayName).isEqualTo(assetModel.name)
        assertThat(result.description).isEqualTo(assetModel.description)
        assertThat(result.abbreviation).isEqualTo("WIS")
    }
}
