# ADR 0005: Treat race-carousel artwork as optional decorative data

## Status

Accepted

## Date

2026-08-13

## Context

Illustrated race selection improves atmosphere and recognition, but bundled artwork can be absent, fail to decode, or
crop differently across windows. Character creation and accessibility cannot depend on image availability.

## Decision

Associate at most one decorative image with each stable base-race data ID. Store it as
`app/src/main/assets/images/races/<race-id>.webp`; subraces do not receive separate assets. Keep race name, mechanics,
selection state, subrace control, page position, and actions available as text and semantics. Exclude decorative images
from accessibility semantics.

Use a deterministic code-drawn fallback for missing, unreadable, or unsupported assets. Never substitute another
race's image, surface a blocking error, or prevent race/subrace selection and progression. Verify both the asset and
fallback in representative screenshot previews. Preserve exact prompts and manifest metadata as provenance.

## Consequences

Artwork can evolve without becoming application data or a functional dependency. The UI must maintain a complete text
contract and deterministic fallback, and asset review must include crop, readability, representation, size, and
provenance checks.

## Alternatives

- Make images required reference data: rejected because asset failure would block character creation.
- Add one image per subrace: rejected because base-race identity is sufficient and stable.
- Announce descriptive artwork to accessibility services: rejected because the mechanics and choice are textual and
  the illustrations are decorative.

## References

- `.agents/skills/design-compose-ui/references/race-carousel-art-production.md`
- `docs/art/race-carousel-art-manifest.json`
- `docs/art/prompts/`
