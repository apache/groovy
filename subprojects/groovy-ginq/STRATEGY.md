# GINQ static compilation and the fluent query API

This note is the implementation strategy for GROOVY-12724. It states which
component owns each decision, which alternatives were rejected, and the
behaviour a caller can rely on.

## What is being compiled

There are two front ends and one runtime.

* `GQ`, `GQL`, and `@GQ` are a macro. At conversion the macro lowers the
  SQL-like syntax to `Queryable` calls. Clause bodies (`where`, `select`,
  joins, ordering, grouping, aggregates) use Groovy's dynamic object protocol,
  because a row may be a bean, a map, or a record. That protocol is the
  behaviour dynamic `GQ` already has.
* `Ginq.from` is ordinary generic methods (`Query` / `GinqQuery`). There is
  no macro and no extra type checker. `@CompileStatic` sees the methods and
  infers closure parameters from the SAM types, the same way it infers any
  other Groovy lambda.
* Both front ends execute on the in-memory `Queryable` runtime. A SQL provider
  still consumes the macro's expression tree; the fluent API is not a second
  translator.

## When static compilation applies

Static compilation is `StaticCompileTransformation`, which runs at instruction
selection. GINQ does not choose a different phase and does not install a
visitor of its own.

| Enclosing scope | What the compiler does |
| --- | --- |
| No checking annotation | The macro's lowered tree is left as it is. The whole method is dynamic. The pipeline is built when the expression is evaluated. |
| `@CompileDynamic`, or `@CompileStatic` / `@TypeChecked` with `TypeCheckingMode.SKIP` | Same as no checking annotation. |
| `@TypeChecked` | The enclosing method is type-checked. The lowered query closure is dynamic (below). |
| `@CompileStatic` | The enclosing method is type-checked and statically compiled. The lowered query closure is dynamic (below). |

`@TypeChecked` does not static-compile. The same extension is registered for
both annotations because both need the row rules.

The extension is `GinqRowChecks`. The macro, or the `@GQ` transform, appends
its name to the `extensions` member of the annotation that the checker will
actually read: the class annotation, or an outer class annotation when the
query is in an inner class, or the method annotation when only the method is
checked. That member is the existing registration point. Nothing is appended
for a dynamic scope.

## The query closure is dynamic; the method is not

The lowered clause bodies are not Java. An operator on a row, a property of a
map, and an aggregate call do not have a static target the checker can prove.
Feeding that tree to the static call-site writer fails, because that writer
only emits operators it can prove (`StaticTypesCallSiteWriter` rejects the
rest).

The static compiler already has the switch for this. A closure whose expression
carries `StaticTypesMarker.DYNAMIC_RESOLUTION` is emitted by
`StaticTypesClosureWriter` with `STATIC_COMPILE_NODE` false, so its `doCall`
uses the dynamic writer. The enclosing method keeps the static writer. Nested
closures created from that dynamic `doCall` are dynamic as well, which is why
the clause lambdas do not need a checker of their own.

`GinqRowChecks.beforeMethodCall` leaves the calls directly inside that closure
dynamic and does not walk into the clause bodies. Those bodies are compiled by
the dynamic writer; checking them as Java would reject `from r in rows where
r.a` on a list of maps. Outside the closure, an unresolved row property or
`toList` / `getAt` is still resolved by the same extension, and a resolved
member is still checked. A missing method on a `String`, or on an `Object`
that is not a query closure, is still an error.

This replaces the previous synthetic `ginqQuery$` method, the
`CompilationUnit.addNewPhaseOperation` that appended it, and the hand-written
local-variable walk. Those three existed only because the query had been moved
into another method: the phase hook papered over adding a method while the
macro visitor walked the method list, and the local walk reimplemented the
capture the closure writer already does. `ClassNode.visitMethods` can accept a
method added during a visit, but a second method is the wrong place for this
work. The closure is the compiler's capture and the compiler's dynamic-method
switch.

## Why execution is lazy, and which lazy

Two different deferrals, deliberately not the same mechanism.

