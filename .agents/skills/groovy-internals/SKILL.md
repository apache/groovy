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
description: Guidance for changes to the Apache Groovy compiler and runtime — parser, AST, type checker, transforms, class generation. Use when editing anything under src/main/java/org/codehaus/groovy/ or src/main/java/org/apache/groovy/parser/, or when adding/modifying an AST transformation, type-checking extension, or compilation customizer.
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: compiler-and-runtime
---

# Groovy internals

Use this skill when the change touches Groovy's compiler or runtime
implementation — not when writing application code that *uses* Groovy.

## When to use this skill

**Use it for:**

- AST transformations (local or global) under `org.codehaus.groovy.transform.*`.
- Parser changes (`src/antlr/*.g4`, `AstBuilder`, hand-written code under `org.apache.groovy.parser.antlr4.*`).
- Static type checker changes (`org.codehaus.groovy.transform.stc.*`).
- Class generation / bytecode emission (`org.codehaus.groovy.classgen.*`).
- Anything in `org.codehaus.groovy.control.*`, `org.codehaus.groovy.ast.*`, `org.codehaus.groovy.runtime.*`, `org.codehaus.groovy.reflection.*`, `org.codehaus.groovy.vmplugin.*`.
- Adding a JIRA reproducer test that needs to fail before, pass after.

**Don't use it for:**

- Writing or fixing user-facing Groovy code samples in `src/spec/doc/`.
- Subproject library code (`groovy-json`, `groovy-sql`, ...) that doesn't touch the core compiler — the subproject's own conventions apply.
- Build-only changes (`build.gradle`, `buildSrc/`, `build-logic/`).

## Read first

Before editing, read [`ARCHITECTURE.md`](../../../ARCHITECTURE.md). It
is the source of truth for the compilation pipeline, package
boundaries, and extension points referenced below. This skill is the
working surface; that document is the map.

## Top failure modes to avoid

These are the recurring mistakes — both human and AI — on internals
work. Each one is cheap to avoid and expensive to land:

1. **Editing generated parser code.** `build/generated/sources/antlr4/...` is regenerated on every build. The grammar lives in `src/antlr/GroovyLexer.g4` and `src/antlr/GroovyParser.g4`; the hand-written wiring is under `src/main/java/org/apache/groovy/parser/antlr4/`. If a fix appears to need a change in `GroovyParser.java` itself, you're editing the wrong file.
2. **Hallucinating AST node shapes.** `ClassNode`, `MethodNode`, `BinaryExpression`, etc. have specific constructors and helpers. Read the actual class before instantiating, and prefer `org.codehaus.groovy.ast.tools.GeneralUtils` factory methods over hand-built nodes.
3. **Constructing `ClassNode` for primitives or common types directly.** Use `ClassHelper.int_TYPE`, `ClassHelper.STRING_TYPE`, `ClassHelper.OBJECT_TYPE`, `ClassHelper.make(SomeType.class)`, etc. Fresh `ClassNode` instances for known types break equality and resolution.
4. **Picking the wrong compile phase for a transform.** Read `CompilePhase` and pick the earliest phase where the AST is in the state your transform needs. Local transforms default to `CANONICALIZATION`; transforms that need resolved types should run at `INSTRUCTION_SELECTION` or later.
5. **Reformatting code outside the change.** This project's review culture rejects drive-by reformatting. Match the surrounding file's indentation, brace style, and import order; do not run a formatter over files you didn't otherwise touch.
6. **Adding a new public API surface "for completeness".** Public API in this codebase is covenanted and hard to remove — see `subprojects/binary-compatibility/`. New API needs a discussion, not just a PR.
7. **Silently widening visibility when refactoring a Groovy helper.** Groovy's default method visibility is **public**, not package-private. Dropping `private` from `private static foo()` to make it a `static` helper makes it part of the public API — `static foo()` is `public static foo()`. For "visible for testing" helpers in `.groovy` files, use `@groovy.transform.PackageScope` so same-package tests see the method while external callers don't. The existing helpers in `groovy.grape.ivy.*` that omit the modifier (`resolvedRefAsFile`, `readPackaging`, `locatePrimaryJar`, etc.) are technically public — match that pattern only if you intend public exposure; otherwise prefer `@PackageScope`.
8. **Using wildcard imports.** Match the explicit-import convention already in the tree.
8. **Skipping the regression test.** Every bug fix that has a JIRA needs a test that fails on master and passes after the fix. The naming convention is in [`groovy-tests`](../groovy-tests/SKILL.md) — standalone classes use `Groovy<NNNN>`; regressions added to an existing class get a `// GROOVY-<NNNN>` comment so the JIRA stays searchable.

