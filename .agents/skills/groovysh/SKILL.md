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
description: Guidance for changes in subprojects/groovy-groovysh/ — REPL command implementations, JLine integration, vendored fork files, and the layered terminal-aware test stack. Use when modifying anything in that subproject's tree, bumping the JLine version, or syncing the vendored fork files against upstream.
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: subproject-groovy-groovysh
---

# groovysh

Use this skill for work in the `subprojects/groovy-groovysh/` tree —
the interactive Groovy REPL. The subproject is unusual in two ways:

1. It vendors files from JLine because we can't depend on the upstream
   `jline-groovy` artifact (circular dependency on Groovy itself).
2. Its tests touch a real JLine `Terminal`, making them more
   platform-sensitive than the rest of the codebase.

This skill layers on top of [`groovy-tests`](../groovy-tests/SKILL.md) —
load both together when adding tests for groovysh code.

## When to use this skill

**Use it for:**

- REPL command changes (`subprojects/groovy-groovysh/src/main/groovy/.../jline/`).
- Anything terminal-related: writing tests, integrating JLine APIs, image rendering.
- Bumping the JLine version in `versions.properties`.
- Syncing vendored fork files against upstream JLine.

**Don't use it for:**

- Pure compiler/runtime changes elsewhere — use [`groovy-internals`](../groovy-internals/SKILL.md).
- Changes to the project-wide build (root `build.gradle`, `build-logic/`) — use [`groovy-build`](../groovy-build/SKILL.md).

## Read first

- [`subprojects/groovy-groovysh/src/spec/doc/groovysh.adoc`](../../../subprojects/groovy-groovysh/src/spec/doc/groovysh.adoc) — user-facing reference, the source of truth for command behaviour.
- The test support classes under `subprojects/groovy-groovysh/src/test/.../commands/`: `ConsoleTestSupport` (engine + console + printer) and `SystemTestSupport` (adds dumb terminal + system registry).

## Vendored JLine files

Five BSD-licensed files under `src/main/groovy/.../jline/` (`GroovyEngine.java`,
`PackageHelper.java`, `JrtJavaBasePackages.java`, `ObjectInspector.groovy`,
`Utils.groovy`) are forks of JLine sources, kept in-tree because the
upstream artifact (`org.jline:jline-groovy`) depends on `org.apache.groovy:groovy`
and would create a circular dependency. `GroovyPosixCommands.java` is
similarly derived but diverged enough to be Apache-licensed.

If our customisations are merged upstream, the goal is to delete the
in-tree forks and depend on the upstream artifact instead. Until then,
treat the forks as code we own — but check the upstream version when
bumping JLine, in case there are fixes worth picking up.

## Test layers

Three layers of decreasing portability — prefer the lowest one that
demonstrates the property under test:

1. **Engine** — `GroovyEngine` directly; no terminal, no registry. See `GroovyEngineTest`.
2. **Registry** — `GroovySystemRegistry` over a dumb terminal. See `GroovySystemRegistryTest`.
3. **Command** — full `SystemTestSupport` stack. See `DelTest`, `ImportTest` for printer-based assertions; `HelpCommandTest` for the `terminalOutput()` capture pattern when a builtin writes through `terminal.writer()` instead of the printer.

## Top failure modes

1. **`TerminalBuilder.builder().build()` in a test.** Auto-detects the
   JVM's TTY and may probe native bindings. Use
   `TerminalBuilder.builder().dumb(true).streams(...).build()` instead;
   `SystemTestSupport` already does this.
2. **Asserting on full terminal output strings.** Prefer
   `printer.output` — the `DummyPrinter` captures `object.toString()`,
   bypassing ANSI rendering. For JLine builtins that write through
   `terminal.writer()` instead (e.g. `/help`), use
   `SystemTestSupport.terminalOutput()` and assert on stable
   substrings, never on full-string compares. The dumb terminal still
   emits capability-probe escapes (`\e[?2027$p\e[c`) at startup, and
   JLine layout/spacing shifts between releases.
3. **Treating the vendored forks as independent.** They are tightly
   coupled to the `GroovyEngine` deep fork — the small files reference
   `GroovyEngine.Format` etc. Don't delete one without re-deriving the
   coupling.