**Element pull is always deferred.** `where` and `select` on `Queryable` build
a pipeline and do not iterate. `toList`, `count`, and the other terminals
pull. Dynamic `GQ` already works this way. Nothing in the static path changes
it.

**Fluent operators record until a terminal.** `GinqQuery` stores the predicate,
the selector, and the orderings, and calls `Queryable` from `materialize`,
which only a terminal invokes. This is the LINQ-to-Objects shape (a query
object that runs when it is enumerated), not the LINQ-to-SQL shape (an
expression tree a provider rewrites). Groovy already has the macro for the
expression-tree case. Copying `IQueryable` would duplicate the macro and force
every in-memory operator through a translator. The fluent API therefore stays
a recording object. It does not use `LazyQueryable`, and it does not depend on
static compilation: the same object is lazy in a dynamic method.

**A static `GQ` also defers building the pipeline.** The lowered form is an
immediately-invoked closure: calling it evaluates `from` and constructs the
`Queryable` chain. Under `@CompileStatic` or `@TypeChecked` that call is moved
behind `LazyQueryable.defer`. The closure value is created when the `GQ`
expression runs, and `call()` runs on the first read. Creating the closure at
the expression is what captures locals then; delaying `call()` is what keeps
`Iterable.iterator` (and any other work inside the lowered tree) off the
expression. `GQL` and `@GQ(List)` call `toList` on the deferred query, so they
still materialize at the expression. Dynamic `GQ` is unchanged: the macro does
not wrap it, and the pipeline is built when the expression is evaluated.
Changing that would change every existing dynamic query.

`LazyQueryable` is only that thunk. It is not a type checker, not a capture
analysis, and not the fluent implementation. Joining, grouping, and the
aggregates are forwarded to the pipeline built on first use, and that pipeline
is cached, matching `QueryableCollection.toList`.

## Locals

The deferred closure is a normal Groovy closure in the enclosing method, and
the closure writer captures it. A local the query assigns is a closure-shared
variable, so the write is visible to the caller. That is the compiler's rule.

A local the query only reads is different, and this is the one place the macro
records names. Dynamic `GQ` creates the clause lambdas when the expression
runs, so a later assignment is not what those lambdas read. A static `GQ`
creates the closure at the expression and calls it on the first read. If the
caller assigns the local again, the closure writer treats it as shared and the
deferred call would see the new value. The macro therefore copies those
read-only names into an array when the expression runs, and the deferred
closure reads the array. It does not rewrite the caller's declarations, box
the variables, or emit stores. Fields are not copied; they are read when the
closure runs.

## `asType`

`Queryable.asType` special-cases `Queryable` (returns this), and `List`,
`Collection`, and `Iterable` (returns `toList()`). Every other target,
including null, is `DefaultGroovyMethods.asType`. That method throws
`GroovyCastException` when the value is not compatible with the target, and
applies the usual string and collection coercions when it is. `LazyQueryable`
returns this for `LazyQueryable` and `Queryable` without building the pipeline,
and otherwise builds it and uses the built query's `asType`. Returning `this`
for an arbitrary target is not a coercion.

## What stays out of the static compiler

GINQ does not subclass `StaticTypeCheckingVisitor` or
`StaticCompilationVisitor`, does not add a compile phase, and does not emit
bytecode. The one compiler change is the closure-writer switch above: a marker
the type checker already uses for dynamic calls, honoured on a closure so the
existing dynamic writer can compile that closure. The static call-site writer
is not taught GINQ operators.

## Intentional limits

* Names bound by `from` inside `GQ` are dynamic. A list of maps keeps working.
  `Ginq.from` is the front end that checks element types.
* An extension value that is neither a string nor a list of strings is left
  alone. The supported annotation forms are the string and the list.
* When the checker runs, a `GQ` call is still `MacroStub.macroMethod(query)`.
  `toList()` on that call is typed as a raw `List`, so assigning it to
  `List<String>` is the unchecked conversion a dynamic query already allowed.
* `@CompileStatic` on an outer class covers an inner class because the outer
  visitor visits inner classes and reads the outer annotation. The extension is
  attached there.
* Fluent `orderBy` compares keys with their natural order when the query runs.
  The macro's `orderby` clause is unchanged.
