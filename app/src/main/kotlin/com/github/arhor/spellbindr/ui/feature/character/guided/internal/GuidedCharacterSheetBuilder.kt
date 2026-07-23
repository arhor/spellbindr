package com.github.arhor.spellbindr.ui.feature.character.guided.internal

import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.Background
import com.github.arhor.spellbindr.domain.model.Character
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSpell
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.CountedEntityRef
import com.github.arhor.spellbindr.domain.model.Effect
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.PactSlotState
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.SavingThrowEntry
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.SkillEntry
import com.github.arhor.spellbindr.domain.model.SpellSlotState
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.model.displayName
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedCharacterSetupUiState
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedCharacterSetupViewModel
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedSelection
import com.github.arhor.spellbindr.ui.feature.character.guided.model.AbilityScoreMethod
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedCharacterPreview
import java.util.UUID

internal fun computeInitialSlotsForClass(
    clazz: CharacterClass?,
): Pair<List<SpellSlotState>, PactSlotState?> {
    val emptySharedSlots = (1..9).map { level -> SpellSlotState(level = level) }
    if (clazz?.spellcasting?.level != 1) return emptySharedSlots to null

    val level1Slots: Map<String, Int> =
        clazz.levels.firstOrNull { it.level == 1 }?.spellcasting?.spellSlots.orEmpty()
    val sharedSlots = (1..9).map { level ->
        SpellSlotState(
            level = level,
            total = level1Slots[level.toString()] ?: 0,
            expended = 0,
        )
    }

    if (clazz.id != "warlock") return sharedSlots to null

    val pactTotal = level1Slots["1"] ?: 0
    return emptySharedSlots to PactSlotState(
        slotLevel = 1,
        total = pactTotal,
        expended = 0,
    )
}

