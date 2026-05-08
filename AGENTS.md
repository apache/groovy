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

# Agent Guide for Apache Groovy

Supplemental guidance for AI coding assistants (Claude Code, Codex, Cursor,
Copilot, Gemini, Aider, and similar tools) contributing to Apache Groovy.

This file **supplements** — it does not replace — the human-facing
contributor docs at the repository root:
[`README.adoc`](README.adoc),
[`CONTRIBUTING.md`](CONTRIBUTING.md),
[`ARCHITECTURE.md`](ARCHITECTURE.md),
[`COMPATIBILITY.md`](COMPATIBILITY.md), and
[`GOVERNANCE.md`](GOVERNANCE.md). Those, together with the project
website at <https://groovy.apache.org/>, remain the authoritative
sources; this file just layers AI-specific guidance on top.

## Licensing and provenance (read first)

Apache Groovy is licensed under Apache License 2.0. Contributions must meet
the ASF's [Generative Tooling guidance](https://www.apache.org/legal/generative-tooling.html).
In particular:

- **Do not copy verbatim from incompatibly-licensed sources.** This includes
  GPL / AGPL / LGPL code, proprietary code, unlicensed snippets, and
  Stack Overflow / blog / forum excerpts whose licensing is unclear.
  Reimplement from specifications, standards, or Apache-compatible sources
  (see the [ASF 3rd Party Licensing Policy](https://www.apache.org/legal/resolved.html)).
- **Every new source file needs the ASF license header.** See any existing
  `.java` or `.groovy` file for the canonical form.
- **Attribute AI assistance in commits.** When AI tooling assisted on
  a change, consider adding an `Assisted-by:` trailer naming the
  tool(s) — for example:

  ```
  Assisted-by: <tool name and version>
  ```

  `Assisted-by:` is the default and reflects the ASF's stance that a
  human contributor performs the final check on every change.
  `Co-authored-by:` is conventionally used for human co-authors.
  `Generated-by:` is reserved for special cases where AI tooling
  produced a change with minimal human modification. The ASF's
  [Generative Tooling guidance](https://www.apache.org/legal/generative-tooling.html)
  is the authoritative source — the wording above reflects the
  emerging consensus from the ASF AI working group, but follow the
  guidance page if the two diverge.
- **The contributor remains responsible for what they submit.** Review
  generated output for licensing, correctness, and style before committing.

## Build and test

Canonical instructions live in `README.adoc` (see the "Building" section).
The short form:

```
./gradlew clean dist        # full build
./gradlew test              # run tests
./gradlew :test --tests <TestClassName>
```

JDK 17+ is required. Use the Gradle wrapper (`./gradlew` / `gradlew.bat`);
do not invoke a system `gradle`.

## Coding conventions

Follow what's already in the tree. Specifically:

- Match the surrounding file's existing style. Groovy source uses
  4-space indent, no tabs; see `.editorconfig`.
- Prefer the narrowest scope. Do not add public API surface unless the change
  requires it; public API is covenanted and hard to remove.
- Do not introduce new runtime dependencies without discussion — every new
  dependency needs a license review and potentially a `NOTICE` / `LICENSE` update.
- Remove unused imports and dead code you introduce.

For API/behaviour changes, add or update tests alongside the code change.

## Commits, PRs, and issue references

- Reference the JIRA issue in commit messages where applicable, e.g.
  `GROOVY-12345: short description`. Project issues are tracked at
  <https://issues.apache.org/jira/browse/GROOVY>.
- Keep commits focused. A bug fix, a refactor, and a formatting pass are
  three separate commits (or PRs), not one.
- Run the build locally before opening a PR.

## What *not* to do

- Don't reformat code outside the lines your change actually touches,
  even if the surrounding style differs from your or the project's
  preferred style elsewhere. Drive-by reformatting hides real changes
  in review and is rejected by this project's review culture.
- Don't rewrite files "for consistency" outside the scope of the task.
- Don't invent APIs, flags, or methods; verify they exist (Groovy is a large
  codebase and hallucinated identifiers are a common failure mode).
- Don't add speculative abstractions, configuration knobs, or
  backwards-compatibility shims the task doesn't call for.
- Don't commit generated scratch files (answers.*, patches, HTML reports, etc.)
  — keep the working tree clean.

## Skills

Task-specific guidance lives under [`.agents/skills/`](.agents/skills/),
each in its own directory with a `SKILL.md` describing when to use
it, the recurring failure modes for that area, and a validation
checklist. Load the relevant skill *before* writing or modifying code
in its area — the skill is more focused than this file and points
into the human-facing docs above.

| Skill | Use for |
|---|---|
| [`groovy-build`](.agents/skills/groovy-build/SKILL.md) | Gradle build changes — convention plugins, build files, dependency verification, ASM/ANTLR repackaging, OSGi, release pipeline |
| [`groovy-internals`](.agents/skills/groovy-internals/SKILL.md) | Compiler and runtime work — parser, AST, type checker, transforms, class generation |
| [`groovy-tests`](.agents/skills/groovy-tests/SKILL.md) | Adding or modifying tests, including JIRA regression tests and executable AsciiDoc examples |
| [`groovysh`](.agents/skills/groovysh/SKILL.md) | Work in `subprojects/groovy-groovysh/` — REPL commands, JLine integration, vendored forks, terminal-aware test stack |

## Subproject guides

Some subprojects have their own `AGENTS.md` with content specific to
that module — additional architecture, test infrastructure, or
conventions that don't apply elsewhere. Load the relevant subproject's
guide when working in its directory tree.

| Subproject | Scope |
|---|---|
| [`groovy-groovysh`](subprojects/groovy-groovysh/AGENTS.md) | Interactive REPL, JLine integration, vendored forks, terminal-aware test stack |

## Where to ask

- Developer list: <dev@groovy.apache.org>
- Slack: the Apache Groovy channel on the ASF Slack workspace
- Issue tracker: <https://issues.apache.org/jira/browse/GROOVY>
