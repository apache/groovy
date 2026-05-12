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

# Apache Groovy — Repository Architecture

A contributor-facing map of how the Groovy compiler and runtime are
organised in this repository. This document is for people working *on*
Groovy. For documentation aimed at people *using* Groovy, see
<https://groovy.apache.org/> and the AsciiDoc sources under
`src/spec/doc/` and `subprojects/<module>/src/spec/doc/`.

This is an overview, not a reference. It exists to give a new
contributor — human or AI — enough orientation to read the code
productively and to avoid a small set of common mis-steps. Code is the
source of truth; this document is a pointer file.

## Repository layout (top level)

| Path | What lives there |
|---|---|
| `src/main/java/org/codehaus/groovy/` | Core compiler and runtime (legacy package — most of the codebase) |
| `src/main/java/org/apache/groovy/` | Newer code added under the `org.apache.groovy.*` package convention |
| `src/main/java/groovy/` | User-facing API (`groovy.lang.*`, `groovy.util.*`, etc.) |
| `src/main/groovy/` | Groovy sources compiled into the core jar |
| `src/main/resources/` | Service files, META-INF, default scripts |
| `src/antlr/` | ANTLR4 grammar (`GroovyLexer.g4`, `GroovyParser.g4`) — see "Generated code" below |
| `src/spec/doc/` | User-facing AsciiDoc reference docs |
| `src/spec/test/` | Executable Groovy snippets `include::`'d by the AsciiDoc sources |
| `src/test/` | JUnit / Spock tests for the core jar |
| `subprojects/` | ~50 modular subprojects (groovy-json, groovy-sql, groovy-xml, groovy-typecheckers, parser-antlr4 wiring, etc.) |
| `subprojects/groovy-binary/` | Aggregator that produces the final distribution and the published spec |
| `subprojects/binary-compatibility/` | Enforces public-API binary compatibility across releases |
| `subprojects/tests-preview/` | Tests that depend on preview JDK features |
| `bootstrap/`, `buildSrc/`, `build-logic/` | Build infrastructure (Gradle convention plugins, bootstrap helpers) |

When in doubt, prefer adding new code under `org.apache.groovy.*`; the
older `org.codehaus.groovy.*` packages remain for legacy reasons but
are kept stable for compatibility.

Some subprojects with significant local complexity carry their own
`ARCHITECTURE.md` alongside the source — see
[`subprojects/groovy-groovysh/ARCHITECTURE.md`](subprojects/groovy-groovysh/ARCHITECTURE.md)
for the worked example.

## Compilation pipeline

The driver is `org.codehaus.groovy.control.CompilationUnit`. A
`SourceUnit` represents a single source file inside it. Compilation
proceeds in numbered phases declared in
[`Phases.java`](src/main/java/org/codehaus/groovy/control/Phases.java)
and exposed as the
[`CompilePhase`](src/main/java/org/codehaus/groovy/control/CompilePhase.java)
enum that AST transformations and customizers attach to:

| # | Phase | What happens                                                                            | Driver classes |
|---|---|-----------------------------------------------------------------------------------------|---|
| 1 | `INITIALIZATION` | Source files opened, `CompilationUnit` configured, customizers applied                  | `CompilationUnit`, `CompilerConfiguration` |
| 2 | `PARSING` | ANTLR4 lexer + parser produce a CST (parse tree)                                        | `Antlr4ParserPlugin`, `GroovyLangLexer`, `GroovyLangParser` |
| 3 | `CONVERSION` | CST → AST (`ModuleNode` / `ClassNode` / `MethodNode` / ...)                             | `AstBuilder` |
| 4 | `SEMANTIC_ANALYSIS` | Class resolution, import handling, validity checks the grammar can't catch              | `ResolveVisitor`, `StaticImportVisitor`, `AnnotationConstantsVisitor` |
| 5 | `CANONICALIZATION` | Fill in the AST: synthesized members, generic types, most local AST transforms run here | `ASTTransformationVisitor`, `GenericsVisitor` |
| 6 | `INSTRUCTION_SELECTION` | Optimisations and instruction-set selection; `@CompileStatic` / `@TypeChecked` run here | `OptimizerVisitor`, `StaticTypeCheckingVisitor` |
| 7 | `CLASS_GENERATION` | AST → bytecode in memory                                                                | `AsmClassGenerator`, `Verifier`, classes under `classgen/asm/` |
| 8 | `OUTPUT` | Write generated `.class` files                                                          | `CompilationUnit` output stage |
| 9 | `FINALIZATION` | Cleanup, `Janitor` callbacks                                                            | `CompilationUnit`, `Janitor` |

