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
name: groovy-skills
description: Conventions for authoring or refactoring a SKILL.md under .agents/skills/ in the Apache Groovy repository — directory layout, required frontmatter, the standard section order (When to use / Read first / Top failure modes / Procedure / Validation checklist / References), the "failure modes are observed mistakes not aphorisms" rule, granularity heuristics for whether a topic deserves its own skill or belongs in an existing one, and the cross-skill linking and `AGENTS.md` table maintenance that keep the skill graph load-bearing. Use when adding a new skill, splitting an existing one, or reviewing a skill PR.
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: meta-skill-authoring
---

# Groovy skills

This is the meta-skill: it describes the conventions the other skills
under [`.agents/skills/`](../) follow. Load it when adding a new
`SKILL.md`, splitting an existing one (as
[`groovy-jira`](../groovy-jira/SKILL.md) was split out of
[`groovy-triage`](../groovy-triage/SKILL.md)), or reviewing a skill PR.

The other skills are the corpus this one describes. They are also the
canonical source — if anything in this skill conflicts with the
practice visible across the existing skills, the corpus wins and this
skill is out of date.

Worked examples in the corpus, when authoring a new skill:

- [`groovy-tests`](../groovy-tests/SKILL.md) — exemplar for the multiple-procedures shape (`## Procedure for X` / `## Procedure for Y`).
- [`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md) — exemplar for the single-procedure shape (plain `## Procedure`).
- [`groovy-triage`](../groovy-triage/SKILL.md) and [`groovy-jira`](../groovy-jira/SKILL.md) — exemplar for the deferral split (workflow skill that defers mechanics to a sister skill).
- [`groovy-reproducer`](../groovy-reproducer/SKILL.md) and [`groovy-reassess`](../groovy-reassess/SKILL.md) — exemplar for a load-bearing piece plus the campaign skill that loops over it.

## When to use this skill

**Use it for:**

- Adding a new `SKILL.md` under `.agents/skills/<name>/`.
- Refactoring or splitting an existing skill (extracting a deferral target, merging overlapping scopes, repositioning a skill against its neighbours).
- Reviewing a PR that adds or modifies a skill.
- Deciding whether a topic deserves its own skill or belongs as a section of an existing one.

**Don't use it for:**

- Editing the *content* of a specific area's skill — for build conventions, load [`groovy-build`](../groovy-build/SKILL.md) and edit there; this skill is about *how skills are written*, not their subject matter.
- Project-wide AI-contributor policy (licensing, `Assisted-by:`, the "what *not* to do" list) — that lives in [`AGENTS.md`](../../../AGENTS.md). This skill points at it, doesn't restate it.
- Authoring tooling outside `.agents/skills/` — `MEMORY.md`, `CLAUDE.md`, `copilot-instructions.md`, etc. — they follow different conventions.

## Read first

There is no separate convention document; **the existing skills are the convention.** Before writing or refactoring, read at least:

- [`groovy-build`](../groovy-build/SKILL.md) — long-running procedural skill, dense failure-mode list.
- [`groovy-tests`](../groovy-tests/SKILL.md) — paired procedures (regression test, AsciiDoc example), good template for multi-flow skills.
- [`groovy-triage`](../groovy-triage/SKILL.md) and [`groovy-jira`](../groovy-jira/SKILL.md) — worked example of a deferral split (workflow skill that defers mechanics to a sister skill).
- [`AGENTS.md`](../../../AGENTS.md) — the project's AI-contributor policy and the `## Skills` table this skill's procedure mutates.

If a question about conventions isn't answered by reading those, the convention probably hasn't been established yet — propose it on `dev@groovy.apache.org` rather than inventing it in a new skill.

## Top failure modes

Recurring mistakes when authoring or reviewing skills:

1. **Aphorisms instead of observed failure modes.** "Be careful with concurrency" is a style guide, not a failure mode. Each entry under `## Top failure modes` should name a *specific* mistake an LLM or contributor has actually made (or would plausibly make) in this area, with a short explanation of what's specifically wrong and what to do instead. If you can't think of a concrete instance, leave the entry out — five real failure modes beat ten generic ones.
2. **Inventing project conventions to fill a section.** If the rule the skill wants to encode isn't backed by [`CONTRIBUTING.md`](../../../CONTRIBUTING.md), [`GOVERNANCE.md`](../../../GOVERNANCE.md), [`ARCHITECTURE.md`](../../../ARCHITECTURE.md), [`COMPATIBILITY.md`](../../../COMPATIBILITY.md), [`AGENTS.md`](../../../AGENTS.md), or visible practice in the corpus, it's something the author made up. Either find the authoritative anchor or surface the gap (the human docs need a section) — don't fabricate one in the skill.
3. **Overlapping an existing skill.** Skills drift toward each other if the author hasn't read the existing surface. Before drafting, scan every `When to use this skill` block — both `Use it for` and `Don't use it for` — and decide whether your scope fits inside an existing skill, extends one, or needs its own (see "Granularity heuristics" below).
4. **Frontmatter `description:` that doesn't include the natural-language trigger phrases.** The `description:` is the discovery surface a loader / agent matches against. If the skill covers "first-pass triage of JIRA issues," that phrase should appear verbatim. Don't be cute — match the language a user would type.
5. **Missing or vague `Don't use it for` list.** Every existing skill has one and it is load-bearing — it tells the loader to stop here and *also* names the adjacent skill that should be loaded instead. A skill without explicit out-of-scope items drifts into being loaded for tasks it doesn't fit.
6. **Cross-links to skills that don't exist.** Every `[label](../<name>/SKILL.md)` must resolve to a real file. If you find yourself about to write "see `groovy-foo`" and `groovy-foo` doesn't exist yet, either author `groovy-foo` first or rephrase to a non-link.
7. **Validation checklist that re-summarises the procedure.** The checklist should be a *test* the skill applies to its own output — outcomes phrased as yes/no questions — not a recap of the steps. If "run `./gradlew test`" is procedure step 5, the checklist item is "targeted test run is green," not "ran the test."
8. **Mandates without an anchor citation.** "Always use X" with no link to where X is documented as the project norm leaves a future author with no way to defend or update the rule. Cite the authoritative source so the next person knows where the rule lives.
9. **Drafting a skill before the convention exists in the codebase.** A skill is the *working surface* — it describes what the project does, not what it should do. If the procedure you're encoding isn't actually project practice yet, propose the change first (on `dev@`), get it adopted, *then* write the skill. Skills are not a vehicle for unilateral policy changes.
10. **Granularity drift.** "How to rename a Groovy method" is too narrow (it's a section of [`groovy-internals`](../groovy-internals/SKILL.md)); "all of Groovy contribution" is too broad (that's [`AGENTS.md`](../../../AGENTS.md)). See "Granularity heuristics."
11. **Forgetting `AGENTS.md`.** A new skill that isn't listed in [`AGENTS.md`](../../../AGENTS.md)'s `## Skills` table is invisible to the loader and to humans browsing the repo. Removed skills mean removed rows. Updates to scope description happen there too.
12. **Skills that aren't an example of themselves.** Especially this one: if a meta-skill can't be read as an example of the conventions it claims, the conventions are weaker than it says. Same trap for any procedural skill — a `groovy-tests` that doesn't itself follow the test conventions it teaches is a tell.
13. **Marketing tone, emojis, hype.** None of the existing skills use them. The voice is terse, opinionated, cites paths and commands. "🎉 Welcome to the X skill!" is a tell the convention has been broken.
14. **Verbose introductions.** Existing skills open with one short paragraph that *positions the skill against its neighbours* — what it is, what to pair with, what to hand off to. Anything longer than ~5 lines before `## When to use this skill` is over-introduced.
15. **Putting universal contributor content in a skill instead of a human-facing doc.** If a rule applies equally to a human contributor making the same change — TDD ordering, scope discipline, JIRA conventions, codebase-specific gotchas, build mechanics — its canonical home is `CONTRIBUTING.md`, `ARCHITECTURE.md`, or another human-facing doc (or a subproject-local `ARCHITECTURE.md` for subproject-specific complexity). The skill cites the convention and adds the AI-specific guardrails on top. Trapping universal content in skill-only form makes the project's conventions invisible to humans browsing the repo and creates an AI-mediated literacy requirement for contributing — see *Skills are the AI layer over canonical docs* below.

## Skills are the AI layer over canonical docs

**Conventions are written for humans. Skills operationalise them
for AI.**

A convention's canonical home is a human-facing document at the
repository root or — for subproject-local complexity — alongside
the subproject's source. Skills cite the convention, sequence it
into AI-suitable procedures, and add the AI-specific guardrails
(hand-back contracts, autonomy limits, no-fabrication rules). If
a rule could equally apply to a human contributor making the
same change, the rule belongs in a human doc — the skill is its
operational shadow, not its origin.

Human-facing canonical docs in this project:

- [`CONTRIBUTING.md`](../../../CONTRIBUTING.md) — workflow,
  tests, JIRA conventions, triage methodology, fix workflow,
  test-writing pitfalls.
- [`ARCHITECTURE.md`](../../../ARCHITECTURE.md) — compilation
  pipeline, compiler/runtime conventions, build infrastructure,
  public API boundaries.
- [`COMPATIBILITY.md`](../../../COMPATIBILITY.md) — stability
  tiers, deprecation policy, binary-compatibility check.
- [`GOVERNANCE.md`](../../../GOVERNANCE.md) — project
  governance, mailing lists, JIRA's role.
- [`AGENTS.md`](../../../AGENTS.md) — AI-contributor policy,
  `Assisted-by:` convention, the "what *not* to do" list.
- Subproject-local `ARCHITECTURE.md` when a subproject has
  significant local complexity — e.g.
  [`subprojects/groovy-groovysh/ARCHITECTURE.md`](../../../subprojects/groovy-groovysh/ARCHITECTURE.md)
  for the vendored JLine forks and terminal-aware tests.

**The test for any rule you're considering writing into a
skill:** would a human contributor making the same change
benefit from this rule? If yes, find or create its canonical
home in a human doc; the skill should cite the rule, not
restate it. AI-specific constraints (autonomy limits, hand-back
contracts, no autonomous JIRA comments, no autonomous PRs) are
different — they belong in skills because they don't apply to
humans, who can't take those actions on autopilot anyway.

This split keeps conventions discoverable to humans (no
AI-mediated literacy requirement), keeps skills focused on
AI-specific concerns, and means the project's docs remain
authoritative if the AI tooling ecosystem changes.

Worked examples in the corpus, each demonstrating the pattern:

- **JIRA mechanics:** canonical in
  [`CONTRIBUTING.md`'s "Working with JIRA" section](../../../CONTRIBUTING.md#working-with-jira);
  AI guardrails in [`groovy-jira`](../groovy-jira/SKILL.md).
- **Triage methodology:** canonical in
  [`CONTRIBUTING.md`'s "Triaging" section](../../../CONTRIBUTING.md#triaging-issues-and-pull-requests);
  AI guardrails in [`groovy-triage`](../groovy-triage/SKILL.md).
- **Fix workflow:** canonical in
  [`CONTRIBUTING.md`'s "Fix workflow" section](../../../CONTRIBUTING.md#fix-workflow);
  AI guardrails in
  [`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md).
- **Test conventions and pitfalls:** canonical in
  [`CONTRIBUTING.md`'s "Tests" section](../../../CONTRIBUTING.md#tests);
  AI guardrails in [`groovy-tests`](../groovy-tests/SKILL.md).
- **Build infrastructure:** canonical in
  [`ARCHITECTURE.md`'s "Build infrastructure" section](../../../ARCHITECTURE.md#build-infrastructure);
  AI guardrails in [`groovy-build`](../groovy-build/SKILL.md).
- **Compiler/runtime conventions:** canonical in
  [`ARCHITECTURE.md`'s "Compiler and runtime conventions" section](../../../ARCHITECTURE.md#compiler-and-runtime-conventions);
  AI guardrails in
  [`groovy-internals`](../groovy-internals/SKILL.md).
- **Subproject-local (groovysh):** canonical in
  [`subprojects/groovy-groovysh/ARCHITECTURE.md`](../../../subprojects/groovy-groovysh/ARCHITECTURE.md);
  AI guardrails in [`groovysh`](../groovysh/SKILL.md).

Two skills in the corpus have no canonical-doc analog because
they're AI-procedural-only:
[`groovy-reproducer`](../groovy-reproducer/SKILL.md) (reproducer
extraction shape taxonomy, evidence packages, batch state
cleanup) and [`groovy-reassess`](../groovy-reassess/SKILL.md)
(bulk-reassessment campaign mechanics, classification taxonomy,
resumable session state). Humans don't formalise these as
discrete activities; the skills exist because AI makes them
tractable at scale. That's the legitimate "skill is the home"
case — when the activity itself has no human-side analog, not
when the convention does but you skipped promoting it.

## Conventions

### Directory layout

```
.agents/skills/
├── <skill-name>/
│   └── SKILL.md
```

One directory per skill, even when `SKILL.md` is the only file. The directory leaves room for examples, scripts, or supporting documents the skill links to. The skill name is kebab-case and usually follows the `groovy-<area>` pattern, except for single-word names that are already canonical project terms (e.g. `groovysh`).

### License header

Every `SKILL.md` opens with the ASF license header as an HTML comment, identical in wording to the existing skills. Copy from any existing skill — do not paraphrase.

### Frontmatter

```yaml
---
name: <kebab-case, matches directory name>
description: <one or more sentences naming the scope, with the natural-language trigger phrases a loader would match against. Mention the files/paths the skill applies to. Use when ... is a common closing pattern.>
license: Apache-2.0
compatibility: claude, codex, copilot, cursor, gemini, aider
metadata:
  audience: contributors to apache/groovy
  scope: <short identifier of the skill's subject area>
---
```

The `compatibility` list is the set of CLIs the skill is intended to work with. The Groovy corpus declares all six; if a skill is known *not* to work with one, drop it (and note the reason in a comment outside the frontmatter).

### Section order

The canonical order is:

1. `# <Title>` — the skill name rendered as words (e.g. `Groovy tests`, not `groovy-tests`).
2. **Opening paragraph (≤5 lines).** What this skill is, what to pair with, what to hand off to. Not a marketing intro — a positional one.
3. *Optional but encouraged for workflow / mechanics skills:* **a short bullet list of related-skill pointers**, each naming the relationship (*pair with* / *defers to … for mechanics* / *hand off to* / *sister skill* / *exemplar for*). The newer corpus members (`groovy-triage`, `groovy-jira`, `groovy-fix-workflow`, `groovy-reproducer`, `groovy-reassess`, and this skill) open this way; older skills predate the pattern and would benefit from being retrofitted. Skip the list if the skill is fully self-contained and has no meaningful relationships to surface.
4. `## When to use this skill` with **Use it for:** and **Don't use it for:** bullet lists.
5. `## Read first` — pointers to authoritative docs (root or subproject `AGENTS.md`, `CONTRIBUTING.md`, `GOVERNANCE.md`, `ARCHITECTURE.md`, `COMPATIBILITY.md`, external specs). Frames the skill as the working surface and those documents as the map.
6. `## Top failure modes` — numbered, each with a **bold one-line name** followed by an explanation.
7. *Optional* **reference sections** — zero or more named sections (e.g. `## Conventions`, `## Granularity heuristics`, a field-ownership table) that define the rules the procedures will defer to. Use when the rules don't fit inline in the procedure steps without bloating them. Skip if the procedures can carry the rules directly.
8. `## Procedure` (single flow) or one or more `## Procedure for …` sections (when the skill has more than one distinct flow) — step-by-step, with commands and file references. Use the `for …` suffix only when disambiguation between flows matters.
9. `## Validation checklist` — literal `- [ ]` checkboxes; outcomes phrased as yes/no.
10. `## References` — cross-links to other skills (with relationship described: "sister skill," "pair with," "hand off to," "defers to this skill for X") and external docs.

A section may be omitted only when it would be empty after honest filling — e.g. a deeply niche skill might have no separate `Procedure` section because the entire skill is failure-modes-and-checklist. Don't pad sections to keep the order, and don't insert reference sections that just restate the procedure.

### Cross-linking

Every cross-skill reference names the relationship. Patterns the corpus uses:

- "**sister skill**" — peer in scope; pair when both apply.
- "**pair with**" — load alongside this one for the typical workflow.
- "**hand off to**" — finish this skill's role here, switch to the other.
- "**defers to … for mechanics**" — this skill's procedure uses the other's vocabulary.

A bare hyperlink with no relationship hint is a smell.

Failure-mode references — within a skill or across skills — cite the failure mode's **italicised bold lead-in name**, never the position number. The number is unstable: renumbering a skill's failure-mode list silently breaks every numeric citation, here and elsewhere in the corpus. The name is the failure mode's identity, survives reordering, and lets a reader `Ctrl-F` to the entry. Example: "See *Inventing `Fix Version/s`* in [`groovy-jira`](../groovy-jira/SKILL.md)" — not "see failure mode 2 in groovy-jira".

## Granularity heuristics

When deciding whether a topic is its own skill or a section of an existing one:

**Make it its own skill if** (any of):

- Other skills will want to defer to it from inside their procedure (the deferral test — this is what motivated splitting [`groovy-jira`](../groovy-jira/SKILL.md) out of [`groovy-triage`](../groovy-triage/SKILL.md)).
- The failure modes are distinct from neighbouring skills' (different mistakes, different anchor docs).
- The procedure is self-contained enough that loading it alone is useful.
- The scope corresponds to a directory or subsystem with its own conventions ([`groovysh`](../groovysh/SKILL.md) is the worked example — a single subproject with enough internal weirdness to warrant its own skill).

**Make it a section of an existing skill if** (any of):

- It only ever runs as part of the parent skill's workflow.
- The failure modes are intermixed with the parent's.
- It's mostly mechanical with no judgement calls.
- Splitting would force readers to load both skills together in practice — that's a hint the split is artificial.

**Don't make it a skill if:**

- The convention it describes doesn't yet exist in project practice (see *Drafting a skill before the convention exists in the codebase* above).
- It's project-wide policy ([`AGENTS.md`](../../../AGENTS.md) territory) rather than area-specific procedure.

## Procedure for adding a new skill

1. **Read the corpus.** Open every existing skill's frontmatter and `When to use this skill` block. Confirm your scope doesn't already belong to one of them.
2. **Apply the granularity heuristics.** Decide skill vs. section. If section: stop here and edit the parent skill instead.
3. **Identify anchor docs.** Apply the *Skills are the AI layer over canonical docs* principle above: for each rule you'd put in the skill, decide whether its canonical home is `CONTRIBUTING.md`, `GOVERNANCE.md`, `AGENTS.md`, `ARCHITECTURE.md`, `COMPATIBILITY.md`, a subproject-local `ARCHITECTURE.md`, or an external spec. If a rule applies to a human contributor equally, its canonical home is a human-facing doc — promote it there first, and have the skill cite it. If a rule is genuinely AI-specific (autonomy limits, hand-back contracts, no-fabrication rules), it lives in the skill. If there's no anchor for a universal rule and no obvious place to put one, surface the gap (see *Drafting a skill before the convention exists in the codebase*) rather than fabricating one in the skill.
4. **Draft the frontmatter.** Include the natural-language trigger phrases in `description:`; match the existing `compatibility` and `metadata` shape.
5. **Draft the failure-mode list.** Aim for 5+ concrete entries. If you can't reach 5 genuine ones, the skill is probably too narrow.
6. **Draft the procedure(s).** Step-by-step with commands and file references. Match the surrounding skills' density — terse and opinionated.
7. **Draft the validation checklist.** Outcomes, not steps. Each item answerable yes/no.
8. **Cross-link.**
   - Outbound: name the relationship at every link.
   - Inbound: update the neighbouring skills that should mention this one in their `Don't use it for`, `References`, or failure-mode list. A new skill nobody points at is invisible.
9. **Update [`AGENTS.md`](../../../AGENTS.md).** Add a row to the `## Skills` table, alphabetically. The table is whitespace-aligned for monospace readability; preserve the column padding when inserting the new row.
10. **Self-consistency pass.** Read your skill as if you'd never seen it. Does it explain when to load it, what it's *not* for, and what to do? Does the validation checklist actually test the output, or just recap the steps?

## Procedure for splitting an existing skill

The [`groovy-triage`](../groovy-triage/SKILL.md) → [`groovy-jira`](../groovy-jira/SKILL.md) extraction is the worked example.

1. **Identify the deferral candidate.** A self-contained body of mechanics that other skills (current or anticipated) will want to defer to. If only one skill cares about it, it's probably a section, not a split.
2. **Create the new skill.** Follow "adding a new skill" above.
3. **Prune the source skill.**
   - Replace the extracted procedure with a one-line pointer to the new skill.
   - Remove failure modes now covered by the new skill, with a one-line "see `<new-skill>` for the rest."
   - Update the source skill's `## References` to list the new skill.
   - Update the source skill's validation checklist to defer where appropriate.
4. **Add inbound link.** The new skill's `## References` points back at the source.
5. **Update [`AGENTS.md`](../../../AGENTS.md).** Add the new row; revise the source row only if its scope description is now misleading.
6. **Re-read both skills together.** A correct split reads naturally when loaded together; if it reads as redundant or as if something is missing, the split line is wrong.

## Validation checklist

Before declaring a new or refactored skill ready:

- [ ] ASF license header is present, copied (not paraphrased) from an existing skill.
- [ ] License header closes with `-->` immediately before the `---` frontmatter delimiter (the common transcription typo is closing it with `---`).
- [ ] Frontmatter has `name`, `description`, `license`, `compatibility`, `metadata.audience`, `metadata.scope`.
- [ ] `name` matches the directory name.
- [ ] `description` contains the natural-language trigger phrases an agent or loader would match against.
- [ ] Standard section order: opening paragraph, *(optional related-skill bullet list)*, `When to use`, `Read first`, `Top failure modes`, *(optional reference sections)*, `Procedure(s)`, `Validation checklist`, `References`.
- [ ] Both **Use it for** and **Don't use it for** lists are populated, with at least one entry each.
- [ ] Every failure mode names a specific observed or plausible mistake (not an aphorism).
- [ ] Every internal `[label](../<name>/SKILL.md)` link resolves to an actual file.
- [ ] Failure-mode cross-references (within or across skills) cite the italicised lead-in name, not a position number.
- [ ] Every mandate cites or links the authoritative source.
- [ ] For each rule in the skill: would a human contributor making the same change benefit from it? If yes, the rule's canonical home is a human-facing doc (`CONTRIBUTING.md`, `ARCHITECTURE.md`, a subproject-local `ARCHITECTURE.md`, etc.); the skill cites it rather than restating it. AI-specific constraints (autonomy limits, hand-back contracts) stay in the skill.
- [ ] Validation checklist tests outcomes, not steps.
- [ ] Neighbouring skills that should reference this one have been updated (failure modes / `Don't use it for` / `References`).
- [ ] Row added to (or removed from) the `## Skills` table in [`AGENTS.md`](../../../AGENTS.md), alphabetically.
- [ ] No emojis, no marketing tone, no padding.
- [ ] The skill is an example of itself — read cold, it would pass its own checklist.

## References

- [`AGENTS.md`](../../../AGENTS.md) — project-wide AI-contributor policy and the `## Skills` table this skill mutates.
- [`groovy-build`](../groovy-build/SKILL.md), [`groovy-fix-workflow`](../groovy-fix-workflow/SKILL.md), [`groovy-internals`](../groovy-internals/SKILL.md), [`groovy-jira`](../groovy-jira/SKILL.md), [`groovy-reassess`](../groovy-reassess/SKILL.md), [`groovy-reproducer`](../groovy-reproducer/SKILL.md), [`groovy-tests`](../groovy-tests/SKILL.md), [`groovy-triage`](../groovy-triage/SKILL.md), [`groovysh`](../groovysh/SKILL.md) — the corpus this skill describes and the canonical source for any convention it leaves under-specified.
- The `groovy-triage` ↔ `groovy-jira` split — worked example of the "Procedure for splitting an existing skill" above.
