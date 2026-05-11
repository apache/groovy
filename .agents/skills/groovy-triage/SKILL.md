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
description: Guidance for AI-assisted triage of Apache Groovy JIRA issues and GitHub pull requests — reproducing reported bugs against `master`, finding duplicates, checking that JIRA component / affected-version fields are populated, and surfacing PR-readiness signals (linked JIRA, ASF license headers, drive-by reformatting, CI status) without taking committer-only actions. Use when grooming the JIRA backlog or doing a first-pass review on a PR.
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: issue-and-pr-triage
---

# Groovy triage

Use this skill when the task is **triaging** — surfacing the state of
a JIRA issue or a pull request and proposing next steps — rather than
writing the fix itself. Triage output is *advisory*: it lands as a
JIRA comment or PR review for a committer to act on. This skill never
resolves a JIRA, sets a fix version, merges a PR, or closes anything
without explicit committer instruction.

For the JIRA *mechanics* the triage pass relies on — JQL recipes, the
`Component/s` taxonomy, the field-ownership matrix, and the
"don't transition workflow states" rule — load
[`groovy-jira`](../groovy-jira/SKILL.md) alongside this skill.

For the actual fix work, hand off to
[`groovy-internals`](../groovy-internals/SKILL.md),
[`groovy-tests`](../groovy-tests/SKILL.md),
[`groovy-build`](../groovy-build/SKILL.md), or
[`groovysh`](../groovysh/SKILL.md) depending on where the change lands.

## When to use this skill

**Use it for:**

- Walking a JIRA issue: reading the report, attempting reproduction on `master`, searching for duplicates / related issues, checking that `Component/s`, `Affects Version/s`, `Priority`, and `Issue Type` are populated, and drafting a comment with findings.
- First-pass review on a GitHub pull request: checking the PR links a JIRA, that new files carry the ASF header, that the diff matches the stated scope, and that CI is green — then drafting review comments.
- Bulk grooming: producing a short, structured report across N issues or N PRs (one bullet per item) for a committer to scan.

**Don't use it for:**

- Actually fixing the bug — once triage points at a real defect, switch to the relevant area skill.
- Setting JIRA workflow state (`In Progress`, `Resolved`, `Fix Version/s`) — that's a committer action; surface a recommendation instead.
- Security reports. Suspected vulnerabilities go to <security@groovy.apache.org> per the ASF process, *not* into a public JIRA or PR comment. If a public issue or PR appears to disclose a vulnerability, flag privately to a committer and stop.
- Anything outside `apache/groovy` — sister repos (`groovy-website`, etc.) have their own conventions.

## Read first

- [`GOVERNANCE.md`](../../../GOVERNANCE.md) — where JIRA sits in the project's communication channels and what it's the canonical record of.
- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — JIRA reference convention in commits (`GROOVY-NNNNN: …`) and the regression-test contract that fixes are expected to satisfy.
- [`AGENTS.md`](../../../AGENTS.md) — ASF licensing / provenance rules, the `Assisted-by:` trailer, and the "what *not* to do" list (drive-by reformat, speculative abstractions) — these are the signals to flag on PR triage.

## Top failure modes

These are the recurring mistakes when AI tooling triages Groovy issues and PRs:

