# Guided Character Creation: Race Carousel and Consolidated Choices

## Objective

Improve guided character creation so selecting a race is a focused, visually engaging decision instead of a long form
that requires scrolling to discover mandatory choices. Move choices derived from race, class, and background into
dedicated later steps that present grants, conflicts, and remaining selections in one place.

The completed flow must:

- let the user select a race and, when applicable, a subrace without scrolling through trait configuration;
- present races in an accessible, horizontally snapping illustrated carousel;
- aggregate skill, tool, armor, weapon, and language proficiency grants and choices from class, race, subrace, and
  background;
- keep uncommon race-trait choices, such as an ability bonus option, ancestry, or racial spell, out of the Race step;
- preserve the source of each fixed grant and selectable choice;
- clear or reconcile dependent selections when an earlier choice changes;
- build the same valid `CharacterSheet` data as the current flow;
- remain usable without race artwork and with large font sizes, screen readers, and reduced motion.

## Current implementation context

Guided setup is implemented entirely in `:app` under:

- `app/src/main/kotlin/com/github/arhor/spellbindr/ui/feature/character/guided/`
- `app/src/test/kotlin/com/github/arhor/spellbindr/ui/feature/character/guided/`
- `app/src/androidTest/kotlin/com/github/arhor/spellbindr/ui/feature/character/guided/`
- `app/src/screenshotTest/kotlin/com/github/arhor/spellbindr/ui/screenshot/`

Important existing behavior:

- `GuidedSetupStepPlanner.kt` currently orders `RACE`, `BACKGROUND`, ability steps, and
  `SKILLS_PROFICIENCIES`.
- `RaceStep.kt` renders a vertical list, then subraces, traits, and every choice embedded in those traits.
- `BackgroundStep.kt` combines background selection with language choices.
- `SkillsStep.kt` only renders `CharacterClass.proficiencyChoices`.
- Choice state is stored in `GuidedSelection.choiceSelections` using stable key prefixes defined by
  `GuidedCharacterSetupViewModel`.
- Changing class, race, or background clears only choices with that source's prefix.
- `GuidedSetupValidation.kt`, `GuidedCharacterSetupScreen.kt`, and `GuidedCharacterSheetBuilder.kt` independently
  inspect the same choices. All three must be updated together to avoid inconsistent navigation, review validation,
  and saved output.
- The repository contains nine races: Dwarf, Elf, Halfling, Human, Dragonborn, Gnome, Half-Elf, Half-Orc, and
  Tiefling. Some have one bundled subrace.
- Compose screenshot testing is available and should be used for fixed-size visual verification.

## Product and UX decisions

Treat the following as implementation requirements unless product review explicitly changes them:

1. The Race step owns race and subrace selection only. A required subrace remains part of identifying the selected
   race, but it must be selectable in the visible carousel card without scrolling below the carousel.
2. The Background step owns background selection only. Its language and equipment choices move to their appropriate
   later steps.
3. Rename `SKILLS_PROFICIENCIES` to `PROFICIENCIES_LANGUAGES`. This step shows:
   - fixed skill, tool, armor, weapon, and language grants;
   - selectable proficiency choices from the class and race traits;
   - selectable language choices from race traits and background;
   - source labels for every grant and choice;
   - duplicate/conflict explanations.
4. Add a conditional `ANCESTRY_CHOICES` step for race-trait choices that are neither proficiency nor language
   choices. This includes ability-bonus, draconic-ancestry, and racial-spell choices. Do not create the step when the
   selected race/subrace has no such unresolved choices.
5. Keep the existing `CLASS_CHOICES` step for subclass and level-one feature choices. Moving those choices is outside
   this change.
6. Keep background equipment selection in the existing `EQUIPMENT` step.
7. Use a finite, horizontally snapping carousel. It may wrap visually only if accessibility behavior remains finite
   and deterministic; do not implement an infinite/cycling pager in the first release.
8. Selecting a page updates `raceId`; advancing remains an explicit action through the persistent Next button.
9. Use two diverse representatives in each illustration. Art should avoid sexualized presentation, fixed gender
   stereotypes, text, logos, and important content near the bottom where the information panel overlays the image.
