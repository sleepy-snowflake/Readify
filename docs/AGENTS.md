# AGENTS.md — Readify

Guidance for AI agents (and humans) working in this repository. Read
`docs/Constraints.md` before writing code — it is binding. Read
`docs/Architecture.md` for module layout and the frozen contracts.

## Project

- Readify: offline-first Android read-later app with user-imported JSON
  extraction rules. Package `com.sleepy.readify`, minSdk 26.
- Current phase: see `docs/Phases.md`. Work only on the current milestone
  unless explicitly asked otherwise.

## Repo map

```
docs/               PRD, Architecture, Phases, Constraints, this file
app/                :app — nav, DI, theming
core/model/         frozen ArticleDocument + RuleFile (pure Kotlin)
core/rules/         engine: matcher/loader/executor/guard + sandbox
core/extractor/     Readability-style fallback
core/network/       OkHttp fetcher, WebView JS rendering
core/database/      Room + file storage
core/export/        ArticleDocument → standalone HTML
feature/*/          library, sources, reader, rules UI
schemas/            JSON Schemas (readify-rule-1) — created in M0
```

(All module dirs are created in M0.)

## Builds & verification (CI-first — no local builds)

Local Gradle builds are **not** part of the workflow; GitHub Actions is the
only build/test/lint gate (workflow configured in M0).

1. Commit and push your branch.
2. Check status: `gh run list --branch <branch>`, watch with `gh run watch`.
3. On failure: `gh run view <run-id> --log-failed`, fix, re-push.

Never declare work done without a green run on the pushed commit. Do not
assume code compiles or tests pass because it "should" — CI is ground truth.

## Working rules for agents

1. **Read Constraints.md first.** Frozen contracts (`ArticleDocument`,
   `RuleFile` v1) are not editable without a version bump + doc updates in
   the same PR.
2. **Follow the milestone.** Do not pull post-v1 backlog items into v1.
3. **Respect module boundaries**: feature → core only; `core:model` stays
   dependency-free; no feature-to-feature imports.
4. **Engine changes need fixture tests**: saved HTML files under
   `core/rules/src/test/resources/fixtures/<site>/` (raw.html + expected.json).
   Add both when touching extraction.
5. **No comments in code** unless the user asks; keep names expressive instead.
6. **Strings, not literals**: all user-facing text goes in `strings.xml`.
7. **Errors are domain types** (Architecture.md §5) — no swallowed exceptions.
8. **Docs stay truthful**: any change to pipeline, schema, or limits updates
   the relevant doc in the same PR. If docs and code disagree, stop and ask.
9. **Don't commit** unless explicitly asked; don't touch signing configs;
   never add network calls beyond the allowed set (Constraints.md C2).
10. Prefer editing existing files; match surrounding style; no drive-by refactors.

## Definition of done

- Code written per conventions above
- Required CI checks green on the pushed commit (build, unit tests, lint)
- New behavior covered by tests (fixtures for engine, screens smoke-tested)
- Relevant docs updated if behavior/contracts changed
