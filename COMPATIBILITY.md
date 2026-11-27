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

### Groovy 6 — a bare `await` or `async` is the keyword, not an existing name (GROOVY-9381, GROOVY-12715)

`async`, `await` and `defer` are contextual keywords (incubating), so
existing variables, fields, parameters and methods with these names still
compile. But `await` at the start of an expression, followed by something
that can begin an expression, is the keyword, and so is `async` followed by
a closure or lambda. Code that used these words as plain names in those
positions compiles to something else, and fails at run time:

- a call to your own method, `await(1)` or `async { ... }`, now awaits `1`
  or starts an async task;
- `await + 1`, `await - 1` and `await[0]`, with a variable or field named
  `await`, now await `+1`, `-1` and the list `[0]`;
- a statically imported `await` with an argument, such as Awaitility's
  `await('alias')`, is the keyword. `await()` and `await().atMost(...)`
  keep working.

**Who is affected (source).** Code written for Groovy 5 or earlier that uses
`await` or `async` as names in those positions. Qualify the name, as in
`this.await(1)`, `obj.await`, `Awaitility.await('alias')`, or rename it.
Declarations, qualified uses and uses later in an expression, such as
`1 + await`, are unchanged. The user guide's "Existing names `async`,
`await` and `defer`" lists the rules.

**Groovy 6.0.1 (GROOVY-12715).** In 6.0.0, a cast in front kept the names
ordinary: `(int) await + 1`, `(String) await(1)` and `(String) async { 1 }`
used the variable or method. A cast now applies to the keyword, as
`(BigDecimal) await(price)` and `(Awaitable<Integer>) async { 7 }` need, so
those forms follow the same rules as without the cast.

### Groovy 6.0.1 — overview of the breaking changes

Code that compiled and ran with 6.0.0 can fail to compile, throw, or give a
different result with 6.0.1 in the cases below, grouped by area. Each has an
entry of its own, in the same order.

| Area | Issues |
|---|---|
| Parsing and statements | GROOVY-12831, GROOVY-12832, GROOVY-12478, GROOVY-12790 |
| Static type checking rejects more code | GROOVY-12714, GROOVY-12525, GROOVY-12484, GROOVY-12428, GROOVY-12599, GROOVY-12568, GROOVY-12569, GROOVY-12526, GROOVY-12717 |
| Static compilation gives different results | GROOVY-12469, GROOVY-12761, GROOVY-12488, GROOVY-12534, GROOVY-12649, GROOVY-12476, GROOVY-12470, GROOVY-12700, GROOVY-12479, GROOVY-12560, GROOVY-12574, GROOVY-12535, GROOVY-12576, GROOVY-12826 |
| Dynamic dispatch and properties | GROOVY-12439, GROOVY-12521, GROOVY-12520, GROOVY-12592, GROOVY-12489, GROOVY-12538, GROOVY-12619, GROOVY-12622, GROOVY-12613, GROOVY-12558, GROOVY-12580, GROOVY-12490 |
| Closures | GROOVY-12673, GROOVY-12667, GROOVY-12578, GROOVY-12829 |
| Traits | GROOVY-12608, GROOVY-12597, GROOVY-12590, GROOVY-12611, GROOVY-12618, GROOVY-12612, GROOVY-12624, GROOVY-12575 |
| Packed closures (incubating) | GROOVY-12658, GROOVY-12666, GROOVY-12670, GROOVY-12660, GROOVY-12663 |
| Libraries and API | GROOVY-12471, GROOVY-12730, GROOVY-10498, GROOVY-12762, GROOVY-11744, GROOVY-12555, GROOVY-8113, GROOVY-12744, GROOVY-12461, GROOVY-12457, GROOVY-12763, GROOVY-12571 |
| groovy-contracts and groovy-typecheckers | GROOVY-12463, GROOVY-12464, GROOVY-12477, GROOVY-12723, GROOVY-12728, GROOVY-12729, GROOVY-12741, GROOVY-12742 |

### Groovy 6.0.1 — `await(x)` binds like a call, and `await` needs its operand on the same line (GROOVY-12831, GROOVY-12832)

Two changes to how `await` (incubating) is parsed:

- A method call, property or index after a parenthesised `await` applies to
  the awaited value: `await(task).toString()` is `(await task).toString()`,
  as `await(task) + 1` already added to the awaited value. Without
  parentheses `await` still takes the whole expression.
- `await` at the end of a line is a name, not the keyword. In 6.0.0 the next
  line became its operand.

**Who is affected.** Code that relied on `await(x).m()` awaiting `x.m()`
(write `await x.m()`), and code with the operand of `await` on the next line
(join the lines, or use `await(` with the arguments on the following lines).

### Groovy 6.0.1 — an early `yield` leaves a statement-position arrow `switch` (GROOVY-12478)

`return`, `break` and `continue` are now allowed in the arrow arms of a
`switch` used as a statement, as in Java; they were a parse error before. With
this, a `yield` that does not end its arm now completes the switch. In 6.0.0
such a `yield` in a statement-position switch was treated as a plain
expression and the arm ran on: `if (flag) yield 'first'` followed by
`yield 'second'` gave `'second'` as the method's result, and any statements
after the early `yield` still ran. It now gives `'first'` and skips them.

