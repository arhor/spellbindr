package io.github.arhor.dnd.companion.api

import io.github.arhor.dnd.companion.api.v1.CatalogReadContext
import io.github.arhor.dnd.companion.api.v1.CatalogSelection
import io.github.arhor.dnd.companion.api.v1.GetMonsterRequest
import io.github.arhor.dnd.companion.api.v1.ListSpellsRequest
import io.github.arhor.dnd.companion.api.v1.MonsterServiceGrpc
import io.github.arhor.dnd.companion.api.v1.PageRequest
import io.github.arhor.dnd.companion.api.v1.SpellServiceGrpc
import io.quarkus.grpc.GrpcClient
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@QuarkusTest
class CatalogTransportTest {
    @GrpcClient("monster-service")
    lateinit var monsters: MonsterServiceGrpc.MonsterServiceBlockingStub

    @GrpcClient("spell-service")
    lateinit var spells: SpellServiceGrpc.SpellServiceBlockingStub

    private val context = CatalogReadContext.newBuilder().setSelection(
        CatalogSelection.newBuilder().setRulesetId("dnd-5e-2014-v1"),
    ).build()

    @Test
    fun `getMonster should return catalog monster when stable id exists`() {
        // Given
        val request = GetMonsterRequest.newBuilder().setContext(context).setId("aboleth").build()

        // When
        val response = monsters.getMonster(request)

        // Then
        assertEquals("aboleth", response.monster.id)
        assertTrue(response.snapshotId.isNotBlank())
    }

    @Test
    fun `listSpells should return canonical page when page size is limited`() {
        // Given
        val request = ListSpellsRequest.newBuilder().setContext(context)
            .setPage(PageRequest.newBuilder().setPageSize(2)).build()

        // When
        val response = spells.listSpells(request)

        // Then
        assertEquals(2, response.spellsCount)
        assertTrue(response.spellsList.zipWithNext().all { (left, right) -> left.id < right.id })
        assertTrue(response.pageInfo.nextPageToken.isNotBlank())
    }
}
