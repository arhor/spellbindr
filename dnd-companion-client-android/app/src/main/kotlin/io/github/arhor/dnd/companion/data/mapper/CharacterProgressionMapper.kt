package io.github.arhor.dnd.companion.data.mapper

import io.github.arhor.dnd.companion.data.local.database.CharacterProgressionJsonCodec
import io.github.arhor.dnd.companion.data.local.database.entity.CharacterProgressionEntity
import io.github.arhor.dnd.companion.domain.model.ProgressionState

fun CharacterProgressionEntity?.toDomain(codec: CharacterProgressionJsonCodec): ProgressionState =
    this?.let { codec.decode(stateJson) } ?: ProgressionState.Unmanaged

fun ProgressionState.toEntity(
    characterId: String,
    codec: CharacterProgressionJsonCodec,
): CharacterProgressionEntity = CharacterProgressionEntity(
    characterId = characterId,
    stateJson = codec.encode(this),
)
