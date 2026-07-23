@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.EquipmentCategory
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.displayName
import com.github.arhor.spellbindr.ui.components.ErrorMessage
import com.github.arhor.spellbindr.ui.components.LoadingIndicator
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.StandardArray
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.pointBuyCost
import com.github.arhor.spellbindr.ui.feature.character.guided.model.AbilityScoreMethod
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedCharacterPreview
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedStep
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedValidationIssue

internal fun CharacterClass.requiresLevelOneSubclass(): Boolean =
    id in setOf("cleric", "sorcerer", "warlock")

internal fun AbilityScores.scoreFor(abilityId: AbilityId): Int =
    when (abilityId.lowercase()) {
        AbilityIds.STR -> strength
        AbilityIds.DEX -> dexterity
        AbilityIds.CON -> constitution
        AbilityIds.INT -> intelligence
        AbilityIds.WIS -> wisdom
        AbilityIds.CHA -> charisma
        else -> 0
    }


@Composable
internal fun GuidedCharacterSetupScreen(
    state: GuidedCharacterSetupUiState,
    dispatch: GuidedCharacterSetupDispatch = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (state) {
            GuidedCharacterSetupUiState.Loading -> LoadingIndicator()
            is GuidedCharacterSetupUiState.Failure -> ErrorMessage(state.errorMessage)
            is GuidedCharacterSetupUiState.Content -> GuidedCharacterSetupContent(
                state = state,
                dispatch = dispatch,
            )
        }
    }
}