10. Race artwork is decorative. Race name and mechanics must remain available as text and the screen must have a
    deterministic gradient/placeholder when an asset is missing.

## Target step order

`GuidedSetupStepPlanner.kt` should produce:

1. Basics
2. Class
3. Class choices, only when required
4. Race
5. Background
6. Ability score method
7. Ability score assignment
8. Ancestry choices, only when required
9. Proficiencies & languages
10. Equipment
11. Spells, only when required
12. Review

This order ensures class, race, background, and ability scores are known before derived choices are resolved. Ability
scores precede ancestry choices because race-specific option summaries may show the resulting score changes. If user
testing shows ancestry configuration should immediately follow Race, move it there without changing the choice
classification model.

## Implementation strategy

Deliver this work in independently verifiable increments. Do not begin generating the full artwork set until one
carousel proof has been rendered inside the actual Race step at the supported phone size.

### Task 1: Add a canonical guided-choice classification model

Create an internal model in the guided feature, for example
`internal/GuidedChoiceRequirements.kt`, that derives all active choices from a small `GuidedChoiceContext`. The context
must contain `GuidedSelection` plus the required reference-data collections/maps; it must not depend on
`GuidedCharacterSetupUiState.Content`. This avoids a cycle because the ViewModel needs the derived requirements to
compute conditional steps before it can construct `Content`.

Define immutable UI/domain-adjacent models with stable IDs:

- `GuidedChoiceRequirement`
  - `key`: existing `choiceSelections` key;
  - `source`: class, race trait, subrace trait, or background;
  - `sourceId` and `sourceLabel`;
  - `category`: proficiency, language, ancestry, or equipment;
  - `choice`: the original `Choice`;
  - resolved display options;
  - selected option IDs;
  - disabled options and reasons.
- `GuidedFixedGrant`
  - option ID and display name;
  - category;
  - source ID and source label.
- `GuidedChoiceCategory`
  - `PROFICIENCY`, `LANGUAGE`, `ANCESTRY`, and `EQUIPMENT`.
- `GuidedChoiceContext`
  - current `GuidedSelection`;
  - classes, races, backgrounds, traits, languages, equipment, features, and spells needed for choice resolution;
  - a reference-data version when it is useful for memoization.

Implement pure functions that:

- resolve the active race plus selected subrace trait IDs;
- classify `Trait.proficiencyChoice` and `Trait.languageChoice`;
- classify trait ability bonus, draconic ancestry, and spell choices as ancestry choices;
- classify class `proficiencyChoices`;
- classify background `languageChoice` and `equipmentChoice`;
- collect fixed grants from class proficiencies and `Effect.AddProficienciesEffect` /
  `Effect.AddLanguagesEffect` on race traits and background;
- produce duplicate-selection reasons using source labels;
- return stable ordering: category, source order, then source label.

Do not move choice state out of `GuidedSelection.choiceSelections` in this task. Reuse the existing keys so persisted
in-progress state semantics and the sheet builder remain understandable.

Acceptance criteria:

- one pure function is the source of truth for which choices belong on each later step;
- requirement derivation can run before `GuidedCharacterSetupUiState.Content` is constructed;
- UI, step planning, navigation blocking, and validation no longer need to rediscover choice types independently;
- all requirements have stable keys and human-readable source labels;
- duplicate fixed grants are retained with all sources for explanation rather than silently discarded.

Tests:

- Add focused JVM tests for a class-only proficiency choice.
- Add tests for race fixed grants plus race selectable proficiency and language choices.
- Add tests for background fixed grants plus selectable language and equipment choices.
- Add a test combining class, race, and background with the same skill/language.
- Add tests that selected subrace traits are included and unselected subrace traits are excluded.
- Add classification tests for ability-bonus, draconic-ancestry, and racial-spell choices.

### Task 2: Rework step planning around derived requirements

Update `GuidedStep.kt`:

