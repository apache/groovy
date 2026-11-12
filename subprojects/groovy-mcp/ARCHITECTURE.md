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

# groovy-mcp — Architecture

Apache Groovy's Model Context Protocol adapter. A model asks the
Groovy compiler about source; the compiler answers. This module does
not compile on its own and does not run scripts.

This document is the contributor map for `subprojects/groovy-mcp/`.
User-facing usage lives in
[`src/spec/doc/mcp-userguide.adoc`](src/spec/doc/mcp-userguide.adoc).
The language core is
[`groovy-lsp`](../groovy-lsp/ARCHITECTURE.md).

## Role

`groovy-mcp` is a sibling of `groovy-lsp`, not a second language
service. `GroovyLanguageEngine` owns buffers, compilation, diagnostics,
hover, navigation, completion, tokens, format, organize-imports, and
rename. This module turns those calls into MCP tools, resources, and
prompts over newline-delimited JSON-RPC on stdio.

There is no public extension SPI. The stable contract is the MCP
protocol described below. The embedding API is `GroovyMcpServer` plus
`McpServerConfig`: one session, one `exchange(String)` call at a time.
`GroovyMcpLauncher` is the process entry.

## What it does not do

- It does not run the source it is shown. There is no evaluate tool,
  and none is added behind a flag.
- `@Grab` and `@ASTTest` stay off. The engine's
  `CompilerSettings.defaults()` keeps `grapeEnabled` and
  `astTestEnabled` false, and `setClasspath` replaces only the
  classpath.
- It does not scan a workspace. `--root` is a jail for `path`
  arguments, not a search root. Startup does not walk the directory.
- It does not invoke Gradle or Maven, download dependencies, or accept
  a classpath from a tool call. `--classpath` is fixed at launch.
- It does not write rename, format, or organize-imports edits. The
  client applies them.
- It does not speak LSP framing. MCP stdio is one JSON value per line.
  Batches (a JSON array) are rejected.

## Transport

Stdout is the protocol stream. Logs go to stderr. `main` moves
incidental `System.out` writes onto stderr before the session starts,
and only when the process is actually serving. `--help` and `--version`
write to stdout and do not redirect it. `run` does not call
`System.exit`; `main` does, after `run` returns.

A frame is UTF-8, without the trailing newline. A CR immediately
before that newline, or at EOF, is the Windows line ending and is not
counted toward `maxMessageBytes`. A CR anywhere else is payload. A
line longer than `maxMessageBytes` is discarded through the next
newline and answered with `-32700`. A line that is not UTF-8 is
`-32700`. The final line of a stream may omit the newline. Input EOF
ends the process with status 0.

`exchange` also measures the UTF-8 size of the string it is given, so
an in-process caller cannot bypass the frame cap. JSON is parsed with
`groovy.json.JsonSlurper` at a nesting depth of 32, and written with
compact `JsonOutput`, so a response stays one line. Parse failures do
not echo the payload. Client strings that must be quoted (an unknown
argument name, a command-line token) pass through `JsonValues.brief`,
which keeps at most 64 code points and replaces controls.

JSON-RPC `id` is a string of at most 1024 characters, or an integral
number. A fractional id, a boolean, or a number that does not fit in a
64-bit integer is `-32600`, and the response id is null so the bad
value is not copied back.

## Lifecycle

`NEW`, then a successful `initialize`, then `INITIALIZED`, then
`notifications/initialized`, then `READY`.

| Call | Before `initialize` | After `initialize`, before the notification | `READY` |
|---|---|---|---|
| `ping` | result | result | result |
| `server/discover` | result | result | result |
| `initialize` | result, once | `-32600` already initialized | `-32600` |
| `notifications/initialized` | ignored | moves to `READY` | ignored |
| any other `notifications/*` | ignored | ignored | ignored |
| tools, resources, prompts | `-32002` | `-32002` | result |

`initialize` selects the client's `protocolVersion` when it is one of
`2025-11-25`, `2025-06-18`, `2025-03-26`, or `2024-11-05`. Any other
non-empty version is answered as `2025-11-25`, the latest handshake
version this server supports. `serverInfo` carries a title, a
description, and `https://groovy.apache.org/`. The phase changes only
after that check succeeds.

`server/discover` answers in every phase and does not change the phase.
It lists `2026-07-28` and the handshake versions. A request whose
`_meta` contains `io.modelcontextprotocol/protocolVersion` set to
`2026-07-28` is served without `initialize`. The same key set to any
other version is `-32022`, with `error.data.supported` and a shortened
`requested` value. `ping` is not part of that per-request mode. List
results and `resources/read` include `resultType`, `ttlMs` of one hour,
and `cacheScope` of `public`, because those payloads do not depend on
the caller and do not change for the life of the process. A per-request
result also carries `serverInfo` under `_meta`. A notification that
fails is logged (class name and a short
message, never source text) and produces no response. An unexpected
exception on a request is `-32603` with the message `Internal error`.