internal fun buildGuidedCharacterSheet(content: GuidedCharacterSetupUiState.Content): CharacterSheet {
    val selection = content.selection
    val clazz = content.classes.firstOrNull { it.id == selection.classId }
    val race = content.races.firstOrNull { it.id == selection.raceId }
    val background = content.backgrounds.firstOrNull { it.id == selection.backgroundId }

    val baseAbilityScores = resolveGuidedBaseAbilityScores(selection) ?: AbilityIds.standardOrder.associateWith { 10 }
    val effects = buildGuidedAllEffects(
        selection = selection,
        clazz = clazz,
        races = content.races,
        backgrounds = content.backgrounds,
        traitsById = content.traitsById,
        featuresById = content.featuresById,
    )
    val computed =
        effects.fold(Character.State(level = 1, abilityScores = baseAbilityScores.toEntityRefMap())) { state, effect ->
            effect.applyTo(state)
        }
    val finalAbilityScores = computed.abilityScores.toAbilityScores()

    val proficiencyBonus = 2
    val conMod = finalAbilityScores.modifierFor(AbilityIds.CON)
    val dexMod = finalAbilityScores.modifierFor(AbilityIds.DEX)
    val baseHp = ((clazz?.hitDie ?: 8) + conMod).coerceAtLeast(1)
    val maxHp = (baseHp + computed.maximumHitPoints).coerceAtLeast(1)

    val raceName = buildString {
        append(race?.name.orEmpty())
        val subraceName = selection.subraceId?.let { id ->
            race?.subraces?.firstOrNull { it.id == id }?.name
        }
        if (!subraceName.isNullOrBlank()) {
            append(" (")
            append(subraceName)
            append(')')
        }
    }.trim()

    val proficiencies = computed.proficiencies.map(EntityRef::prettyString).sorted().joinToString(", ")
    val languages = computed.languages.map { ref ->
        content.languagesById[ref.id]?.name ?: ref.prettyString()
    }.sorted().joinToString(", ")
    val equipmentText = computed.inventory.entries
        .sortedBy { it.key.id }
        .joinToString(separator = "\n") { (ref, count) ->
            val name = content.equipmentById[ref.id]?.name ?: ref.prettyString()
            if (count <= 1) name else "$name x$count"
        }

    val savingThrows = AbilityIds.standardOrder.map { abilityId ->
        val proficient = clazz?.savingThrows?.any { it.equals(abilityId, ignoreCase = true) } == true
        val bonus = finalAbilityScores.modifierFor(abilityId) + if (proficient) proficiencyBonus else 0
        SavingThrowEntry(
            abilityId = abilityId,
            bonus = bonus,
            proficient = proficient,
        )
    }
    val expertiseProficiencies =
        selection.choiceSelections[GuidedCharacterSetupViewModel.featureChoiceKey(ROGUE_EXPERTISE_FEATURE_ID)].orEmpty()
    val expertiseSkills = expertiseProficiencies.mapNotNull(::skillFromProficiencyId).toSet()
    val skillProficiencies = computed.proficiencies.mapNotNull(::skillFromProficiencyId).toSet() + expertiseSkills
    val skills = Skill.entries.map { skill ->
        val proficient = skill in skillProficiencies
        val expertise = skill in expertiseSkills
        val multiplier = when {
            expertise -> 2
            proficient -> 1
            else -> 0
        }
        val bonus = finalAbilityScores.modifierFor(skill.abilityId) + proficiencyBonus * multiplier
        SkillEntry(
            skill = skill,
            bonus = bonus,
            proficient = proficient,
            expertise = expertise,
        )
    }

    val classSpells = buildList {
        val source = clazz?.name.orEmpty()
        val cantrips =
            selection.choiceSelections[GuidedCharacterSetupViewModel.spellCantripsChoiceKey()].orEmpty()
        val level1 =
            selection.choiceSelections[GuidedCharacterSetupViewModel.spellLevel1ChoiceKey()].orEmpty()
        cantrips.forEach { add(CharacterSpell(spellId = it, sourceClass = source)) }
        level1.forEach { add(CharacterSpell(spellId = it, sourceClass = source)) }
    }
    val racialSpells = buildList {
        val selectedRace = race ?: return@buildList
        val traitIds = buildList {
            addAll(selectedRace.traits.map { it.id })
            val subrace = selection.subraceId?.let { sid -> selectedRace.subraces.firstOrNull { it.id == sid } }
            if (subrace != null) {
                addAll(subrace.traits.map { it.id })
            }
        }
        traitIds.mapNotNull(content.traitsById::get).forEach { trait ->
            trait.spellChoice ?: return@forEach
            val selected =
                selection.choiceSelections[GuidedCharacterSetupViewModel.raceTraitSpellChoiceKey(trait.id)].orEmpty()
            if (selected.isNotEmpty()) {
                selected.forEach { spellId ->
                    add(CharacterSpell(spellId = spellId, sourceClass = trait.name))
                }
            }
        }
    }
    val characterSpells = classSpells + racialSpells
    val (spellSlots, pactSlots) = computeInitialSlotsForClass(clazz)

    return CharacterSheet(
        id = UUID.randomUUID().toString(),
        name = content.name.trim(),
        level = 1,
        className = clazz?.name.orEmpty(),
        race = raceName,
        background = background?.name.orEmpty(),
        abilityScores = finalAbilityScores,
        proficiencyBonus = proficiencyBonus,
        maxHitPoints = maxHp,
        currentHitPoints = maxHp,
        armorClass = 10 + dexMod,
        initiative = dexMod,
        speed = "${computed.speed} ft",
        hitDice = "1d${clazz?.hitDie ?: 8}",
        spellSlots = spellSlots,
        pactSlots = pactSlots,
        savingThrows = savingThrows,
        skills = skills,
        languages = languages,
        proficiencies = proficiencies,
        equipment = equipmentText,
        featuresAndTraits = buildGuidedFeaturesAndTraitsText(content, clazz, race, background),
        characterSpells = characterSpells,
    )
}

