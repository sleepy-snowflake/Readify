# Readify — Architecture

- **Status:** Approved (v1)
- **Date:** 2026-08-29

## 1. Overview

```
                ┌──────────────────────────── :app (Compose UI, bottom nav, DI) ─────────────────────────┐
                │   Library (home) · Sources · Source page · Search · Reader · Rule manager + editor     │
                └─────────────────────────────────────────┬──────────────────────────────────────────────┘
                                                          │  feature:* ViewModels (MVVM + UDF, Hilt-injected)
                ┌─────────────────────────────────────────▼──────────────────────────────────────────────┐
                │ :feature:library        :feature:sources (listing, search, open sheet)                 │
                │ :feature:reader         :feature:rules (manager + editor with live preview)            │
                └────────┬───────────────────────┬───────────────────────┬───────────────────────┬───────┘
                         │                       │                       │                       │
                         ▼                       ▼                       ▼                       ▼
        ┌──────────────────────────────────────────── :core:* ────────────────────────────────────────────────┐
        │ :core:model      ArticleDocument (frozen) · RuleFile v1 (frozen) · shared domain types              │
        │ :core:rules      RuleMatcher · RuleLoader (schema validate) · RuleExecutor · JsSandbox · OutputGuard│
        │ :core:extractor  Readability-style fallback extractor (Jsoup DOM)                                   │
        │ :core:network    OkHttp fetcher · WebView JS renderer (requiresJs / gate-fail retry)                │
        │ :core:database   Room (sources, articles, images, listing cache) · raw-HTML store                   │
        │ :core:export     ArticleDocument → standalone HTML serializer (base64 images)                       │
        └───────────────────────────┬─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    └─ WorkManager jobs: SaveJob (fetch→extract→images→persist) · ExportJob · RuleFetchJob (manual)
```

**The single contract:** everything upstream (rule executor, Readability
fallback, WebView-rendered extraction) produces an `ArticleDocument`. The
reader, HTML exporter, and persistence layer only ever see `ArticleDocument`.

## 2. Modules

| Module | Responsibility | Depends on |
|---|---|---|
| `:app` | Navigation, bottom nav (Library start), DI graph, theming | all `feature:*` |
| `:feature:library` | Home list, delete, merge prompt on duplicate | core:* |
| `:feature:sources` | Sources grid, source listing page, search screen, open sheet | core:* |
| `:feature:reader` | Native Compose renderer for `ArticleDocument`, themes, typography | core:model, core:database |
| `:feature:rules` | Rule manager (import/priority/enable/delete) + editor with live preview | core:rules, core:database |
| `:core:model` | `ArticleDocument`, `RuleFile`, domain types — **pure Kotlin, no Android** | — |
| `:core:rules` | Engine: matcher, loader (JSON-Schema validation), executor (cleanup → fields → blocks → pagination), output guard, Rhino sandbox | model |
| `:core:extractor` | Readability-algorithm fallback producing `ArticleDocument` | model |
| `:core:network` | OkHttp client (politeness rules), WebView fetch for `requiresJs` | model |
| `:core:database` | Room entities/DAOs, file storage for images + gzipped raw HTML | model |
| `:core:export` | `ArticleDocument` → standalone HTML (sanitized, base64 images, size cap) | model |

Dependency rule: `feature:* → core:*` only; `core:model` depends on nothing;
no feature-to-feature dependencies.

## 3. Core Contracts (frozen for v1)

### 3.1 `ArticleDocument`

```jsonc
{
  "title": "…", "author": "…?", "date": "ISO-8601?", "source": "…?",
  "cover": "local-path-or-url?",           // og:image
  "blocks": [
    { "type": "heading", "level": 2, "runs": [ ...inline ] },
    { "type": "paragraph", "runs": [ ...inline ] },
    { "type": "image", "src": "local path", "alt": "…?" },
    { "type": "figure", "src": "…", "caption": "…" },
    { "type": "quote", "runs": [ ...inline ], "cite": "…?" },
    { "type": "code", "text": "…", "lang": "…?" },        // no highlighting v1
    { "type": "list", "ordered": false, "items": [        // items nest lists
        { "runs": [...] }, { "list": { ...nested } } ] },
    { "type": "table", "rows": [ [ { "runs": [...] } ] ], "header": true? },
    { "type": "hr" }
  ]
}
```

**Inline runs** (paragraphs are typed runs, never plain strings):

```jsonc
{ "t": "text|link|em|strong|code", "s": "…", "href": "… (link only)" }
```

Frozen block enum for v1: `heading, paragraph, image, figure, quote, code,
list, table, hr`. Math, embeds, and footnotes are **v2** — the enum must
not grow in v1 (see Constraints.md C1).

### 3.2 `RuleFile` v1 (`"schema": "readify-rule/1"`)

```jsonc
{
  "schema": "readify-rule/1",
  "id": "example-blog",                  // required, unique, stable
  "name": "Example Blog",                // required
  "version": 3,                          // required, int, update gate
  "domains": ["example.com", "blog.example.com"],   // required, match key
  "requiresJs": false,                   // hint: use WebView fetch
  "cleanup": { "remove": ["nav", ".ads", "script", "style"] },   // pre-pass
  "listing": {                           // optional: source home page
    "item":   "article.post",
    "title":  "h2 a",
    "link":   "h2 a@href",
    "date":   "time@datetime?",          // "?" suffix = optional field
    "excerpt":".summary?",
    "nextPage": "a.next?"
  },
  "search": {                            // optional: omit → FAB hidden
    "url": "https://example.com/?s={query}",
    "item": "...", "title": "...", "link": "...", "date": "...?", "excerpt": "?"
  },
  "fields": {                            // article metadata
    "title":  { "sel": "h1.article-title" },
    "author": { "sel": ".byline .author?" },
    "date":   { "sel": "time?", "attr": "datetime" }
  },
  "content": {                           // required
    "container": "article.post",
    "blocks": {
      "h2":   { "type": "heading", "level": 2 },
      "p":    { "type": "paragraph" },
      "img":  { "type": "image", "src": "src", "alt": "alt" },
      "figure": { "type": "figure", "src": "img@src", "caption": "figcaption" },
      "pre":  { "type": "code", "lang": "code@class?" },
      "blockquote": { "type": "quote" },
      "ul":   { "type": "list" }, "ol": { "type": "list", "ordered": true },
      "table":{ "type": "table" }
    }
  },
  "pagination": { "nextPage": "a.next?" },          // multi-page articles
  "js": { "preExtract": "function(doc){ … }" }      // optional, sandboxed
}
```

