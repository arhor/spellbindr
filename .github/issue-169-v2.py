from pathlib import Path
import re


def replace_once(path: str, old: str, new: str) -> None:
    file = Path(path)
    text = file.read_text()
    if text.count(old) != 1:
        raise RuntimeError(f"{path}: expected exactly one match for {old[:100]!r}, found {text.count(old)}")
    file.write_text(text.replace(old, new))


engine = "app/src/main/kotlin/com/github/arhor/spellbindr/domain/usecase/LevelUpProgressionEngine.kt"
p = Path(engine)
text = p.read_text()

text, count = re.subn(
    r'''        val featIds = \(plan\.selections\.abilityScoreDecision as\? AbilityScoreDecision\.Feat\)\?\.featId\n            \?\.let\(referenceData\.featsById::get\)\?\.ownedChoiceIds\.orEmpty\(\)\n        return plan\.toRecord\(''',
    '''        val selectedFeat = (plan.selections.abilityScoreDecision as? AbilityScoreDecision.Feat)?.featId
            ?.let(referenceData.featsById::get)
        val featIds = selectedFeat?.let(::activeFeatChoiceIds).orEmpty()
        val persistedPlan = if (selectedFeat?.id == MAGIC_INITIATE_ID && magicInitiateIsSupported(referenceData)) {
            plan.withMagicInitiateSpellGrant()
        } else {
            plan
        }
        return persistedPlan.toRecord(''',
    text,
)
if count != 1:
    raise RuntimeError(f"recordFor patch count={count}")

text, count = re.subn(
    r'''(\s+)val deferredDecision = deferredFeatDecision\(feat\.id\)''',
    r'''\1val deferredDecision = deferredFeatDecision(feat.id)
\1    ?.takeUnless { feat.id == MAGIC_INITIATE_ID && magicInitiateIsSupported(data) }''',
    text,
)
if count != 2:
    raise RuntimeError(f"deferred decision patch count={count}")

needle = '''                    val afterFeat = applyAbilityDecision(abilities, selections, data)
'''
if text.count(needle) != 1:
    raise RuntimeError(f"afterFeat marker count={text.count(needle)}")
text = text.replace(needle, '''                    if (feat.id == MAGIC_INITIATE_ID && magicInitiateIsSupported(data)) {
                        validateMagicInitiateSelections(selections, data, validations)
                    }
''' + needle)

needle = '''                }
            }
            val currentSpellcasting = selectedClass.levels.firstOrNull { it.level == nextClassLevel }?.spellcasting
'''
if text.count(needle) != 1:
    raise RuntimeError(f"requirements marker count={text.count(needle)}")
text = text.replace(needle, '''                    if (feat.id == MAGIC_INITIATE_ID && magicInitiateIsSupported(referenceData)) {
                        addAll(magicInitiateChoiceRequirements(referenceData, plan.selections.featChoices))
                    }
                }
            }
            val currentSpellcasting = selectedClass.levels.firstOrNull { it.level == nextClassLevel }?.spellcasting
''')

needle = '''            known.addAll(record.spellChanges.featureLearned.values.flatten().map { it.spellId })
'''
if text.count(needle) != 1:
    raise RuntimeError(f"classOwnedSpellIds marker count={text.count(needle)}")
text = text.replace(needle, '''            known.addAll(
                record.spellChanges.featureLearned.values.flatten()
                    .filter { it.classId == classId }
                    .map { it.spellId },
            )
''')

needle = '''        return abilityChoiceIsLegal && languageChoiceIsLegal && proficiencyChoiceIsLegal && damageTypeChoiceIsLegal
'''
if text.count(needle) != 1:
    raise RuntimeError(f"feat legality marker count={text.count(needle)}")
text = text.replace(needle, '''        val magicInitiateChoicesAreLegal = feat.id != MAGIC_INITIATE_ID || magicInitiateIsSupported(data)
        return abilityChoiceIsLegal && languageChoiceIsLegal && proficiencyChoiceIsLegal && damageTypeChoiceIsLegal &&
            magicInitiateChoicesAreLegal
''')

helper_marker = '''    private data class FeatOwnedChoiceRequirement(
'''
if text.count(helper_marker) != 1:
    raise RuntimeError(f"helper marker count={text.count(helper_marker)}")