@Composable
private fun GuidedCharacterSetupContent(
    state: GuidedCharacterSetupUiState.Content,
    dispatch: GuidedCharacterSetupDispatch,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(state.step) {
        listState.scrollToItem(0)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        LinearProgressIndicator(
            progress = { (state.currentStepIndex + 1).toFloat() / state.totalSteps.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )

        Box(modifier = Modifier.weight(1f)) {
            when (state.step) {
                GuidedStep.BASICS -> BasicsStep(
                    state = state,
                    onNameChanged = { dispatch(GuidedCharacterSetupIntent.NameChanged(it)) },
                    listState = listState,
                )

                GuidedStep.CLASS -> ClassStep(
                    state = state,
                    onClassSelected = { dispatch(GuidedCharacterSetupIntent.ClassSelected(it)) },
                    listState = listState,
                )

                GuidedStep.CLASS_CHOICES -> ClassChoicesStep(
                    state = state,
                    onSubclassSelected = { dispatch(GuidedCharacterSetupIntent.SubclassSelected(it)) },
                    onChoiceToggled = { key, optionId, maxSelected ->
                        dispatch(GuidedCharacterSetupIntent.ChoiceToggled(key, optionId, maxSelected))
                    },
                    listState = listState,
                )

                GuidedStep.RACE -> RaceStep(
                    state = state,
                    onRaceSelected = { dispatch(GuidedCharacterSetupIntent.RaceSelected(it)) },
                    onSubraceSelected = { dispatch(GuidedCharacterSetupIntent.SubraceSelected(it)) },
                    onChoiceToggled = { key, optionId, maxSelected ->
                        dispatch(GuidedCharacterSetupIntent.ChoiceToggled(key, optionId, maxSelected))
                    },
                    listState = listState,
                )

                GuidedStep.BACKGROUND -> BackgroundStep(
                    state = state,
                    onBackgroundSelected = { dispatch(GuidedCharacterSetupIntent.BackgroundSelected(it)) },
                    onChoiceToggled = { key, optionId, maxSelected ->
                        dispatch(GuidedCharacterSetupIntent.ChoiceToggled(key, optionId, maxSelected))
                    },
                    listState = listState,
                )

                GuidedStep.ABILITY_METHOD -> AbilityMethodStep(
                    state = state,
                    onAbilityMethodSelected = {
                        dispatch(GuidedCharacterSetupIntent.AbilityMethodSelected(it))
                    },
                    listState = listState,
                )

                GuidedStep.ABILITY_ASSIGN -> AbilityAssignStep(
                    state = state,
                    onStandardArrayAssigned = { abilityId, score ->
                        dispatch(GuidedCharacterSetupIntent.StandardArrayAssigned(abilityId, score))
                    },
                    onPointBuyIncrement = { dispatch(GuidedCharacterSetupIntent.PointBuyIncrement(it)) },
                    onPointBuyDecrement = { dispatch(GuidedCharacterSetupIntent.PointBuyDecrement(it)) },
                    listState = listState,
                )

                GuidedStep.SKILLS_PROFICIENCIES -> SkillsStep(
                    state = state,
                    onChoiceToggled = { key, optionId, maxSelected ->
                        dispatch(GuidedCharacterSetupIntent.ChoiceToggled(key, optionId, maxSelected))
                    },
                    listState = listState,
                )

                GuidedStep.EQUIPMENT -> EquipmentStep(
                    state = state,
                    onChoiceToggled = { key, optionId, maxSelected ->
                        dispatch(GuidedCharacterSetupIntent.ChoiceToggled(key, optionId, maxSelected))
                    },
                    listState = listState,
                )

                GuidedStep.SPELLS -> SpellsStep(
                    state = state,
                    onChoiceToggled = { key, optionId, maxSelected ->
                        dispatch(GuidedCharacterSetupIntent.ChoiceToggled(key, optionId, maxSelected))
                    },
                    listState = listState,
                )

                GuidedStep.REVIEW -> ReviewStep(
                    state = state,
                    validation = state.validation,
                    onGoToStep = { dispatch(GuidedCharacterSetupIntent.GoToStep(it)) },
                    listState = listState,
                )
            }
        }

        HorizontalDivider()

        val isReview = state.step == GuidedStep.REVIEW
        val canGoPrev = state.currentStepIndex > 0 && !state.isSaving
        val validation = if (isReview) state.validation else null
        val canContinue = if (isReview) !validation!!.hasErrors && !state.isSaving else canProceedFromStep(state)
        val blockingReason = if (!canContinue) {
            if (isReview) {
                validation?.issues
                    ?.firstOrNull { it.severity == GuidedValidationIssue.Severity.ERROR }
                    ?.message
                    ?: "Fix the required fields to create the character."
            } else {
                stepBlockingReason(state)
            }
        } else {
            null
        }

        if (blockingReason != null) {
            Text(
                text = blockingReason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = { dispatch(GuidedCharacterSetupIntent.BackClicked) },
                enabled = canGoPrev,
            ) {
                Text("Back")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (isReview) {
                        dispatch(GuidedCharacterSetupIntent.CreateClicked)
                    } else {
                        dispatch(GuidedCharacterSetupIntent.NextClicked)
                    }
                },
                enabled = canContinue,
            ) {
                Text(if (isReview) "Create character" else "Next")
            }
        }
    }
}

internal fun resolveOptions(choice: Choice, state: GuidedCharacterSetupUiState.Content): Map<String, String> =
    when (choice) {
        is Choice.FromAllChoice ->
            state.languages.associate { it.id to it.name }

        is Choice.OptionsArrayChoice ->
            choice.from.associateWith { optionId -> displayNameForOption(optionId, state) }

        is Choice.ProficiencyChoice ->
            choice.from.associateWith { optionId -> displayNameForOption(optionId, state) }

        is Choice.EquipmentChoice ->
            choice.from.associateWith { optionId -> displayNameForOption(optionId, state) }

        is Choice.EquipmentCategoriesChoice -> {
            val categoryIds = choice.from.categories
            val categories = categoryIds.mapNotNull(::equipmentCategoryFromId).toSet()
            state.equipment
                .asSequence()
                .filter { categories.all { c -> c in it.categories } }
                .sortedBy { it.name }
                .associate { it.id to it.name }
        }

        is Choice.FeatureChoice ->
            choice.from.associateWith { optionId ->
                state.featuresById[optionId]?.name ?: EntityRef(optionId).prettyString()
            }

        is Choice.FavoredEnemyChoice ->
            choice.from.associateWith { it }

        is Choice.TerrainTypeChoice ->
            choice.from.associateWith { it }

        is Choice.AbilityBonusChoice ->
            choice.from.flatMap { it.keys }.distinct().associateWith { "${it.displayName()} +1" }

        is Choice.ResourceListChoice ->
            when (choice.from.lowercase()) {
                "languages" -> state.languages.associate { it.id to it.name }
                "spells" -> {
                    var seq = state.spells.asSequence()
                    choice.where?.forEach { (key, value) ->
                        when (key.lowercase()) {
                            "classes" -> {
                                seq = seq.filter { spell -> spell.classes.any { it.id == value } }
                            }

                            "level" -> {
                                val level = value.toIntOrNull()
                                if (level != null) {
                                    seq = seq.filter { spell -> spell.level == level }
                                }
                            }
                        }
                    }
                    seq.sortedBy { it.name }.associate { it.id to it.name }
                }
                else -> emptyMap()
            }

        is Choice.IdealChoice ->
            emptyMap()

        is Choice.NestedChoice ->
            emptyMap()
    }

