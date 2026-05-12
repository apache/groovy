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
name: groovy-reproducer
description: Extracting and running reproducer code from a GROOVY JIRA report — locating the reproducer (description, comments, attachments), classifying its shape (runnable script, `@Test` snippet, inline fragment, prose-only, attachment, `@Grab`-using, multi-file project), adapting it to a runnable form *without fabrication*, running it with a bounded timeout, and recording deterministic evidence (revision, JDK, command, output, exit code) plus a classification (same-failure / different-failure / passes / cannot-run / timeout). Use when triage or a reassessment campaign needs to actually run what the reporter described.
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: jira-reproducer-extraction-and-execution
---

# Groovy reproducer

Use this skill when the job is to **take a JIRA-described problem and
actually run it**: find the reproducer code, work out what shape it's
in, adapt it to a runnable form, and execute it against a known
revision and JDK with enough evidence captured that a committer can
trust the verdict without redoing the work.

This skill is the load-bearing piece for both single-issue triage
(when a stronger-than-eyeballed reproduction is wanted) and the
bulk-reassessment campaign — it doesn't speak about workflow, batch
processing, or hand-back. For those, defer:

- [`groovy-triage`](../groovy-triage/SKILL.md) — the surrounding
  triage workflow for a single issue; it calls into this skill at
  the "attempt reproduction on `master`" step.
- [`groovy-reassess`](../groovy-reassess/SKILL.md) — the bulk
  reassessment campaign; it calls into this skill for every issue in
  the candidate set.
- [`groovy-tests`](../groovy-tests/SKILL.md) — when the reproducer
  is adapted into a `@Test`, this skill owns *running* it; that one
  owns *placement and naming*.
- [`groovy-jira`](../groovy-jira/SKILL.md) — the JQL and field
  conventions for the issue being reproduced.

## When to use this skill

**Use it for:**

- Extracting a reproducer from a JIRA description, comment thread, or
  attachment and running it against the current checkout.
- Producing an evidence package (rev, JDK, command, output, exit
  code, classification) that supports a triage finding or a
  reassessment verdict.
- Deciding whether a reporter's snippet can be reasonably adapted to
  a runnable form *without* fabrication.

**Don't use it for:**

- Writing a fresh regression test where no reporter reproducer
  exists — that's [`groovy-tests`](../groovy-tests/SKILL.md), and
  the regression test is the contributor's design call, not a
  reproduction of someone else's report.
- Single-issue triage workflow as a whole — [`groovy-triage`](../groovy-triage/SKILL.md)
  is the workflow; this skill is one of its steps.
- Bulk processing — [`groovy-reassess`](../groovy-reassess/SKILL.md)
  is the campaign layer; this skill handles one reproducer at a time.
- Reporters' projects requiring a separate Gradle / Maven build (a
  zip with its own `build.gradle`). Run those out of the Groovy
  checkout if it matters; this skill focuses on reproducers that
  run against the current Groovy build.

## Read first

- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — the regression-test
  shape that an adapted reproducer often takes.
- [`AGENTS.md`](../../../AGENTS.md) — the "what *not* to do" list
  applies here too: no fabricated reproducers, no hallucinated
  identifiers, no scratch files left behind.
- `build-logic/src/main/groovy/org.apache.groovy-tested.gradle` —
  the source of the `groovy/grape/` `junit.network` exclusion;
  reproducers under `groovy/grape/` need the same gate. See failure
  mode 12 in [`groovy-tests`](../groovy-tests/SKILL.md).

## Top failure modes

These are the recurring mistakes when extracting and running JIRA
reproducers:

1. **Fabricating a reproducer when none exists.** "The reporter
   described X happening; I'll write code that does X." That is the
   agent doing the reporter's job. If the description is prose-only
   and no attachment helps, classify `cannot-run-extraction` and
   stop. The reporter's specific code is what makes a reproduction
   trustworthy; an agent-written stand-in is a different exercise
   (and a different verdict).
