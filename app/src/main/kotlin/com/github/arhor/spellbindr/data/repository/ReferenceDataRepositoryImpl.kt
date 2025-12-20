package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.repository.AlignmentRepository as AlignmentRepositoryContract
import com.github.arhor.spellbindr.domain.repository.RacesRepository as RacesRepositoryContract
import com.github.arhor.spellbindr.domain.repository.ReferenceDataRepository
import com.github.arhor.spellbindr.domain.repository.TraitsRepository as TraitsRepositoryContract
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReferenceDataRepositoryImpl @Inject constructor(
    private val alignmentRepository: AlignmentRepositoryContract,
    private val racesRepository: RacesRepositoryContract,
    private val traitsRepository: TraitsRepositoryContract,
) : ReferenceDataRepository {
    override val allAlignments: Flow<List<Alignment>>
        get() = alignmentRepository.allAlignments

    override val allRaces: Flow<List<Race>>
        get() = racesRepository.allRaces

    override val allTraits: Flow<List<Trait>>
        get() = traitsRepository.allTraits

    override suspend fun findRaceById(id: String): Race? = racesRepository.findRaceById(id)
}
