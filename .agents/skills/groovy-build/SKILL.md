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
name: groovy-build
description: AI-tooling guardrails for changes to the Apache Groovy Gradle build — points at the project's build conventions in ARCHITECTURE.md (convention plugins, version flow through `gradle.properties` and the `Versions` type, dependency verification, repackaging, configuration cache, binary compatibility), then adds the AI-specific constraints on top: no fabricated DSL, no hard-coded versions, regenerate `verification-metadata.xml` after dependency changes, exercise installed builds after repackaging changes, hand-back for review. Use when editing build.gradle, build-logic/, gradle.properties, or gradle/verification-metadata.xml.
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: ai-tooling-build-guardrails
---

# Groovy build

This skill is the **AI-tooling layer** over the project's Gradle
build conventions. The conventions themselves — convention plugins
under `build-logic/`, version flow through `gradle.properties` and
the `Versions` type, dependency verification, repackaging rules,
configuration-cache discipline, binary compatibility — live in
[`ARCHITECTURE.md`'s "Build infrastructure" section](../../../ARCHITECTURE.md#build-infrastructure).
This skill cites them and adds the AI-specific guardrails: no
fabricated DSL, no hard-coded versions, no skipped verification
regeneration, exercise installed builds after repackaging
changes.

- [`groovy-internals`](../groovy-internals/SKILL.md) — pair with
  when a build change correlates with a compiler/runtime change
  (parser grammar, repackaging that affects bytecode-time
  behaviour).
- [`groovy-tests`](../groovy-tests/SKILL.md) — pair with when a
  build change affects test-task configuration.
- [`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md) — pair
  with for the surrounding hand-back contract; build changes use
  the same artefact shape.
- [`ARCHITECTURE.md`](../../../ARCHITECTURE.md) — the canonical
  source for build conventions this skill cites; load alongside.
- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — for the
  canonical command sequence and "Running your local build"
  instructions.

## When to use this skill

**Use it for:**

- Convention plugin changes under
  `build-logic/src/main/groovy/org.apache.groovy-*.gradle`.
- Root `build.gradle` or `settings.gradle` edits.
- Subproject `build.gradle` edits where the change is
  build-shape, not source.
- `gradle.properties` edits (versions, target bytecode, build
  flags).
- Adding, removing, or upgrading a runtime or test dependency,
  including the matching `gradle/verification-metadata.xml`
  regeneration.
- Repackaging rules in
  `groovyLibrary { repackagedDependencies = ... }`.
- OSGi manifest, JaCoCo aggregation, license-report,
  dep-updates, Develocity / build-scans, signing and publishing
  wiring.
- `subprojects/binary-compatibility/` configuration and
  accepted-API-changes files.
- Gradle wrapper version bumps
  (`gradle/wrapper/gradle-wrapper.properties`).

**Don't use it for:**

- Compiler, parser, type-checker, AST, or runtime source — that's
  [`groovy-internals`](../groovy-internals/SKILL.md).
- Adding a regression test or executable AsciiDoc example —
  [`groovy-tests`](../groovy-tests/SKILL.md).
- AsciiDoc prose under `src/spec/doc/` that has no build
  implication.

## Read first

- [`ARCHITECTURE.md`](../../../ARCHITECTURE.md) "Build
  infrastructure" — the canonical build conventions this skill
  cites.
- [`README.adoc`](../../../README.adoc) "Building" — canonical
  build instructions.
- [`COMPATIBILITY.md`](../../../COMPATIBILITY.md) — stability
  tiers, what counts as a breaking change, the
  binary-compatibility check.

## Top failure modes

These are the recurring mistakes specific to AI tooling on build
changes:

1. **Fabricating Gradle DSL.** Reaching for plausible-sounding
   extension names or task types that don't exist in this build.
   The convention plugins under
   `build-logic/src/main/groovy/org.apache.groovy-*.gradle` are
   the source of truth — read the actual extension type before
   writing DSL. `grep -r "groovyLibrary" build-logic/` finds the
   real shape.

2. **Adding a dependency without regenerating
   `verification-metadata.xml`.** This breaks the build. AI
   tooling often forgets the regeneration step. Run
   `./gradlew --write-verification-metadata sha256,pgp help`
   after any dependency change and inspect the diff before
   committing.

3. **Hard-coding versions.** Versions live in `gradle.properties`
   and the `Versions` type under
   `build-logic/src/main/groovy/org/apache/groovy/`, not as
   `'group:artifact:1.2.3'` literals in subproject build files.
   See
   [Build infrastructure](../../../ARCHITECTURE.md#build-infrastructure).

4. **Touching `repackagedDependencies` without an installed-build
   smoke test.** A wrong repackaging rule produces a jar that
   compiles fine but blows up at runtime. After any repackaging
   change, run `./gradlew :groovy-binary:installGroovy` and
   exercise `groovy` / `groovyc` against a non-trivial script.

5. **Editing generated ANTLR sources to "fix" a parser problem.**
   `build/generated/sources/antlr4/...` is regenerated on every
   build; the grammar lives in `src/antlr/*.g4`. See
   [Generated code](../../../ARCHITECTURE.md#generated-code).

6. **Adding a new runtime dependency without flagging dev@
   discussion.** ASF rules apply: new runtime dependencies need
   discussion on dev@ and may require `NOTICE` / `LICENSE`
   updates. AI tooling drafts the dependency change; the
   contributor decides whether to send the dev@ message.

7. **Suppressing binary-compatibility failures.** AI tooling can
   reach for `excludeClass` / suppression as the path of least
   resistance to green CI. Don't — either justify the change in
   the accepted-changes file or revert the API breakage. See
   [`COMPATIBILITY.md`](../../../COMPATIBILITY.md).

8. **Breaking the configuration cache.** AI tooling reaches for
   eager `Project` access (`project.foo` at execution time)
   instead of the provider chains the build expects. See
   [Build infrastructure](../../../ARCHITECTURE.md#build-infrastructure).

9. **Wrapper version bumps without Develocity compatibility
   check.** A wrapper bump can disable build scans or break a
   plugin pinned in the root `plugins {}` block. See
   [Build infrastructure](../../../ARCHITECTURE.md#build-infrastructure).

10. **Reformatting a build script outside the change.** Same
    review-culture rule as code: drive-by reformatting hides
    intent and is rejected. See the "what *not* to do" list in
    [`AGENTS.md`](../../../AGENTS.md).

## Procedure

1. **Classify the scope.** Convention plugin (cross-cutting),
   subproject (local), or root/`settings.gradle`
   (project-wide). State which and stay there.

2. **Read the relevant convention plugin first.** Skim the
   `org.apache.groovy-*` plugin(s) the affected subproject
   applies, plus `Services.groovy` / `SharedConfiguration` /
   `Versions`. Don't guess the DSL.

3. **Make the smallest correct change.** Per
   [Build infrastructure](../../../ARCHITECTURE.md#build-infrastructure).

4. **For dependency changes, regenerate verification metadata:**

   ```
   ./gradlew --write-verification-metadata sha256,pgp help
   ```

   Inspect the diff; commit only the entries your change
   actually introduced.

5. **Run targeted → subproject → full** — see
   [Targeted runs](../../../CONTRIBUTING.md#targeted-runs):

   ```
   ./gradlew :<subproject>:<task>
   ./gradlew :<subproject>:check
   ./gradlew build
   ```

6. **For repackaging or distribution changes, install and
   exercise.** Code-level tests don't catch a broken `groovy` /
   `groovyc` launcher:

   ```
   ./gradlew :groovy-binary:installGroovy
   ```

   See
   ["Running your local build"](../../../CONTRIBUTING.md#running-your-local-build).

7. **For API-affecting changes, run binary compatibility
   explicitly** — `./gradlew :binary-compatibility:check`.

8. **For config-cache or caching investigations, capture a build
   scan** — `./gradlew --scan <task>`.

## Hand-back to a human

AI tooling produces build changes; humans review and land. The
hand-back artefact mirrors
[`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md): branch,
commit, gradle commands and outcomes, dependency-verification
regeneration diff (if applicable), installed-build smoke-test
outcome (if repackaging was touched), any cross-repo follow-up
(dev@ discussion for runtime deps, `NOTICE` / `LICENSE` updates),
open questions for the committer.

## Validation checklist

Before declaring the change ready:

- [ ] Change is in the right scope (convention plugin for
      cross-cutting; subproject build for local; root for
      project-wide) — see
      [Build infrastructure](../../../ARCHITECTURE.md#build-infrastructure).
- [ ] No hard-coded versions in subproject build files.
- [ ] Dependency changes are reflected in
      `gradle/verification-metadata.xml`; only the affected
      entries are touched.
- [ ] No edits to files under `build/generated/`.
- [ ] No formatting changes outside the lines that needed to
      change.
- [ ] If repackaging or distribution changed:
      `:groovy-binary:installGroovy` produced a working install,
      exercised against a non-trivial script.
- [ ] `./gradlew :<subproject>:check` passes locally.
- [ ] `./gradlew build` passes — including
      `:binary-compatibility:check` — or any API breakage is
      justified in the accepted-changes file.
- [ ] New runtime dependencies have had dev@ discussion flagged
      in the hand-back artefact; `NOTICE` / `LICENSE` updates in
      the same change if needed.
- [ ] Commit message references `GROOVY-NNNNN` where applicable;
      AI provenance trailer per
      [`AGENTS.md`](../../../AGENTS.md) policy.

## References

- [`ARCHITECTURE.md`](../../../ARCHITECTURE.md) "Build
  infrastructure" — canonical build conventions.
- [`COMPATIBILITY.md`](../../../COMPATIBILITY.md) — stability
  tiers, binary-compatibility check.
- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — build, test,
  submission process; "Running your local build".
- [`README.adoc`](../../../README.adoc) — canonical "Building"
  instructions.
- [`AGENTS.md`](../../../AGENTS.md) — AI-contributor guidance
  and ASF provenance rules.
- `build-logic/src/main/groovy/org.apache.groovy-*.gradle` —
  convention plugins (source of truth for subproject shape).
- `gradle/verification-metadata.xml` — dependency verification.
- `subprojects/binary-compatibility/` — accepted-changes files.
- `.agents/skills/groovy-internals/SKILL.md`,
  `.agents/skills/groovy-tests/SKILL.md`,
  `.agents/skills/groovy-fix-workflow/SKILL.md` — pair with as
  applicable.
