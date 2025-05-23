package com.github.arhor.spellbindr.data.repository

interface DataLoader {
    val resource: String
    suspend fun loadData()
}
