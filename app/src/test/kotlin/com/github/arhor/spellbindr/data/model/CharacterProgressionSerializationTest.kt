package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.data.local.database.CharacterProgressionJsonCodec
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.github.arhor.spellbindr.domain.model.ProgressionState
import com.github.arhor.spellbindr.domain.model.ProficiencyChoiceSelection
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

    @Test
    fun `encode should use stable discriminator names when progression contains polymorphic decisions`() {
        // Given
        val state = ProgressionState.Managed(
            CharacterProgression(
                referenceDataVersion = "data-v1",
                origin = ProgressionOrigin.Guided,
                levels = listOf(
                    CharacterLevelRecord(
                        characterLevel = 1,
                        classId = "fighter",
                        classLevel = 1,
                        hitPointGain = HitPointGain.Fixed(10),
                        proficiencyChoices = listOf(
                            ProficiencyChoiceSelection(
                                choiceId = "class/fighter/starting-proficiency/1",
                                selectedProficiencyIds = setOf("skill-athletics"),
                            ),
                        ),
                        abilityScoreDecision = AbilityScoreDecision.Increase(mapOf(AbilityIds.STR to 2)),
                    ),
                    CharacterLevelRecord(
                        characterLevel = 2,
                        classId = "wizard",
                        classLevel = 1,
                        hitPointGain = HitPointGain.Rolled(5),
                        abilityScoreDecision = AbilityScoreDecision.Feat("alert"),
                    ),
                    CharacterLevelRecord(
                        characterLevel = 3,
                        classId = "fighter",
                        classLevel = 2,
                        hitPointGain = HitPointGain.Manual(7),
                    ),
                ),
            ),
        )

        // When
        val encoded = codec.encode(state)

        // Then
        val expectedJson = """
            {"type":"managed","progression":{"referenceDataVersion":"data-v1","origin":"Guided","levels":[
            {"characterLevel":1,"classId":"fighter","classLevel":1,
            "hitPointGain":{"type":"fixed","rolledValue":10},
            "proficiencyChoices":[{"choiceId":"class/fighter/starting-proficiency/1",
            "selectedProficiencyIds":["skill-athletics"]}],
            "abilityScoreDecision":{"type":"ability-score-increase","increases":{"str":2}}},
            {"characterLevel":2,"classId":"wizard","classLevel":1,
            "hitPointGain":{"type":"rolled","rolledValue":5},
            "abilityScoreDecision":{"type":"feat","featId":"alert"}},
            {"characterLevel":3,"classId":"fighter","classLevel":2,
            "hitPointGain":{"type":"manual","rolledValue":7}}]}}
        """.trimIndent().lineSequence().joinToString("")
        assertThat(encoded).isEqualTo(expectedJson)
    }

    @Test
    fun `decode should apply defaults and ignore unknown fields when persisted JSON uses legacy discriminators`() {
        // Given
        val legacyJson = """
            {
              "type": "com.github.arhor.spellbindr.domain.model.ProgressionState.Managed",
              "futureStateField": true,
              "progression": {
                "referenceDataVersion": "legacy-v1",
                "origin": "Guided",
                "futureProgressionField": "ignored",
                "levels": [
                  {
                    "characterLevel": 1,
                    "classId": "fighter",
                    "classLevel": 1,
                    "hitPointGain": {
                      "type": "com.github.arhor.spellbindr.domain.model.HitPointGain.Fixed",
                      "rolledValue": 10,
                      "futureHitPointField": 99
                    },
                    "abilityScoreDecision": {
                      "type": "com.github.arhor.spellbindr.domain.model.AbilityScoreDecision.Increase",
                      "increases": {"str": 2}
                    }
                  },
                  {
                    "characterLevel": 2,
                    "classId": "wizard",
                    "classLevel": 1,
                    "hitPointGain": {
                      "type": "com.github.arhor.spellbindr.domain.model.HitPointGain.Rolled",
                      "rolledValue": 5
                    },
                    "abilityScoreDecision": {
                      "type": "com.github.arhor.spellbindr.domain.model.AbilityScoreDecision.Feat",
                      "featId": "alert"
                    }
                  },
                  {
                    "characterLevel": 3,
                    "classId": "fighter",
                    "classLevel": 2,
                    "hitPointGain": {
                      "type": "com.github.arhor.spellbindr.domain.model.HitPointGain.Manual",
                      "rolledValue": 7
                    }
                  }
                ]
              }
            }
        """.trimIndent()

        // When
        val decoded = codec.decode(legacyJson)

        // Then
        assertThat(decoded).isEqualTo(
            ProgressionState.Managed(
                CharacterProgression(
                    referenceDataVersion = "legacy-v1",
                    origin = ProgressionOrigin.Guided,
                    levels = listOf(
                        CharacterLevelRecord(
                            characterLevel = 1,
                            classId = "fighter",
                            classLevel = 1,
                            hitPointGain = HitPointGain.Fixed(10),
                            abilityScoreDecision = AbilityScoreDecision.Increase(mapOf(AbilityIds.STR to 2)),
                        ),
                        CharacterLevelRecord(
                            characterLevel = 2,
                            classId = "wizard",
                            classLevel = 1,
                            hitPointGain = HitPointGain.Rolled(5),
                            abilityScoreDecision = AbilityScoreDecision.Feat("alert"),
                        ),
                        CharacterLevelRecord(
                            characterLevel = 3,
                            classId = "fighter",
                            classLevel = 2,
                            hitPointGain = HitPointGain.Manual(7),
                        ),
                    ),
                ),
            ),
        )
    }

    @Test
    fun `decode should restore unmanaged state when persisted JSON uses legacy discriminator`() {
        // Given
        val legacyJson = """
            {
              "type": "com.github.arhor.spellbindr.domain.model.ProgressionState.Unmanaged",
              "futureStateField": "ignored"
            }
        """.trimIndent()

        // When
        val decoded = codec.decode(legacyJson)

        // Then
        assertThat(decoded).isEqualTo(ProgressionState.Unmanaged)
    }
}