helpers = '''    private fun activeFeatChoiceIds(feat: Feat): Set<String> = feat.ownedChoiceIds +
        if (feat.id == MAGIC_INITIATE_ID) MAGIC_INITIATE_CHOICE_IDS else emptySet()

    private fun magicInitiateIsSupported(data: LevelUpReferenceData): Boolean =
        magicInitiateClassIds(data).isNotEmpty()

    private fun magicInitiateClassIds(data: LevelUpReferenceData): List<String> = MAGIC_INITIATE_CLASS_IDS
        .filter { classId ->
            classId in data.classesById &&
                magicInitiateSpellCandidates(data, classId, 0).size >= 2 &&
                magicInitiateSpellCandidates(data, classId, 1).isNotEmpty()
        }
        .sorted()

    private fun magicInitiateSpellCandidates(
        data: LevelUpReferenceData,
        classId: String,
        level: Int,
    ) = data.spells.asSequence()
        .filter { spell -> spell.level == level && classId in spell.classes.map { it.id } }
        .sortedWith(compareBy({ it.name }, { it.id }))
        .toList()

    private fun validateMagicInitiateSelections(
        selections: LevelUpSelections,
        data: LevelUpReferenceData,
        validations: MutableList<LevelUpValidationIssue>,
    ) {
        val classIds = magicInitiateClassIds(data)
        validateChoice(
            id = MAGIC_INITIATE_CLASS_LIST_CHOICE_ID,
            choice = Choice.OptionsArrayChoice(1, classIds),
            selected = selections.featChoices[MAGIC_INITIATE_CLASS_LIST_CHOICE_ID].orEmpty(),
            label = "Magic Initiate spell list",
            validations = validations,
            legalOptionIds = classIds.toSet(),
        )
        val classId = selections.featChoices[MAGIC_INITIATE_CLASS_LIST_CHOICE_ID]
            ?.singleOrNull()
            ?.takeIf { it in classIds }
            ?: return
        val cantripIds = magicInitiateSpellCandidates(data, classId, 0).map { it.id }
        validateChoice(
            id = MAGIC_INITIATE_CANTRIP_CHOICE_ID,
            choice = Choice.OptionsArrayChoice(2, cantripIds),
            selected = selections.featChoices[MAGIC_INITIATE_CANTRIP_CHOICE_ID].orEmpty(),
            label = "Magic Initiate cantrips",
            validations = validations,
            legalOptionIds = cantripIds.toSet(),
        )
        val firstLevelIds = magicInitiateSpellCandidates(data, classId, 1).map { it.id }
        validateChoice(
            id = MAGIC_INITIATE_FIRST_LEVEL_SPELL_CHOICE_ID,
            choice = Choice.OptionsArrayChoice(1, firstLevelIds),
            selected = selections.featChoices[MAGIC_INITIATE_FIRST_LEVEL_SPELL_CHOICE_ID].orEmpty(),
            label = "Magic Initiate 1st-level spell",
            validations = validations,
            legalOptionIds = firstLevelIds.toSet(),
        )
    }

    private fun magicInitiateChoiceRequirements(
        data: LevelUpReferenceData,
        featChoices: Map<String, Set<String>>,
    ): List<LevelUpRequirement.ChoiceSelection> = buildList {
        val classIds = magicInitiateClassIds(data)
        val classSelection = featChoices[MAGIC_INITIATE_CLASS_LIST_CHOICE_ID].orEmpty()
        add(LevelUpRequirement.ChoiceSelection(
            id = MAGIC_INITIATE_CLASS_LIST_CHOICE_ID,
            sourceId = MAGIC_INITIATE_ID,
            label = "Magic Initiate spell list",
            choice = Choice.OptionsArrayChoice(1, classIds),
            selectedOptionIds = classSelection,
            category = LevelUpChoiceCategory.Feat,
            options = classIds.map { classId ->
                LevelUpChoiceOption(classId, data.classesById[classId]?.name ?: classId)
            },
        ))
        val classId = classSelection.singleOrNull()?.takeIf { it in classIds } ?: return@buildList
        val cantrips = magicInitiateSpellCandidates(data, classId, 0)
        add(LevelUpRequirement.ChoiceSelection(
            id = MAGIC_INITIATE_CANTRIP_CHOICE_ID,
            sourceId = MAGIC_INITIATE_ID,
            label = "Magic Initiate cantrips",
            choice = Choice.OptionsArrayChoice(2, cantrips.map { it.id }),
            selectedOptionIds = featChoices[MAGIC_INITIATE_CANTRIP_CHOICE_ID].orEmpty(),
            category = LevelUpChoiceCategory.Feat,
            options = cantrips.map { LevelUpChoiceOption(it.id, it.name) },
        ))
        val firstLevelSpells = magicInitiateSpellCandidates(data, classId, 1)
        add(LevelUpRequirement.ChoiceSelection(
            id = MAGIC_INITIATE_FIRST_LEVEL_SPELL_CHOICE_ID,
            sourceId = MAGIC_INITIATE_ID,
            label = "Magic Initiate 1st-level spell",
            choice = Choice.OptionsArrayChoice(1, firstLevelSpells.map { it.id }),
            selectedOptionIds = featChoices[MAGIC_INITIATE_FIRST_LEVEL_SPELL_CHOICE_ID].orEmpty(),
            category = LevelUpChoiceCategory.Feat,
            options = firstLevelSpells.map { LevelUpChoiceOption(it.id, it.name) },
        ))
    }

    private fun LevelUpPlan.withMagicInitiateSpellGrant(): LevelUpPlan {
        val classId = selections.featChoices[MAGIC_INITIATE_CLASS_LIST_CHOICE_ID]?.singleOrNull() ?: return this
        val spellIds = selections.featChoices[MAGIC_INITIATE_CANTRIP_CHOICE_ID].orEmpty() +
            selections.featChoices[MAGIC_INITIATE_FIRST_LEVEL_SPELL_CHOICE_ID].orEmpty()
        val grants = spellIds.mapTo(linkedSetOf()) { spellId ->
            com.github.arhor.spellbindr.domain.model.ClassSpellRef(classId, spellId)
        }
        return copy(selections = selections.copy(
            spellChanges = selections.spellChanges.copy(
                featureLearned = selections.spellChanges.featureLearned +
                    (MAGIC_INITIATE_SPELL_GRANT_OWNER_ID to grants),
            ),
        ))
    }

'''
text = text.replace(helper_marker, helpers + helper_marker)

