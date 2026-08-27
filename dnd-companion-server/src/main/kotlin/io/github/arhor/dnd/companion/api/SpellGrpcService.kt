package io.github.arhor.dnd.companion.api

import io.github.arhor.dnd.companion.api.v1.*
import io.grpc.stub.StreamObserver
import io.quarkus.grpc.GrpcService

@GrpcService
class SpellGrpcService(private val store: CatalogStore) : SpellServiceGrpc.SpellServiceImplBase() {
    override fun getSpell(request: GetSpellRequest, observer: StreamObserver<GetSpellResponse>) = respond(observer) {
        GetSpellResponse.newBuilder().setSnapshotId(store.snapshot.manifest.snapshotId)
            .setCollectionRevision(store.revision(ResourceKind.RESOURCE_KIND_SPELL))
            .setSpell(store.get(request.context, request.id, store.snapshot.data.spellsList, Spell::getId)).build()
    }
    override fun listSpells(request: ListSpellsRequest, observer: StreamObserver<ListSpellsResponse>) = respond(observer) {
        val (items, page) = store.page(request.context, request.idsList, request.page.pageSize, request.page.pageToken,
            ResourceKind.RESOURCE_KIND_SPELL, store.snapshot.data.spellsList, Spell::getId)
        ListSpellsResponse.newBuilder().setSnapshotId(store.snapshot.manifest.snapshotId)
            .setCollectionRevision(store.revision(ResourceKind.RESOURCE_KIND_SPELL)).addAllSpells(items).setPageInfo(page).build()
    }
    override fun getMagicSchool(request: GetMagicSchoolRequest, observer: StreamObserver<GetMagicSchoolResponse>) = respond(observer) {
        GetMagicSchoolResponse.newBuilder().setSnapshotId(store.snapshot.manifest.snapshotId)
            .setCollectionRevision(store.revision(ResourceKind.RESOURCE_KIND_MAGIC_SCHOOL))
            .setMagicSchool(store.get(request.context, request.id, store.snapshot.data.magicSchoolsList, MagicSchool::getId)).build()
    }
    override fun listMagicSchools(request: ListMagicSchoolsRequest, observer: StreamObserver<ListMagicSchoolsResponse>) = respond(observer) {
        val (items, page) = store.page(request.context, request.idsList, request.page.pageSize, request.page.pageToken,
            ResourceKind.RESOURCE_KIND_MAGIC_SCHOOL, store.snapshot.data.magicSchoolsList, MagicSchool::getId)
        ListMagicSchoolsResponse.newBuilder().setSnapshotId(store.snapshot.manifest.snapshotId)
            .setCollectionRevision(store.revision(ResourceKind.RESOURCE_KIND_MAGIC_SCHOOL)).addAllMagicSchools(items).setPageInfo(page).build()
    }
}
