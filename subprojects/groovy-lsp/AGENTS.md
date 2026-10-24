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

# Agent Guide for groovy-lsp

Subproject-specific supplement to the [root `AGENTS.md`](../../AGENTS.md).

For architecture — layers, capability policy, position encoding,
compile-without-classgen, scope vs other Groovy LSPs, rename
guarantees, and the test harness — see
[`ARCHITECTURE.md`](ARCHITECTURE.md).

Rename of a method *call* is allowed only when `methodTarget` is
set. Do not add an identifier-text fallback (that is how
`groovy-language-server-edt`'s AST path rewrites the wrong `foo`).

New Groovy-specific behaviour that is not protocol adaptation belongs
in one `feature/*` type (not a shared grab bag), `compile/*`, or the
in-process `engine/*` facade, not a public SPI. Call and override
resolution lives in `MethodBinding`, shared by navigation, hover,
completion and rename. `GroovyLanguageEngine` remains an internal
facade that returns JDK records. Groovy Console and groovysh do not
use it. They share `GroovyLanguageClient`, which calls
`GroovyLanguageServer` the way a remote editor does (`initialize` without a workspace root,
`textDocument/*`, `workspace/didChangeConfiguration`,
`workspace/executeCommand`). Those calls stay in-process. They are not
JSON-serialized on each keystroke. Hosts do not import
`org.apache.groovy.lsp.internal`.
A highlighter sets `groovy.compile` to `false` and paints
`textDocument/semanticTokens`. String modifiers `slashy` and
`singleQuoted` are append-only. A string without them is double-quoted.
The `$` that ends a GString begin or part is omitted from the string
token. `groovy.imports`, `groovy.starImports`, and
`groovy.scriptBaseClass` are client settings. `groovy.parentLoader`
accepts a `ClassLoader` in the same JVM. A remote client sends
`groovy.classpath` instead. `groovy.scriptNames` returns script-level
names. `groovy.compile` compiles immediately. Hosts resend
configuration only when it changed, so an unchanged buffer reuses the
snapshot.
Brace matching uses the append-only `delimiter` semantic-token type
from that same request, so the console does not import `GroovyLexer`.
groovysh keeps nanorc.
The server does not reserve buffer names. Console and groovysh each take
an `implementation` dependency on groovy-lsp. groovysh does not depend
on groovy-console. LSP4J is an `api` dependency of groovy-lsp so those
hosts can compile against the protocol types. They do not re-export
groovy-lsp. `/console` and `/inspect --gui` load `groovy.console.ui`
by name when that jar is already on the classpath.
Configuration is `workspace/didChangeConfiguration` and
`workspace/configuration` under the `groovy` section (see
`lsp-userguide.adoc`). Do not add OSGi, extra JSON-RPC methods, or a
second hover/completion SPI. Do not add Maven/Gradle invocation,
DAP, or a test runner.

Do not add language-server fields or visitor special cases to
groovy-core (`AnnotatedNode` name spans, `AstQuery` parameter walks).
Identifier ranges and parameter caret hits live in groovy-lsp.

Do not hard-code lexer keywords or default-import packages in
groovy-lsp. Keywords come from `GroovyLexer.VOCABULARY`;
default stars from `ResolveVisitor.DEFAULT_IMPORTS`.

`groovy-mcp` is the MCP adapter over `GroovyLanguageEngine`. Do not
grow a second compiler or a second hover path for it. See
[`../groovy-mcp/AGENTS.md`](../groovy-mcp/AGENTS.md).

## References

- [Root `AGENTS.md`](../../AGENTS.md)
- [`ARCHITECTURE.md`](ARCHITECTURE.md)
- [`src/spec/doc/lsp-userguide.adoc`](src/spec/doc/lsp-userguide.adoc)
