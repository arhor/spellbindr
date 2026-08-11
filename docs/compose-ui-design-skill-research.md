# Android and Compose UI design skill research

Date: 2026-08-11
Issue: [#150](https://github.com/arhor/spellbindr/issues/150)

## Decision

Use the repository-local `$design-compose-ui` skill in `.codex/skills/design-compose-ui` as a layered harness:

1. a short critique/design/implementation/verification workflow;
2. a required Spellbindr visual-language reference;
3. focused Compose, accessibility, adaptive-layout, and screenshot-verification references;
4. a provenance and maintenance record.

Do not install any reviewed public skill unchanged. The strongest general-design candidates remain web-oriented or too
broad, while the Android-aware candidates emphasize API correctness and baseline Material patterns more than design
judgment, product identity, critique, and rendered iteration. Combining whole upstream skills would duplicate guidance,
increase activation/context cost, and introduce conflicting assumptions. The local harness instead uses original,
repository-specific instructions informed by their strongest concepts and current official Android guidance.

## Repository needs

The repository inspection covered the theme, shared components, representative feature screens, image-led race carousel,
previews, screenshot source set, export helper, and MVI contract.

Spellbindr already has a recognizable language: parchment/gold and blue-black/gold schemes, violet and ember supporting
roles, rounded and occasional fantasy-geometric shapes, semibold hierarchy, dense game data, and character artwork. It
also has useful implementation precedents: `MaterialTheme` roles, feature-local components, explicit semantics, 48 dp
touch support, reduced-motion pager behavior, light/dark previews, font-scale previews, and AGP screenshot tests.

The missing capability was therefore not “teach Compose from zero.” It was a reliable workflow that makes an agent:

- read and preserve the existing identity before proposing a visual direction;
- critique hierarchy and clutter before editing;
- distinguish UX structure from Compose implementation defects;
- prefer focused subtraction over container/chip/copy accumulation;
- preserve route/screen behavior and MVI ownership;
- render the exact state, inspect it, and iterate from evidence.

## Sources and candidate inventory

Sources were reviewed at their default branches on 2026-08-11. Repository counts are volatile, weak discovery signals;
they are not treated as proof that a skill regularly improves real tasks.

### Serious candidates

| Candidate | Concrete behavior | Evidence and activity | Limitation for Spellbindr | License |
|---|---|---|---|---|
| [Anthropic `frontend-design`](https://github.com/anthropics/skills/blob/main/skills/frontend-design/SKILL.md) | Grounds a visual direction in subject matter; plans palette/type/layout/signature; critiques generic defaults; encourages restraint and screenshots | High-visibility upstream with recent repository activity. Community reports are mixed: [positive experience](https://www.reddit.com/r/ClaudeAI/comments/1oxn1gj/frontenddesign_skill_is_so_amazing/), [an eval-driven rewrite](https://www.reddit.com/r/ClaudeAI/comments/1q7rnpk/i_rewrote_anthropics_frontenddesign_skill_and/), and [reported disappointment](https://www.reddit.com/r/ClaudeCode/comments/1q9mkx9/anyone_else_struggling_with_the_official/) | Web hero/page framing, font-pairing expectations, CSS concerns, hover/focus language, and “aesthetic risk” can conflict with a compact native product and an established theme | Apache-2.0 in its bundled `LICENSE.txt` |
| [`mobile-android-design`](https://github.com/wshobson/agents/tree/main/plugins/ui-design/skills/mobile-android-design) | Supplies Material 3/Compose examples, adaptive layout, theme roles, state hoisting, previews, accessibility, and 48 dp targets | Large, actively maintained parent collection; registry install count is only a popularity signal | Mostly baseline component recipes. Its generic card example is exactly the template tendency this issue wants to resist; little critique or rendered iteration | MIT |
| [`mobile-ui-ux-designer`](https://github.com/mdrmuhaimin/agentic-skills/blob/main/codex/mobile-ui-ux-designer/skill.md) | User-goal-first brief, hierarchy, state model, responsive plan, platform conventions, accessibility contract, tokens, and rendered-verification gates | Concrete and comprehensive, but the repository showed only an initial commit and one skill-add commit; independent usage evidence was not found | Roughly 43 KB/700+ lines, cross-platform, template-heavy output, placeholders, and web/iOS/Flutter branching create high context cost | MIT stated in skill |
| [`compose-expert`](https://github.com/aldefy/compose-skill) | Progressive references for state, modifiers, performance, animation, navigation, design-to-code, and source-backed API checks | Recent claim-verification commits, releases/install paths, and hundreds of public stars; a [developer discussion](https://www.reddit.com/r/androiddev/comments/1rripih/i_built_an_agent_skill_that_gives_ai_tools/) provides interest, not independent outcome proof | Strong engineering harness, but broad triggers and a large reference surface; visual taste, product-specific critique, and screenshot iteration are secondary | MIT; vendored AndroidX excerpts Apache-2.0 |
| [`material-3`](https://github.com/hamen/material-3-skill) | Material token/component reference, Compose mappings, adaptive layout, and a 10-category audit | Recent packaging fixes and versioned releases show maintenance; limited independent real-world evidence | Mixes Compose, Flutter, and substantial web content; compliance scoring can reward generic Material conformity over product identity; volatile Expressive/API claims need current verification | MIT |
| [Google `android/skills`](https://github.com/android/skills) | Official, narrowly scoped Android workflows grounded in developer.android.com and the open Agent Skills format | Frequent releases/updates and official ownership; the repository explicitly says it does not prioritize basic Compose practices | No general visual-design/critique skill matching this issue. Current skills complement rather than replace the harness | Apache-2.0 |
| [`ui-ux-pro-max`](https://github.com/nextlevelbuilder/ui-ux-pro-max-skill) | Searchable design data, stack-specific rules, design-system persistence, and recent Jetpack Compose support | Versioned releases, CLI, many stack adapters, and community posts; install/popularity signals are not measured task outcomes | Large generated/data-driven package, broad stack surface, external CLI/update path, and insufficient evidence that its Compose adapter preserves a repository's identity | MIT |
| [`impeccable`](https://github.com/pbakaus/impeccable) | Product-context capture, critique, audit, polish, anti-pattern detection, bounded screenshot passes, and design-system documentation | High public visibility and active packaging; feedback is primarily web-oriented | Explicitly frontend/browser focused, includes executable hooks/CLI and many commands, and uses an intentionally maximal design-director stance unsuited to automatic activation for focused native changes | Apache-2.0 |

### Considered but not shortlisted

- [`fixing-accessibility`](https://github.com/ibelick/ui-skills/tree/main/skills/fixing-accessibility): useful audit ordering,
  but HTML/ARIA/keyboard specific; official Compose semantics guidance is safer.
- [`iOS-Design-Agent-Skill`](https://github.com/vermont42/iOS-Design-Agent-Skill): demonstrates a thoughtful native port of
  frontend-design and validates the adaptation strategy, but SwiftUI/HIG details do not transfer directly.
- [`expo-ui`](https://github.com/expo/skills/tree/main/plugins/expo/skills/expo-ui): its “Jetpack Compose” target is Expo's
  React bridge, not a native Kotlin Compose application.
- Figma-to-Compose skills: useful only with a Figma/MCP source and do not solve critique, product identity, or verification.
- Screenshot-critique and web accessibility micro-skills: narrow supplements, but browser automation and DOM assumptions
  duplicate or conflict with the existing Compose screenshot workflow.

## Comparison matrix

Scores are 1 (poor) through 5 (strong) for this repository's goal, based on inspected instructions rather than marketing.
“Evidence” rates maintenance plus independent outcome evidence; most skills score modestly because public outcome data is
sparse.

| Candidate | Design specificity | Compose / M3 | Critique | Existing identity | A11y / adaptive | Render loop | Activation / context | Evidence | Adaptation |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| frontend-design | 5 | 1 | 4 | 3 | 2 | 3 | 5 | 3 | 2 |
| mobile-android-design | 2 | 4 | 1 | 2 | 4 | 2 | 4 | 2 | 3 |
| mobile-ui-ux-designer | 4 | 3 | 4 | 4 | 5 | 4 | 1 | 1 | 2 |
| compose-expert | 2 | 5 | 2 | 3 | 4 | 3 | 2 | 3 | 3 |
| material-3 | 3 | 4 | 3 | 2 | 4 | 2 | 2 | 2 | 3 |
| android/skills | 1 | 5 | 1 | 3 | 4 | 1 | 5 | 4 | 2 |
| ui-ux-pro-max | 4 | 3 | 3 | 3 | 3 | 2 | 1 | 2 | 2 |
| impeccable | 5 | 1 | 5 | 5 | 4 | 5 | 1 | 3 | 1 |
| local layered harness | 5 | 5 | 5 | 5 | 5 | 5 | 5 | 2 | 5 |

Operational criteria:

| Candidate | Codex portability | License / provenance | Supply-chain surface | Overlap risk |
|---|---|---|---|---|
| frontend-design | Standard skill file | Clear Apache-2.0 | Markdown only if copied | Medium: generic design layer |
| mobile-android-design | Standard skill + reference | Clear MIT parent license | Markdown only if copied | High: generic Compose guidance |
| mobile-ui-ux-designer | Compatible frontmatter with extra fields | MIT stated; small provenance trail | Single large instruction file | High: workflow and accessibility |
| compose-expert | Codex install documented | MIT plus Apache excerpts identified | Large copied/submodule reference set | High: implementation expertise |
| material-3 | Codex/manual install documented | MIT and source list | Plugin/package plus cross-stack refs | High: Material implementation |
| android/skills | Open standard and Android CLI | Official Apache-2.0 | CLI-managed remote updates if installed | Low: narrow official workflows |
| ui-ux-pro-max | Codex adapter and CLI | MIT | npm/CLI, release archives, generated data | High: broad design database |
| impeccable | Codex skill packaging | Apache-2.0 | Node CLI and optional hooks | High: critique/audit/polish |
| local layered harness | Native `.codex/skills` | Original text with source record | No executables or network dependency | Controlled within four references |

## Research questions answered

1. **Which skills exist?** The inventory above covers high-signal general design, native mobile, Android/Compose, Material,
   accessibility, critique, and screenshot-oriented options across official, vendor, and community repositories.
2. **Which are maintained or reused?** Android/skills, Anthropic skills, wshobson/agents, compose-skill, Impeccable, and
   ui-ux-pro-max show recent maintenance. Stars, forks, downloads, and install counts establish visibility only. Independent
   outcome reports are scarce and mixed, especially for frontend-design.
3. **What do they add beyond “beautiful”?** Subject grounding and restraint; user-goal/state contracts; Android component
   and adaptive rules; source-backed Compose implementation; token audits; screenshot iteration; or deterministic hooks.
4. **Which are Codex-compatible?** All shortlisted instruction sets can be read by Codex; several ship standard skill
   folders. Plugin commands, hooks, and other-agent metadata are not automatically portable and were excluded.
5. **Are any Android/Compose-aware?** Yes: mobile-android-design, compose-expert, material-3, ui-ux-pro-max's Compose
   adapter, and Android's official focused skills. None alone covers product-specific visual critique plus rendered iteration.
6. **What transfers from web?** Subject grounding, explicit hierarchy, one memorable idea, disciplined typography and
   color, meaningful structure, concise copy, restraint, self-critique, and screenshot review.
7. **What must be replaced?** DOM/CSS selectors, browser frameworks, hover, web breakpoints, page heroes, web font loading,
   ARIA recipes, viewport assumptions, and browser screenshot tools become Compose semantics, Material roles, touch,
   window size classes, system insets/IME, previews, instrumentation, and AGP screenshot tests.
8. **One or several skills?** One narrowly triggered skill with progressive references. Multiple broad skills would
   activate together, conflict, and repeat context.
9. **How does it use repository context?** It requires the theme, nearby UI/components, feature entry contract, exact state,
   `ScreenshotHarness`, filtered screenshot generation, exported PNG inspection, and a bounded correction pass.
10. **Which deterministic checks help?** Screenshot validation/diff reports, filtered previews at risk variants, Compose
    semantics/UI tests, automated accessibility checks where device infrastructure exists, compilation, lint, and contrast
    calculation. None replaces human judgment.
11. **What are the risks?** Skill instructions are code-adjacent supply-chain inputs. Risks include prompt injection,
    overbroad activation, hidden shell/network steps, stale APIs, unpinned remote updates, license loss, and architectural
    pattern bleed. The local skill contains no scripts, network dependency, or copied source, and records provenance.
12. **What gaps remain?** A model cannot prove taste, cultural fit, readability, or delight. Host previews cannot validate
    TalkBack, runtime insets, IME, gestures, lifecycle, or device rendering. Human product/design and accessibility review
    remains necessary for high-impact changes.

## Why the harness is concise

The activation description names both positive UI triggers and negative boundaries so ordinary Compose logic work does not
load it. The core file contains the invariant workflow. Repository identity, detailed Compose design rules, verification,
and provenance are one reference level deep. There are no personas, generic output templates, bundled assets, or scripts.

## Security, provenance, and maintenance

Agent skills can alter tool use and generated code, so popularity is not a trust boundary. Before vendoring a future skill:

1. inspect every instruction, script, asset, hook, manifest, and dependency;
2. verify repository ownership, license, and attribution requirements;
3. pin or record a reviewed commit and avoid unreviewed auto-updates;
4. reject unexpected credential, network, destructive-shell, or unrelated tool instructions;
5. test activation precision and task behavior against local scenarios;
6. compare new guidance with repository architecture and current official APIs.

Maintain the local harness when the theme, shared components, MVI contract, screenshot plugin, supported window policy, or
Compose accessibility/adaptive APIs change. Re-run the scenarios in
`docs/compose-ui-design-skill-validation.md`, the skill validator, and the relevant Gradle checks.

## Official technical references

- [Android skills overview](https://developer.android.com/tools/agents/android-skills)
- [Accessibility in Jetpack Compose](https://developer.android.com/develop/ui/compose/accessibility)
- [Compose semantics](https://developer.android.com/develop/ui/compose/accessibility/semantics)
- [Compose accessibility API defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults)
- [Build adaptive apps](https://developer.android.com/develop/ui/compose/build-adaptive-apps)
- [Support different display sizes](https://developer.android.com/develop/adaptive-apps/guides/support-different-display-sizes)
- [Canonical adaptive layouts](https://developer.android.com/develop/adaptive-apps/guides/canonical-layouts)
- [Compose preview screenshot testing](https://developer.android.com/studio/preview/compose-screenshot-testing)
- [Agent-skill semantic supply-chain research](https://arxiv.org/abs/2605.11418)
