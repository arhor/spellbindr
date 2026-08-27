package io.github.arhor.dnd.companion.api

import io.github.arhor.dnd.companion.api.v1.*
import io.grpc.stub.StreamObserver
import io.quarkus.grpc.GrpcService

@GrpcService
class CatalogGrpcService(private val store: CatalogStore) : CatalogServiceGrpc.CatalogServiceImplBase() {
    override fun listRulesets(request: ListRulesetsRequest, observer: StreamObserver<ListRulesetsResponse>) = respond(observer) {
        val size = request.page.pageSize.let { if (it == 0) 50 else it }
        require(size in 1..100) { "page_size must be between 0 and 100" }
        ListRulesetsResponse.newBuilder().addAllRulesets(store.snapshot.rulesetsList)
            .setPageInfo(PageInfo.newBuilder().setTotalSize(store.snapshot.rulesetsCount)).build()
    }

    override fun listContentSources(request: ListContentSourcesRequest, observer: StreamObserver<ListContentSourcesResponse>) = respond(observer) {
        if (request.rulesetId != store.snapshot.manifest.resolvedSelection.rulesetId) invalid("Unknown ruleset")
        ListContentSourcesResponse.newBuilder().addAllSources(store.snapshot.sourcesList)
            .setPageInfo(PageInfo.newBuilder().setTotalSize(store.snapshot.sourcesCount)).build()
    }

    override fun getCatalogManifest(request: GetCatalogManifestRequest, observer: StreamObserver<GetCatalogManifestResponse>) = respond(observer) {
        store.validate(CatalogReadContext.newBuilder().setSelection(request.selection).build())
        GetCatalogManifestResponse.newBuilder().setManifest(store.snapshot.manifest).build()
    }
}

internal fun invalid(message: String): Nothing = throw io.grpc.Status.INVALID_ARGUMENT.withDescription(message).asRuntimeException()

internal inline fun <T> respond(observer: StreamObserver<T>, block: () -> T) {
    try {
        observer.onNext(block())
        observer.onCompleted()
    } catch (error: Throwable) {
        observer.onError(if (error is IllegalArgumentException) io.grpc.Status.INVALID_ARGUMENT.withDescription(error.message).asRuntimeException() else error)
    }
}
