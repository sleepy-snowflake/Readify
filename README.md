# Readify

Offline-first Android read-later app with user-imported JSON extraction rules.
Save any article for distraction-free offline reading, export it as a
self-contained HTML file, and teach the app how to extract clean content from
your favorite sites with per-site rule files.

**No backend, no accounts, no analytics, no telemetry.** All data and rules
live on the device; the app only phones home to fetch content you ask for.

- **Status:** v1 development — milestone M0 (scaffold & contracts)
- **Package:** `com.sleepy.readify` · minSdk 26
- **License:** GPL-3.0 (app) · rules contributed to [Readify-Rules](https://github.com/sleepy-snowflake/Readify-Rules) are CC0
- **Docs:** [PRD](docs/PRD.md) · [Architecture](docs/Architecture.md) · [Phases](docs/Phases.md) · [Constraints](docs/Constraints.md) · [Rule schema reference](docs/RuleSchema.md)

## Building

All builds, tests, and lint run in GitHub Actions (see `.github/workflows/ci.yml`)
— there is no local build gate. Open a PR and watch the checks.

## Rules

Readify extracts articles using user-imported rule files (`readify-rule/1`
JSON). See the [rule schema reference](docs/RuleSchema.md) and the
authoritative [JSON Schema](schemas/readify-rule-1.schema.json). Ready-made
rules live in the [Readify-Rules](https://github.com/sleepy-snowflake/Readify-Rules)
repository.
