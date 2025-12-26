# Architecture notes

- Domain code must stay independent of data implementation details. Specifically, domain packages must **not** import
  anything from `data.model` or the asset loaders under `data.local.assets`; ArchUnit guards this boundary.
- Mapping between data and domain types should live in `app/src/main/kotlin/com/github/arhor/spellbindr/data/mapper`
  and be exercised through mapper unit tests to keep conversions from drifting into other layers.
