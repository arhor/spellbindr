# Compose UI design skill validation

Date: 2026-08-11

## Method

The `$design-compose-ui` harness was walked through against repository-native artifacts without committing experimental
product changes. Each scenario records the context the skill routes to, the expected actionable output, and whether that
behavior is represented in the skill. Existing PNGs were visually inspected for the critique scenario. Source, preview,
and test artifacts were used where a live interaction or new product requirement was intentionally absent.

This is a scenario/dry-run evaluation, not evidence that prompt text alone guarantees design quality. Deterministic skill
validation and repository checks are recorded at the end.

## 1. Existing-screen critique

**Prompt shape:** “Critique the character-sheet Spells tab and prioritize focused improvements.”

**Artifacts:**

- `SpellsTabScreenshotPreviews.kt`
- existing light screenshot `SpellsTab_Screenshot_8953c3c2_0.png`
- spell-tab components under `ui/feature/character/sheet/components/tabs/spells/`

**Observed harness behavior:** It classifies the task as critique, loads the product language and visual-verification
reference, requires the render before conclusions, and separates UX from implementation.

**Sample output:**

1. P1 UX: spell-slot controls consume most of the first viewport through repeated nested surfaces and repeated Use/Restore
   controls, delaying the spell list and “Add spells” action. First test a more compact slot composition while preserving
   per-level state and 48 dp actions.
2. P2 UX: the Level 4 `0/0` surface carries the same container emphasis as actionable levels despite offering no action.
   Reduce or omit that inactive group if the rules/product requirement allows it.
3. Preserve: the tab hierarchy, gold/violet palette, clear used/available slot distinction, disabled-state visibility,
   class grouping, and direct cast affordances already fit the product.
4. Implementation follow-up: inspect modifier hit areas and semantics before changing pixels; do not infer accessibility
   failure from the screenshot alone.

**Result:** Pass. The output is prioritized, evidence-based, and bounded; it does not propose a wholesale redesign.

## 2. Focused refinement

**Prompt shape:** “Refine the guided race selection without changing navigation, selection behavior, or state.”

**Artifacts:** `RaceCarousel.kt`, `RaceCarouselPage.kt`, `RaceSummaryPanel.kt`, `RaceArtwork.kt`, and
`RaceCarouselScreenshotPreviews.kt`.

**Observed harness behavior:** It requires behavior and component inspection before edits, names state/navigation as
preservation boundaries, identifies image-led precedent, and selects only risk-relevant preview variants.

**Refinement plan produced:**

- preserve pager selection, subrace selection, details dialog, custom TalkBack previous/next actions, selected state,
  reduced-motion external page changes, and artwork mapping;
- critique only overlay density, summary hierarchy, scrim balance, arrow prominence, and pagination legibility;
- verify selected and unselected states at 360×640 plus the existing 1.3 font-scale preview;
- change a shared component only if the pattern repeats outside race selection.

**Result:** Pass. The harness prevents visual polish from leaking into MVI or pager behavior.

## 3. New-screen design

**Prompt shape:** “Add a feat-details screen that fits Spellbindr.”

**Context route:** Theme and nearby compendium details/list screens, shared chrome, MVI dispatch contract, Compose guidance,
and screenshot verification.

**Compact brief produced:**

- user job: understand a feat's benefits and prerequisites, then return to the prior compendium context;
- hierarchy: feat name → eligibility/prerequisites → mechanical effects → rules description/source;
- signature: restrained fantasy-rulebook treatment using the existing gold hierarchy and shape language, not a generic
  hero-gradient/card dashboard;
- reuse: `AppTopBar`, theme roles, existing compendium detail spacing and loading/error patterns;
- states: loading, missing feat/error, long prerequisite list, long rules text;
- adaptive behavior: readable constrained column on wide windows, reflowing metadata at large font, system inset handling;
- verification: standard compact, 1.3 font scale with long content, and dark theme.

