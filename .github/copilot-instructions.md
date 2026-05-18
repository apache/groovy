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

See [`../AGENTS.md`](../AGENTS.md) for project-specific guidance for AI
coding assistants contributing to Apache Groovy, including ASF licensing
and provenance requirements.

## Reviewing tests (inline Javadoc tests)

Groovy tests many methods inline: a `<pre class="...groovyTestCase">`
block inside a Javadoc/GroovyDoc comment is **extracted and executed as
a real JUnit test** by `groovy.test.JavadocAssertionTestSuite`
(`src/test/groovy/MainJavadocAssertionTest.groovy` for `src/main`;
subprojects have their own `*JavadocAssertionTest`). This is the
standard test form for the GDK (`DefaultGroovyMethods`,
`ArrayGroovyMethods`, and similar) — the worked `assert` examples in a
method's Javadoc *are* its test suite.

Therefore, when a change adds or contains `groovyTestCase` Javadoc
blocks covering the new behaviour, **do not flag it as "missing tests"
or "no accompanying unit tests"** — the tests are present and run in
CI. Only flag genuinely untested behaviour (no `groovyTestCase` block
and nothing else exercising it). Canonical detail: see "Inline Javadoc
tests" in [`../CONTRIBUTING.md`](../CONTRIBUTING.md).