constant_marker = '''    private const val SAVING_THROW_PREFIX = "saving-throw-"
'''
if text.count(constant_marker) != 1:
    raise RuntimeError(f"constant marker count={text.count(constant_marker)}")
text = text.replace(constant_marker, '''    private const val MAGIC_INITIATE_ID = "magic-initiate"
    private const val MAGIC_INITIATE_CLASS_LIST_CHOICE_ID = "magic-initiate:class-list"
    private const val MAGIC_INITIATE_CANTRIP_CHOICE_ID = "magic-initiate:cantrips"
    private const val MAGIC_INITIATE_FIRST_LEVEL_SPELL_CHOICE_ID = "magic-initiate:first-level-spell"
    private const val MAGIC_INITIATE_SPELL_GRANT_OWNER_ID = "feat:magic-initiate"
    private val MAGIC_INITIATE_CLASS_IDS = setOf("bard", "cleric", "druid", "sorcerer", "warlock", "wizard")
    private val MAGIC_INITIATE_CHOICE_IDS = setOf(
        MAGIC_INITIATE_CLASS_LIST_CHOICE_ID,
        MAGIC_INITIATE_CANTRIP_CHOICE_ID,
        MAGIC_INITIATE_FIRST_LEVEL_SPELL_CHOICE_ID,
    )

''' + constant_marker)
p.write_text(text)

review = "app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/levelup/CharacterLevelUpClassProgressionReview.kt"
replace_once(review, '''            }
        }
    }
}

@Composable
private fun ClassProgressionReviewRow''', '''            }
            MagicInitiateReview(state)
        }
    }
}

@Composable
private fun MagicInitiateReview(state: CharacterLevelUpUiState.Content) {
    val requirements = state.preview.requirements
        .filterIsInstance<LevelUpRequirement.ChoiceSelection>()
        .filter { it.sourceId == "magic-initiate" }
    if (requirements.isEmpty()) return

    fun selectedLabels(id: String): List<String> {
        val requirement = requirements.firstOrNull { it.id == id } ?: return emptyList()
        return requirement.options.filter { it.id in requirement.selectedOptionIds }.map { it.label }
    }

    Text("Magic Initiate", style = MaterialTheme.typography.titleSmall)
    selectedLabels("magic-initiate:class-list").singleOrNull()?.let { Text("Spell list: $it") }
    selectedLabels("magic-initiate:cantrips").takeIf { it.isNotEmpty() }
        ?.let { Text("Cantrips: ${it.joinToString()}") }
    selectedLabels("magic-initiate:first-level-spell").singleOrNull()?.let { Text("1st-level spell: $it") }
}

@Composable
private fun ClassProgressionReviewRow''')

