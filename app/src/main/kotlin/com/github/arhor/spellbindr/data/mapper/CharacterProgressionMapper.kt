package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.data.local.database.CharacterProgressionJsonCodec
import com.github.arhor.spellbindr.data.local.database.entity.CharacterProgressionEntity
import com.github.arhor.spellbindr.domain.model.ProgressionState

fun CharacterProgressionEntity?.toDomain(codec: CharacterProgressionJsonCodec): ProgressionState =
    this?.let { codec.decode(stateJson) } ?: ProgressionState.Unmanaged

fun ProgressionState.toEntity(
    characterId: String,
    codec: CharacterProgressionJsonCodec,
): CharacterProgressionEntity = CharacterProgressionEntity(
    characterId = characterId,
    stateJson = codec.encode(this),
)
