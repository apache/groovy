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
name: groovy-fix-workflow
description: AI-tooling guardrails for fixing a JIRA-tracked GROOVY bug — points at the project's fix workflow in CONTRIBUTING.md (failing-test-first ordering, scope discipline, commit reference, IDE-vs-build pitfalls), then adds the AI-specific constraints on top: no autonomous PR opening, no JIRA comments on the project's behalf, no merges, no editing someone else's commit message, no sibling-repo edits without committer flag. Use when implementing a fix after triage has identified a real defect and pointed at an area.
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: ai-tooling-fix-workflow-guardrails
---

# Groovy fix workflow

This skill is the **AI-tooling layer** over the project's fix
workflow. The workflow itself — test first, fail on `master`,
smallest fix, targeted run green, module run green, scope
discipline, commit with `GROOVY-NNNNN` reference — lives in
[`CONTRIBUTING.md`'s "Fix workflow" section](../../../CONTRIBUTING.md#fix-workflow).
This skill cites it and adds the AI-specific guardrails: a
hand-back contract that keeps PR opening, JIRA comments, workflow
transitions, and merges in committer hands, and the small set of
mistakes AI tooling specifically tends to make.

- [`groovy-triage`](../groovy-triage/SKILL.md) — predecessor;
  produces the reproducer + area pointer this skill builds on.
- [`groovy-tests`](../groovy-tests/SKILL.md) — owns regression-test
  placement and naming.
- [`groovy-jira`](../groovy-jira/SKILL.md) — AI guardrails for the
  `GROOVY-NNNNN: …` commit reference and any JIRA touch on the way
  through.
- Area depth: load
  [`groovy-internals`](../groovy-internals/SKILL.md),
  [`groovy-build`](../groovy-build/SKILL.md), or
  [`groovysh`](../groovysh/SKILL.md) for the codebase the fix
  touches.
- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — the canonical
  source for the fix workflow this skill cites; load alongside.

## When to use this skill

**Use it for:**

- Implementing a fix for a JIRA-tracked Groovy bug, where triage
  has already produced a reproducer and pointed at an area.
- Preparing a clean, focused branch and commit for a human to land
  — including a fix you intend to submit yourself as a contributor.

**Don't use it for:**

- First-pass investigation of a fresh report — that's
  [`groovy-triage`](../groovy-triage/SKILL.md).
- Pure test work without a paired production change — that's
  [`groovy-tests`](../groovy-tests/SKILL.md) on its own.
- Build / packaging changes —
  [`groovy-build`](../groovy-build/SKILL.md) is the workflow for
  those; the TDD shape applies less directly.
- Documentation-only fixes where there is no behavioural change to
  test.
- Security-sensitive fixes. Suspected vulnerabilities go to
  <security@groovy.apache.org>; the fix is prepared privately and
  lands through a different channel.

## Read first

- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) "Fix workflow" —
  the canonical TDD ordering and scope discipline; the skill
  restates only what AI tooling specifically needs.
- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) "Regression tests
  for JIRA fixes" — companion section on test shape and placement.
- [`AGENTS.md`](../../../AGENTS.md) — ASF licensing / provenance,
  `Assisted-by:` trailer policy, the "what *not* to do" list.

## Top failure modes

These are the recurring mistakes AI tooling specifically makes when
implementing a fix:

1. **Hallucinated identifiers.** AI tooling reaches for plausible
   method or flag names that don't exist or have been renamed.
   `git grep` the identifier in the working tree before depending
   on it. If it isn't there, it isn't there.

2. **Read-only research mistaken for a green build.** A research
   subagent or an `Explore` pass can map code and surface call
   sites; it cannot tell you the fix works. The fix only counts
   when the targeted Gradle invocation passes against the
   working-tree change.

3. **Pushing to someone else's branch, or auto-opening a PR.** The
   hand-back contract (below) is firm: even when the fix is
   complete and clean, the agent does not open a PR, comment on
   the JIRA, self-assign, or transition workflow state without
   explicit instruction. See *Proposing a workflow transition* in
   [`groovy-jira`](../groovy-jira/SKILL.md).

4. **Writing an `Assisted-by:` trailer on someone else's commit.**
   The trailer is the *contributor's* call on a commit they author
   ([`AGENTS.md`](../../../AGENTS.md)). If you are the contributor,
   follow the AGENTS.md policy; if you are preparing a change for
   someone else to commit, leave their commit message alone.

5. **Quietly cloning and editing a sibling repo.** Some fixes
   touch `groovy-website`, `groovy-eclipse`, or another ASF repo.
   Those have their own conventions, reviewers, and ICLA
   requirements; don't auto-clone and patch. Flag the cross-repo
   need in the hand-back artefact and let a committer decide.

6. **Treating a green build as license to publish.** The build
   going green is the *floor*, not the ceiling. Scope discipline,
   regression-test quality, and the hand-back contract all still
   apply. A clean diff a committer can read in two minutes beats a
   sprawling one they have to unpick.

