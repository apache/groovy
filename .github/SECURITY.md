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

# Security Policy

## Supported Versions

The latest released version in the Groovy 4.0.x stream of releases
is the currently recommended version of Groovy and requires JDK8 as a minimum.

The latest released version in the Groovy 3.0.x stream of releases
is the currently recommended version of Groovy if you require access
to the legacy parser or legacy packages of classes whose packages
were renamed due to split package remediation ([GROOVY-10542](https://issues.apache.org/jira/browse/GROOVY-10542)).

The latest released version in the Groovy 2.5.x stream is recommended where JDK7 is required.

| Version  | Supported          | Comment                              |
|----------|--------------------|--------------------------------------|
| <= 2.4.x | :x:                |                                      |
| 2.5.x    | :x:                |                                      |
| 3.0.x    | :white_check_mark: | Reduced releases on this branch (\*) |
| 4.0.x    | :white_check_mark: |                                      |
| 5.0.x    | :white_check_mark: |                                      |
| 6.0.x    | :grey_question:    | Alpha releases status (**)           |

(\*) The 3.0.x stream is no longer the focus of the core team,
but we are currently still doing critical security fixes if needed.

(**) While in early stages of pre-release, security fixes are
done on a best-effort basis.

`@Incubating` APIs carry reduced-priority status for security fixes.
We caution users about relying on such features for production systems.
Genuine vulnerabilities are still fixed and disclosed; only their priority is
reduced while the API remains incubating.

## List of Security Vulnerability Fixes

The Groovy website has a list of [Security fixes](https://groovy-lang.org/security.html)
applicable to Groovy 2.4.4 and above (versions released since moving to Apache).

## Threat Model

Before reporting, please consult the project [Threat Model](../THREAT_MODEL.md).
Because Groovy is a general-purpose programming and scripting language,
running the code it is given — including filesystem, network, and process
access — is by design, not a vulnerability. The threat model states what
Groovy treats as a security issue and what it does not (e.g. executing
untrusted scripts/templates or deserializing untrusted data are out of
model), so genuine issues can be told apart from expected language
behaviour. It also guides triage of automated code-scanning results, which
may arrive as a single report bundling many candidate findings: each is
assigned one disposition from the threat model, and only confirmed issues
enter the private reporting flow below.

**Compiling untrusted Groovy is already code execution — not only
running it.** Groovy runs code *during* compilation: global AST
transforms on the compile classpath, static initializers, `@Grab`
dependency resolution, and the `@ASTTest` annotation (whose closure is
evaluated at compile time in a fresh shell). A pipeline that *compiles*
attacker-supplied Groovy for analysis, linting, or type-checking — but
deliberately never runs the output — is therefore **not** a safety
boundary; it has already granted code execution. Treat "compile-only"
as equivalent to "run": keep `@Grab` off for such input
(`-Dgroovy.grape.enable=false`), and note that `SecureASTCustomizer` is
a best-effort grammar filter, not a sandbox, so it does not change this.
See [`THREAT_MODEL.md`](../THREAT_MODEL.md) §3.

## Reporting a Vulnerability

Do **not** open a public JIRA issue, GitHub issue, pull request, or
discussion for a suspected vulnerability — that defeats coordinated
disclosure.

Report it privately by email to **security@apache.org** (the Apache
Software Foundation's central security address). You may also, or
instead, write to the Apache Groovy PMC's private list,
**private@groovy.apache.org**.

Apache Groovy follows the Apache
[general guidelines for handling security vulnerabilities](http://www.apache.org/security/committers.html)
and the [ASF security process](https://www.apache.org/security/).

## Disclosure hygiene for contributors

Until a fix has been publicly announced, do **not** reveal the security
nature of a change anywhere public — commit messages, pull request
titles or bodies, JIRA issues, or review comments — even when the fix
touches security-adjacent code (parsing, deserialization, classloading,
sandboxing). Describe the behaviour change neutrally. A public commit or
PR that advertises "fixes the CVE", "security fix", or "patches the
vulnerability" discloses the issue before it is announced and defeats
the coordinated-disclosure process above.

This applies to every contributor, and identically to any AI tooling
acting on a contributor's behalf.
