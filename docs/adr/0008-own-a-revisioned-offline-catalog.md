# ADR 0008: Own a revisioned offline catalog

## Status

Proposed

## Date

2026-08-27

## Context

Spellbindr's reference data is currently bundled directly with Android, while ADRs 0006 and 0007 establish a gRPC
server and a shared protobuf compatibility boundary. The application needs server-delivered corrections and additional
redistributable sources without making first launch or ordinary offline use depend on network availability. Stable game
rules identity, publication provenance, and the revision of a particular data snapshot are separate concerns and must
not invalidate managed characters for compatible data corrections.

These concerns cross schema ownership, data generation, server query behavior, Android persistence, and progression
compatibility. They therefore require durable boundaries rather than runtime-specific conventions.

## Decision

Expose the read-only `dnd.companion.api.v1` catalog as domain-oriented gRPC services. Every read resolves a ruleset and
source selection against an immutable snapshot; entity IDs are stable within a ruleset, and each entity records one or
more provenance source IDs. Compatible corrections change collection and snapshot revisions without changing the
ruleset ID. Materially incompatible mechanics require a new ruleset ID.

Own normalized inputs, provenance, deterministic generation, and validation in the neutral `dnd-companion-catalog`
build. The build produces one protobuf snapshot consumed by the server and bundled with Android. `ReferenceData`
remains a snapshot container and is not exposed as a network endpoint.

The server loads an immutable active snapshot and provides snapshot-bound, opaque pagination. Android keeps the bundled
snapshot as its bootstrap and fallback, stages changed collections in a separate catalog database, validates complete
downloads, and atomically activates a new snapshot. Existing domain repository and Flow interfaces remain independent
of whether records came from the bundle or remote synchronization.

Authentication, write operations, arbitrary server-side search/filtering, and user-specific data are outside this
decision.

## Consequences

- Client and server share stable resource messages and generated service stubs, reducing contract drift.
- Ruleset compatibility no longer depends on the current catalog revision, so compatible corrections preserve
  character and favorite references.
- Explicit provenance prevents guessed publication metadata; unresolved legacy records remain visibly attributed to a
  declared legacy bundle.
- Deterministic generation and per-collection hashes allow Android to download only changed collections.
- Snapshot-bound paging and atomic activation prevent clients from observing mixed revisions, at the cost of token,
  staging, validation, and retention logic in both runtimes.
- A neutral catalog build and a separate Android Room database add build and storage complexity.
- Keeping a bundled snapshot increases application size but preserves immediate offline behavior.

## Alternatives

Keeping Android assets as the sole source was not selected because corrections would require an application release and
the server could not provide a reusable catalog.

Treating every catalog correction as a new ruleset was not selected because it would incorrectly make compatible data
maintenance a character-progression incompatibility.

Serving one monolithic `ReferenceData` response was not selected because it prevents collection-level synchronization,
creates large retries, and couples the public API to an internal snapshot container.

Making either Android or the server own generation was not selected because the other runtime would become dependent
on platform-specific implementation details.

## References

- [ADR 0006: Use Quarkus and gRPC for the server API](0006-use-quarkus-grpc-for-server-api.md)
- [ADR 0007: Share protobuf contracts across client and server](0007-share-protobuf-contracts-across-client-and-server.md)
- [Shared catalog contract](../../dnd-companion-schema/src/main/proto/catalog_services.proto)
- [Catalog build](../../dnd-companion-catalog)
