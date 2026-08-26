package io.github.arhor.dnd.companion.api

import io.github.arhor.dnd.companion.api.v1.CompanionApi
import io.github.arhor.dnd.companion.api.v1.PingRequest
import io.quarkus.grpc.GrpcClient
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@QuarkusTest
class CompanionGrpcServiceTest {
    @GrpcClient("companion-api")
    lateinit var client: CompanionApi

    @Test
    fun `ping should return a greeting when request contains a name`() {
        // Given
        val request = PingRequest.newBuilder().setName("Minsc").build()

        // When
        val response = client.ping(request)
            .await().indefinitely()

        // Then
        assertEquals("Hello, Minsc!", response.message)
    }
}
