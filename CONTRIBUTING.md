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
[project contribute page](https://groovy.apache.org/).

## Building and testing

JDK 17 or later is required. The canonical build instructions live in
[`README.adoc`](README.adoc). The short form:

```
./gradlew clean dist                                # full build
./gradlew test                                      # run tests
./gradlew :<module>:test --tests <TestClassName>    # run a single test
```

Use the Gradle wrapper (`./gradlew` / `gradlew.bat`) rather than a
system `gradle` — the wrapper pins the version the build expects.
Most modern IDEs open the Gradle project directly.

Run `./gradlew test` locally before sending a pull request. All tests
should be green.

### Running your local build

To exercise a build with your changes applied — running scripts,
trying the REPL, or smoke-testing behaviour the test suite doesn't
cover — produce a local installation:

```
./gradlew :groovy-binary:installGroovy
```

The installation lands under
`subprojects/groovy-binary/build/install/`. Its `bin/` directory
contains the `groovy`, `groovysh`, `groovyc`, and `groovyConsole`
launchers, so you can invoke

```
subprojects/groovy-binary/build/install/bin/groovy <script>
```

to run a script against the build you just produced.

On Unix-like systems, if you have SDKMAN (or any other tool that
sets `GROOVY_HOME` to a fixed installation), `unset GROOVY_HOME`
before running the launchers — otherwise they pick up that
environment variable instead of using the local build, and your
changes appear not to take effect.

## Tests

For an overall map of the test layout, see
[`ARCHITECTURE.md`](ARCHITECTURE.md). This section covers the bits a
contributor most often needs to know.

### Targeted runs

Reach for the narrowest run that reproduces what you're working on
before falling back to the full suite:

```
./gradlew :test --tests <FullyQualifiedClassName>             # one class, core
./gradlew :test --tests <FQN>.<methodName>                    # one method
./gradlew :<subproject>:test --tests <FQN>                    # one class, subproject
./gradlew :<subproject>:test                                  # one whole module
./gradlew --rerun-tasks :test --tests <FQN>                   # bypass the up-to-date cache
```

The full `./gradlew test` is appropriate as a final check before a PR
but is the wrong feedback loop for development.

### Test framework

New tests use **JUnit 5**: `org.junit.jupiter.api.Test` with
`org.junit.jupiter.api.Assertions.*`. Older tests in the tree use a
mix of JUnit 3 (`extends GroovyTestCase`) and JUnit 4 — match the
surrounding file when adding a method to an existing test class, but
write new test classes in JUnit 5. Spock is bundled and available,
but the core repo's own tests are not generally Spock-based; reach
for it only when you have a specific reason.

Test method names in new classes drop the `test` prefix that older
JUnit required — JUnit 5 picks up methods by the `@Test` annotation,
not by name. So `void octalLiteral()` is preferred over
`void testOctalLiteral()`. When adding a method to an existing test
class that uses the older prefixed style, matching the surrounding
file is the better fit.

Static helpers worth knowing:

- `groovy.test.GroovyAssert.shouldFail(...)` — asserts a closure throws.
- `gls.CompilableTestSupport` — base class used by spec tests when a
  test needs to assert a snippet compiles.

### Regression tests for JIRA fixes

Every bug fix that has a JIRA needs a test that fails on `master`
before the fix and passes after. There are two shapes:

**Standalone class, when the bug doesn't fit naturally with existing
tests:**

- Class name: `Groovy<NNNN>` for newer tests
  (e.g. `src/test/groovy/bugs/Groovy11955.groovy`). Older tests in
  the tree end in `Bug` or `Test`; new tests use the unsuffixed form.
- Follow-on tests on the same JIRA: append `pt2`, `pt3`
  (e.g. `Groovy10122pt2.groovy`).
- Location: `src/test/groovy/bugs/` for general bugs;
  `src/test/groovy/<package-mirror>/` when the bug is scoped to a
  specific area (the existing `org/codehaus/groovy/tools/stubgenerator/`
  directory shows the pattern).
- Subproject bugs go under that subproject's `src/test/`.

**Method on an existing class, when the regression fits with similar
tests already there:**

- Add a `@Test` method on the appropriate existing class.
- Place a `// GROOVY-<NNNN>` comment on the line immediately above
  the method so a search for the JIRA still finds it.
- The surrounding file's naming style applies — if the existing class
  uses `testFoo` style, follow that for the new method; otherwise
  use the unprefixed style.

In both cases, the test should fail on `master` before the fix is
applied — that's the proof it actually reproduces the bug.

To find precedent for a similar past fix:

```
git log --grep='GROOVY-12345'                                 # commits referencing the JIRA
git log --grep='GROOVY-' -- src/test/groovy/bugs/             # all bug-fix commits in core regression tests
```

### Fix workflow

For a JIRA-tracked bug, the order of operations is **test first,
then fix**:

1. **Write the failing regression test** (per the shapes above).
2. **Confirm it fails on `master`** — run the targeted test and
   watch the failure. This is the proof the test exercises the bug.
3. **Implement the smallest fix that makes the test pass.** Trace
   the cause up the call stack; a null-guard at the failure site is
   often the symptom, not the cause. Don't add speculative
   abstractions, configuration knobs, or "while I'm here" refactors.
   See [What *not* to do](AGENTS.md) for the longer list of
   surrounding-cleanup pitfalls.
4. **Run the targeted test again.** Green.
5. **Run the surrounding module's test pass** —
   `./gradlew :<subproject>:test` or `./gradlew :test` with a
   package filter — to catch nearby behaviour the fix regressed.
6. **Diff the working tree.** Anything outside the regression test
   and the production change needs a reason. Drive-by reformatting
   and stray imports should be reverted; they hide real changes in
   review.
7. **Commit with a JIRA reference.** `GROOVY-NNNNN: <short subject>`
   on the first line — see
   [Submitting a pull request](#submitting-a-pull-request).

A few specific traps:

- **"Local IDE green" is not "build green".** An IDE test runner
  bypasses Gradle's `Test` task configuration, including the
  `junit.network` exclusion that gates `groovy/grape/` tests. The
  signal that counts is `./gradlew :test --tests <FQN>`, not the
  IDE play button.
- **Targeted green is necessary but not sufficient.** The
  surrounding module's test pass catches the nearby regression the
  fix introduced. Skipping that step is the most common source of
  "my fix broke CI."
- **Sometimes the report describes intended behaviour.** If your
  would-be regression test confirms documented behaviour rather
  than contradicting it, the fix is a doc clarification or a
  recommendation to close as Not A Bug — not a code change.
- **Cross-repo fixes need committer coordination.** Some fixes
  touch `groovy-website`, `groovy-eclipse`, or another ASF repo.
  Those have their own conventions and reviewers; flag the
  cross-repo need rather than auto-cloning and patching.

### Executable AsciiDoc examples

Examples in the user-facing documentation under `src/spec/doc/` and
`subprojects/<module>/src/spec/doc/` are not pasted snippets — they
are `include::`'d from real test files under a matching `src/spec/test/`
directory, so every example compiles and runs as part of the build.

The pattern in three pieces:

1. **AsciiDoc include** in `src/spec/doc/<topic>.adoc`:

   ```asciidoc
   include::../test/<TopicTest>.groovy[tags=octal_literal_example,indent=0]
   ```

2. **Tagged region** in `src/spec/test/<TopicTest>.groovy`:

   ```groovy
   @Test
   void octalLiteral() {
       // tag::octal_literal_example[]
       int xInt = 077
       assert xInt == 63
       // end::octal_literal_example[]
   }
   ```

   (Many existing spec tests still use the older `testFoo` method-name
   style; match the surrounding file when adding to one of those.)

3. **Tag name** matches between the two. The test class extends
   `CompilableTestSupport` (or any JUnit 5 test class) and runs as
   part of the normal test task.

A change to a documented example normally touches *both* files in
the same PR.

**Common pitfalls:**

- **Mismatched tag and include.** The tag name in the AsciiDoc
  `include::../test/X.groovy[tags=foo,...]` must match
  `// tag::foo[]` / `// end::foo[]` in the Groovy test file.
  Renaming one without the other silently breaks the include and
  the build doesn't fail loudly.
- **Editing only one side.** Documentation examples are
  dual-edited: `src/spec/doc/<topic>.adoc` and
  `src/spec/test/<TopicTest>.groovy` change together in the same
  PR.
- **Orphaned tagged regions.** A `// tag::...[] ... // end::...[]`
  block in `src/spec/test/` that no AsciiDoc file `include::`'s
  is dead weight. If you removed the include, remove the tagged
  region too.

### Tests-preview

[`subprojects/tests-preview/`](subprojects/tests-preview/) is for
tests that depend on a JDK preview feature. Anything that needs
`--enable-preview` to compile or run goes there, not in core
`src/test/`.

### Test-writing pitfalls

A small set of recurring traps that look like flakiness or
platform issues but are actually project-specific:

- **`String.valueOf(object)` bypasses Groovy `MetaClass` dispatch.**
  Using `String.valueOf(map)` produces Java's `{k=v}` rendering;
  Groovy's `map.toString()` produces `[k:v]`. Tests that assert on
  collection-stringification need `object.toString()` to pick up
  the Groovy extensions. (`null.toString()` returns `'null'` in
  Groovy, so no separate null guard is needed.)
- **Locale-, line-ending-, and path-portability traps.** JVM
  defaults for locale, timezone, line endings, file path
  separators, and charset vary across CI agents and contributor
  machines. Two specific traps that recur:
  - **Path strings in a parsed command line.** A Windows-native
    `Path.toString()` like `C:\Users\…\foo.json` interpolated
    into a `system.execute("cmd ${file}")`-style invocation gets
    its backslashes eaten by JLine's `DefaultParser`, which
    treats `\` as an escape character. Forward-slash the path
    before interpolating: `path.toString().replace('\\', '/')`.
    Java NIO accepts forward-slash paths on Windows.
  - **Output captured from `println`.** `PrintStream.println`
    uses `System.lineSeparator()`, which is `\r\n` on Windows.
    Line-aware assertions (`output.split('\n')`,
    `output.contains('foo\n')`) silently fail on Windows. Use
    Groovy's `String.normalize()` extension to collapse platform
    line separators to `\n` before splitting or comparing.

  Other defences: `Locale.ROOT` for date/number formatting,
  explicit `StandardCharsets.UTF_8` rather than the platform
  default, or assert on parsed values rather than their
  stringified forms.
- **`-Djunit.network=true` is required for tests under
  `groovy/grape/`.** The `Test` task in
  `org.apache.groovy-tested.gradle` applies an
  `exclude buildExcludeFilter(...)` filter that drops anything
  under `groovy/grape/` from execution unless `junit.network` is
  set. Without it, `:groovy-grape-*:test --tests <FQN>` reports
  `BUILD SUCCESSFUL` but no test results appear (and
  `--rerun-tasks` reports `NO-SOURCE`). The test classes compile
  normally; they just aren't run. Always pass
  `-Djunit.network=true` when iterating on tests in
  `subprojects/groovy-grape-*`.
- **`-Djunit.network` on the Gradle CLI doesn't reach the test
  JVM automatically.** Separate trap: a test that reads the
  property at runtime — gated via
  `@EnabledIfSystemProperty(named = 'junit.network', matches = 'true')`
  — needs the subproject's `build.gradle` to forward it
  explicitly:

  ```groovy
  tasks.named('test') {
      def network = System.getProperty('junit.network')
      if (network) systemProperty 'junit.network', network
  }
  ```

  Without forwarding, the gated test always skips even with
  `-Djunit.network=true` on the Gradle CLI.

- **Treacherous substring matching in verification logic.** When
  scripting verification (e.g. checking whether some token
  survived a transformation), plain `.contains()` can silently
  produce false positives near common prefixes —
  `output.contains('xmlns:xs')` matches `xmlns:xsi` as a prefix.
  Prefer anchored regex (`output =~ /xmlns:xs="/`) or parsed-tree
  inspection (`new XmlSlurper().parseText(output)`) over substring
  matching. The trap also bites verification logic written for
  reassessments and triage probes, not just tests; the principle
  applies anywhere you're matching tokens with shared prefixes
  (`xs` / `xsi`, `groovy` / `groovy-`).

### For agents working on tests

The [`.agents/skills/groovy-tests/SKILL.md`](.agents/skills/groovy-tests/SKILL.md)
skill captures the recurring failure modes when adding or modifying
tests in this repo, and the procedure for landing a regression test
or a documented example cleanly.

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
repository; see the [project contribute page](https://groovy.apache.org/)
for how to contribute there.

## Working with JIRA

Bugs, feature requests, and improvements for Groovy are tracked at
<https://issues.apache.org/jira/browse/GROOVY>. The live JIRA project
is the canonical record of "what's planned, in progress, and done"
(see [`GOVERNANCE.md`](GOVERNANCE.md) for how JIRA fits into the
project's broader governance).

This section covers the contributor-facing conventions: states,
fields, components, JQL searches, and how JIRA links to commits and
pull requests.

### Issue states

The GROOVY project uses the standard ASF JIRA workflow:

- **Open** — filed, unassigned, not yet started.
- **In Progress** — a contributor is actively working on it (typically self-assigned).
- **Resolved** — fix has landed; awaiting verification or release.
- **Closed** — terminal state, with a `Resolution` (Fixed / Won't Fix / Duplicate / Cannot Reproduce / Incomplete / Not A Bug / Done).
- **Reopened** — a previously-resolved issue that came back; treat like Open, but read the prior resolution comment first.

State transitions are a committer responsibility. Contributors
comment on issues with findings, recommendations, and reproductions;
committers move issues through the workflow as code lands. The rule
of thumb: **comment, don't transition.**

### Fields and who sets them

| Field | Set by | Notes |
|---|---|---|
| `Summary` | Reporter; committer may tidy | A sharper rewording is fine as a comment. |
| `Description` | Reporter | Don't rewrite someone else's report. |
| `Issue Type` (Bug / Improvement / New Feature / Task / Sub-task) | Reporter; committer corrects | Flag a misclassification (e.g. a "Bug" that's really an "Improvement") as a comment. |
| `Priority` | Committer / project | Don't infer from the reporter's tone ("URGENT!!!"); leave unless asked. |
| `Component/s` | Reporter or committer | Suggest from the package or subproject the bug touches (see below). |
| `Affects Version/s` | Reporter | The version the bug was hit on. If empty, ask the reporter; don't guess. |
| `Fix Version/s` | Committer, on resolve | Set when an issue is being resolved, normally to the next release that will contain the fix. |
| `Resolution` | Committer, on resolve | Filled in when an issue is resolved; an open issue with a resolution is malformed. |
| `Assignee` | Self-assigning contributor or committer | Don't assign on someone else's behalf. |
| `Labels` | Anyone (low ceremony) | Match existing labels; don't invent new ones. |
| Workflow state | Committer | See above — comment, don't transition. |

### Components

The authoritative component list lives at the
[GROOVY project's components page](https://issues.apache.org/jira/projects/GROOVY?selectedItem=com.atlassian.jira.jira-projects-plugin:components-page)
and evolves over time, so refetch rather than relying on memory.

When suggesting (or filing against) a component:

1. Identify the package or subproject the bug reaches. The top
   non-JDK frame of a stack trace, or the file that needs to change,
   usually points at it.
2. Map that area to a component using the live list. Component names
   typically follow the area-of-the-codebase shape (parser, compiler,
   type checker, AST transforms, runtime/MOP, a specific subproject
   under `subprojects/`, build, docs).
3. If multiple components fit, suggest the *narrower* one —
   `Static Type Checker` over `Compiler` when the issue is
   specifically STC.
4. If none fit, propose the closest match and note the mismatch in a
   comment — a recurring miss is a signal the component list needs a
   new entry, which is a project-admin action.

When *reading* a `Component/s` value already set, treat it as a
routing hint, not a constraint. A bug filed against `Parser` that
turns out to be a runtime issue gets re-suggested in a comment, not
silently re-tagged.

### Searching with JQL

JIRA Query Language (JQL) is the search language used throughout the
Atlassian JIRA UI and REST API; Atlassian's
[JQL reference](https://support.atlassian.com/jira-service-management-cloud/docs/jql-fields/)
covers the syntax. A few project conventions that prevent common
pitfalls:

- **Lead every query with `project = GROOVY`.** Apache's JIRA hosts
  many projects; an unscoped query returns cross-project noise.
- **Quote multi-word values:** `component = "Static Type Checker"`
  is correct; `component = Static Type Checker` is a syntax error.
- **Multi-value fields use `in`, not `=` with parentheses:**
  `component in ("Parser", "Compiler")` is correct;
  `component = ("Parser", "Compiler")` is not.

Recurring search templates (substitute the bracketed placeholders):

**Stale open issues (no activity in N days):**

```jql
project = GROOVY AND statusCategory != Done AND updated < -<N>d ORDER BY updated ASC
```

**Open issues in a component:**

```jql
project = GROOVY AND statusCategory != Done AND component = "<Component>" ORDER BY priority DESC, created DESC
```

**Open issues without a component (triage candidates):**

```jql
project = GROOVY AND statusCategory != Done AND component is EMPTY ORDER BY created DESC
```

**Open issues against an affected version:**

```jql
project = GROOVY AND statusCategory != Done AND affectedVersion = "<X.Y.Z>" ORDER BY priority DESC
```

**Resolved issues for a release (changelog mining):**

```jql
project = GROOVY AND fixVersion = "<X.Y.Z>" AND resolution = Fixed ORDER BY resolved DESC
```

**Recently opened (last N days), needs first-pass triage:**

```jql
project = GROOVY AND created > -<N>d ORDER BY created DESC
```

**Duplicate hunting by error string:**

```jql
project = GROOVY AND text ~ "<distinctive phrase>"
```

`text ~` searches summary, description, and comments. A distinctive
phrase from a stack trace or error message works well; a common word
like `NullPointerException` alone returns too many hits.

**By reporter (for context on a reporter's prior issues):**

```jql
project = GROOVY AND reporter = "<asf-id>" ORDER BY created DESC
```

**Closed without a resolution (malformed state, surface to a committer):**

```jql
project = GROOVY AND statusCategory = Done AND resolution is EMPTY
```

**Sub-tasks of an epic / parent:**

```jql
project = GROOVY AND parent = GROOVY-<NNNN>
```

#### Pool-selection heuristics for re-triage sweeps

When picking a pool of issues to work through (whether by hand or
in a tool-assisted sweep), the pool's *status mix* shapes what
you'll find:

- **`status = Reopened`** — small pool, over-represents
  *feature-requests-disguised-as-bugs*: issues someone re-examined
  and left open while pondering a spec change. Useful when the
  goal is *"what spec debates is the project sitting on?"*.
- **`status = Open AND affectedVersion in ("<EOL-versions>")`** —
  larger pool, over-represents *silent fixes*, *real open bugs*,
  and *partial fixes*. Useful when the goal is *"find what's been
  silently resolved by later refactoring."* Target affected
  versions that pre-date a known major refactor of the relevant
  subsystem.

The two pools answer different questions; choose deliberately
rather than by default-sort. See *Nature analysis* under
[Triaging issues and pull requests](#triaging-issues-and-pull-requests)
for what the populations look like in practice.

### Linking JIRA to commits and pull requests

Every commit or pull request that fixes a JIRA-tracked issue
references it using the full key, `GROOVY-NNNNN`, at the start of
the commit subject. JIRA picks up the link via smart-commit handling;
`git log --grep='GROOVY-NNNNN'` finds the change later. Variants
like `[GROOVY-NNNNN]`, lowercase `groovy-`, or omitting the number
break that search.

- **From a commit subject:** `GROOVY-NNNNN: <short summary>` as the
  first line.
- **From a comment on issue A about issue B:** plain text
  `GROOVY-NNNNN` is enough — JIRA auto-links it. Use this for
  "see also" references; reserve the formal "Linked Issues"
  relationship (is duplicated by / blocks / relates to / etc.) for
  committers, since those are project-level metadata.
- **From a branch name:** `GROOVY-NNNNN-<short-slug>` is a common
  shape; not enforced, but it keeps the linkage visible in
  `git branch`.
- **Cross-references in comments:** if you cite another `GROOVY-NNNNN`
  in a comment, include enough context that the cross-reference still
  makes sense if the linked issue is later edited.

### For agents working on JIRA

The [`.agents/skills/groovy-jira/SKILL.md`](.agents/skills/groovy-jira/SKILL.md)
skill operationalises this section for AI tooling: when to load it,
how to draft a JIRA comment without overstepping, and the hand-back
contract for any output that would post to JIRA. The conventions
here are the canonical source; the skill cites them.

## Triaging issues and pull requests

Triage is the project's first-pass response to incoming bug reports
and pull requests: reproducing what was reported, finding duplicates,
suggesting next steps. Anyone with access to the
[JIRA project](https://issues.apache.org/jira/browse/GROOVY) and the
GitHub repository can help triage; committers act on the
recommendations that come out of it.

Triage output is **advisory**. It lands as a JIRA comment or a PR
review for a committer to decide on. Triage does not resolve a JIRA,
set a fix version, close anything, or merge a PR.

### Triaging a JIRA issue

For each issue:

1. **Read the full thread.** Description, every comment, every
   attachment. Note the reported Groovy version, JDK, and OS. If the
   reporter included a stack trace, the top non-JDK frame usually
   points at the area of the code involved.

   Also scan for **historical baselines**: prior committer comments
   that say "I just ran this on version X, here's what I got." These
   are checkpoints the current triage can compare against — the
   headline finding for an old issue may be "state unchanged since
   the 2013 committer baseline" rather than the current state alone.

2. **Search for duplicates and same-family related work** before
   writing a long analysis. A one-line "duplicate of GROOVY-XXXX,
   fixed in 4.0.Y" beats a 500-word root-cause summary of a known
   bug. A "GROOVY-YYYY is in the same neighbourhood" pointer helps
   the committer decide on batch-or-sequential treatment.

   ```
   git log --grep='GROOVY-<NNNN>'                # commits referencing the JIRA
   git log --grep='<distinctive phrase>' -- src/ # prior edits in the area
   ```

   Plus a JQL text search — see the
   [Duplicate hunting by error string](#searching-with-jql) recipe
   above. The same JQL with a topic-keyword surfaces same-family
   open issues: `project = GROOVY AND text ~ "<topic>"`.

3. **Attempt reproduction on `master`.** Drop the reporter's script
   into a temp file and run it against a local build, or paste their
   snippet into the relevant test class as a `@Test` and run
   targeted (`./gradlew :test --tests <FQN>`). Record the `master`
   revision (`git rev-parse --short HEAD`), the JDK
   (`java -version`), the command, and the outcome. A reproducer
   that's now passing on `master` is meaningful information; a
   missing reproduction is speculation.

   When the issue thread contains **multiple distinct reproducers**
   (the description plus a follow-up comment that demonstrates a
   different symptom of the same root cause), run each. They may
   have different fates: one silently fixed, another still broken
   — that's a *split-candidate* signal (see step 7 below).

   When the operator or expression under test spans **multiple
   backing types** (range/index on `List` / `Object[]` / `int[]` /
   `String`; GPath on Map / JSON / XML / POGO / POJO) or has
   **multiple operator variants** (safe-navigation `?.` / `??.` /
   `?[..]`), probing the same expression across the family is
   often cheap and surfaces signal the reporter missed. See the
   family taxonomies in [`ARCHITECTURE.md`'s "Operator families"
   section](ARCHITECTURE.md#operator-families).

4. **Locate the code, lightly.** If the failure reaches the
   runtime/compiler, identify the package or class the stack flows
   through — that's the "where" to point a fix at. Don't go deeper
   than the area unless asked.

5. **Check JIRA fields.** Note (don't edit) what's missing or
   wrong; field-ownership rules and `Component/s` suggestion
   guidance are above under
   [Fields and who sets them](#fields-and-who-sets-them) and
   [Components](#components).

6. **Search for documented workarounds.** Before recommending a
   closure path, check three places:

   - `src/spec/doc/` (user-facing docs) — `grep -rn '<topic>' src/spec/doc/`
   - Groovydoc on the relevant source classes — `grep -B2 -A8` on
     the source file
   - JIRA comments — keywords like `workaround`, `prefix`,
     `coerce`, `use ... instead`

   The outcome shapes the recommended close path:

   - **Workaround documented user-facing** → close as "Not A Bug"
     or "Won't Fix"; cite the doc.
   - **Workaround exists but is undocumented user-facing** → close
     as "Not A Bug **+ add docs**". The documentation deliverable
     is the actionable artefact; closing without it leaves the
     surprise intact for the next user.
   - **No workaround** → keep open OR re-type as Improvement (per
     nature analysis, below).

7. **Consider split candidacy.** When the issue bundles **multiple
   reproducers with mixed fates** (some silently fixed, some still
   broken) or **multiple user-visible symptoms with independently-
   fixable causes**, recommend a split: close the original with a
   per-case summary, suggest a focused new JIRA for the remaining
   unfixed case(s) with the targeted reproducer. Old multi-case
   JIRAs often resolve partially over time; the constructive close
   path is a per-case status update plus carry-over.

8. **Draft a comment** with: the state of the reproduction
   (passed / failed / could not run, with revision + JDK), the
   duplicate-search result, the likely area of the code, suggested
   missing fields, the workaround-search outcome, and a recommended
   next action ("needs a minimal reproducer," "looks fixed on
   master — propose closing as Cannot Reproduce after a second pair
   of eyes," "appears intended-behaviour-but-undocumented; propose
   closing + docs PR," "appears to need a fix in `<area>`,"
   "consider splitting — A is fixed, B remains"). Factual, helpful,
   specific.

9. **Don't transition the issue.** Even when the recommendation is
   clear, leave the workflow state to a committer.

### Triaging a pull request

For each PR:

1. **Read the PR description and the linked JIRA (if any).** A
   bug-fix PR without a `GROOVY-NNNNN` reference is the first thing
   to flag; a docs / build-housekeeping PR without one is fine.

2. **Scan the diff for shape, not just substance:**
   - **New files** — every `.java`, `.groovy`, `.gradle`, `.xml`
     must carry the ASF license header. Missing header → flag.
   - **Drive-by reformatting** — large whitespace-only hunks,
     end-of-line changes, or reordered imports outside the touched
     method signal a scope violation (see
     [What *not* to do in AGENTS.md](AGENTS.md)).
   - **Hallucinated identifiers** — API methods or flags that
     `git grep` doesn't find in the codebase. Search before
     assuming a name is real.
   - **Scope creep** — does the diff match what the description and
     the JIRA say it should do? Surrounding refactors get called
     out.

3. **Check for the regression test.** If the PR claims to fix a
   JIRA-tracked bug,
   [Regression tests for JIRA fixes](#regression-tests-for-jira-fixes)
   calls for an accompanying test. Confirm the test exists, follows
   the project's naming, and would actually fail without the
   production change (revert just the production hunk, run the
   test, expect failure).

4. **Look at CI.** Identify the *first* failing job and the
   *first* failing test in that job; quote them. Don't say "CI red"
   without specifics. Note known-flaky jobs (joint-validation, JMH)
   separately from core test failures.

5. **Note ICLA / first-time-contributor signal.** First-time
   external contributors need an ICLA on file for non-trivial
   changes. Don't assert ICLA status — the bot or a committer
   confirms it — but flag "first-time contributor, ICLA status
   unknown" so the committer can check.

6. **Draft the review.** Order findings by what would block merge
   first: license-header / scope / test, then style / drive-by
   reformat, then nits. Use file-path:line references so committers
   can jump to each finding.

### Nature analysis: bug-as-advertised vs wouldn't-it-be-nice

Before recommending a close path, ask: **is this not operating as
advertised, or is this 'wouldn't it be nice if'?** The answer
shapes the action differently from what the reproducer's outcome
alone suggests. Two issues can both reproduce verbatim and need
totally different closures.

| Nature | Meaning | Recommended action |
|---|---|---|
| **bug-as-advertised** | A documented or implicit promise isn't being kept. The code does not deliver what its signature, Groovydoc, or spec says. | Fix it. The reproducer is the regression-test target. |
| **bug-as-advertised, partial fix** | Originally multi-case; some cases silently fixed, others still broken. | Split — close original with per-case summary, open focused new JIRA for the remaining case(s). See "Consider split candidacy" in the procedure above. |
| **feature-request** | The reporter wants a different spec; the behaviour matches what's promised. JIRA is correctly typed as Improvement. | Re-typing not needed. Decide on `dev@` whether to accept the Improvement. |
| **feature-request-disguised-as-bug** | Same as above but mis-typed as Bug in JIRA — the reporter framed an unmet wish as a defect. | Recommend re-typing Bug → Improvement, then design discussion on `dev@`. |
| **intended-and-documented** | Behaviour is correct *and* the docs clearly cover it. | Close as Not A Bug. The issue is the reporter not having found the docs; consider whether a docs cross-link or better discoverability would help. |

Two pool-level observations are useful here:

- **The Reopened pool over-represents the feature-request shapes.**
  An issue that's been reopened is one someone re-examined and
  consciously left open while pondering a spec change.
- **The Open + EOL-affected-version pool over-represents the
  bug-as-advertised shapes.** Issues nobody looked at while the
  runtime/compiler under them was rewritten are more likely to be
  silent fixes or genuine open bugs than wishlists.

The distinction matters when choosing which JIRAs to work through
in a re-triage pass: pick the pool that matches what you're
hunting for.

### Drafting a useful comment or review

Whether triaging an issue or a PR, the output is:

- **Grounded.** Each non-trivial claim cites either a command
  output, a `git grep` hit, or a JIRA / file reference. Speculation
  reads as filler.
- **Helpful and specific.** The default tone is collaborative.
  Don't dismiss a report as "works for me" without showing what you
  ran.
- **Phrased as recommendations, not directives.** "Looks fixed on
  master at `<rev>` — may be a candidate for closing as Cannot
  Reproduce" is a recommendation; "Closing this as Cannot
  Reproduce" is a transition. Triage recommends; committers
  transition.
- **Free of security-sensitive content** in public comments.
  Vulnerabilities go to <security@groovy.apache.org>, not into a
  JIRA comment or a PR review.

### For agents helping with triage

The [`.agents/skills/groovy-triage/SKILL.md`](.agents/skills/groovy-triage/SKILL.md)
skill operationalises this section for AI tooling — the AI-specific
guardrails on top of the methodology described above, plus the
hand-back contract that keeps any posting to JIRA or PR review in
human hands.

## Submitting a pull request

1. Fork <https://github.com/apache/groovy> and create a feature branch.
2. Reference the JIRA issue in commits, for example
   `GROOVY-12345: short description`.
3. Keep commits focused. A bug fix, a refactor, and a formatting pass
   are three separate commits (or pull requests), not one.
4. Run `./gradlew test` locally and confirm it passes.
5. Open a pull request against `master`.

If the change adds or alters public API, read
[`COMPATIBILITY.md`](COMPATIBILITY.md) before opening the PR — API
additions and breaking changes have a stricter review path, with
the dev@ discussion described in
[`GOVERNANCE.md`](GOVERNANCE.md).

GitHub's [fork a repo](https://docs.github.com/en/get-started/quickstart/fork-a-repo)
and [creating a pull request](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request)
guides cover the generic git mechanics.

## Using AI tooling

Contributors using AI coding assistants (Claude Code, Codex, Cursor, Copilot,
Gemini, Aider, and similar) should read [AGENTS.md](AGENTS.md) for
project-specific guidance, and in particular follow the ASF's
[Generative Tooling guidance](https://www.apache.org/legal/generative-tooling.html).
If AI tooling assisted on a change, consider adding an
`Assisted-by: <tool name and version>` trailer to the commit message;
`AGENTS.md` covers when each of `Assisted-by:`, `Co-authored-by:`, and
`Generated-by:` is appropriate. The contributor remains responsible for
the licensing, correctness, and style of everything they submit.
