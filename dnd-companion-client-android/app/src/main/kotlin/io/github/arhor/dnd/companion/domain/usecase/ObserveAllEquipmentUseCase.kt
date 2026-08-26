package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.Equipment
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.EquipmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllEquipmentUseCase @Inject constructor(
    private val equipmentRepository: EquipmentRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Equipment>>> =
        equipmentRepository.allEquipmentState
}

