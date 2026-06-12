<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
-->
---
name: groovysh
description: AI-tooling guardrails for changes in subprojects/groovy-groovysh/ — points at the subproject's canonical architecture doc (vendored JLine forks, three-layer test infrastructure, terminal-aware test patterns, command-specific quirks, JLine bump procedure), then adds the AI-specific constraints on top: no fabricated JLine API names, terminal tests must use `dumb(true).streams(...)`, no full-string ANSI assertions, fork-sync diffs go against the fork-base tag, no reformatting of fork files, hand-back for review. Use when modifying anything in that subproject's tree, bumping JLine, or syncing vendored fork files.
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: ai-tooling-groovysh-guardrails
---

# groovysh

This skill is the **AI-tooling layer** over the `groovy-groovysh`
subproject's architecture. The conventions themselves — the
vendored JLine forks, the three-layer test infrastructure, the
terminal-aware test patterns, command-specific quirks, and the
JLine bump procedure — live in
[`subprojects/groovy-groovysh/ARCHITECTURE.md`](../../../subprojects/groovy-groovysh/ARCHITECTURE.md).
This skill cites them and adds the AI-specific guardrails: no
fabricated JLine API names, terminal tests use
`dumb(true).streams(...)`, no full-string ANSI assertions,
fork-sync diffs against the fork-base tag, no reformatting of
fork files.

- [`groovy-tests`](../groovy-tests/SKILL.md) — pair with for
  test conventions; this skill complements it for the
  terminal-aware testing layer.
- [`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md) — AI
  guardrails for the surrounding fix workflow when a fix lands
  in this subproject.
- [`subprojects/groovy-groovysh/ARCHITECTURE.md`](../../../subprojects/groovy-groovysh/ARCHITECTURE.md)
  — canonical subproject architecture; load alongside.

## When to use this skill

**Use it for:**

- REPL command changes
  (`subprojects/groovy-groovysh/src/main/groovy/.../jline/`).
- Anything terminal-related: writing tests, integrating JLine
  APIs, image rendering.
- Bumping the JLine version in `versions.properties`.
- Syncing vendored fork files against upstream JLine.

**Don't use it for:**

- Pure compiler/runtime changes elsewhere —
  [`groovy-internals`](../groovy-internals/SKILL.md).
- Changes to the project-wide build (root `build.gradle`,
  `build-logic/`) — [`groovy-build`](../groovy-build/SKILL.md).

## Read first

- [`subprojects/groovy-groovysh/ARCHITECTURE.md`](../../../subprojects/groovy-groovysh/ARCHITECTURE.md)
  — the canonical conventions this skill cites (vendored forks,
  test layers, gotchas, JLine bump procedure).
- [`subprojects/groovy-groovysh/src/spec/doc/groovysh.adoc`](../../../subprojects/groovy-groovysh/src/spec/doc/groovysh.adoc)
  — user-facing reference, source of truth for command
  behaviour.

## Top failure modes

These are the recurring mistakes specific to AI tooling working
in `groovy-groovysh`:

1. **Hallucinated JLine API names.** JLine has a large API
   surface that evolves between releases. AI tooling reaches
   for plausible-sounding method or class names. `git grep` the
   identifier in the working tree or the upstream JLine source
   before depending on it.

2. **Auto-detecting `Terminal` in tests.**
   `TerminalBuilder.builder().build()` probes the JVM's TTY and
   may invoke native bindings. Use
   `dumb(true).streams(...)` instead — see
   [Test infrastructure](../../../subprojects/groovy-groovysh/ARCHITECTURE.md#test-infrastructure).

3. **Full-string assertions on terminal output.** ANSI escapes,
   capability probes, and JLine layout shifts make full-string
   compares brittle. Prefer `printer.output` for printer-based
   tests, `terminalOutput()` for terminal-side capture, and
   substring assertions over equality. See
   [Subproject-specific gotchas](../../../subprojects/groovy-groovysh/ARCHITECTURE.md#subproject-specific-gotchas).

4. **Fabricating fork content when syncing.** AI tooling, when
   asked to sync the vendored forks against upstream, may
   "smooth over" diffs in ways that obscure our customisations.
   Diff against the *fork-base* tag and preserve our changes
   explicitly — see
   [Vendored JLine files](../../../subprojects/groovy-groovysh/ARCHITECTURE.md#vendored-jline-files).

5. **Treating vendored forks as independent files.** The five
   small forks reference `GroovyEngine.Format` etc. Don't
   delete or move one without re-deriving the coupling.

6. **Reformatting JLine fork files outside the change.** The
   forks are derived from upstream BSD-licensed sources; their
   formatting often differs from project house style. Don't
   reformat — it makes future upstream-sync diffs unreadable.
   Same as the project-wide "what *not* to do" rule in
   [`AGENTS.md`](../../../AGENTS.md).

7. **Calling deprecated JLine APIs in new code.** `getWidth()`
   / `getHeight()` are deprecated since JLine 4.x; new code
   uses `getColumns()` / `getRows()`. AI tooling defaults to
   the older names if it's seen them in adjacent code — see
   [Subproject-specific gotchas](../../../subprojects/groovy-groovysh/ARCHITECTURE.md#subproject-specific-gotchas).

## Hand-back to a human

AI tooling working in `groovy-groovysh` produces drafts; humans
review and land. Mirrors
[`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md)'s
hand-back contract: no autonomous PR, no JIRA comment, no merge,
no `Assisted-by:` trailer on someone else's commit. For a JLine
version bump or fork sync, the hand-back artefact includes the
bump itself, the fork-sync diffs (with fork-base tag named), the
`:groovy-groovysh:test` outcome, and any deprecation warnings
surfaced.

