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

New Groovy-specific behaviour that is not core language support
belongs in a `GroovyLspExtension`, not a fork of
`LanguageServerContext`. Do not add OSGi, extra JSON-RPC methods, or
a second hover/completion SPI — `configure` / `initialized` /
`afterCompile` / extra diagnostics (including `ModuleNode`) / extra
code actions / commands are the extension valves. Inject members in
`afterCompile` so core hover and completion see them. The SPI must
not take `internal` types; plugins see `GroovyLspSession` (document
text, folders, classpath, extra settings, apply-edit) and groovy-core
AST (`CompilationUnit`, `ModuleNode`).

Do not add language-server fields or visitor special cases to
groovy-core (`AnnotatedNode` name spans, `AstQuery` parameter walks).
Identifier ranges and parameter caret hits live in groovy-lsp.

Do not hard-code lexer keywords or default-import packages in
groovy-lsp. Keywords come from `GroovyLexer.VOCABULARY`;
default stars from `ResolveVisitor.DEFAULT_IMPORTS`.

## References

- [Root `AGENTS.md`](../../AGENTS.md)
- [`ARCHITECTURE.md`](ARCHITECTURE.md)
- [`src/spec/doc/groovy-lsp.adoc`](src/spec/doc/groovy-lsp.adoc)