## Procedure

1. **Locate the precedent.** Run `git log --grep GROOVY-` for a similar past fix; the diff and its test are usually the fastest orientation.
2. **Identify the affected phase.** Use the phase table in `ARCHITECTURE.md`. State which phase your change runs in and why.
3. **Read the actual classes you're about to call into.** Don't guess at constructor signatures or method names — `Read` the file. The two highest-traffic places are `ClassHelper` and `GeneralUtils`; both are large and worth scanning before writing AST code.
4. **Match package conventions.** New code prefers `org.apache.groovy.*`; legacy code stays under `org.codehaus.groovy.*` only if it has to integrate with existing internals there. Mark anything not part of the public surface with `@groovy.transform.Internal` or place it in a package named `internal`.
5. **Add the regression test before the fix where possible.** Get it failing on master first; that proves the test reproduces the bug. Then apply the fix.
6. **Run the targeted test, then the full module test, then the build.** Don't run only `./gradlew test`; targeted runs are faster feedback and the full build catches binary-compatibility regressions:

   ```
   ./gradlew :test --tests <FQN>
   ./gradlew :test
   ./gradlew build
   ```

   For changes confined to a subproject:

   ```
   ./gradlew :<subproject>:test --tests <FQN>
   ./gradlew :<subproject>:test
   ```

7. **For end-to-end behavioural changes, run a script against an installed build.** Tests verify code; an installed build verifies the actual `groovy` / `groovyc` launchers see the change. `./gradlew :groovy-binary:installGroovy` produces an installation under `subprojects/groovy-binary/build/install/`. See "Running your local build" in [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) for the launcher path and the `GROOVY_HOME` caveat.

## ASF provenance reminders

These apply to every contribution; they are not negotiable.

- ASF license header on every new source file. Copy from a sibling.
- Don't copy verbatim from incompatibly-licensed sources (GPL/AGPL/LGPL, proprietary, unattributed Stack Overflow / blog snippets). Reimplement from specifications or Apache-compatible sources.
- New runtime dependencies need discussion and `NOTICE` / `LICENSE` updates.
- If AI tooling assisted on the change, declare it in the commit trailer per `AGENTS.md` (default form: `Assisted-by:`).

## Validation checklist

Before declaring the change ready:

- [ ] No edits to files under `build/generated/`.
- [ ] No edits to formatting outside the lines that needed to change.
- [ ] Imports are explicit, not wildcards.
- [ ] AST node construction uses `ClassHelper` for known types and `GeneralUtils` factories where applicable.
- [ ] AST transformation declares a phase, and the phase is the earliest one that works.
- [ ] New code lives in `org.apache.groovy.*` unless tied to existing `org.codehaus.groovy.*` internals.
- [ ] Public-API additions have been justified, or marked `@Internal` / placed in an `internal` package.
- [ ] Regression test exists, fails on master, passes after the fix; named after the JIRA.
- [ ] `./gradlew :<subproject>:test` (or the appropriate full test task) is green locally.
- [ ] `./gradlew build` passes — including binary-compatibility checks.
- [ ] Commit message references `GROOVY-NNNNN`; AI provenance trailer added if applicable.

## References

- [`ARCHITECTURE.md`](../../../ARCHITECTURE.md) — repository architecture and compilation pipeline.
- [`COMPATIBILITY.md`](../../../COMPATIBILITY.md) — stability tiers, what counts as a breaking change, deprecation policy, and the binary-compatibility check.
- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — build, test, and submission process.
- [`AGENTS.md`](../../../AGENTS.md) — overall AI-contributor guidance and ASF provenance rules.
- `src/main/java/org/codehaus/groovy/control/CompilePhase.java` — authoritative list of compile phases.
- `src/main/java/org/codehaus/groovy/ast/ClassHelper.java` — canonical access for common `ClassNode` instances.
- `src/main/java/org/codehaus/groovy/ast/tools/GeneralUtils.java` — AST construction helpers.
- `.agents/skills/groovy-tests/SKILL.md` — sister skill for the test-side of any compiler change (regression tests, executable-AsciiDoc examples).