2. **Skipping the comment thread.** Reporters frequently post a
   simplified reproducer in a comment after the initial description.
   Reading only the description misses it. Inventory every code
   block in the description *and* every comment *and* every
   attachment before picking a candidate.
3. **Treating `@Test` adaptation as equivalent to a script run.**
   Groovy scripts and class methods have different scoping (script
   bindings vs. fields, implicit `main`, `def` vs. typed locals).
   A reproducer that fails as a script may pass inside a `@Test` and
   vice versa. If the original was a script, *also* run it as a
   script before claiming the bug is gone — the `@Test` adaptation
   exercises different paths.
4. **Wrapping a JIRA snippet in `assert false` to "force a failure".**
   That signals nothing about the underlying behaviour. The
   adaptation should exercise the same path the reporter described
   and let it fail (or not) naturally. `assert <reporter-expected>`
   is fine; `assert false // bug` is theatre.
5. **Skipping the original JDK.** A reproducer that passes on
   JDK 21 may still fail on the JDK the reporter used (JDK 8, 11,
   17). The strict claim is "passes on JDK X master." Without JDK
   awareness, the verdict is incomplete — note the JDK in evidence,
   and where the verdict matters, retry on the originally-affected
   JDK if it is reasonably available locally.
6. **Running `@Grab` and silently absorbing the dependency failure.**
   Grape resolution that 404s or hits a dead repo should be a
   recognised classification (`cannot-run-dependency`), not "test
   passes" because the resolution exception was swallowed and the
   body never ran. Check exit code *and* output for resolution
   errors before classifying.
7. **No timeout.** A buggy reproducer can hang (infinite loop,
   deadlock, awaiting input). Without a timeout, one bad issue burns
   hours. Default to a bounded run (60s is a reasonable starting
   point; raise per-issue if the reporter notes long-running
   behaviour) and classify as `timeout` if hit.
8. **Capturing stdout but not stderr.** Many reproducers print the
   bug indicator (stack traces, MOP errors, "expected X got Y") to
   stderr. Capture both streams and surface both in the evidence.
9. **Comparing run output without normalising line endings or
   locale.** Same trap as [`groovy-tests`](../groovy-tests/SKILL.md)
   failure mode 11. Output captured on Windows uses `\r\n`; locale
   shifts number/date formatting. Normalise before string-compare,
   or compare on parsed values.
10. **Over-claiming "fixed" from a single-environment pass.** A
    clean run on your laptop may be environment-luck, not a real
    fix — locale, charset, default JDK, file-encoding defaults all
    bite. Where the verdict is `passes`, qualify it with the
    environment that produced the pass; don't generalise.
11. **Discarding the working tree before recording the run.**
    Capture `rev`, `jdk`, command, output, runtime, exit code
    *before* reverting any test additions or cleaning the scratch
    directory. The evidence is what gives the verdict its weight;
    losing it means the work has to be redone.
