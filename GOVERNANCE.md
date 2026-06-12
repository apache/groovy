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

# Apache Groovy — Governance and Decision-Making

> **Status: placeholder draft.** This document captures the parts of
> the project's decision-making that are uncontroversial, and flags
> the rest as open questions for the dev list. Items marked **TBD**
> reflect Groovy practice that hasn't been written down rather than
> policy that hasn't been chosen — they need confirmation, not
> invention. See the "Open questions" section at the end.

This file describes how decisions get made on Apache Groovy: where
discussions happen, what counts as consensus, and which kinds of
changes need a wider audience before they land. It complements
[`CONTRIBUTING.md`](CONTRIBUTING.md) (the *how* of submitting code)
and is referenced from [`AGENTS.md`](AGENTS.md) (where AI-assisted
contributions need to slot into these processes the same as any
other).

## The Apache Way

Apache Groovy operates as an Apache Software Foundation project,
which means we follow the broad pattern the ASF calls *the Apache
Way*: community over code, meritocratic decision-making, public
communication, and consensus-based decisions reached on public
mailing lists. Different ASF projects tune the finer details — voting
thresholds, review modes, release cadence — to suit their size and
culture. This document is Groovy's local interpretation.

The ASF's general guidance is at <https://www.apache.org/foundation/>
and <https://community.apache.org/>. Those pages occasionally evolve;
where this document and the ASF guidance disagree on a point of
foundation-level rule (for example release voting), the ASF rule
wins. Where they disagree on a point of project-local practice, this
document wins.

## Where decisions happen

