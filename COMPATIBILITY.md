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

# Apache Groovy — Compatibility and Stability

Public API in Groovy is covenanted: applications and libraries depend
on it across releases, and removing or breaking it has costs we don't
always see locally. This document is the contributor-facing reference
for what is public, what is internal, what counts as a breaking
change, and how the build helps us notice when one slips through.

If you're orienting in the codebase generally, start with
[`ARCHITECTURE.md`](ARCHITECTURE.md). For build and submission
mechanics, see [`CONTRIBUTING.md`](CONTRIBUTING.md). The user-facing
version-numbering scheme (SemVer since 2.0.0) is at
[`src/spec/doc/version-scheme.adoc`](src/spec/doc/version-scheme.adoc).

## Stability tiers

Stability is signalled by package, by annotation, and occasionally by
documented convention. The four tiers from most to least stable:

| Tier | How it's marked | Stability commitment |
|---|---|---|
| **Public API** | Lives in `groovy.*`, or in `org.apache.groovy.*` / `org.codehaus.groovy.*` without an internal marker | Source- and binary-compatible across minor and patch releases. Breaking changes need a major version. |
| **Incubating** | Annotated [`@org.apache.groovy.lang.annotation.Incubating`](src/main/java/org/apache/groovy/lang/annotation/Incubating.java), or noted as "incubating" in the feature's prose documentation | Reduced stability guarantee — design may still settle, so users opting in accept some risk of change in a minor release. See "Incubating features" below for what this means in practice. |
| **Internal** | Annotated [`@groovy.transform.Internal`](src/main/java/groovy/transform/Internal.java), or in a package whose name contains `internal` | No stability guarantee. Treat as implementation detail, even if technically reachable. |
| **Generated** | Anything emitted by the build into `build/generated/...` or repackaged via the `groovyjarjar*` namespace | Not API at all; avoid referring to these from anywhere stable. |

`org.codehaus.groovy.*` is a historical complication: most of it is
internal-by-intent but treated as public-by-practice because users
have come to depend on it. The safe assumption is that it's public
unless explicitly marked `@Internal` or living in an `internal`
sub-package.

The `internal` package convention is the one the build actually
enforces — see "The binary-compatibility check" below. Existing
examples include `org.apache.groovy.internal.util.*`,
`org.apache.groovy.internal.metaclass.*`, and
`org.apache.groovy.parser.antlr4.internal.*`.

## Incubating features

`@Incubating` reduces the formal stability guarantee — it tells users
this surface may still change as the design settles — but project
practice is more conservative than the annotation's Javadoc wording
suggests. We don't break incubating APIs gratuitously: when a feature
has settled into the shape it wants and nothing else forces a change,
it stays as-is. `@Incubating` is the permission slip we use only when
the design genuinely has open questions, not a licence for churn.

Some features can't carry the annotation because they aren't expressed
as a class or method — a grammar tweak, a few lines added to an
existing visitor, a behavioural adjustment scattered across the
runtime. In those cases the incubating signal moves to the prose: the
feature's section under `src/spec/doc/` (or the relevant subproject's
`src/spec/doc/`) says explicitly that the feature is incubating.
The reduced guarantee and the project's posture of caution before
changing them are the same as for an `@Incubating` annotation.

A new feature that should be incubating but can't carry the
annotation gets the prose marker in the documentation in the same
PR as the code change. When the feature graduates, the marker comes
out of both places (annotation and prose) at the same time.

## What counts as a breaking change

For anything in the **Public API** tier, all of the following are
breaking:

- Removing a class, interface, method, field, or annotation type.
- Changing a method's name, return type, parameter list, or thrown
  checked exceptions.
- Narrowing visibility (public → protected, protected → package-private).
- Adding a method to an interface or `abstract` method to a class
  without a default implementation.
- Renaming or moving a package.
- Changing a class's superclass or removing an implemented interface.
- Removing or renaming an enum constant.
- Renaming a service-file key, or removing a previously-published
  service implementation (see "Service files" below).

These are also breaking, even though `japicmp` may not catch them:

- Behavioural changes that users have come to rely on, including
  MetaClass dispatch, method-resolution order, GDK method semantics,
  and serialization formats.
- Changing the bytecode shape that `@CompileStatic` produces in a way
  that breaks reflection-based callers.
- Changing the AST shape produced for a given source construct in a
  way that breaks third-party AST transformations.
- Changing the order or timing of compilation phases that user
  customizers attach to.
- Breaking Java interoperability — including changes that prevent
  Java code from calling compiled Groovy classes, that prevent Groovy
  code from calling existing Java libraries, that change
  joint-compilation semantics, or that change the shape of generated
  Java stubs. Seamless Java interop is a covenant of Groovy, not just
  a feature.

The bar for a breaking change is *discussion on the dev list and a
major version*, not a single PR. If a change might be breaking, the
safe default is to assume it is and ask on the list.

## Adding new public API

New API is the easier-to-fix mistake — you can deprecate and remove —
but it's still costly. Before adding any:

1. **Justify the surface.** Is this something one or many users have
   asked for, or is it convenience for a single internal caller? If
   the latter, make it package-private or `@Internal`.
2. **Pick the narrowest visibility that works.** Default to
   package-private; widen only when the cross-package or cross-module
   need is real.
3. **Place it in `org.apache.groovy.*` for new code.** Use
   `org.codehaus.groovy.*` only when the new symbol must integrate
   with existing internals there.
4. **Consider marking the feature as incubating** if the design is
   still settling. Annotate API surfaces with `@Incubating`; for
   features that aren't expressed as a class or method (grammar
   tweaks, cross-cutting behaviour), say "incubating" in the prose
   documentation instead. Either form buys room to refine in a minor
   release. See "Incubating features" above.
5. **Document it.** Public types need accurate Groovydoc/Javadoc; if
   the feature is user-facing, also add or update an AsciiDoc section
   under `src/spec/doc/` or the relevant subproject's `src/spec/doc/`.
6. **Add tests.** Both unit tests and, where appropriate, an
   executable spec example under `src/spec/test/`.

## Deprecation policy

When you need to remove or replace public API:

- Mark the old symbol `@Deprecated(since = "X.Y.Z", forRemoval = true)`
  when removal is planned, or `@Deprecated(since = "X.Y.Z")` when
  retirement is open-ended.
- In the Javadoc, name the replacement explicitly with `@deprecated`
  and a one-line pointer:

  ```java
  /**
   * @deprecated since 5.1.0, use {@link #newMethod(String)} instead.
   */
  ```

- Keep the deprecated symbol working — same semantics, no behaviour
  drift — until it is removed.
- Remove deprecated symbols only in a major release, and only after
  a deprecation has shipped in at least one minor release first.
- If a behavioural change is unavoidable in the same release as the
  deprecation (rare), call it out in the release notes.

## The binary-compatibility check

The [`subprojects/binary-compatibility/`](subprojects/binary-compatibility)
module wires
[`japicmp`](https://github.com/siom79/japicmp) into the build. For
each library subproject, it compares the current `jarjar` output
against the published artefact of a baseline version and produces an
HTML report.

| Aspect | Detail |
|---|---|
| Tool | `me.champeau.gradle.japicmp` Gradle plugin |
| Baseline version | `binaryCompatibilityBaseline` Gradle property (`-PbinaryCompatibilityBaseline=5.0.4` or `gradle.properties`) |
| Coverage | All subprojects with `groovyLibrary.checkBinaryCompatibility = true` (the default) |
| Visibility checked | `protected` and above |
| Excluded | Packages matching `**internal**` and `groovyjarjar**`; closure classes and `dgm$*` runtime helpers |
| Where reports land | `subprojects/<name>/build/reports/japicmp<Name>.html` |
| Aggregating task | `./gradlew :binary-compatibility:checkBinaryCompatibility` |

A few things to know about the check:

- **It does not fail the build.** `failOnModification = false` —
  reports are produced, but a CI job has to read them and a human
  has to interpret them. The report is a guidance tool, not a gate.
- **`@Internal` is not what excludes a symbol from the check.**
  Exclusion is by package name (`**internal**`). The annotation is a
  documentation marker that helps Groovydoc, AST tools, and reviewers;
  to genuinely move something out of the binary-compatibility
  surface, an `internal` package is the right home.
- **The baseline is a Gradle property, not pinned in code.** When a
  release is cut, the property is updated to the previous release.
  Local runs default to whatever's in `gradle.properties`.

If `japicmp` flags a change you intended:

1. Confirm the affected symbol was actually public, not internal.
2. Confirm the change is necessary — most "improvements" to public
   API are not.
3. If it stays, raise it on the dev list. The release-management
   conversation is the one that signs off on what version absorbs the
   break.

## Service files and SPI

These are public surface even though they don't show up in `japicmp`:

- `META-INF/services/org.codehaus.groovy.transform.ASTTransformation` —
  global AST transformations.
- `META-INF/groovy/org.codehaus.groovy.runtime.ExtensionModule` —
  GDK-style extension modules adding methods to existing classes.
- Anything published under `META-INF/services/` or `META-INF/groovy/`.

Removing or renaming an entry in these files is as breaking as
removing the corresponding type. New entries get added additively,
the same way as a new public type.

## Cross-references

- [`ARCHITECTURE.md`](ARCHITECTURE.md) — overall layout; the
  package-conventions section there is summarised in this document
  and elaborated with stability semantics here.
- [`CONTRIBUTING.md`](CONTRIBUTING.md) — build, test, submission.
- [`GOVERNANCE.md`](GOVERNANCE.md) — how breaking-change and
  API-addition decisions are taken to dev@; review modes and wait
  periods.
- [`AGENTS.md`](AGENTS.md) — AI-contributor supplement.
- [`src/spec/doc/version-scheme.adoc`](src/spec/doc/version-scheme.adoc)
  — user-facing version-numbering scheme.
- [`subprojects/binary-compatibility/build.gradle`](subprojects/binary-compatibility/build.gradle)
  — exact configuration of the japicmp check.
