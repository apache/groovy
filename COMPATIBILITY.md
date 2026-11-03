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

# Apache Groovy — Compatibility and Stability

Public API in Groovy is covenanted: applications and libraries depend
on it across releases, and removing or breaking it has costs we don't
always see locally. This document is the contributor-facing reference
for what is public, what is internal, what counts as a breaking
change, and how the build helps us notice when one slips through.

If you're orienting in the codebase generally, start with
[`ARCHITECTURE.md`](ARCHITECTURE.md). For build and submission
mechanics, see [`CONTRIBUTING.md`](CONTRIBUTING.md). The user-facing
version-numbering scheme (SemVer since 2.0.0) is at
[`src/spec/doc/version-scheme.adoc`](src/spec/doc/version-scheme.adoc).

## Stability tiers

Stability is signalled by package, by annotation, and occasionally by
documented convention. The four tiers from most to least stable:

| Tier | How it's marked | Stability commitment |
|---|---|---|
| **Public API** | Lives in `groovy.*`, or in `org.apache.groovy.*` / `org.codehaus.groovy.*` without an internal marker | Source- and binary-compatible across minor and patch releases. Breaking changes need a major version. |
| **Incubating** | Annotated [`@org.apache.groovy.lang.annotation.Incubating`](src/main/java/org/apache/groovy/lang/annotation/Incubating.java), or noted as "incubating" in the feature's prose documentation | Reduced stability guarantee — design may still settle, so users opting in accept some risk of change in a minor release. See "Incubating features" below for what this means in practice. |
| **Internal** | Annotated [`@groovy.transform.Internal`](src/main/java/groovy/transform/Internal.java), or in a package whose name contains `internal` | No stability guarantee. Treat as implementation detail, even if technically reachable. |
| **Generated** | Anything emitted by the build into `build/generated/...` or repackaged via the `groovyjarjar*` namespace | Not API at all; avoid referring to these from anywhere stable. |

`org.codehaus.groovy.*` is a historical complication: most of it is
internal-by-intent but treated as public-by-practice because users
have come to depend on it. The safe assumption is that it's public
unless explicitly marked `@Internal` or living in an `internal`
sub-package.

The `internal` package convention is the one the build actually
enforces — see "The binary-compatibility check" below. Existing
examples include `org.apache.groovy.internal.util.*`,
`org.apache.groovy.internal.metaclass.*`, and
`org.apache.groovy.parser.antlr4.internal.*`.

## Compiler-referenced ABI (@GroovyABI)

Some internals are not Public API, yet are still part of the compatibility
contract because bytecode the compiler emits links against them directly
(call sites, `ScriptBytecodeAdapter`, indy bootstraps, transform helpers).
That surface is marked with
`@org.apache.groovy.lang.annotation.GroovyABI`.

- **Who is protected:** only the compiler↔runtime link, not user code.
  `@GroovyABI` does not make something public API; it records that compiled
  Groovy may outlive the compiler version that produced it.
- **`since`:** mandatory, full three-part version form (`"1.0.0"`, not
  `"1.0"`), giving the first release in which the element joined the ABI.
- **Placement:** prefer the class level when every reachable member shares
  the same `since`; annotate members when their `since` differs or only part
  of the type is reachable. Private fields and methods are never reachable by
  compiler-emitted bytecode and must not be annotated.
- **Retention:** `CLASS` — kept in published jars for build-time binary-
  compatibility checks.
- **Scope guidance:** the marked set must be complete for a stated scope
  (e.g. the core always-emitted bridges) or explicitly work-in-progress, so
  it stays a trustworthy signal. Don't extend it to a whole GDK-style class
  unless those members are actually referenced by emitted bytecode.

Removing or changing an `@GroovyABI`-marked element is a breaking change to
compiled Groovy code, handled like any other break via the release-management
process below.

### The `@GroovyABI` registry and enforcement

The complete annotated surface is recorded in
[`compatibility/groovy-abi-surface.json`](compatibility/groovy-abi-surface.json),
one entry per annotated element (method / field / constructor), keyed by JVM
descriptor — the exact signature emitted bytecode binds to. The file's schema
is `compatibility/groovy-abi-surface.schema.json`.

- **Modules:** the registry is a list of modules, each owning its elements by
  nesting. Module ownership is explicit (never derived by package prefix), so a
  whole-module removal (e.g. dropping `groovy-callsite`) is visible as that
  module block disappearing rather than as scattered per-element breaks.
- **Allow-list:** an `@GroovyABI` may appear only in a tracked module (listed
  in the registry) or in an explicitly ignored module. `checkGroovyABISync`
  fails if an annotation shows up anywhere else.
- **`since`** is the historical version that introduced each element, recorded
  alongside it; for class-level annotations every covered public method
  inherits the class `since` unless it carries its own.

#### The checks

- **`checkGroovyABISync`** (part of `check`): scans the compiled classes with
  ASM and fails if they disagree with the committed registry — an element
  removed, its descriptor changed, its annotation dropped (while the method
  still exists), or a `since` decreased without authorisation.
- **`checkGroovyABIAgainstBaseline`** (PR mode): compares the committed
  registry against a baseline registry, e.g. from the merge-base
  (`git show <base>:compatibility/groovy-abi-surface.json`), passed via
  `-PgroovyAbiBaselineRegistry=<file or JSON>`.
- **`checkGroovyABIAgainstPreviousRelease`** (release mode): the same check
  against the previous `X.Y.Z` release tag's registry. When no previous
  registry exists (a release first creating the file, or a baseline before the
  file was introduced) the prior is treated as empty: only additions are
  expected, and the inaugural file must not contain tombstones.

#### Intentional breaks ("tombstones")

The registry is also how intentional breaks are expressed and reviewed. An
element that is deliberately removed, loses its annotation, or has its `since`
changed is not deleted from the file — it stays as a **`tombstone`** record
marking where it used to be and why:

```json
"tombstone": {
  "type": "removed",            // removed | annotation-removed | version-changed
  "version": "6.1.0",           // the release the break lands in
  "previous-since": "2.0.0",    // only for version-changed
  "note": "replaced by ..."
}
```

An unmarked removal / annotation drop / `since` change fails the check; a
`tombstone` authorizes it. The PR that breaks compatibility *is* the PR that
edits the registry, so the diff is the review surface.