Each phase iterates over all `SourceUnit`s before the next phase
begins. AST transformations declare which phase they run in; the
canonical question to ask before adding one is *"what state must the
AST be in for this transform to make sense?"* — pick the earliest phase
where that holds.

The phase enum is the right anchor for any documentation that talks
about "when X happens during compilation". Quoting the phase names
verbatim keeps the reference precise; paraphrasing tends to drift.

### Parser (phase 2)

- Grammar lives in `src/antlr/GroovyLexer.g4` and
  `src/antlr/GroovyParser.g4`. The generated parser is regenerated
  from these sources on every build, so changes belong in the `.g4`
  files.
- The ANTLR Gradle plugin generates `GroovyLexer`, `GroovyParser`,
  `GroovyParserVisitor`, and `GroovyParserBaseVisitor` into
  `build/generated/sources/antlr4/org/apache/groovy/parser/antlr4/`.
- Hand-written code that wires the parser into `CompilationUnit` lives
  in `src/main/java/org/apache/groovy/parser/antlr4/`
  (`Antlr4PluginFactory`, `Antlr4ParserPlugin`, `GroovyLangLexer`,
  `GroovyLangParser`, `AstBuilder`, plus support classes:
  `ModifierManager`, `GroovydocManager`, `SemanticPredicates`,
  `PositionInfo`).
- `AstBuilder` is the hand-off from CST to AST. It is large; almost
  every parser-visible language change touches it.

### AST (phase 3 onward)

- Root: `org.codehaus.groovy.ast.ASTNode`.
- Sub-packages:
  - `org.codehaus.groovy.ast.expr` — expression nodes
    (`BinaryExpression`, `MethodCallExpression`, ...)
  - `org.codehaus.groovy.ast.stmt` — statement nodes
    (`BlockStatement`, `ForStatement`, ...)
  - `org.codehaus.groovy.ast.tools` — helpers (`GeneralUtils` is the
    common one — prefer its factory methods over hand-built nodes)
- Top-level structural nodes: `ModuleNode` (one per source file) →
  `ClassNode` → `MethodNode` / `FieldNode` / `PropertyNode` /
  `ConstructorNode`.
- `ClassNode` instances for primitive and common types should be
  obtained from `ClassHelper`, not constructed directly. Constructing
  fresh `ClassNode`s for `int`, `String`, `Object`, etc. is a frequent
  source of equality and resolution bugs.
- Visitors: `GroovyCodeVisitor` (expression + statement),
  `GroovyClassVisitor` (class members), with `ClassCodeVisitorSupport`
  / `CodeVisitorSupport` as bases, and
  `ClassCodeExpressionTransformer` for transforms that rewrite
  expressions in place.

### Static type checker (phase 6)

- Entry point: `org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor`.
- Driven by `@TypeChecked` and `@CompileStatic`. The latter runs the
  same checker, then directs `AsmClassGenerator` to emit direct calls
  rather than dynamic dispatch.
- Extensible from user code via type-checking extension scripts; see
  `src/spec/doc/_type-checking-extensions.adoc` for the user-facing
  documentation of that mechanism.

### Class generation (phase 7)

- `org.codehaus.groovy.classgen.AsmClassGenerator` walks the AST and
  emits bytecode via ASM. Supporting visitors run here too:
  `Verifier` (synthesizes bridge methods, accessors, default
  constructors), `EnumVisitor`, `EnumCompletionVisitor`,
  `InnerClassVisitor`, `InnerClassCompletionVisitor`,
  `VariableScopeVisitor`, `ReturnAdder`.
- ASM-specific helpers: `org.codehaus.groovy.classgen.asm.*`.
- The class loader path for compiled classes goes through
  `org.codehaus.groovy.reflection.*` and the meta-class system in
  `groovy.lang.MetaClass*`.

## Extension points

Most contributor work touches one of these. Each has a dedicated
mechanism — knowing which one applies tells you where the change
belongs:

- **AST transformations** — annotation-driven AST rewrites. Local
  transforms run in `CANONICALIZATION` by default; global transforms
  apply to every compilation unit and are registered via
  `META-INF/services/org.codehaus.groovy.transform.ASTTransformation`.
  Implementations live in `org.codehaus.groovy.transform.*`.
  `AbstractASTTransformation` is the usual base class, and
  `org.codehaus.groovy.ast.tools.GeneralUtils` is the standard library
  for building AST fragments.