integration = "app/src/test/kotlin/com/github/arhor/spellbindr/data/repository/CharacterLevelUpRepositoryIntegrationTest.kt"
replace_once(integration, "import com.github.arhor.spellbindr.domain.model.AbilityIds\n", "import com.github.arhor.spellbindr.domain.model.AbilityIds\nimport com.github.arhor.spellbindr.domain.model.AbilityScoreDecision\n")
replace_once(integration, "import com.github.arhor.spellbindr.domain.model.EntityRef\n", "import com.github.arhor.spellbindr.domain.model.EntityRef\nimport com.github.arhor.spellbindr.domain.model.Feat\n")
marker = "    private suspend fun seed(sheet: CharacterSheet, progression: CharacterProgression) {\n"
test = '''    @Test
    fun `applyLevelUp should persist Magic Initiate spells with feat ownership`() = runBlocking {
        val progression = CharacterProgression(
            referenceDataVersion = REFERENCE_VERSION,
            origin = ProgressionOrigin.Guided,
            levels = (1..3).map { level ->
                CharacterLevelRecord(
                    characterLevel = level,
                    classId = "fighter",
                    classLevel = level,
                    hitPointGain = HitPointGain.Fixed(if (level == 1) 10 else 6),
                )
            },
        )
        val sheet = fighterSheet().copy(level = 3, className = "Fighter 3", maxHitPoints = 22, currentHitPoints = 22)
        seed(sheet, progression)
        val plan = LevelUpPlan(
            expectedTotalLevel = 3,
            rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
            referenceDataVersion = REFERENCE_VERSION,
            selectedClassId = "fighter",
            selections = LevelUpSelections(
                hitPointGain = HitPointGain.Fixed(6),
                abilityScoreDecision = AbilityScoreDecision.Feat("magic-initiate"),
                featChoices = mapOf(
                    "magic-initiate:class-list" to setOf("wizard"),
                    "magic-initiate:cantrips" to setOf("fire-bolt", "mage-hand"),
                    "magic-initiate:first-level-spell" to setOf("magic-missile"),
                ),
            ),
        )
        fun magicSpell(id: String, level: Int) = Spell(
            id = id,
            name = id.replace('-', ' ').replaceFirstChar { it.uppercase() },
            desc = emptyList(),
            level = level,
            range = "Self",
            ritual = false,
            school = EntityRef("evocation"),
            duration = "Instantaneous",
            castingTime = "1 action",
            classes = listOf(EntityRef("wizard")),
            components = emptyList(),
            concentration = false,
            source = "test",
        )
        val data = LevelUpReferenceData(
            classes = listOf(characterClass("fighter", 10), characterClass("wizard", 6)),
            features = emptyList(),
            feats = listOf(Feat("magic-initiate", "Magic Initiate", emptyList())),
            spells = listOf(
                magicSpell("fire-bolt", 0),
                magicSpell("mage-hand", 0),
                magicSpell("magic-missile", 1),
                magicSpell("shield", 1),
            ),
            referenceDataVersion = REFERENCE_VERSION,
        )

        val result = repository.applyLevelUp(CHARACTER_ID, 3, plan, data)
        val stored = requireNotNull(dao.getCharacterWithProgression(CHARACTER_ID))
        val storedProgression = stored.progression.toDomain(codec) as ProgressionState.Managed
        val storedSheet = requireNotNull(stored.character.manualSheet?.toDomain(CHARACTER_ID))

        assertThat(result).isInstanceOf(ApplyLevelUpResult.Success::class.java)
        assertThat(storedProgression.progression.levels.last().featChoices).containsEntry(
            "magic-initiate:class-list", setOf("wizard"),
        )
        assertThat(storedSheet.characterSpells.map { it.spellId }).containsAtLeast(
            "fire-bolt", "mage-hand", "magic-missile",
        )
        val featGrants = storedSheet.managedProgression?.ownedSpellGrants.orEmpty()
            .filter { ":feature:feat:magic-initiate:" in it.ownerKey }
        assertThat(featGrants.map { it.spell }).containsExactly(
            ClassSpellRef("wizard", "fire-bolt"),
            ClassSpellRef("wizard", "mage-hand"),
            ClassSpellRef("wizard", "magic-missile"),
        )
    }

'''
replace_once(integration, marker, test + marker)
