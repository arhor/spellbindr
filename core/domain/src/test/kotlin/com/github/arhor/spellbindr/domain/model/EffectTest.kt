package com.github.arhor.spellbindr.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EffectTest {

    @Test
    fun `add hp effect should add flat value when not per level`() {
        // Given
        val state = Character.State(level = 5, maximumHitPoints = 10)
        val effect = Effect.AddHpEffect(value = 2)

        // When
        val updated = effect.applyTo(state)

        // Then
        assertThat(updated.maximumHitPoints).isEqualTo(12)
    }

    @Test
    fun `add hp effect should scale value by level when marked per level`() {
        // Given
        val state = Character.State(level = 3, maximumHitPoints = 7)
        val effect = Effect.AddHpEffect(value = 2, perLevel = true)

        // When
        val updated = effect.applyTo(state)

        // Then
        assertThat(updated.maximumHitPoints).isEqualTo(13)
    }

    @Test
    fun `add equipment effect should merge equipment ids and quantities`() {
        // Given
        val state = Character.State(
            equipment = setOf(EntityRef("torch")),
            inventory = mapOf(EntityRef("pouch") to 1),
        )
        val effect = Effect.AddEquipmentEffect(
            equipment = listOf(
                CountedEntityRef(id = "pouch", quantity = 2),
                CountedEntityRef(id = "rope-hempen", quantity = 1),
            ),
        )

        // When
        val updated = effect.applyTo(state)

        // Then
        assertThat(updated.equipment).containsExactlyElementsIn(
            setOf(
                EntityRef("torch"),
                EntityRef("pouch"),
                EntityRef("rope-hempen"),
            ),
        )
        assertThat(updated.inventory).containsExactlyEntriesIn(
            mapOf(
                EntityRef("pouch") to 3,
                EntityRef("rope-hempen") to 1,
            ),
        )
    }
}
