---
name: design-compose-ui
description: >-
  Design, critique, refine, or visually verify Spellbindr Android UI built with Jetpack Compose and Material 3. Use
  for new screens, focused visual polish, screenshot matching or review, image-led composition, visual hierarchy,
  spacing, typography, color, shape, density, motion, accessibility, adaptive layouts, or UI design-system work.
  Do not use for Compose changes limited to state, navigation, business logic, performance, or tests with no visual
  or interaction-design decision.
---

# Design Compose UI

Produce focused, product-specific UI work that preserves behavior and is judged from rendered evidence.

## Load the relevant context

1. Read repository instructions and the target feature's route, screen, state, components, previews, and tests.
2. Read [Spellbindr visual language](references/spellbindr-visual-language.md) for every task.
3. Read [Compose design guidance](references/compose-design-guidance.md) when designing or changing UI.
4. Read [Visual verification](references/visual-verification.md) before planning rendered checks or reviewing an image.
5. Read [ADR 0002](../../../docs/adr/0002-use-route-owned-mvi-dispatch.md) when a change touches feature entry points,
   dispatch, effects, or navigation.
6. Read [Race-carousel art production](references/race-carousel-art-production.md) only when creating, replacing,
   cropping, reviewing, or recording provenance for race artwork.
7. Read [Provenance and maintenance](references/provenance.md) only when updating this skill or importing guidance.

Do not load unrelated references. Treat the running UI and repository source as more authoritative than this skill when
they disagree; call out the mismatch and update the skill if the repository's intentional design language changed.

## Classify the task

- **Critique**: inspect the render first; report strengths and prioritized problems before proposing edits.
- **Focused refinement**: identify the smallest coherent correction and preserve behavior, navigation, state, and
  identity.
- **New UI**: establish the user job, hierarchy, states, visual direction, reuse plan, and verification matrix before
  code.
- **Image-led UI**: make artwork structurally important; keep controls legible without burying the image under cards or
  copy.
- **Reference match**: reproduce the reference state and dimensions, compare renders, and iterate from observed
  differences.

If no render is available, inspect the code but label visual conclusions as hypotheses. Do not claim polish from source
inspection alone.

## Form the design intent

Write a compact internal brief before editing:

- user job and primary action;
- ordered information hierarchy;
- one product-specific visual idea or existing signature to preserve;
- existing theme roles and components to reuse;
- clutter to remove or combine;
- loading, empty, error, disabled, and selected states that matter;
- compact, large-font, and wider-window behavior;
- accessibility contract;
- exact preview and screenshot checks.

Prefer subtraction. Every label, chip, divider, container, instruction, icon, and animation must communicate hierarchy,
state, action, grouping, or product character. Remove it when it does none of those jobs.

## Critique before changing

Evaluate in this order:

1. User job and action clarity.
2. Hierarchy and reading path.
3. Content density and redundant explanation.
4. Composition, spacing rhythm, alignment, and grouping.
5. Typography, color-role pairing, shape, elevation, imagery, and motion.
6. Touch, TalkBack semantics, contrast, font scaling, system insets, and adaptive behavior.
7. Consistency with nearby Spellbindr UI.

Separate findings into:

- **UX problem**: the structure, priority, copy, state, or interaction is wrong.
- **Implementation problem**: the intended design is sound but the Compose code, modifier order, semantics, or sizing is
  wrong.

For each material finding, cite visible or source evidence, state user impact, and propose a bounded correction.
Preserve what already works. Do not turn a focused request into a broad redesign.

## Implement idiomatically

- Keep route-owned navigation and dispatch-based screen APIs intact.
- Prefer existing components and `MaterialTheme` roles. Introduce a reusable component only for a repeated semantic
  pattern.
- Hoist interaction state and callbacks; keep previews deterministic and free of Android runtime dependencies.
- Use semantic Material/Foundation controls before low-level gesture handling. Add custom semantics only when defaults do
  not express the action, role, value, state, or traversal correctly.
- Keep decorative imagery and icons out of the semantics tree; describe meaningful imagery by purpose, not appearance.
- Use scalable text styles and allow content to reflow. Avoid fixed heights around text and actions.
- Make layout decisions from available window space, not device names or orientation checks.
- Use motion to explain state or spatial continuity. Keep it nonessential and provide a reduced-motion path.
- Preserve MVI behavior and existing tests unless the requested UX intentionally changes them.

## Verify from rendered output

1. Add or update a dedicated `@PreviewTest` in `app/src/screenshotTest/kotlin` for the exact state.
2. Include the standard compact render plus only the risk-relevant variants: dark/light, large font, narrow height, wider
   window, RTL, or state variants.
3. Wrap the preview with `ScreenshotHarness`.
4. Generate and export PNGs with the repository commands in [Visual verification](references/visual-verification.md).
5. Inspect the PNG. Compare hierarchy, clipping, alignment, density, contrast, touch affordance, and reference
   differences.
6. Apply one batched correction pass, render again, and re-inspect. Continue only when a concrete defect remains.
7. Run the narrowest screenshot validation, Compose/UI test, compile, and lint checks proportional to the change.

Never update a reference image merely to make a failure pass. Review the actual/reference/diff first and state why the
new rendering is intentional. Screenshot checks complement, but do not replace, semantics tests and human judgment.

## Hand off the result

Report:

- the design outcome and what was deliberately preserved or removed;
- files and behavior changed;
- rendered variants inspected and concrete visual observations;
- deterministic checks run and their results;
- known tradeoffs or remaining human-design decisions.

Do not use vague conclusions such as "cleaner" or "more modern" without naming the observable change.
