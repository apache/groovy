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
| `@TypeChecked(SKIP)`, or `@CompileStatic` / `@CompileDynamic` on a class or method the static-compilation visitor skips | Same as no checking annotation. |
| `@TypeChecked` class, method `@CompileDynamic` or `@CompileStatic(SKIP)` | The method is still type-checked. `@CompileDynamic` is not `@TypeChecked(SKIP)`. The row extension is installed on the class annotation the type-checked visitor reads. |
| `@TypeChecked` | The enclosing method is type-checked. Row access inside the query is dynamic per expression (below). |
| `@CompileStatic` | The enclosing method is type-checked and statically compiled. Row access inside the query is dynamic per expression (below). |

`@TypeChecked` does not static-compile. The same extension is registered for
both annotations because both need the row rules. Which annotation counts is
the same decision `StaticTypeCheckingVisitor.isSkipMode` makes: the
type-checked visitor looks only at `@TypeChecked`, and the static-compilation
visitor looks at `@TypeChecked` and `@CompileStatic`. `@CompileDynamic` is
`@CompileStatic(SKIP)` and skips only that second visitor.

The extension is `GinqRowChecks`. The macro, or the `@GQ` transform, appends
its name to the `extensions` member of the annotation that the checker will
actually read: the class annotation, or an outer class annotation when the
query is in an inner class, or the method annotation when the method carries
its own non-skip checking annotation. That member is the existing registration
point. A string, a class literal, or a list or array of those is updated. Any
other shape is a compile error and is left unchanged, so a value the bridge
does not understand is never dropped silently. Nothing is appended for a scope
the checker will not visit.

## Row access is dynamic; the method is not

The lowered clause bodies are not Java. An operator on a row and a property of
a map do not have a static target the checker can prove. The static call-site
writer throws when it is asked to emit an operator it cannot prove
(`StaticTypesCallSiteWriter.makeSingleArgumentCall`).

The query closure is emitted by the dynamic writer
(`DYNAMIC_RESOLUTION` set to `Boolean.TRUE` on that closure only). A static
lambda cannot capture a local of an enclosing closure, which is what the
read-only snapshot array is, and a statically inferred clause lambda rejects a
row comparison whose types are not provable. Rewriting those operators into
per-expression calls was tried and dropped: it broke `orderby ... in desc`,
join predicates, and parallel clause lambdas. The enclosing method stays on
the static writer. The marker is not applied to every expression in the method.

The marker is on the closure, so it is not limited to GINQ operators. Every
expression in that closure is compiled dynamically, including a user call in
`where` or `select` and a call whose target another type-checking extension
has already chosen. For example, an extension that selects `choose(CharSequence)`
for a `String` argument is honoured in the enclosing method and ignored inside
the query: the call is dispatched at runtime and hits `choose(String)`.
Compiling the clause bodies with the static writer, and leaving only the
unprovable row operators dynamic, was tried again. It keeps that extension
target, and then breaks calls the dynamic query already accepted: an owner
method such as `note(n)` is emitted as `invokevirtual` of a method `Object`
does not have, a join alias is resolved against the query object, and parallel
joins stop returning rows. The closure marker stays until the static writer
can emit those calls without changing them.

`GinqRowChecks` is still a normal type-checking extension, appended after
extensions already on the annotation. Inside the marked closure every call is
`makeDynamic`, so the dynamic writer does not invoke a stand-in target. The
checker does not infer the clause-lambda parameter types. Extensions registered
ahead of `GinqRowChecks` still see the calls the visitor delivers; what those
calls are named is up to that visitor (`toList`, the macro call, `defer`), not
a guarantee that `where` and `select` are visible as those names.

Outside the marked closure the extension has an opinion only for a query result
used as the receiver of `toList`, `get`, `getAt`, or a subscript. That is the
row value the macro leaves typed as `Object`. A field, an enclosing-method
parameter, and `value[0]` on that parameter stay compile errors. A local that
holds the query, such as `result[0].toList()`, is accepted.

