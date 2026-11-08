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

# Agent Guide for groovy-mcp

Subproject-specific supplement to the [root `AGENTS.md`](../../AGENTS.md).

For the protocol, the caps, and the relationship to the language
engine, see [`ARCHITECTURE.md`](ARCHITECTURE.md). User-facing usage
is [`src/spec/doc/mcp-userguide.adoc`](src/spec/doc/mcp-userguide.adoc).

This module is a Model Context Protocol adapter over
`GroovyLanguageEngine`. Do not reimplement compile, hover, completion,
rename, tokens, or diagnostics here. Do not add a second compiler, a
public SPI, or a dependency on the MCP Java SDK
(<https://github.com/modelcontextprotocol/java-sdk>) or an LLM framework.
The protocol types stay in this module. The dependency decision is in
ARCHITECTURE.md.

Do not execute scripts. Do not enable `@Grab` or `@ASTTest`. Do not
scan a workspace on startup. `--root` only confines `path` arguments.
Do not accept a classpath from a tool call. Do not hard-code lexer
keywords or default-import packages; read them from the engine.

Positions passed to the engine stay 0-based UTF-16. Stdout is
newline-delimited JSON-RPC. Logs go to stderr and must not contain
source text.

## References

- [Root `AGENTS.md`](../../AGENTS.md)
- [`ARCHITECTURE.md`](ARCHITECTURE.md)
- [`../groovy-lsp/AGENTS.md`](../groovy-lsp/AGENTS.md)
- [`src/spec/doc/mcp-userguide.adoc`](src/spec/doc/mcp-userguide.adoc)
