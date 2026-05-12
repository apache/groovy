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
name: groovy-reassess
description: Running a bulk reassessment campaign over old GROOVY JIRA issues — narrow JQL selection (Reopened pool surfaces wishlists; Open+EOL surfaces silent fixes and real bugs), per-issue reproducer extraction and execution via `groovy-reproducer`, classification (`fixed-on-master` / `still-fails-same` / `still-fails-different` / `cannot-run-*` / `intended-behaviour` / `duplicate-of-resolved` / `timeout`), orthogonal `nature` analysis (`bug-as-advertised` vs `feature-request-disguised-as-bug` vs `intended-and-documented`), workaround search, related-JIRA scan, split-candidate assessment, structured report and per-issue evidence package, and a strict hand-back contract — no JIRA comments, no transitions, no closures posted on behalf of the project. Use when sweeping old issues to surface candidates for closure (silently-fixed), candidates for a real fix (still-failing), feature-requests-disguised-as-bugs, and documentation gaps.
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: bulk-reassessment-of-old-jira-issues
---

# Groovy reassess

Use this skill when the task is a **campaign** over old GROOVY JIRA
issues: pick a bounded candidate set, run each reporter's reproducer
against the current `master`, classify the outcome, and produce a
report a committer can scan and act on. The campaign is the safest
non-trivial automation against ASF Groovy — read-only against JIRA,
side-effect-free against the project, advisory output. But "safe"
does not mean "no rules": the hand-back contract below is firm.

This skill is the **campaign layer**. Per-issue mechanics live
elsewhere:

- [`groovy-jira`](../groovy-jira/SKILL.md) — the JQL recipes that
  select the candidate set; the field-ownership rules; the
  "comment, don't transition" rule that applies at scale here.