**Hard rule:** a `removed` or `annotation-removed` tombstone requires
`deprecated-since` — the element must have been marked `@Deprecated` in the
previous major version line (for 6.x, the latest 5.y) before the break is
accepted. `version-changed` tombstones must record `previous-since`. This is
enforced mechanically once both sides have the file; at the 5.y→6.0 transition
the evidence has to come from the 5.y source (`@Deprecated` present there) and
human review, since 6.0 is the inaugural file.

## Incubating features

`@Incubating` reduces the formal stability guarantee — it tells users
this surface may still change as the design settles — but project
practice is more conservative than the annotation's Javadoc wording
suggests. We don't break incubating APIs gratuitously: when a feature
has settled into the shape it wants and nothing else forces a change,
it stays as-is. `@Incubating` is the permission slip we use only when
the design genuinely has open questions, not a licence for churn.

Some features can't carry the annotation because they aren't expressed
as a class or method — a grammar tweak, a few lines added to an
existing visitor, a behavioural adjustment scattered across the
runtime. In those cases the incubating signal moves to the prose: the
feature's section under `src/spec/doc/` (or the relevant subproject's
`src/spec/doc/`) says explicitly that the feature is incubating.
The reduced guarantee and the project's posture of caution before
changing them are the same as for an `@Incubating` annotation.

A new feature that should be incubating but can't carry the
annotation gets the prose marker in the documentation in the same
PR as the code change. When the feature graduates, the marker comes
out of both places (annotation and prose) at the same time.

## What counts as a breaking change

For anything in the **Public API** tier, all of the following are
breaking:

- Removing a class, interface, method, field, or annotation type.
- Changing a method's name, return type, parameter list, or thrown
  checked exceptions.
- Narrowing visibility (public → protected, protected → package-private).
- Adding a method to an interface or `abstract` method to a class
  without a default implementation.
- Renaming or moving a package.
- Changing a class's superclass or removing an implemented interface.
- Removing or renaming an enum constant.
- Renaming a service-file key, or removing a previously-published
  service implementation (see "Service files" below).

These are also breaking, even though `japicmp` may not catch them:

- Behavioural changes that users have come to rely on, including
  MetaClass dispatch, method-resolution order, GDK method semantics,
  and serialization formats.
- Changing the bytecode shape that `@CompileStatic` produces in a way
  that breaks reflection-based callers.
- Changing the AST shape produced for a given source construct in a
  way that breaks third-party AST transformations.
- Changing the order or timing of compilation phases that user
  customizers attach to.
- Breaking Java interoperability — including changes that prevent
  Java code from calling compiled Groovy classes, that prevent Groovy
  code from calling existing Java libraries, that change
  joint-compilation semantics, or that change the shape of generated
  Java stubs. Seamless Java interop is a covenant of Groovy, not just
  a feature.

The bar for a breaking change is *discussion on the dev list and a
major version*, not a single PR. If a change might be breaking, the
safe default is to assume it is and ask on the list.

## Adding new public API

New API is the easier-to-fix mistake — you can deprecate and remove —
but it's still costly. Before adding any:

1. **Justify the surface.** Is this something one or many users have
   asked for, or is it convenience for a single internal caller? If
   the latter, make it package-private or `@Internal`.
2. **Pick the narrowest visibility that works.** Default to
   package-private; widen only when the cross-package or cross-module
   need is real.
3. **Place it in `org.apache.groovy.*` for new code.** Use
   `org.codehaus.groovy.*` only when the new symbol must integrate
   with existing internals there.
4. **Consider marking the feature as incubating** if the design is
   still settling. Annotate API surfaces with `@Incubating`; for
   features that aren't expressed as a class or method (grammar
   tweaks, cross-cutting behaviour), say "incubating" in the prose
   documentation instead. Either form buys room to refine in a minor
   release. See "Incubating features" above.
5. **Document it.** Public types need accurate Groovydoc/Javadoc; if
   the feature is user-facing, also add or update an AsciiDoc section
   under `src/spec/doc/` or the relevant subproject's `src/spec/doc/`.
6. **Add tests.** Both unit tests and, where appropriate, an
   executable spec example under `src/spec/test/`.

## Deprecation policy

When you need to remove or replace public API:

- Mark the old symbol `@Deprecated(since = "X.Y.Z", forRemoval = true)`
  when removal is planned, or `@Deprecated(since = "X.Y.Z")` when
  retirement is open-ended.
- In the Javadoc, name the replacement explicitly with `@deprecated`
  and a one-line pointer:

  ```java
  /**
   * @deprecated since 5.1.0, use {@link #newMethod(String)} instead.
   */
  ```

- Keep the deprecated symbol working — same semantics, no behaviour
  drift — until it is removed.
- Remove deprecated symbols only in a major release, and only after
  a deprecation has shipped in at least one minor release first.
- If a behavioural change is unavoidable in the same release as the
  deprecation (rare), call it out in the release notes.

### Groovy 6 — classic call-site caching (GROOVY-12185)

Classic (non-`invokedynamic`) call-site classes under
`org.codehaus.groovy.runtime.callsite` were moved out of groovy-core
into the optional module `groovy-callsite`.

**Primary purpose:** keep **runtime binary compatibility** for classes
compiled by **Groovy 4 and Groovy 5** (and earlier releases that always
used classic call sites). Those classes embed
`$getCallSiteArray` / `CallSiteArray` / `CallSite` linkage. With
`org.apache.groovy:groovy-callsite` on the runtime classpath, that
bytecode must still **load and execute** on Groovy 6. The published
module preserves the same public linkage surface that Groovy 4/5
classic bytecode depends on (`CallSiteArray(Class, String[])`, public
`array` / `owner` fields, and the full `CallSite` method set).

Core does not depend on that module; runtime dispatch uses invokedynamic
by default (as since Groovy 4). Related helpers that existed only to
construct classic call sites (for example `MetaClassImpl#createPojoCallSite`,
`CachedMethod#createPojoMetaMethodSite`, `CachedClass#getCallSiteLoader`)
were removed from core in this major version. Public method-selection
overloads remain on `MetaClassImpl` for the optional runtime without
package-private coupling.

To **compile** with `indy` disabled, or to **run** classic-mode classes
from older releases, add `org.apache.groovy:groovy-callsite` to the
classpath. Compiling with `indy=false` without that module fails fast
with a clear error rather than emitting unloadable bytecode. Valuable
helpers that used to live beside the call-site cache
(`BooleanClosureWrapper` and related types) remain in core under
`org.codehaus.groovy.runtime`.

