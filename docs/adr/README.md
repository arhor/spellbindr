# Architecture Decision Records

ADRs record durable architecture and product-engineering decisions and their rationale. Read only the entries relevant
to the work. Use [the template](template.md) for new records and the repository
`record-architecture-decision` skill when creating or changing ADR lifecycle state.

Statuses are `Proposed`, `Accepted`, `Superseded`, and `Deprecated`. An accepted ADR is an historical record: correct
minor errors or links in place, but record a changed decision in a new ADR and link both records explicitly.

| ID                                                            | Title                                                            | Status   | Affected areas                               |
|---------------------------------------------------------------|------------------------------------------------------------------|----------|----------------------------------------------|
| [0001](0001-separate-repository-knowledge-by-purpose.md)      | Separate repository knowledge by purpose                         | Accepted | Agent guidance, documentation, configuration |
| [0002](0002-use-route-owned-mvi-dispatch.md)                  | Use route-owned MVI dispatch at Compose feature entry points     | Accepted | Compose features, navigation, ViewModels     |
| [0003](0003-separate-managed-progression-from-sheet-state.md) | Persist managed progression separately from mutable sheet state  | Accepted | Character persistence, level-up, editor      |
| [0004](0004-centralize-guided-character-choices.md)           | Centralize guided-character choice derivation and step ownership | Accepted | Guided creation, validation, materialization |
| [0005](0005-treat-race-artwork-as-optional-decoration.md)     | Treat race-carousel artwork as optional decorative data          | Accepted | Guided race selection, assets, accessibility |
