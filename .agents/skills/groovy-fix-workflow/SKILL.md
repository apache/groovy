gro<!--
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
description: Workflow for AI-assisted fixing of a JIRA-tracked Apache Groovy bug — test-driven ordering (failing test on `master` first, then the smallest fix, then targeted/module re-runs), scope discipline, the `Assisted-by:` commit trailer, and the hand-back contract that keeps PR opening, JIRA comments, workflow transitions, and merges in committer hands. Use after triage has identified a real defect and pointed at an area, when you are about to write the regression test and the fix.
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: ai-assisted-fix-workflow
---

# Groovy fix workflow

Use this skill when triage has identified a real defect and you are
about to write the regression test and the fix. This skill is the
*recipe* — the ordering of steps, the stop points, and the artefacts
the human reviewer receives. It does not restate test placement,
JIRA mechanics, or area-specific gotchas; for those it defers.

- Triage and reproduction first: [`groovy-triage`](../groovy-triage/SKILL.md) establishes that
  the bug reproduces on `master` and locates it to an area. If that
  hasn't happened, do triage before this skill.
- Test placement and naming: [`groovy-tests`](../groovy-tests/SKILL.md) owns regression-test
  conventions (`Groovy<NNNN>` vs `// GROOVY-<NNNN>` on a method,
  JUnit 5 shape, the `src/test/` / `src/spec/test/` / `tests-preview`
  trees).
- JIRA mechanics and the commit reference: [`groovy-jira`](../groovy-jira/SKILL.md) owns the
  `GROOVY-NNNNN: …` form, field ownership, and the
  "AI tooling comments, humans transition" rule.
- Area depth: load [`groovy-internals`](../groovy-internals/SKILL.md), [`groovy-build`](../groovy-build/SKILL.md), or
  [`groovysh`](../groovysh/SKILL.md) for the codebase the fix touches; they hold the
  area-specific failure modes this skill deliberately omits.

## When to use this skill

**Use it for:**

- Implementing a fix for a JIRA-tracked Groovy bug, where triage has
  already produced a reproducer and pointed at an area of the code.
- Writing the regression test and the production change as a paired,
  ordered piece of work suitable for a committer to review.
- Preparing a clean, focused branch and commit for a human to land —
  including a fix you intend to submit yourself as a contributor.

**Don't use it for:**

- First-pass investigation of a fresh report — that's [`groovy-triage`](../groovy-triage/SKILL.md).
- Pure test work without a paired production change (executable spec
  examples, test-only refactors) — that's [`groovy-tests`](../groovy-tests/SKILL.md) on its own.
- Build / packaging changes — [`groovy-build`](../groovy-build/SKILL.md) is the workflow for
  those; the TDD shape here applies less directly.
- Documentation-only fixes where there is no behavioural change to
  test.
- Security-sensitive fixes. Suspected vulnerabilities go to
  <security@groovy.apache.org> per the ASF process; the fix is
  prepared privately and lands through a different channel, not via
  this skill.

## Read first

- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — the regression-test
  contract for JIRA-tracked fixes, the commit-message reference
  convention, and the "build locally before opening a PR" rule.
- [`AGENTS.md`](../../../AGENTS.md) — ASF licensing and provenance,
  the `Assisted-by:` / `Co-authored-by:` / `Generated-by:` trailer
  policy, and the "what *not* to do" list (drive-by reformat,
  speculative abstractions, hallucinated APIs, scratch files) that
  defines the scope discipline expected here.
- [`COMPATIBILITY.md`](../../../COMPATIBILITY.md) — for any fix that
  could touch observable API or runtime behaviour.

## Top failure modes

These are the recurring mistakes when AI tooling drives a fix:

1. **Skipping the failing-test-first step.** Writing the fix first and
   then writing a test that passes by construction is not test-driven —
   it's test-shaped post-rationalisation. The regression test must
   fail against `master` *before* the production change is applied.
   Quickest proof: stash the production hunk, run the targeted test,
   watch it fail; pop the hunk, run again, watch it pass.
2. **"Local IDE green" mistaken for "build green".** A test passing
   through an IDE runner bypasses Gradle's `Test` task configuration,
   including the `groovy/grape/` `junit.network` exclusion and any
   subproject-specific system properties. The signal that counts is
   `./gradlew :test --tests <FQN>` (or the subproject equivalent),
   not the IDE play button.
3. **Stopping at the targeted test.** Targeted run green is necessary
   but not sufficient. Run the surrounding module test pass next —
   `./gradlew :<subproject>:test` or `./gradlew :test`-with-a-package
   filter — to catch the nearby breakage the fix introduced.
4. **Scope creep once the test is green.** Drive-by formatting,
   "while I'm here" refactors, import reordering outside the touched
   method, comment rewordings — all of these are exactly what
   [`AGENTS.md`](../../../AGENTS.md) calls out under "what *not* to
   do." The diff after the test goes green should contain the test,
   the production hunk(s) the fix needs, and nothing else.