- replace `SKILLS_PROFICIENCIES` with `PROFICIENCIES_LANGUAGES`;
- add `ANCESTRY_CHOICES`;
- use the user-facing titles “Ancestry choices” and “Proficiencies & languages”.

Update `computeGuidedSetupSteps` to accept the selected race/subrace and the derived choice requirements, or a small
summary produced by Task 1. Add `ANCESTRY_CHOICES` only when active ancestry requirements exist. Always retain
`PROFICIENCIES_LANGUAGES` when fixed grants or selectable requirements exist; if no content is possible, omit it
instead of showing an empty step.

Update step resolution in `GuidedCharacterSetupViewModel` so changing class/race/background while currently on a
removed conditional step resolves to the closest preceding valid step, not always the first step.

Acceptance criteria:

- all target step-order variants are deterministic;
- selecting a race that adds/removes ancestry choices updates the step list without jumping to Basics;
- progress indicator count and Review navigation reflect conditional steps;
- `GoToStep` from Review maps obsolete step destinations to the nearest relevant current step.

Tests:

- Expand `GuidedCharacterSetupViewModelTest` or add `GuidedSetupStepPlannerTest`.
- Cover a non-spellcasting race without ancestry choices.
- Cover High Elf or another race with a racial spell/language choice.
- Cover Dragonborn ancestry choice.
- Cover a level-one spellcasting class and conditional class choices.
- Cover changing race while positioned on `ANCESTRY_CHOICES`.

### Task 3: Make race and background mutations reconcile downstream choices

Replace prefix-only cleanup with a pure reconciliation function that receives the new class/race/subrace/background
selection and the active choice requirements.

The reconciliation function must:

- remove selections whose keys are no longer active;
- preserve selections for still-active requirements;
- remove option IDs that are no longer legal for a preserved requirement;
- remove now-duplicate selections when an earlier fixed grant makes the option unavailable;
- never clear unrelated ability, equipment, or spell selections;
- apply the same reconciliation when subrace changes;
- run after class, race, subrace, or background selection.

Where a duplicate becomes invalid, prefer deterministic cleanup and a non-blocking explanatory UI message on the
later step. Do not leave an invisible invalid selection that only fails on Review.

Acceptance criteria:

- going back and changing race/background/class cannot leave stale selections in the created character;
- choices shared by the old and new configuration are preserved when still valid;
- cleanup logic is pure and unit-tested.

Tests:

- race A → race B removes race A trait choices;
- subrace A → subrace B removes only subrace-dependent choices;
- background change removes old background language/equipment but preserves class choices;
- changing to a fixed proficiency removes the same selectable proficiency from another source;
- changing class preserves valid race/background selections.

### Task 4: Refactor validation and navigation completeness

Update `GuidedSetupValidation.kt` and screen navigation helpers to consume the canonical requirements from Task 1.

Define:

- `isRequirementComplete(requirement, selections)`;
- `firstIncompleteRequirement(category)`;
- category-specific blocking messages that include source and remaining count.

Change completion rules:

- Race requires `raceId` and a valid `subraceId` when the selected race has subraces; it must not require any trait
  choices.
- Background requires only `backgroundId`.
- Ancestry Choices requires all active ancestry requirements.
- Proficiencies & Languages requires all active proficiency and language requirements.
- Equipment requires active equipment requirements.
- Review validation reports each incomplete requirement once and points to its owning step.

Remove duplicated loops from `raceComplete`, `backgroundComplete`, `classProficiencyChoicesComplete`, and corresponding
branches of `stepBlockingReason` after their replacements are covered.

Acceptance criteria:

- Next becomes enabled on Race immediately after race/subrace selection;
- Next becomes enabled on Background immediately after background selection;
- no required choice can be skipped merely because it moved steps;
- each Review error navigates to the step that now owns the missing choice;
- validation and navigation use the same requirement list.

Tests:

- unit-test complete and incomplete states for every category;
- verify Race completeness ignores moved trait choices;
- verify Review routes missing race language/proficiency/ancestry/equipment choices correctly;
- verify a configuration with no choices does not generate false errors.

### Task 5: Build the race carousel without final artwork

