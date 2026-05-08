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

# Agent Guide for groovy-groovysh

Subproject-specific supplement to the [root `AGENTS.md`](../../AGENTS.md).

## What's special about this subproject

- groovysh is the interactive Groovy REPL, built on JLine 4.x.
- It vendors a small set of files derived from JLine sources, plus a
  deep fork of `GroovyEngine.java` that we maintain ourselves.
- Tests touch a real JLine `Terminal`, which makes them more
  platform-sensitive than the rest of the codebase.

For substantive guidance — what to read first, the vendored fork
inventory, test layers, top failure modes, the JLine bump procedure,
and the platform-fragility checklist — load the
[`groovysh`](../../.agents/skills/groovysh/SKILL.md) skill.

## References

- [Root `AGENTS.md`](../../AGENTS.md) — licensing, commit conventions, project-wide rules.
- [`src/spec/doc/groovysh.adoc`](src/spec/doc/groovysh.adoc) — user-facing reference.
- [`LICENSE`](LICENSE) — provenance for the BSD-licensed vendored files.