4. **Confusing "we changed it" vs "upstream changed it".** When
   syncing forks, diff against the *fork-base* tag (the JLine version
   we originally forked from), not just current upstream. Otherwise
   our renames look like upstream additions.
5. **Network/Maven tests without `-Djunit.network=true` gating.**
   `/grab` and similar pull from the network; they must be opt-in.
6. **Hard-coded or implicit terminal width.** The dumb terminal
   reports columns/rows of 0. If a test cares, set
   `terminal.size = new Size(120, 40)` explicitly.
7. **Calling `getWidth()`/`getHeight()` after a JLine bump.**
   Deprecated since JLine 4.x; use `getColumns()`/`getRows()`.
8. **`/grep` and similar Posix commands emit ANSI match highlights
   by default.** The colour decision is per-command, not per-terminal,
   so a dumb terminal doesn't suppress it. When unit-testing, pass
   `--color=never` (or strip ANSI from the captured output) so
   substring assertions match contiguously.
9. **Assuming `/save <file>` captures variable assignments.** It
   serialises `engine.buffer`, which in default mode includes only
   `IMPORT|TYPE|METHOD` snippets — bare variable assignments aren't
   there. Variables enter the buffer only when interpreter mode is
   enabled (`GROOVYSH_OPTIONS[INTERPRETER_MODE_PREFERENCE_KEY] = true`).
   The no-arg `/save` form is a separate path that JSON-serialises
   `engine.sharedData` and *does* include variables. Round-trip tests
   for the file-form should exercise definitions, not bare variables.
10. **`Less` and other highlight-aware JLine constructors silently
    no-op highlighting if `ConfigurationPath` is omitted.** `Less` has
    both 3-arg `(terminal, dir, opt)` and 4-arg
    `(terminal, dir, opt, configPath)` constructors; the shorter form
    compiles without warning but produces plain-text output. When
    forking or wrapping these commands, plumb `configPath` through and
    verify with a round-trip test (e.g. `GroovyPosixContextTest`). User-
    visible symptom: plain text where coloured tokens are expected. We
    hit this with `/less` after forking `PosixCommands`; `/nano` stayed
    correct because it routes through `Commands.nano(...)`, which takes
    `configPath` as a required argument.

(Locale/platform/format brittleness is covered by the project-wide
[`groovy-tests`](../groovy-tests/SKILL.md) skill — it's not unique to
groovysh, though terminal-aware tests are particularly exposed.)

## Procedure for a JLine version bump

1. Bump `versions.properties:jline=...`.
2. Run `:groovy-groovysh:test`.
3. Compile-scan for new deprecation warnings; fix at the call site.
4. Diff each vendored fork against the new upstream tag. Pick up
   substantive upstream fixes; skip cosmetic noise.
5. Update this skill if any finding above changed.

## Validation checklist

Before declaring a groovysh change ready:

- [ ] Tests use `dumb(true).streams(...)` for any terminal they construct.
- [ ] Output assertions use stable substrings, not full-string compares. Prefer `printer.output`; for terminal-side use `terminalOutput()`; for Posix commands use the context's `out` buffer with `--color=never` where applicable.
- [ ] No locale-, platform-, or width-dependent assumptions.
- [ ] `@AfterEach` closes any terminal the test constructed.
- [ ] Network/Maven tests are gated by `-Djunit.network=true` or skipped.
- [ ] No new calls to deprecated JLine APIs (`getWidth`/`getHeight`).
- [ ] If a fork file was synced, the diff against fork-base distinguishes our changes from upstream.

## References

- [`subprojects/groovy-groovysh/AGENTS.md`](../../../subprojects/groovy-groovysh/AGENTS.md) — subproject pointer file that loads this skill.
- [`subprojects/groovy-groovysh/src/spec/doc/groovysh.adoc`](../../../subprojects/groovy-groovysh/src/spec/doc/groovysh.adoc) — user-facing reference.
- [`subprojects/groovy-groovysh/LICENSE`](../../../subprojects/groovy-groovysh/LICENSE) — provenance for the BSD-licensed vendored files.
- [`groovy-tests`](../groovy-tests/SKILL.md) — sister skill for test conventions; load alongside this one.
- [`AGENTS.md`](../../../AGENTS.md) — root agent guide.
