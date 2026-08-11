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
run/export-preview-screenshot.sh --module :app --tests '*Feature_State_Screenshot*'
```

If Gradle already ran or the script cannot invoke it:

```text
run/export-preview-screenshot.sh --module :app --tests '*Feature_State_Screenshot*' --skip-gradle
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

For reference matching, list concrete deltas: bounding position, padding, line wrapping, type role/weight, color role,
corner shape, crop, icon geometry, and missing/extra elements. Batch related corrections. Re-render before declaring a match.

## Verification boundaries

- A passing screenshot test proves similarity to an approved raster, not usability or accessibility.
- Host-rendered previews do not exercise runtime system bars, IME, TalkBack traversal, gestures, or lifecycle behavior.
- Pixel diffs can change after rendering-tool or font upgrades. Review the report before accepting a new baseline.
- Reference PNGs are gitignored in this repository; the preview source and documented verification commands are the durable
  review artifacts. Exported PNGs can be shared during review without committing generated build output.
