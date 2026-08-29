# Readify — Phases (Milestones)

- **Status:** Approved (v1)
- **Date:** 2026-08-29

Each milestone ends with everything merged, green CI, and its acceptance
criteria demonstrably met. "Frozen" artifacts (see Constraints.md) may not
change after their milestone except via a version bump + doc update.

## M0 — Scaffold & Contracts

**Deliverables**
- Gradle multi-module project per Architecture.md §2, Kotlin 2.x, Compose,
  Hilt, Room, DataStore, WorkManager, OkHttp, Jsoup, Rhino wired in.
- `:core:model` implements `ArticleDocument` + `RuleFile` v1 (incl. listing
  and search sections) with kotlinx.serialization + JSON Schema files.
- CI (GitHub Actions): build + unit tests + lint on every push/PR — the only
  build gate; no local builds.
- Machine-readable JSON Schema (`schemas/readify-rule-1.schema.json`) plus a
  human-readable reference (`docs/RuleSchema.md`) — the schema file is
  authoritative over the illustrative JSON in Architecture.md. A JVM
  JSON-Schema validator dependency is wired (e.g., `networknt/json-schema-validator`).
- GPL-3.0 LICENSE (app), README, rules-repo stub (CC0).
- Package `com.sleepy.readify`, minSdk 26.

**Acceptance**
- [ ] Required CI checks green on a PR touching all modules.
- [ ] ArticleDocument round-trips JSON ↔ object (serialization tests).
- [ ] A sample RuleFile validates against the shipped JSON Schema (valid + invalid fixtures).

## M1 — Extraction Engine End-to-End

**Deliverables**
- `:core:network` fetcher (canonicalization, politeness) + WebView path for
  `requiresJs` and for static fetches that fail the quality gate.
- `:core:rules`: matcher, loader, executor (cleanup → fields → tree-walking
  blocks → pagination: ≤10 pages, ≥1 s delay), selector resolvers (CSS
  primary; `@attr`; regex; JSONPath), OutputGuard + quality gate (title +
  ≥3 blocks or ≥80 words).
- `:core:extractor`: Readability-style fallback emitting `ArticleDocument`.
- Rhino sandbox with allowlisted API, CPU cap, no I/O.
- Unit/integration tests incl. saved-HTML fixtures from 3 real sites.

**Acceptance**
- [ ] Rule-based extraction produces valid ArticleDocument for ≥3 real sites (fixture-tested).
- [ ] Fallback extractor produces valid output for ≥1 site without a rule.
- [ ] A 3-page `nextPage` chain merges into one continuous ArticleDocument (fixture-tested).
- [ ] A rule with an infinite JS loop times out inside the cap without crashing the test JVM.
- [ ] Malformed rules are rejected at load with readable errors.

## M2 — Core App: Browse, Read, Library

**Deliverables**
- Bottom nav: Library (start) + Sources; source page (native listing +
  pagination, back arrow, search FAB hidden when rule lacks `search`);
  search screen; open bottom sheet [Read] [Download] (Download stub ok).
- Read pipeline wired to engine + fallback; save to Room + images + gzipped
  raw HTML (FR-11); library list with cover/title/source/date/read-state;
  delete removes all artifacts; duplicate canonical-URL merge prompt.
- Minimal rule import (file/paste only — full manager with URL import,
  editing, and priority lands in M4) so the pipeline is usable end-to-end.
- Failure handling: articles failing gate + fallback are saved raw with a
  visible "extraction failed" state (retry actions and auto-retry land in M4).
- Basic reader rendering the full frozen block set (single theme).

**Acceptance**
- [ ] Dogfood flow: import rule → browse source → search → Read → kill app →
      open Library offline → article renders with images.
- [ ] No-rule URL read lands in Library via fallback.
- [ ] Deleting an article leaves zero orphan files (test).

## M3 — Reader Polish & Media

**Deliverables**
- Reader themes (light/dark/sepia/black) + typography settings (DataStore).
- Full image pipeline (srcset, lazy attrs, cap, rewrite) replacing M2's
  minimal image handling; listing cache for offline source browsing.
- Continuous-scroll polish for long articles — multi-page content arrives
  pre-stitched by the engine (M1); the reader only ever renders one document.

**Acceptance**
- [ ] Lazy-loaded images appear offline after save on a `data-src` site fixture.
- [ ] A pre-stitched 3-page article renders as one continuous scroll.
- [ ] Theme/typography choices persist across restarts.

## M4 — Export & Rule Manager

**Deliverables**
- `:core:export` HTML serializer (sanitized, base64 images, ~50 MB cap,
  oversize → remote + footnote) + MediaStore write to `Downloads/Readify/`
  (app-dir fallback 26–28) + filename sanitization/collision.
- `:feature:rules`: import (URL/file/paste) with validation errors, re-import
  update (id match, higher version), enable/disable/delete, priority ordering,
  export; in-app editor with live preview (JSON + rendered) using saved raw
  HTML or a pasted URL; manual "check update" for URL-imported rules.
- "Extraction failed" state + manual retry + auto-retry after rule update.

**Acceptance**
- [ ] Exported file opens offline in a browser; images visible; >cap file degrades gracefully.
- [ ] Rule round-trip: import → edit → preview → export → re-import, no loss.
- [ ] Fixing a broken rule and re-importing flips failed articles to OK on retry.

## M5 — Hardening & Release

**Deliverables**
- Error-state UI sweep (no silent failures), empty states, save-failure retry.
- Performance pass (NFR-1), proguard/R8 config, signed release build, F-Droid /
  GitHub release metadata.
- v1.0 tag; publish rules repo with ≥5 tested rules (CC0).

**Acceptance**
- [ ] 1-week dogfood: no crashes across PRD flows (success criterion 5).
- [ ] All PRD success criteria checked.

## Post-v1 Backlog (explicitly out of v1)

Full-text library search · article refresh/re-extract UI · EPUB/PDF export ·
WebDAV backup · KaTeX math · embeds · footnotes · syntax highlighting · TTS ·
rule schema v2.