The classic call-site types are **not** marked `@Deprecated` in 6.0.0
beta releases so downstream (notably Grails) can validate invokedynamic
performance without formal deprecation noise. Javadoc states they are
planned for deprecation/removal in a future Groovy version. Formal
`@Deprecated` may be restored before 6 GA once beta feedback confirms
indy remains acceptable for those use cases.

### Groovy 5/6 — `ObjectUtil` restored for Groovy 4 compiled `@Immutable` classes (GROOVY-12257)

The `@Immutable` transform in Groovy 4.0.5 through 4.0.x emitted
references to `org.apache.groovy.runtime.ObjectUtil.cloneObject` into
generated constructors and getters for defensive copies of array,
`Cloneable`, and collection properties (GROOVY-10747). The class was
removed in 5.0.0-alpha-1 together with the `$getLookup` machinery it
relied on (GROOVY-10931), so those pre-compiled classes failed on
Groovy 5+ with `NoClassDefFoundError` — only at first execution of a
clone path, which for collection properties depends on the runtime
value being `Cloneable`, making the failure intermittent and easy to
miss in testing.

`ObjectUtil` is restored (5.1.1, 6.0.0) as a deprecated
binary-compatibility facade with the original semantics: arrays clone
via the unchanged `ArrayUtil` fast paths, other `Cloneable`s through
their public `clone()` via the MOP, and a non-public `clone()` still
surfaces `NoSuchMethodException` exactly as before. Classes compiled
by ≤ 4.0.4 (which used `ReflectionMethodInvoker`, still present) and
by 5+ (whose transforms emit `invokedynamic` or `InvokerHelper` clone
calls) never reference the class. It exists only as a link target for
Groovy 4 bytecode and is not intended for direct use. This lets
"legacy" Grails plugins compiled with Groovy 4 still work with later
Grails versions using more recent Groovy versions.

### Groovy 6 — `instanceof` pattern variable flow scoping (GROOVY-12242)

Groovy 6 aligns JEP 394 / JLS §6.3-style *pattern variable* scoping for
`e instanceof T t` and `e !instanceof T t` (equivalent to
`!(e instanceof T t)`) with Java for the common shapes: `if`/`else`
(including abrupt-completion survivors), short-circuit `&&` / `||`,
ternary/Elvis arms, and true-path bindings in `while` bodies.

**Who is affected (silent behaviour change in dynamic Groovy).** Code that
previously treated a pattern variable as a local outside its JLS live range
— for example in an `else` branch of `if (o instanceof String s)`, after a
fall-through `if`, or on the RHS of `o instanceof String s || s.isEmpty()` —
now resolves that name as a dynamic property. At runtime that yields
`MissingPropertyException` (or a type-checker error under `@TypeChecked` /
`@CompileStatic`), with **no** additional compile-time warning in dynamic
mode. That is the intended Java-aligned semantics; Groovy 6 is the major
version for the change.

**What is *not* claimed.** `while` / `do`-`while` deliberately get only
*partial* flow scoping: true-path bindings are available in a `while` body
and short-circuit rules apply in conditions, but Groovy does **not**
introduce false-path bindings after a loop when the body cannot complete
normally (JLS §6.3.2.3). Pattern names never leak past the loop. Full
after-loop introduction is a possible future enhancement, not a 6.0 gap to
back-port silently.

Release notes for the 6.0 beta line should call this out (JIRA
`breaking` label on GROOVY-12242).

### Groovy 6 — first-class switch expressions (GROOVY-12255)

Groovy 6 compiles switch expressions (JEP 361) as first-class AST
(`SwitchExpression` / `YieldStatement`) instead of desugaring them to an
immediately-called closure around a switch statement (the GROOVY-9272
implementation shipped in 4.x / 5.x). As in Java (JEP 361) the `->` arm
only decides fall-through; the position decides whether a value is produced
(GROOVY-12399, GROOVY-12408). A `switch` whose value is not used is a
`SwitchStatement`, whatever its arm shape, and does nothing when unmatched.
That includes the last statement of a closure, a script or a method: the
implicit return applies to the statement, as it does to an `if` or a
colon-form `switch`. In expression position it is a `SwitchExpression` and
every path must `yield` or `throw`; write `return switch (...) {...}`, or
assign the result, to ask for that.

**Who is affected (runtime behaviour).** A switch *expression* whose selector
matches no arm throws `IllegalStateException`, where the 4.x / 5.x desugared
closure completed without a `return` and evaluated to `null`. A switch
*statement* does nothing, as before. The difference is visible where a method
body is a bare `switch`: it is a statement, so an unmatched selector leaves
the method's default value (`null`, or `0` / `false` for a primitive return
type) rather than the `null` a 5.x expression produced, and rather than the
`GroovyCastException` 5.x raised when coercing that `null` to a primitive.
Under `@TypeChecked` / `@CompileStatic`, a non-exhaustive switch expression
(no `default`, and not a complete enum) is a compile-time error; a statement
needs no `default`.

**Who is affected (AST tools).** Visitors, macros, and AST transforms that
assumed a switch expression was a `MethodCallExpression` wrapping a
`SwitchStatement` need to handle `SwitchExpression` and `YieldStatement`.
`GroovyCodeVisitor` supplies default methods so existing visitors keep
compiling. `SwitchExpression.transformExpression` rewrites the selector and
case-label expressions only and does not copy arm statements.
`ClassCodeExpressionTransformer.transform` stays generic — it
delegates to `transformExpression`, the same way it treats
`ClosureExpression`. `ResolveVisitor`, `StaticImportVisitor` and the static-compilation
transformer visit the node from their own `transform` override so
arm expressions still run through resolve, static-import rewrite
and static compilation.
A plain `ExpressionTransformer` leaves arm bodies untouched.

Under `@CompileStatic`, a non-intrinsic `case` label is resolved as
`label.isCase(selector)` and emitted as a direct method call. An
`Object`-typed label therefore uses `isCase(Object, Object)` (equals),
the same as a statically compiled `label.isCase(selector)` call. Dynamic
Groovy still dispatches `isCase` on the label's runtime class.

**What is *not* claimed.** Matching still uses Groovy `isCase` (Class, regex,
Collection, Closure). `tableswitch` / `lookupswitch` are emitted only when
the selector and labels are constants of a type `javac` would switch on.

