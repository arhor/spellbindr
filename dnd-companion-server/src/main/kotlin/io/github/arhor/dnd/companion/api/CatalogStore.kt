package io.github.arhor.dnd.companion.api

import com.google.protobuf.Message
import io.github.arhor.dnd.companion.api.v1.CatalogReadContext
import io.github.arhor.dnd.companion.api.v1.CatalogSnapshot
import io.github.arhor.dnd.companion.api.v1.PageInfo
import io.github.arhor.dnd.companion.api.v1.ResourceKind
import io.grpc.Status
import jakarta.inject.Singleton
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64

@Singleton
class CatalogStore {
    val snapshot: CatalogSnapshot = requireNotNull(javaClass.getResourceAsStream("/catalog/catalog.pb")) {
        "Generated catalog snapshot is missing"
    }.use(CatalogSnapshot::parseFrom)

    fun validate(context: CatalogReadContext) {
        val selection = context.selection
        if (selection.rulesetId != snapshot.manifest.resolvedSelection.rulesetId) {
            throw Status.INVALID_ARGUMENT.withDescription("Unknown ruleset or source selection").asRuntimeException()
        }
        val requestedSources = selection.sourceIdsList.ifEmpty { snapshot.manifest.resolvedSelection.sourceIdsList }
        if (requestedSources.toSet() != snapshot.manifest.resolvedSelection.sourceIdsList.toSet()) {
            throw Status.INVALID_ARGUMENT.withDescription("Unknown ruleset or source selection").asRuntimeException()
        }
        if (context.hasSnapshotId() && context.snapshotId != snapshot.manifest.snapshotId) {
            throw Status.FAILED_PRECONDITION.withDescription("Requested snapshot is unavailable").asRuntimeException()
        }
    }

    fun revision(kind: ResourceKind): String = snapshot.manifest.collectionsList
        .first { it.resourceKind == kind }.revision

    fun <T : Message> get(context: CatalogReadContext, id: String, values: List<T>, idOf: (T) -> String): T {
        validate(context)
        if (id.isBlank()) throw Status.INVALID_ARGUMENT.withDescription("id is required").asRuntimeException()
        return values.firstOrNull { idOf(it) == id }
            ?: throw Status.NOT_FOUND.withDescription("Resource $id was not found").asRuntimeException()
    }

    fun <T : Message> page(
        context: CatalogReadContext,
        ids: List<String>,
        pageSize: Int,
        token: String,
        kind: ResourceKind,
        values: List<T>,
        idOf: (T) -> String,
    ): Pair<List<T>, PageInfo> {
        validate(context)
        if (ids.size > 100) throw Status.INVALID_ARGUMENT.withDescription("At most 100 ids may be requested").asRuntimeException()
        if (pageSize < 0 || pageSize > 100) throw Status.INVALID_ARGUMENT.withDescription("page_size must be between 0 and 100").asRuntimeException()
        val size = if (pageSize == 0) 50 else pageSize
        val filtered = values.asSequence().filter { ids.isEmpty() || idOf(it) in ids }.sortedBy(idOf).toList()
        val binding = binding(kind, context, ids)
        val cursor = decodeToken(token, binding)
        if (cursor > filtered.size) throw Status.INVALID_ARGUMENT.withDescription("Invalid page token").asRuntimeException()
        val end = minOf(cursor + size, filtered.size)
        val next = if (end < filtered.size) encodeToken(binding, end) else ""
        return filtered.subList(cursor, end) to PageInfo.newBuilder().setNextPageToken(next).setTotalSize(filtered.size).build()
    }

    private fun binding(kind: ResourceKind, context: CatalogReadContext, ids: List<String>): String =
        listOf(kind.number, snapshot.manifest.snapshotId, context.selection.rulesetId,
            context.selection.sourceIdsList.sorted().joinToString(","), ids.sorted().joinToString(",")).joinToString("|")

    private fun encodeToken(binding: String, cursor: Int): String {
        val payload = "$binding|$cursor"
        val signature = sha256("spellbindr-page-token|$payload").take(24)
        return Base64.getUrlEncoder().withoutPadding().encodeToString("$payload|$signature".toByteArray())
    }

    private fun decodeToken(token: String, binding: String): Int {
        if (token.isBlank()) return 0
        val decoded = runCatching { String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8) }
            .getOrElse { throw Status.INVALID_ARGUMENT.withDescription("Invalid page token").asRuntimeException() }
        val cursor = decoded.substringAfterLast('|').let { signature ->
            val payload = decoded.substringBeforeLast('|')
            if (sha256("spellbindr-page-token|$payload").take(24) != signature || !payload.startsWith("$binding|")) {
                throw Status.INVALID_ARGUMENT.withDescription("Invalid page token").asRuntimeException()
            }
            payload.substringAfterLast('|').toIntOrNull()
        }
        return cursor ?: throw Status.INVALID_ARGUMENT.withDescription("Invalid page token").asRuntimeException()
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}
