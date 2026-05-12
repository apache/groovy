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
description: JIRA mechanics for the GROOVY project — JQL query recipes, the Component/s taxonomy, the field-ownership matrix (who sets Affects/Fix Version, Resolution, Priority), workflow states and the "don't transition workflow states" rule for AI tooling, and how `GROOVY-NNNNN` references connect commits and PRs back to JIRA. Use when constructing a JQL search, choosing or suggesting a Component, drafting a JIRA comment, or interpreting an issue's workflow state.
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: jira-mechanics-and-conventions
---

# Groovy JIRA

This skill is the **mechanics** of JIRA as the GROOVY project uses it:
JQL, fields, components, workflow states, and the commit ↔ JIRA link.
[`groovy-triage`](../groovy-triage/SKILL.md) calls into this skill
when triaging an issue; on its own, this skill is the right one to
load when constructing a JQL query, drafting a JIRA comment, or
reasoning about an issue's state — independent of any triage pass.

The canonical JIRA project is <https://issues.apache.org/jira/browse/GROOVY>.
The live project metadata (components, versions, custom fields) is
authoritative; this skill captures *conventions* and *recurring
queries*, not a frozen copy of project state.

## When to use this skill

**Use it for:**

- Writing or modifying a JQL query — for a sweep, a dashboard filter, a release-notes pass, or a duplicate search.
- Suggesting a `Component/s` value for an issue, or interpreting an existing one.
- Reasoning about whether a field (`Fix Version/s`, `Resolution`, `Priority`, `Affects Version/s`) should be touched, and by whom.
- Drafting an issue comment that references other GROOVY issues, commits, or PRs.
- Producing or reading the `GROOVY-NNNNN: …` reference in a commit message or branch name.

**Don't use it for:**

- Triage workflow itself (reproducing, drafting findings, PR-readiness checks) — that's [`groovy-triage`](../groovy-triage/SKILL.md). Pair the two for a triage pass.
- Security-sensitive issues. Suspected vulnerabilities go to <security@groovy.apache.org>, not into JIRA or a public comment. If a public JIRA appears to disclose a vulnerability, flag privately to a committer and stop.
- Workflow administration (creating versions, renaming components, editing the workflow itself) — that's a project-admin action, not triage and not this skill.

## Read first

- [`GOVERNANCE.md`](../../../GOVERNANCE.md) — JIRA's role as the canonical record of "what's planned, in progress, and done."
- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — `GROOVY-NNNNN: …` reference convention in commits.
- Live JIRA project page: <https://issues.apache.org/jira/projects/GROOVY> — components, versions, and the actual workflow are configured there, not in this repo.

## Top failure modes

These are the recurring mistakes when AI tooling reaches for JIRA mechanics:

1. **Transitioning workflow state.** AI tooling must *not* recommend or perform a transition (`Open → In Progress`, `Resolved`, `Closed`, `Reopened`). State changes are committer / contributor actions tied to actual code changes shipping. Surface a recommendation in a comment instead: "appears fixed on master at `<rev>` — proposing a committer close this as Cannot Reproduce after a second look."
2. **Inventing `Fix Version/s`.** `Fix Version/s` is set by the committer who resolves the issue, normally to the *next* release that will contain the fix. AI tooling should never propose a specific version — it has no way to know the release schedule. Leave the field alone.
3. **Inventing `Affects Version/s`.** This is the reporter's domain — the version they hit the bug on. If empty, *ask* the reporter; don't guess from "4.x" or the issue date.
4. **Setting `Resolution` on an Open issue.** `Resolution` is filled in *when an issue is resolved*, not before. An open issue with a resolution is malformed; don't suggest one.
5. **Confusing `Priority` with severity.** Priority is the project's call on what to work on next; don't infer it from the reporter's tone ("URGENT!!!"). Leave it alone unless a committer asks for a recommendation.
6. **Fabricating a Component.** Suggest `Component/s` only from the area of the codebase the bug touches (a real package path under `src/main/`), and frame it as a suggestion. If you can't map the bug to a package, say so — don't reach for a plausible-sounding component name.
7. **JQL that doesn't quote multi-word values.** `component = Static Type Checker` is a JQL syntax error; `component = "Static Type Checker"` is correct. Same for any value containing spaces, hyphens, or reserved characters.
8. **JQL using `=` on a multi-value field.** `Component/s` and `Affects Version/s` are multi-valued; the correct operator is `in` (`component in ("Parser")`) or `=` against a single value. Don't write `component = ("Parser", "Compiler")`.
9. **Forgetting `project = GROOVY`.** Apache's JIRA hosts many projects; a query without a `project` clause returns cross-project noise. Every saved or shared JQL should start `project = GROOVY AND …`.
10. **Bare JIRA IDs in commits.** Commits referencing an issue must use the full key, `GROOVY-NNNNN`, at the start of the subject line per [`CONTRIBUTING.md`](../../../CONTRIBUTING.md). `#12345`, `JIRA-12345`, or `groovy-12345` (lowercase) break the search-by-grep workflow contributors and tooling rely on.
11. **Linking an issue when a comment would do.** JIRA "is duplicated by / duplicates / blocks / is blocked by / relates to" links are project-level metadata; commenting "see also GROOVY-XXXX" is fine and reversible, formally linking an issue is committer territory.
12. **Quoting a stale JIRA snapshot.** Memory or cached search results can lag the live project by days. For decisions that hinge on current state (open vs. closed, latest comment), refetch — don't cite from memory.

## Field ownership

Who sets what, and what AI tooling may safely *suggest*:

