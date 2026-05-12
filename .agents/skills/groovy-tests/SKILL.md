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
description: AI-tooling guardrails for writing and modifying tests in the Apache Groovy repository — points at the project's test conventions in CONTRIBUTING.md (JUnit 5 framework, `Groovy<NNNN>` regression-test naming, executable-AsciiDoc pattern, targeted runs, test-writing pitfalls), then adds the AI-specific constraints on top: no fabricated assertions, no `assert false` theatre, regression tests that actually fail on master before the fix, no scratch files left in the working tree, hand-back for human review. Use when writing or modifying tests under src/test/, src/spec/test/, or a subproject's src/test/.
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: ai-tooling-test-guardrails
---

# Groovy tests

This skill is the **AI-tooling layer** over the project's test
conventions. The conventions themselves — JUnit 5 framework, the
`Groovy<NNNN>` regression-test naming, the executable-AsciiDoc
pattern, targeted Gradle runs, and the platform-portability
pitfalls — live in
[`CONTRIBUTING.md`'s "Tests" section](../../../CONTRIBUTING.md#tests).
This skill cites them and adds the AI-specific guardrails: no
fabricated assertions, regression tests that actually exercise the
bug, no scratch files left behind, hand-back for review.

- [`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md) — pair
  with when the test is part of a JIRA-tracked fix; that skill
  owns the AI guardrails for the surrounding fix workflow.
- [`groovy-internals`](../groovy-internals/SKILL.md) — pair with
  when the test exercises the compiler/runtime.
- [`groovy-reproducer`](../groovy-reproducer/SKILL.md) — pair with
  when the test starts as an extracted reproducer.
- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — the canonical
  source for every test convention this skill cites; load
  alongside.

## When to use this skill

**Use it for:**

- Writing or modifying a regression test for a JIRA-tracked bug.
- Adding or modifying an executable AsciiDoc example.
- Modifying any test under `src/test/`,
  `subprojects/<module>/src/test/`, or
  `subprojects/tests-preview/src/test/`.
- Choosing which Gradle test task to run for fastest feedback.

**Don't use it for:**

- Pure compiler/runtime changes —
  [`groovy-internals`](../groovy-internals/SKILL.md) is the home;
  this skill complements it for the test side.
- User-facing AsciiDoc prose that doesn't include any executable
  example.
- Build-script changes to `build.gradle` test configuration —
  [`groovy-build`](../groovy-build/SKILL.md) is the home.

## Read first

- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) "Tests" — the
  canonical conventions this skill cites (framework, naming,
  AsciiDoc pattern, targeted runs, pitfalls).
- [`ARCHITECTURE.md`](../../../ARCHITECTURE.md) "Tests" — the
  test-layout map.
- [`COMPATIBILITY.md`](../../../COMPATIBILITY.md) — what
  API-affecting tests need to demonstrate.

## Top failure modes

These are the recurring mistakes specific to AI tooling writing
tests:

1. **Regression test that passes by construction.** Writing the
   test after the fix, with assertions that mirror the fix rather
   than reproducing the bug. The test must fail on `master`
   before the fix is applied — see
   [Fix workflow](../../../CONTRIBUTING.md#fix-workflow).
   Quickest proof: stash the production change, run the targeted
   test, watch it fail; pop, run, watch it pass.

2. **Hallucinated assertions.** Asserting on output the code
   doesn't actually produce. Run the test and check the actual
   output before locking in `assertEquals("x", actual)`.

3. **`assert false // bug` to "force a failure".** That tells you
   nothing about the underlying behaviour. The test should
   exercise the same path the bug report describes and let it
   fail naturally; `assert <reporter-expected>` is fine,
   `assert false` is theatre.

4. **Ignoring the platform-portability pitfalls.** Locale, line
   endings, Windows path escaping, the `junit.network` gate are
   all covered in
   [Test-writing pitfalls](../../../CONTRIBUTING.md#test-writing-pitfalls).
   AI tooling tends to hard-code platform defaults; check before
   asserting.

5. **Scratch test files left in the working tree.** When running
   reproducers or experimenting, AI tooling can leave
   half-written test files behind. `git status` before declaring
   done; the working tree at hand-back contains only the
   intentional changes.

6. **Reaching for Spock by reflex.** New tests in this repo are
   JUnit 5 — see
   [Test framework](../../../CONTRIBUTING.md#test-framework).
   Spock works but isn't the house style; don't import
   `spock.lang.Specification` in a new test class without a
   specific reason.

## Procedure

When writing or modifying a test:

1. **Identify the shape.** Regression test (standalone class or
   method-on-existing) per
   [Regression tests for JIRA fixes](../../../CONTRIBUTING.md#regression-tests-for-jira-fixes),
   or executable example per
   [Executable AsciiDoc examples](../../../CONTRIBUTING.md#executable-asciidoc-examples).

2. **Follow the convention** in `CONTRIBUTING.md` — naming,
   JUnit 5 shape, tag/include matching, target tree.

3. **For a regression test, prove it fails on `master` first.**
   See [Fix workflow](../../../CONTRIBUTING.md#fix-workflow).

4. **Run targeted before module before full** — see
   [Targeted runs](../../../CONTRIBUTING.md#targeted-runs).

5. **Verify nothing leaks.** Clean working tree, no orphaned
   tagged regions, no half-finished scratch files.

## Validation checklist

Before declaring the change ready:

- [ ] Test follows the convention in
      [`CONTRIBUTING.md`](../../../CONTRIBUTING.md#tests) —
      JUnit 5 shape, naming, placement.
- [ ] Regression test was confirmed to fail on `master` *before*
      any fix was applied (not after, not by construction).
- [ ] Targeted Gradle test run is green.
- [ ] If the change touches an executable example: AsciiDoc and
      tagged Groovy snippet were edited in the same PR; tag
      names match; no orphaned tagged regions (see
      [Executable AsciiDoc examples](../../../CONTRIBUTING.md#executable-asciidoc-examples)).
- [ ] No platform-default assumptions (locale, line endings,
      path separators) baked into assertions — see
      [Test-writing pitfalls](../../../CONTRIBUTING.md#test-writing-pitfalls).
- [ ] `git status` shows no scratch files in the working tree.
- [ ] Commit message references `GROOVY-NNNNN` where applicable.

## References

- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) "Tests" — the
  canonical test conventions (framework, naming, AsciiDoc
  pattern, pitfalls).
- [`ARCHITECTURE.md`](../../../ARCHITECTURE.md) — overall test
  layout.
- [`COMPATIBILITY.md`](../../../COMPATIBILITY.md) — what
  API-affecting tests need to demonstrate.
- `.agents/skills/groovy-fix-workflow/SKILL.md` — AI guardrails
  for the surrounding fix workflow.
- `.agents/skills/groovy-internals/SKILL.md` — AI guardrails for
  compiler/runtime changes.
- `.agents/skills/groovy-reproducer/SKILL.md` — pair with when
  the test starts as an extracted reproducer.
