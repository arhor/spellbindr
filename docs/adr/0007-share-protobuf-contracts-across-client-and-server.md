# ADR 0007: Share protobuf contracts across client and server

## Status

Accepted

## Date

2026-08-27

## Context

The versioned protobuf API established by ADR 0006 is consumed by both the Android client and the server. Keeping the
contract and its generated types inside the server build makes the server their implicit owner and leaves the client
without a direct, platform-neutral dependency on the same definitions. Contract evolution must remain coordinated and
protobuf-compatible across both applications.

## Decision

Own shared reference-data protobuf definitions in a dedicated `dnd-companion-schema` Gradle library. Generate protobuf
types in that library and make both `dnd-companion-client-android` and `dnd-companion-server` depend on its published
coordinates through Gradle composite-build substitution during local development. Runtime-specific service contracts
may remain in the build that implements them when their code generator requires local ownership.

The schema library is platform-neutral. It must not depend on Android application code or server implementation code.
The protobuf files and their stable field numbers remain the client-server compatibility boundary described by ADR
0006.

## Consequences

- Client and server compile against one generated contract artifact, preventing independent copies from drifting.
- Contract ownership is separate from either runtime and can evolve or be published independently.
- Both consumer builds must include or resolve the schema build, adding a shared build dependency and protobuf code
  generation to the repository.
- Schema dependencies and generated Java APIs become part of both consumers' dependency graphs, including Android.
- Runtime-specific adapters and service implementations remain in their respective client and server builds.

## Alternatives

Keeping protobuf files in the server was not selected because it makes a shared compatibility contract appear to be
server implementation detail and requires special handling for Android consumption.

Duplicating the protobuf files in both builds was not selected because copies can drift while retaining identical
package and type names.

## References

- [ADR 0006: Use Quarkus and gRPC for the server API](0006-use-quarkus-grpc-for-server-api.md)
- [Shared schema build](../../dnd-companion-schema/build.gradle.kts)
- [Shared protobuf API](../../dnd-companion-schema/src/main/proto/companion.proto)