### Groovy 6 — `CompilerConfiguration` copy constructor copies customizers (GROOVY-9585)

`CompilerConfiguration(CompilerConfiguration)` now copies the source
configuration's compilation customizers along with every other setting.
Before Groovy 6 it copied everything *except* customizers.

**Who is affected.** Code that derives a configuration from an existing
one and then registers its own customizers:

```java
CompilerConfiguration child = new CompilerConfiguration(parent);
child.addCompilationCustomizers(mine);   // now: parent's customizers *and* mine
```

The change is silent — no exception, just customizers running that
previously did not. An `ImportCustomizer` adds its imports twice; an
`ASTTransformationCustomizer`, which carries mutable `applied` state, is
invoked for a second compilation.

**To restore the old behaviour**, use the two-argument copy constructor added in
6.0.0:

```java
CompilerConfiguration child = new CompilerConfiguration(parent, false);
```

**Nested compilation.** A copied customizer is invoked for every primary
class node of the child compilation, including nodes added via
`CompilationUnit.addClassNode`, which have no `SourceUnit` — as that
method's javadoc warns. A customizer that dereferences the `SourceUnit`
it is handed will therefore throw `NullPointerException`; Gradle's
incremental-compilation customizer is one such, and Groovy's own
`SourceAwareCustomizer` is another. A customizer may equally assume the
class nodes it sees belong to the compilation it was registered for.
Groovy's own nested compilations —
`StaticTypeCheckingSupport.evaluateExpression`, which compiles a
synthetic expression holder, and `GroovyTypeCheckingExtensionSupport`,
which compiles a type checking DSL script — therefore pass `false`.
**Prefer `new CompilerConfiguration(parent, false)` whenever you derive a
configuration for a nested compilation**, and null-check the `SourceUnit`
in any customizer that might be applied to one.

### Groovy 6 — method-level type-checking annotations override class-level SKIP (GROOVY-12292)

A method (or constructor) whose own `@TypeChecked` or `@CompileStatic`
annotation has the default non-`SKIP` mode is now type checked — and for
`@CompileStatic`, statically compiled — even when its declaring class is
annotated with `@CompileDynamic`, `@CompileStatic(TypeCheckingMode.SKIP)`
or `@TypeChecked(TypeCheckingMode.SKIP)`. Previously the class-level
`SKIP` silently won and the method-level annotation was ignored. Nested
classes already behaved this way (GROOVY-10238); this aligns methods
with them: the most specific annotation wins, and a class-level `SKIP`
is the default only for members without their own annotation.

**Who is affected.** Code with a method-level opt-in under a class-level
opt-out. Such methods may now raise type-checking errors that were
previously not reported, and their bodies are statically compiled where
they were previously dynamic. Remove the method-level annotation (or
change it to `SKIP` mode) to retain the old behaviour.

**What is unchanged.** The opt-out direction is untouched:
`@CompileDynamic` / `SKIP`-mode methods inside checked classes are
skipped exactly as before. Cross-family behaviour is also unchanged: a
method-level `@CompileStatic(TypeCheckingMode.SKIP)` disables static
compilation but does not exempt the method from an enclosing class's
`@TypeChecked` checking; only `@TypeChecked(TypeCheckingMode.SKIP)`
does that.

### Groovy 6 — error tolerance applies to type checking errors (GROOVY-12306)

The compiler's error tolerance — the number of non-fatal errors collected
before compilation is abandoned, `CompilerConfiguration.getTolerance()`,
`groovyc -t` — is now also enforced for errors reported through
`ClassCodeVisitorSupport#addError`, which includes all static type
checking errors. It previously covered only errors reported through
`SourceUnit#addError` (parse and class generation); visitor errors went
straight to `ErrorCollector#addErrorAndContinue` and were unbounded.

The default is unchanged at 10 (now named
`CompilerConfiguration.DEFAULT_TOLERANCE`). A tolerance of zero or less
now means unlimited, which is the setting to reach for when every error
is wanted; previously zero was indistinguishable from the option being
absent on the command line, and would have bailed out on the first error
if set through the API.

**Who is affected.** Any compilation reporting more than the tolerance in
type checking errors. Where 40 such errors were previously all reported,
10 are now reported and compilation stops. Pass `-t 0` on the groovyc
command line, set `tolerance="0"` on the Ant `<groovyc>` task, or set
`configuration.tolerance = 0` in a compiler configuration script (the
route Gradle users have, via `groovyOptions.configurationScript`) to
restore full reporting.

This is not limited to the compiler front ends: it applies to any caller
driving a `ClassCodeVisitorSupport` subclass over a `SourceUnit` and
counting the errors it collects — static analysers, IDE integrations and
AST transformation test harnesses among them. Note in particular that
`SourceUnit.create(String, String)` selects a tolerance of **1**, so a
visitor driven over a source unit from that factory now stops at the
first error. Use the three-argument overload to state the tolerance the
caller actually wants.

**What is unchanged.** The default of 10, so parse and class generation
errors behave exactly as before. The temporary error collectors that
`StaticTypeCheckingVisitor` pushes for speculative checks are also
unaffected — they must keep collecting without bailing out, because their
errors are routinely discarded once a candidate is ruled in or out, so
tolerance is enforced only against the source unit's own collector.

Errors that reach the collector without passing through an enforcing
path also behave as before: direct calls to
`ErrorCollector#addErrorAndContinue` — the route most AST
transformations take, via `AbstractASTTransformation#addError` — and
bulk merges via `ErrorCollector#addCollectorContents` (annotation
checking in `ExtendedVerifier`, for example) report their errors and
carry on regardless of the threshold. Such errors still count towards
the error total, so the next error reported through an enforcing path
takes them into account.

Completeness remains per-phase, as it always has been: `failIfErrors()`
runs at the end of each phase, so a single type checking error anywhere
in the compilation still suppresses every class generation error,
whatever the tolerance.

### Groovy 6 — String-to-`Class` coercion does not run the class's static initializer (GROOVY-12375)

Coercing a `String` to a `Class` — `'com.example.Foo' as Class`, and the
`(Class)` cast the compiler routes through
`ShortTypeHandling.castToClass` — now *resolves* the named class without
*initializing* it. Previously it called the single-argument
`Class.forName(name)`, which runs the class's static initializer as a side
effect; it now uses `Class.forName(name, false, loader)` with the same class
loader as before. The class is still initialized lazily on first real use, as
the JVM always does.

