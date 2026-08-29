# Readify — PRD (Product Requirements Document)

- **Status:** Approved (v1 scope frozen)
- **Date:** 2026-08-29
- **Package:** `com.sleepy.readify`
- **License:** GPL-3.0 (app), CC0 (rules repo)
- **Platform:** Android, minSdk 26

## 1. Summary

Readify is an open-source, offline-first Android read-it-later app. Users import
**rule files** (JSON) that teach the app how to extract clean, structured content
from specific websites. Saved articles are rendered as a distraction-free native
reading experience and can be exported as standalone, self-contained HTML files.

There is **no backend, no registry, no account, no analytics**. All data and all
rules live on the device.

## 2. Problem

Modern blogs and articles are hostile to reading: ads, cookie walls, JS-heavy
layouts, broken mobile rendering. Existing read-later apps use generic extraction
that fails on many sites. Readify lets per-site rules fix extraction precisely,
while a generic fallback keeps the app usable on any URL out of the box.

## 3. Goals

1. Save any article for offline reading with a two-tap flow.
2. Let power users author, import, and share extraction rules without app updates.
3. Produce genuinely clean content: a typed JSON document model, rendered natively.
4. One-file HTML export that works fully offline (images embedded).
5. Total privacy: nothing leaves the device except the article fetch itself.

## 4. Non-Goals (v1)

- Rule registry / auto-update service (explicitly removed — see ADR in Architecture.md)
- Cloud sync, accounts, telemetry, analytics, ads
- EPUB/PDF export, WebDAV backup
- Math (KaTeX), embeds (YouTube), footnotes, syntax highlighting, TTS
- Full-text library search, article refresh/re-extract UI
- iOS / desktop

## 5. User Flows

### F1 — Library (Home, start destination)
App opens on the **Library** tab: list of saved articles (cover, title, source,
save date, unread/read state). Tap → native reader. Delete (swipe/long-press) →
removes DB row, images, and raw HTML.

### F2 — Sources
**Sources** tab lists imported, enabled sources (rules). Tap a source →
**Source page**: native article listing rendered from the rule's `listing`
fields, with back arrow and a search FAB (hidden if the rule has no `search`
section). Listing supports pagination via `listing.nextPage`.

### F3 — Search
Search FAB → **Search screen** scoped to that source, using the rule's
`search.url` template (`{query}` placeholder). Results render as a native list.
Tap a result → open sheet.

### F4 — Open sheet (listing item or search result)
Bottom sheet with two actions:
- **Read** → canonicalize URL → dedupe check → extract (rule → quality gate →
  Readability fallback) → save to Library → open reader.
- **Download** → extract → serialize `ArticleDocument` → standalone HTML →
  save to `Downloads/Readify/<sanitized-title>.html`.

### F5 — Rules
Import via **URL / file / paste JSON**. Rules are schema-validated at import;
invalid rules are rejected with a readable error. Re-importing the same `id`
with a higher `version` updates it. Manage: enable/disable, delete, user-ordered
priority (first match wins on overlapping domains). In-app editor with live
preview (paste URL → run rule → extracted JSON + rendered article). Export rule
as JSON. URL-imported rules remember their source for manual "check update"
(user-triggered, never automatic).

## 6. Functional Requirements

| ID | Requirement |
|----|-------------|
| FR-1 | Library tab is the start destination; lists saved articles with cover, title, source name, date, read state. |
| FR-2 | Reader renders every frozen v1 block type (see Architecture.md §3.1) with light/dark/sepia/black themes and typography settings (font size, line height). |
| FR-3 | Sources tab lists enabled sources; disabled sources are hidden from browse but their saved articles remain readable. |
| FR-4 | Source page renders a native listing from rule fields with pagination; back arrow returns to Sources; FAB hidden when rule lacks `search`. |
| FR-5 | Search screen queries the source site via the rule's search template and renders results natively. |
| FR-6 | Opening an article (listing or search result) always presents the Read/Download bottom sheet. |
| FR-7 | Read: strips tracking params, honors `rel=canonical`/`og:url`, dedupes on canonical URL with a merge prompt (keep newer extraction, merge read state). |
| FR-8 | Read: extraction passes the quality gate (title + ≥3 blocks or ≥80 words); on gate failure the Readability fallback runs; on double failure the article is saved raw with an "extraction failed" state, a manual retry action, and automatic retry after that source's rule is updated. |
| FR-9 | Read: multi-page articles follow `pagination.nextPage` (max 10 pages, ≥1 s delay between page fetches). |
| FR-10 | Read: images resolved (relative → absolute, `srcset` best candidate, lazy `data-src` honored), downloaded, cached, rewritten to local paths; 25 MB cap per article. |
| FR-11 | Read: original fetched HTML is retained (gzipped) alongside extracted JSON for re-extraction and rule preview. |
| FR-12 | Download: exports a standalone HTML file with images embedded as base64 (cap ~50 MB; oversized images stay remote with a footnote in the file). |
| FR-13 | Download: filename = sanitized title `[a-zA-Z0-9._-]`, ≤80 chars, collision → `-2` suffix; saved via MediaStore (`Downloads/Readify/`) on API 29+, app-specific dir fallback on 26–28. |
| FR-14 | Rules: import (URL/file/paste) with schema validation and actionable error messages; version-gated update by re-import; enable/disable/delete; priority ordering. |
| FR-15 | Rules: in-app editor with live preview (JSON view + rendered view) and export. |
| FR-16 | Full offline operation after save: reading, browsing cached listings, and rule editing require no network. |

## 7. Non-Functional Requirements

| ID | Requirement |
|----|-------------|
| NFR-1 | Cold start < 2 s on mid-range hardware; library list usable < 500 ms. |
| NFR-2 | Zero network calls except: article/listing/search fetches, image downloads, and explicit user-triggered rule URL fetches. |
| NFR-3 | No analytics, crash reporting, or trackers of any kind. |
| NFR-4 | A broken or malicious rule may waste seconds — never freeze the app or escape its sandbox (see Constraints.md). |
| NFR-5 | Room schema exported and migration tests exist from day 1. |
| NFR-6 | All user-facing strings in `strings.xml` (i18n-ready) from day 1. |

## 8. Success Criteria (v1)

1. End-to-end Read works via rules on ≥ 3 real sites and via fallback on ≥ 1 site without a rule.
2. An exported HTML file opens offline in a browser with images visible.
3. Rule import → edit → preview → re-import round-trips without data loss.
4. Killing the network mid-save never corrupts the library (job resumes or fails cleanly with retry).
5. No crashes across the five flows during a 1-week dogfood.
