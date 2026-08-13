# Spellbindr

Spellbindr is an Android companion for Dungeons & Dragons 5e. It provides a Jetpack Compose interface for browsing
bundled multi-source reference data, managing characters through guided or manual workflows, leveling managed
characters, and rolling dice.

## Architecture

Spellbindr is a single-module (`:app`) Kotlin application. Application wiring, domain models and use cases, data
implementations, Compose features, and tests are organized by package in `app`. Room stores character data, DataStore
stores preferences and favorites, and bundled JSON under `app/src/main/assets/data` supplies the reference catalog.

Durable technical and product-engineering decisions are indexed in [docs/adr/README.md](docs/adr/README.md).
Repeatable agent workflows live as repository skills under [.agents/skills](.agents/skills).

## Getting started

The project requires JDK 17 and the Android SDK configured by `app/build.gradle.kts`. Linux environments can use
`run/setup.sh` to bootstrap command-line Android tools. See [CONTRIBUTING.md](CONTRIBUTING.md) for setup, focused tests,
the CI-equivalent check, device-test constraints, and pull-request guidance.
