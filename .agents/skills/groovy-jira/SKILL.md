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
---
name: groovy-jira
description: AI-tooling guardrails for working with JIRA on the GROOVY project — points at the project's JIRA conventions in CONTRIBUTING.md (states, fields, components, JQL recipes, the commit ↔ JIRA link), and adds the AI-specific constraints on top: never propose a workflow transition, never set committer-owned fields, never post comments or open formal issue links autonomously, refetch live state rather than citing trained memory, hand drafts back to a human for review. Use when constructing a JQL query, suggesting a Component, drafting a JIRA comment, or producing any AI output that touches JIRA.
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: ai-tooling-jira-guardrails
---

# Groovy JIRA

This skill is the **AI-tooling layer** over the project's JIRA
conventions. The conventions themselves — states, fields, components,
JQL recipes, the commit ↔ JIRA link — live in
[`CONTRIBUTING.md`'s "Working with JIRA" section](../../../CONTRIBUTING.md#working-with-jira).
This skill cites them and adds the AI-specific guardrails: no
autonomous comments, no proposed transitions, no fabricated field
values, no trained-memory citations of live state. The output is a
draft a human committer reviews and posts.

- [`groovy-triage`](../groovy-triage/SKILL.md) — sister skill that
  drives a full triage pass; pair with this one.
- [`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md) — hand off
  to once a triage finding becomes a fix in flight.
- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — the canonical
  source for every JIRA convention this skill cites; load alongside
  this skill.

## When to use this skill

**Use it for:**

- Drafting JIRA-related output (comments, field suggestions, query
  results) that a human will review before posting.
- Constructing a JQL query for grooming, duplicate hunting,
  release-notes mining, or a triage pass.
- Suggesting a `Component/s` value or interpreting one already set.
- Producing or reading a `GROOVY-NNNNN` reference in a commit, branch
  name, or comment.

**Don't use it for:**

- Posting to JIRA on the project's behalf, transitioning workflow
  state, setting committer-owned fields, or opening formal issue
  links. The hand-back contract (below) is firm.
- Triage workflow itself (reproducing, drafting findings, PR-readiness
  checks) — that's [`groovy-triage`](../groovy-triage/SKILL.md).
- Security-sensitive issues. Suspected vulnerabilities go to
  <security@groovy.apache.org>, not into JIRA or a public comment.
  If a public JIRA appears to disclose a vulnerability, flag
  privately to a committer and stop.
- Workflow administration (creating versions, renaming components,
  editing the workflow itself) — that's a project-admin action.

## Read first

- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) "Working with JIRA"
  — fields, states, components, JQL recipes, commit-reference
  convention. The canonical source for everything below; the skill
  restates only what AI tooling specifically needs.
- [`GOVERNANCE.md`](../../../GOVERNANCE.md) — JIRA's role as the
  project's canonical record.
- Live JIRA project page:
  <https://issues.apache.org/jira/projects/GROOVY> — components,
  versions, and the workflow are configured there, not in this repo.

## Top failure modes

These are the recurring mistakes when AI tooling reaches for JIRA:

1. **Proposing a workflow transition.** State changes are committer
   actions tied to actual code shipping. AI tooling never recommends
   or performs a transition — surface a recommendation in a comment
   instead: "appears fixed on master at `<rev>` — proposing a
   committer close this as Cannot Reproduce after a second look." The
   contributor norm is "comment, don't transition" (see
   [`CONTRIBUTING.md`](../../../CONTRIBUTING.md#working-with-jira));
   for AI tooling it tightens to "never."

2. **Setting or proposing a committer-owned field.** `Fix Version/s`,
   `Resolution`, and `Priority` are committer territory; AI tooling
   does not propose values for them. `Affects Version/s` is the
   reporter's domain; ask if missing, don't fabricate. `Component/s`
   AI may *suggest* — but only from a real area of the codebase, not
   a plausible-sounding name. See the field-ownership table in
   [`CONTRIBUTING.md`](../../../CONTRIBUTING.md#working-with-jira)
   for who sets what.

3. **Fabricating a Component.** AI tooling reaches for
   plausible-sounding component names that aren't in the live list.
   Refetch the
   [components page](https://issues.apache.org/jira/projects/GROOVY?selectedItem=com.atlassian.jira.jira-projects-plugin:components-page)
   rather than guessing. If the bug doesn't map cleanly to an
   existing component, say so in the comment — don't manufacture one.

4. **Posting a comment, link, or assignment without explicit
   instruction.** Even when the draft is clean and the recommendation
   obvious, AI tooling produces drafts; humans post. Formal "Linked
   Issues" relationships are committer territory regardless; AI
   cross-references stay in comment text only.

5. **Quoting a stale JIRA snapshot.** Trained memory and cached
   search results lag the live project. For decisions that hinge on
   current state (open vs. closed, latest comment, current
   `Fix Version/s` on a related issue), refetch — don't cite from
   memory. This bites AI tooling harder than humans, because humans
   typically load JIRA in a browser before commenting.

6. **JQL that drops `project = GROOVY` or mis-quotes values.** The
   contributor rules (lead with `project = GROOVY`, quote multi-word
   values, use `in` for multi-value fields) are in
   [`CONTRIBUTING.md`](../../../CONTRIBUTING.md#working-with-jira) —
   AI output is held to them too. A query that's "right in principle
   but produces cross-project noise" is wrong output.

7. **Bare or malformed JIRA references in drafted output.** Commits,
   comments, and branch names use `GROOVY-NNNNN` — uppercase, no
   brackets, no prefix. Drafts that emit `#12345`, `JIRA-12345`, or
   lowercase `groovy-12345` break the search-by-grep workflow
   contributors and tooling rely on.

## Drafting a JIRA comment

When the task is to produce a JIRA comment a human will review and
post:

1. **Refetch live state for any factual claim.** The issue's current
   status, the latest comments, the `Component/s` and `Fix Version/s`
   fields, related issues — pull from the live JIRA, not from
   training memory or stale cache.
2. **Use plain `GROOVY-NNNNN` for any cross-reference**; let JIRA
   auto-link.
3. **Surface recommendations, not directives.** "Looks fixed on
   master at `<rev>` — may be a candidate for closing as Cannot
   Reproduce" reads as a recommendation; "Closing this as Cannot
   Reproduce" reads as an action. The committer makes the call.
4. **Cite evidence.** A reproduction outcome on `master` cites the
   revision and JDK (see [`groovy-reproducer`](../groovy-reproducer/SKILL.md));
   a duplicate citation names the linked JIRA; a `Component/s`
   suggestion names the package path it derives from.
5. **No security-sensitive content** in a public JIRA comment.
   Vulnerabilities go to <security@groovy.apache.org>.
6. **Stop at the draft.** Output is text for a human to review and
   post. The human assesses tone, accuracy, and timing.

## Hand-back contract

AI tooling working with JIRA produces drafts; it does not act on
JIRA. Specifically, AI tooling does **not**:

- Post comments to JIRA.
- Transition workflow state (any of `Open / In Progress / Resolved /
  Closed / Reopened`).
- Set or change any field (`Fix Version/s`, `Resolution`, `Priority`,
  `Component/s`, `Affects Version/s`, `Assignee`, `Labels`,
  `Summary`, `Description`).
- Create formal "Linked Issues" relationships.
- Assign issues to anyone, including the user.

With explicit instruction, AI tooling *may*:

- Produce a draft comment, draft `Component/s` suggestion, or draft
  cross-reference for the human to post.
- Run a JQL query and return the result set (read-only).
- Cite a `GROOVY-NNNNN` in a commit message or branch name the human
  is authoring.

The committer / contributor decides what gets posted and when.

## Validation checklist

Before declaring a JIRA-related output ready for human review:

- [ ] No proposed workflow transition; recommendations only.
- [ ] No proposed `Fix Version/s`, `Resolution`, or `Priority`.
- [ ] Any suggested `Component/s` maps to a real area of the codebase
      and matches a component on the live list (refetched, not from
      memory).
- [ ] Any `Affects Version/s` ask routes back to the reporter; no
      fabricated value.
- [ ] If the output relies on issue state: refetched from the live
      JIRA in this session, not cited from memory.
- [ ] Any JIRA reference uses the full `GROOVY-NNNNN` key (uppercase,
      no brackets, no prefix).
- [ ] Any JQL leads with `project = GROOVY`, quotes multi-word
      values, and uses the right operator for multi-value fields.
- [ ] No security-sensitive content in a draft destined for a public
      comment.
- [ ] The output is a draft for a human to review and post — not a
      payload to be sent automatically.

## References

- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) "Working with JIRA"
  — the canonical source for every convention restated above (states,
  fields, components, JQL recipes, commit references). Update this
  skill if a divergence appears.
- [`GOVERNANCE.md`](../../../GOVERNANCE.md) — JIRA as the project's
  canonical record.
- `.agents/skills/groovy-triage/SKILL.md` — sister skill; drives a
  full triage pass and defers to this skill for the AI-specific JIRA
  bits.
- `.agents/skills/groovy-fix-workflow/SKILL.md` — hand off to once a
  triage finding becomes a fix in flight.
- Live JIRA project: <https://issues.apache.org/jira/projects/GROOVY>
  — components, versions, workflow.
