<!--
SPDX-License-Identifier: Apache-2.0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Contributing

We welcome all contributors. This file covers code contributions to the
Groovy repository. For ways to contribute that don't involve changing the
code — helping on the mailing lists, reporting issues, writing blog posts,
or contributing to the reference documentation on the website — see the
[project contribute page](https://groovy.apache.org/contribute.html).

## Building and testing

JDK 17 or later is required. The canonical build instructions live in
[`README.adoc`](README.adoc). The short form:

```
./gradlew clean dist                                # full build
./gradlew test                                      # run tests
./gradlew :<module>:test --tests <TestClassName>    # run a single test
```

Use the Gradle wrapper (`./gradlew` / `gradlew.bat`); do not invoke a
system `gradle`. Most modern IDEs open the Gradle project directly.

Run `./gradlew test` locally before sending a pull request. All tests
should be green.

## Documentation

Documentation is a first-class deliverable, not an afterthought. When
contributing code, please treat documentation as part of the change:

- **Docs live in the code repository.** AsciiDoc sources are under
  `src/spec/doc/` for cross-cutting material and
  `subprojects/<module>/src/spec/doc/` for module-specific material.
  Each large module should have at least one AsciiDoc file covering
  what it offers.
- **Examples are executable.** Code snippets in the AsciiDoc are
  `include::`'d from real Groovy files under a matching `src/spec/test/`
  directory, so every example compiles and runs as part of the build.
  When you add an example, make it executable the same way — see any
  existing `.adoc` file under `subprojects/*/src/spec/doc/` for the
  pattern.
- **Documentation changes ship with the code.** If a pull request adds,
  changes, or removes user-visible behaviour, the relevant AsciiDoc and
  test examples should change in the same pull request. Reviewers will
  ask.
- **Groovydoc is part of the public API.** Public classes and methods
  need accurate Groovydoc/Javadoc. Match the style of existing classes
  in the module you're editing.

Cross-version reference documentation, the GDK, and the website itself
live in the separate [`groovy-website`](https://github.com/apache/groovy-website)
repository; see the [project contribute page](https://groovy.apache.org/contribute.html)
for how to contribute there.

## Submitting a pull request

1. Fork <https://github.com/apache/groovy> and create a feature branch.
2. Reference the JIRA issue in commits, for example
   `GROOVY-12345: short description`.
3. Keep commits focused. A bug fix, a refactor, and a formatting pass
   are three separate commits (or pull requests), not one.
4. Run `./gradlew test` locally and confirm it passes.
5. Open a pull request against `master`.

GitHub's [fork a repo](https://docs.github.com/en/get-started/quickstart/fork-a-repo)
and [creating a pull request](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request)
guides cover the generic git mechanics.

## Using AI tooling

Contributors using AI coding assistants (Claude Code, Codex, Cursor, Copilot,
Gemini, Aider, and similar) should read [AGENTS.md](AGENTS.md) for
project-specific guidance, and in particular follow the ASF's
[Generative Tooling guidance](https://www.apache.org/legal/generative-tooling.html).
If AI tooling authored a non-trivial part of a change, consider adding a
`Generated-by: <tool name and version>` trailer to the commit message.
The contributor remains responsible for the licensing, correctness, and
style of everything they submit.