`notifications/cancelled` is a no-op. The session is single-threaded:
`exchange` and `close` share one lock, and the engine is not called
from another thread.

Tool mistakes (`ToolFailure`, or an `IllegalArgumentException` /
`IllegalStateException` from the engine) are a successful JSON-RPC
result with `isError: true`. An unknown tool is the same kind of
result, with the message `unknown tool`, so a model can pick another
tool. The supplied name is not echoed. Unknown resources, unknown
prompts, and a tool or prompt argument that is not an object are
protocol errors (`-32602`).

## Tools

Every tool is read-only on the user's files (`readOnlyHint`,
`destructiveHint: false`, `openWorldHint: false`). Positions are
0-based UTF-16, the same numbers the engine already uses. They are not
converted to code points.

| Tool | Required | Engine call |
|---|---|---|
| `groovy_diagnostics` | `text`, `path`, or `uri` | `diagnostics` |
| `groovy_describe` | source, `line`, `character` | `describe` |
| `groovy_definition` | source, `line`, `character` | `definition` |
| `groovy_implementations` | source, `line`, `character` | `implementations` |
| `groovy_references` | source, `line`, `character` | `references` |
| `groovy_symbols` | none; `query` and `maxResults` optional | `symbols` |
| `groovy_complete` | source, `line`, `character` | `complete` |
| `groovy_signatures` | source, `line`, `character` | `signatures` |
| `groovy_tokens` | source | `tokens` |
| `groovy_format` | source; `tabSize` 1..16, `insertSpaces` | `format` |
| `groovy_organize_imports` | source | `organizeImports` |
| `groovy_rename` | source, `line`, `character`, `newName` | `rename` |
| `groovy_close` | `uri` | `closeBuffer` when open |

`groovy_symbols` may omit the source. It then searches buffers that are
already open. Passing only `query` is valid. The other source tools
reject a call that has neither `text`, nor `path`, nor `uri`.

`text` and `path` together are rejected, and so are `path` and `uri`.
`text` together with a `uri` is the way to name an in-memory buffer.
The input schema's `oneOf` accepts that pair, and accepts `path` or
`uri` alone. `groovy_symbols` may also match none of those keys.
`groovy_rename` requires `newName` in the schema, not only at runtime.
`text` opens a `groovy-buffer` URI (the default is
`groovy-buffer:snippet`) and is not attached to a `file:` URI, because
a file URI would make the compiler treat the buffer as a path.
`path` is resolved under `--root` and opened with `engine.open`.
`uri` must already be open. A `file:` URI is accepted only when both
the lexical path and `toRealPath()` stay inside the root. The buffer
identity is the real path, which is what `open(Path)` stores, so a
URI that spells a symlink still finds the open file.
`Path.startsWith` is component-wise, so a sibling directory with the
root's name as a prefix does not match.

`newName` must be a Java identifier of at most 200 code points. An
empty rename result means the compiler did not prove the binding. That
is a successful tool result, not an error. `groovy_close` drops a
buffer and does not delete a file.

List caps, applied after the engine returns, keep one payload bounded:
diagnostics 200, sites 50, completions 200, tokens 4000, edits 2000.
`maxResults` defaults to 50 and cannot exceed 200. Symbols asks the
engine for one extra hit so `truncated` is true only when a hit was
actually dropped. `line` is `0..1000000` and `character` is
`0..100000`.

A tool JSON text longer than `maxResultChars` is replaced with an
`isError` payload. The replacement is `result exceeds the size limit`,
or `too large` when that sentence itself does not fit the cap.

## Resources and prompts

| URI or name | Body |
|---|---|
| `groovy://language/keywords` | `GroovyLanguageEngine.keywords()` |
| `groovy://language/default-imports` | `GroovyLanguageEngine.defaultImports()` |
| `groovy://server/limits` | execution flag, grape and ASTTest flags, classpath size, root, caps |

Keyword and import lists are not copied into this module. The limits
resource reports `execution: false` and the live compiler flags, and it
reports how many classpath entries were set, not the paths.
`resources/templates/list` is empty. A non-blank `cursor` on any list
method is `-32602`. A resource body over `maxResultChars` is `-32602`.

`explain-groovy` takes `code` and an optional `question`.
`review-groovy` takes `code`. The code is data inside the prompt text.
The prompt tells the model to call the compiler tools and not to run
the source. `code` longer than `maxSourceChars` is `-32602`.

## Caps and classpath

