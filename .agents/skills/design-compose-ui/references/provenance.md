# Provenance and maintenance

This skill is original repository-local guidance informed by public skills and official Android documentation. It does
not vendor executable code, third-party assets, or substantial copied passages.

Conceptual influences:

- Anthropic `frontend-design` (Apache-2.0): subject-grounded visual direction, restraint, deliberate hierarchy, and
  screenshot-based critique. Web/CSS/DOM guidance was not carried over.
- `wshobson/agents` `mobile-android-design` (MIT): Compose, Material 3, adaptive-layout, preview, and touch-target coverage.
- `mdrmuhaimin/agentic-skills` `mobile-ui-ux-designer` (MIT): user-goal-first briefs, state completeness, accessibility
  contracts, and rendered verification. Its large cross-platform output template was not adopted.
- `aldefy/compose-skill` (MIT, with Apache-2.0 AndroidX source excerpts): progressive Compose references and
  source-grounded implementation checks. No upstream source excerpts are included here.
- `hamen/material-3-skill` (MIT): Compose-oriented Material role and audit concepts. Cross-platform web/Flutter material
  and volatile API claims were excluded.
- `android/skills` and developer.android.com (Apache-2.0/content license as applicable): official open-skill conventions
  and current Android technical guidance.

The repository adopted this skill after evaluating general frontend-design, Android-design, Material 3, and Compose
guidance. The retained text is deliberately repository-specific; discarded research and validation reports are not
part of the operational knowledge surface.

Representative positive activations include “match this screenshot,” “polish the race picker,” “design a new Compose
screen,” and “review spacing, hierarchy, and accessibility.” Representative negative activations include “fix this
ViewModel race,” “change navigation state only,” “optimize recomposition without visual changes,” and “add unit tests
for this use case.”

## Maintenance

Review this skill when any of these change:

- `AppTheme`, color, typography, or shape roles;
- shared UI components or feature-entry architecture;
- AGP screenshot plugin tasks, paths, or `ScreenshotHarness`;
- supported Android window/form-factor policy;
- Compose Material 3, adaptive, or accessibility APIs used by the app.
- the race-carousel asset contract, fallback, manifest, or art-production pipeline.

Prefer updating repository-specific references over importing a broad upstream skill. Re-check upstream sources and
licenses before copying any new material. Pin or record a commit when vendoring content; review instructions and scripts
for network access, shell execution, credential requests, and prompt-injection behavior before installation.
