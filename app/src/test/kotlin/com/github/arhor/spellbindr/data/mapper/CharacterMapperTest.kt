package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.data.local.db.CharacterEntity
import com.github.arhor.spellbindr.data.local.db.CharacterSheetSnapshot
import com.github.arhor.spellbindr.domain.model.Character
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CharacterMapperTest {

    @Test
    fun `toDomain should preserve core character fields when mapping from entity`() {
        // Given
        val entity = CharacterEntity(
            id = "char-1",
            name = "Lia",
            race = EntityRef("elf"),
            subrace = EntityRef("high-elf"),
            classes = mapOf(EntityRef("wizard") to 5),
            background = EntityRef("sage"),
            abilityScores = mapOf(EntityRef("int") to 18),
            proficiencies = setOf(EntityRef("arcana")),
            equipment = setOf(EntityRef("spellbook")),
            inventory = mapOf(EntityRef("potion") to 2),
            spells = setOf(EntityRef("magic-missile")),
            manualSheet = CharacterSheetSnapshot(name = "ignored-for-mapping"),
        )

        // When
        val result = entity.toDomain()

        // Then
        assertThat(result.id).isEqualTo(entity.id)
        assertThat(result.name).isEqualTo(entity.name)
        assertThat(result.race).isEqualTo(entity.race)
        assertThat(result.subrace).isEqualTo(entity.subrace)
        assertThat(result.classes).isEqualTo(entity.classes)
        assertThat(result.background).isEqualTo(entity.background)
        assertThat(result.abilityScores).isEqualTo(entity.abilityScores)
        assertThat(result.proficiencies).isEqualTo(entity.proficiencies)
        assertThat(result.equipment).isEqualTo(entity.equipment)
        assertThat(result.inventory).isEqualTo(entity.inventory)
        assertThat(result.spells).isEqualTo(entity.spells)
    }

    @Test
    fun `toEntity should leave manualSheet null when domain model omits it`() {
        // Given
        val character = Character(
            id = "char-2",
            name = "Ryn",
            race = EntityRef("human"),
            subrace = null,
            classes = mapOf(EntityRef("fighter") to 3),
            background = EntityRef("soldier"),
            abilityScores = mapOf(EntityRef("str") to 16),
            proficiencies = setOf(EntityRef("athletics")),
            equipment = setOf(EntityRef("shield")),
            inventory = mapOf(EntityRef("torch") to 4),
            spells = emptySet(),
        )

        // When
        val result = character.toEntity()

        // Then
        assertThat(result.id).isEqualTo(character.id)
        assertThat(result.name).isEqualTo(character.name)
        assertThat(result.manualSheet).isNull()
    }
}
