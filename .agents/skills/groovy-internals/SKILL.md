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
name: groovy-internals
description: AI-tooling guardrails for changes to the Apache Groovy compiler and runtime — points at the project's compiler architecture in ARCHITECTURE.md (compilation pipeline phases, AST conventions, ClassHelper / GeneralUtils preference, default-public Groovy visibility, package conventions), then adds the AI-specific constraints on top: no hallucinated AST shapes, no fabricated method names, verified identifiers, regression test before the fix, hand-back for review. Use when editing anything under src/main/java/org/codehaus/groovy/ or src/main/java/org/apache/groovy/, or when adding/modifying an AST transformation, type-checking extension, or compilation customizer.
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: ai-tooling-compiler-runtime-guardrails
---

# Groovy internals

This skill is the **AI-tooling layer** over the project's compiler
and runtime architecture. The architecture itself — compilation
pipeline phases, AST node conventions, `ClassHelper` and
`GeneralUtils` preferences, the default-public-visibility trap,
package conventions (`org.apache.groovy.*` for new code) — lives
in [`ARCHITECTURE.md`](../../../ARCHITECTURE.md). This skill cites
it and adds the AI-specific guardrails: no hallucinated AST
shapes, no fabricated method names, regression test before the
fix, hand-back for review.

- [`groovy-tests`](../groovy-tests/SKILL.md) — pair with for the
  regression test that accompanies any fix.
- [`groovy-build`](../groovy-build/SKILL.md) — pair with if the
  change touches generated parser sources or repackaging.