**Who is affected (runtime behaviour).** Code that relied on the coercion to
trigger a static initializer — most commonly the legacy JDBC idiom
`'com.mysql.jdbc.Driver' as Class` to register a driver — no longer gets that
side effect. Call `Class.forName(name)` (or `Class.forName(name, true, loader)`)
explicitly where the initialization is wanted; modern JDBC drivers auto-register
via `ServiceLoader` and do not need it. The `@GroovyABI` signature is unchanged,
so `japicmp` sees nothing — this is a behavioural change only.

Groovy 6 is the major version for the change. On 3.0.x/4.0.x/5.0.x/5.1.x the
coercion still initializes the class.

### Groovy 6.0.1 — `abs()` on a `Number` receiver keeps the number's precision (GROOVY-12471)

`DefaultGroovyMethods.abs(Number)` used to return `int`, truncating any
`Long`, `BigInteger`, `BigDecimal` or floating-point value whose static type
was `Number`. Dynamic dispatch rarely hit it, because the `Long`, `Float` and
`Double` overloads and the `BigInteger`/`BigDecimal` instance methods were
selected first, but `@CompileStatic` code with a `Number`-typed receiver —
`(item - value).abs()` — always did. The method now returns `Number`, computed
with the same type rules as Groovy's arithmetic operators. New `int abs(Integer)`,
`int abs(Short)` and `int abs(Byte)` overloads keep the integral receivers on
`int`.

**Who is affected (source).** Java code that assigns the result directly to a
primitive — `int x = DefaultGroovyMethods.abs(n)` — no longer compiles, and
`@CompileStatic` code doing the same through a `Number`-typed receiver —
`int x = n.abs()` — is now a type-checking error. Both were truncating; use
`.intValue()` or a cast where the narrowing is intended. Receivers typed
`Integer`, `Short`, `Byte`, `Long`, `Float` or `Double`, and all dynamic code,
compile and behave as before.

**Who is not affected (binary).** The old `int abs(Number)` descriptor is kept
as a synthetic bridge method, so classes compiled against earlier releases
still link and still get the old truncating result. `japicmp` sees no change.
A `Float` reached through a `Number` receiver now yields a `Double`, as `Float`
arithmetic does; through a `Float` receiver it still yields a `Float`.

### Groovy 6.0.1 — statically compiled variables declared wider than their initializer's generic type keep the value (GROOVY-12469)

Under `@CompileStatic`, a local variable declared with a type wider than the
one inferred for its initializer from generics — `CharSequence cs = list.get(0)`
with `list` a `List<String>` — was converted to the inferred type where it was
declared. Because generic types are not checked at runtime, the element need
not be of that type: a `GString` added by dynamic code became a `String`, and
a `StringBuilder` became its text. The variable now holds the value unchanged,
as it does in dynamic Groovy and in Java. Calls to methods of the declared type
are made through that type, as javac does. A `GString` is converted to a
`String` where a method that only `String` has is called on it, both through
such a variable and on a generic result used directly —
`list.get(0).toUpperCase()`, which used to throw `ClassCastException`.

**Who is affected (source).** Code that relied on the conversion at the
declaration: for a `GString` element, `cs.equals('ab')` and
`cs instanceof String` are now false (`cs == 'ab'` is still true), and a lazy
`GString` is no longer fixed at the declaration. Any other non-`String` element,
such as a `StringBuilder`, now throws `ClassCastException` where a
`String`-only method is called on it. Variables declared `String`, `def` or
`var`, and dynamic code, behave as before.

### Groovy 6.0.1 — enum constants with literal arguments are created without the meta class (GROOVY-12490)

GROOVY-12240 made an enum whose constants have no arguments create them with a
direct constructor call. An enum whose constants have arguments is now created
the same way when every argument is a literal (a constant, `null` or a class
literal) and the compiler can tell which constructor the runtime would select:
one fixed-arity constructor of that arity, each literal of the parameter's own
type, the primitive of a wrapper literal, or a type other than `Boolean`
that it extends or implements. Any other
enum keeps the synthetic `$INIT` helper, which selects the constructor through
the meta class.

**Who is affected.** Code that registered a meta class, or an
`ExpandoMetaClass` constructor, for such an enum before it initialized, to
observe or replace the creation of its constants. That no longer sees them, as
it already did not for enums without arguments. Writing one argument as a
non-literal expression, such as `'one'.toString()` in place of `'one'`, keeps
the whole enum on `$INIT`.

**What is unchanged.** The constants' values, ordinals and fields, the
constructor chosen, and enums with named arguments, class bodies, abstract
methods or overloads the literals cannot tell apart.

### Groovy 6.0.1 — `super.x = v` calls a setter of the superclass when it has several (GROOVY-12521)

When a superclass declared more than one `setX` method — `setX(int)` and
`setX(String)` — `super.x = v` in dynamic Groovy and under `@TypeChecked` used
ordinary dispatch on the receiver. A `setX` that the class itself overrides or
declares, including one generated for a redeclared `x` property, was called
instead of the superclass's. An overriding setter that delegated with
`super.x = v` therefore called itself until a `StackOverflowError`. The write
now chooses among the superclass's `setX` methods by the type of `v`, as
`super.setX(v)` and `@CompileStatic` do, and as `super.x = v` already did when
the superclass had a single setter. `super.x++` and `super.x += v` write the
same way.

**Who is affected.** Classes whose superclass overloads a property's setter,
that override one of those setters or redeclare the property, and that write
the property through `super`. Such a write now runs the superclass's setter,
not the class's own; use `this.x = v` where the class's own setter is wanted.
In the same case, a `setX` added through the class's `ExpandoMetaClass` or
through a category no longer receives `super.x = v`, which now resolves as it
does for a single setter. `@CompileStatic` code, reads of `super.x`, writes
through `this` or another reference, and superclasses with a single setter
behave as before. There are no API changes.

### Groovy 6.0.1 — a repeated `_` is an ordinary variable holding the last value (GROOVY-11744)

A variable named `_` may now be declared again in the same or an enclosing
scope, the later declaration replacing the earlier one, as Java allows for
its unnamed variables. A `_` repeated in a multi-assignment declaration or in
a closure or lambda parameter list is therefore simply `_` declared more than
once: after `def (_, y, _) = [3, 4, 5]` the variable `_` is `5`, and
`{ _, _ -> _ }` returns its last argument.

