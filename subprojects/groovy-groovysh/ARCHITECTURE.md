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

# Apache Groovy — `groovy-groovysh` Subproject Architecture

A contributor-facing map of the `subprojects/groovy-groovysh/`
tree — the interactive Groovy REPL. This document is for people
working *on* groovysh. For documentation aimed at people *using*
groovysh, see [`src/spec/doc/groovysh.adoc`](src/spec/doc/groovysh.adoc).

The subproject is unusual in two ways:

1. **It vendors files from JLine.** We can't depend on the
   upstream `jline-groovy` artifact because it depends on Groovy
   itself, which would create a circular build dependency.
2. **Its tests touch a real JLine `Terminal`.** That makes them
   more platform-sensitive than the rest of the codebase —
   terminal capabilities, ANSI rendering, locale, and width all
   bite here in ways they don't in most subprojects.

This document covers what those quirks mean in practice.

## Vendored JLine files

Five BSD-licensed files under `src/main/groovy/.../jline/` are
forks of JLine sources:

- `GroovyEngine.java` — the deep fork; large and actively
  maintained.
- `PackageHelper.java`
- `JrtJavaBasePackages.java`
- `ObjectInspector.groovy`
- `Utils.groovy`

Plus `GroovyPosixCommands.java`, similarly derived but diverged
enough from upstream that it is Apache-licensed in-tree.

These are kept in-tree because the upstream artefact
(`org.jline:jline-groovy`) depends on `org.apache.groovy:groovy`,
which would create a circular dependency. If our customisations
are merged upstream, the goal is to delete the in-tree forks and
depend on the upstream artefact instead. Until then, treat the
forks as code we own — but check the upstream version when
bumping JLine, in case there are fixes worth picking up.

Provenance for the BSD-licensed vendored files is in
[`LICENSE`](LICENSE).

**Syncing the forks with upstream:** diff against the *fork-base*
tag (the JLine version we originally forked from), not just
current upstream. Otherwise our renames look like upstream
additions. The small files are tightly coupled to the
`GroovyEngine` deep fork — they reference `GroovyEngine.Format`
etc. — so don't delete one without re-deriving the coupling.

## Test infrastructure

Three layers of decreasing portability — prefer the lowest one
that demonstrates the property under test:

1. **Engine layer** — `GroovyEngine` directly; no terminal, no
   registry. See `GroovyEngineTest`. Fastest, most portable.
2. **Registry layer** — `GroovySystemRegistry` over a dumb
   terminal. See `GroovySystemRegistryTest`.
3. **Command layer** — full `SystemTestSupport` stack (engine +
   console + printer + dumb terminal + system registry). See
   `DelTest`, `ImportTest` for printer-based assertions;
   `HelpCommandTest` for the `terminalOutput()` capture pattern
   used when a builtin writes through `terminal.writer()` instead
   of the printer.

Support classes under `src/test/.../commands/`:

- `ConsoleTestSupport` — engine + console + printer.
- `SystemTestSupport` — adds dumb terminal + system registry.
- `DummyPrinter` — captures `object.toString()` output,
  bypassing ANSI rendering.

`SystemTestSupport` already constructs terminals with
`dumb(true).streams(...)` — don't reach for
`TerminalBuilder.builder().build()` in tests; it auto-detects the
JVM's TTY and may probe native bindings.

## Subproject-specific gotchas

Recurring traps when working in this tree:

- **`TerminalBuilder.builder().build()` in a test.** Auto-detects
  the JVM's TTY and may probe native bindings. Use
  `TerminalBuilder.builder().dumb(true).streams(...).build()`
  instead. `SystemTestSupport` already does this.

- **Asserting on full terminal output strings.** Prefer
  `printer.output` — the `DummyPrinter` captures
  `object.toString()`, bypassing ANSI rendering. For JLine
  builtins that write through `terminal.writer()` instead (e.g.
  `/help`), use `SystemTestSupport.terminalOutput()` and assert
  on stable substrings, never on full-string compares. The dumb
  terminal still emits capability-probe escapes
  (`\e[?2027$p\e[c`) at startup, and JLine layout/spacing
  shifts between releases.

