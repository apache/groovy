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

# groovy-lsp — Architecture

Apache Groovy's Language Server Protocol implementation. The compiler AST
is the source of truth; Eclipse LSP4J is the JSON-RPC transport.

This document is the contributor map for `subprojects/groovy-lsp/`.
User-facing usage lives in
[`src/spec/doc/groovy-lsp.adoc`](src/spec/doc/groovy-lsp.adoc).

## Why a first-party server

Community servers (`groovy-language-server`, `gvy`/`gls`,
`trustytrojan/groovyls`, `tomaszrup/groovy-language-server-edt`),
`kotlin-lsp`, Metals, and Eclipse JDT LS informed the shape, but
this module is original Apache License 2.0 code. It compiles with the
in-tree Groovy compiler rather than a fork, so language changes land
here automatically.

### What it is for

A **Groovy language core** that editors and AI tools can share:

- Humans in an editor that speaks LSP (VS Code, IntelliJ via an LSP
  client, Vim, …) get diagnostics, completion, hover, navigation and
  conservative rename on `.groovy` sources.
- AI agents get the same snapshot: structure, types where the compiler
  knows them, and edits that refuse to guess. A later `groovy-mcp`
  module is expected to wrap this server so a model can ask “what is
  this identifier?” without re-implementing the compiler.

It is intended for **direct use by an IDE or editor as an LSP
server**, as the **semantic backend** for Groovy-aware AI, and as the
**Groovy-AST compile + protocol loop** other Apache-2.0 Groovy LSPs
can sit on. Plugins implement incubating
[`GroovyLspExtension`](src/main/java/org/apache/groovy/lsp/spi/GroovyLspExtension.java)
(`ServiceLoader` or tests register an `ExtensionHost`). That is how
Jenkins GDSL, a forced STC pass, or `groovy-mcp` add behaviour
without forking `LanguageServerContext`.

It is not a complete IDE. Eclipse JDT / groovy-eclipse servers
cannot replace this compiler through the SPI — they remain a
parallel stack. GDSL and Maven downloads stay in the *plugin*, not
in this module.

### What it is not

The server does **not** try to replace Groovy-Eclipse, IntelliJ IDEA,
or a Gradle/Maven-aware IDE. Out of scope:

- Joint Groovy+Java compilation and cross-language rename
- GDSL / Jenkins pipeline descriptors
- Invoking Gradle or Maven (the client pushes `groovy.classpath`)
- Debugger, test runner, build tool windows
- Identifier-text “rename every `foo` in the file” fallbacks

Those are the differentiator of `groovy-language-server-edt` (EPL,
JDT + groovy-eclipse) and, for Jenkins GDSL, `groovyls` (Apache 2.0
fork of Prominic). This module stays on the Groovy AST.

### Positions (not an LSP break)

GROOVY-12085 made **compiler AST columns** 1-based Unicode code
points. LSP positions stay 0-based in the negotiated encoding. The
server converts; clients never see AST columns. Existing LSP clients
do not need to change.

### Semantic guarantees

| Feature | When `methodTarget` is set | When it is not (typical dynamic Groovy) |
|---|---|---|
| Hover | Bound signature | Unique arity match on a resolved receiver, labelled `_inferred_`; otherwise `name(...)` |
| Definition | That method | Every arity-matching candidate with a source location; never a random first match |
| Rename / prepareRename | Allowed | **Refused.** A heuristic must not rewrite the wrong overload. |

Declarations (class, method, field, parameter, bound local) rename by
identity (owner + name + descriptor), not the unqualified identifier.

### Relation to other Groovy language servers

- **Prominic `groovy-language-server`.** Earlier community server;
  this module is a new implementation on the current compiler, not a
  fork.
- **`trustytrojan/groovyls`.** Apache 2.0 fork adding Jenkins GDSL and
  a forced static-type-checking pass. Designs that fit (initializer
  types, refuse unresolved rename) are reimplemented here. GDSL and
  Aether downloads are not.
- **`tomaszrup/groovy-language-server-edt`.** EPL 2.0, Equinox + JDT +
  groovy-eclipse: joint Java/Groovy, library sources, file-move
  imports. Complementary product; none of its source is copied.
  Its identifier-scan rename fallback is intentionally **not** used.

### AI / MCP

A model using this server (directly over LSP, or later through
`groovy-mcp`) can trust: document symbols, diagnostics, hover, and
definition candidates. It cannot yet trust flow-sensitive types on
`def` (no STC intelligence pass — documented follow-up), Java
members without a client-supplied classpath, or rename of a dynamic
call. Wrong edits are worse for an agent than a refused rename.

## Layers

```
┌──────────────────────────────────────────────┐
│  GroovyLanguageServer / Launcher (stdio)     │
├──────────────────────────────────────────────┤
│  GroovyTextDocumentService                   │
│  GroovyWorkspaceService                      │
├──────────────────────────────────────────────┤
│  feature/*   (completion, hover, nav, …)     │
├──────────────────────────────────────────────┤
│  compile/*   (CompilationUnit, AstQuery)     │
│  position/*  (Groovy 1-based code points ↔   │
│               LSP encoding)                  │
│  workspace/* (open buffers)                  │
└──────────────────────────────────────────────┘
```