Replace the vertical race list in `RaceStep.kt` with composables in a new
`guided/components/race/` package:

- `RaceCarousel`;
- `RaceCarouselPage`;
- `RaceSummaryPanel`;
- `RaceArtwork` with placeholder support;
- `SubraceSelector`, shown inside the visible summary panel when required;
- a compact page indicator.

Use the stable Compose pager API available in the repository's Compose version. Required behavior:

- finite horizontal paging with snap behavior;
- approximately 80–88% page width or side padding that exposes neighboring pages;
- initial page follows `selection.raceId`, or visually displays the first race when no selection exists;
- do not select the initially displayed first race automatically; Next stays disabled until the user taps it or
  deliberately changes pages;
- after user interaction, a newly settled page dispatches `RaceSelected` once, avoiding feedback loops when state
  updates;
- state-driven race changes scroll to the matching page;
- save/restore the selected page across recomposition;
- no vertical list or hidden configuration controls below the carousel;
- summary remains readable on a 360 × 640 dp viewport and with font scale 1.3;
- details open in a modal bottom sheet or dialog and do not alter selection.

The summary panel should show:

- race name and selected subrace;
- size and speed when resolvable;
- concise ability bonuses;
- up to three defining trait names;
- counts such as “2 proficiency choices later”;
- a “View details” action for full trait descriptions.

Extract a pure `RaceSummaryUiModel` mapper. Do not parse human-readable mechanics directly inside composables.

Accessibility:

- expose the race name, position (“3 of 9”), selection state, and concise summary through semantics;
- provide explicit Previous/Next custom actions or buttons for switch-access users;
- decorative artwork has no duplicate content description;
- ensure partially visible pages are not announced as selected;
- respect system motion settings where supported; selection must work without animation;
- all controls meet minimum touch-target sizes.

Acceptance criteria:

- race selection requires no vertical scroll;
- required subrace selection is visible within the selected page;
- carousel behavior is deterministic after Back/Next navigation and configuration changes;
- placeholder artwork produces a polished, usable screen;
- details are available without bloating the summary panel.

Tests:

- JVM tests for `RaceSummaryUiModel` mapping.
- Compose UI tests for swipe selection, tap selection, subrace selection, page indicator, details, and dispatched
  intents.
- Accessibility assertions for selected page semantics and Previous/Next actions.
- A UI test at increased font scale confirming Next and subrace controls remain reachable.

### Task 6: Build the consolidated Proficiencies & Languages step

Replace `SkillsStep.kt` with `ProficienciesLanguagesStep.kt`.

Render content in this order:

1. A compact “Already granted” section grouped by Skills, Tools, Armor & Weapons, and Languages.
2. Required choice cards grouped by their source:
   - Class;
   - Race/Subrace;
   - Background.
3. A completion summary, for example “3 of 4 choices complete”.

Each fixed grant chip/row must state its source on expansion or through supporting text. Each choice card must show:

- source label;
- instruction and remaining selection count;
- selected options;
- disabled duplicate options with the existing source-specific explanation;
- a clear completed state.

If an option is granted by multiple fixed sources, display it once with all sources. If a source offers a choice that
duplicates a fixed grant, keep it disabled and explain why.

Do not merge distinct requirements into one synthetic selection pool; retain individual requirement limits and keys
so D&D rules and the existing builder remain correct.

Acceptance criteria:

- all class, race, subrace, and background proficiency/language choices appear in one step;
- users can distinguish fixed grants from required choices;
- duplicate options cannot be selected;
- every incomplete requirement has an actionable visible control;
- no content from these categories remains in Race or Background.

Tests:

- Compose UI tests with multiple sources and conflicts;
- test all-fixed/no-choice state;
- test multiple simultaneous requirements with independent limits;
- test completed-state rendering and remaining counts;
- screenshot previews for empty, mixed/incomplete, complete, and conflict states.

### Task 7: Build the conditional Ancestry Choices step

Create `AncestryChoicesStep.kt` using requirements classified as `ANCESTRY`.

Render separate cards for:

- ability score choice;
- draconic ancestry;
- racial spell/cantrip;
- any future race-trait choice not classified as proficiency, language, or equipment.