## Validation checklist

Before declaring a groovysh change ready:

- [ ] Tests use `dumb(true).streams(...)` for any terminal they
      construct — see
      [Test infrastructure](../../../subprojects/groovy-groovysh/ARCHITECTURE.md#test-infrastructure).
- [ ] Output assertions use stable substrings, not full-string
      compares. Prefer `printer.output`; for terminal-side use
      `terminalOutput()`; for Posix commands use the context's
      `out` buffer with `--color=never` where applicable.
- [ ] No locale-, platform-, or width-dependent assumptions —
      see
      [Test-writing pitfalls](../../../CONTRIBUTING.md#test-writing-pitfalls).
- [ ] `@AfterEach` closes any terminal the test constructed.
- [ ] Network/Maven tests are gated by `-Djunit.network=true`.
- [ ] No new calls to deprecated JLine APIs
      (`getWidth`/`getHeight`).
- [ ] If a fork file was synced, the diff against fork-base
      distinguishes our changes from upstream — see
      [Vendored JLine files](../../../subprojects/groovy-groovysh/ARCHITECTURE.md#vendored-jline-files).
- [ ] Verified JLine API names exist (`git grep` or upstream
      source) — no hallucinated identifiers.
- [ ] No fork files reformatted outside the change.
- [ ] Commit message references `GROOVY-NNNNN` where
      applicable; AI provenance trailer per
      [`AGENTS.md`](../../../AGENTS.md) if AI assisted.

## References

- [`subprojects/groovy-groovysh/ARCHITECTURE.md`](../../../subprojects/groovy-groovysh/ARCHITECTURE.md)
  — canonical subproject architecture and conventions.
- [`subprojects/groovy-groovysh/AGENTS.md`](../../../subprojects/groovy-groovysh/AGENTS.md)
  — subproject AI-tooling pointer file.
- [`subprojects/groovy-groovysh/src/spec/doc/groovysh.adoc`](../../../subprojects/groovy-groovysh/src/spec/doc/groovysh.adoc)
  — user-facing reference.
- [`subprojects/groovy-groovysh/LICENSE`](../../../subprojects/groovy-groovysh/LICENSE)
  — provenance for the BSD-licensed vendored files.
- `.agents/skills/groovy-tests/SKILL.md` — sister skill for
  test conventions; load alongside.
- `.agents/skills/groovy-fix-workflow/SKILL.md` — AI guardrails
  for the surrounding fix workflow.
- [`AGENTS.md`](../../../AGENTS.md) — root agent guide.
