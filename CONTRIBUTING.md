# Contributing to Spellbindr

## Prerequisites and setup

- Install JDK 17 (also recorded in `.java-version`).
- Install the Android SDK platform and build-tools versions configured by `app/build.gradle.kts`.
- Set the SDK path in `local.properties` or `ANDROID_HOME`.
- On Linux, `run/setup.sh` can install the command-line tools and configured SDK baseline.

Build a debug APK with `./gradlew assembleDebug`. The output is
`app/build/outputs/apk/debug/app-debug.apk`.

## Tests and checks

Run the narrowest check that covers the change while iterating. Examples:

```text
./gradlew testDebugUnitTest --tests 'fully.qualified.TestClass'
./gradlew connectedDebugAndroidTest
```

The second command requires a connected device or emulator. Instrumentation and screenshot tests are not part of the
broad CI task, so run their owning tasks when changing Android integration, Compose interaction, DI, or rendered UI.
Repository UI skills under `.agents/skills` document the screenshot-preview workflow.

Before opening a pull request, run:

```text
./gradlew lintDebug test assembleRelease --stacktrace
```

## Commits and pull requests

Prefer commit subjects in the form `type: summary`, such as `feat: add condition filters` or
`docs: record progression persistence decision`. CI runs for pull requests targeting `master` or `stable`; choose the
target branch appropriate to the release workflow and describe any checks that could not be run.

## Review-only knowledge placement checklist

During review, confirm that new guidance lives at the right loading level:

- durable architecture or product-engineering decision and rationale: indexed ADR;
- repeatable agent procedure or detailed task reference: `.agents/skills`;
- human setup and contribution workflow: `CONTRIBUTING.md`;
- volatile version or executable behavior: code, Gradle, scripts, or CI;
- temporary plan, implementation ledger, or review status: issue or pull request.

Do not add an automated gate for this checklist. Review it in context, and avoid duplicating authoritative facts across
documentation layers.