internal fun computeGuidedPreview(
    selection: GuidedSelection,
    selectedClass: CharacterClass?,
    races: List<Race>,
    backgrounds: List<Background>,
    traitsById: Map<String, Trait>,
    featuresById: Map<String, Feature>,
): GuidedCharacterPreview {
    val baseAbilityScores = resolveGuidedBaseAbilityScores(selection) ?: AbilityIds.standardOrder.associateWith { 10 }
    val effects = buildGuidedAllEffects(
        selection = selection,
        clazz = selectedClass,
        races = races,
        backgrounds = backgrounds,
        traitsById = traitsById,
        featuresById = featuresById,
    )
    val computed =
        effects.fold(Character.State(level = 1, abilityScores = baseAbilityScores.toEntityRefMap())) { state, effect ->
            effect.applyTo(state)
        }
    val finalAbilityScores = computed.abilityScores.toAbilityScores()

    val conMod = finalAbilityScores.modifierFor(AbilityIds.CON)
    val dexMod = finalAbilityScores.modifierFor(AbilityIds.DEX)
    val baseHp = ((selectedClass?.hitDie ?: 8) + conMod).coerceAtLeast(1)
    val maxHp = (baseHp + computed.maximumHitPoints).coerceAtLeast(1)

    return GuidedCharacterPreview(
        abilityScores = finalAbilityScores,
        maxHitPoints = maxHp,
        armorClass = 10 + dexMod,
        speed = computed.speed,
        languagesCount = computed.languages.size,
        proficienciesCount = computed.proficiencies.size,
    )
}

internal fun findGuidedLevelOneFeatureChoices(
    clazz: CharacterClass?,
    featuresById: Map<String, Feature>,
): List<Pair<String, Choice>> {
    if (clazz == null) return emptyList()
    val level1Features = clazz.levels.firstOrNull { it.level == 1 }?.features.orEmpty()
    return level1Features.mapNotNull { featureId ->
        val choice = featuresById[featureId]?.choice ?: return@mapNotNull null
        featureId to choice
    }
}

internal fun computeGuidedSpellRequirementSummary(
    clazz: CharacterClass,
    preview: GuidedCharacterPreview,
): GuidedSpellRequirementSummary? {
    if (clazz.spellcasting?.level != 1) return null

    val level1 = clazz.levels.firstOrNull { it.level == 1 }?.spellcasting
    val cantrips = level1?.cantrips ?: 0
    val level1Spells = when {
        clazz.id == "wizard" -> 6
        level1?.spells != null -> level1.spells ?: 0
        clazz.id == "cleric" || clazz.id == "druid" -> (preview.abilityScores.modifierFor(AbilityIds.WIS) + 1)
            .coerceAtLeast(1)

        else -> 0
    }
    val level1Label = when (clazz.id) {
        "wizard" -> "spellbook spell(s)"
        "cleric", "druid" -> "prepared spell(s)"
        else -> "spell(s)"
    }

    return GuidedSpellRequirementSummary(
        cantrips = cantrips,
        level1Spells = level1Spells,
        level1Label = level1Label,
    )
}

internal fun resolveGuidedBaseAbilityScores(selection: GuidedSelection): Map<AbilityId, Int>? =
    when (selection.abilityMethod) {
        AbilityScoreMethod.STANDARD_ARRAY -> {
            if (!guidedIsStandardArrayValid(selection.standardArrayAssignments)) return null
            selection.standardArrayAssignments.mapValues { it.value ?: 10 }
        }

        AbilityScoreMethod.POINT_BUY -> selection.pointBuyScores
        null -> null
    }

