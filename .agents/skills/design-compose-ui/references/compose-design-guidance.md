# Compose design guidance

## Contents

1. Hierarchy and composition
2. Material roles and product identity
3. Adaptive layout and system UI
4. Accessibility contract
5. Motion and interaction
6. Compose implementation review
7. Sources

## Hierarchy and composition

Start with the user's next decision or action. Make the primary content visually dominant through position, scale,
contrast, or available area. Use no more than one dominant emphasis in a compact viewport.

- Establish a spacing rhythm from repeated values already present nearby. Treat exceptions as design decisions.
- Use alignment and whitespace as the first grouping tools; containers and dividers are secondary tools.
- Keep action labels specific and consistent across control, confirmation, and error states.
- Avoid equal visual weight for title, metadata, instruction, primary action, and secondary action.
- Preserve real content during design. Placeholder copy hides wrapping, density, and empty-state failures.
- Spend visual character in one or two places. Let the rest of the composition support them quietly.

For image-led composition, decide whether the image is content, identity, or decoration. Content and identity imagery need
meaningful space and deliberate cropping. Place legible text over a localized gradient or separate surface; avoid a
large opaque card that reduces the image to a background strip.

## Material roles and product identity

Material 3 is the component and semantic-token foundation, not the complete product identity.

- Pair container and content roles (`primary`/`onPrimary`, `surface`/`onSurface`) instead of selecting colors by eye.
- Use typography roles for meaning. Do not encode hierarchy solely with arbitrary `fontSize` or weight.
- Use theme shapes and tonal surface differences consistently. Add shadow only when separation from a busy layer requires it.
- Prefer explicit product schemes when they are intentional. Dynamic color is an option, not a universal requirement.
- Keep contrast valid in every supported scheme and state, including disabled content and image overlays.

## Adaptive layout and system UI

Design for the current window, which can resize independently of the physical device.

- Branch on window size classes or measured constraints when the composition must change.
- Reflow before shrinking. Allow text to wrap, actions to stack, grids to change columns, and supporting panes to move.
- Constrain readable content width on expansive windows; do not stretch paragraphs and forms edge to edge.
- Keep critical content away from folds, hinges, display cutouts, and occlusion regions.
- Consume `Scaffold` padding and deliberate `WindowInsets`; verify status bar, navigation bar, IME, and gesture navigation.
- Test at least one compact width and every size class whose composition differs. Use a large-font preview when text can
  influence geometry.

## Accessibility contract

Define accessibility as behavior, not a final checklist.

- Prefer Material and Foundation controls because they supply interaction and semantics defaults.
- Keep effective touch targets at least 48 dp even when visual glyphs are smaller.
- Provide an accessible name for interactive icons. Use `contentDescription = null` for purely decorative icons or images.
- Express selection, toggle, range, progress, error, and disabled state through semantics and visible cues; never color alone.
- Merge descendants only when a group should be announced as one action. Clear semantics only when replacing them with a
  complete equivalent.
- Keep traversal order aligned with the visual reading path. Recheck sheets, dialogs, pagers, and custom layouts.
- Use `sp` and scalable Material type. Avoid fixed-height text containers and truncating critical instructions or actions.
- Verify contrast with a tool when values are uncertain. Screenshot inspection cannot prove a ratio.
- Add Compose semantics assertions for critical custom controls; use automated accessibility checks when instrumentation
  infrastructure supports them, then perform TalkBack/manual review for behavior automation cannot judge.

## Motion and interaction

Motion should explain cause, state change, or spatial relationship.

- Prefer one coordinated transition over unrelated animation on every element.
- Keep the task possible with animation disabled or reduced.
- Avoid infinite ambient motion near reading or input unless the user explicitly requested it and it can be paused.
- Preserve input and scroll state across recomposition and configuration changes when users expect continuity.
- Use whole-surface semantics and click handling for rows and cards; avoid competing nested targets without clear need.
- Make loading, pressed, selected, success, and failure feedback distinguishable without relying only on color or motion.

## Compose implementation review

Check implementation details only after the UX and composition are sound:

- modifier order matches intended hit area, clipping, drawing, padding, and semantics;
- state is hoisted to the owning layer and transient UI state uses the appropriate saveability;
- lazy items have stable keys when identity matters;
- preview inputs are stable, representative, and deterministic;
- strings shown to users follow repository localization practice rather than a new ad hoc mechanism;
- custom Canvas or pointer-input code is justified over semantic components;
- adaptive branches preserve the same state and actions;
- tests assert behavior and semantics while screenshots assert pixels.

## Sources

Use current official documentation when an API or recommendation may have changed:

- [Accessibility in Jetpack Compose](https://developer.android.com/develop/ui/compose/accessibility)
- [Compose semantics](https://developer.android.com/develop/ui/compose/accessibility/semantics)
- [Compose accessibility API defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults)
- [Build adaptive apps](https://developer.android.com/develop/ui/compose/build-adaptive-apps)
- [Support different display sizes](https://developer.android.com/develop/adaptive-apps/guides/support-different-display-sizes)
- [Canonical adaptive layouts](https://developer.android.com/develop/adaptive-apps/guides/canonical-layouts)
- [Compose preview screenshot testing](https://developer.android.com/studio/preview/compose-screenshot-testing)