- **Type-checking extensions** — DSL scripts that hook into the
  static type checker. See
  `org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport`
  and the user docs at `src/spec/doc/_type-checking-extensions.adoc`.
- **Compilation customizers** —
  `org.codehaus.groovy.control.customizers.*`. Programmatic
  configuration applied at `INITIALIZATION`: `ImportCustomizer`,
  `ASTTransformationCustomizer`, `SecureASTCustomizer`,
  `CompilationCustomizer` (base class for custom ones).
- **Extension modules** — add instance / static methods to existing
  classes via descriptor files. Discovered through
  `META-INF/groovy/org.codehaus.groovy.runtime.ExtensionModule`. The
  GDK itself is built this way; see
  `org.codehaus.groovy.runtime.DefaultGroovyMethods` and friends, and
  the user-facing description in `src/spec/doc/core-gdk.adoc`.
- **Parser plugin** — `org.codehaus.groovy.control.ParserPluginFactory`
  selects the parser. The ANTLR4 implementation is the only supported
  one; the older Antlr2-based parser has been removed.

## Compiler and runtime conventions

A few project-specific conventions on top of the architecture
above. Each bites contributors quickly if missed:

- **Default visibility in Groovy sources is `public`, not
  package-private.** A `static foo()` method in a `.groovy` file
  is `public static foo()`. For "visible for testing" helpers in
  `.groovy` files, use `@groovy.transform.PackageScope` so
  same-package tests see the method while external callers
  don't. Some existing helpers in the tree (e.g. in
  `groovy.grape.ivy.*`) omit the modifier and are technically
  public; match that pattern only when you intend public
  exposure.
- **Explicit imports, not wildcards.** The codebase uses explicit
  per-class imports; new code should match. IDE-default wildcard
  imports get flagged in review.
- **New code prefers `org.apache.groovy.*`.** Legacy code under
  `org.codehaus.groovy.*` stays where it is for compatibility,
  but new packages should follow the `org.apache.groovy.*`
  convention.
- **Mark non-public surface explicitly.** Use
  `@groovy.transform.Internal` or place the code in a package
  named `internal` to signal "no stability guarantee" — see the
  Public API boundaries table below.
- **Use `ClassHelper` for known `ClassNode` instances.** Fresh
  `ClassNode` instances for primitives (`int`, `boolean`) or
  common reference types (`String`, `Object`) break equality and
  resolution. Prefer `ClassHelper.int_TYPE`,
  `ClassHelper.STRING_TYPE`, `ClassHelper.make(SomeType.class)`,
  etc.
- **Use `GeneralUtils` factories over hand-built AST nodes.** The
  helpers under `org.codehaus.groovy.ast.tools.GeneralUtils`
  cover the common construction patterns and avoid the
  shape-mismatch traps that hand-building tends to produce.