**Result:** Pass as a design/implementation plan. Limitation: final hierarchy and copy require an actual feature contract;
the skill correctly does not invent actions or data.

## 4. Image-led composition

**Prompt shape:** “Design race selection around the artwork instead of putting it behind a card.”

**Artifact:** The existing `RaceCarouselPage` provides a real solution: full-size `RaceArtwork`, a localized vertical
gradient scrim, and bottom-aligned summary/actions.

**Observed harness behavior:** The skill identifies imagery as identity/content, protects crop and visual area, asks whether
overlay elements communicate state/action, and explicitly rejects burying art under a large opaque card.

**Result:** Pass. The local product reference gives a concrete repository precedent instead of generic image-card advice.

## 5. Adaptive and accessible UI

**Prompt shape:** “Make this selection UI work with large text, TalkBack, and wider windows.”

**Evidence:**

- `RaceCarousel_NoSelectionLargeFont_Screenshot` uses `fontScale = 1.3f`;
- `RaceCarouselPage` exposes radio-button role, selected state, position, summary, and custom previous/next actions;
- `RaceCarouselPagination` provides a page-position description;
- Compose guidance covers 48 dp targets, semantic defaults, reflow, window-based decisions, insets, contrast, and
  instrumentation limits.

**Expected check plan:** compact default + compact large font + a width around the actual layout transition; semantics
assertions for role/state/custom actions; device review for TalkBack traversal and runtime insets if implementation changes.

**Result:** Pass. The harness does not treat a screenshot as proof of TalkBack behavior and avoids device-name/orientation
breakpoints.

## 6. Visual verification

**Prompt shape:** “Match a supplied screenshot and show the result.”

**Observed harness behavior:** It requires an exact-state `@PreviewTest`, `ScreenshotHarness`, explicit size when relevant,
filtered generation/export, direct PNG inspection, concrete delta reporting, a batched correction pass, and validation
against reviewed baselines.

**Repository commands selected:**

```text
./gradlew :app:updateDebugScreenshotTest --tests '*Target_Screenshot*'
run/export-preview-screenshot.sh --module :app --tests '*Target_Screenshot*'
./gradlew :app:validateDebugScreenshotTest --tests '*Target_Screenshot*'
```

**Result:** Pass. The workflow matches the repository's configured AGP screenshot source set and calls out host-preview
limits. It forbids blindly accepting changed references.

## Activation tests

Should activate:

- “Critique this Compose screen screenshot.”
- “Polish the hierarchy and spacing without changing behavior.”
- “Build a new character inventory screen that fits the current theme.”
- “Make the race artwork the focus.”
- “Match this reference PNG and verify it at large font.”

Should not activate:

- “Fix this `LaunchedEffect` cancellation bug.”
- “Change the navigation route argument type.”
- “Optimize LazyColumn recomposition.”
- “Add unit tests for the progression engine.”

The negative boundary is included in frontmatter, where activation decisions are made, rather than only in the loaded body.

## Deterministic validation

Completed on 2026-08-11:

- skill-creator `quick_validate.py .codex/skills/design-compose-ui`: **passed** (`Skill is valid!`);
- initializer TODO scan: **passed**, no placeholders found;
- `git diff --check`: **passed**;
- `./gradlew :app:validateDebugScreenshotTest --tests '*SpellsTab_Screenshot*'`: **passed** in 18 seconds, exercising
  the configured preview renderer and existing reference PNGs.

The Gradle run emitted existing Kotlin/compiler warnings but no screenshot failure. The implementation makes
documentation and skill-only changes, so no product behavior or screenshot baseline changes are expected.

## Remaining human review

This validation demonstrates routing, specificity, critique, preservation, implementation guidance, accessibility/adaptive
coverage, and rendered-output workflow. It does not demonstrate that an agent can independently make every subjective
choice well. High-impact visual direction, cultural tone, readability, accessibility with assistive technology, and final
baseline approval still require human review.