This restores the Groovy 5 behaviour in dynamic code. In 6.0.0, reading `_`
after such a declaration threw `MissingPropertyException`, or found a `_` in
the script binding. That change was not announced and was most likely
unintentional: it was a side effect of the `instanceof` pattern variable
scoping work (GROOVY-12242), which made a variable that is not declared in
scope always resolve as a property, even when a local of that name exists. A
closure could not see such a `_` in either version; it now can.

**Who is affected (source).**
- Code that compiled before behaves as in Groovy 5 and as before, with the
  exception of the 6.0.0 regression described above.
- Under `@CompileStatic` and `@TypeChecked`, reading `_` after a repeated
  `_` used to be the error "The variable [_] is undeclared"; it now has the
  type of the last declaration.
- Redeclaring `_` — `def _ = 1` followed by `def _ = 2`, `def (_, y) = …`,
  `{ _ -> … }` or `catch (Exception _)` — used to be the error "The current
  scope already contains a variable of the name _", and now compiles. An
  inner declaration shadows the outer `_` rather than changing it.

Method and constructor parameters, fields and properties named `_`, and
variables of any other name, are still checked for duplicates. The
undeclared multi-assignment `(_, y, _) = …` was never affected: it assigns
the existing `_` (or the binding variable) twice.

### Groovy 6.0.1 — parallel collections run on the pool in scope (GROOVY-12457)

Parallel collection methods (incubating) run on the pool bound with
`Pool.withCurrent`. In 6.0.0 a pool not backed by a `ForkJoinPool`, such as
`Pool.virtual()` or `Pool.io()`, was silently replaced by the common
fork-join pool, so blocking callbacks gained nothing from it.

**Who is affected.**

- Callbacks under `Pool.virtual()` or `Pool.io()` now run on that pool's
  threads. A short-circuit (`findParallel`, `findAnyParallel`,
  `anyParallel`, `everyParallel`) or a failing sibling interrupts callbacks
  still running there, which closes an interruptible channel they are
  blocked on.
- `Pool.current()` returns the pool on its own worker threads, where it
  returned `null`, so a nested parallel call stays on that pool.
- `groupByParallel` returns its keys in first-seen order, as `groupBy` does.
- `grepParallel` keeps an element when `isCase` is Groovy-true, as `grep`
  does. A filter whose `isCase` returns `null`, `0`, an empty string or an
  empty collection no longer keeps every element.

### Groovy 6.0.1 — type checking rejects a call to a property accessor that is not generated (GROOVY-12525)

Static type checking assumed that a property had both an `isX()` and a
`getX()` accessor, whether or not the class generates them. A call to an
accessor that does not exist was accepted at compile time and failed at
run time with `MissingMethodException` under `@TypeChecked`, or
`NoSuchMethodError` under `@CompileStatic`. Such a call is now a compile
error, `Cannot find matching method`. The accessors type checking accepts
are the ones the class generates:

- A `boolean` property generates `isX()` and `getX()`, unless the class
  declares one of them itself, in which case only that one exists.
- A `Boolean` property, or a property of any other type, generates only
  `getX()`.

**Who is affected.** `@TypeChecked` code that calls such a missing accessor
on a class with `methodMissing`, for example `b.isName()` for a `String`
property `name`. That call reached `methodMissing` at run time and now no
longer compiles. Call the accessor the class generates, read the property
(`b.name`), or mark the method `@TypeChecked(TypeCheckingMode.SKIP)` to keep
the dynamic call. Other calls to a missing accessor already failed at run
time, and dynamic code is unchanged.

### Groovy 6.0.1 — the receiver of a compound-assigned or incremented property is evaluated once (GROOVY-12619)

A compound assignment or an increment of a property, such as `obj().v += 2`
or `obj().v++`, evaluated the receiver expression twice: once for the read
and once for the write. The receiver is now evaluated once and used for
both, as it already was for subscripts such as `list[i()] += 2`. This
applies in dynamic Groovy, under `@TypeChecked` and under `@CompileStatic`.

It covers `+=` and the other compound assignment operators, prefix and
postfix `++` and `--`, attribute access (`obj().@f += 2`), safe navigation
(`obj()?.v += 2`), property chains, a static property reached through an
instance, extension properties such as `file.text += 'x'`, and a receiver
named by a property or dynamic variable that is read through a getter. The
elvis assignment `?=` is unchanged.

**Who is affected.** Code whose receiver expression has a side effect or
returns a different object on each evaluation:

- A method or getter that counts calls, logs, or otherwise has an effect
  now runs once per compound assignment or increment, where it ran twice.
- A receiver that yields a new or a different object each time, such as
  `iterator.next().v += 1` or `new Box().v++`, now reads and writes the
  same object. Before, the read came from one object and the write went to
  the next.

### Groovy 7 — a `setProperty` or `getProperty` from another interface's default method receives every access (GROOVY-12489)

For a dynamic property write or read, the runtime checks whether the receiver
overrides the `setProperty` or `getProperty` that `GroovyObject` provides. An
override receives the access. For the `GroovyObject` default, the runtime calls
the metaclass itself and tells it which class is making the access. Since Groovy 5
(GROOVY-8283), the check treated any interface default method as `GroovyObject`'s
own. So when a class took `setProperty` or `getProperty` from a default method of
another interface, such as one extending `GroovyObject`, `obj.x = v` writes and
computed-name reads (`obj."$name"`) went around that method to the metaclass.
`obj.setProperty(...)` calls and `obj.x` reads did reach it. Now only
`GroovyObject`'s own default is set aside, so such a method receives every write
and read. That matches a method the class declares, and Groovy 4, which compiled
interface default methods as traits.

**Who is affected.** Classes that implement an interface supplying a default
`setProperty` or `getProperty`. The method now also receives the writes and
computed-name reads that used to bypass it. Like any override, it receives the
access without knowing which class made it, so the access is resolved for the
receiver's own class. For example, a superclass that writes its own private field
on an instance of a subclass, `other.secret = v`, now fails with
`MissingPropertyException`, as the read `other.secret` already did. Use direct
field access (`other.@secret = v`) where that is intended. An `@Internal` default
`getProperty` of another interface now answers computed-name reads, while `obj.x`
reads still go around it, as in Groovy 4.

