package com.github.arhor.spellbindr.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.arhor.spellbindr.data.local.database.CharacterProgressionJsonCodec
import com.github.arhor.spellbindr.data.local.database.SpellbindrDatabase
import com.github.arhor.spellbindr.data.local.database.converter.CharacterSheetConverter
import com.github.arhor.spellbindr.data.local.database.converter.EntityRefConverter
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSpell
import com.github.arhor.spellbindr.domain.model.CharacterSpellOwnership
import com.github.arhor.spellbindr.domain.model.CharacterSpellPreparation
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterSpellPersistenceIntegrationTest {

    private lateinit var database: SpellbindrDatabase
    private lateinit var repository: CharacterRepositoryImpl
    private lateinit var converter: CharacterSheetConverter

    @Before
    fun setUp() {
        val json = Json {
            ignoreUnknownKeys = true
            classDiscriminator = "type"
        }
        converter = CharacterSheetConverter(json)
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            SpellbindrDatabase::class.java,
        )
            .addTypeConverter(converter)
            .addTypeConverter(EntityRefConverter(json))
            .allowMainThreadQueries()
            .build()
        repository = CharacterRepositoryImpl(
            characterDao = database.characterDao(),
            progressionJsonCodec = CharacterProgressionJsonCodec(json),
            database = database,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `repository should round trip spell ownership and every preparation state`() = runBlocking {
        // Given
        val spells = listOf(
            CharacterSpell(
                spellId = "shield",
                sourceClass = "wizard",
                ownership = CharacterSpellOwnership.Spellbook,
                preparation = CharacterSpellPreparation.Unprepared,
            ),
            CharacterSpell(
                spellId = "magic-missile",
                sourceClass = "wizard",
                ownership = CharacterSpellOwnership.Spellbook,
                preparation = CharacterSpellPreparation.Prepared,
            ),
            CharacterSpell(
                spellId = "bless",
                sourceClass = "cleric-domain",
                ownership = CharacterSpellOwnership.Known,
                preparation = CharacterSpellPreparation.AlwaysPrepared,
            ),
            CharacterSpell(
                spellId = "healing-word",
                sourceClass = "bard",
            ),
        )
        val sheet = CharacterSheet(
            id = CHARACTER_ID,
            name = "Aster",
            className = "Wizard 3 / Bard 1",
            characterSpells = spells,
        )

        // When
        repository.upsertCharacterSheet(sheet)
        val restored = repository.observeCharacterSheet(CHARACTER_ID).first()

        // Then
        assertThat(restored?.characterSpells).containsExactlyElementsIn(spells).inOrder()
        val wizardSpells = restored?.characterSpells.orEmpty().filter { it.sourceClass == "wizard" }
        assertThat(wizardSpells.map(CharacterSpell::ownership)).containsExactly(
            CharacterSpellOwnership.Spellbook,
            CharacterSpellOwnership.Spellbook,
        )
        assertThat(wizardSpells.map(CharacterSpell::preparation)).containsExactly(
            CharacterSpellPreparation.Unprepared,
            CharacterSpellPreparation.Prepared,
        ).inOrder()
    }

    @Test
    fun `legacy spell snapshot should keep known spell behavior`() {
        // Given
        val legacyJson = """
            {
              "name": "Tamsin",
              "className": "bard",
              "characterSpells": [
                {
                  "spellId": "healing-word",
                  "sourceClass": "bard"
                }
              ]
            }
        """.trimIndent()

        // When
        val restored = requireNotNull(converter.intoCharacterSheetSnapshot(legacyJson))

        // Then
        assertThat(restored.characterSpells).containsExactly(
            CharacterSpell(
                spellId = "healing-word",
                sourceClass = "bard",
                ownership = CharacterSpellOwnership.Known,
                preparation = CharacterSpellPreparation.Prepared,
            )
        )
    }

    private companion object {
        private const val CHARACTER_ID = "character-spell-state"
    }
}