internal fun computeAlreadySelectedLanguageReasons(
    state: GuidedCharacterSetupUiState.Content,
    currentChoiceKey: String,
): Map<String, String> {
    val result = linkedMapOf<String, String>()

    fun record(languageId: String, reason: String) {
        result.putIfAbsent(languageId, reason)
    }

    val selectedRace = state.selection.raceId?.let { rid -> state.races.firstOrNull { it.id == rid } }
    if (selectedRace != null) {
        val traitIds = buildList {
            addAll(selectedRace.traits.map { it.id })
            val subrace = state.selection.subraceId?.let { sid -> selectedRace.subraces.firstOrNull { it.id == sid } }
            if (subrace != null) {
                addAll(subrace.traits.map { it.id })
            }
        }
        traitIds.mapNotNull { state.traitsById[it] }.forEach { trait ->
            trait.effects.orEmpty().forEach { effect ->
                if (effect is com.github.arhor.spellbindr.domain.model.Effect.AddLanguagesEffect) {
                    effect.languages.forEach { languageId ->
                        record(languageId, "Race trait: ${trait.name}")
                    }
                }
            }
        }
    }

    val selectedBackground = state.selection.backgroundId?.let { bid -> state.backgrounds.firstOrNull { it.id == bid } }
    if (selectedBackground != null) {
        selectedBackground.effects.forEach { effect ->
            if (effect is com.github.arhor.spellbindr.domain.model.Effect.AddLanguagesEffect) {
                effect.languages.forEach { languageId ->
                    record(languageId, "Background: ${selectedBackground.name}")
                }
            }
        }
    }

    state.selection.choiceSelections.forEach { (key, selectedIds) ->
        if (key == currentChoiceKey) return@forEach
        val reason = when {
            key.startsWith("race/trait/") && key.endsWith("/language") -> {
                val parts = key.split('/')
                val traitId = parts.getOrNull(2)
                val traitName = traitId?.let { state.traitsById[it]?.name }
                if (!traitName.isNullOrBlank()) "Race choice: $traitName" else "Race choice"
            }

            key == GuidedCharacterSetupViewModel.backgroundLanguageChoiceKey() -> {
                selectedBackground?.let { "Background choice: ${it.name}" } ?: "Background choice"
            }

            else -> null
        }
        if (reason != null) {
            selectedIds.forEach { record(it, reason) }
        }
    }

    return result
}