**Classes reflection cannot read.** Whether a class overrides these methods is now
decided once per class and cached. Reflection may be unable to read a class's
public methods: in a GraalVM native image without reflection metadata for the
class, or on the JVM when a public method names a type missing at run time. Such a
class now counts as overriding, so the access reaches the object's own
`setProperty` or `getProperty`. Before, the access threw, or a read went around a
real override to the metaclass. If the class has the `GroovyObject` default, the
access is resolved for the receiver's own class.

**What is unchanged.** Classes that use `GroovyObject`'s own defaults, and classes
that declare `setProperty` or `getProperty` or inherit it from a superclass or a
trait. Also unchanged: a `setProperty` or `getProperty` contributed through
`ExpandoMetaClass`, a per-instance metaclass or a mixin, because the metaclass is
still consulted on each access. `GroovyObjectOverrides`, which holds the check, is
internal, so `japicmp` does not report this change.

### Groovy 7 — Groovy Console smart highlighter and the language host

`SmartDocumentFilter.getLatestTokenList()` now returns delimiter spans
(`offset`, `length`, `text`) from semantic tokens, not ANTLR `Token`s.
`HIGHLIGHTED_TOKEN_TYPE_LIST` is no longer published. Both types have been
public since 3.0, so this is a source and binary break for embedders that
read the lexer token list. Brace matching uses the new spans.
`GroovyLanguageClient` is `@Internal`. `GroovyLanguageEngine` lives in an
internal package; hosts talk to `GroovyLanguageClient`.

`groovy-console` and `groovy-groovysh` depend on `groovy-lsp` at runtime,
so a distribution that includes those modules also includes LSP4J and Gson.
`groovy-groovysh` no longer puts `groovy-console` on its compile classpath.
`/console` from the shell needs `groovy-console` available separately and
fails quietly when it is absent.

`ImportCustomizer` reports star imports as package names without a
trailing dot (`java.io`, not `java.io.`). `getImportClassNames`,
`getStarImports`, `getStaticStarImports`, `getAliases` and
`getStaticImports` are new in 7.0.0.

A distribution that omits `groovy-lsp` or Gson still opens the console
and groovysh. The console uses regex highlighting, leaves the language
host unset, and disables Compiler Assistance on that window. A warning
is logged for the highlighter and another for the host. The saved
preference is not changed. The shell uses its Inspector completer and
prints one warning. `noSyntaxCheck` set in
`groovysh_init.groovy` is applied before the language session starts.

`@ASTTest` stays a no-op when `groovy.asttest.enable` is `false`. A
per-thread override used by in-process tooling can only force that
no-op. It cannot evaluate the closure against that property.

### Groovy 7 — `SignatureCodec` version 1 no longer round-trips a union node

`SignatureCodecVersion1` used to encode a checker-only `UnionTypeClassNode` by
name and rebuild one on decode. Newly encoded signatures store the lowest upper
bound that has a stable binary name, or `Object`. Historical payloads that still
carry the `UnionTypeClassNode` tag are consumed and then widened the same way.

The method descriptors are unchanged, so `japicmp` does not report this.
Callers of `SignatureCodecVersion1.decode` that expected a union node now
receive the shared supertype. Between GROOVY-6401 (2013) and GROOVY-12472 the
compiler wrote no `@TypeCheckingInfo` inferred-return annotations.

### Groovy 7 — inferred return types of methods with a sole implementation (GROOVY-12472)

A call to a method declared with `def` now has the type the method's body
returns, rather than `Object`, when no other body can run for the call: a
`final` method or a method of a `final` class, and a `private` or `static`
method when the call is statically compiled (or, for a `static` method, names
its class). The type is inferred within the class being checked and read from
the class file for a precompiled class. Reading such a getter as a property
(`label` for `def getLabel()`) gets the same type. Overridable methods keep
their declared return type, as since GROOVY-6401.

- **More programs compile.** Code that called such a method and relied on the
  result had to cast or declare; it now type checks as written.
- **Statically compiled overload selection can change.** An argument whose type
  was `Object` now has the inferred type, so `pick(name())` with `pick(Object)`
  and `pick(String)` overloads selects `pick(String)` where it used to select
  `pick(Object)`. Type checked (dynamically dispatched) code already selected by
  the runtime type and is unaffected.
- **Class files carry `@TypeChecked.TypeCheckingInfo` again**, on each such
  method that is not `private` and is type checked (`@TypeChecked` or
  `@CompileStatic`); a dynamically compiled method records nothing, so its
  callers keep its declared return type. It is a runtime-retained annotation, so
  code that lists a method's annotations by reflection sees it.
- **The inferred type is part of the method's contract for statically compiled
  callers.** A caller compiled against one version of a class relies on the
  result having the type recorded there; if a later version of the method
  returns a value of another type, the caller can fail at runtime. Declare the return type of a
  method whose result type may change between versions.

The method descriptors are unchanged, so `japicmp` does not report this.

### Groovy 7 — `>` and `>=` with a primitive NaN operand are false (GROOVY-12538)

When both operands of a comparison are primitive `double` or `float` values,
the compiler compares them with a JVM instruction. It used the variant that
treats NaN as greater than every value for all four ordered operators, so
`NaN > x` and `NaN >= x` were `true` while `NaN < x` and `NaN <= x` were
`false`. All four are now `false`, as in Java (JLS 15.20.1), and as Groovy
already gave when both operands are `Double` objects. This applies to
variables, fields and parameters declared `double` or `float`, in dynamic code
as well as under `@TypeChecked` and `@CompileStatic`, and to `Double.NaN` and
`Float.NaN` under `@CompileStatic`, where they are primitive.

**Who is affected.** Code that relied on a NaN comparing greater, for example
`if (reading > limit)` treating a NaN reading as over the limit. Use
`Double.isNaN` where NaN needs handling of its own.

**What is unchanged.** Comparisons that go through Groovy's number comparison
rather than a primitive instruction: operands of different types such as
`Double` against `Integer` or `BigDecimal`, where NaN is still greater than
every value as in `Double.compareTo` (GROOVY-3309); `==` on `Double` objects,
which still treats NaN as equal to NaN as `Double.equals` does; and `<=>`,
which follows `Double.compare` for primitives and objects alike. Classes
compiled by an earlier release keep the old comparison until they are
recompiled.

