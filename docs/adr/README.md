# Architecture Decision Records

ADRs record durable architecture and product-engineering decisions and their rationale. Read only the entries relevant
to the work. Use [the template](template.md) for new records and the `recording-architecture-decisions` skill when
creating or changing ADR lifecycle state.

Canonical statuses are `Proposed`, `Accepted`, `Rejected`, `Deprecated`, and `Superseded by ADR NNNN`. Preserve an
ADR after its decision reaches any terminal status; record a changed accepted decision in a new ADR and link both
records explicitly.

| ID                                                            | Decision                                                         | Status   | Date       |
|---------------------------------------------------------------|------------------------------------------------------------------|----------|------------|
| [0001](0001-separate-repository-knowledge-by-purpose.md)      | Separate repository knowledge by purpose                         | Accepted | 2026-08-13 |
| [0002](0002-use-route-owned-mvi-dispatch.md)                  | Use route-owned MVI dispatch at Compose feature entry points     | Accepted | 2026-08-13 |
| [0003](0003-separate-managed-progression-from-sheet-state.md) | Persist managed progression separately from mutable sheet state  | Accepted | 2026-08-13 |
| [0004](0004-centralize-guided-character-choices.md)           | Centralize guided-character choice derivation and step ownership | Accepted | 2026-08-13 |
| [0005](0005-treat-race-artwork-as-optional-decoration.md)     | Treat race-carousel artwork as optional decorative data          | Accepted | 2026-08-13 |
| [0006](0006-use-quarkus-grpc-for-server-api.md)               | Use Quarkus and gRPC for the server API                           | Accepted | 2026-08-26 |