Use the source trait name and its short description. Resolve spells lazily as today, but update
`shouldLoadSpells` so visiting `ANCESTRY_CHOICES`, or having an active racial-spell requirement, loads spells. Do not
tie spell loading to the Race step after this refactor.

Acceptance criteria:

- Race remains selection-only for every bundled race;
- the conditional step appears only when necessary;
- all bundled race choice types remain configurable and saved;
- spell asset loading still occurs before a racial spell selector is needed.

Tests:

- Dragonborn ancestry selection;
- High Elf racial cantrip selection;
- Half-Elf ability/proficiency choice split across the correct steps;
- conditional absence for a race without ancestry requirements;
- spell-loading trigger tests.

### Task 8: Move background equipment and verify sheet building

Ensure `EquipmentStep.kt` renders background equipment requirements from the canonical model. Remove language-choice UI
from `BackgroundStep.kt` and any equipment-specific assumptions that bypass the requirement model.

Update `GuidedCharacterSheetBuilder.kt` only where necessary. It should continue reading stable keys, but centralize
iteration through active requirements when that prevents the builder from drifting from validation.

Add builder regression tests covering:

- fixed and selected proficiencies from all sources;
- fixed and selected languages from all sources;
- selected background equipment;
- selected ancestry and racial spell choices represented in features/traits text;
- no stale selections after changing upstream choices;
- identical output for an existing valid configuration before and after the UI reorganization.

Acceptance criteria:

- no character-sheet field regresses;
- moved choices appear exactly once in the saved sheet;
- inactive/stale choice keys never affect output.

### Task 9: Establish the race artwork pipeline with one proof asset

Before producing nine images, define and record an art brief under
`docs/art/race-carousel-art-direction.md` containing:

- target aspect ratio and pixel dimensions based on the screenshot-tested carousel page;
- crop-safe zone, especially the bottom overlay region;
- palette, lighting, rendering style, background complexity, and camera distance;
- representation guidelines;
- filename convention: `app/src/main/assets/images/races/<race-id>.webp`;
- required license/provenance metadata and generation prompt archive;
- compression and maximum file-size target;
- placeholder/fallback policy.

Generate one Human or Elf proof image with two diverse representatives. Convert it to WebP using a deterministic
quality setting, add it to the application, and render it in light and dark screenshot previews. Evaluate:

- focal points remain visible behind the summary panel;
- text contrast meets accessibility expectations;
- the composition survives center-crop at supported phone aspect ratios;
- visual style fits Spellbindr's Material theme;
- APK size impact is acceptable.

Do not generate the remaining images until this proof is approved.

Acceptance criteria:

- the art direction can be repeated consistently;
- provenance is recorded;
- the proof works in the real carousel rather than a detached mockup;
- placeholder behavior remains testable.

### Task 10: Produce and integrate the complete artwork set

After proof approval, create one consistent asset for each current race ID:

- `dwarf`
- `elf`
- `halfling`
- `human`
- `dragonborn`
- `gnome`
- `half-elf`
- `half-orc`
- `tiefling`

For each asset:

- follow the approved art brief and shared prompt/style seed strategy;
- visually inspect hands, faces, weapons, anatomy, cropping, stereotypes, and accidental text/logos;
- convert and compress using the same pipeline as the proof;
- record provenance and final dimensions/file size;
- verify the ID-to-asset mapping;
- render a screenshot at the carousel crop, not only the raw image.

Add an asset-manifest unit test that verifies every race ID maps to either an existing image or an explicitly approved
fallback. Do not make a missing image crash asset bootstrap or guided setup.

Acceptance criteria:

- all nine races have a consistent visual language;
- every image remains legible under the information panel;
- total size stays within the budget established by the proof;
- missing/corrupt artwork degrades to the placeholder.

### Task 11: Add screenshot coverage and perform visual iteration

Add dedicated `@PreviewTest` previews using `ScreenshotHarness` for:

