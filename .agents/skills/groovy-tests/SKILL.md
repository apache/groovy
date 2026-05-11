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
name: groovy-tests
description: Guidance for writing and modifying tests in the Apache Groovy repository — JUnit 5 conventions, JIRA regression tests, the executable-AsciiDoc-spec pattern, and how to run targeted Gradle test tasks. Use when adding a regression test, documenting a feature with an executable example, or modifying anything under src/test/, src/spec/test/, or a subproject's src/test/ or src/spec/test/.
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: testing-and-documentation-examples
---

# Groovy tests

Use this skill for test work in the apache/groovy repository — adding
a JIRA regression test, modifying an existing test, or adding or
changing an executable example in the user-facing AsciiDoc.

## When to use this skill

**Use it for:**

- Writing a regression test for a JIRA-tracked bug.
- Adding or changing a tagged code snippet under `src/spec/test/` (or a subproject's `src/spec/test/`) that an AsciiDoc file `include::`'s.
- Modifying any test under `src/test/`, `subprojects/<module>/src/test/`, or `subprojects/tests-preview/src/test/`.
- Choosing which Gradle test task to run for fastest feedback.

**Don't use it for:**

- Pure compiler/runtime changes — use the `groovy-internals` skill;
  this skill complements it for the test side.
- User-facing AsciiDoc prose that doesn't include any executable
  example.
- Build-script changes to `build.gradle` test configuration.

## Read first

Read the "Tests" section in
[`CONTRIBUTING.md`](../../../CONTRIBUTING.md) and the test layout in
[`ARCHITECTURE.md`](../../../ARCHITECTURE.md). This skill is the
working surface; those documents are the map.

## Top failure modes

These are the recurring mistakes when working with Groovy tests:

1. **Reaching for Spock by reflex.** New tests in this repo are JUnit 5 (`org.junit.jupiter.api.Test`, `Assertions.*`). Spock is bundled and works, but the core repo's own tests are not generally Spock-based. Don't import `spock.lang.Specification` in a new test class without a specific reason.
2. **Wrong regression-test naming.** For new standalone test classes, use `Groovy<NNNN>` (e.g. `Groovy11955.groovy`) — drop the older `Bug` / `Test` suffix. Follow-on tests on the same JIRA append `pt2`, `pt3`, not `_2` or `V2`. New JUnit 5 test methods drop the `test` prefix that older JUnit required — `void someBehaviour()`, not `void testSomeBehaviour()`. When adding a method to an existing class that uses the older prefixed style, match the surrounding file.
3. **Forcing a standalone test when the regression fits with existing tests.** If the bug logically belongs in an existing test class, add a `@Test` method there with a `// GROOVY-<NNNN>` comment on the line above so the JIRA stays searchable. Use the standalone `Groovy<NNNN>` shape only when there isn't an obvious home.
4. **Regression test that doesn't reproduce on master.** Run the new test against `master` *before* applying the fix. If it passes, the test isn't exercising the bug — fix the test, not the symptom.
5. **Mismatched tag and include.** The tag name in the AsciiDoc `include::../test/X.groovy[tags=foo,...]` must match `// tag::foo[]` / `// end::foo[]` in the Groovy test file. Renaming one without the other silently breaks the include and the build doesn't fail loudly.
6. **Editing the AsciiDoc but not the matching tagged snippet (or vice versa).** Documentation examples are dual-edited: `src/spec/doc/<topic>.adoc` and `src/spec/test/<TopicTest>.groovy` change together in the same PR.
7. **Orphaned tagged regions.** A `// tag::...[] ... // end::...[]` block in `src/spec/test/` that no AsciiDoc file `include::`'s is dead weight. If you removed the include, remove the tagged region too.
8. **`./gradlew test` as the inner loop.** It builds and runs the whole core suite. Use targeted runs (`:test --tests <FQN>` or `:<subproject>:test --tests <FQN>`) for development; reserve the full run for the final pre-PR check.
9. **JDK-preview-dependent test in the wrong location.** Tests that need `--enable-preview` go in `subprojects/tests-preview/src/test/`, not core `src/test/`.
10. **Using `String.valueOf(object)` in test assertions.** It calls Java's static `String.valueOf` and bypasses Groovy MetaClass dispatch — Maps render as `{k=v}` instead of `[k:v]`, and similar mismatches hit other Groovy-flavoured collections. Use `object.toString()` so the Groovy extensions apply. (`null.toString()` returns `'null'` in Groovy, so no separate null guard is needed.)
11. **Locale-, platform-, or format-dependent assertions.** Don't bake JVM defaults into expected output — locale (number/date formatting), default timezone, line endings, file path separators, and default charset all vary across CI agents and contributor machines. Symptom: a test passes for the author and fails on a colleague's Windows box, or starts failing when CI rotates locales. Two patterns that bite repeatedly:
    - **Path strings interpolated into a parsed command line.** A Windows-native `Path.toString()` like `C:\Users\…\foo.json` interpolated into a `system.execute("cmd ${file}")`-style line gets its backslashes eaten by JLine's `DefaultParser` (which treats `\` as an escape). Forward-slash the path before interpolating: `path.toString().replace('\\', '/')`. Java NIO accepts forward-slash paths on Windows.
    - **Output captured from `PrintStream.println`.** `println` uses `System.lineSeparator()`, which is `\r\n` on Windows. Line-aware assertions (`output.split('\n')`, `output.contains('foo\n')`) silently fail on Windows. Use Groovy's `String.normalize()` extension to collapse platform line separators to `\n` before splitting/comparing.
    Other defences: `Locale.ROOT` for date/number formatting, explicit `StandardCharsets.UTF_8` rather than the platform default, or assert on parsed values rather than their stringified forms.
12. **`-Djunit.network=true` is required for any test under `groovy/grape/`.** Symptom: `:groovy-grape-*:test --tests <FQN>` reports `BUILD SUCCESSFUL` but no test results appear (and `--rerun-tasks` reports `NO-SOURCE`). The `Test` task in `org.apache.groovy-tested.gradle` applies an `exclude buildExcludeFilter(...)` filter (lines ~136, 265-278) that drops anything under `groovy/grape/` from execution unless `junit.network` is set. The test classes compile normally; they just aren't run. The plugin also warns via `gradle.taskGraph.whenReady` when this exclusion swallows a project's entire test source set — easy to miss in the log. Always pass `-Djunit.network=true` when iterating on tests in `subprojects/groovy-grape-*`.
13. **`-Djunit.network` set on the Gradle CLI doesn't reach the test JVM automatically.** Separate from the source-set filter above: if a test reads the property at runtime — e.g. gated via `@EnabledIfSystemProperty(named = 'junit.network', matches = 'true')` — the subproject's `build.gradle` has to forward it explicitly:

    ```groovy
    tasks.named('test') {
        def network = System.getProperty('junit.network')
        if (network) systemProperty 'junit.network', network
    }
    ```

    Without forwarding the gated test always skips, even with `-Djunit.network=true` on the Gradle CLI.

## Procedure for a JIRA regression test

1. **Find precedent.** `git log --grep='GROOVY-NNNN'` for the issue, and `git log --grep='GROOVY-' -- src/test/groovy/bugs/` for similar past regression-test shapes. Reading the diff and test of an analogous fix is usually the fastest orientation.
2. **Decide: standalone class or method on an existing class?**
   - If the regression fits with similar tests already in the tree, add a `@Test` method on that existing class with a `// GROOVY-<NNNN>` comment immediately above it. Match the surrounding file's method-name style.
   - Otherwise create a standalone class:
     - General bug → `src/test/groovy/bugs/Groovy<NNNN>.groovy`.
     - Bug scoped to a specific area → mirror the package
       (e.g. `src/test/groovy/org/codehaus/groovy/tools/stubgenerator/Groovy<NNNN>.groovy`).
     - Bug in a subproject → that subproject's `src/test/`.
3. **Write the test in JUnit 5.** For a new class: `final class Groovy<NNNN>`, no base class needed; use `@Test` and `Assertions.*`; method names drop the `test` prefix. Copy a recent neighbour for the exact import set.
4. **Confirm it fails on master.** Check out master, run the targeted test, watch it fail. This is the test working — it proves the bug reproduces.
5. **Apply the fix.** Run the targeted test, then the surrounding test class or package, then the full module.
6. **Reference the JIRA** in the commit message (`GROOVY-NNNNN: short description`).

## Procedure for an executable-AsciiDoc example

When adding or changing a code snippet that appears in the user-facing
documentation:

1. **Locate the test file paired with the AsciiDoc.** AsciiDoc at
   `src/spec/doc/<topic>.adoc` typically pairs with
   `src/spec/test/<TopicTest>.groovy`. Subproject docs follow the
   same pattern under their own `src/spec/`.
2. **Add or modify the tagged region** inside an `@Test` method:

   ```groovy
   @Test
   void feature() {
       // tag::feature_example[]
       def x = 'hello'
       assert x.size() == 5
       // end::feature_example[]
   }
   ```

   (Match the surrounding file's method-name style if it predates the
   no-`test`-prefix convention.)

3. **Add or update the AsciiDoc include** with a matching tag name:

   ```asciidoc
   include::../test/<TopicTest>.groovy[tags=feature_example,indent=0]
   ```

4. **Run the spec test** to confirm the snippet still compiles and runs:

   ```
   ./gradlew :test --tests <TopicTest>
   ```

5. **Eyeball the rendered AsciiDoc** for a long-form example to check the snippet appears intact.

## Validation checklist

Before declaring the change ready:

- [ ] New test classes use JUnit 5 (`org.junit.jupiter.api.Test`).
- [ ] Standalone regression test follows `Groovy<NNNN>` naming (no `Bug` / `Test` suffix); or, if added to an existing class, has a `// GROOVY-<NNNN>` comment above the new method.
- [ ] New `@Test` method names drop the `test` prefix (or match the surrounding file's older style when extending an existing class).
- [ ] Regression test was confirmed to fail on master before the fix.
- [ ] Targeted Gradle test run is green: `./gradlew :test --tests <FQN>` or `:<subproject>:test --tests <FQN>`.
- [ ] If the change touches an executable example: AsciiDoc and tagged Groovy snippet were edited in the same PR, with matching tag names.
- [ ] No orphaned `// tag::...[]` regions left behind from removed includes.
- [ ] Tests requiring `--enable-preview` live under `subprojects/tests-preview/`.
- [ ] Commit message references `GROOVY-NNNNN` where applicable.

## References

- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — "Tests" section: targeted runs, regression-test conventions, executable-AsciiDoc workflow.
- [`ARCHITECTURE.md`](../../../ARCHITECTURE.md) — overall test layout (core, spec, subprojects, tests-preview).
- [`COMPATIBILITY.md`](../../../COMPATIBILITY.md) — what API-affecting tests need to demonstrate.
- [`AGENTS.md`](../../../AGENTS.md) — overall AI-contributor guidance.
- `.agents/skills/groovy-internals/SKILL.md` — sister skill for compiler/runtime changes; pair it with this skill when fixing a compiler bug that needs a regression test.
