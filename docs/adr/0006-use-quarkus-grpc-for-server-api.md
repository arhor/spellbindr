# ADR 0006: Use Quarkus and gRPC for the server API

## Status

Accepted

## Date

2026-08-26

## Context

Spellbindr needs a server-side API for its mobile client. The server build was an empty Gradle build, so its framework,
runtime baseline, implementation language, and client-facing transport were not yet constrained. These choices affect
future endpoints, generated client contracts, deployment environments, testing, and contributor workflows across the
server and mobile client.

## Decision

Use Quarkus as the server application framework, Kotlin as the server implementation language, and Java 21 as the JVM
baseline. Expose the mobile-facing API through versioned Protocol Buffers contracts and Quarkus gRPC services.

Keep dependency patch versions in executable Gradle configuration rather than this ADR. This decision establishes the
primary API transport but does not prohibit adding other protocols for operational endpoints or integrations when a
separate need is established.

## Consequences

- The protobuf definitions become the compatibility boundary between the server and mobile clients. Field numbers and
  published contracts must evolve according to protobuf compatibility rules.
- Quarkus supplies dependency injection, application lifecycle, development mode, code generation, and gRPC server
  integration, reducing custom service wiring while coupling server code and build configuration to Quarkus.
- Kotlin and Java 21 provide a consistent modern JVM baseline, but development and deployment environments must make a
  Java 21 toolchain available.
- gRPC provides typed generated clients and efficient binary transport, while requiring HTTP/2-capable infrastructure
  and tooling that can inspect protobuf APIs.
- Service implementations must respect Quarkus gRPC execution semantics and avoid blocking event-loop threads unless
  blocking work is explicitly dispatched.

## Alternatives

No alternative framework, language, JVM baseline, or primary API protocol was evaluated for this decision because the
project owner explicitly selected Quarkus, Kotlin, Java 21, and gRPC as migration constraints.

## References

- [Server build configuration](../../dnd-companion-server/build.gradle.kts)
- [Server dependency catalog](../../dnd-companion-server/gradle/libs.versions.toml)
- [Shared protobuf API](../../dnd-companion-schema/src/main/proto)
- [ADR 0007: Share protobuf contracts across client and server](0007-share-protobuf-contracts-across-client-and-server.md)