| Field | Set by | AI may suggest? |
|---|---|---|
| `Summary` | Reporter (initially); committer may tidy | Yes — a sharper rewording, as a comment. |
| `Description` | Reporter | No — don't rewrite someone else's report. |
| `Issue Type` (Bug / Improvement / New Feature / Task / Sub-task) | Reporter; committer corrects | Yes — flag a "Bug" that's really an "Improvement," as a comment. |
| `Priority` | Committer / project | No — leave it. |
| `Component/s` | Reporter or committer | Yes — suggest from the package path the bug touches. |
| `Affects Version/s` | Reporter | Ask if missing; don't fabricate. |
| `Fix Version/s` | Committer, on resolve | No — never. |
| `Resolution` | Committer, on resolve | No — never. |
| `Assignee` | Self-assigning contributor or committer | No — don't assign on someone's behalf. |
| `Labels` | Anyone (low ceremony) | Sparingly. Match existing labels; don't invent new ones. |
| Workflow state (Open / In Progress / Resolved / Closed / Reopened) | Committer | No transitions — recommend in a comment. |

The rule of thumb: **AI tooling comments, humans transition.**

## Component/s taxonomy

The authoritative component list lives at
<https://issues.apache.org/jira/projects/GROOVY?selectedItem=com.atlassian.jira.jira-projects-plugin:components-page>.
Refetch it rather than relying on memory — the list evolves.

When suggesting a `Component/s` value for an open issue:

1. Identify the package or subproject the bug reaches. The top non-JDK frame of a stack trace, or the file that needs to change, usually points at it.
2. Map that area to a component using the live list. Component names typically follow the area-of-the-codebase shape (parser, compiler, type checker, AST transforms, runtime/MOP, a specific subproject under `subprojects/`, build, docs).
3. If multiple components fit, suggest the *narrower* one — `Static Type Checker` over `Compiler` when the issue is specifically STC.
4. If none fit, propose the closest match *and* note the mismatch in the comment — a recurring miss is a signal the component list needs a new entry, which is a separate (admin-side) action.

When *reading* a `Component/s` field that's already set, treat it as a routing hint, not a constraint. A bug filed against `Parser` that turns out to be a runtime issue gets re-suggested in a comment, not silently re-tagged.

## Workflow states

The GROOVY project uses the ASF JIRA workflow. The states an AI triage pass will see:

- **Open** — newly filed, unassigned, not yet started.
- **In Progress** — a contributor is actively working on it (self-assignment).
- **Reopened** — a previously-resolved issue that came back. Treat like Open for triage purposes, but check the prior resolution comment first.
- **Resolved** — fix has landed; awaiting verification or release.
- **Closed** — terminal state; with a `Resolution` (Fixed / Won't Fix / Duplicate / Cannot Reproduce / Incomplete / Not A Bug / Done).

For each, the AI-safe action is the same: **read, comment, do not transition.** The transition itself is the committer's call. The most useful AI output for any state is a factual summary plus a recommended next action — phrased as a recommendation, not an instruction.

## JQL recipes

Templates for the recurring searches. Substitute the bracketed placeholders before running.

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

**Recently updated (last N days), to catch reporter follow-ups:**

```jql
project = GROOVY AND updated > -<N>d ORDER BY updated DESC
```

**Duplicate hunting by error string:**

```jql
project = GROOVY AND text ~ "<distinctive phrase>"
```

`text ~` searches summary, description, and comments. Use a distinctive phrase from a stack trace or error message; a common word like `NullPointerException` alone produces too many hits.

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

Always lead with `project = GROOVY` — Apache's JIRA hosts many projects and an unscoped query returns cross-project noise (failure mode 9).

## Linking and references

- **From a commit:** `GROOVY-NNNNN: <short subject>` as the first line. JIRA picks up the link via the smart-commit handler; `git log --grep='GROOVY-NNNNN'` finds the change later. Variants like `[GROOVY-NNNNN]`, lowercase `groovy-`, or omitting the number break that search.
- **From a comment on issue A about issue B:** plain text `GROOVY-NNNNN` is enough — JIRA auto-links it. Use this for "see also" references; reserve the formal "Linked Issues" relationship for committers.
- **From a branch name:** `GROOVY-NNNNN-<short-slug>` is a common shape; not enforced, but it keeps the linkage visible in `git branch`.
- **Cross-references:** an AI-drafted JIRA comment that names another `GROOVY-NNNNN` should always include enough context that the cross-reference still makes sense if the linked issue is later edited.

## Validation checklist

Before posting JIRA-mechanics output (a query, a suggested field value, a drafted comment):

- [ ] If it's a JQL: leads with `project = GROOVY`; multi-word values quoted; multi-value fields use the correct operator.
- [ ] If it's a suggested `Component/s`: maps to a real package or subproject the issue touches, and matches a component on the live list (not a plausible-sounding invention).
- [ ] If it's a `Fix Version/s`, `Resolution`, or workflow transition: **don't.** Recast as a recommendation in a comment.
- [ ] If it's a JIRA reference: full `GROOVY-NNNNN` key, uppercase, no brackets or prefix.
- [ ] If the output relies on issue state: refetched from the live JIRA (or from a query run in this session), not cited from memory.
- [ ] No security-sensitive content in a public JIRA comment.

## References

- [`GOVERNANCE.md`](../../../GOVERNANCE.md) — JIRA as the project's canonical record.
- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — `GROOVY-NNNNN: …` commit-reference convention.
- `.agents/skills/groovy-triage/SKILL.md` — sister skill that drives a full triage pass; defers to this skill for JIRA mechanics.
- Live JIRA project: <https://issues.apache.org/jira/projects/GROOVY> — components, versions, workflow.
- Atlassian's JQL reference: <https://support.atlassian.com/jira-service-management-cloud/docs/jql-fields/>.
