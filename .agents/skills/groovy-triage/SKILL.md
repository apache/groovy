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
name: groovy-triage
description: AI-tooling guardrails for triaging GROOVY JIRA issues and GitHub pull requests — points at the project's triage methodology in CONTRIBUTING.md, then adds the AI-specific constraints on top: triage output is always advisory and never posts to JIRA or PR autonomously; no workflow transitions, no closures, no merges; no `Assisted-by:` trailer on someone else's commit. Use when grooming the JIRA backlog or doing a first-pass review on a PR.
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: ai-tooling-triage-guardrails
---

# Groovy triage

This skill is the **AI-tooling layer** over the project's triage
methodology. The methodology itself — how to triage a JIRA issue,
how to triage a PR, how to draft a useful comment — lives in
[`CONTRIBUTING.md`'s "Triaging issues and pull requests" section](../../../CONTRIBUTING.md#triaging-issues-and-pull-requests).
This skill cites it and adds the AI-specific guardrails: triage
output stays as a draft for a human to post, never transitions
workflow state, never resolves or closes anything autonomously,
never edits someone else's commit message.

- [`groovy-jira`](../groovy-jira/SKILL.md) — pair with for the
  JIRA-mechanics half of any triage pass (JQL, fields, components).
- [`groovy-reproducer`](../groovy-reproducer/SKILL.md) — pair with
  when the "attempt reproduction on `master`" step is the
  load-bearing piece.
- [`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md) — hand
  off to once triage points at a real defect with an area.
- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — the canonical
  source for the triage methodology this skill cites; load
  alongside.

## When to use this skill

**Use it for:**

- Walking a JIRA issue: reproducing on `master`, finding
  duplicates, checking fields, drafting a comment with findings.
- First-pass review on a GitHub PR: linked JIRA, ASF headers, diff
  shape, CI specificity, ICLA note, drafting a review.
- Bulk grooming: producing a short structured report across N
  issues or N PRs (one bullet per item) for a committer to scan.

**Don't use it for:**

- Posting to JIRA, transitioning workflow state, merging a PR,
  closing an issue, or any other committer action. The hand-back
  contract (below) is firm.
- Actually fixing the bug — once triage points at a real defect,
  hand off to [`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md).
- Security reports. Suspected vulnerabilities go to
  <security@groovy.apache.org> per the ASF process, *not* into a
  public JIRA or PR comment. If a public issue or PR appears to
  disclose a vulnerability, flag privately to a committer and
  stop.
- Anything outside `apache/groovy` — sister repos
  (`groovy-website`, etc.) have their own conventions.

## Read first

- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) "Triaging issues
  and pull requests" — the canonical methodology this skill cites.
- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) "Working with JIRA"
  — fields, states, components, JQL recipes; pair with the triage
  methodology.
- [`AGENTS.md`](../../../AGENTS.md) — ASF licensing / provenance
  rules, the `Assisted-by:` trailer convention, and the
  "what *not* to do" list that drives most PR-triage findings.

## Top failure modes

These are the recurring mistakes specific to AI tooling doing
triage:

1. **Posting the draft.** Triage output is *advisory* — it lands as
   a draft for a human to review and send. AI tooling does not post
   comments, transition issues, merge PRs, or close anything. See
   the hand-back contract below, and *Proposing a workflow
   transition* in [`groovy-jira`](../groovy-jira/SKILL.md).

2. **Writing an `Assisted-by:` trailer the contributor didn't ask
   for.** The trailer is the *contributor's* call on their own
   commit (see [`AGENTS.md`](../../../AGENTS.md)). Triage output is
   a comment, not a commit; don't suggest editing someone else's
   commit message to add the trailer — just point at AGENTS.md if
   AI involvement is obvious and undisclosed.

3. **Approving without an ICLA check on first-time contributors.**
   AI tooling cannot assert ICLA status; flag "first-time
   contributor, ICLA status unknown" in the draft so the committer
   can check.