### Groovy 7 — `PlaceholderVisitor` is removed (GROOVY-11744)

`org.codehaus.groovy.control.PlaceholderVisitor` marked a `_` repeated in a
multi-assignment declaration or a closure or lambda parameter list, so that
`VariableScopeVisitor` would skip its duplicate check. Since a repeated `_` is
an ordinary variable that may be declared again (see the Groovy 6.0.1 entry
above), nothing reads the mark, and the compiler no longer runs the visitor.
The class was deprecated in Groovy 6.

**Who is affected.** Code that constructs `PlaceholderVisitor`, such as a copy
of `CompilationUnit` that adds it as a phase operation, or that calls
`PlaceholderVisitor.isPlaceholder`. Drop the phase operation. Where code needs
to know whether a variable is `_`, check its name.

### Groovy 6.0.1 — statically compiled constructor references call the constructor directly (GROOVY-12488)

Under `@CompileStatic`, a constructor reference such as `X::new` created the
object through the meta-class, which selected the constructor at run time from
the types of the arguments. The constructor is now selected at compile time,
among the constructors visible from the enclosing class, for the parameter
types of the functional interface, as for `new X(args)` in the same class and
as in Java, and it is called directly.

**Who is affected.** Statically compiled constructor references whose result
depended on that run-time selection:

- The overload is chosen by the static parameter types of the functional
  interface. `Function<CharSequence, X> f = X::new` calls `X(CharSequence)` even
  when it is given a `String`. With `IL(Object)` and `IL(long)`,
  `Function<Integer, IL>` calls `IL(Object)`, where it called `IL(long)`; with
  `IL2(int)` and `IL2(Integer)`, `IntFunction<IL2>` calls `IL2(int)`, where it
  called `IL2(Integer)`.
- An `Object[]` given to `Function<Object, VO>` for `VO(Object...)` is passed as
  one element, where its elements were passed.
- `null` given to `Function<Integer, PI>` for `PI(int)` throws
  `NullPointerException`, where it threw `GroovyRuntimeException`.
- A constructor the enclosing class cannot call no longer wins over one it can.
  With `private Foo(String)` and `public Foo(Object)`, `Function<String, Foo>`
  in another class calls `Foo(Object)`, as `new Foo(s)` does.
- A constructor added or overridden through the meta-class, for example with
  `ExpandoMetaClass`, is not used, as for a statically compiled `new`.

**What is unchanged.** The constructor is still selected at run time when no
single constructor matches the parameter types (GROOVY-10971), for an enum, for
a protected constructor of another package, and for a protected or
package-private constructor of a class that a `GroovyClassLoader` defined in
another compilation, such as a class of an earlier `GroovyShell` parse. Dynamic
and `@TypeChecked` code select the constructor at run time as before.

## The binary-compatibility check

The [`subprojects/binary-compatibility/`](subprojects/binary-compatibility)
module wires
[`japicmp`](https://github.com/siom79/japicmp) into the build. For
each library subproject, it compares the current `repackageJar` output
(shadow-relocated published jar) against the published artefact of a
baseline version and produces an HTML report.

| Aspect | Detail |
|---|---|
| Tool | `me.champeau.gradle.japicmp` Gradle plugin |
| Baseline version | `binaryCompatibilityBaseline` Gradle property (`-PbinaryCompatibilityBaseline=5.0.4` or `gradle.properties`) |
| Coverage | All subprojects with `groovyLibrary.checkBinaryCompatibility = true` (the default) |
| Visibility checked | `protected` and above |
| Excluded | Packages matching `**internal**` and `groovyjarjar**`; closure classes and `dgm$*` runtime helpers |
| Where reports land | `subprojects/<name>/build/reports/japicmp<Name>.html` |
| Aggregating task | `./gradlew :binary-compatibility:checkBinaryCompatibility` |

A few things to know about the check:

- **It does not fail the build.** `failOnModification = false` —
  reports are produced, but a CI job has to read them and a human
  has to interpret them. The report is a guidance tool, not a gate.
- **The `@GroovyABI`-marked subset can be gated.** Because the marker is
  retained in class files, a stricter task variant can use
  `annotationIncludes = ['@org.apache.groovy.lang.annotation.GroovyABI']`
  with `failOnModification = true` to enforce the annotated subset while the
  broad report stays advisory.
- **`@Internal` is not what excludes a symbol from the check.**
  Exclusion is by package name (`**internal**`). The annotation is a
  documentation marker that helps Groovydoc, AST tools, and reviewers;
  to genuinely move something out of the binary-compatibility
  surface, an `internal` package is the right home.
- **The baseline is a Gradle property, not pinned in code.** When a
  release is cut, the property is updated to the previous release.
  Local runs default to whatever's in `gradle.properties`.

If `japicmp` flags a change you intended:

1. Confirm the affected symbol was actually public, not internal.
2. Confirm the change is necessary — most "improvements" to public
   API are not.
3. If it stays, raise it on the dev list. The release-management
   conversation is the one that signs off on what version absorbs the
   break.

## Service files and SPI

These are public surface even though they don't show up in `japicmp`:

- `META-INF/services/org.codehaus.groovy.transform.ASTTransformation` —
  global AST transformations.
- `META-INF/groovy/org.codehaus.groovy.runtime.ExtensionModule` —
  GDK-style extension modules adding methods to existing classes.
- Anything published under `META-INF/services/` or `META-INF/groovy/`.

Removing or renaming an entry in these files is as breaking as
removing the corresponding type. New entries get added additively,
the same way as a new public type.

## Cross-references

- [`ARCHITECTURE.md`](ARCHITECTURE.md) — overall layout; the
  package-conventions section there is summarised in this document
  and elaborated with stability semantics here.
- [`CONTRIBUTING.md`](CONTRIBUTING.md) — build, test, submission.
- [`GOVERNANCE.md`](GOVERNANCE.md) — how breaking-change and
  API-addition decisions are taken to dev@; review modes and wait
  periods.
- [`AGENTS.md`](AGENTS.md) — AI-contributor supplement.
- [`src/spec/doc/version-scheme.adoc`](src/spec/doc/version-scheme.adoc)
  — user-facing version-numbering scheme.
- [`subprojects/binary-compatibility/build.gradle`](subprojects/binary-compatibility/build.gradle)
  — exact configuration of the japicmp check.
