# Spellbindr visual language

Use this repository map before designing. Inspect the current files because this reference records a baseline, not an
immutable specification.

## Product character

Spellbindr is a compact D&D 5e companion. Its established identity is warm parchment and gold in light mode, deep
blue-black surfaces and muted gold in dark mode, with restrained violet and ember accents. Rounded surfaces, occasional
fantasy geometry, and character artwork provide personality. Dense rules data should remain scannable and operational;
fantasy atmosphere must not obstruct play.

Preserve these traits:

- warm gold as the primary emphasis, not a generic purple-gradient treatment;
- violet as a supporting selection or secondary role and ember for limited tertiary emphasis;
- high-radius Material surfaces plus purposeful custom shapes where they encode game character;
- typography hierarchy built from `MaterialTheme.typography`, with semibold display/headline roles and readable body text;
- artwork as a focal layer where the content warrants it, especially guided character creation;
- concise interface copy and visible game state rather than explanatory cards around every control.

## Source map

| Concern | Source of truth |
|---|---|
| Theme entry | `app/src/main/kotlin/com/github/arhor/spellbindr/ui/theme/AppTheme.kt` |
| Color roles | `app/src/main/kotlin/com/github/arhor/spellbindr/ui/theme/AppColorScheme.kt` |
| Type roles | `app/src/main/kotlin/com/github/arhor/spellbindr/ui/theme/AppTypography.kt` |
| Shape roles | `app/src/main/kotlin/com/github/arhor/spellbindr/ui/theme/AppShapes.kt` |
| Shared chrome and primitives | `app/src/main/kotlin/com/github/arhor/spellbindr/ui/components/` |
| Feature UI | `app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/` |
| Image-led precedent | `ui/feature/character/guided/components/race/` |
| Screenshot previews | `app/src/screenshotTest/kotlin/com/github/arhor/spellbindr/ui/` |
| Screenshot wrapper | `app/src/screenshotTest/kotlin/com/github/arhor/spellbindr/ui/screenshot/ScreenshotHarness.kt` |
| Export helper | `run/export-preview-screenshot.sh` |
| MVI entry contract | `docs/mvi-dispatch-contract.md` |

## Reuse before invention

Search shared components and the target feature before introducing another card, row, picker, top bar, bottom bar,
loading state, error state, or selected indicator. Reuse means preserving semantics and visual rhythm, not forcing every
new concept into an unrelated component.

Examples worth inspecting include `AppTopBar`, `AppBottomBar`, `SelectableGrid`, `SelectedIndicator`, `InlineTextField`,
`InlineNumberField`, `LoadingIndicator`, and `ErrorMessage`. Feature-local patterns are intentionally local when their
meaning is specific to dice, spells, the character sheet, guided creation, or level-up.

## Current layout and accessibility evidence

The repository already uses:

- `@PreviewLightDark` and explicit screenshot preview sizes;
- `fontScale` screenshot variants;
- Material/Foundation semantics plus custom `stateDescription`, `customActions`, and `Role` where necessary;
- `minimumInteractiveComponentSize()` for compact custom controls;
- reduced-motion behavior for externally driven pager changes;
- full-bleed race artwork with a gradient readability scrim and concise overlay content.

Extend those patterns rather than resetting the product to baseline Material samples.

## Design boundaries

- Do not replace the static brand schemes with dynamic wallpaper color without an explicit product decision.
- Do not hardcode a new palette inside feature composables. Add or use semantic theme roles.
- Do not use nested opaque cards merely to group every heading and value.
- Do not add chips for static metadata when aligned text, type hierarchy, or spacing communicates the same relation.
- Do not hide primary play actions behind decorative motion or imagery.
- Do not alter Route/Intent/Effect ownership to simplify a visual implementation.