internal fun computeAlreadySelectedProficiencyReasons(
    state: GuidedCharacterSetupUiState.Content,
    currentChoiceKey: String,
): Map<String, String> {
    val result = linkedMapOf<String, String>()

    fun record(proficiencyId: String, reason: String) {
        result.putIfAbsent(proficiencyId, reason)
    }

    val selectedClass = state.selection.classId?.let { cid -> state.classes.firstOrNull { it.id == cid } }
    if (selectedClass != null) {
        selectedClass.proficiencies.forEach { proficiencyId ->
            record(proficiencyId, "Class: ${selectedClass.name}")
        }
    }

    val selectedRace = state.selection.raceId?.let { rid -> state.races.firstOrNull { it.id == rid } }
    if (selectedRace != null) {
        val traitIds = buildList {
            addAll(selectedRace.traits.map { it.id })
            val subrace = state.selection.subraceId?.let { sid -> selectedRace.subraces.firstOrNull { it.id == sid } }
            if (subrace != null) {
                addAll(subrace.traits.map { it.id })
            }
        }
        traitIds.mapNotNull { state.traitsById[it] }.forEach { trait ->
            trait.effects.orEmpty().forEach { effect ->
                if (effect is com.github.arhor.spellbindr.domain.model.Effect.AddProficienciesEffect) {
                    effect.proficiencies.forEach { proficiencyId ->
                        record(proficiencyId, "Race trait: ${trait.name}")
                    }
                }
            }
        }
    }

    val selectedBackground = state.selection.backgroundId?.let { bid -> state.backgrounds.firstOrNull { it.id == bid } }
    if (selectedBackground != null) {
        selectedBackground.effects.forEach { effect ->
            if (effect is com.github.arhor.spellbindr.domain.model.Effect.AddProficienciesEffect) {
                effect.proficiencies.forEach { proficiencyId ->
                    record(proficiencyId, "Background: ${selectedBackground.name}")
                }
            }
        }
    }

    state.selection.choiceSelections.forEach { (key, selectedIds) ->
        if (key == currentChoiceKey) return@forEach
        val reason = when {
            key.startsWith("class/proficiency/") -> {
                val index = key.substringAfterLast('/').toIntOrNull()
                if (index != null) "Class choice ${index + 1}" else "Class choice"
            }

            key.startsWith("race/trait/") && key.endsWith("/proficiency") -> {
                val parts = key.split('/')
                val traitId = parts.getOrNull(2)
                val traitName = traitId?.let { state.traitsById[it]?.name }
                if (!traitName.isNullOrBlank()) "Race choice: $traitName" else "Race choice"
            }

            key.startsWith("feature/") -> {
                val featureId = key.removePrefix("feature/")
                val featureName = state.featuresById[featureId]?.name
                if (!featureName.isNullOrBlank()) "Feature choice: $featureName" else "Feature choice"
            }

            else -> null
        }
        if (reason != null) {
            selectedIds.forEach { record(it, reason) }
        }
    }

    return result
}

internal fun displayNameForOption(id: String, state: GuidedCharacterSetupUiState.Content): String {
    if (id.startsWith("skill-")) {
        val normalized = id.removePrefix("skill-").replace("-", "_").uppercase()
        val skill = Skill.entries.firstOrNull { it.name == normalized }
        if (skill != null) return skill.displayName
    }
    state.languagesById[id]?.let { return it.name }
    state.equipmentById[id]?.let { return it.name }
    state.featuresById[id]?.let { return it.name }
    state.traitsById[id]?.let { return it.name }
    state.spellsById[id]?.let { return it.name }
    return EntityRef(id).prettyString()
}

internal data class SpellRequirements(
    val cantrips: Int,
    val level1Spells: Int,
    val level1Label: String,
)

internal fun computeSpellRequirements(
    clazz: CharacterClass,
    preview: GuidedCharacterPreview,
): SpellRequirements? {
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
    val label = when (clazz.id) {
        "wizard" -> "spellbook spell(s)"
        "cleric", "druid" -> "prepared spell(s)"
        else -> "spell(s)"
    }

    return SpellRequirements(
        cantrips = cantrips,
        level1Spells = level1Spells,
        level1Label = label,
    )
}

private fun canProceedFromStep(state: GuidedCharacterSetupUiState.Content): Boolean {
    val selection = state.selection
    return when (state.step) {
        GuidedStep.BASICS -> true
        GuidedStep.CLASS -> selection.classId != null
        GuidedStep.CLASS_CHOICES -> classChoicesComplete(state)
        GuidedStep.RACE -> raceComplete(state)
        GuidedStep.BACKGROUND -> backgroundComplete(state)
        GuidedStep.ABILITY_METHOD -> selection.abilityMethod != null
        GuidedStep.ABILITY_ASSIGN -> abilityAssignComplete(state)
        GuidedStep.SKILLS_PROFICIENCIES -> classProficiencyChoicesComplete(state)
        GuidedStep.EQUIPMENT -> backgroundEquipmentComplete(state)
        GuidedStep.SPELLS -> spellsComplete(state)
        GuidedStep.REVIEW -> false
    }
}

