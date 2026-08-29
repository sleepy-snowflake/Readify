# Readify — Constraints

Hard rules for every contribution. **MUST** / **NEVER** are binding; violations
fail review and CI. Changing anything marked FROZEN requires a schema/version
bump plus updates to Architecture.md and this file in the same PR.

## C1 — Frozen contracts

- `ArticleDocument` block enum is FROZEN for v1: `heading, paragraph, image,
  figure, quote, code, list, table, hr`. Inline run types: `text, link, em,
  strong, code`. New types → `RuleFile`/document schema v2, not v1.
- `RuleFile` is versioned via `"schema": "readify-rule/1"`. The engine must
  keep validating v1 forever; v2 support is additive.

## C2 — Product boundaries

- MUST NOT add: registry client, background rule fetching, accounts, cloud
  sync, analytics, crash reporting, ads, or any telemetry. The app phones home
  **only** to fetch user-requested content (articles, listings, searches,
  images) and user-triggered rule URLs.
- Rules enter the app exclusively by user import (URL / file / paste). No
  bundled rules, no remote index parsing — ever (ADR-001).

## C3 — Engine trust boundary

- The engine NEVER trusts rule output: every extraction result is re-validated
  into `ArticleDocument` by `OutputGuard` before persistence or rendering.
- A rule failure MUST degrade to the Readability fallback or the
  "extraction failed" state — it can never crash the app, corrupt the library,
  or block the UI thread.

## C4 — Sandbox (all rule JS)

- Rhino only, with an allowlisted API surface; NEVER expose file, network,
  reflection, or class-loader access to rule scripts.
- Hard limits (enforced in `:core:rules`): rule file ≤ 64 KB; regex match
  timeouts with ReDoS-safe flags; bounded selector counts; CPU-time cap per JS
  invocation. A hostile rule may waste seconds — never minutes, never a freeze.

## C5 — Network politeness

- Requests are strictly serial — one at a time, never parallel, prefetched,
  or crawled; pagination ≤ 10 pages with ≥ 1 s delay between page fetches;
  no retry storms (exponential backoff, bounded retries).
- No robots.txt enforcement in v1 (single-user browsing equivalence) — this
  decision MUST stay documented in the rules repo README.

## C6 — Storage & privacy

- All content (articles, images, raw HTML, rules) stays in app-private
  storage; the only public write is the HTML export to `Downloads/Readify/`
  via MediaStore (API 29+) or app-specific dir (26–28).
- Raw fetched HTML is always retained (gzipped) with the extracted document.
- Deleting an article MUST remove its DB row, images, and raw HTML — no orphans.

## C7 — Platform & architecture

- Kotlin + Jetpack Compose + Material 3 only; no XML views for new UI, no
  WebView for content rendering (WebView is fetch-only, never reader UI).
- Module dependency direction: `feature:* → core:*` only; `core:model` has
  zero dependencies (pure Kotlin); no feature-to-feature deps.
- minSdk 26; target/compile latest stable.
- Room: schema export ON; every migration has a migration test.

## C8 — Code

- All user-facing strings in `strings.xml`; no hard-coded text in composables.
- Coroutines/Flow for all async work; no blocking I/O on the main thread.
- Error handling: domain error types per Architecture.md §5; no swallowed
  exceptions; no `!!` outside tests.
- License headers GPL-3.0; new rules contributed to the rules repo are CC0.

## C9 — Verification gate

- Builds run **exclusively in GitHub Actions** — no local build gate exists.
  A PR is mergeable only with all required CI checks green (build, unit
  tests, lint); new engine behavior covered by fixture tests (saved HTML in,
  ArticleDocument out).