internal fun buildGuidedAllEffects(
    selection: GuidedSelection,
    clazz: CharacterClass?,
    races: List<Race>,
    backgrounds: List<Background>,
    traitsById: Map<String, Trait>,
    featuresById: Map<String, Feature>,
): List<Effect> {
    val effects = mutableListOf<Effect>()

    if (clazz != null) {
        effects += Effect.AddProficienciesEffect(clazz.proficiencies.toSet())
    }
    val startingEquipment = clazz?.startingEquipment
    if (startingEquipment != null) {
        effects += Effect.AddEquipmentEffect(
            startingEquipment.map { CountedEntityRef(it.id, it.quantity) },
        )
    }

    val background = backgrounds.firstOrNull { it.id == selection.backgroundId }
    if (background != null) {
        effects += background.effects
        val backgroundLanguages =
            selection.choiceSelections[GuidedCharacterSetupViewModel.backgroundLanguageChoiceKey()].orEmpty()
        if (background.languageChoice != null && backgroundLanguages.isNotEmpty()) {
            effects += Effect.AddLanguagesEffect(backgroundLanguages)
        }
        val backgroundEquipment =
            selection.choiceSelections[GuidedCharacterSetupViewModel.backgroundEquipmentChoiceKey()].orEmpty()
        if (background.equipmentChoice != null && backgroundEquipment.isNotEmpty()) {
            effects += Effect.AddEquipmentEffect(
                backgroundEquipment.map { CountedEntityRef(it, 1) },
            )
        }
    }

    val race = races.firstOrNull { it.id == selection.raceId }
    if (race != null) {
        val traitIds = buildList {
            addAll(race.traits.map { it.id })
            val subrace = selection.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
            if (subrace != null) {
                addAll(subrace.traits.map { it.id })
            }
        }
        val traits = traitIds.mapNotNull(traitsById::get)
        traits.forEach { trait ->
            trait.effects?.let(effects::addAll)

            trait.abilityBonusChoice?.let {
                val selected =
                    selection.choiceSelections[GuidedCharacterSetupViewModel.raceTraitAbilityBonusChoiceKey(
                        trait.id
                    )]
                        .orEmpty()
                if (selected.isNotEmpty()) {
                    effects += Effect.ModifyAbilityEffect(selected.associateWith { 1 })
                }
            }
            trait.languageChoice?.let {
                val selected =
                    selection.choiceSelections[GuidedCharacterSetupViewModel.raceTraitLanguageChoiceKey(trait.id)]
                        .orEmpty()
                if (selected.isNotEmpty()) {
                    effects += Effect.AddLanguagesEffect(selected)
                }
            }
            trait.proficiencyChoice?.let {
                val selected =
                    selection.choiceSelections[GuidedCharacterSetupViewModel.raceTraitProficiencyChoiceKey(
                        trait.id
                    )]
                        .orEmpty()
                if (selected.isNotEmpty()) {
                    effects += Effect.AddProficienciesEffect(selected)
                }
            }
            trait.draconicAncestryChoice?.let {
                val selected =
                    selection.choiceSelections[GuidedCharacterSetupViewModel.raceTraitDraconicAncestryChoiceKey(
                        trait.id
                    )]
                        .orEmpty()
                selected.mapNotNull(traitsById::get).forEach { selectedTrait ->
                    selectedTrait.effects?.let(effects::addAll)
                }
            }
        }
    }

    if (clazz != null) {
        clazz.proficiencyChoices.forEachIndexed { index, _ ->
            val selected =
                selection.choiceSelections[GuidedCharacterSetupViewModel.classProficiencyChoiceKey(index)].orEmpty()
            if (selected.isNotEmpty()) {
                effects += Effect.AddProficienciesEffect(selected)
            }
        }
    }

    findGuidedLevelOneFeatureChoices(clazz, featuresById).forEach { (featureId, choice) ->
        val selected =
            selection.choiceSelections[GuidedCharacterSetupViewModel.featureChoiceKey(featureId)].orEmpty()
        if (selected.isEmpty()) return@forEach
        if (choice is Choice.ProficiencyChoice && featureId != ROGUE_EXPERTISE_FEATURE_ID) {
            effects += Effect.AddProficienciesEffect(selected)
        }
    }

    return effects
}

