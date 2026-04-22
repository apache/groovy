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

This file **supplements** — it does not replace — `CONTRIBUTING.md`, the
project website at <https://groovy.apache.org/>, and `README.adoc`. Human
contributors should keep reading those; they remain the authoritative sources.

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
- **Attribute generated work in commits.** When AI tooling authored a
  non-trivial portion of a change, add a trailer to the commit message,
  for example: `Generated-by: <tool name and version>`. This aligns with
  the ASF's recommendation on AI provenance tracking.
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

- Match the surrounding file's style (indentation, import ordering, brace
  placement). Groovy source uses 4-space indent, no tabs; see `.editorconfig`.
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

- Don't rewrite files "for consistency" outside the scope of the task.
- Don't invent APIs, flags, or methods; verify they exist (Groovy is a large
  codebase and hallucinated identifiers are a common failure mode).
- Don't add speculative abstractions, configuration knobs, or
  backwards-compatibility shims the task doesn't call for.
- Don't commit generated scratch files (answers.*, patches, HTML reports, etc.)
  — keep the working tree clean.

## Where to ask

- Developer list: <dev@groovy.apache.org>
- Slack: the Apache Groovy channel on the ASF Slack workspace
- Issue tracker: <https://issues.apache.org/jira/browse/GROOVY>