Notes:
- The illustrative JSON above is NOT normative: the authoritative contract is
  the machine-readable schema at `schemas/readify-rule-1.schema.json`, with a
  human-readable field reference in `docs/RuleSchema.md` (both created in M0).
- Selector types: CSS (Jsoup) primary; `@attr` reads attributes; `?` marks
  optional. Regex and JSONPath selector modes exist for special cases.
- Block extraction is **tree-walking**: the executor walks `container`'s
  descendants, maps matching nodes via `blocks`, and recurses into known
  container elements (li → runs, figure → img+caption). Flat sibling selectors
  alone are not sufficient — nested structures must resolve.
- Every rule is validated against the published JSON Schema at import; the
  engine additionally validates at load. Invalid rules never execute.

### 3.3 ADR-001: No registry (removed by product decision)

Rules are **user-imported only** (URL / file / paste). A separate repo may host
rule files for humans to download manually; the app never parses any index and
has no background rule fetching. Consequence: no supply chain → rule `js` is
permitted (sandboxed) in all rules. See Constraints.md C2 (user-import only)
and C4 (sandboxed JS).

## 4. Extraction Pipeline (Read)

```
URL → canonicalize (strip utm_*/fbclid/gclid…, honor rel=canonical / og:url)
    → dedupe check (canonical URL unique; duplicate → merge prompt)
    → rule match (domain, user priority order, enabled only)
    → fetch (OkHttp; WebView if requiresJs OR static fetch fails quality gate)
    → [rule present]  validate → cleanup → fields → blocks → paginate (≤10, ≥1 s)
    → [no rule]       Readability fallback on fetched HTML
    → OutputGuard: quality gate (title + (≥3 blocks or ≥80 words))
         pass → image pipeline → persist (JSON + gzipped raw HTML + images)
         fail → other path's output? → take better
         both fail → save raw + status=EXTRACTION_FAILED (+ retry / auto-retry
                     after rule update for that source)
    → open reader
```

**Image pipeline:** resolve relative URLs → `srcset` best candidate → lazy
attrs (`data-src`, `data-lazy-src`, …) → download to app-private storage →
rewrite `src` to local path → 25 MB per-article cap.

**Politeness:** requests are strictly serial — one at a time, never parallel,
prefetched, or crawled; ≥1 s delay between pagination page fetches; no
robots.txt enforcement (equivalent to a user browsing; documented in the
rules repo).

## 5. Engine Components (`:core:rules`)

| Component | Duty |
|---|---|
| `RuleMatcher` | URL → best rule by registered domains, user priority, enabled flag |
| `RuleLoader` | Parse + JSON-Schema validate + hard-limit checks; throws `RuleInvalid` |
| `RuleExecutor` | Cleanup → metadata fields → tree-walk blocks → pagination loop |
| `SelectorResolver` | One impl per selector kind (CSS / `@attr` / regex / JSONPath); rule `js` is a sandboxed `preExtract` hook, not a selector |
| `JsSandbox` | Rhino engine: allowlisted API surface, CPU-time limit, no I/O |
| `OutputGuard` | Re-validate result into `ArticleDocument`; quality score; never trusts rule output |

Failure taxonomy: `RuleNotFound` (→ fallback), `RuleInvalid` (import-time),
`NetworkError` (retryable), `ExtractionLowQuality` (→ fallback → failed state),
`StorageError`, `ExportError`.

## 6. Data Layer

Room (schema export + migration tests from day 1):

- `sources` — imported rules: id, name, version, domains JSON, rule JSON,
  enabled, priority, origin (url/file/manual), originUrl?, importedAt
- `articles` — canonicalUrl (unique), sourceId?, title, author, date, coverPath,
  docJson, rawHtmlPath, status (`OK` | `EXTRACTION_FAILED`), wordCount,
  addedAt, readState, readProgress, exportedPath?
- `images` — articleId, originalUrl, localPath, bytes
- `listingCache` — sourceId, url, listingJson, fetchedAt

Files: images + gzipped raw HTML in app-private storage; HTML exports go to
the public Downloads dir via MediaStore (`Downloads/Readify/`, API 29+;
app-specific dir on 26–28). Deleting an article removes its row, images,
and raw HTML.

## 7. UI Layer

Compose + Material 3, MVVM + unidirectional data flow, Compose Navigation,
Hilt. Bottom navigation with two destinations: **Library** (start) and
**Sources**; source page, search, reader, and rule editor are nested
destinations. Typography/theme settings persist via DataStore.

## 8. HTML Export (`:core:export`)

`ArticleDocument` → minimal, sanitized standalone HTML document (own CSS,
dark-mode aware): images inlined as base64 data URIs up to a ~50 MB total cap;
images exceeding the cap keep remote URLs with an HTML footnote noting the
omission. Output: `Downloads/Readify/<sanitized-title>.html`, filename
`[a-zA-Z0-9._-]`, ≤80 chars, collision → `-2` suffix.
