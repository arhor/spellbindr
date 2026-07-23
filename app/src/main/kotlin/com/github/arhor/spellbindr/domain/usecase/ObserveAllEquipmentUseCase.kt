package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Equipment
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.EquipmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllEquipmentUseCase @Inject constructor(
    private val equipmentRepository: EquipmentRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Equipment>>> =
        equipmentRepository.allEquipmentState
}