- **Pick the earliest compile phase that works for an AST
  transform.** Local transforms default to `CANONICALIZATION`;
  transforms that need resolved types belong in
  `INSTRUCTION_SELECTION` or later. See the
  [Compilation pipeline](#compilation-pipeline) phase table.

## Operator families

Several Groovy operators and expression forms are defined for
**multiple backing types**, and behaviour across the members of a
family is sometimes inconsistent. When investigating a bug
reported for one type, probing the same expression across siblings
often surfaces nuance the reporter missed — a hidden bug in a
sibling type, confirmation that an asymmetry spans the whole
family, or a project-wide spec gap that wasn't visible from a
single-type report.

### Type families

| Family | Members | Notes |
|---|---|---|
| **Range / index operators** (`agg[idx]`, `agg[range]`) | `List` / `Object[]` / primitive arrays (`int[]`, `long[]`, …) / `String` / `CharSequence` | Different exception classes (`IndexOutOfBoundsException` vs `ArrayIndexOutOfBoundsException` vs `StringIndexOutOfBoundsException`). Negative-endpoint and out-of-range-negative semantics have historically diverged across types — see GROOVY-3974 for a concrete example surfaced by cross-type probing. |
| **GPath expressions** (`x.y.z`, `x?.y`, `x*.y`) | In-memory (Map, List, nested combinations) / JSON (`JsonSlurper`) / XML (`XmlSlurper` / `XmlParser`) / POGO / Java POJO / SQL result sets (`groovy.sql.Sql`) | XML has special handling for attributes (`@attr` syntax) and returns empty `NodeChild` collections on missing children rather than null. Map/JSON return null on missing keys. POGOs and POJOs throw `MissingPropertyException` for missing properties — the asymmetry is by-design (each backend's natural type semantics) but surfaces as a cross-type inconsistency from the user's perspective. |
| **Numeric coercion** (`+`, `-`, `*`, `/`, comparison) | `int` / `long` / `BigInteger` / `BigDecimal` / `double` / `Float` / `Long` (boxed) | Coercion rules vary; the result type of `int + BigDecimal` may surprise. |

### Operator-variant families

Some operators have **multiple syntactic variants** that share a
family but dispatch differently:

| Family | Variants | Dispatch notes |
|---|---|---|
| **Safe navigation** | `?.` (SAFE_DOT) / `??.` (SAFE_CHAIN_DOT — shorthand for chained `?.`) / `?[..]` (SAFE_INDEX) | `?.` and `??.` call `getProperty(String)`. `?[..]` calls `getAt(Object)`, but on POGOs that routes through `getProperty` for missing keys, so the variants behave identically for POGO missing-property access. |
| **Spread** | `*.` / `*[..]` / `*:` | Different unpacking semantics across iteration / indexing / map-merge. |
| **Equality / identity** | `==` / `.equals()` / `is` | `==` is `equals`-based in Groovy (not reference-equality as in Java); `is` is Java's `==` (reference). |
| **Coercion** | `as` / `asType()` / constructor + `from` | Different conversion paths; `as` is statically-resolvable, `asType` is dynamic. |
| **Range** | `..` / `..<` / `..>` | Endpoint inclusion / direction differences. |
| **Elvis / null-coalesce** | `?:` and elaborations | Truthy-vs-null differences in the left-hand side. |

### Why this matters for investigation

For an investigation of a bug in one family member, probing across
siblings is a recurring technique. A ~50-line probe script
(constructing each backend, running the same expression, recording
outcomes in a table) is usually enough to:

- confirm whether an asymmetry the reporter found spans the family
  or is type-specific;
- surface a hidden bug in a sibling type the reporter didn't test
  (and which may warrant its own JIRA);
- reveal that what looks like a bug is actually consistent
  documented behaviour with a documented or implicit workaround in
  a sibling form.

See `.agents/skills/groovy-reproducer/SKILL.md`'s "Cross-family
probes" section for the AI-tooling pattern. The probe approach is
equally useful when investigating by hand.

## Generated code

The following are produced by the build and regenerated on every
run, so direct edits to them are overwritten. Changes belong in the
source they're generated from.

| Generated artefact | Source |
|---|---|
| `build/generated/sources/antlr4/org/apache/groovy/parser/antlr4/Groovy{Lexer,Parser,ParserVisitor,ParserBaseVisitor}.java` | `src/antlr/GroovyLexer.g4`, `src/antlr/GroovyParser.g4` |
| Anything under `build/`, `*/build/`, `out/`, `subprojects/*/build/` | The build itself; never committed |
| Repackaged dependency classes (ASM, ANTLR runtime, picocli) | Configured in `build.gradle` under `repackagedDependencies` |

If a `.java` file under `build/generated/...` looks like the right
thing to change, you are looking at the wrong file. The grammar fix
goes in `src/antlr/`.

## Build infrastructure

The Gradle build is driven by:

- **Convention plugins** under
  `build-logic/src/main/groovy/org.apache.groovy-*.gradle`.
  These describe the shape every subproject takes: `-base`,
  `-common`, `-core`, `-library`, `-published-library`,
  `-aggregating-project`, `-tested`, and so on. A subproject
  applies the appropriate conventions and overrides only what's
  specific to it.
- **Shared types** under
  `build-logic/src/main/groovy/org/apache/groovy/`. `Versions`,
  `SharedConfiguration`, and `Services` hold the canonical
  pinned versions and configuration the convention plugins read
  from.
- **Root** `build.gradle`, `settings.gradle`, and
  `gradle.properties` for project-wide settings, version pins,
  target bytecode, and build flags.

A few build-side conventions:

- **Cross-cutting build behaviour belongs in a convention
  plugin**, not duplicated across subproject `build.gradle`
  files. If two or more subprojects need the same configuration,
  add it to the right `org.apache.groovy-*.gradle`; conversely,
  don't push a one-off into a shared convention plugin.
- **Versions flow through `gradle.properties` and the shared
  `Versions` type**, not as `'group:artifact:1.2.3'` literals in
  subproject builds. Ad-hoc version pins drift.
- **Dependency changes require regenerating
  `gradle/verification-metadata.xml`.** The build runs Gradle
  dependency verification; an unverified artifact fails the
  build. Regenerate with
  `./gradlew --write-verification-metadata sha256,pgp help` and
  inspect the diff before committing.
- **ASM, ANTLR runtime, and picocli are jarjar-relocated** into
  `groovyjarjar*` packages, configured via
  `groovyLibrary { repackagedDependencies = ... }`. A wrong
  repackaging rule produces a jar that compiles fine but fails
  at runtime — run `./gradlew :groovy-binary:installGroovy` and
  exercise `groovy` / `groovyc` against a non-trivial script
  whenever a repackaging change lands.
- **The configuration cache constrains build logic.** Custom
  code must not access mutable `Project` state at execution
  time. Prefer providers (`providers.gradleProperty`,
  `providers.environmentVariable`, `Provider` chains) and
  `tasks.register` / `configureEach` over eager realisation.
- **Wrapper bumps need a Develocity compatibility check.** A
  Gradle wrapper version bump can disable build scans or break a
  plugin pinned in the root `plugins {}` block; check the
  Develocity compatibility matrix linked in `build.gradle`
  before bumping.
- **Binary compatibility is enforced.**
  `subprojects/binary-compatibility/` runs `japicmp` against a
  baseline release. Don't suppress it for green CI — either
  justify the change in the accepted-changes file or revert the
  API breakage. See [`COMPATIBILITY.md`](COMPATIBILITY.md).

For the canonical command sequence (targeted → subproject → full
build, with the dependency-verification regeneration and
installed-build smoke test), see
[`CONTRIBUTING.md`](CONTRIBUTING.md). For the public API
contract that the binary-compatibility check defends, see
[`COMPATIBILITY.md`](COMPATIBILITY.md).

## Public API boundaries

Groovy has a covenanted public API. The shape of a change determines
which review path applies — see [`CONTRIBUTING.md`](CONTRIBUTING.md).

| Package convention | Audience |
|---|---|
| `groovy.*` | End users — the public API surface |
| `org.apache.groovy.*` | Mixed; preferred location for new code |
| `org.codehaus.groovy.*` | Historical core — some user-visible, much internal |
| Anything annotated [`@groovy.transform.Internal`](src/main/java/groovy/transform/Internal.java) or in a package named `internal` | Implementation detail |

For the stability commitment each tier carries — what counts as
breaking, the deprecation policy, the four-tier stability model
(Public / Incubating / Internal / Generated), and how the
`japicmp`-based binary-compatibility check is wired up — see
[`COMPATIBILITY.md`](COMPATIBILITY.md). Binary compatibility against
a baseline release is enforced by the
`subprojects/binary-compatibility/` module as part of the build.

## Tests

Test code is laid out in parallel with source code:

| Directory | Purpose |
|---|---|
| `src/test/` | Core tests for the core jar |
| `subprojects/<module>/src/test/` | Module-specific tests |
| `src/spec/test/` and `subprojects/<module>/src/spec/test/` | Executable Groovy snippets that AsciiDoc sources `include::` to keep documentation examples runnable |
| `subprojects/tests-preview/src/test/` | Tests that depend on JDK preview features |

For test framework conventions (JUnit 5, regression-test naming, the
fix-workflow ordering, the executable-AsciiDoc pattern, and
test-writing pitfalls) and the targeted-run command sequence, see
the "Tests" section in
[`CONTRIBUTING.md`](CONTRIBUTING.md#tests).

## Where to read next

- [`CONTRIBUTING.md`](CONTRIBUTING.md) — how to build, test, and
  submit a change.
- [`COMPATIBILITY.md`](COMPATIBILITY.md) — stability tiers, what
  counts as a breaking change, deprecation policy, and the
  binary-compatibility check.
- [`GOVERNANCE.md`](GOVERNANCE.md) — how decisions get made, where
  discussions happen, review modes, and wait periods (placeholder
  draft pending dev@ confirmation).
- [`AGENTS.md`](AGENTS.md) — supplemental guidance for AI coding
  assistants; layered on top of this document, not a replacement for
  it.
- `README.adoc` — the canonical build instructions.
- `src/spec/doc/core-metaprogramming.adoc` — user-facing description
  of AST transformations and metaprogramming.
- `src/spec/doc/_type-checking-extensions.adoc` — user-facing
  description of the type-checking extension mechanism.
- The Groovy issue tracker (<https://issues.apache.org/jira/browse/GROOVY>)
  and the existing test suite are the best source of precedent for any
  given change. `git log --grep GROOVY-NNNNN` finds the original fix
  for an issue.