12. **Letting working-tree state leak between reproducers.** When
    running many in sequence (the reassessment case), reset between
    issues — `git stash --include-untracked && git clean -fd` on
    a sacrificial branch, or a separate scratch directory per
    issue. Crosstalk between reproducers (a file written by issue
    A's reproducer that issue B picks up) corrupts verdicts in ways
    that are hard to spot.
13. **Polluting the local Grape cache across many issues.** A sweep
    that runs many `@Grab`-using scripts can degrade
    `~/.groovy/grapes/` (see GROOVY-12005). For a campaign,
    consider a per-sweep Grape root via `-Dgrape.root=<scratch>` so
    the user's everyday cache stays clean.

## Reproducer shape taxonomy

Most JIRA reproducers fall into one of these shapes. The handling
recipe for each is the meat of this skill.

**A. Complete runnable Groovy script** — a `.groovy` file body in
the description or a comment, with imports and a top-level expression
or `main`. Recipe: save to a scratch `.groovy`, build a current
distribution (`./gradlew :installDist` on the relevant subproject),
and run with the built `groovy` binary. Or, for many cases, adapt as
a `@Test` per [`groovy-tests`](../groovy-tests/SKILL.md) and run
targeted — but be aware of failure mode 3 (script vs `@Test`
semantics).

**B. `@Test`-shaped snippet** — already class-and-annotation shaped.
Recipe: place under `src/test/groovy/bugs/Groovy<NNNN>.groovy` (or
the subproject's `src/test/`) per [`groovy-tests`](../groovy-tests/SKILL.md);
run targeted (`./gradlew :test --tests <FQN>`).

**C. Inline fragment** — a few lines, not a complete script ("when I
write `foo.bar()` I get NPE"). Recipe: wrap minimally — a `@Test`
with the fragment as the body, or a script with `def` declarations
for any referenced names. Where the wrap requires guessing types or
context, lean toward `needs-info` rather than speculating.

**D. Stack-trace-only** — a `.printStackTrace()` output, no code.
Recipe: classify `cannot-run-extraction`. The stack trace is a *hint
about the area*, not a reproducer. Don't construct code to "make
that stack trace appear"; that's fabrication.

**E. Prose-only** — natural-language description, no code. Recipe:
`cannot-run-extraction`. Don't write code from prose.

**F. Attachment** — `.groovy`, `.java`, `.zip` (project), `.txt`
(log), `.gz` (heap dump, etc.). For `.groovy` / `.java` files,
handle per shape A/B. For project zips, this is out of scope for
this skill — flag as `needs-separate-workspace` and let the
campaign / triage handle it. For logs and heap dumps, treat as
hints (shape D).

**G. `@Grab`-using script** — depends on resolution against Maven
Central or other repos. Recipe: run as in A, but with network
available; verify the resolution succeeded before interpreting the
result. A pinned old version may no longer be in the configured
repos (`cannot-run-dependency`).

**H. Multi-file project** — a tarball or zip with its own
`build.gradle` / `pom.xml`. Recipe: this skill's posture is "run
against the Groovy checkout"; project-style reproducers run *with*
their own build. Classify as `needs-separate-workspace` and surface;
the user / campaign can spin up an isolated workspace if it matters.

## Procedure

For each reproducer:

1. **Inventory.** Read the JIRA description, every comment, every
   attachment. Note all code blocks (verbatim, with their location
   — "description", "comment 3 by …", "attachment foo.groovy").
   Note the reporter's claimed environment: Groovy version, JDK, OS.
2. **Pick the candidate.** When multiple reproducers exist, prefer
   the simplest *complete* one. Note the fallback chain — if the
   simplest fails to adapt, the next one in line is the
   reporter's original.
3. **Classify the shape** per the taxonomy above. Output the shape
   category as part of the evidence package.
4. **Adapt without fabrication.**
   - Shape A: copy verbatim to a scratch file.
   - Shape B: place per [`groovy-tests`](../groovy-tests/SKILL.md).
   - Shape C: wrap minimally; if the wrap requires speculation
     (guessing a type, inventing a missing variable), stop and
     classify `cannot-run-extraction` with a note about what was
     missing.
   - Shapes D, E: classify `cannot-run-extraction`; don't adapt.
   - Shape F: per inner shape; for project zips, classify
     `needs-separate-workspace`.
   - Shape G: copy verbatim; flag for Grape-aware running.
   - Shape H: classify `needs-separate-workspace`.
5. **Build the current Groovy distribution** if the reproducer is a
   script that needs the produced `groovy` binary. For `@Test`-shape
   reproducers, the Gradle test invocation handles the build.
6. **Run with bounded resources.** Timeout (60s default), capture
   stdout + stderr + exit code + runtime. Record the command
   verbatim.
7. **Compare to the original failure pattern.** "Fails with the
   reported exception type and a message containing the reported
   substring" is `same-failure`. "Fails with something different" is
   `different-failure`. "Doesn't fail" is `passes`. "Hangs past
   timeout" is `timeout`. "Errors before exercising the path" is
   `cannot-run-*`.
8. **Record the evidence package** before doing anything else.
9. **Reset the working tree** if you adapted as a `@Test` (the
   added file must not leak to the next issue).

## Run posture

- **Timeout:** 60s default. Bump only when the reporter notes
  longer-running behaviour, and record the bump in evidence.
- **Network:** Grape needs it for shape G. Leave it on. Dependency
  failures get the `cannot-run-dependency` classification; they are
  not "fixed."
- **Filesystem:** scratch directory per issue, under
  `~/work/groovy-reassessment/<KEY>/` (or wherever the campaign
  layout puts it — see [`groovy-reassess`](../groovy-reassess/SKILL.md)).
  Don't write under the Groovy checkout.
- **Working tree:** clean between reproducers. The
  added-and-then-removed `@Test` is the most common leak source.
- **Grape cache:** for a campaign with many `@Grab` reproducers,
  consider `-Dgrape.root=<scratch>` so the user's
  `~/.groovy/grapes/` stays clean.
- **JDK selection:** record the JDK used. For verdicts where it
  matters (`passes`, `fixed-on-master`), retry on the
  originally-affected JDK via Gradle toolchains where reasonable.

## Evidence package

For each reproducer run, persist:

- `description.md` — the JIRA description and the comments quoted
  verbatim (so the verdict is auditable even if JIRA changes).
- `reproducer.<ext>` — the adapted runnable form, or
  `extraction-failed.md` if the shape didn't support adaptation.
- `original.<ext>` — the literal source from JIRA, untouched, when
  extracted.
- `run.log` — stdout + stderr from the run, with the exact command
  on the first line.
- `verdict.json` — `{ "key": "GROOVY-NNNNN", "shape": "<A|B|…>",
  "classification": "<one of: same-failure | different-failure |
  passes | cannot-run-extraction | cannot-run-environment |
  cannot-run-dependency | timeout | needs-separate-workspace>",
  "rev": "<short-sha>", "jdk": "<vendor+version>",
  "command": "<verbatim>", "runtime-ms": <int>,
  "exit-code": <int>, "matched-original-failure": <bool>,
  "notes": "<short>" }`.

This package is what a committer needs to trust the verdict, and it
is what [`groovy-reassess`](../groovy-reassess/SKILL.md) feeds into
its report.

## Validation checklist

Before recording a verdict:

- [ ] Every code block in the description, comments, and attachments
      was inventoried — not just the description.
- [ ] The chosen reproducer is verbatim from the report (or the
      adaptation is mechanical, not speculative).
- [ ] The shape is classified; if `cannot-run-*` or
      `needs-separate-workspace`, no execution attempt was claimed.
- [ ] Run was bounded by a timeout; the bound is recorded.
- [ ] Both stdout and stderr were captured.
- [ ] The exact command, revision, and JDK were recorded.
- [ ] The classification distinguishes `same-failure` from
      `different-failure` — the original failure pattern was
      consulted, not just "did it throw."
- [ ] For `@Grab` reproducers: dependency resolution succeeded
      before any "passes" verdict was claimed.
- [ ] For `passes`: the run environment is qualified; the verdict
      doesn't over-claim "fixed" from a single environment.
- [ ] Working tree was reset (no leftover scratch test class).
- [ ] Evidence package was written before the next issue started.

## References

- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — regression-test
  shape that `@Test`-adapted reproducers fit into.
- [`AGENTS.md`](../../../AGENTS.md) — the no-fabrication, no
  drive-by, no scratch-files-in-tree principles applied here.
- `.agents/skills/groovy-triage/SKILL.md` — single-issue caller of
  this skill.
- `.agents/skills/groovy-reassess/SKILL.md` — campaign-level caller
  of this skill.
- `.agents/skills/groovy-tests/SKILL.md` — placement and naming
  when a reproducer is adapted as a `@Test`; the
  `junit.network`-gating story for `groovy/grape/` reproducers.
- `.agents/skills/groovy-jira/SKILL.md` — JIRA mechanics for the
  issue around the reproducer.