private fun stepBlockingReason(state: GuidedCharacterSetupUiState.Content): String? {
    val selection = state.selection
    return when (state.step) {
        GuidedStep.BASICS -> null

        GuidedStep.CLASS ->
            if (selection.classId == null) "Select a class to continue." else null

        GuidedStep.CLASS_CHOICES -> {
            val clazz = selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } }
                ?: return "Select a class first."

            if (clazz.requiresLevelOneSubclass() && selection.subclassId == null) {
                return "Select a subclass to continue."
            }

            val level1Features = clazz.levels.firstOrNull { it.level == 1 }?.features.orEmpty()
            for (featureId in level1Features) {
                val feature = state.featuresById[featureId] ?: continue
                val choice = feature.choice ?: continue
                val selected =
                    selection.choiceSelections[GuidedCharacterSetupViewModel.featureChoiceKey(featureId)].orEmpty()
                if (selected.size != choice.choose) {
                    return "Select ${choice.choose} option(s) for ${feature.name}."
                }
            }
            null
        }

        GuidedStep.RACE -> {
            val race = selection.raceId?.let { id -> state.races.firstOrNull { it.id == id } }
                ?: return "Select a race to continue."
            if (race.subraces.isNotEmpty() && selection.subraceId == null) return "Select a subrace to continue."

            val traitIds = buildList {
                addAll(race.traits.map { it.id })
                val subrace = selection.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
                if (subrace != null) addAll(subrace.traits.map { it.id })
            }
            val traits = traitIds.mapNotNull { state.traitsById[it] }
            for (trait in traits) {
                val requiredChoices = listOfNotNull(
                    trait.abilityBonusChoice?.let { GuidedCharacterSetupViewModel.raceTraitAbilityBonusChoiceKey(trait.id) to it.choose },
                    trait.languageChoice?.let { GuidedCharacterSetupViewModel.raceTraitLanguageChoiceKey(trait.id) to it.choose },
                    trait.proficiencyChoice?.let { GuidedCharacterSetupViewModel.raceTraitProficiencyChoiceKey(trait.id) to it.choose },
                    trait.draconicAncestryChoice?.let {
                        GuidedCharacterSetupViewModel.raceTraitDraconicAncestryChoiceKey(trait.id) to it.choose
                    },
                    trait.spellChoice?.let { GuidedCharacterSetupViewModel.raceTraitSpellChoiceKey(trait.id) to it.choose },
                )
                for ((key, choose) in requiredChoices) {
                    if (selection.choiceSelections[key].orEmpty().size != choose) {
                        return "Select $choose option(s) for ${trait.name}."
                    }
                }
            }
            null
        }

        GuidedStep.BACKGROUND -> {
            val bg = selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id } }
                ?: return "Select a background to continue."
            val languageChoice = bg.languageChoice ?: return null
            val selected =
                selection.choiceSelections[GuidedCharacterSetupViewModel.backgroundLanguageChoiceKey()].orEmpty()
            if (selected.size != languageChoice.choose) {
                "Select ${languageChoice.choose} language(s) to continue."
            } else {
                null
            }
        }

        GuidedStep.ABILITY_METHOD ->
            if (selection.abilityMethod == null) "Choose an ability score method to continue." else null

        GuidedStep.ABILITY_ASSIGN -> {
            when (selection.abilityMethod) {
                AbilityScoreMethod.STANDARD_ARRAY ->
                    if (!abilityAssignComplete(state)) "Assign all six standard array values." else null

                AbilityScoreMethod.POINT_BUY -> {
                    val cost = selection.pointBuyScores.values.sumOf(::pointBuyCost)
                    if (cost > 27) "Point buy exceeds 27 points." else null
                }

                null -> "Choose an ability score method first."
            }
        }

        GuidedStep.SKILLS_PROFICIENCIES -> {
            val clazz = selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } } ?: return null
            clazz.proficiencyChoices.withIndex().firstOrNull { (index, choice) ->
                selection.choiceSelections[GuidedCharacterSetupViewModel.classProficiencyChoiceKey(index)].orEmpty().size != choice.choose
            }?.let { (index, choice) ->
                "Select ${choice.choose} option(s) for class choice ${index + 1}."
            }
        }

        GuidedStep.EQUIPMENT -> {
            val bg = selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id } } ?: return null
            val equipmentChoice = bg.equipmentChoice ?: return null
            val selected =
                selection.choiceSelections[GuidedCharacterSetupViewModel.backgroundEquipmentChoiceKey()].orEmpty()
            if (selected.size != equipmentChoice.choose) {
                "Select ${equipmentChoice.choose} equipment option(s) to continue."
            } else {
                null
            }
        }

        GuidedStep.SPELLS -> {
            val clazz = selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } } ?: return null
            val requirements = computeSpellRequirements(clazz, state.preview) ?: return null
            val selectedCantrips =
                selection.choiceSelections[GuidedCharacterSetupViewModel.spellCantripsChoiceKey()].orEmpty()
            if (requirements.cantrips > 0 && selectedCantrips.size != requirements.cantrips) {
                return "Select ${requirements.cantrips} cantrip(s) to continue."
            }
            val selectedSpells =
                selection.choiceSelections[GuidedCharacterSetupViewModel.spellLevel1ChoiceKey()].orEmpty()
            if (requirements.level1Spells > 0 && selectedSpells.size != requirements.level1Spells) {
                return "Select ${requirements.level1Spells} ${requirements.level1Label} to continue."
            }
            null
        }

        GuidedStep.REVIEW -> null
    }
}