`McpServerConfig` defaults are 2 MiB per message, 512 Ki chars of
source (UTF-16 length for `text`, file bytes for `path`), and 512 Ki
chars of result JSON. A non-positive cap is rejected. The classpath
list is copied. A null root allows in-memory buffers only.

The server version string is the jar's `Implementation-Version`, or
`dev`. It is never the literal `null`.

## Build and distribution

The module is `optionalModule()` and is not part of `groovy-all`.
Binary-compatibility checks are off until a release has published
`org.apache.groovy:groovy-mcp` for the baseline to resolve. That is
the same stance as `groovy-lsp`.

Runtime dependencies are the in-tree `groovy-lsp` and `groovy-json`
projects. LSP4J arrives transitively from `groovy-lsp`; this module
does not declare it again. The jar is on
`distributionsRuntimeClasspath`, so the binary distribution copies it
into `lib/`. `src/bin/groovymcp` and `groovymcp.bat` are the launchers.
`.gitignore` ignores every `bin/` directory, so those two scripts are
force-added, the same way `groovylsp` is already tracked. The jar
manifest `Main-Class` is `org.apache.groovy.mcp.GroovyMcpLauncher`.

Public types are `GroovyMcpServer`, `GroovyMcpLauncher` (and its
`Parsed` record), and `McpServerConfig`. Everything under `internal`
can change with the module.

## Why this module does not depend on the MCP Java SDK

The wire protocol is implemented in this module. The candidate library
was the official Java SDK at
<https://github.com/modelcontextprotocol/java-sdk>
(`io.modelcontextprotocol.sdk`). Its licence is MIT, which Apache
projects may ship with a `NOTICE` entry, so the licence is not the
reason it was left out.

`mcp-core` requires `reactor-core`, `slf4j-api`, and
`com.fasterxml.jackson.core:jackson-annotations` at runtime. The
convenience artifact `mcp` adds Jackson 3 (`tools.jackson.core:jackson-databind`)
and `com.networknt:json-schema-validator`. A caller can replace the
JSON mapper, and that still leaves the annotations jar and Reactor on
the classpath. `jakarta.servlet-api` is `provided`, so a stdio-only
server would not pull the Servlet API. This jar is on
`distributionsRuntimeClasspath`, so every runtime dependency is copied
into the binary distribution's `lib/`. Groovy's CSV, YAML, and TOML
modules already use Jackson 2 (`com.fasterxml`). Taking the convenience
artifact would put a second Jackson major beside them.

The SDK's synchronous server is a facade over Reactor, and it is built
for concurrent sessions. `GroovyLanguageEngine` compiles on one thread.
A binding would have to serialize every call, or accept overlapping
compiles. An SLF4J binding can also write to stdout and break the
stdio protocol stream. The SDK does not implement this module's path
jail, the message and source caps, the refusal to run scripts, the
disabled Grab and ASTTest switches, or the rule that a tool error does
not repeat the name the client sent.

The SDK has already published breaking migration guides for 1.0 and
2.0. The 2.0 line speaks protocol `2025-11-25`, and later trunk work
moved on to the `2026-07-28` revision. Groovy's release train would
have had to follow those majors for a stdio subset.

What this server needs from those revisions is implemented here, in
the existing session. Handshake clients still use `initialize`.
`server/discover` and a request whose `_meta` selects `2026-07-28`
cover the per-request era. List results carry cache hints. Tool
schemas use JSON Schema 2020-12, including `outputSchema`. Tool names
follow SEP-986. Sampling, elicitation, tasks, subscriptions, and HTTP
transports stay out: this process does not call a model and does not
listen on a socket. `notifications/cancelled` stays a no-op because a
compile cannot be interrupted and the session handles one request at
a time.

LangChain4j is a separate case. It is an LLM client framework. Its
MCP support calls other servers. It does not host a compiler, and it
would add another client stack to the distribution.

Revisit a dependency on `mcp-core` only if this module grows
multi-session HTTP, sampling, or tasks. Even then, do not take the
Jackson 3 convenience artifact, and treat the dependency as a new
runtime dependency that needs the usual licence and `NOTICE` review.

## Security properties

The process that launches the server chooses the root and the
classpath. A model cannot widen either through a tool. Symlinks are
resolved and rejected when the real path leaves the root. Source text
is not written to the log. `@Grab` and `@ASTTest` cannot run during a
query compile, because the engine never turns those settings on.
Compiling is not executing: global transforms that the stock compiler
runs are still part of a query, which is why the two switches that
have a compile-time execution path stay off.

This module does not change [`THREAT_MODEL.md`](../../THREAT_MODEL.md).
Groovy remains a language that runs the program it is given when a
user asks it to. `groovy-mcp` is a reader of source, not that runner.