1. **Treating the reporter adversarially.** The default tone for JIRA comments is helpful and specific. Don't dismiss a report as "works for me" without showing what you ran. Don't suggest closing as "Cannot Reproduce" without a documented reproduction attempt against a stated `master` revision and the JDK noted in the issue.
2. **Asking for a reproducer that's already inline.** Before requesting "a minimal example," check the description, comments, and any attachments. A surprising fraction of issues have a complete reproducer the LLM skipped past.
3. **Not actually reproducing against `master`.** A report against `4.0.x` may already be fixed on `master`. Run the reproducer (or a stub of it) on `master` with the targeted Gradle test invocation before commenting. If it passes on `master`, say so explicitly and name the revision and JDK — that's the useful signal, not a guess.
4. **Missing the duplicate.** `git log --grep='GROOVY-NNNN'` finds prior commits referencing a JIRA; a JIRA text search finds similar wording. Spend the search budget *before* writing a long analysis — a one-line "duplicate of GROOVY-XXXX, fixed in 4.0.Y" beats a 500-word root-cause summary of a known bug.
5. **Confusing "the test passes" with "the bug is fixed."** A reporter's snippet may need adaptation to run as a `@Test`. If your adapted version passes but the original snippet (as a script) still fails, the bug isn't fixed — note the discrepancy.
6. **Drive-by closure of stale issues.** The Groovy JIRA holds long-tail reports that remain valid even after years of silence. "Last activity 2019" is *not* sufficient grounds to recommend closure. Only recommend closure with a concrete reason: reproduction now passes on `master`, the affected version is past end-of-life, or the report duplicates a resolved issue (cite the JIRA).
7. **PR triage that ignores the AGENTS.md "what *not* to do" list.** The list (drive-by reformatting, unsolicited refactors, hallucinated APIs, speculative abstractions, scratch files) is exactly what a first-pass reviewer should be flagging. If a PR diff is 80% whitespace, that's the headline finding.
8. **Missing the JIRA reference on a bug-fix PR.** `CONTRIBUTING.md` calls for `GROOVY-NNNNN: …` in the commit message for issue-linked changes. If a PR claims to fix a bug but doesn't reference a JIRA, the triage comment should ask for one (or point out that pure refactors / docs PRs may legitimately have none).
9. **Approving without an ICLA check on first-time contributors.** New external contributors need an ICLA on file for non-trivial changes. Don't assert ICLA status — the bot or a committer confirms it — but do flag "first-time contributor, ICLA status unknown" so the committer can check.
10. **Conflating CI red with PR bad.** GitHub Actions on this repo include long-running and occasionally flaky jobs (joint-validation, JMH). Distinguish a genuine failure in `groovy-build-test` from a transient timeout in `grails-joint-validation`; quote the failing job name and the first failing test rather than just "CI red."
11. **Writing an `Assisted-by:` trailer the contributor didn't ask for.** The trailer is the *contributor's* call to make on their own commit. Triage output is a comment, not a commit — don't suggest editing someone else's commit message to add the trailer; just point at [`AGENTS.md`](../../../AGENTS.md) if AI involvement is obvious and undisclosed.

For the JIRA-mechanics failure modes that used to live here — inventing `Fix Version/s`, transitioning workflow states, fabricating components, malformed JQL — see [`groovy-jira`](../groovy-jira/SKILL.md).

## Procedure for JIRA-issue triage

For each issue you triage:

1. **Read the full thread.** Description, all comments, attachments. Note the reported Groovy version, JDK, and OS. If the reporter included a stack trace, identify the top non-JDK frame — that's usually the entry point for "where in the code?"
2. **Search for duplicates / related work.**

   ```
   git log --grep='GROOVY-<NNNN>'             # commits referencing this JIRA
   git log --grep='<short distinctive phrase>' -- src/
   ```

   Plus a JQL text search — see the "Duplicate hunting by error string" recipe in [`groovy-jira`](../groovy-jira/SKILL.md). If you find a duplicate, stop and draft a "duplicate of …" comment.

3. **Attempt reproduction on `master`.**
   - If the reporter gave a script: drop it into a temp file and run via `./gradlew run` or paste it into the relevant test class as a `@Test`. Capture the exact output.
   - If the reporter gave a `@Test`-shaped snippet: follow [`groovy-tests`](../groovy-tests/SKILL.md) for placement and run targeted (`:test --tests <FQN>`).
   - Record: the `master` revision (`git rev-parse --short HEAD`), the JDK (`java -version`), the targeted command, and the outcome.

4. **Locate the code, lightly.** If the reproducer reaches the runtime/compiler, identify the package or class the failure flows through (top non-JDK frame in the trace, or `git log -p -S '<unique string>'` to find recent edits). Don't go deeper than "this lives under `org.codehaus.groovy.transform.stc.*`" unless asked — pointing the right area skill at it is the goal.