5. **Fixing the symptom instead of the cause.** The reproducer throws
   `NullPointerException` at line N; the patch adds a null guard at
   line N. Sometimes correct, often not — the null may indicate
   earlier state the surrounding code assumed was populated. Trace
   one or two frames up before reaching for the local guard. Pair
   this with the area skill for the cause-vs-symptom call.
6. **Misclassifying intended behaviour as a bug.** Some reports
   describe behaviour the spec or the docs already cover. If your
   would-be regression test confirms documented behaviour rather than
   contradicting it, the right output is a doc clarification and a
   JIRA comment recommending closure — not a code change. Surface
   this *before* writing the fix, not after.
7. **Hallucinated identifiers.** AI tooling will reach for plausible
   method or flag names that don't exist (or were renamed). `git grep`
   the identifier in the working tree before depending on it. If it
   isn't there, it isn't there.
8. **Read-only research treated as a substitute for running the
   build.** A research subagent or an `Explore` agent can map code
   and surface call sites; it cannot tell you the fix works. The fix
   only counts when the targeted Gradle invocation passes against
   the working-tree change.
9. **Pushing to someone else's branch, or auto-opening a PR.** The
   hand-back contract (below) is firm: even when the fix is complete
   and clean, the agent does not open a PR, comment on the JIRA,
   self-assign, or transition workflow state without explicit
   instruction. See also failure mode 11 in [`groovy-jira`](../groovy-jira/SKILL.md).
10. **Writing an `Assisted-by:` trailer on someone else's commit.**
    The trailer is the *contributor's* call on a commit they author.
    If you are the contributor, follow [`AGENTS.md`](../../../AGENTS.md);
    if you are preparing a change another contributor will commit,
    leave their commit message alone.
11. **Quietly cloning and editing a sibling repo.** Some fixes touch
    `groovy-website`, `groovy-eclipse`, or another ASF repo. Those
    have their own conventions, reviewers, and ICLA requirements;
    don't auto-clone and patch them. Flag the cross-repo need in the
    hand-back artefact and let a committer decide.
12. **Treating a green build as license to publish.** The build going
    green is the *floor*, not the ceiling. Scope discipline, regression
    test quality, and the hand-back contract all still apply. A clean
    diff a committer can read in two minutes beats a sprawling one
    they have to unpick.

## Procedure

When you have a triaged JIRA, an area, and a reproducer, work in this
order. Do not skip steps; do not reorder.

1. **Confirm prerequisites from triage.** You should have, from a
   [`groovy-triage`](../groovy-triage/SKILL.md) pass (or equivalent): the `master` revision the
   bug reproduces on, the JDK used, the area of the code, and the
   reproducer (snippet, script, or test stub). If any of these is
   missing, do that triage step now rather than guessing.

2. **Load the relevant area skill.** [`groovy-internals`](../groovy-internals/SKILL.md) for
   compiler/runtime work, [`groovy-build`](../groovy-build/SKILL.md) for build/packaging,
   [`groovysh`](../groovysh/SKILL.md) for the REPL. If the fix straddles areas, load each.
   The area skill points at the codebase-specific gotchas this skill
   deliberately doesn't repeat.

