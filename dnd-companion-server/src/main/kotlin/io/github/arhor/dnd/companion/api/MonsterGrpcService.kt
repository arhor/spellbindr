package io.github.arhor.dnd.companion.api

import io.github.arhor.dnd.companion.api.v1.*
import io.grpc.stub.StreamObserver
import io.quarkus.grpc.GrpcService

@GrpcService
class MonsterGrpcService(private val store: CatalogStore) : MonsterServiceGrpc.MonsterServiceImplBase() {
    override fun getMonster(request: GetMonsterRequest, observer: StreamObserver<GetMonsterResponse>) = respond(observer) {
        GetMonsterResponse.newBuilder().setSnapshotId(store.snapshot.manifest.snapshotId)
            .setCollectionRevision(store.revision(ResourceKind.RESOURCE_KIND_MONSTER))
            .setMonster(store.get(request.context, request.id, store.snapshot.data.monstersList, Monster::getId)).build()
    }

    override fun listMonsters(request: ListMonstersRequest, observer: StreamObserver<ListMonstersResponse>) = respond(observer) {
        val (items, page) = store.page(request.context, request.idsList, request.page.pageSize, request.page.pageToken,
            ResourceKind.RESOURCE_KIND_MONSTER, store.snapshot.data.monstersList, Monster::getId)
        ListMonstersResponse.newBuilder().setSnapshotId(store.snapshot.manifest.snapshotId)
            .setCollectionRevision(store.revision(ResourceKind.RESOURCE_KIND_MONSTER)).addAllMonsters(items).setPageInfo(page).build()
    }
}
