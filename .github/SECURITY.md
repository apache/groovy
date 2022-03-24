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

| Version  | Supported          | Comment                                  |
|----------| ------------------ |------------------------------------------|
| <= 2.3.x | :x:                |                                          |
| 2.4.x    | :grey_question:    | Only severe/critical vulnerabilities (*) |
| 2.5.x    | :white_check_mark: | Reduced releases on this branch          |
| 3.0.x    | :white_check_mark: |                                          |
| 4.0.x    | :white_check_mark: |                                          |
| 5.x      | :grey_question:    | Pre-release status (**)                  |

(\*) The 2.4.x stream is no longer the focus of the core team
but critical security fixes or community contributions may lead
to additional releases.

(**) While in early stages of pre-release, security fixes are
done on a best effort basis.

## List of Security Vulnerability Fixes

The Groovy website has a list of [Security fixes](https://groovy-lang.org/security.html)
applicable to Groovy 2.4.4 and above (versions released since moving to Apache).

## Reporting a Vulnerability

Apache Groovy follows the Apache
[general guidelines for handling security vulnerabilities](http://www.apache.org/security/committers.html).