3. **Write the failing regression test.** Per [`groovy-tests`](../groovy-tests/SKILL.md):

   - Decide standalone class (`Groovy<NNNN>.groovy` in `src/test/`
     or a subproject's `src/test/`) vs. a `@Test` method on an
     existing class with a `// GROOVY-<NNNN>` comment immediately
     above it. Prefer the latter when an obvious home exists.
   - JUnit 5 shape, no `test` prefix on new methods, no Spock by
     reflex.
   - Use the reporter's reproducer as the body wherever it fits.

4. **Confirm it fails on `master`.** Run targeted:

   ```
   ./gradlew :test --tests <FQN>
   ./gradlew :<subproject>:test --tests <FQN>
   ```

   The test must fail with the same symptom the reporter described.
   If it passes, the test isn't exercising the bug — fix the test,
   not the symptom. If it fails for a *different* reason, you may
   be looking at a related-but-not-identical issue; re-triage rather
   than press on.

5. **Implement the smallest fix.** Cause-not-symptom (failure mode 5);
   no speculative abstraction or "while I'm here" cleanup (failure
   mode 4); match surrounding style; verified identifiers only
   (failure mode 7).

6. **Run the targeted test again.** Green. If not, iterate; do not
   weaken the test to match the implementation.

7. **Run the surrounding module test pass.** At minimum the
   subproject's `test` task; for a core change, `./gradlew :test`
   with a package filter is the practical inner loop. Confirm the
   fix did not regress nearby behaviour. Note: for tests under
   `groovy/grape/`, pass `-Djunit.network=true` — see failure mode
   12 in [`groovy-tests`](../groovy-tests/SKILL.md).

8. **If triage produced a reproducer project, run it against the
   working-tree build.** A `@Test` adapter is not the original
   reporter's reproducer; the script form often exercises slightly
   different paths. If the original reproducer still fails, the fix
   is incomplete.

9. **Diff the working tree.** Anything outside the regression test
   and the production hunks the fix requires needs a reason. If
   there isn't one, revert it — drive-by reformatting and stray
   imports are exactly what PR review will flag (and what
   [`groovy-triage`](../groovy-triage/SKILL.md) will surface on the way in).

10. **Stage the commit with the correct reference.** Per
    [`groovy-jira`](../groovy-jira/SKILL.md) and [`CONTRIBUTING.md`](../../../CONTRIBUTING.md):
    `GROOVY-NNNNN: <short subject>` on the first line, full key,
    uppercase, no brackets. Keep the test and the fix in one commit
    (or a paired test-then-fix sequence if the project's review
    practice in this area prefers that — match precedent in
    `git log --grep='GROOVY-' -- <touched path>`).

11. **Decide the `Assisted-by:` trailer.** If you are the
    contributor authoring this commit and AI tooling did substantive
    work, follow [`AGENTS.md`](../../../AGENTS.md) — `Assisted-by:`
    is the default. If you are preparing the change for *someone
    else* to commit, leave their commit message alone (failure mode
    10); the trailer is their call.

12. **Stop here.** Do not open a PR, comment on the JIRA, self-assign,
    or transition workflow state. Produce the hand-back artefact
    (below) and wait for explicit instruction.

## Cross-cutting and multi-subproject changes

Fixes that touch core plus a subproject (or two subprojects) follow
the same procedure with one elaboration: run the targeted test in
each affected module, then the module test pass for each, before
diff-checking scope. Cross-module fixes are also the case most
likely to drag in unrelated edits — be ruthless about scope at step 9.

A fix that genuinely needs changes in a sibling ASF repo
(`groovy-website`, `groovy-eclipse`, etc.) is the exception:
those repos have their own conventions and reviewers. Don't
auto-clone and patch them. Flag the cross-repo need in the hand-back
artefact and let a committer decide whether to coordinate the change.

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
- The diff scope summary — files changed and a one-line "why each".
- Any cross-repo follow-up that's needed (flagged, not actioned).
- Any open questions for the committer.

A committer reading that note should be able to decide "open the PR
and merge" or "needs another look at X" without having to re-run the
investigation.

## Validation checklist

Before producing the hand-back artefact:

- [ ] Regression test was confirmed to fail on `master` *before* the
      fix was applied (not after, not by construction).
- [ ] Test follows [`groovy-tests`](../groovy-tests/SKILL.md) conventions: JUnit 5, naming
      (`Groovy<NNNN>` or `// GROOVY-<NNNN>` comment), correct tree.
- [ ] Targeted Gradle run is green.
- [ ] Surrounding module test pass is green; nearby regression check
      done.
- [ ] If a reporter reproducer existed: it passes against the
      working-tree build, not just an adapted `@Test`.
- [ ] Working-tree diff contains only the test, the production
      change, and any directly-required edit. No drive-by reformat,
      stray imports, or speculative refactor.
- [ ] No new public API surface introduced unless the fix required
      it; if it did, [`COMPATIBILITY.md`](../../../COMPATIBILITY.md)
      was consulted.
- [ ] Commit subject starts `GROOVY-NNNNN: …` (uppercase, no
      brackets, no prefix).
- [ ] If authoring as a contributor: `Assisted-by:` trailer follows
      the [`AGENTS.md`](../../../AGENTS.md) policy; not added to
      anyone else's commit.
- [ ] No PR opened, JIRA comment posted, workflow transition
      proposed, or merge attempted on autopilot.
- [ ] Hand-back artefact lists branch, commit, gradle commands and
      outcomes, reproducer outcome, scope summary, and any
      cross-repo flag — enough for a committer to decide next.

## References

- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — regression-test
  contract for JIRA-tracked fixes, commit-message convention,
  "build locally before opening a PR" rule.
- [`AGENTS.md`](../../../AGENTS.md) — provenance and licensing,
  trailer policy, the "what *not* to do" list.
- [`COMPATIBILITY.md`](../../../COMPATIBILITY.md) — observable API
  and runtime-behaviour boundaries.
- `.agents/skills/groovy-triage/SKILL.md` — predecessor in the
  workflow; identifies the bug and the area before this skill picks
  up.
- `.agents/skills/groovy-jira/SKILL.md` — JIRA mechanics for the
  `GROOVY-NNNNN` reference, field ownership, and the
  "comment-not-transition" rule.
- `.agents/skills/groovy-tests/SKILL.md` — regression-test placement,
  naming, and the targeted-Gradle inner loop.
- `.agents/skills/groovy-internals/SKILL.md` — compiler/runtime area
  depth; pair with this skill on internals fixes.
- `.agents/skills/groovy-build/SKILL.md` — build/packaging area
  depth; pair with this skill on build-side fixes.
- `.agents/skills/groovysh/SKILL.md` — REPL subproject; pair with
  this skill on groovysh fixes.
- ASF Generative Tooling guidance:
  <https://www.apache.org/legal/generative-tooling.html>.