| Channel | What happens here | Authoritative for |
|---|---|---|
| **dev@groovy.apache.org** mailing list | Design discussions, consensus calls, votes, release announcements | All non-trivial decisions. *"If it didn't happen on the list, it didn't happen."* |
| **JIRA** (<https://issues.apache.org/jira/browse/GROOVY>) | Issue tracking, change proposals, status | The canonical record of what's planned, in progress, and done |
| **GitHub pull requests** | Code review of specific changes | Mechanics of an individual change |
| **Slack** (Apache Groovy channel on the ASF workspace) | Synchronous chat | Nothing binding — ephemeral discussion only |

The split matters: a discussion that reaches a conclusion in Slack or
a PR thread but hasn't surfaced on dev@ is not a project decision.
For anything beyond a localised code change, the list is the place
for it — and a re-summary on the list is the way to capture a
conclusion reached elsewhere.

## When to bring something to the dev list

The kinds of changes worth a dev@ thread before committing:

- New features that add public API surface (see [`COMPATIBILITY.md`](COMPATIBILITY.md)).
- Behavioural changes users may have come to rely on, even if technically not breaking (MetaClass dispatch, GDK semantics, AST shape for third-party transforms).
- Breaking changes of any kind.
- New runtime dependencies.
- Build-system changes that affect contributors broadly (Gradle version, JDK baseline).
- Process or policy changes (including this document).
- Anything that touches the release pipeline, signing, or distribution.

Routine bug fixes, internal refactors that don't cross the public-API
boundary, documentation improvements, and test additions don't need a
dev@ thread before they're committed — though they still go through
PR review.

## Consensus

We operate on **lazy consensus** by default: a proposal is taken as
agreed if no objection is raised within a reasonable time. The
emphasis is on *reasonable*: trivial things move quickly, larger
things give time zones a chance to weigh in. See "Wait periods"
below.

When consensus is unclear or contested, the resolution mechanism is a
**vote** on dev@:

- `+1` — in favour
- `+0` — neutral, no objection
- `-1` — against, with rationale required for code/release votes (a `-1` on a code change is a *veto* and must include a technical justification)

For routine code-change votes, simple majority of `+1` votes from
committers carries. Release votes follow the ASF rules, which require
at least three `+1` votes from PMC members and more `+1` than `-1`.

## Review modes

Two review modes are common across the ASF, and Groovy uses both:

- **Commit-then-review (CTR)** — commit first, expect review afterwards. Used for low-risk changes where the cost of a temporary regression is small and the cost of waiting is high.
- **Review-then-commit (RTC)** — review and approval first, commit afterwards. Used for higher-risk or wider-impact changes where a regression would be expensive or hard to reverse.

**TBD: which change types are CTR vs RTC for Groovy specifically.**
Existing practice (to be confirmed on dev@):

| Change type | Mode (proposed) |
|---|---|
| Documentation typo / obvious doc fix | CTR |
| Test-only additions | CTR |
| Localised bug fix with a regression test | CTR |
| Refactor confined to internal packages | CTR |
| New public API | RTC, after dev@ discussion |
| Breaking change | RTC, after dev@ discussion and consensus |
| Dependency version bump | RTC if it changes major/minor; CTR for security-only patch bumps |
| Build / CI infrastructure change | RTC |
| Anything `-1`'d after a CTR commit | Reverts to RTC; the change comes out and is re-discussed |

Reviewers approving an RTC change should explicitly check the
applicable wait period has elapsed before merging.

## Wait periods

For RTC changes, allow time for committers across time zones to read
and respond before merging. **TBD: precise duration to confirm on
dev@.** Working assumption (typical ASF practice):

- 72 hours for substantial changes (new features, API additions, breaking changes, build infrastructure).
- 24 hours for smaller RTC changes where dev@ has already discussed the proposal.
- No fixed wait for CTR changes — but a `-1` after the fact triggers a revert and a discussion.

The 72-hour figure is widely used at the ASF; whether Groovy adopts
exactly that or differs is a question for dev@.

## Releases

Releases follow ASF release policy
(<https://www.apache.org/legal/release-policy.html>) and require a
formal vote on dev@ with at least three `+1` votes from PMC members.
The mechanics of cutting a release (signing, staging, distribution)
are out of scope for this document. **TBD: link to a Groovy-specific
release-process document if one exists, or note that one should be
written.**

## ASF references and the churn problem

ASF foundation-level documentation occasionally moves or rewords. We
deal with this by:

- Stating our project-local interpretation here, in our own words. This file is authoritative for project-level practice.
- Linking to *topic pages*, not deep URLs (e.g. <https://www.apache.org/foundation/voting.html>, <https://www.apache.org/foundation/how-it-works.html>) which have been more stable.
- Treating ASF foundation-level rules (release voting thresholds, code-of-conduct, security disclosure) as authoritative when they conflict with anything written here. A change at the foundation level supersedes our restatement; this document gets updated to follow.

Useful starting points:

- <https://www.apache.org/foundation/how-it-works.html> — overview of ASF governance
- <https://www.apache.org/foundation/voting.html> — voting mechanics and thresholds
- <https://community.apache.org/committers/decisionMaking.html> — how ASF projects make decisions
- <https://community.apache.org/committers/lazyConsensus.html> — lazy consensus, with examples
- <https://www.apache.org/foundation/policies/conduct.html> — code of conduct
- <https://www.apache.org/legal/release-policy.html> — release requirements

## Open questions

These items need a dev@ thread to settle. Until they do, the values
above are working assumptions, not policy:

1. **Which change types are CTR vs RTC.** The table above is a best-guess summary of how the project already operates; it has not been ratified.
2. **Exact wait period for RTC changes.** 72 hours is the working assumption; a shorter or longer figure may better match Groovy's contributor base.
3. **Whether routine dependency bumps need dev@ discussion** or can be CTR with the existing PR review.
4. **Whether this document supersedes or supplements anything in `CONTRIBUTING.md`** that touches review process — currently `CONTRIBUTING.md` covers only the mechanics of submitting a PR.
5. **Whether to write a separate release-process document** or fold it into this one.

Resolving these turns this from a placeholder into policy. Until
then, when the document is wrong, code wins — and the dev list is
the place to call it out.

## Cross-references

- [`CONTRIBUTING.md`](CONTRIBUTING.md) — submitting a change.
- [`SECURITY.md`](.github/SECURITY.md) — supported versions, private vulnerability reporting, and disclosure hygiene for contributors (binds AI tooling identically).
- [`COMPATIBILITY.md`](COMPATIBILITY.md) — what counts as breaking, and therefore what needs a dev@ discussion before landing.
- [`ARCHITECTURE.md`](ARCHITECTURE.md) — repository layout and compilation pipeline.
- [`AGENTS.md`](AGENTS.md) — AI-contributor supplement; AI-assisted contributions follow the same governance as any other.
