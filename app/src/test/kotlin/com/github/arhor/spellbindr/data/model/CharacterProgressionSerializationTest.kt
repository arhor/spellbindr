package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.data.local.database.CharacterProgressionJsonCodec
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.github.arhor.spellbindr.domain.model.ProgressionState
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.github.arhor.spellbindr.domain.model.SpellReplacement
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class CharacterProgressionSerializationTest {

    private val codec = CharacterProgressionJsonCodec(
        Json {
            ignoreUnknownKeys = true
            classDiscriminator = "type"
        }
    )

    @Test
    fun `decode should restore managed progression when codec round trips persisted state`() {
        // Given
        val state = ProgressionState.Managed(
            CharacterProgression(
                referenceDataVersion = "srd-5e-2014-2026-07-29",
                origin = ProgressionOrigin.Guided,
                levels = listOf(
                    CharacterLevelRecord(
                        characterLevel = 1,
                        classId = "fighter",
                        classLevel = 1,
                        hitPointGain = HitPointGain.Fixed(10),
                    ),
                    CharacterLevelRecord(
                        characterLevel = 2,
                        classId = "wizard",
                        classLevel = 1,
                        subclassId = "school-of-evocation",
                        hitPointGain = HitPointGain.Rolled(5),
                        featureChoices = mapOf("arcane-tradition" to setOf("school-of-evocation")),
                        abilityScoreDecision = AbilityScoreDecision.Increase(
                            mapOf(AbilityIds.INT to 2)
                        ),
                        spellChanges = SpellChanges(
                            replaced = setOf(
                                SpellReplacement(
                                    classId = "wizard",
                                    removedSpellId = "mage-armor",
                                    learnedSpellId = "shield",
                                )
                            )
                        ),
                    ),
                ),
            )
        )

        // When
        val restored = codec.decode(codec.encode(state))

        // Then
        assertThat(restored).isEqualTo(state)
    }

    @Test
    fun `decode should restore unmanaged progression when codec round trips persisted state`() {
        // Given
        val state = ProgressionState.Unmanaged

        // When
        val restored = codec.decode(codec.encode(state))

        // Then
        assertThat(restored).isEqualTo(state)
    }
}