**Who is affected.** Code with a `yield` before the end of an arrow arm, in a
switch whose value is not used (or is the method's implicit result), that
relied on the rest of the arm running. Remove the early `yield` or restructure
the arm with `if`/`else`. Switch expressions whose value is used are unchanged.

### Groovy 6.0.1 — multiple assignments in a script with an instance `main` or `run` method behave as elsewhere (GROOVY-12790)

When a script has an instance `main` or `run` method and only declarations
besides, its declarations become fields. A multiple assignment with a list
literal paired targets with elements one by one, so
`def (h, *t) = [1, 2, 3]` set `t` to `2`. One with any other right-hand side
was dropped, together with every declaration after it, so its initializer
never ran. The assignment is now kept in the instance initializer: `t` is
`[2, 3]`, and the initializer runs when the script is created.

**Who is affected.** Such scripts with a rest binding, and scripts whose
dropped declarations had side effects (method calls on the right-hand side),
which now run.

### Groovy 6.0.1 — static type checking applies the wildcard containment rules of JLS 4.5.1 (GROOVY-12714)

Static type checking (`@TypeChecked` and `@CompileStatic`) was more permissive
than `javac` about wildcard type arguments. It accepted code that can pollute
a collection or that `javac` rejects, and it now rejects it:

```groovy
void add(List<? super Integer> l)    { l.add(1.5d) }          // was accepted
void narrow(List<? extends Number> n) { List<? extends Integer> i = n }
void widen(List<? super Integer> n)   { List<? super Number> s = n }
void copy(List<? extends Number> src) { List<Number> dst = src }
void take(Collection<CharSequence> c) { }
void pass(Collection<? extends CharSequence> s) { take(s) }
A `? extends` type argument is contained only in a `? extends` of a supertype,
a `? super` only in a `? super` of a subtype, and an invariant type argument
only in itself. Messages now name the capture
(`capture-of ? super java.lang.Integer`).

**Who is affected (source).** Statically typed code that compiled with 6.0.0
and relied on the looser rules fails with `Incompatible generic argument types`
or `Cannot call ... with arguments`. Fix the declaration: take the wildcard
type (`Collection<? extends CharSequence>`), declare the consumer with the
supertype (`List<? super Number>`), or copy (`new ArrayList<Number>(src)`).
Where a declaration cannot change, mark the affected methods, the caller
included, `@CompileDynamic`. Compiled classes are not affected.

Groovy 6.0.1 also keeps some looser forms that 6.0.0 accepted. In Groovy 7, a
value that is not a `Class` may still be assigned to a parameterized `Class`
(GROOVY-12756), while `getClass()` follows GROOVY-12755 (see below): it is
typed `Class<? extends |T|>`, so `Class<String> c = s.getClass()` is rejected,
as `javac` rejects it.

### Groovy 6.0.1 — type checking rejects a call to a property accessor that is not generated (GROOVY-12525)

Static type checking assumed that every property had both `isX()` and
`getX()`. A call to an accessor the class does not generate is now the error
`Cannot find matching method`. A `boolean` property generates `isX()` and
`getX()`, or only the one the class declares itself; any other property,
`Boolean` included, generates only `getX()`.

**Who is affected.** `@TypeChecked` code that calls such an accessor on a class
with `methodMissing`, such as `b.isName()` for a `String` property `name`: it
reached `methodMissing` at run time and no longer compiles. Read the property
(`b.name`), call the generated accessor, or use `TypeCheckingMode.SKIP`.

### Groovy 6.0.1 — type checking rejects calls through an inaccessible type (GROOVY-12484)

The type checker now reports a static method call or constructor call whose
qualifying type is not accessible from the calling class, such as a
package-private class in another package: `p.Top.greet()` gives "Cannot access
method: greet of class: p.Top", and `new p.Top()` "Cannot access constructor of
class: p.Top". Under `@CompileStatic` these compiled in 6.0.0 and failed at run
time with `IllegalAccessError`; under `@TypeChecked` they compiled and ran
through dynamic dispatch. A static method inherited through an accessible
subclass, class literals, and dynamic code are unaffected.

**Who is affected.** `@TypeChecked` code that calls static methods or
constructors of a type it cannot access. Make the type accessible, call through
an accessible subtype or factory, or mark the method `@TypeChecked(SKIP)`.

### Groovy 6.0.1 — `instanceof` narrowing in a `switch` case does not reach later cases (GROOVY-12428)

Under static type checking, a narrowing made inside one `case` of a `switch`,
by `instanceof` (for example `assert value instanceof String`) or a nested
class label, applied to later cases too, even where the case ended with
`break` or `return`. It now ends with its case; on fall-through only the
switch value's narrowing carries on into a following class label. In 6.0.0
`value.toUpperCase()` in `case 'name':` type-checked because of an
`instanceof String` in the preceding `case 'text':`; it is now "Cannot find
matching method java.lang.Object#toUpperCase()".

**Who is affected.** `@TypeChecked`/`@CompileStatic` code that used a
narrowed type in a case that did not do the narrowing itself. Add an
`instanceof` check or a cast in that case.

### Groovy 6.0.1 — type checking checks the result type of a `Closure<R>` value used as a SAM type (GROOVY-12599)

When a value of static type `Closure<R>` (a variable or a `Closure` subclass,
not a closure literal) is passed or returned where a parameterized functional
interface is expected, the type checker now checks that the interface's method
accepts `R`, as it does for a closure literal. In 6.0.0 a `Closure<Integer>`
passed for `Supplier<String>` compiled; it now gives "Cannot call ... with
arguments [groovy.lang.Closure<java.lang.Integer>]". A raw `Closure`,
`Closure<Object>`, a `void` or `boolean` SAM return, and lossless primitive
conversions are still accepted.

**Who is affected.** `@TypeChecked` and `@CompileStatic` code passing a
mismatched `Closure<R>` that happened to work at run time, for example because
the value was only printed or Groovy converted it on return. Declare the
closure with the right `R` (or as plain `Closure`), or use a closure literal.

### Groovy 6.0.1 — statically compiled code in a `GroovyShell` reports what the JVM refuses across class loaders (GROOVY-12568, GROOVY-12569)

A `GroovyClassLoader` defines each compilation with a class loader of its own,
so a class of an earlier `GroovyShell` parse is in another run-time package
even when its package name is the same. Statically compiled code that used
its protected or package-private members compiled, then failed with
`IllegalAccessError`. With the new `@Incubating` option
`CompilerConfiguration.RUNTIME_PACKAGE_CHECK`, which a `GroovyShell` turns on
for the class loader it creates, such an access is a compile error. So is naming a package-private class of
another run-time package, as in `new Hidden()` or `Hidden.FIELD`.

**Who is affected.** `@CompileStatic` code compiled by a `GroovyShell`,
including scripts run with the `groovy` command, now fails to compile where
it used to fail at run time, also on paths that never ran. Use
`@CompileDynamic` or make the member public, or set the option or the system
property `groovy.runtime.package.check` to `false`. Code compiled to class
files is not affected.

### Groovy 6.0.1 — `Iface.super` needs an interface the class implements, and a direct one under static compilation (GROOVY-12526)

`Iface.super.m()` is now a compile error when `Iface` is neither the current
type nor one of its superinterfaces. That used to fail at run time with
`BUG! ... caller class must be a subclass below the method`. Under
`@CompileStatic` the interface must also be a direct superinterface. An
indirect one used to compile and then fail to load with `VerifyError`.
Dynamic and `@TypeChecked` code may still name an indirect superinterface.

**Who is affected.** Only code where such a call sits on a path that never
ran. It now fails to compile. Remove the call, or name a direct
superinterface.

### Groovy 6.0.1 — static compilation reports protected members of another package that the JVM refuses (GROOVY-12717)

Under `@CompileStatic`, `new A()` of a protected constructor of another
package (outside `super(...)` or an anonymous class) is now a compile error,
"Cannot call protected constructor". So is a protected method of another
package called through a receiver of the superclass type, "Cannot access
method". Both used to make the class fail to load with `VerifyError`. A
method reference `a::m` to such a method used to fail only when evaluated,
with `BootstrapMethodError`.

**Who is affected.** Only a class holding such a method reference that was
never evaluated. It now fails to compile. Use a receiver of the subclass
type, or `@CompileDynamic`.

### Groovy 6.0.1 — statically compiled variables declared wider than their initializer's generic type keep the value (GROOVY-12469)

Under `@CompileStatic`, a local variable declared wider than the generic type
of its initializer, such as `CharSequence cs = list.get(0)` for a
`List<String>`, was converted to that type, so a `GString` element became a
`String`. The variable now holds the value unchanged, as in dynamic Groovy
and Java.

**Who is affected.** For a `GString` element, `cs.equals('ab')` and
`cs instanceof String` are now false (`cs == 'ab'` is still true). Any other
non-`String` element throws `ClassCastException` where a method that only
`String` has is called on it. Variables declared `String`, `def` or `var` are
unchanged.

### Groovy 6.0.1 — instance initializer blocks of `@CompileStatic` classes are statically compiled (GROOVY-12761)

Since Groovy 5.0, method calls in an instance initializer block of a
`@CompileStatic` class were type checked but dispatched dynamically, and in
6.0.0 an anonymous inner class created there was compiled dynamically. Both
are statically compiled again, as in Groovy 4.

**Who is affected.** Calls in such a block to an overloaded method, with an
argument whose static type is wider than its value, now choose the overload
from the static type. Meta-class and category changes no longer apply to
those calls.

### Groovy 6.0.1 — statically compiled constructor references call the constructor directly (GROOVY-12488)

Under `@CompileStatic`, `X::new` selected the constructor at run time through
the meta-class. It is now selected at compile time for the parameter types of
the functional interface, as for `new X(args)` and in Java, and called
directly.

**Who is affected.** References whose result depended on run-time selection.
`Function<CharSequence, X> f = X::new` calls `X(CharSequence)` even for a
`String`. An `Object[]` given to a varargs constructor is passed as one
element. A constructor the enclosing class cannot call no longer wins over
one it can, and a constructor added through the meta-class is not used. The
constructor is still selected at run time when no single constructor matches
(GROOVY-10971), for an enum, and for a protected or package-private
constructor of a class from another class loader.

### Groovy 6.0.1 — `@CompileStatic` property and spread results on collections match dynamic Groovy (GROOVY-12534, GROOVY-12649)

Under `@CompileStatic`, property and spread access on a `List`, `Set` or other
`Iterable` type that declares a property, or gets one from an extension
method, now gives the results of dynamic Groovy and `@TypeChecked`:
`coll.prop` reads the collection's own property, `coll*.prop` reads it from
each element, and `coll*.prop = v` writes it to each element.

**Who is affected.** Statically compiled code that relied on the previous
results gets different ones once recompiled.

### Groovy 6.0.1 — statically compiled `toString()` and `super.toString()` match dynamic Groovy (GROOVY-12476, GROOVY-12470)

Under `@CompileStatic`, `x.toString()` on a receiver whose static type does
not declare `toString()` (an interface, `Object`, `List`, `Map`, ...) binds to
a `DefaultGroovyMethods` extension. That extension now calls the runtime
class's own `toString()` when dynamic dispatch would select it, instead of
always using Groovy's list/map formatting. Similarly, `super.toString()` now
calls the superclass method rather than the extension. In 6.0.0 a `List`-typed
`ArrayList` subclass with its own `toString()` printed `[1]`, a
`CopyOnWriteArrayList` printed `[[k:1]]` and a `Properties` `[k:v]`; they now
print the override's result, `[{k=1}]` and `{k=v}`. In a `@CompileStatic`
`ArrayList` subclass, `'SL:' + super.toString()` gave `SL:[[k:1]]` and now
gives `SL:[{k=1}]`; a plain class whose `toString()` called `super.toString()`
recursed into a `StackOverflowError` and now works.

**Who is affected.** Statically compiled code, and Java or Groovy code calling
`DefaultGroovyMethods.toString(x)` directly, that relied on the Groovy format
for collections and maps whose class declares its own `toString()` (including
JDK classes such as `CopyOnWriteArrayList`, `ConcurrentHashMap` and
`Properties`). Use `org.codehaus.groovy.runtime.FormatHelper.toString(x)`
where the Groovy format is wanted. String concatenation (`'x' + list`) and GString
interpolation keep the Groovy format.

### Groovy 6.0.1 — a statically compiled multiple assignment from a list literal has the list as its value (GROOVY-12700)

Under `@CompileStatic`, a multiple assignment whose right-hand side is a list
literal was rewritten into single assignments everywhere. Its value was then
the last value assigned, not the list, and in a declaration a later value
naming an earlier target read the new local instead of the field or property.
In 6.0.0 `(x, y) = [y, x]` as the last statement of a method returned `1`
(now `[2, 1]`), and with a field `a`, `def (a, b) = [1, a]` set `b` to `1`
(now the field's value). Used as a value elsewhere, for example as a closure's
result, it threw `GroovyCastException`; it now yields the list.

**Who is affected.** Statically compiled methods or closures whose last
statement is a multiple assignment from a list literal now return the list,
and declarations of the form `def (a, b) = [1, a]` now read the field or
property `a`. Dynamic code already behaved this way.

### Groovy 6.0.1 — a statically compiled static member reached through an expression evaluates it (GROOVY-12479)

Under `@CompileStatic`, a static field, property or method reached through an
expression, such as `make().CONST`, `make().sf = v` or `make().stat()`, was
compiled to a bare static access, so the expression never ran. It is now
evaluated and its value discarded, as in Java, dynamic Groovy and
`@TypeChecked`. A class literal, a variable or the implicit `this` is not
evaluated, as before.

**Who is affected.** Statically compiled code whose receiver expression has a
side effect, such as a call that logs, counts or throws. That effect now
happens. Write the class name instead (`K.stat()`) to skip it.

### Groovy 6.0.1 — statically compiled `<=>` of operands of different types compares as dynamic Groovy does (GROOVY-12560)

Under `@CompileStatic`, `a <=> b` with a primitive right operand called the
`compare` method of the left operand's wrapper type even when the types
differed. That narrowed the right operand: `0 <=> 4294967296L` and
`1 <=> 1.5d` gave `0`, and a `null` left `Integer` threw
`NullPointerException`. A left operand with no such method, such as a
`BigDecimal`, `Number` or type parameter, crashed the compiler. Only operands
of the same primitive type now use the wrapper's `compare`. Any other pair is
compared as in dynamic Groovy (`-1` for each example above).

**Who is affected.** Statically compiled `<=>` (and sorting comparators using
it) on mixed `int`/`long`/`double` and similar operands whose values differ
only beyond the left type's range or precision. These now return a non-zero
result.

### Groovy 6.0.1 — a call from an inner class to an inherited outer method uses the nearest enclosing instance (GROOVY-12574)

An implicit-this call from an inner class to a method of an enclosing class
needs the instance whose class has the method. Under `@CompileStatic` the
outermost class was used. So when `Outer` and its inner class `Mid` both
extend `Base`, `base()` called from `Mid.Inner` ran on the `Outer` instance.
Now it runs on the `Mid` instance, as in dynamic Groovy and Java. Property
reads through an inherited getter behave the same. The other cases (where the
outermost class does not inherit the method, or a closure in a field
initializer with `@CompileDynamic` constructors) failed with `VerifyError` or
`GroovyCastException` and now work.

**Who is affected.** Statically compiled inner classes nested two or more
levels deep, where an intermediate and the outer class both inherit the
called method. The result now comes from the intermediate instance. Write
`Outer.this.base()` to keep the old target.

### Groovy 6.0.1 — a value written to a typed map is converted to the map's value type (GROOVY-12535)

Under `@TypeChecked` and `@CompileStatic`, a property or subscript write to a
map whose value type is known (`m.k = v`, `m['k'] = v`, `m.k ?= v`,
`m.with { k = v }`) now converts the value as an assignment to a variable of
that type does. For a `Map<String,String>`, `"$name"` is stored as a `String`
and `123` as `"123"`; for a `Map<String,Long>`, an `Integer` is stored as a
`Long`. Before, the property write stored the value unchanged and the
subscript write was rejected.

**Who is affected.** Code that read such a value back with its original type,
or compared it with `is`. Maps whose value type is not known, and dynamic
code, are unchanged. `groovy.classtag.preemption.disable` keeps the previous
behaviour.

### Groovy 6.0.1 — `Object` methods in statically compiled closures and lambdas use the enclosing object (GROOVY-12576, GROOVY-12826)

Under `@CompileStatic`, an implicit `toString()`, `hashCode()`, `equals()` or
`getClass()` in a closure or lambda was called on the generated closure or
lambda itself. It now uses what dynamic Groovy uses: in a closure, the object
found through its resolve strategy, so `new D().with { toString() }` calls
`D.toString()`; in a lambda, the enclosing instance, as in Java. Under
`@PackedClosures` a closure that makes such a call is no longer packed.

**Who is affected.** Statically compiled code that relied on such a call
returning the closure's or lambda's own string, hash code, equality or class.
Classes compiled by an earlier release keep the old behaviour until they are
recompiled.

### Groovy 6.0.1 — dynamic overload selection prefers `double` over `float` for `BigDecimal` and `BigInteger` arguments (GROOVY-12439)

When a call mixes integral arguments with a `BigDecimal` or `BigInteger`
argument, dynamic method selection now prefers a `double`/`Double` overload
over a `float`/`Float` one. In 6.0.0 the `float` overload could win and the
value was silently narrowed: with `g(float, float)` and `g(double, double)`,
`g(3, 1.1)` computed `3.3000000715255737`; it now gives `3.3000000000000003`.
A `float` method is still chosen when it is the only applicable one, when all
arguments are integral, and over a `double...` varargs method.

**Who is affected.** Dynamic code calling overloads that differ only in
`float` versus `double` with a mix of integral and `BigDecimal`/`BigInteger`
arguments now runs the `double` overload. Cast the argument (`1.1f`) or call
the `float` overload explicitly if that is what was intended.

### Groovy 6.0.1 — `super.x = v` calls a setter of the superclass when it has several (GROOVY-12521)

When a superclass declared more than one `setX` method, `super.x = v` in
dynamic Groovy and under `@TypeChecked` used ordinary dispatch, so a `setX`
the class itself declared or overrode was called instead, and one that
delegated with `super.x = v` recursed until a `StackOverflowError`. The write
now chooses among the superclass's setters by the type of `v`, as
`super.setX(v)` and `@CompileStatic` do. `super.x++` and `super.x += v` write
the same way.

**Who is affected.** Such classes now run the superclass's setter rather than
their own; use `this.x = v` where the class's own setter is wanted. A `setX`
added through the class's `ExpandoMetaClass` or a category no longer receives
`super.x = v`.

### Groovy 6.0.1 — `super.x` reads a property through `isX()` when the class overrides it (GROOVY-12520)

A read of a property that has both `isX()` and `getX()` calls `isX()`, and
that is meant to hold through `super` too. In dynamic Groovy and under
`@TypeChecked`, `super.x` called `getX()` instead when the class overrode
`isX()` or declared a `boolean` property `x`. So with `A` declaring
`isX() { true }` and `getX() { false }`, and `B extends A` overriding
`isX()`, `super.x` in `B` gave `false`. It now calls `A.isX()` and gives
`true`, as Groovy 4 did.

**Who is affected.** Only classes whose `isX()` and `getX()` return different
values. Call `super.getX()` explicitly where the `getX()` result is wanted.

### Groovy 6.0.1 — `super.name()` in a closure, and `Outer.super.name()`, call the inherited method (GROOVY-12592)

A `super` call made from a closure (`{ super.name() }`) or a nested class
(`Outer.super.name()`), for a method the class inherits but does not
override, used the MOP method of the class that declares it. That skipped a
level of the hierarchy: with `B extends A` overriding `name()` and `E extends B`
not overriding it, `{ super.name() }` in `E` called `A.name()`. It now calls
`B.name()`, as `super.name()` outside a closure does. The class now also
generates the `super$` methods its closures and nested classes need, so such a
call never reaches the override of a subclass.

**Who is affected.** Code whose closure `super` calls returned the
grandparent's result. Results now match the same call made outside the
closure.

### Groovy 6.0.1 — a `setProperty` or `getProperty` from another interface's default method receives every access (GROOVY-12489)

Since Groovy 5 (GROOVY-8283), a `setProperty` or `getProperty` that a class
took from a default method of an interface other than `GroovyObject` was
bypassed by `obj.x = v` writes and computed-name reads (`obj."$name"`), which
went straight to the metaclass. Such a method now receives every access, as a
declared method does and as in Groovy 4.

**Who is affected.** Classes implementing such an interface. The method does
not know which class made the access, so a superclass that writes its own
private field on a subclass instance, `other.secret = v`, now throws
`MissingPropertyException`, as the read already did. Use `other.@secret = v`.

### Groovy 6.0.1 — `>` and `>=` with a primitive NaN operand are false (GROOVY-12538)

When both operands are primitive `double` or `float` values, `NaN > x` and
`NaN >= x` were `true`. All four ordered comparisons with NaN are now `false`,
as in Java and as Groovy already gave for `Double` objects. Under
`@CompileStatic` this includes `Double.NaN` and `Float.NaN`, which are
primitive there.

**Who is affected.** Code that relied on NaN comparing greater, such as
`if (reading > limit)` treating a NaN reading as over the limit; use
`Double.isNaN`. Mixed-type comparisons, `==` and `<=>` are unchanged. Classes
compiled by an earlier release keep the old comparison until recompiled.

### Groovy 6.0.1 — the receiver of a compound-assigned or incremented property is evaluated once (GROOVY-12619, GROOVY-12622, GROOVY-12613)

`obj().v += 2` and `obj().v++` evaluated the receiver expression twice, once
for the read and once for the write. It is now evaluated once, as for
subscripts, in dynamic and statically compiled code alike: for every compound
assignment operator, `++` and `--`, attribute access, safe navigation and
extension properties such as `file.text += 'x'`. Under `@CompileStatic`,
`++` and `--` on a non-primitive property or subscript, and in trait code a
compound assignment to a subscript, likewise evaluate each part once: on
`[10, 20]`, `list[i++]++` gave `[21, 20]` and left `i` at 2, and now gives
`[11, 20]` with `i` at 1.

**Who is affected.** Receivers with a side effect, which now runs once, and
receivers that return another object each time, such as
`iterator.next().v += 1`, which now read and write the same object.

### Groovy 6.0.1 — a call site re-links when the metaclass a receiver shares with another class changes (GROOVY-12558)

A class can answer another class's metaclass from `getMetaClass()`, as Spock
mocks and many proxy libraries do. In 6.0.0, changing that metaclass, for
example replacing a method on an `ExpandoMetaClass` set for the stood-in type,
did not re-link call sites that had already dispatched on the proxy, so they
kept calling the old method: `[first, first]` instead of `[first, second]`.
Such call sites now re-link, as they did in Groovy 5. That includes the
reflective cold tier (`groovy.indy.cold.reflection=true`).

**Who is affected.** Code that, through a proxy or mock, relied on getting
the method in place when the call site was first linked. It now gets the
current method.

### Groovy 6.0.1 — `ClassInfo.remove` keeps a class's Groovy methods and drops the metaclasses set for it (GROOVY-12580)

After `ClassInfo.remove(C)`, the class used to get a fresh `ClassInfo` with no
default Groovy or extension methods. Calls on a JDK class then threw
`MissingMethodException` (`sb[0..0] = 'z'` on a `StringBuilder`) or picked a
supertype's overload (`sb << 'x'` could return a new builder). A Groovy class
kept, through its cached `ClassInfo`, a metaclass set on it before the
removal, so `Host.metaClass.perClass = {...}` still answered afterwards. The
class now keeps its methods. The metaclasses set for it and its instances are
removed, so `new Host().perClass()` now throws `MissingMethodException`.

**Who is affected.** Code that calls `ClassInfo.remove` on a class that is
still in use and expects metaclass changes made before the call to survive.
Make the changes again after the removal.

### Groovy 6.0.1 — enum constants with literal arguments are created without the meta class (GROOVY-12490)

An enum whose constants all have literal arguments, and whose constructor
the compiler can pick unambiguously, now creates them with a direct
constructor call rather than through the meta class, as GROOVY-12240 already
did for constants without arguments.

**Who is affected.** Code that registered a meta class or an
`ExpandoMetaClass` constructor for such an enum before it initialized, to
observe or replace its constants. Writing one argument as a non-literal
expression, such as `'one'.toString()`, keeps the old path.

### Groovy 6.0.1 — closure wrappers report and take the delegation settings of the closure they wrap (GROOVY-12673)

The closures returned by `memoize()` and its variants, `trampoline()`,
`curry()` and its relatives, `>>`/`<<` composition and `asWritable()` now
report the `thisObject` of the closure they wrap. The memoize, trampoline and
async wrappers (GROOVY-12679) also report and take its `delegate` and
`resolveStrategy`, so a memoized or trampolined closure works with builders,
`with()` and `tap()`. `dehydrate()` and `rehydrate()` on a wrapper return a
wrapper over the new copy (GROOVY-12665, GROOVY-12669).

**Who is affected.** Code that read `thisObject` from a wrapper, or that set
the delegate of a wrapper and expected the wrapped closure to keep its own
(call `clone()` first). A `wrapAsync` or `wrapAsyncGenerator` closure
serialized by 6.0.0 does not deserialize in 6.0.1. `Memoize` gains two public
overloads, which `japicmp` reports as additions.

### Groovy 6.0.1 — a closure method call skips a null owner or delegate (GROOVY-12667)

After `dehydrate()`, `rehydrate` with a null argument, or `delegate = null`, a
method call in the closure body skips a null owner or delegate instead of
calling on null. With `OWNER_FIRST` and a null owner the delegate's method is
used, and with `DELEGATE_FIRST` and a null delegate the owner's. With no
candidate, a method of the closure itself (`toString()`, `with`, `tap`) runs on
the closure, and any other method throws `MissingMethodException`.

**Who is affected.** Code that catches `NullPointerException` around such a
call, and code that relied on `toString()` or `with` receiving `null` there,
as in 6.0.0. Property access is unchanged.

### Groovy 6.0.1 — a closure's own method called with no usable delegate or owner runs once (GROOVY-12578)

An implicit-this call in a closure body to a method the closure inherits,
such as `toString()`, `with`, `tap` or `identity`, falls back to the closure's
own method when neither the delegate nor the owner can take it. That happens
after `rehydrate(null, null, null)` or `dehydrate()` with `OWNER_ONLY`, or with
`DELEGATE_ONLY` and no delegate. In 6.0.0 the fallback discarded its result
and threw `MissingMethodException`. Compiled code then called the method a
second time, so `{ -> int n = 0; with { n++ }; n }` returned `2`. The method
now runs once and its result is returned (`1`).

**Who is affected.** Closures in that state whose fallback method has side
effects. A direct `metaClass.invokeMethod` on them no longer throws.

### Groovy 6.0.1 — a closure passes a method of `Object` on for a closure nested in it (GROOVY-12829)

A call without a receiver to a method that every object has (`toString()`,
`hashCode()`, `equals`, `getClass()`, and `Object` extension methods such as
`with` and `inspect`) is first tried on the closure's owner and delegate
(GROOVY-11858, Groovy 6.0.0). When the owner is itself a closure, as for a
nested closure or a closure passed to `each` inside another, that enclosing
closure answered the call itself. It now passes the call on by its own
resolve strategy, as for any other name, in dynamic, type checked and
statically compiled code alike:

```groovy
class C {
    String toString() { 'C' }
    def m() {
        def outer = { ->
            def inner = { -> toString() }
            [toString(), inner()]
        }
        outer()
    }
}
assert new C().m() == ['C', 'C']    // was ['C', 'C$_m_closure1@...']
**Who is affected.** Nested closures that call such a method without a
receiver to get the enclosing closure, for example `getClass().name` for the
closure's class name. Use `owner.getClass()`, or name the closure through a
variable.

The rules are stated in full under "Resolution rules" in
[`src/spec/doc/core-closures.adoc`](src/spec/doc/core-closures.adoc).

### Groovy 6.0.1 — a lambda in a statically compiled trait reads a super trait's static accessor directly (GROOVY-12608)

In a lambda in a `@CompileStatic` trait, the property form of a super trait's
static accessor (`cnt` for `static int getCnt()`, or for a static property
`cnt`) is now a call of that accessor. In 6.0.0 the lambda resolved the name
at run time, and a lambda passed as a closure took it from the closure's
delegate.

**Who is affected.** A statically compiled trait that relied on that, as in
`p.with((x) -> cnt)` where `p` has a `cnt` property. Use a closure
(`p.with { cnt }`) or qualify the name (`x.cnt`).

### Groovy 6.0.1 — trait static state used through the trait itself throws `MissingFieldException` (GROOVY-12597)

A trait's static fields exist only in the classes that implement it. Reading or
writing one through the trait itself, as in `T.count`, or `T.m()` where `m`
uses a static field of `T`, now throws `MissingFieldException`. In 6.0.0 it
threw a `MissingMethodException` naming an internal accessor such as
`T__count$get`. The trait interface now declares a synthetic public static
accessor for each such field.

**Who is affected.** Code that catches `MissingMethodException`, or reads its
message, around such a call. Catch `MissingFieldException` (or
`GroovyRuntimeException`), or call through an implementing class.

### Groovy 6.0.1 — a statically compiled trait reads its static accessor property on the implementing class (GROOVY-12590)

Under `@CompileStatic`, trait code that used a static accessor of the trait
with property syntax, such as `ready` for `isReady()` or `cnt = 1` for
`setCnt(int)`, called it with the trait itself as the class. An accessor that
reads the trait's static state threw `MissingMethodException`. One that uses
the class, such as `static String getWho() { this.simpleName }`, returned the
trait's name. It is now called with the implementing class, as the method
call form `getWho()` already was.

**Who is affected.** Statically compiled trait code that reads a static
accessor as a property and depends on the class it runs for. Such an accessor
now sees the implementing class (`C`, not `T`).

### Groovy 6.0.1 — a private or static trait method called from a closure is resolved through the closure's delegate (GROOVY-12611, GROOVY-12618)

In a closure in trait code, an implicit-this call to a private or static
method of the trait, or to a public static method of a super trait, went
straight to the trait. The closure's delegate was never consulted. It is now
resolved as in a class: a `DELEGATE_FIRST` closure such as `with` calls the
delegate's method when the delegate has one. So in a trait with
`private int size() { 1 }`, `[5, 6].with { size() }` now gives `2`, not `1`.
This applies in dynamic and statically compiled code.

**Who is affected.** Trait code that calls its own private or static methods
inside `with`, `tap`, builders or other delegating closures, where the
delegate has a method of the same name. In dynamic code, a delegate with an
ambiguous no-match overload set can now throw, as it does in a class. Call
the method outside the closure, or qualify it (`T.size()` for a static).

### Groovy 6.0.1 — an explicit `this.m()` in a trait closure calls the implementing object (GROOVY-12612)

In a closure or lambda in trait code, an explicit `this.m()` called the
trait's own method, ignoring an override in the implementing class. Under
`@CompileStatic` it threw `ClassCastException`. It now calls `m` on the
implementing object, never on the closure's delegate, as `this.m()` does
outside the closure. So with `class C implements T { String getName() { 'C' } }`,
`{ -> this.getName() }()` in `T` now gives `'C'`, not `'T'`.

**Who is affected.** Dynamic trait code that calls `this.m()` inside a closure
for a method the implementing class overrides. The override is now used.

### Groovy 6.0.1 — a statically compiled trait closure no longer resolves the trait's own calls on the delegate (GROOVY-12624)

Under `@CompileStatic`, the `$self.getClass()` and trait helper calls that the
trait transformation adds to a closure in trait code could be resolved
against the closure's delegate. In `new Object().with { who() }`, with
`static String who() { this.simpleName }`, `who()` ran for `Object`, and a
delegate method `who(Class, int)` was called in place of the trait's `who(int)`.
These calls now target the implementing class or object (`C`), as in dynamic
code.

**Who is affected.** Statically compiled trait code calling a static trait
method, or using trait static state, inside a delegating closure. It now sees
the implementing class instead of the delegate.

### Groovy 6.0.1 — a trait's instance initializer blocks run once under joint compilation (GROOVY-12575)

When Groovy and Java sources were compiled together (`groovyc -j`, or the
Gradle and Maven joint compilation), the stub generator duplicated a trait's
instance initializer blocks. So each block ran twice per instance of an
implementing class: `trait T { List log = []; { log << 'block' } }` gave
`[block, block]`. Each block now runs once, as without joint compilation.
Field initializers and static initializer blocks were not affected.

**Who is affected.** Joint-compiled projects with traits that have instance
initializer blocks with side effects. Code written around the double run
(such as halving a counter) must drop that workaround.

### Groovy 6.0.1 — packed closures access owner properties through their accessors (GROOVY-12658)

With `@PackedClosures` or the `groovy.target.closure.pack` flag (both
incubating), a packed closure body is a method of the owner class, and `x` or
`this.x` in it read and wrote the owner's field directly, skipping a custom
`getX()`/`setX()` or `getProperty`. It now goes through the accessor, as the
closure class it replaces does; `this.@x` still accesses the field. With a
`getE()` that returns 100 over a field holding 1, `{ -> e }()` gave `1`.

**Who is affected.** Packed closures in classes whose property accessors do
more than return or set the field now see the accessor's result and effects.

### Groovy 6.0.1 — a rehydrated packed closure runs against its new this object (GROOVY-12666, GROOVY-12670)

A packed closure (incubating) that uses its owner kept running against the
instance it was created on after `rehydrate`. A closure class runs against
the new this object, which a `@CompileStatic` packed closure now does too,
throwing `ClassCastException` when that object is not an instance of the
enclosing class. Under the `groovy.target.closure.pack` flag, a dynamic
closure that names `this` (`this.foo()`, `this.@f`, `Outer.this`, …) is no
longer packed, so it also uses the new this object. A packed body that does
not use its owner still runs as before.

**Who is affected.** Code that rehydrated such packed closures onto another
object and relied on them still answering for the original owner. Such a
copy now reads the new object's members, or throws `ClassCastException` for
an unrelated object under `@CompileStatic`.

### Groovy 6.0.1 — packed closures that call a method of the closure itself are not packed (GROOVY-12660, GROOVY-12663)

A packed closure (incubating) body runs as a method of the owner class, so a
call resolved against the closure itself went to the owner: under
`@CompileStatic`, `toString()`, `hashCode()`, `equals()` or `getClass()` with
the `TO_SELF` strategy, and in dynamic code a method or property `Closure`
declares, such as `curry`, `call` or `maximumNumberOfParameters`. These
closures are no longer packed, so the closure answers, as it does without
packing.

**Who is affected.** Owners that declare such a method or property and had
packed closures reach it, for example a `maximumNumberOfParameters` property
of 7 read by `{ -> maximumNumberOfParameters }` (now 0). Qualify the call
with `this.` or rename the member.

### Groovy 6.0.1 — `abs()` on a `Number` receiver keeps the number's precision (GROOVY-12471)

`DefaultGroovyMethods.abs(Number)` returned `int`, truncating a `Long`,
`BigInteger`, `BigDecimal` or floating-point value whose static type was
`Number`, as `@CompileStatic` code such as `(a - b).abs()` always found. It now
returns a `Number` computed with the type rules of the arithmetic operators,
and new `abs(Integer)`, `abs(Short)` and `abs(Byte)` overloads return `int`.

**Who is affected.** `int x = DefaultGroovyMethods.abs(n)` in Java, and
`int x = n.abs()` for a `Number n` under `@CompileStatic`, no longer compile;
add `.intValue()` where the narrowing is wanted. A `Float` reached through a
`Number` receiver now gives a `Double`. Classes compiled against earlier
releases still link to a synthetic bridge with the old result.

### Groovy 6.0.1 — `@IndexedProperty` adds no indexed setter to a record (GROOVY-12730)

An `@IndexedProperty` component of a record, or property of a `@RecordType`
or `@RecordBase` class, now gets only the indexed getter `getX(int)`, as an
`@Immutable` class's property already did. Earlier versions also generated
the indexed setter `setX(int, V)`.

**Who is affected.** Code that calls `setX(int, V)` on such a record no longer
compiles under static compilation or Java, and fails with a
`MissingMethodException` in dynamic Groovy. `japicmp` reports the method as
removed.

### Groovy 6.0.1 — `@NamedVariant` keeps explicit falsy arguments and generates default-argument variants (GROOVY-10498, GROOVY-12762)

The map variant generated by `@NamedVariant` now passes a positional argument
through unchanged. In 6.0.0 it re-applied the parameter's default with `?:`,
so for `m(@NamedDelegate Color c, Number alpha = 4.5)` the call
`m(r:1, g:2, b:3, 0.0)` used `4.5`; it now uses `0.0` (and `null` stays
`null`). The map variant also gets its own default-argument variants, so
`m(r:1, g:2, b:3)` now works instead of throwing `MissingMethodException`, and
default values referencing a `@NamedDelegate` or an earlier parameter now work.
Two forms that compiled in 6.0.0 are now errors: a declared member with the
same signature as one of the new default-argument variants (such as
`m(Map)` beside `@NamedVariant m(@NamedParam String n, int x = 3)`), and a
positional parameter whose default references a `@NamedDelegate` parameter
(`Color d = c`).

**Who is affected.** Callers passing `0`, `false`, `''` or `null` to a
positional parameter with a default through the map variant now get that
value. Classes with a clashing `m(Map)` member must rename one of the methods
or drop the default; a positional default that references a `@NamedDelegate`
parameter must be made a `@NamedParam` or computed in the method body.

### Groovy 6.0.1 — a repeated `_` is an ordinary variable holding the last value (GROOVY-11744)

A `_` repeated in a declaration or a closure parameter list is `_` declared
again, holding the last value, as in Groovy 5. Version 6.0.0 lost this
unintentionally. `_` may now also be redeclared in the same or an enclosing
scope.

### Groovy 6.0.1 — the default-package lookup before the default imports is an opt-in (GROOVY-12555)

The change described under Groovy 7 is in 6.0.1 behind the same two
options, both off by default: the resolution order of 6.0.0 is unchanged
unless the optimization option `defaultPackageFirst` is `true`, and with it
only the compiled classes of the default package come before the default
imports unless `defaultPackageSources` is `true` as well. Set them through
the system properties `groovy.resolve.default.package.first=true` and
`groovy.resolve.default.package.sources=true`, a configuration script, or,
in Gradle, `groovyOptions.optimizationOptions['defaultPackageFirst'] = true`,
the only one of the three that Gradle passes on to the compiler. An
incremental build that compiles a default-package class against previously
compiled classes of the default package needs `defaultPackageFirst` for the
same source to compile to the same class as in a full build, and does not
need `defaultPackageSources`.

### Groovy 6.0.1 — anonymous scripts no longer go through the overloads that take a file name (GROOVY-8113)

To stop generated script class names from being retained by parent loaders,
`GroovyShell.evaluate(String)`, `evaluate(Reader)`, `parse(String)`,
`parse(Reader)` and their variants without a file name, and
`GroovyClassLoader.parseClass(String)`, now build a `GroovyCodeSource` marked
as name-generated. They no longer delegate to `evaluate(String, String, String)`,
`parse(String, String, Binding)`, `parse(Reader, String)` or
`parseClass(String, String)`, so subclasses that override those overloads no
longer see anonymous scripts. Loading `<generated name>Customizer`, such as
`Script1Customizer`, through the script's loader now throws an internal
`RuntimeException` instead of `ClassNotFoundException` when no such class was
defined. For a generated name, a custom `groovy.runtime.metaclass.Script1MetaClass`
and a `Script1BeanInfo` are no longer looked up.

**Who is affected.** `GroovyShell`/`GroovyClassLoader` subclasses that
intercept those overloads: override `parse(GroovyCodeSource, Binding)` or
`parseClass(GroovyCodeSource)` instead (`isNameGenerated()` tells the cases
apart). Code that catches `ClassNotFoundException` for such names, or relies
on companions for generated names, should name the script explicitly.

### Groovy 6.0.1 — `ListHashMap` views are live and read-only, so node metadata reads stay current (GROOVY-12744)

`org.codehaus.groovy.util.ListHashMap`, which stores AST node metadata,
returned snapshot views. In list mode, `keySet()`, `entrySet()` and `values()`
were mutable copies: changes to them were silently lost, and later puts were
missing from them. As a result, `ASTNode.getNodeMetaData()` and
`copyNodeMetaData` could return stale contents: after a second
`putNodeMetaData`, `getNodeMetaData()` still gave `[a:1]`. The views now
reflect later changes, and every view is read-only, so modifying one throws
`UnsupportedOperationException`.

**Who is affected.** AST transforms and other code that used these views to
modify the map, or relied on them as snapshots. Use `put`/`remove` on the map
itself, or copy the view.

### Groovy 6.0.1 — `@GrabConfig` system properties follow `Grape.enableGrapes` (GROOVY-12461)

The `systemProperties` of `@GrabConfig` are set at run time only when grapes
are enabled. 6.0.0 decided this from the system property
`groovy.grape.enable` at the time the script ran. 6.0.1 uses
`Grape.getEnableGrapes()`, so after `Grape.setEnableGrapes(false)` the
properties are no longer set, though 6.0.0 still set them. A
`-Dgroovy.grape.enable=false` given at startup disables them on both versions.

**Who is affected.** Applications that turn grapes off programmatically but
rely on `@GrabConfig` to set system properties. Set those properties
directly instead.

### Groovy 6.0.1 — parallel collections run on the pool in scope (GROOVY-12457)

Parallel collection methods (incubating) run on the pool bound with
`Pool.withCurrent`. In 6.0.0 a pool not backed by a `ForkJoinPool`, such as
`Pool.virtual()` or `Pool.io()`, was silently replaced by the common
fork-join pool.

**Who is affected.**

- Callbacks under `Pool.virtual()` or `Pool.io()` now run on that pool. A
  short-circuit or a failing sibling interrupts callbacks still running there,
  which closes an interruptible channel they are blocked on.
- `Pool.current()` returns the pool on its own worker threads, where it
  returned `null`.
- `groupByParallel` returns its keys in first-seen order, and `grepParallel`
  keeps an element only when `isCase` is Groovy-true, as `groupBy` and `grep`
  do.

### Groovy 6.0.1 — async constructs rethrow a checked exception as itself (GROOVY-12763)

In 6.0.0 `await` rethrew a checked exception from an async task unchanged,
but a scope's child task failure, a `defer` action, a `withScope` or
generator body, and `for await` over a `Flow.Publisher` wrapped it in
`RuntimeException` or `UndeclaredThrowableException`. All of them now rethrow
the original exception, as `await` does. Async/await is incubating.

**Who is affected.** Code that catches `RuntimeException` or
`UndeclaredThrowableException` around these constructs and reads
`getCause()`. Catch the checked exception directly. Java callers of
`AsyncScope.close()` and `withScope` catch `Exception` and test its type.

### Groovy 6.0.1 — a late subscriber of `BroadcastChannel.asPublisher()` completes (GROOVY-12571)

A subscriber of `BroadcastChannel.asPublisher()` that arrived after the
broadcast had closed received `onError(ChannelClosedException)`. It now ends as
a subscriber attached before the close does: `onComplete` after `close()`, and
`onError` with the failure itself after `close(failure)`.

**Who is affected.** Code that caught `ChannelClosedException` from such a
publisher. `BroadcastChannel.subscribe()` on a closed broadcast still throws
it.

### Groovy 6.0.1 — postconditions and class invariants are checked on a trailing implicit return after an early return (GROOVY-12463)

When a method with an `@Ensures` postcondition, or in a class with an
`@Invariant`, had an explicit early `return` and ended with an implicit return
value, the trailing value was returned without the postcondition or the class
invariant being checked. `if (early) return 1; -1` under
`@Ensures({ result > 0 })` returned `-1`. Both are now checked on that path as
on every other.

**Who is affected.** Methods whose trailing value violates their postcondition
or leaves the object violating its invariant now throw
`PostconditionViolation` or `ClassInvariantViolation` where they returned.

### Groovy 6.0.1 — `PurityChecker` reports mutating operators in `@Pure` methods (GROOVY-12464)

The checker rejected a mutating call such as `s.add(v)` on a parameter, field
or `this`, but accepted the same mutation written as an operator. Operators
that stand for a method call are now checked as that call: `s << v`
(`leftShift`), `a[i] = v`, `a[i] += v` and `a[i]++` (`putAt`), and a compound
assignment that resolves to a `*Assign` method such as `plusAssign`, including
one renamed with `@OperatorRename`. Building up a local value is still accepted.

**Who is affected.** `@Pure` methods checked by `PurityChecker` that mutate a
parameter, field or `this` through one of these operators no longer compile.
Remove `@Pure`, copy into a local first, or allow the effect.

### Groovy 6.0.1 — `@Modifies` needs a closure, and `@Modifies({ [] })` declares an empty frame (GROOVY-12477, GROOVY-12723)

`@Modifies({ [] })` left no frame on the method, so `ModifiesChecker` and the
`@Ensures` `old` validation treated it as if there were no `@Modifies`. It
now declares that the method modifies nothing, as the newly accepted
`@Modifies({})` does. A bare `@Modifies`, and any value that is not a closure,
such as `@Modifies(['this.count'])` or `@Modifies('this.count')`, compiled with
nothing checked; each is now a compile error that names the value found.

**Who is affected.** Methods with `@Modifies({ [] })` that assign a field
(under `ModifiesChecker`) or whose `@Ensures` reads `old.x` no longer compile;
list the targets in the closure. Bare or non-closure `@Modifies` values must
be rewritten as a closure, for example `@Modifies({ this.count })`.

### Groovy 6.0.1 — a class literal as a contract's value is a compile error (GROOVY-12728, GROOVY-12729)

The contract annotations declare their value as a `Class`, so a class literal
such as `@Requires(Object)` passed the annotation checks, but the contract was
then taken for an already generated condition class and never checked. The
same held for `@Ensures`, a class `@Invariant`, a loop `@Invariant`, and loop
and method `@Decreases`. Each now reports "requires a closure containing the
condition". An empty `@Invariant({})` or `@Decreases({})` now compiles with a
warning that nothing is checked.

**Who is affected.** Code that put a class literal in one of these annotations
no longer compiles. It was never checked; replace it with a closure holding
the condition, or remove the annotation.

### Groovy 6.0.1 — contracts on traits and on trait methods with a body are rejected (GROOVY-12741)

The contracts transform treated a trait as a class. `@Requires`, `@Ensures`,
`@ThrowsIf` and `@Decreases` on a trait method with a body already failed,
at compile time or on every call. A trait-level `@Invariant` failed when the
trait had fields, but on a trait without fields it compiled and was checked.
All of them are now compile errors naming the annotation and the trait or
method. Contracts on abstract trait methods keep working.

**Who is affected.** A trait annotated with `@Invariant` no longer compiles.
Put the invariant on the implementing classes instead.

### Groovy 6.0.1 — trait methods of a class with an `@Invariant` return their value (GROOVY-12742)

In a class with a class `@Invariant`, the forwarders and property accessors
generated for the traits it implements returned `null`, because the invariant
check was appended after the bare return expression, and a primitive return
threw `GroovyCastException`. The invariant is now checked before the value is
returned. A `@Synchronized` method with an explicit `return`, which was left
unchecked, is now checked against the invariant too.

**Who is affected.** Code that called such trait methods and got `null` now
gets the method's result. A `@Synchronized` method that leaves the object
violating its invariant now throws `ClassInvariantViolation`.

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

### Groovy 7 — a property's accessor is chosen over an interface's abstract accessor (GROOVY-12552)

When an interface declares an abstract getter or setter for a property that a
class declares, and a call's receiver is inferred to be a subclass of that
class (`Base b = new Impl()` is inferred to be an `Impl`), static type
checking chose the interface's method if the class was compiled together with
the call, and the class's accessor if the class was already compiled. It now
chooses the class's accessor either way, so the same source compiles to the
same class file whether or not the class is compiled with it.

- **`obj.x = v` calls the class's setter again.** From 5.0.0, the interface's
  setter was called when compiled together (GROOVY-11381); Groovy 4 called
  the class's setter.
- **`obj.getX()` and `obj.setX(v)` call the class's accessor.** Every earlier
  version called the interface's method when compiled together.
- **A getter call can have a narrower type.** Where the interface declares a
  wider type than the property, such as `Object getName()` for a `String name`
  property, or `T getValue()` of `Holder<Number>` for an `Integer value`
  property, a call of the getter now has the property's type, as reading the
  property (`obj.name`) already did. `obj.getName().toUpperCase()` now type
  checks. Statically compiled overload selection can change with no compile
  error: `h(obj.getName())` with `h(Object)` and `h(String)` overloads selects
  `h(String)` where it used to select `h(Object)`.

**Who is affected.** `@CompileStatic` code compiled together with such a class
now calls the class's accessor with `invokevirtual` rather than the
interface's method with `invokeinterface`. The same method runs, unless a
different overload is selected. Type checked (dynamically dispatched) code
selects its overload by the runtime type, so only its inferred type changes.
A call compiled against the class file, a receiver inferred to be the class
that declares the property, and dynamic Groovy behave as before. There are no
API changes.

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

### Groovy 7 — casting and `as` share default collection and map implementations (GROOVY-12585)

A collection, array, stream or map that is assigned or cast to a collection or
map type (`T x = value`, `(T) value`) and one coerced with `as` now get the
same result. A value that is already an instance of the target is returned
unchanged, as before. Otherwise an interface target gets a default
implementation:

| Target | Default |
|---|---|
| `List`, `Collection`, `Iterable` | `ArrayList` |
| `Set` | `LinkedHashSet` |
| `SortedSet`, `NavigableSet` | `TreeSet` |
| `Queue`, `Deque` | `LinkedList` |
| `BlockingQueue` | `LinkedBlockingQueue` |
| `SortedMap`, `NavigableMap` | `TreeMap` |
| `ConcurrentMap` | `ConcurrentHashMap` |

A concrete collection class gets its copy constructor, or else its no-arg
constructor followed by `addAll`, before the elements are tried as
constructor arguments.

- **More assignments succeed.** `SortedSet s = list`, `Queue q = list`,
  `Deque d = list`, `Stack s = list`, `List l = aSet`, `SortedMap m = map` and
  `Iterable i = anArray` failed with `GroovyCastException`; they now succeed.
- **`as` returns a real collection instead of a proxy** for `NavigableSet`,
  `Deque`, `BlockingQueue`, `SortedMap`, `NavigableMap` and `ConcurrentMap`,
  and for `Iterable` and `Deque` with an array source. The proxy delegated to
  the original collection or map without providing the interface's behaviour,
  so `([3, 1, 2, 1] as NavigableSet).size()` was 4 and
  `([b: 2, a: 1] as SortedMap).firstKey()` threw. The result is now a copy, so
  code relying on the proxy seeing later changes to the original sees a
  snapshot.
- **`anArray as Set` keeps element order**, giving a `LinkedHashSet` rather
  than a `HashSet`, as `Set s = anArray` and `aList as Set` already did.
- **A single-element list assigned to a collection class fills it.**
  `Vector v = [5]`, `PriorityQueue q = [5]` and `ArrayDeque d = [5]` passed the
  element to the class's capacity constructor and gave an empty collection.
  They now contain the element, as `[5] as Vector` already did. For
  `@CompileStatic` code, see GROOVY-12643 below.

**What is unchanged.** Targets that already worked, such as `Set`, `List`,
`HashSet` (still a `LinkedHashSet` on assignment), `TreeSet`, `ArrayList`,
`LinkedList`, `TreeMap` and the JDK 21 sequenced types. A map coerced with
`as` to a non-collection interface, such as `Runnable` or `Comparator`, is
still a proxy that calls the map's closures. `TreeSet` and `TreeMap` defaults
throw for incomparable or `null` elements and keys, as `as SortedSet` and
`TreeSet t = list` already did. The method signatures are unchanged, so
`japicmp` does not report this.

### Groovy 7 — type checking of list and map literals assigned to collection types (GROOVY-12643)

Under `@TypeChecked` and `@CompileStatic`, a list or map literal assigned to a
collection or map type now follows the defaults of the runtime cast (see
GROOVY-12585 above).

- **Interface targets compile.** A list literal assigned to `SortedSet`,
  `NavigableSet`, `Queue`, `Deque` or `BlockingQueue`, and a map literal
  assigned to `SortedMap`, `NavigableMap` or `ConcurrentMap`, used to be a
  compile error ("Cannot assign value of type ArrayList…" or "Cannot find
  matching constructor"). It now compiles. The variable's inferred type is the
  default implementation, as `Set` infers `LinkedHashSet`, and the elements
  are checked against the declared type arguments.
- **A collection class copies the elements.** For a list literal assigned to a
  concrete collection class with a constructor taking a collection, or a
  no-arg constructor, the list-constructor rule no longer passes the elements
  as constructor arguments; the elements are copied, and checked against the
  declared type arguments. So `LinkedList l = [3, 1]` and `Stack s = [3, 1]`
  now compile, and under `@CompileStatic`, `Vector v = [5]`,
  `PriorityQueue q = [5]` and `ArrayDeque d = [5]` hold the element instead of
  being empty with that capacity.

**Who is affected.** Statically compiled code that used a one-element literal
to set a capacity, such as `Vector v = [10]`, now gets a one-element
collection, and with type arguments, such as `ArrayDeque<String> d = [16]`, it
is a compile error. Write `new ArrayDeque(16)` for a capacity.

**What is unchanged.** Other classes still use the list-constructor rule, such
as `Dimension d = [100, 200]`, as does a collection class whose only
constructors take the elements. Assigning a variable of another collection
type, such as `SortedSet s = aList`, is still an error. For a literal returned
from a method, see GROOVY-12644 below.

### Groovy 7 — type checking of list and map literals returned from a method (GROOVY-12644)

Under `@TypeChecked` and `@CompileStatic`, a list or map literal returned from
a method with a declared return type is now checked as it is when assigned to a
variable of that type. Before, it was accepted only when the literal's own
class (`ArrayList` or `LinkedHashMap`) was assignable to the return type, so
`Set m() { [3, 1] }`, `TreeMap m() { [b: 2, a: 1] }` and
`Point m() { [1, 2] }` were compile errors ("Cannot return value of type …
for method returning …"). They now compile and return the value the
declaration would hold, with the elements checked against the declared type
arguments.

**Who is affected.** Only code that did not compile before; a return that was
accepted is unchanged. A closure or lambda result, a non-literal value such as
`Set m() { aList }`, and a literal inside another expression, such as
`b ? [1] : [2]`, are still checked as before.

### Groovy 7 — values returned to a functional interface type are checked as a method's return (GROOVY-12616, GROOVY-12617, GROOVY-12623)

Under `@TypeChecked` and `@CompileStatic`, a value that a closure, lambda,
method reference (`::`) or method pointer (`.&`) returns to a functional
interface whose method returns a primitive type is now checked as the `return`
of a method declared with that type. A number may be widened, but not narrowed
unless it is a constant that fits; `char` is not converted to a number type,
and only an `int` or a one-character string constant is converted to `char`;
and any value is accepted for `boolean`, by Groovy truth. A method reference or
method pointer returning a number to a wrapper type, as in `Supplier<Byte>`, is
checked for loss of precision too. Under `@CompileStatic`, the value is
converted as that method would convert it.
Dynamically compiled code is unaffected.

**No longer rejected, or no longer failing at run time.**

- A closure or lambda may return a value that widens, as in
  `ToLongFunction<String> f = s -> s.length()`, a constant that fits, as in
  `IntSupplier s = () -> 7L`, a `BigDecimal` for `double` or `float`, or an
  `int` for `char`. These were rejected with "Cannot return value of type …".
- Under `@CompileStatic`, a method reference needing Groovy truth, as in
  `Predicate<String> p = H::str`, `BigDecimal` to `double` or `float`, or `int`
  to `char` compiled but threw `BootstrapMethodError`, or `ClassCastException`
  for a method returning a type parameter. It now runs.

**Now rejected at compile time.** The following compiled before:

- A method pointer assigned to a functional interface was not checked, and its
  value was converted at run time as by a cast. For example, with
  `static long longValue()` and `static String str() { 'A' }`,
  `IntSupplier s = H.&longValue` narrowed the value, and `IntSupplier s = H.&str`
  returned 65. It is now checked as a method reference is, in both modes. A
  method pointer passed as an argument was already checked, but narrowing was
  allowed. A method pointer to a method returning a type parameter, as in
  `ToIntFunction<Long> f = H.&id`, is checked against the type argument
  inferred from the functional interface's parameter types, as a method
  reference is. The method pointer is still a `Closure`, and which of several
  overloaded methods it calls is still decided at run time.
- A method reference to a method returning a type parameter was not checked,
  as in `ToIntFunction<Long> f = H::id` for `static <T> T id(T t)`. Under
  `@CompileStatic`, a narrowing ran and truncated the value through
  `Number.intValue()` and similar; under `@TypeChecked`, every conversion ran as
  a cast, including `String` and `char` to a number. The type argument is now
  inferred from the functional interface's parameter types and checked.
- Under `@TypeChecked`, a method reference that narrows, as in
  `IntSupplier s = H::longValue`, converted the value at run time. Under
  `@CompileStatic` it threw `BootstrapMethodError`, so there it now fails
  earlier rather than differently.
- A method reference or method pointer whose number value would be narrowed
  for a wrapper return type, as in `Supplier<Byte> s = H::v` or `H.&v` for
  `static int v()`, is now reported as "Possible loss of precision", as a
  method `Byte m() { H.v() }` already was. Before, `s.get()` returned an
  `Integer`.

To keep a narrowing, convert explicitly, as in
`IntSupplier s = () -> (int) H.longValue()`.

**What is unchanged.** A functional interface whose method returns a reference
type, such as `Supplier<Long>`, still requires the returned value's type to be
compatible, so `Supplier<Long> s = () -> 1` remains an error (GROOVY-11414).

### Groovy 7 — a trait's static accessor read as a property is a call of the accessor (GROOVY-12614)

In the body of a trait method, the property form of a static accessor that
the trait declares (`label` or `this.label` for `static String getLabel()`)
is now a call of that accessor, exactly as the method form `getLabel()` is.
A static method of a trait is not overridden by a static method of the
implementing class unless the trait marks it `@Virtual`, so the property form
no longer uses an accessor that the implementing class declares. Before,
dynamic and `@TypeChecked` code used the implementing class's accessor for the
property form and the trait's own for the method form, and `@CompileStatic`
code used the trait's own for both. Assignments (`label = v`) and compound
assignments (`label += v`) call the trait's setter and getter in the same way.

**Who is affected.** Dynamic or `@TypeChecked` trait code that reads or
assigns a static accessor of the trait in the property form and relies on an
implementing class supplying its own static accessor of the same name. Mark
the trait's accessor `@Virtual` to keep that behaviour:

```groovy
trait Described {
    @Virtual static String getLabel() { 'default' }   // was: static String getLabel()
    static String describe() { "label is $label" }
}
class Special implements Described {
    static String getLabel() { 'special' }
}
assert Special.describe() == 'label is special'
```

A `@Virtual` static is not available through the trait itself, so
`Described.label` and `Described.getLabel()` must be written with an
implementing class instead.

**What is unchanged.** The method form, a `@Virtual` accessor, a declared
static property (`static String label = 'x'`, which trait code always reads
from the trait's own field), instance accessors and properties, names in a
closure or lambda (which the closure resolves), `++` and `--`, and access from
outside the trait (`Special.label`).

### Groovy 7 — a declared trait property in a closure is read and written through its accessor (GROOVY-12598)

In a closure in a trait method, the name of an instance property that the
trait declares is resolved by the closure, through its delegate and owner,
and `this.name` is a call of the accessor on the implementing instance.
Before, both went straight to the trait's backing field. This is what the
same names mean in a closure in a class that declares the property.

**Who is affected.** A trait that declares a property and uses it in a
closure, when an implementing class declares its own accessor for that
property, or when the closure's delegate has a property of the same name and
the closure's resolve strategy consults the delegate first. In a closure
within a lambda the name is the accessor of the implementing instance, and the
delegate is not consulted. A getter or
setter that the trait itself declares for the property and that uses the
property in a closure now calls itself; use the property outside the closure
there.

**What is unchanged.** The property in a method body or a lambda, which is
the trait's backing field; private and static fields of the trait; and
`getName()` and `setName(v)`.

### Groovy 7 — a final `def` field is read with the type of its value (GROOVY-12315)

Within the class that declares it, a read of a final field declared without a
type has the type of the value assigned by its initializer, its initializer
blocks and its constructors, as a local variable does. The field keeps `Object`
as its declared type. The flow type applies where the field itself is read: in
a method, constructor or initializer body of the declaring class, and in a
closure or lambda there too for a private field, a static property, or a
property of a `final` class with a generated getter. Any other read through
the property getter keeps the declared type, because a subclass may override
the getter: from a closure or lambda in the class, an inner class, a subclass
or another class. A value of no one type (`Object`, or a bound of
several types assigned by different constructors) gives no flow type, nor does
a field written by a compound assignment, `++`, `--` or a tuple assignment.

- **More programs compile.** `final bar = 'abc'` followed by `bar.length()` in
  a method of the class type checks without a cast.
- **Statically compiled overload selection can change.** `m(bar)` with
  `m(Object)` and `m(String)` overloads now selects `m(String)`. Type checked
  (dynamically dispatched) code already selected by the runtime type.
- **An `instanceof` that cannot be true is an error**, as for a local variable:
  `bar instanceof Integer` when `bar` was assigned a String.

The field descriptors are unchanged, so `japicmp` does not report this.

### Groovy 7 — groovy-yaml, groovy-toml and groovy-csv use Jackson 3 (GROOVY-12433)

The YAML, TOML and CSV modules now read and write with Jackson 3
(`tools.jackson`) instead of Jackson 2 (`com.fasterxml.jackson`). None of them
exposes a Jackson type, so calling code compiles unchanged and `japicmp` does
not report this. The behaviour changes below are what users have to check.

**Unquoted YAML values resolve by the JSON schema.** Jackson 3's YAML parser
fixes its scalar resolver, and it cannot be configured. Only lowercase `true`,
`false` and `null` and plain decimal numbers take a type. That is stricter than
the YAML 1.2 core schema most parsers use, as well as the YAML 1.1 rules.
`yes`/`no`/`on`/`off`, `True`/`FALSE`, `Null`/`NULL`, `~` and
`0x1F`/`017`/`1_000` now arrive as strings, where Groovy 6 read them as
booleans, `null` or integers. A non-empty string is truthy, so `enabled: off`
read with `YamlSlurper.parseText` now tests as true, without any error.
`parseAs` and `parseTextAs` instead reject such a value for a `boolean`
property. `.inf`, `-.inf` and `.nan`, which Groovy 6 rejected, now parse: as
`Double` values through the typed API, and as the strings `'Infinity'`,
`'-Infinity'` and `'NaN'` through the untyped one. Quote values whose type
matters, or load documents that rely on the older typing with SnakeYAML
directly, as the YAML guide shows.

**Jackson 2 databind annotations are ignored.** `@JsonSerialize`,
`@JsonDeserialize`, `@JsonNaming`, `@JsonPOJOBuilder` and the other
annotations in `com.fasterxml.jackson.databind.annotation` have no effect on
YAML, TOML and CSV typed parsing and writing, and nothing reports it. Use the
versions in `tools.jackson.databind.annotation`.

**Typed XML parsing follows the classpath.** `XmlParser.parseAs` and
`parseTextAs` use Jackson 3 whenever it is present, even alongside Jackson 2,
and fall back to Jackson 2 otherwise. An application that gains Jackson 3
transitively, for example through Spring Boot 4, moves typed XML parsing to
Jackson 3, and the databind annotations it honours change with it.

**Jackson 2 and SnakeYAML no longer arrive transitively.** groovy-yaml,
groovy-toml and groovy-csv no longer put
`com.fasterxml.jackson.core:jackson-databind` on the runtime classpath, and
groovy-yaml no longer brings `org.yaml:snakeyaml`. Code that used either
directly must declare it.

**Parser limits follow Jackson 3's defaults.** YAML and TOML input nested
deeper than 500 levels now fails, where Jackson 2 allowed 1000. A TOML string
may be up to 100,000,000 characters, where Jackson 2 allowed 20,000,000. A YAML
document is still limited to 3,145,728 code points, as in Groovy 6.

**What is unchanged.** `@JsonProperty`, `@JsonFormat`, `@JsonCreator` and the
rest of `com.fasterxml.jackson.annotation` work as before, because Jackson 3
still uses that jar. YAML aliases, merge keys, tags, complex keys and
multi-document streams behave as in Groovy 6. `JsonSlurper` and `XmlParser`
keep their own nesting-depth caps of 1000.

### Groovy 7 — `==` on a `Number` that is not `Comparable` compares numerically (GROOVY-12699)

`==` and `!=` between two numbers, where the left one is a `Number` that does
not implement `Comparable`, now compare the values numerically, as `<=>`,
`<` and `>` already did. Before, they called `equals()`, so a `Number`
subclass such as a fraction type was never `==` to an equal `BigDecimal`,
`Double` or `Integer`, even when `<=>` returned `0`. This applies in dynamic
code and under `@TypeChecked` and `@CompileStatic` alike.

**Who is affected.** Besides custom `Number` classes, the JDK's own
non-`Comparable` numbers: `AtomicInteger`, `AtomicLong`, `LongAdder`,
`DoubleAdder`, `LongAccumulator` and `DoubleAccumulator`. For example,
`new AtomicInteger(1) == 1` is now `true`, and two different
`AtomicInteger` instances holding the same value are now `==`. Before, that
second comparison was an identity check. Use `is()` where identity is
meant.

**What is unchanged.** Comparisons where either side is not a `Number`,
which still use `equals()`; numbers that implement `Comparable`, such as
`Integer` and `BigDecimal`; and Java collection methods such as
`List.contains`, which call `equals()` and not Groovy's `==`.

### Groovy 7 — `MatchException` for an exhaustive switch expression on a Java 21+ target

A switch expression the type checker accepted without a `default` (a complete
enum, an unconditional pattern, or labels covering a native sealed hierarchy)
can match nothing at run time only when an enum constant or sealed subtype was
added after the switch was compiled. When the target bytecode is 21 or later
that now throws `java.lang.MatchException`, as `javac` does (JEP 441). The
decision is made from `CompilerConfiguration.getTargetBytecode()`, so it is
fixed per compiled class and does not depend on the JDK that later runs it; a
class compiled for 17 keeps throwing `IncompatibleClassChangeError` (enum) or
`IllegalStateException` on every JDK.

**Who is affected.** Code compiled for a 21+ target that catches
`IncompatibleClassChangeError` or `Error` around such a switch: the enum case
moves from an `Error` to a `RuntimeException`. Neither exception is meant to be
caught; write a `default` arm that throws when a defined exception is wanted.
A `null` selector is not this case: no constant, class or pattern label
matches it, so unless the switch has an explicit `case null` arm it throws
`IllegalStateException` on every target. A switch *statement* still does
nothing when unmatched.

### Groovy 7 — `case Foo() ->` is a zero-component record pattern (GEP-19)

An arrow-form case label made of a capitalised name and empty parentheses,
such as `case None() ->` or `case Opt.None() ->`, is now a record pattern that
matches an instance of the record `None` without components, as in Java
(JEP 440). It used to call a method named `None` and use the result as an
`isCase` label. The same applies where such a call sits inside another
pattern-shaped label, for example `case [Foo(), 2] ->` or `case Some(None()) ->`.

**Who is affected.** Code whose arrow-form case labels call a method with a
capitalised name and no arguments. It now fails to compile with `unable to
resolve class`, at the label, unless a class of that name exists. Quote the
name (`case 'Evens'() ->`) or qualify the call (`case this.Evens() ->`) to keep
calling the method. Colon-form labels (`case Evens():`), lowercase names
(`case evens() ->`) and calls with arguments are unchanged.

### Groovy 7 — `case []`, `case [:]` and `_` in a list or map label are patterns (GEP-19)

A `[...]` or `[k: v]` case label is a pattern when it is empty or contains a
binding form (`var x`, `def x`, `Type x`, `_`, a rest binding); any other list
or map label keeps its `isCase` meaning. Three labels that were valid before
patterns therefore change meaning: `case []` now matches an empty list, array
or `Iterable` where `[].isCase(x)` matched nothing; `case [:]` now matches an
empty map where `[:].isCase(x)` matched nothing; and `_`, which Groovy accepts
as a variable name, is the wildcard pattern inside a list or map label, so
`case [_, 1]` matches any two-element list whose second element is `1` rather
than testing containment in the list `[_, 1]`.

**Who is affected.** Code with such labels, which is rare, as the first two
never matched. To keep the old meaning, bind the literal to a variable first
or spell it differently: `case [].asImmutable()`, or `case [x, 1]` with the
variable renamed.

### Groovy 7 — `instanceof T name` is a `PatternExpression` in the AST (GEP-19)

The parser builds `e instanceof T name` and `e !instanceof T name` as a
`PatternExpression` holding a `TypePattern`, the same node that represents
record patterns and the labels of pattern arms, where Groovy 6 built a
`BinaryExpression` whose right operand was a `DeclarationExpression`. The
Groovy 6 shape is still what the type checker and class generation see:
`PatternLowering`, the first operation of instruction selection, derives it
from the pattern, and a `GroovyCodeVisitor` without an interest in patterns
traverses it from then on through the default `visitPatternExpression`.
Compiled code is unchanged.

**Who is affected.** AST transforms, compilation customizers and tools that
run before instruction selection and look for the Groovy 6 shape, by matching
`BinaryExpression` with `Types.KEYWORD_INSTANCEOF` or
`Types.COMPARE_NOT_INSTANCEOF` and a `DeclarationExpression` right operand.
They now see a `PatternExpression` with `isInstanceOf()` true; the matched
value is `getSource()`, the type and variable are on the `TypePattern`, and
the negated form is a `NotExpression` around it. `e instanceof T` without a
binding is a `BinaryExpression` as before.

### Groovy 7 — a functional argument selects a functional-interface overload it can be called as (GROOVY-11010, GROOVY-12759)

When overloads take functional interfaces whose abstract methods have
different numbers of parameters, such as `Function` and `BinaryOperator`,
`Function` and `Supplier`, or `Consumer` and `Runnable`, method selection now
first drops the candidates whose interface method the functional argument
cannot be called with, as Java does. This applies to method references and
method pointers, and to closures and lambdas with declared parameters,
counting parameters with a default value and a trailing array. It applies
under `@TypeChecked` and `@CompileStatic`, and at run time for dynamic code
and for `@TypeChecked` calls, which are dispatched at run time. When no
candidate fits, all are kept and selection proceeds as before. Run-time
selection for method references and method pointers does this from Groovy
6.0.1 (GROOVY-12758); Groovy 7 adds static type checking, and closures and
lambdas.

Before, such calls failed or picked the wrong overload:

- `red(numbers, Integer::sum)` with `red(Collection, BinaryOperator)` and
  `red(Collection, Function)`, or `from(Main::m)` with `Function` and
  `Supplier` overloads, was ambiguous: `Reference to method is ambiguous`
  under static type checking, `Ambiguous method overloading` at run time.
- A method reference, or a closure or lambda with declared parameters,
  passed where `Runnable` or `Callable` competed with an interface that takes
  arguments, such as `Consumer` or `Function`, matched `Runnable` or
  `Callable`, because `Closure` implements them. Static type checking then
  rejected the call (for a closure, `Wrong number of parameters`); at run
  time the closure was called with `null` arguments, and the method
  reference failed when run, as no method of its name takes no arguments.

**Who is affected (runtime behaviour).** Dynamic code that passes a closure
with declared parameters, such as `{ String s -> ... }`, to overloads where
`Runnable` or `Callable` competes with an interface of matching arity. Such a call now selects that interface, so the
closure receives the arguments it declares instead of `null`. Code that
relied on `Runnable` or `Callable` being chosen, and on the closure being
called without arguments, should pass a closure without parameters
(`{ -> ... }`).

**What is unchanged.** A closure with an implicit parameter (`{ ... }`,
`{ it }`) and `{ -> ... }` still prefer `Runnable` and `Callable`, and
interfaces of the same arity, such as `Runnable` and `Callable`, are chosen
between as before. Parameters of type `Closure`, `Object` and interfaces
without a single abstract method, such as `Serializable`, are not affected.
A method reference whose methods accept both arities, such as
`Integer::valueOf`, is still ambiguous. The wrappers `memoize()`, `curry()`
and `trampoline()` return, and a dehydrated method closure, are selected for
as before.

**Who is not affected (binary).** There is no change to public API apart
from a static helper on the internal `MetaClassHelper`.

### Groovy 7 — a closure skips a null owner or delegate for a property (GROOVY-12671)

A closure's owner or delegate can be null: after `dehydrate()`, after
`rehydrate` with a null argument, or after `delegate = null`. A property access
in the closure body that resolves through the owner and delegate now skips a
null one instead of reading or writing on null, as a method call does from
Groovy 6.0.1 (GROOVY-12667, see the entry above):

- **The other candidate answers.** With `OWNER_FIRST` (the default) and a null
  owner, the delegate's property is used. Earlier versions threw
  `NullPointerException`. With `DELEGATE_FIRST` and a null delegate, the
  owner's property was already used.
- **With no candidate, the error is a missing property.** When both are null,
  or the one target of `OWNER_ONLY` or `DELEGATE_ONLY` is null, a property
  read or write throws `MissingPropertyException`, where
  `NullPointerException` was thrown before. So does a write that the first
  candidate does not take when the second is null.

In a `@CompileStatic` closure, a property write resolves this way too. A
property read compiled against the owner is unchanged.

The resulting rules are stated in full under "Resolution rules" in
[`src/spec/doc/core-closures.adoc`](src/spec/doc/core-closures.adoc).

**Who is affected.** Code that catches `NullPointerException` around a
property access in a closure with a null owner or delegate. Nothing that ran
without an exception before behaves differently. No public API changes, so
`japicmp` does not report this.

### Groovy 7 — a closure's metaclass does not expose the private fields of `Closure` (GROOVY-12675, GROOVY-12680)

A closure class does not inherit the private fields of `groovy.lang.Closure`
(`bcw`, `thisType`, `mopPerturbed`, `serialVersionUID` and others), and its
metaclass now treats them as any metaclass treats a superclass's private
fields: `getProperties()` does not list them, `getMetaProperty` and
`hasProperty` return null, and `getProperty` and `setProperty` throw
`MissingPropertyException`. Earlier versions returned `bcw` and
`serialVersionUID` through the metaclass. From Groovy 5 they also found and
overwrote `thisType`, through the metaclass or through `closure.thisType = ...`
under `TO_SELF`, and overwriting it broke the closure's access to private
fields of its enclosing class. A `MissingPropertyException` for a closure no
longer suggests these fields ("Possible solutions: bcw").

**Who is affected.** Only code reading these fields through a closure's
metaclass. `closure.properties` and the public properties of `Closure`
(`owner`, `delegate`, `resolveStrategy`, `maximumNumberOfParameters` and the
rest) are unchanged.

### Groovy 7 — closure parameters for an extension method's functional parameter are typed from the receiver (GROOVY-12746)

Under `@TypeChecked` and `@CompileStatic`, a closure or lambda passed for a
functional-interface parameter of an extension method now gets parameter
types from the receiver's type arguments. Before, a type parameter that
only the extension method's `self` parameter mentions fell back to its
bound, usually `Object`, so `List<Integer>` with
`numbers.collectParallel { it * 2 }` or `numbers.sumParallel { a, b -> a + b }`
did not compile. Type arguments inferred from the call's arguments still
take precedence.

**Who is affected.** Code that compiled before, with parameters typed as
the bound, now has them typed as the receiver's element type, as for any
other functional-interface parameter such as `Stream.map`'s:

- Under static compilation, a call in the closure that passes such a
  parameter to an overloaded method can select a more specific overload,
  such as `helper(Integer)` instead of `helper(Object)`, as dynamic Groovy
  already does.
- A body that is invalid for the element type is reported with that type.
  For example, `it instanceof String` for an `Integer` element no longer
  compiles, as with DGM `collect`.
- A closure that assigns a value of another type to its own parameter, such
  as `numbers.collectParallel { n -> n = n.toString(); n }`, no longer
  compiles, as for any other functional-interface parameter (GROOVY-12747).
  Assign to a new local variable instead.

### Groovy 7 — assigning a value of another type to a closure parameter typed from a functional interface is an error (GROOVY-12747)

Under `@TypeChecked` and `@CompileStatic`, an untyped closure or lambda
parameter whose type comes from a functional interface is declared with
that type. Assigning it a value of another type used to compile, and the
value was then converted to the parameter's type when the closure ran:
`List<Integer> numbers; numbers.stream().map { n -> n = n.toString(); n }`
gave `[49, 50]` for `[1, 2]` (the character codes of `"1"` and `"2"`),
`n = n + 0.5` dropped the fraction, and `n = 'ab'` failed with a
`GroovyCastException`. Such an assignment is now reported:
`Cannot assign value of type java.lang.String to variable of type java.lang.Integer`.

**Who is affected.** Code that compiled before and assigns a value of
another type to such a parameter. It gave a converted value or failed at
run time; assign to a new local variable instead. Assigning a value of the
parameter's type, such as `n = n * 2` or `n += 1`, is unchanged. So are an
implicit `it`, which is declared `Object`, and parameters typed through
`@ClosureParams`, as for DGM `collect`, which keep the assigned value as
dynamic Groovy does.

### Groovy 7 — `sumParallel`, `minParallel` and `maxParallel` accept a transform or key (GROOVY-12722)

`sumParallel(Function)`, `minParallel(Function)` and `maxParallel(Function)`
sit beside the existing `BinaryOperator` and `Comparator` overloads. A closure
or lambda literal, or a method reference, selects between them by the number
of parameters it takes (GROOVY-11010, GROOVY-10714), so existing calls keep
their meaning.

**Who is affected (source).** Under `@TypeChecked` and `@CompileStatic`, a
variable or expression of type `Closure` passed to these methods is now
ambiguous, because its parameter count is not known statically:
`Closure<Integer> c = { Integer a, Integer b -> a + b }; numbers.sumParallel(c)`
compiled before and now reports `Reference to method is ambiguous`. Declare the
variable with the functional interface instead (`BinaryOperator<Integer>`,
`Comparator<T>`, `Function<T, R>`), cast with `as`, or pass the closure literal
directly. Dynamic code, where the closure's parameter count is known at run
time, is unaffected.

**Who is not affected (binary).** Existing call sites are compiled against
the unchanged existing overloads.

### Groovy 7 — a default-package class on the class path is resolved before the default imports (GROOVY-12555)

In a compilation unit of the default package, a simple type name that the
unit neither declares nor imports, and that no other unit of the same
`CompilationUnit` declares, is now looked up as a class of the default
package on the class path, and on the source path of the
`GroovyClassLoader`, *before* the default imports (`java.lang`, `java.util`,
`java.io`, `java.net`, `java.time`, `groovy.lang`, `groovy.util`,
`BigInteger`, `BigDecimal`). That lookup used to come after the default
imports, so a default-package `Factory` was found while it was compiled
together with its callers but lost to `groovy.util.Factory` once it had been
compiled ahead of them, as in an incremental build, and the same source
compiled to a different class. A class of a named package was already
resolved before the default imports for a unit of the same package, and the
Java Language Specification (§6.4.1) has a type of the same package, the
unnamed package included, shadow a type imported on demand. An explicit star
import still takes precedence over a class of the default package, as
elsewhere in Groovy. A compiled nested class named through its outer class
is looked up the same way: with a compiled default-package `Proxy` that has
a nested `Type`, `Proxy.Type` is that class and not `java.net.Proxy.Type`.

**Who is affected.** Default-package code whose class path has a class, or a
`.groovy` source, of the default package with the simple name of a
default-import type. The `groovy` command uses the current directory as the
class path when none is given, so in a directory of scripts a sibling
`Node.groovy`, `Timer.groovy` or `Math.groovy` now stands for that name in
the other scripts, where `groovy.util.Node`, `java.util.Timer` and
`java.lang.Math` did, and a sibling source that does not compile is now a
compile error for a script that uses its name. The roots of a
`GroovyScriptEngine`, and so of groovlets, are source paths of the same kind.
A script there records a dependency only on the class a name resolved to, so
a script that resolved `Date` to `java.util.Date` is not recompiled when a
`Date.groovy` appears in a root: the same engine then gives `java.util.Date`
to that script and the root's `Date` to a script compiled after it, and a
script that resolved `Date` to the root's `Date.groovy` keeps it after the
file is deleted, until the engine is recreated. The lookup also takes in the
classes that the class loader has already compiled or loaded, so in a
`GroovyShell` or `GroovyScriptEngine` that is reused, and in groovysh or the
console, a name resolves to a default-package class compiled earlier by the
same loader, such as a `Timer` from a `Timer.groovy` parsed before, and to
the default import while there is none.

**Remedies.** Rename the file, give it a package, or use the fully qualified
name of the default-import type. Set the optimization option
`defaultPackageSources` to `false` to look up only the `.class` files of the
default package before the default imports: a compiled class still shadows a
default import, while a source of the default package, and a class compiled
earlier by the class loader, are found only after the default imports, as
before. Set the optimization option `defaultPackageFirst` to `false` to
restore the previous order altogether; `defaultPackageSources` is then
ignored. Either can be set through its system property,
`groovy.resolve.default.package.sources=false` or
`groovy.resolve.default.package.first=false`, a configuration script, or, in
Gradle, `groovyOptions.optimizationOptions['defaultPackageSources'] = false`
or `groovyOptions.optimizationOptions['defaultPackageFirst'] = false`. Only
the last of these reaches the compiler from Gradle, which replaces the
compiler's optimization options with that map after the configuration script
has run. An incremental Gradle build gives the same result whatever
`defaultPackageSources` is, since the classes it does not recompile are
`.class` files in its output directory and its source directories are not on
the compile class path.

**Cost.** A compilation unit of the default package also pays one class-path
lookup per default-import type it names, as a unit of a named package always
has; a `GroovyShell`, `Eval` or `GroovyScriptEngine` that compiles many small
scripts pays it for each of them, since the lookup cache is per compilation
unit. A `GroovyScriptEngine` also looks each such name up in its roots, two
more root lookups per name in each compile, unless `defaultPackageSources` is
`false`.

Code in a named package, and default-package code whose class path has no
such class or source, resolve as before. The two options are the only API
change.

### Groovy 7 — type checking compares a wildcard argument with every bound of a type parameter (GROOVY-12754)

Under `@TypeChecked` and `@CompileStatic`, an argument with an upper-bounded
wildcard, such as a `List<? extends B>` passed to `<T extends U> m(List<T>)`,
was compared with the bound of `T` the wrong way round, and only with the
erasure of its first bound. The wildcard is now checked as Java checks it: it
stands for an unknown subtype of `B`, which must be within every bound of `T`,
type arguments included.

- **Calls that were rejected compile.** A `List<? extends Integer>` for
  `<T extends Number>`, a `Class<? extends String>` for
  `<T extends CharSequence>` and `Collections.sort(list)` for a
  `List<? extends Integer>` were compile errors ("Cannot call <T extends …>")
  and now compile.
- **Calls that were accepted are rejected.** A wildcard whose bound is not
  within a bound of the type parameter was accepted, and the call could then
  return a wrong value: statically compiled,
  `<T extends Integer> T narrow(List<T>)` returned `1` for a
  `List<? extends Number>` holding `1.5`. Such a call is now a compile error,
  `Cannot call <T extends …> … with arguments […]`. Other examples are a
  `List<? extends Serializable>` for `<T extends Number>`, a
  `Class<? extends List<String>>` for `<T extends List<Integer>>`,
  `Enum.valueOf(type, name)` for a `Class<? extends Enum<?>>` and
  `Collections.sort(list)` for a `List<? extends Comparable<?>>`.
- **A wildcard with no upper bound is checked too.** `?` and `? super B`
  stand for a type with the upper bound that the generic type declares for
  that type argument: `Object` for a `List<?>` or a `Class<?>`, and `Number`
  for a `NumBox<?>` declared as `NumBox<N extends Number>`. So a `List<?>` and
  a `List<? super Integer>` are rejected for `<T extends Number>`, while an
  `EnumSet<?>` is accepted for `<T extends Enum<T>>`. `Enum.valueOf(type, name)`
  and `method.getAnnotation(type)` for a `Class<?>`, and
  `Collections.sort(list)` for a `List<?>`, were accepted and are now compile
  errors, as in Java.

**Who is affected.** Statically checked code that makes such a call. Declare
the wildcard with a bound that is within the bounds of the type parameter,
such as a `Class<? extends Annotation>` for `getAnnotation`, or use a raw
type, which is still an unchecked conversion: a `Class<? extends Enum>` or
`(Class) type` for `Enum.valueOf`, and `(List) list` for `Collections.sort`.

**What is unchanged.** Dynamic code, an argument without a wildcard and a
method whose type parameter has no bound.

### Groovy 7 — type checking types `getClass()` by its receiver (GROOVY-12755)

Under `@TypeChecked` and `@CompileStatic`, `e.getClass()` and `e.class` were
typed `Class<? extends Object>`, and that type was accepted wherever a
`Class<? extends X>` was expected, whatever `X` was. They are now typed
`Class<? extends |T|>`, where `|T|` is the erasure of the static type of `e`,
as in Java: `Class<? extends String>` for a `String` and
`Class<? extends List>` for a `List<String>`. The type is checked like any
other type.

- **A correct bound compiles, as before.** `Class<? extends String> c = s.getClass()`,
  and `datastore.getClass()` passed where a `Class<? extends Datastore>` is
  expected, compile.
- **A wrong bound is a compile error.** For a `String s`,
  `Class<? extends Integer> c = s.getClass()`, and `s.getClass()` passed to a
  parameter typed `Class<? extends Number>`, compiled. The variable or
  parameter then held a class outside its bound. These are now compile errors,
  as in Java.
- **`@CompileStatic` may select a more specific overload.** A value made from
  `x.getClass()`, such as `x.getClass().cast(o)`,
  `x.getClass().getDeclaredConstructor().newInstance()` or the result of
  `<T> T create(Class<T>)`, now has the type of `x`. With overloads
  `f(Object)`, `f(CharSequence)` and `f(StringBuilder)` and a
  `StringBuilder x`, `f(x.getClass().cast(o))` calls `f(StringBuilder)` where
  it called `f(Object)`. That is the overload Java selects, and the one dynamic
  Groovy and `@TypeChecked` code select at run time. There is no compile error.
  As before, the overload is selected before its generics are checked, and a
  selected overload that fails that check is a compile error rather than a
  reason to select a less specific one: with `f(Object)` and
  `f(Class<String>)`, `f(new StringBuilder().getClass())` is rejected, as it
  was before, where Java selects `f(Object)`.
  Cast the argument to `Object` to select that overload.
- **A generic call takes type arguments from its target.** In an assignment, a
  return or an argument, a call of a generic method is accepted when the target
  type determines its type arguments, as in Java. The target of an argument is
  the parameter of the method that is called, once that method is selected:
  `List<Class<?>> l = Collections.singletonList(x.getClass())`,
  `takes(Collections.singletonList(x.getClass()))` for a
  `takes(List<Class<?>>)`, `Arrays.asList(a.getClass(), b.getClass())`,
  `Optional.of(x.getClass())` and `list.collect { it.getClass() }` compile, as
  does a generic call nested in another, such as
  `same(Collections.singletonList(x.getClass()))`. These compiled before
  because the type of `getClass()` was not checked; they now compile because
  the target gives the type argument. So do
  `List<Number> l = Collections.singletonList(1)` and
  `count(Collections.singletonList(1))` for a `count(List<Number>)`, which were
  compile errors. The type argument is not a conversion: an assigned value may
  be converted, as an `int` to a `Long` or a `GString` to a `String`, but the
  elements of a list are stored as they are. So
  `List<Long> l = Collections.singletonList(1)` and a `List<String>` made by
  `Collections.singletonList("a$x")` are still compile errors, as they were
  before and are in Java, since the list would hold an `Integer` or a
  `GString`. A spread expression makes a new list, so it takes its element
  type from the target in the same way: `list*.getClass()` and `list*.class`
  compile for a `List<Class<?>>`, and `people*.name` for a `List<CharSequence>`.
  A variable declared with such a target has the declared type. A value that
  already has a type is not converted: assigning or passing a variable of type
  `List<Class<? extends String>>` as a `List<Class<?>>` is a compile error, as
  it is in Java.
- **A method reference passed to such a call is checked and compiled for the
  functional interface type that the target gives.** For
  `<T, R> Function<T, R> toFunction(Function<T, R> fn)` and the like:
  - **Calls that were rejected compile.** `Type::instanceMethod` on a generic
    class takes the type arguments of the class from the first parameter of
    the functional interface, as in Java, so
    `Function<Optional<String>, String> f = toFunction(Optional::get)` and
    `BiFunction<List<String>, Integer, String> f = toBiFunction(List::get)`
    compile. So do a reference to a supertype's method, an overloaded or
    variadic method, and an extension method:
    `Function<String, Integer> f = toFunction(CharSequence::length)`,
    `Function<char[], String> f = toFunction(String::valueOf)`,
    `Consumer<String> c = toConsumer(System.out::print)` and
    `Function<String, Integer> f = toFunction(String::size)`. The overload is
    the one a direct assignment of the reference selects.
  - **A variadic method runs under `@CompileStatic`.**
    `Function<String, List<String>> f = toFunction(Arrays::asList)` compiled,
    and statically compiled it failed with a `ClassCastException` when called.
    It now returns a list of the one value, as `@TypeChecked` and dynamic code
    do.
  - **Calls that were accepted are rejected.** A method whose type parameter
    has a bound that the target does not keep compiled, and failed when
    called: `Consumer<String> c = toConsumer(C::takeNumber)` for
    `<T extends Number> void takeNumber(T t)`. The bound is also checked
    where only the result of the method names the type parameter,
    `Supplier<List<String>> s = toSupplier(C::numbers)` for
    `<T extends Number> List<T> numbers()`, which let a `String` be added to
    a list of numbers, and where the reference goes through a second generic
    call, `toConsumer(sameConsumer(C::takeNumber))`. So did a method whose other
    parameters or return type do not fit the type arguments of its receiver:
    `BiFunction<List<String>, Integer, Boolean> f = toBiFunction(List::add)`
    added an `Integer` to a `List<String>`, and
    `Function<List<Integer>, String> f = toFunction(List::first)` returned an
    `Integer` as a `String`. These are now compile errors,
    `Incompatible generic argument types`, as their Java equivalents are
    (`List::first` is a Groovy method).
    Declare the functional interface with the types that the method accepts
    and returns.
- **A constructor call with a diamond infers the type arguments of the class
  from its arguments.** With no target, `new Holder<>(list)` takes the type
  arguments of `Holder` from the constructor arguments, as a generic method
  call does, and they must be within the bounds of the class. For
  `class Holder<T extends Number>` with a `Holder(List<T>)` constructor, a
  `List<? extends Integer>` is accepted, as before, and gives a
  `Holder<? extends Integer>`. A `List<String>` or a `List<? extends String>`
  compiled and failed when run with a `GroovyCastException`, and a `List<?>`
  compiled with no check of its elements. These are now compile errors,
  `Cannot call Holder#<init>(...) with arguments [...]`, as in Java. Where the
  elements are known to fit, declare the parameter as a
  `List<? extends Number>`, cast the argument to one, or pass it as a raw
  `List`; each of these also compiles with earlier versions.
- **A variable declared with `def` or `var` has the type of its value.** So
  `def classes = Collections.singletonList(x.getClass())` is a
  `List<Class<? extends String>>`, and passing it to a `takes(List<Class<?>>)`,
  which compiled before, is now a compile
  error, as it is in Java with `var`. The error suggests the declared type:
  `Cannot call …#takes(java.util.List<java.lang.Class<?>>) with arguments
  [java.util.List<java.lang.Class<? extends java.lang.String>>]. Declare
  variable classes as java.util.List<java.lang.Class<?>> to pass it`. Declare
  the variable with that type, or pass the call directly.
- **A raw bound is not within a parameterized one, and a type is not within a
  type variable.** For an assignment, a return and a declared parameter,
  `? extends A` is no longer accepted for `? extends B` when `B` is
  parameterized and `A` is raw, or when `B` is a type variable and `A` is not
  one. These were accepted and are now compile errors, as in Java:

  ```groovy
  Class<? extends List<String>> a(Class<? extends List> c) { c }
  <T extends Number> Class<? extends T> b(Class<? extends Number> c) { c }
  <T> Class<? extends T> c(T t) { t.getClass() }
  ```

  This also holds within a type argument, and for a spread expression or a
  closure typed by its target. A `List<Class<? extends List>>`, from a
  variable, `raw*.getClass()` for a `List<List> raw`, or
  `classes.collect { it }`, is not a `List<Class<? extends List<String>>>`.
  `items*.getClass()` for a `List<Object> items` is not a
  `List<Class<? extends T>>`. These compiled and are now compile errors, as in
  Java.

- **The class of a value of a type variable is not the class of the type
  variable.** For a `T t`, `t.getClass()` is a `Class<? extends Object>`.
  `<T> Class<T> typeOf(T t) { t.getClass() }` compiled and is now a compile
  error, as in Java. So is passing `t.getClass()` to a parameter typed by a
  type variable in scope, such as `accept(Class<? extends T>)` or
  `Map<Class<? extends T>, T>.put` within `Box<T>`, and so is
  `List<Class<? extends T>> l = items*.getClass()` for a `List<T> items`.
  `T` may be a parameterized type, such as `List<String>`, and a class does
  not have type arguments. With a `Class<T> type`,
  `items*.getClass()*.asSubclass(type)` compiles and checks each class when
  run; otherwise add the cast that Java requires.
- **The class of a variable follows `instanceof`.** After
  `o instanceof String` for an `Object o`, `o.getClass()` and `o.class` are a
  `Class<? extends String>`, like every other member of `o` there; this is
  Groovy's `instanceof` inference. Java keeps `o` an `Object`, so it rejects
  `Class<? extends String> c = o.getClass()`, and passing `o.getClass()` as a
  `Class<? extends CharSequence>`; both compile in Groovy, as before. The
  difference only accepts more code: the Java forms,
  `o instanceof String s` followed by `s.getClass()`, and
  `((String) o).getClass()`, also compile.

- **The simple name of an inner class keeps the type arguments of the
  enclosing class.** Within `Outer<T>`, `Inner` now means `Outer<T>.Inner`
  and not the raw type `Outer.Inner`, with or without static compilation. The
  `Signature` attribute of a member typed with such a name names the enclosing
  type, so reflection reports the generic type `Outer<T>$Inner` where it
  reported the class `Outer$Inner`. As a type argument it is not the raw type
  either: `List<Inner> a = raw` and `List<? extends Inner> b = raw` for a
  `List<Outer.Inner> raw` compiled and are now compile errors, as in Java.

**Who is affected.** Statically checked code that gives the class of a value
a bound it does not have, code that relied on one of the conversions above,
and `@CompileStatic` code whose overloads differ for the class of a receiver.
Correct the bound, add the cast that Java requires, such as
`(Class<? extends T>) t.getClass()`, or declare the raw bound, such as
`Class<? extends List>`. The class of a value is also now checked against the
bound of a type parameter (see GROOVY-12754 above), so
`Enum.valueOf(e.getClass(), name)` for an `E e` of an enum type `E` is a
compile error; use
`e.getDeclaringClass()`. Code that reads the generic signature of an inner
class member by reflection sees the enclosing type.

**What is unchanged.** Dynamic code, apart from the generic signatures. A raw
type that is not the bound of a wildcard is still an unchecked conversion, as
in `List<String> x = rawList`. A member of a raw type whose type parameter has
a raw bound has the erasure of its type, as in Java: for a raw `Holder`
declared as `Holder<T extends List>`, a field `Class<? extends T> type` is a
`Class`, so `Class<? extends List<String>> c = holder.type` is an unchecked
conversion.

### Groovy 7 — an `instanceof` check narrows a variable only where it guards the code (GROOVY-12562)

Under `@TypeChecked` and `@CompileStatic`, an `instanceof` check used to narrow
the variable it checks for the rest of the expression or condition, wherever
the check was. It now narrows only where its outcome decides what is evaluated
next: in the condition of an `if`, `while`, `for`, `assert` or conditional
expression, in an operand of `&&`, `||`, `|` or `!`, and when the check is compared
with a boolean literal, assigned in a condition or cast to `boolean`. The
condition of a loop narrows inside the loop only.

```groovy
@CompileStatic
def f(Object x) { '' + (x instanceof String) + x.length() }
// before: compiled; f(new StringBuilder('ab')) threw ClassCastException
// now:    Cannot find matching method java.lang.Object#length()

@CompileStatic
def g(Object x) { x instanceof String ? 'true' + x.length() : 'false' }
// before and now: compiles, the check guards the call
```

**Who is affected.** Code that compiled before and now fails with an error
such as "Cannot find matching method", because it relied on a narrowing that
nothing ensured:
- a check that is an operand of any other operator, a method argument or a
  list element: `'' + (x instanceof String) + x.length()`,
  `foo(x instanceof String, x.length())`, `[x instanceof String, x.length()]`,
  `(x instanceof String) == x.isEmpty()`, and the right operand of `&` in
  `x instanceof String & x.length() > 0`;
- a variable used after a loop whose condition checked it:
  `while (x instanceof String) { break }; x.length()`, and the same for a
  classic `for` and a `do`/`while` loop;
- the branch of a check compared with `false`, or with `true` using `!=`:
  `if ((x instanceof String) == false) x.length()` used to narrow `x` to
  `String` in the branch where it is not one.

Under `@CompileStatic` such code threw `ClassCastException` whenever the check
was false. Under `@TypeChecked` the call is dispatched dynamically, so it may
have run correctly. Move the check into a condition that guards the use, or
cast the variable.

**More programs compile.** The `else` branch of
`if ((x instanceof String) == false)` and the code after
`if ((x instanceof String) == false) return` are now narrowed to `String`, as
for `!(x instanceof String)`.

**What is unchanged.** A check that is a statement of its own,
`x instanceof String; x.length()`, or that is only stored,
`boolean b = x instanceof String; x.length()`, did not narrow the following
statements before and does not now. `assert x instanceof String` and
`if (!(x instanceof String)) return` still narrow the code after them.
`case String:` in a `switch`, `catch` parameters, and dynamic code are
unaffected. An `instanceof` pattern such as `x instanceof String s` narrows
`x` in the same guard positions as a plain `instanceof` check; the scoping of
the pattern variable `s` is unchanged.

### Groovy 7 — `super` reaches a static member the class hides (GROOVY-12519)

When a class declared a static method or property with the same signature as
one of its superclass, `super.sm()` within the class threw
`MissingMethodException` in dynamic Groovy and under `@TypeChecked`, or called
another overload of the superclass with `null`, and `super.sp` threw
`MissingPropertyException`. They now reach the static member of the nearest
superclass that declares one, as javac and `@CompileStatic` do. Under
`@CompileStatic`, `super.sm()` in a closure or lambda failed with
`IncompatibleClassChangeError` for any static method of the superclass, hidden
or not, and an extension method called through `super` there, such as
`super.first()` in a subclass of `ArrayList`, failed with `NoSuchMethodError`.
Both now call the method.

**Who is affected.** Code that relied on the old failure, or on the other
overload being called. A private static method of the superclass stays out of
reach. Calls through `this` or the class name and instance methods behave as
before, and so does `super` with a static member the class does not hide,
apart from the `@CompileStatic` closure and lambda case above. There are no
API changes.

### Groovy 7 — `N.super.name` and `Outer.super.name` reach the member of the superclass (GROOVY-12514, GROOVY-12517)

Within a nested class `N`, `N.super` is now `super`. Within a class nested in
`Outer`, `Outer.super` is the enclosing instance of `Outer` seen as an instance
of `Outer`'s superclass, as in Java. In dynamic Groovy, a property read or
write through either — `N.super.p`, `Outer.super.p = v`, `Outer.super.p++` —
and a field access — `N.super.@f`, `Outer.super.@f = v` — used to reach the
instance's own member: the one `N` or `Outer` declares, or, for an enclosing
instance of a subclass of `Outer`, the subclass's. It now reaches the
superclass's property, accessor or field, as `super.p` and `super.@f` do.
`Outer.super.m()` already called the superclass's method, except for an
enclosing instance of a subclass of `Outer`, where it called the method that
`super.m()` in the subclass would call (`Outer`'s, if `Outer` overrides `m`);
it now calls the method of `Outer`'s superclass. Under `@TypeChecked` and
`@CompileStatic`, these property and field forms failed to compile ("No such
property: super") and now compile with the same meaning.

**Who is affected.** Dynamic code that reads or writes a property or field
through `N.super` or `Outer.super` where `N` or `Outer`, or the class of the
enclosing instance, declares or overrides that member. The access now reaches
the superclass's member; use `this.p`, `N.this.p` or `Outer.this.p` where the
class's own member is intended. Classes compiled by an earlier release keep
the old behaviour until they are recompiled.

**What is unchanged.** Unqualified `super`, `Outer.this`, `Iface.super.m()`
for interface default methods, method calls through `N.super` and through
`Outer.super` on an instance of `Outer` itself, and members that no class
below the superclass declares. `ExpressionUtils.isOuterSuperExpression` is
new; there are no other API changes.

### Groovy 7 — a serializable lambda inside a closure can use the closure's delegate (GROOVY-12830)

A serializable lambda that resolves a name through the closure around it now
captures that closure too, as a non-serializable lambda does from Groovy 6.0.1
(GROOVY-12827). Before, the name threw
`ClassCastException` when evaluated.

**Who is affected (runtime behaviour).** Only a serializable lambda containing
such a name, which could not previously evaluate it:

- Serializing the lambda also serializes the closure, including its delegate
  and the values it holds. If one of them is not serializable, writing the
  lambda throws `NotSerializableException`, even when the name is on a branch
  that never runs. Dynamic Groovy behaves the same way.
- The lambda is a `java.lang.reflect.Proxy` over a `ConvertedClosure`, unless
  its functional interface is or implements a trait. An undeclared checked
  exception thrown from a proxied lambda reaches the caller as
  `UndeclaredThrowableException`, as in dynamic Groovy.

To keep such a lambda serializable with a non-serializable delegate, remove
the reference to the delegate from the lambda, or pass the value in through a
serializable local variable.

**Who is not affected.** Non-serializable lambdas, and serializable lambdas
that do not resolve a name through the closure around them, keep their
serialized form and behaviour.

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