7. **Reaching for the symptom-fix when the cause is a frame up.**
   The reproducer throws `NullPointerException` at line N; the
   patch adds a null guard at line N. Sometimes correct, often
   not — the null may indicate earlier state the surrounding code
   assumed was populated. Trace one or two frames up before
   reaching for the local guard. Pair with the area skill for the
   cause-vs-symptom call.

## Procedure

When triage has produced a reproducer and pointed at an area:

1. **Load the relevant area skill** —
   [`groovy-internals`](../groovy-internals/SKILL.md) for
   compiler/runtime, [`groovy-build`](../groovy-build/SKILL.md)
   for build/packaging, [`groovysh`](../groovysh/SKILL.md) for the
   REPL. The area skill has the codebase-specific gotchas this
   skill deliberately doesn't repeat.

2. **Follow the fix workflow in
   [`CONTRIBUTING.md`](../../../CONTRIBUTING.md#fix-workflow)** —
   failing test on `master` first, smallest fix, targeted run
   green, module run green, scope check, commit with
   `GROOVY-NNNNN:` reference.

3. **Stop at the commit.** Don't open a PR, post a JIRA comment,
   self-assign, transition workflow state, or push to anyone's
   branch on autopilot. Produce the hand-back artefact (below) and
   wait for explicit instruction.

## Hand-back to a human

The AI-driven part of the workflow ends with a clean local branch
and an artefact a committer can review in a few minutes. The agent
does **not**:

- open a PR (draft or otherwise) without explicit instruction;
- post a JIRA comment on the issue;
- self-assign the JIRA, or transition its workflow state;
- push to a contributor's fork or branch on their behalf;
- merge anything.

With explicit instruction, the agent *may*:

- open a *draft* PR against `apache/groovy` (instruction must say
  "open a draft PR" — never on autopilot, never non-draft);
- post a prepared comment as a JIRA comment, where the human has
  reviewed the draft text first;
- run the build one more time on request.

The hand-back artefact is a short note (in the conversation, or as
a branch description) containing:

- The JIRA key and one-line summary.
- The branch name and the local commit hash(es).
- The targeted Gradle command and its result.
- The module Gradle command and its result.
- If a reporter reproducer was run: the command and its result.
- The diff scope summary — files changed and a one-line "why
  each".
- Any cross-repo follow-up that's needed (flagged, not actioned).
- Any open questions for the committer.

A committer reading that note should be able to decide "open the
PR and merge" or "needs another look at X" without having to
re-run the investigation.

## Validation checklist

Before producing the hand-back artefact:

- [ ] Fix workflow ordering observed (failing test on `master`
      first per
      [`CONTRIBUTING.md`](../../../CONTRIBUTING.md#fix-workflow);
      targeted and module test runs green).
- [ ] Test follows
      [`CONTRIBUTING.md`](../../../CONTRIBUTING.md#regression-tests-for-jira-fixes)
      conventions: JUnit 5, naming (`Groovy<NNNN>` or
      `// GROOVY-<NNNN>` comment), correct tree.
- [ ] Working-tree diff contains only the test, the production
      change, and any directly-required edit. No drive-by
      reformat, stray imports, or speculative refactor.
- [ ] No new public API surface introduced unless the fix required
      it; if it did,
      [`COMPATIBILITY.md`](../../../COMPATIBILITY.md) was
      consulted.
- [ ] Commit subject starts `GROOVY-NNNNN: …` (uppercase, no
      brackets, no prefix).
- [ ] If authoring as a contributor: `Assisted-by:` trailer
      follows the [`AGENTS.md`](../../../AGENTS.md) policy; not
      added to anyone else's commit.
- [ ] No PR opened, JIRA comment posted, workflow transition
      proposed, or merge attempted on autopilot.
- [ ] Hand-back artefact lists branch, commit, gradle commands and
      outcomes, reproducer outcome, scope summary, and any
      cross-repo flag.

## References

- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) "Fix workflow" —
  the TDD ordering, scope discipline, and IDE-vs-build pitfalls.
- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) "Regression tests
  for JIRA fixes" — test shape and placement.
- [`AGENTS.md`](../../../AGENTS.md) — provenance and licensing,
  trailer policy, the "what *not* to do" list.
- [`COMPATIBILITY.md`](../../../COMPATIBILITY.md) — observable API
  and runtime-behaviour boundaries.
- `.agents/skills/groovy-triage/SKILL.md` — predecessor; produces
  the triaged finding this skill builds on.
- `.agents/skills/groovy-jira/SKILL.md` — AI guardrails for JIRA
  references and field-touching.
- `.agents/skills/groovy-tests/SKILL.md` — regression-test
  placement and naming.
- `.agents/skills/groovy-internals/SKILL.md`,
  `.agents/skills/groovy-build/SKILL.md`,
  `.agents/skills/groovysh/SKILL.md` — area depth; pair with this
  skill on fixes in those areas.
- ASF Generative Tooling guidance:
  <https://www.apache.org/legal/generative-tooling.html>.
