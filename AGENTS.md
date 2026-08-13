# Repository Guidelines

Spellbindr is a single-module Android project. `:app` owns application wiring, domain and data code, Compose UI,
assets, and all test source sets. Treat `app/build.gradle.kts`, `gradle/libs.versions.toml`, executable scripts, and CI
workflows as authoritative for versions and runnable behavior.

## Build and verification

Use the narrowest relevant check while developing. JVM tests live in `app/src/test/kotlin`; use
`./gradlew testDebugUnitTest --tests 'fully.qualified.TestClass'` for focused runs. Instrumentation and Compose UI tests
live in `app/src/androidTest/kotlin` and require a device or emulator. Screenshot previews and tests live in
`app/src/screenshotTest/kotlin`.

Before handoff, run checks proportional to the change. The broad CI-equivalent command is:

```text
./gradlew lintDebug test assembleRelease --stacktrace
```

## Code organization and style

Keep code and tests close to the package that owns them. Static multi-source 5e reference data lives in
`app/src/main/assets/data`; runtime artwork and icons live under `app/src/main/assets`.

Follow `.editorconfig`: four-space indentation, LF endings, a 120-character line limit, and a final newline; JSON and
YAML use two spaces. Preserve established names such as `*Screen`, `*Route`, `*ViewModel`, `*UseCase`, `*Repository`,
and `*RepositoryImpl`.

## Repository knowledge

Use [the ADR index](docs/adr/README.md) to find only the decisions relevant to the work. Do not load every ADR by
default. Accepted ADRs describe durable constraints and rationale; when a decision changes, add a superseding ADR
instead of materially rewriting the accepted record.

Put durable architecture or product-engineering decisions in ADRs, repeatable agent procedures in repository skills
under `.agents/skills`, human contribution instructions in `CONTRIBUTING.md`, and volatile facts in code, Gradle,
scripts, or CI. Keep temporary plans, progress ledgers, and review status in issues or pull requests.