- **Transport.** Eclipse LSP4J 1.0 (LSP 3.18 types). EPL 2.0 Category B
  binary; not mixed into Groovy sources. See `licenses/lsp4j-BINZIP.txt`.
- **Compile.** `CompilationUnit.compile(Phases.SEMANTIC_ANALYSIS)` with
  `tolerance = 0` (unlimited). Class generation is skipped. Errors are
  collected; a partial AST is still queried. `@Grab` is disabled via
  `disabledGlobalASTTransformations`. `@ASTTest` is a local transform, so
  it is disabled with `groovy.asttest.enable=false` (and Grape with
  `groovy.grape.enable=false`) unless the client sets
  `groovy.astTestEnabled` / `groovy.grapeEnabled`. Diagnostics are
  filtered to the owning source unit — `CompilationUnit` shares one
  `ErrorCollector` across files. The `GroovyClassLoader` is reused until
  the classpath (or Grape / `@ASTTest` flags) change, and closed on
  session shutdown.
- **Navigation.** `AstQuery.nodeAt` lifts method/property name
  `ConstantExpression` leaves to the enclosing call, and the compiler
  query visits parameters so a caret on `bar` in `def foo(String bar)`
  hits the parameter. Rename and references match declaration identity
  (owner + name + descriptor), not the unqualified identifier. Call and
  type hierarchy incoming/outgoing/super/sub walk the snapshot.
- **Edits.** `groovy.lsp.organizeImports` preserves star, static and
  alias imports and sorts them. Unused simple imports are a separate
  "Remove unused imports" action. The organize command applies a
  `WorkspaceEdit` when the client advertised `workspace.applyEdit`.
  Extra actions (Metals / JDT LS inspired, original code): remove
  unused imports, implement abstract methods, insert inferred field
  types, add a package from the file path, create a missing class
  stub, generate accessors / toString / equals / constructor, and
  add {@code @Override}. Reference-count code lenses. Renaming a
  class whose name matches the file stem also renames the file.
  Completions of workspace types include `additionalTextEdits`;
  `completionItem/resolve` fills Groovydoc. Groovy's default imports
  are not re-inserted. Newline on-type formatting indents the next
  line. Compile reports `window/workDoneProgress` when the client
  supports it. References skip files that do not contain the
  identifier as a word.
- **Compile cache.** A fingerprint of open buffers, extra-file mtimes,
  settings and extension ids skips a full recompile when nothing
  changed. Compile runs off the session lock so document sync is
  not blocked; a generation counter drops superseded compiles. When the
  client omits classpath and source paths, `WorkspaceLayout` uses
  `src/main/groovy` (and friends) plus `lib/*.jar` — it never invokes
  Gradle or Maven.
- **Extensions.** `GroovyLspExtension` may `configure` the
  `CompilerConfiguration`, run `afterCompile` on the live
  `CompilationUnit` (before the snapshot freezes), add diagnostics
  and code actions by URI, and handle extra commands through
  `GroovyLspSession.applyEdit`. A throwing plugin is isolated; it
  must not compile or publish on its own.
- **Positions.** AST columns are 1-based Unicode code points (see
  `PositionConfigureUtils` and GROOVY-12085). LSP characters are 0-based
  in the negotiated encoding (`utf-16` default, `utf-8` / `utf-32`
  optional). Identifier selection for rename/highlight is
  `Positions.toNameRange`: it finds the node's name in its source
  span so groovy-lsp does not add fields to `AnnotatedNode`.
  Implicit-this calls without `methodTarget` resolve against the enclosing
  class. Field references keep the declaring type, so two `name` fields
  do not collide.

## Capability policy

Advertise only what is implemented. Groovy has no document colors or
notebook cells, so those capabilities are omitted. Incoming 3.19 fields
the server does not understand are ignored (LSP rule).

## Concurrency

Document store is concurrent. Compilation runs on a single daemon thread
with 150 ms debounce on `didChange`. Feature requests read the last
completed `CompilationSnapshot` and do not block the compile thread.

## Testing

In-process: construct `GroovyLanguageServer`, connect a collecting
`LanguageClient`, `didOpen`, then `context.recompile()` (do not wait on
the debounce in unit tests). Do not set `initialize.rootUri` to the
process working directory — that would compile the whole checkout.
JSON-RPC round-trips are not required to prove feature logic.

## Related

- LSP 3.18: https://microsoft.github.io/language-server-protocol/specifications/lsp/3.18/specification/
- LSP 3.19 (draft): https://microsoft.github.io/language-server-protocol/specifications/lsp/3.19/specification/
- Compiler pipeline: repository-root `ARCHITECTURE.md`
