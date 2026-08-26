package io.github.arhor.dnd.companion.api

import io.github.arhor.dnd.companion.api.v1.CompanionApi
import io.github.arhor.dnd.companion.api.v1.PingRequest
import io.github.arhor.dnd.companion.api.v1.PingResponse
import io.quarkus.grpc.GrpcService
import io.smallrye.mutiny.Uni

@GrpcService
class CompanionGrpcService : CompanionApi {
    override fun ping(request: PingRequest): Uni<PingResponse> =
        Uni.createFrom().item(
            PingResponse.newBuilder()
                .setMessage("Hello, ${request.name.ifBlank { "adventurer" }}!")
                .build(),
        )
}