private fun classChoicesComplete(state: GuidedCharacterSetupUiState.Content): Boolean {
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } } ?: return false
    val requiresSubclass = clazz.id in setOf("cleric", "sorcerer", "warlock")
    if (requiresSubclass && state.selection.subclassId == null) return false

    val level1Features = clazz.levels.firstOrNull { it.level == 1 }?.features.orEmpty()
    val requiredFeatureChoices = level1Features.mapNotNull { featureId ->
        val choice = state.featuresById[featureId]?.choice ?: return@mapNotNull null
        featureId to choice.choose
    }
    return !requiredFeatureChoices.any { (featureId, choose) ->
        state.selection.choiceSelections[GuidedCharacterSetupViewModel.featureChoiceKey(featureId)].orEmpty().size != choose
    }
}

private fun raceComplete(state: GuidedCharacterSetupUiState.Content): Boolean {
    val race = state.selection.raceId?.let { id -> state.races.firstOrNull { it.id == id } } ?: return false
    if (race.subraces.isNotEmpty() && state.selection.subraceId == null) return false

    val traitIds = buildList {
        addAll(race.traits.map { it.id })
        val subrace = state.selection.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
        if (subrace != null) {
            addAll(subrace.traits.map { it.id })
        }
    }
    val traits = traitIds.mapNotNull { state.traitsById[it] }
    return traits.all { trait ->
        listOfNotNull(
            trait.abilityBonusChoice?.let { GuidedCharacterSetupViewModel.raceTraitAbilityBonusChoiceKey(trait.id) to it.choose },
            trait.languageChoice?.let { GuidedCharacterSetupViewModel.raceTraitLanguageChoiceKey(trait.id) to it.choose },
            trait.proficiencyChoice?.let { GuidedCharacterSetupViewModel.raceTraitProficiencyChoiceKey(trait.id) to it.choose },
            trait.draconicAncestryChoice?.let { GuidedCharacterSetupViewModel.raceTraitDraconicAncestryChoiceKey(trait.id) to it.choose },
            trait.spellChoice?.let { GuidedCharacterSetupViewModel.raceTraitSpellChoiceKey(trait.id) to it.choose },
        ).all { (key, choose) ->
            state.selection.choiceSelections[key].orEmpty().size == choose
        }
    }
}

private fun backgroundComplete(state: GuidedCharacterSetupUiState.Content): Boolean {
    val bg = state.selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id } } ?: return false
    val languageChoice = bg.languageChoice ?: return true
    return state.selection.choiceSelections[GuidedCharacterSetupViewModel.backgroundLanguageChoiceKey()].orEmpty().size == languageChoice.choose
}