- [`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md) — AI
  guardrails for the surrounding fix workflow.
- [`ARCHITECTURE.md`](../../../ARCHITECTURE.md) — canonical
  compiler architecture; load alongside.

## When to use this skill

**Use it for:**

- AST transformations (local or global) under
  `org.codehaus.groovy.transform.*`.
- Parser changes (`src/antlr/*.g4`, `AstBuilder`, hand-written
  code under `org.apache.groovy.parser.antlr4.*`).
- Static type checker changes
  (`org.codehaus.groovy.transform.stc.*`).
- Class generation / bytecode emission
  (`org.codehaus.groovy.classgen.*`).
- Anything in `org.codehaus.groovy.control.*`,
  `org.codehaus.groovy.ast.*`,
  `org.codehaus.groovy.runtime.*`,
  `org.codehaus.groovy.reflection.*`,
  `org.codehaus.groovy.vmplugin.*`.

**Don't use it for:**

- Writing or fixing user-facing Groovy code samples in
  `src/spec/doc/`.
- Subproject library code (`groovy-json`, `groovy-sql`, …) that
  doesn't touch the core compiler — the subproject's own
  conventions apply.
- Build-only changes (`build.gradle`, `buildSrc/`,
  `build-logic/`) — [`groovy-build`](../groovy-build/SKILL.md).

## Read first

- [`ARCHITECTURE.md`](../../../ARCHITECTURE.md) — canonical
  source for compilation pipeline, AST conventions, extension
  points, and the
  [compiler and runtime conventions](../../../ARCHITECTURE.md#compiler-and-runtime-conventions)
  (default visibility, imports, package conventions,
  `ClassHelper` / `GeneralUtils` preference, compile-phase
  selection).
- [`COMPATIBILITY.md`](../../../COMPATIBILITY.md) — stability
  tiers, public API contract, binary-compatibility check.

## Top failure modes

These are the recurring mistakes specific to AI tooling on
compiler/runtime work:

1. **Hallucinating AST node shapes.** `ClassNode`, `MethodNode`,
   `BinaryExpression`, etc. have specific constructors and
   helpers. AI tooling guesses; verify by reading the actual
   class. Prefer
   `org.codehaus.groovy.ast.tools.GeneralUtils` factory methods
   over hand-built nodes — see
   [Compiler and runtime conventions](../../../ARCHITECTURE.md#compiler-and-runtime-conventions).

2. **Constructing `ClassNode` for primitives or common types
   directly.** Use `ClassHelper.int_TYPE`,
   `ClassHelper.STRING_TYPE`, `ClassHelper.OBJECT_TYPE`,
   `ClassHelper.make(SomeType.class)`. Fresh `ClassNode`
   instances for known types break equality and resolution.

3. **Hallucinated method names.** AI tooling reaches for
   plausible-sounding API names. `git grep` the identifier
   before depending on it. If it isn't there, it isn't there.

4. **Editing generated parser code.**
   `build/generated/sources/antlr4/...` is regenerated on every
   build; the grammar lives in `src/antlr/*.g4`. See
   [Generated code](../../../ARCHITECTURE.md#generated-code).

5. **Silently widening visibility.** Groovy's default method
   visibility in `.groovy` files is **public**, not
   package-private. Dropping `private` from
   `private static foo()` makes it part of the public API. Use
   `@groovy.transform.PackageScope` for "visible for testing"
   helpers — see
   [Compiler and runtime conventions](../../../ARCHITECTURE.md#compiler-and-runtime-conventions).

6. **Picking the wrong compile phase for a transform.** Pick the
   *earliest* phase where the AST is in the state the transform
   needs. The phase table is in
   [Compilation pipeline](../../../ARCHITECTURE.md#compilation-pipeline).

7. **Wildcard imports.** Match the explicit-import convention
   already in the tree — see
   [Compiler and runtime conventions](../../../ARCHITECTURE.md#compiler-and-runtime-conventions).

8. **Adding public API surface "for completeness".** Public API
   is covenanted — see
   [Public API boundaries](../../../ARCHITECTURE.md#public-api-boundaries)
   and [`COMPATIBILITY.md`](../../../COMPATIBILITY.md). New API
   needs discussion, not just a PR.

9. **Reformatting code outside the change.** The project's
   review culture rejects drive-by reformatting — see the
   "what *not* to do" list in
   [`AGENTS.md`](../../../AGENTS.md).

## Procedure

When making a compiler/runtime change:

1. **Locate precedent.** `git log --grep GROOVY-` for a similar
   past fix; the diff and its test are the fastest orientation.

2. **Identify the affected phase.** Use the phase table in
   [Compilation pipeline](../../../ARCHITECTURE.md#compilation-pipeline).
   State which phase your change runs in and why.

3. **Read the classes you're calling into.** Don't guess
   constructor signatures or method names — `Read` the file.
   `ClassHelper` and `GeneralUtils` are the two highest-traffic
   ones; both are worth scanning before writing AST code.

4. **Follow the fix workflow** in
   [`CONTRIBUTING.md`](../../../CONTRIBUTING.md#fix-workflow) —
   failing test on `master` first, smallest fix, targeted run
   green, module run green, build green (including binary
   compatibility).

5. **Match package conventions** — see
   [Compiler and runtime conventions](../../../ARCHITECTURE.md#compiler-and-runtime-conventions).
   New code prefers `org.apache.groovy.*`; mark non-public
   surface with `@groovy.transform.Internal` or place in an
   `internal` package.

6. **For end-to-end behaviour, exercise an installed build** —
   `./gradlew :groovy-binary:installGroovy` and run a script
   against it. Tests verify code; an installed build verifies
   the `groovy` / `groovyc` launchers see the change. See
   ["Running your local build"](../../../CONTRIBUTING.md#running-your-local-build).

## Hand-back to a human

AI tooling produces the fix; humans review and land. Mirrors
[`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md)'s
hand-back contract: no autonomous PR, no JIRA comment, no merge,
no `Assisted-by:` trailer on someone else's commit. The artefact
for a committer is branch + commit + gradle commands + outcomes
+ installed-build outcome if applicable.

## Validation checklist

Before declaring the change ready:

- [ ] No edits to files under `build/generated/`.
- [ ] No formatting changes outside the lines that needed to
      change.
- [ ] Imports are explicit, not wildcards.
- [ ] AST node construction uses `ClassHelper` for known types
      and `GeneralUtils` factories where applicable.
- [ ] AST transformation declares a phase, and the phase is the
      earliest one that works (see
      [Compilation pipeline](../../../ARCHITECTURE.md#compilation-pipeline)).
- [ ] New code lives in `org.apache.groovy.*` unless tied to
      existing `org.codehaus.groovy.*` internals.
- [ ] Visibility is intentional — `@PackageScope` for
      "visible for testing", `@Internal` or `internal` package
      for implementation detail.
- [ ] Public-API additions have been justified — see
      [Adding new public API](../../../COMPATIBILITY.md#adding-new-public-api).
- [ ] Regression test exists, fails on `master`, passes after
      the fix — see
      [Fix workflow](../../../CONTRIBUTING.md#fix-workflow) and
      [`groovy-tests`](../groovy-tests/SKILL.md).
- [ ] `./gradlew :<subproject>:test` (or appropriate full test
      task) green locally.
- [ ] `./gradlew build` green — including binary-compatibility.
- [ ] Commit message references `GROOVY-NNNNN`; AI provenance
      trailer per [`AGENTS.md`](../../../AGENTS.md) if AI
      assisted.

## References

- [`ARCHITECTURE.md`](../../../ARCHITECTURE.md) — compilation
  pipeline, AST conventions, extension points,
  compiler/runtime code conventions.
- [`COMPATIBILITY.md`](../../../COMPATIBILITY.md) — stability
  tiers, breaking-change policy, binary-compatibility check.
- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — build, test,
  fix workflow.
- [`AGENTS.md`](../../../AGENTS.md) — AI-contributor guidance,
  provenance rules.
- `src/main/java/org/codehaus/groovy/control/CompilePhase.java`
  — authoritative compile phases.
- `src/main/java/org/codehaus/groovy/ast/ClassHelper.java` —
  canonical access for common `ClassNode` instances.
- `src/main/java/org/codehaus/groovy/ast/tools/GeneralUtils.java`
  — AST construction helpers.
- `.agents/skills/groovy-tests/SKILL.md`,
  `.agents/skills/groovy-build/SKILL.md`,
  `.agents/skills/groovy-fix-workflow/SKILL.md` — pair with as
  applicable.