There is no synthetic method and no `CompilationUnit.addNewPhaseOperation`.
The closure the macro already builds is the compiler's capture.

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
analysis, and not the fluent implementation. It is public because compiled
code calls `LazyQueryable.defer`, and that method is part of the binary ABI
(`@GroovyABI`). The class is `@Internal`: callers program to `Queryable`.
Joining, grouping, and the aggregates are forwarded to the pipeline built on
first use, and that pipeline is cached, matching `QueryableCollection.toList`.

## Locals

The deferred closure is a normal Groovy closure in the enclosing method, and
the closure writer captures it. A local the query assigns is a closure-shared
variable, so the write is visible to the caller. That is the compiler's rule.

A local the query only reads is different. Dynamic `GQ` creates the clause
lambdas when the expression runs, so a later assignment is not what those
lambdas read. A static `GQ` creates the closure at the expression and calls it
on the first read. If the caller assigns the local again, the closure writer
treats it as shared and the deferred call would see the new value. The bridge
therefore copies those read-only locals into an array when the expression
runs, and the deferred closure reads the array.

Which locals those are is `VariableScopeVisitor`'s answer, not a set of names.
The visitor links each reference to the declaration in scope. A field or a
property and a local of the same name are distinct, as are a closure parameter,
a local declared in a nested closure, and a name that is not a local. A name
that resolves to nothing is a `DynamicVariable` (an import alias, including one
written in lower case, or a class name the resolver has not rewritten yet) and
is not copied. A field or a property is not copied; it is read when the closure
runs, so an assignment after the expression and before a deferred terminal is
visible. `GQL` and `@GQ(List)` have already read it. The bridge does not
rewrite the caller's declarations, box the variables, or emit stores. Two
locals of the same name in nested scopes are still rejected.

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
`StaticCompilationVisitor`, does not add a compile phase, and does not teach
the static call-site writer about GINQ operators. The one compiler change is
the existing dynamic-call marker honoured on a closure: `StaticTypesClosureWriter`
emits that closure with the dynamic writer. A type-checking extension already
uses the same marker, with a type as its value, for one expression. `Boolean.TRUE`
on the closure is the switch for the whole closure, and only the lowered query
closure sets it. Row properties that the checker cannot resolve are still
`makeDynamic` on that expression.

## Re-enumeration

Two sources, two rules.

* `Ginq.from` on an `Iterable` or array re-reads that source on every terminal.
  `where`, `select`, and the other operators record the work; they do not copy
  the elements. Adding to the list between two `toList()` calls is visible.
  This is the LINQ-to-Objects rule.
* `Ginq.from` on a `Stream` reads that stream on the first terminal and replays
  the buffered elements after that. A stream cannot be read twice.
* `Queryable.stream()` and a fluent `stream()` return a new stream every time.
  Creating the stream does not pull. A one-shot pipeline is buffered as its
  elements are pulled, and a later `stream()` reads that buffer from the start
  before pulling anything still unread. A sized pipeline can be counted without
  pulling, so a projection with a side effect is not run merely because
  `exists` asks for `count`. After the one-shot source has been read to the
  end, later calls stream the buffer. A caller is never handed a stream that
  has already been consumed.
* A `GQ` / `QueryableCollection` terminal still materializes once and then
  reuses that list. Building the pipeline does not iterate. That is the
  behaviour dynamic `GQ` already had.

## Intentional limits

* Names bound by `from` inside `GQ` are dynamic. A list of maps keeps working.
  `Ginq.from` is the front end that checks element types.
* When the checker runs, a `GQ` call is still `MacroStub.macroMethod(query)`.
  `toList()` on that call is typed as a raw `List`, so assigning it to
  `List<String>` is the unchecked conversion a dynamic query already allowed.
* `@CompileStatic` on an outer class covers an inner class because the outer
  visitor visits inner classes and reads the outer annotation. The extension is
  attached there.
* Fluent `orderBy` compares keys with their natural order when the query runs.
  The macro's `orderby` clause is unchanged.