internal fun buildGuidedFeaturesAndTraitsText(
    content: GuidedCharacterSetupUiState.Content,
    clazz: CharacterClass?,
    race: Race?,
    background: Background?,
): String {
    val selection = content.selection
    val lines = mutableListOf<String>()

    fun header(title: String) {
        if (lines.isNotEmpty()) lines += ""
        lines += title
    }

    fun bullet(text: String) {
        lines += "• $text"
    }

    fun spellName(id: String): String =
        content.spellsById[id]?.name ?: id

    fun optionName(id: String): String {
        if (id.lowercase() in AbilityIds.standardOrder) return id.displayName()
        if (id.startsWith("skill-")) {
            val normalized = id.removePrefix("skill-").replace("-", "_").uppercase()
            val skill = Skill.entries.firstOrNull { it.name == normalized }
            if (skill != null) return skill.displayName
        }
        content.languagesById[id]?.let { return it.name }
        content.equipmentById[id]?.let { return it.name }
        content.featuresById[id]?.let { return it.name }
        content.traitsById[id]?.let { return it.name }
        content.spellsById[id]?.let { return it.name }
        return EntityRef(id).prettyString()
    }

    if (clazz != null) {
        header("Class")
        bullet(clazz.name)

        val subclass = selection.subclassId?.let { sid -> clazz.subclasses.firstOrNull { it.id == sid } }
        if (subclass != null) {
            bullet("Subclass: ${subclass.name}")
            val subclassLevel1Features = subclass.levels?.firstOrNull { it.level == 1 }?.features.orEmpty()
            subclassLevel1Features.mapNotNull(content.featuresById::get).forEach { feature ->
                val summary = feature.desc.firstOrNull().orEmpty()
                bullet(if (summary.isBlank()) feature.name else "${feature.name} — $summary")
            }
        }

        findGuidedLevelOneFeatureChoices(clazz, content.featuresById).forEach { (featureId, _) ->
            val selected =
                selection.choiceSelections[GuidedCharacterSetupViewModel.featureChoiceKey(featureId)].orEmpty()
            if (selected.isEmpty()) return@forEach
            val title = content.featuresById[featureId]?.name ?: featureId
            val values = selected.map(::optionName).sorted().joinToString(", ")
            bullet("$title: $values")
        }

        val selectedCantrips =
            selection.choiceSelections[GuidedCharacterSetupViewModel.spellCantripsChoiceKey()].orEmpty()
        val selectedLevel1 =
            selection.choiceSelections[GuidedCharacterSetupViewModel.spellLevel1ChoiceKey()].orEmpty()
        if (selectedCantrips.isNotEmpty() || selectedLevel1.isNotEmpty()) {
            header("Spells")
            if (selectedCantrips.isNotEmpty()) {
                bullet("Cantrips: ${selectedCantrips.map(::spellName).sorted().joinToString(", ")}")
            }
            if (selectedLevel1.isNotEmpty()) {
                bullet("Level 1: ${selectedLevel1.map(::spellName).sorted().joinToString(", ")}")
            }
        }
    }

    if (race != null) {
        header("Race")
        val raceLabel = buildString {
            append(race.name)
            val subrace = selection.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
            if (subrace != null) {
                append(" (")
                append(subrace.name)
                append(")")
            }
        }
        bullet(raceLabel)

        val traitIds = buildList {
            addAll(race.traits.map { it.id })
            val subrace = selection.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
            if (subrace != null) {
                addAll(subrace.traits.map { it.id })
            }
        }
        val traits = traitIds.mapNotNull(content.traitsById::get)
        val traitNames = traits.map { it.name }.sorted()
        if (traitNames.isNotEmpty()) {
            bullet("Traits: ${traitNames.joinToString(", ")}")
        }

        traits.forEach { trait ->
            trait.abilityBonusChoice?.let {
                val selected =
                    selection.choiceSelections[GuidedCharacterSetupViewModel.raceTraitAbilityBonusChoiceKey(
                        trait.id
                    )]
                        .orEmpty()
                if (selected.isNotEmpty()) {
                    bullet("${trait.name}: ${selected.joinToString(", ") { id -> "${id.displayName()} +1" }}")
                }
            }
            trait.languageChoice?.let {
                val selected =
                    selection.choiceSelections[GuidedCharacterSetupViewModel.raceTraitLanguageChoiceKey(trait.id)]
                        .orEmpty()
                if (selected.isNotEmpty()) {
                    bullet("${trait.name}: ${selected.map(::optionName).sorted().joinToString(", ")}")
                }
            }
            trait.proficiencyChoice?.let {
                val selected =
                    selection.choiceSelections[GuidedCharacterSetupViewModel.raceTraitProficiencyChoiceKey(
                        trait.id
                    )]
                        .orEmpty()
                if (selected.isNotEmpty()) {
                    bullet("${trait.name}: ${selected.map(::optionName).sorted().joinToString(", ")}")
                }
            }
            trait.draconicAncestryChoice?.let {
                val selected =
                    selection.choiceSelections[GuidedCharacterSetupViewModel.raceTraitDraconicAncestryChoiceKey(
                        trait.id
                    )]
                        .orEmpty()
                if (selected.isNotEmpty()) {
                    bullet("${trait.name}: ${selected.map(::optionName).sorted().joinToString(", ")}")
                }
            }
            trait.spellChoice?.let {
                val selected =
                    selection.choiceSelections[GuidedCharacterSetupViewModel.raceTraitSpellChoiceKey(trait.id)].orEmpty()
                if (selected.isNotEmpty()) {
                    bullet("${trait.name}: ${selected.map(::spellName).sorted().joinToString(", ")}")
                }
            }
        }
    }

    if (background != null) {
        header("Background")
        bullet(background.name)
        bullet("Feature: ${background.feature.name}")

        background.languageChoice?.let {
            val selected =
                selection.choiceSelections[GuidedCharacterSetupViewModel.backgroundLanguageChoiceKey()].orEmpty()
            if (selected.isNotEmpty()) {
                bullet("Languages: ${selected.map(::optionName).sorted().joinToString(", ")}")
            }
        }
        background.equipmentChoice?.let {
            val selected =
                selection.choiceSelections[GuidedCharacterSetupViewModel.backgroundEquipmentChoiceKey()].orEmpty()
            if (selected.isNotEmpty()) {
                bullet("Equipment: ${selected.map(::optionName).sorted().joinToString(", ")}")
            }
        }
    }

    return lines.joinToString("\n").trim()
}

internal fun Map<AbilityId, Int>.toEntityRefMap(): Map<EntityRef, Int> =
    entries.associate { (abilityId, score) -> EntityRef(abilityId) to score }

internal fun Map<EntityRef, Int>.toAbilityScores(): AbilityScores =
    AbilityScores(
        strength = this[EntityRef(AbilityIds.STR)] ?: 10,
        dexterity = this[EntityRef(AbilityIds.DEX)] ?: 10,
        constitution = this[EntityRef(AbilityIds.CON)] ?: 10,
        intelligence = this[EntityRef(AbilityIds.INT)] ?: 10,
        wisdom = this[EntityRef(AbilityIds.WIS)] ?: 10,
        charisma = this[EntityRef(AbilityIds.CHA)] ?: 10,
    )

internal fun skillFromProficiencyId(id: EntityRef): Skill? = skillFromProficiencyId(id.id)

internal fun skillFromProficiencyId(id: String): Skill? {
    val normalized = id.removePrefix("skill-")
        .replace("-", "_")
        .uppercase()
    return Skill.entries.firstOrNull { it.name == normalized }
}

private const val ROGUE_EXPERTISE_FEATURE_ID = "rogue-expertise-1"