- [`groovy-triage`](../groovy-triage/SKILL.md) — AI guardrails
  over the single-issue triage methodology in
  [`CONTRIBUTING.md`](../../../CONTRIBUTING.md#triaging-issues-and-pull-requests);
  pieces of that methodology apply to each candidate.
- [`groovy-reproducer`](../groovy-reproducer/SKILL.md) — the
  load-bearing per-issue piece: locate the reproducer, classify the
  shape, adapt, run, record evidence.
- [`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md) — where
  the `still-fails-*` tail goes once the campaign is done; the
  campaign produces ready-made reproducers for the fix workflow.

## When to use this skill

**Use it for:**

- A bounded sweep of old GROOVY issues (e.g. 10–50 at a time) to
  identify silent fixes, still-failing bugs, intended-behaviour
  misclassifications, or duplicates.
- Producing a structured report a committer can review in one
  sitting and decide what to act on.
- Generating ready-made reproducers and evidence packages that feed
  [`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md) for the
  still-failing tail.

**Don't use it for:**

- Single-issue triage — that's [`groovy-triage`](../groovy-triage/SKILL.md)
  on its own.
- Mass JIRA mutation (comments, transitions, closures, label
  edits). The campaign is **read-only against JIRA**.
- "Reassessing all 800 open issues" in one go — the campaign needs
  bounding. Many small passes beat one giant one.
- Anything that needs project-side authorisation (a bot identity,
  a dev@ mandate). The campaign is contributor work; the report is
  the output a contributor brings to dev@ or to a committer, not a
  project-mandated activity.

## Read first

- [`GOVERNANCE.md`](../../../GOVERNANCE.md) — JIRA's role as the
  canonical record; the campaign produces recommendations, not
  decisions.
- [`AGENTS.md`](../../../AGENTS.md) — the no-fabrication,
  no-comment-on-behalf-of-the-project posture that scales up here.
- [`groovy-reproducer`](../groovy-reproducer/SKILL.md) — fully.
  The campaign is largely a loop over this skill.

## Top failure modes

These are the recurring mistakes at the campaign level:

1. **Bulk-posting findings to JIRA.** Even when 30 of 30 findings
   say `fixed-on-master` with strong evidence, the campaign does
   *not* post 30 JIRA comments, transition 30 issues, or close
   anything. The output is a report; a committer decides whether
   and how to publish it. See *Transitioning workflow state* in
   [`groovy-jira`](../groovy-jira/SKILL.md) for the underlying rule.
2. **Optimistic classification on weak evidence.** A reproducer
   that didn't compile is `cannot-run-extraction`, not
   `fixed-on-master`. The taxonomy has cells for these for a
   reason; reach for the precise one. "Looks fine to me" is not a
   classification.
3. **Confusing "passes on this JDK" with "fixed."** The classic
   over-claim from *Over-claiming "fixed" from a single-environment
   pass* in [`groovy-reproducer`](../groovy-reproducer/SKILL.md),
   multiplied by the campaign size. Where the verdict matters and
   a JDK retry is feasible, do it before landing `fixed-on-master`.
4. **Unbounded scope.** Trying to sweep 200 issues in one session
   blows context, produces low-quality bulk output, and means a
   crash at issue 150 is a 150-issue loss. Bound the candidate set
   *before* the loop starts: a JQL with a `LIMIT`-equivalent, an
   age bucket, a component slice.
5. **No resumability.** A 50-issue run that crashes at issue 30
   must be resumable from issue 31. Per-issue evidence files on disk
   (per [`groovy-reproducer`](../groovy-reproducer/SKILL.md)'s
   evidence package) are the resumption point — in-memory campaign
   state is not.
6. **Burying the headlines.** "30 fixed-on-master, 5 still-fail,
   15 cannot-run." The 5 still-fails are usually the most
   important rows — they are tickets where work might actually be
   done. Surface them at the top of the report, not buried by the
   `fixed-on-master` majority.
7. **Recommending workflow transitions in the report.** "Close
   GROOVY-1234 as Cannot Reproduce" frames the agent as the
   decider. Phrase as recommendation: "`fixed-on-master`; a
   committer may want to consider closing as Cannot Reproduce
   after a second pair of eyes." See
   [`groovy-jira`](../groovy-jira/SKILL.md) on
   comment-not-transition.
8. **Fabricating evidence for `cannot-run-*`.** "Probably passes on
   master." That's a guess in a verdict slot. If it can't be run,
   the verdict is the cannot-run category — no further claim.
9. **Drifting from the original report.** Heavy adaptation of a
   reproducer can exercise different code paths from the
   reporter's original. The verdict has to be against the
   *original* behaviour; if the adaptation is so heavy that it's
   really a different test, the issue is `cannot-run-extraction`,
   not `passes`.
10. **Skipping the read-first pass on the issue.** A surprising
    fraction of old issues have a closing comment like "fixed in
    4.0.x, issue left open by mistake" or "won't fix, see
    GROOVY-XXXX". Skim the comments before reproducing — saves the
    reproduction budget and produces better classifications.
11. **Hammering JIRA's REST API.** ASF JIRA is anonymous-readable
    but shared. Cache aggressively (per-issue evidence retains the
    description and comments), throttle requests, and never run
    the campaign in a tight loop that re-fetches the same issue.
12. **Treating duplicates as findings.** If the reassessment
    discovers issue A is a duplicate of resolved issue B, the
    classification is `duplicate-of-resolved` with a citation —
    not "closing A." Same hand-back posture as everything else.
13. **Posting a wall-of-text to dev@.** A 50-row table without a
    summary is noise. The report opens with a short stanza — N
    issues swept, M classified each way, K headline findings — and
    *then* the table. The committer's first 30 seconds with the
    report should produce a decision about whether to read on.

## Selecting the candidate set

Selection drives the campaign's quality. Pick a *narrow*, *bounded*
slice; do not boil the ocean.

**Pool choice matters.** The pool-selection heuristics — which
status / affected-version slices over-represent which natures —
live in [`CONTRIBUTING.md`'s "Pool-selection heuristics for
re-triage sweeps"](../../../CONTRIBUTING.md#pool-selection-heuristics-for-re-triage-sweeps).
Choose the pool deliberately based on what the campaign is hunting
for.

JQL building blocks come from
[`groovy-jira`](../groovy-jira/SKILL.md). Useful slices for
reassessment:

- **Reopened (wishlist hunting):** `project = GROOVY AND status =
  Reopened ORDER BY updated ASC`. Small, well-bounded pool.
- **Open + EOL bugs (silent-fix hunting):** `project = GROOVY AND
  status = Open AND issuetype = Bug AND affectedVersion in
  ("1.5.6", "1.6.0", "1.6.5", "1.7.0", "1.8.0") ORDER BY created
  ASC`. The version list should target releases before a known
  major refactor of the relevant subsystem.
- **Age bucket × open:** `project = GROOVY AND statusCategory != Done
  AND created < "2020/01/01" AND created >= "2018/01/01"
  ORDER BY created ASC`. Buckets of two years are scannable; ten
  years is not.
- **Stale within a component:** `project = GROOVY AND statusCategory
  != Done AND component = "<X>" AND updated < -730d`. Pairs well
  with an area you know — your fix-side strength shapes the
  candidate set.
- **No component (triage-then-reassess):** `project = GROOVY AND
  statusCategory != Done AND component is EMPTY`. The reassessment
  can also produce a `Component/s` suggestion (see
  [`groovy-jira`](../groovy-jira/SKILL.md)).

Cap the per-session set. A practical first pilot is 5–10 issues
spanning *different reproducer shapes* (one runnable script, one
attachment, one prose-only-but-precise, one `@Grab`,
one comment-with-snippet) so the pipeline meets each shape early.
Pilots beat the first hundred issues you'd naturally pick.

## Procedure

For each campaign session:

1. **Pick the candidate set.** JQL from
   [`groovy-jira`](../groovy-jira/SKILL.md), capped (see above).
   Persist the candidate list to disk before the loop starts so a
   crash leaves a recoverable plan.
2. **Set up the scratch corpus.** A directory hierarchy under
   `~/work/groovy-reassess/<campaign-id>/`, one subdirectory per
   issue. Per [`groovy-reproducer`](../groovy-reproducer/SKILL.md),
   each subdirectory holds `description.md`, `reproducer.<ext>`,
   `original.<ext>`, `run.log`, `verdict.json`. This is *not* in
   the Groovy checkout.
3. **For each issue, in order:**

   **a. Resumability check.** Skip if its `verdict.json` already
   exists and is well-formed. The per-issue evidence files on
   disk are the resumption point — in-memory campaign state is
   not.

   **b. Triage the issue per
   [`CONTRIBUTING.md`'s "Triaging a JIRA issue"](../../../CONTRIBUTING.md#triaging-a-jira-issue).**
   That procedure is the canonical methodology — read the thread
   (with historical baselines), search for duplicates and
   same-family related work, attempt reproduction (including
   per-reproducer execution when the thread contains multiple
   distinct reproducers), locate the code, check JIRA fields,
   search for documented workarounds, and assess split
   candidacy. The bulk of the per-issue work is in that section.

   **AI-specific recording** during this step — these are the
   campaign's value-adds on top of the manual methodology:
   - Historical baselines from comments → `verdict.json.cases[].history`
   - Related-JIRA citations from the JQL scan → `verdict.json.notes`
   - Per-reproducer outcomes (when multiple reproducers exist) →
     `verdict.json.cases` (multi-case array)
   - Workaround-search outcome and the close-path it implies →
     `verdict.json.notes`
   - Split-candidacy recommendation → `verdict.json.notes`

   **c. Hand off to
   [`groovy-reproducer`](../groovy-reproducer/SKILL.md)** for the
   reproducer extraction, adaptation, running, and evidence
   capture. This includes the optional cross-family probe when
   the operation under test spans multiple types or operator
   variants — see
   [ARCHITECTURE.md "Operator families"](../../../ARCHITECTURE.md#operator-families)
   for the family taxonomies.

   **d. Apply nature analysis.** Per
   [`CONTRIBUTING.md`'s "Nature analysis"](../../../CONTRIBUTING.md#nature-analysis-bug-as-advertised-vs-wouldnt-it-be-nice),
   determine whether the issue is `bug-as-advertised`,
   `feature-request-disguised-as-bug`, `intended-and-documented`,
   etc. Record in `verdict.json.nature`. This is orthogonal to
   `classification` (a still-reproducing wishlist and a
   still-reproducing bug recommend different actions).

   **e. Compose the final `verdict.json`.** Classification +
   nature + cases (if multi-case) + cross-type-probe /
   operator-variants-probe (if run) + notes with the close-path
   recommendation. See
   [`groovy-reproducer`](../groovy-reproducer/SKILL.md)'s
   Evidence package section for the schema.

   **f. Reset the working tree** before the next issue.
4. **After the loop, build the report** (see below). Do *not*
   post anything.
5. **Hand back** to the human — branch (if any local Groovy
   commits, e.g. adapted `@Test` files kept for follow-up),
   scratch corpus path, report path.

## Nature analysis (AI-recording layer)

The nature taxonomy and the "bug-as-advertised vs wouldn't-it-be-
nice" question live in
[`CONTRIBUTING.md`'s "Nature analysis" section](../../../CONTRIBUTING.md#nature-analysis-bug-as-advertised-vs-wouldnt-it-be-nice).
That section is the canonical source; apply it per-issue during
procedure step 3d above.

This skill's contribution is to **record** the result in
`verdict.json.nature` so the campaign-level aggregator can roll
up nature classifications across the issue set (e.g. "of N
issues reassessed, M classified as
`feature-request-disguised-as-bug` — those rows recommend
re-typing rather than fixing"). The allowed values:

- `bug-as-advertised`
- `bug-as-advertised-partial-fix` (paired with a split
  recommendation in `notes`)
- `feature-request` (correctly typed as Improvement; no re-type
  needed)
- `feature-request-disguised-as-bug` (Bug → Improvement re-type
  recommendation)
- `intended-and-documented`

See the verdict-template `_field_help` for the schema. Apply
exactly one value per verdict — `nature` is required.

## Classification taxonomy

The campaign uses the per-issue classifications produced by
[`groovy-reproducer`](../groovy-reproducer/SKILL.md), and adds two
of its own that don't fall out of a single run.

Per-issue (from `groovy-reproducer`):

| Classification | Meaning | Recommendation a committer might consider |
|---|---|---|
| `fixed-on-master` | Reproducer passes on current `master` with the originally-affected JDK reachable, and the original failure pattern is gone. | Close as Cannot Reproduce after a second pair of eyes. |
| `still-fails-same` | Reproducer fails on `master` with the same failure pattern as the report. | Hand off to [`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md). |
| `still-fails-different` | Reproducer fails on `master` but with a different signature (different exception, different message). | Committer judgement — could be a regression-of-a-regression, a related bug, or environmental. |
| `cannot-run-extraction` | No usable reproducer (prose-only, missing attachment, fragment too incomplete to adapt without speculation). | Needs-info: ask the reporter, or close as Incomplete if old enough and reporter is unreachable (committer call). |
| `cannot-run-environment` | Reproducer needs a JDK/OS/dep we don't have locally. | Note the gap; may be a candidate for a richer environment matrix. |
| `cannot-run-dependency` | `@Grab` resolution failed (dep gone from configured repos). | Often correlates with very old issues; needs-info or close as Incomplete. |
| `timeout` | Hung past the configured bound. | Human review — might be the bug, might be a hung adaptation. |
| `needs-separate-workspace` | Reproducer is a multi-file project. | Spin up an isolated workspace if it matters; otherwise leave open. |

Campaign-level adds:

| Classification | Meaning | Recommendation |
|---|---|---|
| `intended-behaviour` | Reproduction runs and behaves as the spec/docs describe; the "bug" is by design. | Doc clarification or close as Not A Bug. |
| `duplicate-of-resolved` | Reassessment discovered the issue duplicates a resolved one (citation required). | Close as Duplicate, citing the linked issue. |

Each per-issue verdict in the report carries the classification
plus the evidence-package path. Each campaign-level classification
carries the citation that justifies it.

## Report shape

A reassessment report has three layers:

1. **One-paragraph summary at the top.** N issues swept, breakdown
   per classification, K headline findings worth a committer's
   attention (typically the `still-fails-*` rows and any
   `duplicate-of-resolved` with strong citation).
2. **Per-classification sections, headlines first.** Order:
   `still-fails-same`, `still-fails-different`, `duplicate-of-resolved`,
   `fixed-on-master`, `intended-behaviour`, then the cannot-runs and
   `needs-separate-workspace`. Within each section, a table:
   JIRA-ID, one-line summary, evidence-package path, recommended
   next action.
3. **Per-issue evidence packages** (filesystem; not inlined in the
   report). The report links into them.

Suggested filename for the report: `report.md` inside the campaign
scratch directory. A committer reading it should be able to decide
"yes I'll act on these N items" in a single scan.

## Hand-back to a human

The campaign produces:

- The scratch corpus path (with per-issue evidence packages).
- The report path.
- Any local Groovy-checkout changes worth keeping (e.g. adapted
  `@Test` files that the committer might want as a starting point
  for a real regression test).
- A short verbal summary: "Swept N issues. M `fixed-on-master`, K
  `still-fails-same`. Headlines: GROOVY-A, GROOVY-B, …."
- **Split recommendations** for any issue where the reassessment
  surfaced multiple cases with distinct fates (some silently fixed,
  some still failing) or distinct user-visible symptoms.
  Format: "close GROOVY-X with per-case summary; open new JIRA
  for GROOVY-X-residual-A (the remaining case)."
- **New-JIRA candidates** surfaced by cross-family probes — when
  probing a sibling type or operator variant uncovered a bug the
  original report didn't contain, flag it as a candidate for its
  own JIRA. The verdict notes should sketch the 4-line reproducer.
- **Documentation candidates** — issues where the close path is
  "Not A Bug + add docs" (workaround exists but undocumented; spec
  gap; behaviour-by-design but surprising). The report should
  list each docs deliverable with a one-line scope.

The campaign does **not**:

- Post comments to JIRA.
- Transition any issue.
- Open any PR (draft or otherwise).
- Email dev@ or anyone else on behalf of the project.
- Self-assign or otherwise touch JIRA fields.

With explicit instruction (per [`groovy-jira`](../groovy-jira/SKILL.md)
and [`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md)), the
output may then be used as:

- The basis for a single dev@ post summarising the campaign
  ("Reassessed N issues from 2018–2019; here is the report and
  evidence — feedback welcome before any committer acts on the
  recommendations").
- Per-issue JIRA comment drafts the committer reviews and posts.
- A starting point for [`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md)
  on each `still-fails-same` row.

The single dev@-thread shape is the recommended publication path —
N separate JIRA comments at once is noisy and out of step with how
the project communicates.

## Validation checklist

Before declaring a campaign session complete:

- [ ] **Pool was chosen deliberately** (Reopened vs Open+EOL) and
      the choice matches the campaign's goal (spec-debate review vs
      silent-fix hunting).
- [ ] Candidate set was bounded *before* the loop started; the
      bound is recorded in the report.
- [ ] Per-issue evidence package on disk for every candidate, with
      `verdict.json` and a non-empty `description.md`.
- [ ] Every `classification` used is one of the taxonomy entries;
      every `nature` used is one of the nature values; no free-form
      labels.
- [ ] **`nature` populated** on every verdict — orthogonal to
      classification.
- [ ] **Historical baselines from the comment thread** are recorded
      in `cases[].history` where applicable, so the headline
      finding can be "unchanged since baseline X" rather than just
      "current state is Y."
- [ ] **Workaround search done** for every still-fails-* verdict
      (`src/spec/doc/` + Groovydoc + JIRA comments). The
      recommendation distinguishes "close as Not A Bug" from
      "close + add docs."
- [ ] **Related-JIRA scan** done for every verdict; same-family
      issues cited in notes.
- [ ] `fixed-on-master` rows include the rev and JDK in the
      evidence; the verdict isn't over-claimed.
- [ ] `still-fails-same` rows are surfaced at the top of the
      report, not buried.
- [ ] `cannot-run-*` rows have a concrete reason in the evidence
      (which dep failed, what was missing) — not "could not run."
- [ ] **Split candidates flagged** for any issue with multi-case
      mixed fates or distinct user-visible symptoms.
- [ ] **Cross-family probe results recorded** in
      `cross_type_probe` or `operator_variants_probe` when the
      probe was run; any sibling-type bugs are flagged as new-JIRA
      candidates.
- [ ] No JIRA mutation occurred. No PR was opened. No dev@ post
      was sent.
- [ ] The report opens with a summary stanza a committer can scan
      in 30 seconds.
- [ ] Working tree was clean at the end of the session.
- [ ] Hand-back artefact lists the scratch corpus path, the report
      path, any local commits worth keeping, any split or
      documentation candidates surfaced, and the recommended
      publication path (typically a single dev@ thread).

## References

- [`GOVERNANCE.md`](../../../GOVERNANCE.md) — JIRA as canonical
  record; dev@ as the project's primary discussion channel.
- [`AGENTS.md`](../../../AGENTS.md) — the licensing, provenance,
  and "what *not* to do" rules the campaign inherits.
- `.agents/skills/groovy-jira/SKILL.md` — JQL recipes for selection;
  the comment-not-transition rule applied campaign-wide.
- `.agents/skills/groovy-triage/SKILL.md` — AI guardrails over the
  single-issue triage methodology in
  [`CONTRIBUTING.md`](../../../CONTRIBUTING.md#triaging-issues-and-pull-requests);
  pieces of that methodology apply to each candidate.
- `.agents/skills/groovy-reproducer/SKILL.md` — the per-issue load
  -bearing skill; the campaign is a loop over it.
- `.agents/skills/groovy-fix-workflow/SKILL.md` — AI guardrails
  over the fix workflow in
  [`CONTRIBUTING.md`](../../../CONTRIBUTING.md#fix-workflow);
  destination for the `still-fails-same` tail.
- `.agents/skills/groovy-tests/SKILL.md` — when an adapted `@Test`
  is kept as the starting point for a real regression test.