5. **Check JIRA fields.** Note (don't edit) what's missing or wrong. Field-ownership rules and `Component/s` suggestion guidance live in [`groovy-jira`](../groovy-jira/SKILL.md); apply them here.

6. **Draft the comment.** Structure it as: state of repro (passed / failed / could not run, with revision + JDK), duplicate search result, likely area of the code, suggested missing JIRA fields, recommended next action (e.g. "needs a minimal reproducer," "looks fixed on master — propose closing as Cannot Reproduce after a second pair of eyes," "appears to need a fix in `<area>`"). Keep it factual; don't editorialise.

7. **Don't transition the issue.** Even when the recommendation is clear, leave the workflow state to a committer (see [`groovy-jira`](../groovy-jira/SKILL.md)).

## Procedure for PR triage

For each PR:

1. **Read the PR description and the linked JIRA (if any).** A bug-fix PR without a `GROOVY-NNNNN` reference is the first thing to flag; a docs / build-housekeeping PR without one is fine.
2. **Scan the diff for shape, not just substance.**
   - **New files:** every `.java`, `.groovy`, `.gradle`, `.xml` must carry the ASF license header. Missing header → flag.
   - **Drive-by reformatting:** large whitespace-only hunks, end-of-line changes, or reordered imports outside the touched method signal a `what *not* to do` violation. Quote a representative file:line range.
   - **Hallucinated identifiers:** API methods or flags that grep doesn't find in the codebase. Search `git grep` before assuming a name is real.
   - **Scope creep:** does the diff match what the description and the JIRA say it should do? Surrounding refactors get called out.
3. **Check for the regression test.** If the PR claims to fix a JIRA-tracked bug, [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) calls for an accompanying test. Confirm the new test exists, follows `Groovy<NNNN>` or `// GROOVY-<NNNN>`-comment naming (see [`groovy-tests`](../groovy-tests/SKILL.md)), and lives in a sensible location. Confirm it would fail without the production change — easiest check: revert just the production hunk, run the test, expect failure.
4. **Look at CI.** Identify the *first* failing job and the *first* failing test in that job; quote them. Don't say "CI red" without specifics. Note known-flaky jobs (joint-validation, JMH) separately from core test failures.
5. **Note ICLA / first-time-contributor signal.** Surface it as a note for the committer; don't gate review on it.
6. **Draft the review.** Order findings by what would block merge first: license-header / scope / test, then style / drive-by reformat, then nits. Use the file-path:line format from this repo's conventions so committers can jump to each finding.

## Validation checklist

Before posting the triage output:

- [ ] The comment / review is grounded in something you *ran* or *searched* — not speculation. Each non-trivial claim cites either a command output, a `git grep` hit, or a JIRA / file reference.
- [ ] JIRA-mechanics checks pass — see the validation checklist in [`groovy-jira`](../groovy-jira/SKILL.md) (no workflow transitions, no invented `Fix Version/s`, suggested components are real, JQL leads with `project = GROOVY`).
- [ ] No suggested edit to someone else's commit message (including `Assisted-by:` trailer).
- [ ] If recommending closure of an old JIRA: a concrete reason (revision + JDK on which it now passes, or the duplicate JIRA-ID).
- [ ] If recommending duplicate: the duplicate JIRA is linked.
- [ ] CI claims name the specific failing job and first failing test.
- [ ] No security-sensitive detail in a public comment — if any suspicion of a vulnerability, route to <security@groovy.apache.org> and stop the public triage.
- [ ] Reviewer / triage tone is helpful and specific; the comment would read fine to the reporter or PR author.
- [ ] If AI tooling did substantive analysis, the *human* posting the comment is the one making the call; the triage output is a draft for them to review.

## References

- [`GOVERNANCE.md`](../../../GOVERNANCE.md) — JIRA's role as the canonical record; mailing list and Slack as the discussion channels.
- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — commit-message JIRA reference convention, regression-test requirement for JIRA-tracked fixes.
- [`AGENTS.md`](../../../AGENTS.md) — ASF licensing / provenance, `Assisted-by:` convention, and the "what *not* to do" list that drives most PR-triage findings.
- `.agents/skills/groovy-jira/SKILL.md` — JIRA mechanics (JQL, components, field ownership, workflow-state rule); pair with this skill on every triage pass.
- `.agents/skills/groovy-tests/SKILL.md` — regression-test naming and placement; pair with this skill when the triage produces a "needs regression test" finding.
- `.agents/skills/groovy-internals/SKILL.md` — hand off to this skill when triage points at a compiler/runtime defect.
- `.agents/skills/groovy-build/SKILL.md` — hand off when triage points at a build / packaging defect.
- `.agents/skills/groovysh/SKILL.md` — hand off when triage points at the REPL subproject.
- ASF Generative Tooling guidance: <https://www.apache.org/legal/generative-tooling.html>.