private fun backgroundEquipmentComplete(state: GuidedCharacterSetupUiState.Content): Boolean {
    val bg = state.selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id } } ?: return true
    val equipmentChoice = bg.equipmentChoice ?: return true
    return state.selection.choiceSelections[GuidedCharacterSetupViewModel.backgroundEquipmentChoiceKey()].orEmpty().size == equipmentChoice.choose
}

private fun abilityAssignComplete(state: GuidedCharacterSetupUiState.Content): Boolean {
    return when (state.selection.abilityMethod) {
        AbilityScoreMethod.STANDARD_ARRAY -> {
            val values = state.selection.standardArrayAssignments.values.filterNotNull()
            values.size == 6 && values.sorted() == StandardArray.sorted()
        }

        AbilityScoreMethod.POINT_BUY -> state.selection.pointBuyScores.values.sumOf(::pointBuyCost) <= 27
        null -> false
    }
}

private fun classProficiencyChoicesComplete(state: GuidedCharacterSetupUiState.Content): Boolean {
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } } ?: return true
    return clazz.proficiencyChoices.withIndex().all { (index, choice) ->
        state.selection.choiceSelections[GuidedCharacterSetupViewModel.classProficiencyChoiceKey(index)]
            .orEmpty()
            .size == choice.choose
    }
}

private fun spellsComplete(state: GuidedCharacterSetupUiState.Content): Boolean {
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } } ?: return true
    val requirements = computeSpellRequirements(clazz, state.preview) ?: return true

    val selectedCantrips =
        state.selection.choiceSelections[GuidedCharacterSetupViewModel.spellCantripsChoiceKey()].orEmpty()
    if (requirements.cantrips > 0 && selectedCantrips.size != requirements.cantrips) return false

    val selectedSpells =
        state.selection.choiceSelections[GuidedCharacterSetupViewModel.spellLevel1ChoiceKey()].orEmpty()
    return !(requirements.level1Spells > 0 && selectedSpells.size != requirements.level1Spells)
}

private fun equipmentCategoryFromId(id: String): EquipmentCategory? = when (id) {
    "weapon" -> EquipmentCategory.WEAPON
    "armor" -> EquipmentCategory.ARMOR
    "tool" -> EquipmentCategory.TOOL
    "gear" -> EquipmentCategory.GEAR
    "holy-symbol" -> EquipmentCategory.HOLY_SYMBOL
    "standard" -> EquipmentCategory.STANDARD
    "musical-instrument" -> EquipmentCategory.MUSICAL_INSTRUMENT
    "gaming-set" -> EquipmentCategory.GAMING_SET
    "other" -> EquipmentCategory.OTHER
    "arcane-focus" -> EquipmentCategory.ARCANE_FOCUS
    "druidic-focus" -> EquipmentCategory.DRUIDIC_FOCUS
    "kit" -> EquipmentCategory.KIT
    "simple" -> EquipmentCategory.SIMPLE
    "martial" -> EquipmentCategory.MARTIAL
    "ranged" -> EquipmentCategory.RANGED
    "melee" -> EquipmentCategory.MELEE
    "shield" -> EquipmentCategory.SHIELD
    "light" -> EquipmentCategory.LIGHT
    "heavy" -> EquipmentCategory.HEAVY
    "medium" -> EquipmentCategory.MEDIUM
    "ammunition" -> EquipmentCategory.AMMUNITION
    "equipment-pack" -> EquipmentCategory.EQUIPMENT_PACK
    "artisans-tool" -> EquipmentCategory.ARTISANS_TOOL
    "gaming-sets" -> EquipmentCategory.GAMING_SETS
    "mounts-and-other-animals" -> EquipmentCategory.MOUNTS_AND_OTHER_ANIMALS
    "vehicle" -> EquipmentCategory.VEHICLE
    "tack-harness-and-drawn-vehicle" -> EquipmentCategory.TACK_HARNESS_AND_DRAWN_VEHICLE
    "waterborne-vehicle" -> EquipmentCategory.WATERBORNE_VEHICLE
    else -> null
}
