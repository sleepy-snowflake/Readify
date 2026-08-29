# Rule Schema Reference — `readify-rule/1`

Human-readable companion to the authoritative machine-readable schema at
[`schemas/readify-rule-1.schema.json`](../schemas/readify-rule-1.schema.json).
If this file and the JSON Schema disagree, **the JSON Schema wins**.

Rules enter the app exclusively by user import (URL / file / paste). Every rule
is validated against the schema at import and re-validated by the engine at
load. Invalid rules never execute. Rule files are capped at **64 KB**.

## Top-level fields

| Field | Type | Required | Notes |
|---|---|---|---|
| `schema` | string | yes | Must be exactly `"readify-rule/1"`. |
| `id` | string | yes | `^[a-z0-9][a-z0-9._-]*$`, ≤ 64 chars, stable and unique. Same `id` + higher `version` on re-import = update. |
| `name` | string | yes | Display name, ≤ 120 chars. |
| `version` | integer | yes | ≥ 1. Update gate for re-import. |
| `domains` | string[] | yes | 1+ hostnames. Match key for `RuleMatcher`; first match by user priority wins. |
| `requiresJs` | boolean | no | Default `false`. Hint to fetch via WebView before extraction. |
| `cleanup` | object | no | `{ "remove": [selector, …] }` pre-pass. |
| `listing` | object | no | Source home page listing (see below). |
| `search` | object | no | Search support (see below). Omit → search FAB hidden. |
| `fields` | object | no | Article metadata; `title` required if present, `author`/`date` optional. |
| `content` | object | yes | `{ "container": selector, "blocks": { … } }`. |
| `pagination` | object | no | `{ "nextPage": selector }` for multi-page articles (≤ 10 pages, ≥ 1 s between fetches). |
| `js` | object | no | `{ "preExtract": "function(doc){ … }" }` — sandboxed (Rhino, allowlisted API, CPU cap, no I/O). |

## Selector syntax

- **CSS** (Jsoup) is the primary selector language, used everywhere a
  selector is expected.
- `@attr` appended to a selector reads an attribute
  (e.g. `h2 a@href`, `img@data-src`).
- A trailing `?` marks the field **optional** — missing matches do not fail
  extraction (e.g. `time@datetime?`).
- Regex and JSONPath selector modes exist for special cases (engine-level
  feature, see Architecture.md §5).

## `listing` / `search`

Both map a results page to items. `listing` requires `item`, `title`, `link`
and allows optional `date`, `excerpt`, `nextPage` (listing pagination).
`search` additionally requires `url` — a template where `{query}` is replaced
with the URL-encoded search terms.

## `content.blocks`

Maps element selectors inside `container` to block producers. `type` is one of
the frozen v1 block types: `heading`, `paragraph`, `image`, `figure`, `quote`,
`code`, `list`, `table` (`hr` is not mappable). Supported extras per type:

| type | extras |
|---|---|
| `heading` | `level` (1–6) |
| `image` | `src` (selector/`@attr`), `alt` |
| `figure` | `src`, `caption` |
| `code` | `lang` (e.g. `code@class?`) |
| `list` | `ordered` (`true` for `<ol>`) |
| `paragraph`, `quote`, `table` | — |

Block extraction is **tree-walking**: the executor walks `container`'s
descendants, maps matching nodes via `blocks`, and recurses into known
container elements (li → runs, figure → img + caption).

## Versioning (C1)

`readify-rule/1` is frozen for v1. Any change that adds block types, inline
run types, or fields requires a new schema version (`readify-rule/2`) —
additive only; v1 rules must keep validating forever.
