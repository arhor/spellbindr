# Visual verification

## Choose evidence by risk

| Risk | Render or check |
|---|---|
| Hierarchy, spacing, shape, imagery | Screenshot preview at the target state and size |
| Theme roles and contrast | Light and dark renders; calculate uncertain contrast ratios |
| Wrapping and action reachability | `fontScale` preview, usually 1.3 or higher when risk warrants |
| Compact-height overflow or IME | Short-height preview plus device/instrumentation check for runtime insets |
| Adaptive composition | Explicit compact and wider-window previews around actual layout transitions |
| RTL or directional controls | RTL preview and semantic review |
| Interaction and state | Compose UI/instrumentation test; screenshots for key stable states |
| Visual regression | `validateDebugScreenshotTest` against reviewed reference images |

Do not multiply previews mechanically. Select variants that could falsify the design.

## Create the preview

Place dedicated exported previews under `app/src/screenshotTest/kotlin` near the owning feature. Each preview requires:

```kotlin
@PreviewTest
@Preview(widthDp = 360, heightDp = 640)
@Composable
fun Feature_State_Screenshot() {
    ScreenshotHarness {
        FeatureScreen(...)
    }
}
```

Use the real `ScreenshotHarness`, theme, state model, and representative content. Keep callbacks deterministic. Add
`uiMode`, `fontScale`, locale, or a different width/height only when it exercises a design risk.

## Generate and export

Generate a filtered reference:

```text
./gradlew :app:updateDebugScreenshotTest --tests '*Feature_State_Screenshot*'
```

Export the newly generated PNGs:

```text
.agents/skills/design-compose-ui/scripts/export-preview-screenshot.sh \
  --module :app --tests '*Feature_State_Screenshot*'
```

If Gradle already ran or the script cannot invoke it:

```text
.agents/skills/design-compose-ui/scripts/export-preview-screenshot.sh \
  --module :app --tests '*Feature_State_Screenshot*' --skip-gradle
```

Validate reviewed baselines:

```text
./gradlew :app:validateDebugScreenshotTest --tests '*Feature_State_Screenshot*'
```

The HTML diff report is under `app/build/reports/screenshotTest/preview/debug/`.

## Inspect and compare

Review the full image before zooming into details:

1. Is the user job and primary action obvious at first glance?
2. Does the reading path match importance?
3. Is the density intentional, with redundant copy and containers removed?
4. Are groups expressed consistently through alignment, spacing, surface, and type?
5. Do artwork, text, and controls compete or cooperate?
6. Are content and actions clipped, crowded, or unreachable in risk variants?
7. Do disabled, selected, loading, empty, and error states remain understandable?

## Match a supplied image

Treat these as separate artifacts:

- **Target**: the image, screenshot, or mockup supplied by the user. This is the visual goal, not an AGP baseline.
- **Render**: the PNG generated from the current Compose `@PreviewTest` and exported by the repository script.
- **AGP baseline**: the generated PNG under `src/screenshotTestDebug/reference`; validation compares future renders to
  this file, not to the user's target.

Use this loop whenever implementing from a supplied image:

1. Inspect the target before editing. Identify the represented state, content, viewport or aspect ratio, theme, system
   chrome, and any ambiguity that affects implementation.
2. Match the preview's logical viewport, content, theme, and state as closely as the target permits. Compare layout in
   logical dimensions; do not require identical raw pixel dimensions across different renderers or display densities.
3. Exclude status bars, navigation bars, device frames, and other capture chrome unless they are product UI or the user
   explicitly asks to reproduce them.
4. Export the render and inspect target and render at full-image scale first. Then compare concrete deltas in this
   order: composition and bounds; alignment and spacing; text wrapping, role, size, and weight; color and contrast;
   shape and elevation; imagery and crop; icon geometry; missing or extra elements; clipping and touch affordance.
5. Record the material deltas, group related corrections into one pass, implement them, rerender, and compare again.
6. Repeat until no material mismatch remains. If closer matching would violate Spellbindr's design roles, accessibility,
   route-owned MVI behavior, or the requested behavior, preserve the constraint and report the remaining deviation.

Do not declare a match from source inspection or a successful Gradle task alone. Do not introduce a numeric similarity
threshold for targets captured with a different renderer, density, font rasterizer, crop, or system chrome.

## Verification boundaries

- A passing screenshot test proves similarity to its AGP baseline, not to a user-supplied target and not usability or
  accessibility.
- Host-rendered previews do not exercise runtime system bars, IME, TalkBack traversal, gestures, or lifecycle behavior.
- Pixel diffs can change after rendering-tool or font upgrades. Review the report before accepting a new baseline.
- Reference PNGs are gitignored in this repository; the preview source and documented verification commands are the
  durable review artifacts. Exported PNGs can be shared during review without committing generated build output.