- Race carousel with placeholder, no selection;
- Race carousel with the first page visible but Next disabled;
- Human/Elf proof artwork with summary panel;
- race with required subrace;
- long race/trait text;
- light and dark themes;
- 360 × 640 dp and one larger representative viewport;
- Proficiencies & Languages with conflicts;
- Ancestry Choices with racial spell selection.

Use the repository workflow:

```shell
./gradlew :app:updateDebugScreenshotTest --tests '*RaceCarousel*'
run/export-preview-screenshot.sh --module :app --tests '*RaceCarousel*'
```

Inspect exported images after each meaningful UI change. Check:

- carousel page width and neighbor visibility;
- image crop and overlay height;
- contrast and typography;
- persistent navigation visibility;
- subrace control visibility;
- long text truncation;
- light/dark consistency.

Reference images are gitignored in the current repository, so exported screenshots are review artifacts rather than CI
goldens unless the project separately changes that policy. Changing the global screenshot policy is out of scope.

### Task 12: Complete end-to-end regression and accessibility verification

Add or update instrumentation tests for these user journeys:

1. Select race in the carousel, select subrace, and continue without scrolling.
2. Select class, race, and background that all contribute proficiencies; resolve them on the consolidated step.
3. Go back, change race, return forward, and verify stale choices are removed while valid choices remain.
4. Complete a race with ancestry and racial spell choices.
5. Complete a character with background language and equipment choices.
6. Navigate from Review validation errors to the correct owning step.
7. Recreate the current Standard Array guided path to guard against unrelated regression.

Run:

```shell
./gradlew lintDebug test testDebugUnitTest --stacktrace
./gradlew connectedDebugAndroidTest
./gradlew :app:validateDebugScreenshotTest
./gradlew assembleDebug
```

`connectedDebugAndroidTest` requires a device or emulator. Configure SDK 37 through `local.properties` or
`ANDROID_HOME` before running Gradle.

Manual exploratory checks:

- smallest supported phone viewport;
- large font and display size;
- TalkBack traversal and announcements;
- switch access or keyboard Previous/Next actions;
- rapid swiping followed immediately by Next;
- process recreation on Race and later choice steps;
- missing artwork;
- light/dark theme change while on the carousel;
- all nine races and all bundled subraces.

## Delivery sequence

Use these reviewable increments:

1. Canonical choice model, step planning, reconciliation, validation, and JVM tests.
2. Placeholder-based race carousel plus Compose UI and screenshot tests.
3. Consolidated Proficiencies & Languages and Ancestry Choices steps.
4. Builder regression coverage and complete end-to-end tests.
5. One proof race artwork asset and in-app visual review.
6. Remaining artwork and final visual/accessibility polish.

Do not combine the full artwork generation with the state-management refactor in one change. The behavioral refactor
must be reviewable and testable without binary assets.

## Definition of done

- Race and Background steps contain selection controls only.
- Race/subrace selection is possible without vertical scrolling.
- The carousel is finite, snapping, state-driven, and accessible.
- Proficiency and language grants/choices from class, race, subrace, and background are visible in one step with
  source attribution.
- Other race-trait choices are handled by a conditional Ancestry Choices step.
- Background equipment remains in Equipment.
- Upstream changes reconcile downstream selections without stale data or unnecessary loss.
- Navigation blocking, Review validation, and sheet building share the same canonical requirements.
- Unit, Compose UI, instrumentation, and screenshot tests cover the new behavior.
- All Gradle checks pass with SDK 37 configured.
- The proof artwork is approved in the real layout before batch generation.
- Every final race image has consistent art direction, provenance, compression, fallback behavior, and verified crop.
- No unrelated compendium, persistence, dependency, or project-structure work is included.

## Explicitly out of scope

- Changes to D&D rules or bundled SRD race definitions.
- Adding new races or subraces.
- Reworking class selection, ability assignment, spells, or character-sheet UI beyond integration required here.
- Persisting partially completed guided setup across app restarts.
- Replacing the existing `choiceSelections` storage representation.
- An infinite carousel.
- Global screenshot-reference or CI policy changes.
- Room migration, backup configuration, compendium expansion, or other repository roadmap work.