4. **Conflating CI red with PR bad.** GitHub Actions on this repo
   include long-running and occasionally flaky jobs
   (joint-validation, JMH). Distinguish a genuine failure in
   `groovy-build-test` from a transient timeout in
   `grails-joint-validation`; quote the failing job name and the
   first failing test rather than just "CI red."

5. **Hallucinated identifiers in PR-diff analysis.** When reviewing
   a PR diff, AI tooling reaches for plausible-sounding API names
   to "verify" the change. `git grep` the identifier first; if it
   isn't there, that's a finding.

6. **Drafting in a tone that overstates AI authority.** Triage
   output is a recommendation a human posts under their name;
   phrase it as a recommendation, not a verdict. "Looks fixed on
   master at `<rev>` — may be a candidate for closing as Cannot
   Reproduce" is a recommendation; "Closing this as Cannot
   Reproduce" is theatre the human posting will have to walk back.

7. **Treating the reporter or PR author adversarially.** AI tooling
   tends to default to dismissive phrasing ("works for me",
   "without further information…") when the right move is patient
   specificity. The default tone for any GROOVY-facing comment is
   collaborative; the methodology in
   [`CONTRIBUTING.md`](../../../CONTRIBUTING.md#drafting-a-useful-comment-or-review)
   holds for AI drafts too.

## Hand-back contract

AI-assisted triage produces drafts; humans post and act.
Specifically, AI tooling does **not**:

- Post a JIRA comment or PR review.
- Transition any issue or merge any PR.
- Close, resolve, or self-assign an issue.
- Set or change any JIRA field (see
  [`groovy-jira`](../groovy-jira/SKILL.md) for the full
  field-by-field rule).
- Edit anyone's commit message, including adding an `Assisted-by:`
  trailer.

With explicit instruction, AI tooling *may*:

- Produce a draft comment or review for a human to post.
- Run a JQL query and return the result set (read-only).
- Quote a `GROOVY-NNNNN` reference in text the human is drafting.

The committer / contributor decides what gets posted and when.

## Validation checklist

Before declaring a triage draft ready for human review:

- [ ] Each non-trivial claim is grounded in something run or
      searched (command output, `git grep` hit, JIRA / file
      reference) — not speculation.
- [ ] No proposed JIRA workflow transition, closure, or merge.
- [ ] No suggested edit to anyone's commit message (including
      `Assisted-by:` trailer).
- [ ] If recommending closure of an old JIRA: a concrete reason
      (revision + JDK on which it now passes, or the duplicate
      JIRA-ID).
- [ ] If recommending duplicate: the duplicate JIRA is linked.
- [ ] CI claims name the specific failing job and first failing
      test.
- [ ] ICLA status of first-time contributors is flagged, not
      asserted.
- [ ] No security-sensitive content in a draft destined for a
      public comment.
- [ ] Output reads as a draft for a human to review and send — not
      a payload to be sent automatically.

## References

- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) "Triaging issues
  and pull requests" — canonical methodology.
- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) "Working with JIRA"
  — fields, components, JQL, commit-reference convention.
- [`AGENTS.md`](../../../AGENTS.md) — ASF licensing / provenance,
  `Assisted-by:` convention, and the "what *not* to do" list that
  drives most PR-triage findings.
- `.agents/skills/groovy-jira/SKILL.md` — sister skill for the
  JIRA-mechanics half; pair on every triage pass.
- `.agents/skills/groovy-reproducer/SKILL.md` — pair with for the
  "attempt reproduction on master" step.
- `.agents/skills/groovy-fix-workflow/SKILL.md` — hand off to once
  triage points at a real defect.
- `.agents/skills/groovy-tests/SKILL.md` — regression-test naming
  and placement; pair when triage produces a "needs regression
  test" finding.
- `.agents/skills/groovy-internals/SKILL.md` — hand off when
  triage points at a compiler/runtime defect.
- `.agents/skills/groovy-build/SKILL.md` — hand off when triage
  points at a build / packaging defect.
- `.agents/skills/groovysh/SKILL.md` — hand off when triage points
  at the REPL subproject.