- **Hard-coded or implicit terminal width.** The dumb terminal
  reports columns/rows of 0. If a test cares, set
  `terminal.size = new Size(120, 40)` explicitly.

- **Deprecated JLine width accessors.** `getWidth()` /
  `getHeight()` have been deprecated since JLine 4.x; use
  `getColumns()` / `getRows()`. Old call sites in this tree
  haven't all been updated — match the deprecation when adding
  new code.

- **`/grep` and similar Posix commands emit ANSI match highlights
  by default.** The colour decision is per-command, not
  per-terminal, so a dumb terminal doesn't suppress it. When
  unit-testing, pass `--color=never` (or strip ANSI from the
  captured output) so substring assertions match contiguously.

- **`/save <file>` serialises `engine.buffer`,** which in default
  mode includes only `IMPORT|TYPE|METHOD` snippets — bare
  variable assignments aren't there. Variables enter the buffer
  only when interpreter mode is enabled
  (`GROOVYSH_OPTIONS[INTERPRETER_MODE_PREFERENCE_KEY] = true`).
  The no-arg `/save` form is a separate path that
  JSON-serialises `engine.sharedData` and *does* include
  variables. Round-trip tests for the file-form should exercise
  definitions, not bare variables.

- **`Less` and other highlight-aware JLine constructors silently
  no-op highlighting if `ConfigurationPath` is omitted.** `Less`
  has both 3-arg `(terminal, dir, opt)` and 4-arg
  `(terminal, dir, opt, configPath)` constructors; the shorter
  form compiles without warning but produces plain-text output.
  When forking or wrapping these commands, plumb `configPath`
  through and verify with a round-trip test (e.g.
  `GroovyPosixContextTest`). User-visible symptom: plain text
  where coloured tokens are expected. We hit this with `/less`
  after forking `PosixCommands`; `/nano` stayed correct because
  it routes through `Commands.nano(...)`, which takes
  `configPath` as a required argument.

- **Network/Maven tests need `-Djunit.network=true`.** `/grab`
  and similar pull from the network; they must be opt-in. See
  the broader [Test-writing pitfalls](../../CONTRIBUTING.md#test-writing-pitfalls)
  in root `CONTRIBUTING.md` for the gating mechanism.

Locale-, line-ending-, and path-portability traps are covered by
the project-wide
[Test-writing pitfalls](../../CONTRIBUTING.md#test-writing-pitfalls)
— they're not unique to groovysh, though terminal-aware tests are
particularly exposed to them.

## JLine version bump procedure

1. Bump `versions.properties:jline=...`.
2. Run `:groovy-groovysh:test`.
3. Compile-scan for new deprecation warnings; fix at the call
   site.
4. Diff each vendored fork against the new upstream tag. Pick
   up substantive upstream fixes; skip cosmetic noise.
5. Update this document if any finding above changed.

## Where to read next

- [Root `ARCHITECTURE.md`](../../ARCHITECTURE.md) — project-wide
  architecture, compilation pipeline, build infrastructure.
- [Root `CONTRIBUTING.md`](../../CONTRIBUTING.md) — build,
  test, fix workflow, test-writing pitfalls, triage, JIRA
  conventions.
- [`src/spec/doc/groovysh.adoc`](src/spec/doc/groovysh.adoc) —
  the user-facing reference, source of truth for command
  behaviour.
- [`LICENSE`](LICENSE) — provenance for the BSD-licensed
  vendored files.
- [`AGENTS.md`](AGENTS.md) — subproject AI-tooling pointer.
- [`.agents/skills/groovysh/SKILL.md`](../../.agents/skills/groovysh/SKILL.md)
  — AI-tooling guardrails over the conventions in this
  document.
