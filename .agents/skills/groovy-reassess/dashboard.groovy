/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

// dashboard.groovy — render a reassessment-campaign dashboard from a
// directory of per-issue verdict.json files.
//
// A vetted, deterministic generator shipped per the "Helper mechanisms
// and token economy" policy in AGENTS.md: emitting a multi-KB HTML
// report token-by-token on every campaign is the single most
// token-expensive step in the reassess workflow, and the inputs and
// layout change rarely. The groovy-reassess SKILL.md "Campaign
// dashboard" section documents what this computes and why, and is the
// manual equivalent if you ever need to render it by hand.
//
// Usage:
//   groovy dashboard.groovy <campaign-dir> [out.html]
//   (defaults: campaign-dir = ., out = <campaign-dir>/dashboard.html)
//
// Reads <campaign-dir>/*/verdict.json, schema_version 1 (the schema in
// groovy-reproducer/SKILL.md — snake_case keys, optional cases[]).
//
// Requires Groovy 4.0+ (validated on 4.0.27). The body is kept
// parser-conservative (no computed-key map literals etc.) so the file
// still *parses* on older Groovy and the startup version check below
// can fail fast with a clear message instead of a cryptic crash —
// rather than asserting nothing and breaking deep in execution. (A
// future jbang header or a `groovyw`-style auto-version wrapper would
// remove the need for the manual check; until then this is the
// fallback.)
//
// Aggregation guardrails (enforced here, per the SKILL.md section):
//  - a verdict.json that fails to parse, or whose schema_version is
//    not 1, is surfaced as a visible error in the output — never
//    silently dropped, coerced, or counted as zero;
//  - exactly one campaign directory is processed — this never walks
//    upward or aggregates across sibling campaigns.

import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder

// --- Groovy version self-check (kept syntactically ancient-safe so it
// --- runs even on the version it is rejecting). GroovySystem is
// --- default-imported (groovy.lang). Fail fast with remediation.
def __ver = GroovySystem.version
def __parts = __ver.tokenize('.')
def __major = __parts[0].toInteger()
if (__major < 4) {
    System.err.println("dashboard.groovy requires Groovy 4.0+ (validated on 4.0.27); found ${__ver}.")
    System.err.println("Switch the active Groovy (e.g. `sdk use groovy 4.0.27`) and re-run.")
    System.exit(2)
}

def campaignDir = new File(args.length > 0 ? args[0] : '.').canonicalFile
if (!campaignDir.isDirectory()) {
    System.err.println("not a directory: ${campaignDir}")
    System.exit(2)
}
def outFile = args.length > 1 ? new File(args[1]) : new File(campaignDir, 'dashboard.html')

def slurper = new JsonSlurper()
def verdicts = []
def parseErrors = []
campaignDir.eachFile { entry ->
    if (!entry.isDirectory()) return
    def vf = new File(entry, 'verdict.json')
    if (!vf.exists()) return
    try {
        def raw = slurper.parse(vf)
        // Explicit schema_version guard (replaces field-name sniffing):
        // an unrecognised generation fails loudly, never silently coerced.
        // See groovy-reproducer/SKILL.md for the version contract.
        if (raw.schema_version != 1) {
            parseErrors << [dir: entry.name, why: "unsupported schema_version: ${raw.schema_version} (expected 1)"]
            return
        }
        if (!raw.key) { parseErrors << [dir: entry.name, why: 'no key field']; return }
        verdicts << raw
    } catch (Exception ex) {
        // Guardrail: a broken verdict is a surfaced error, not a zero.
        def why = (ex.message ?: ex.class.simpleName).toString().replaceAll(/\s+/, ' ').trim()
        parseErrors << [dir: entry.name, why: why.size() > 200 ? why[0..199] + '…' : why]
    }
}
verdicts = verdicts.sort { it.key }

if (verdicts.isEmpty() && parseErrors.isEmpty()) {
    System.err.println("no verdict.json files under ${campaignDir} — is this a campaign directory?")
    System.exit(3)
}

def failingClasses = ['still-fails-same', 'still-fails-different']
def total   = verdicts.size()
def failing = verdicts.findAll { it.classification in failingClasses }
def fixed   = verdicts.count { it.classification == 'fixed-on-master' }
def intended = verdicts.count { it.classification == 'intended-behaviour' }
def unrun   = verdicts.count { (it.classification ?: '') ==~ /cannot-run-.*|timeout|needs-separate-workspace|needs-safety-review/ }
def partial = verdicts.count { v ->
    if (!(v.cases instanceof List)) return false
    v.cases.any { it.match_on_master == true } && v.cases.any { it.match_on_master == false }
}
def failingPct = total > 0 ? (failing.size() * 100.0 / total) : 0.0

// Health-rating thresholds. Proposed defaults pending team review
// (see the SKILL.md "Campaign dashboard" section); tune there, not here.
def rating
if (failingPct < 5)       rating = 'Healthy'
else if (failingPct < 20) rating = 'Needs attention'
else                      rating = 'Action needed'
def ratingClass = rating == 'Healthy' ? 'green' : (rating == 'Action needed' ? 'red' : 'amber')

// classification x nature cross-tab — the view that drives action
// (a still-failing wishlist and a still-failing bug differ).
def classes = (verdicts.collect { it.classification ?: '(none)' } as Set).toList().sort()
def natures = (verdicts.collect { it.nature ?: '(none)' } as Set).toList().sort()
def crosstab = [:]
classes.each { c ->
    def row = [:]
    natures.each { n ->
        row[n] = verdicts.count { (it.classification ?: '(none)') == c && (it.nature ?: '(none)') == n }
    }
    crosstab[c] = row
}

def panel = { String nat -> failing.findAll { it.nature == nat } }
def directFixes  = panel('bug-as-advertised')
def partialFixes = panel('bug-as-advertised-partial-fix')
def hygiene      = verdicts.findAll { it.nature == 'feature-request-disguised-as-bug' }
def features     = verdicts.findAll { it.nature == 'feature-request' }
def notABug      = verdicts.findAll { it.classification == 'intended-behaviour' || it.nature == 'intended-and-documented' }
def probeNew = []
verdicts.each { v ->
    if (v.cross_type_probe?.findings) probeNew << [key: v.key, fam: 'cross-type', sum: v.cross_type_probe?.summary]
    if (v.operator_variants_probe?.findings) probeNew << [key: v.key, fam: 'operator-variants', sum: v.operator_variants_probe?.summary]
}

// Staleness of still-failing issues, derived only from data actually
// present (cases[].history[].year). No fabricated dates.
def thisYear = Calendar.instance.get(Calendar.YEAR)
def stale = []
failing.each { v ->
    def years = []
    if (v.cases instanceof List) {
        v.cases.each { c -> (c.history ?: []).each { h -> if (h.year) years << (h.year as int) } }
    }
    if (years) stale << [key: v.key, age: thisYear - years.min()]
}
stale = stale.sort { -it.age }

def esc = { o -> (o == null ? '' : o.toString()) }
def sw = new StringWriter()
def mb = new MarkupBuilder(sw)
mb.html {
    head {
        meta(charset: 'UTF-8')
        title("Reassessment dashboard — ${campaignDir.name}")
        style('''
            body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; max-width: 1100px; margin: 24px auto; padding: 0 16px; color: #1f2328; }
            h1, h2 { border-bottom: 1px solid #d0d7de; padding-bottom: 6px; margin-top: 28px; }
            .hero { display: flex; gap: 12px; margin: 16px 0; flex-wrap: wrap; }
            .card { flex: 1 1 160px; padding: 16px; border-radius: 8px; background: #f6f8fa; min-width: 130px; }
            .card.red { background: #ffe9e6; } .card.amber { background: #fff8c5; } .card.green { background: #dafbe1; }
            .card .label { font-size: 11px; color: #57606a; text-transform: uppercase; letter-spacing: .04em; }
            .card .value { font-size: 28px; font-weight: 600; margin-top: 4px; }
            .rating { padding: 12px 16px; border-radius: 8px; margin: 12px 0; font-weight: 600; color: #fff; }
            .rating.green { background: #2da44e; } .rating.amber { background: #bf8700; } .rating.red { background: #cf222e; }
            .warn { background: #fff8c5; border: 1px solid #d4a72c; padding: 10px 14px; border-radius: 8px; margin: 12px 0; }
            table { width: 100%; border-collapse: collapse; margin: 12px 0; }
            th, td { padding: 6px 8px; text-align: left; border-bottom: 1px solid #d0d7de; font-size: 13px; }
            tr:nth-child(even) { background: #f6f8fa; }
            .footer { font-size: 12px; color: #57606a; margin-top: 24px; padding-top: 12px; border-top: 1px solid #d0d7de; }
            ul { padding-left: 24px; } ul li { margin: 6px 0; }
        ''')
    }
    body {
        h1("Reassessment dashboard — ${campaignDir.name}")
        p("${total} issue(s) swept. Rev: ${esc(verdicts[0]?.rev)}, JDK: ${esc(verdicts[0]?.jdk)}.")

        if (parseErrors) {
            div(class: 'warn') {
                b("⚠ ${parseErrors.size()} verdict file(s) could not be parsed")
                mkp.yield(' — surfaced, not counted. Fix and re-run before trusting the totals:')
                ul { parseErrors.each { e -> li("${e.dir}: ${esc(e.why)}") } }
            }
        }

        div(class: 'hero') {
            [['Candidates', total, 'neutral'],
             ['Still failing', failing.size(), failingPct > 20 ? 'red' : (failingPct > 5 ? 'amber' : 'green')],
             ['Fixed on master', fixed, 'green'],
             ['Partial fix', partial, partial > 0 ? 'amber' : 'green'],
             ['Intended behaviour', intended, 'neutral'],
             ['Unrun', unrun, 'neutral']].each { row ->
                div(class: "card ${row[2]}") {
                    div(class: 'label', row[0])
                    div(class: 'value', String.valueOf(row[1]))
                }
            }
        }
        div(class: "rating ${ratingClass}", "Health: ${rating} (${String.format('%.0f', failingPct)}% still failing)")

        h2('Classification × nature')
        table {
            thead { tr { th('classification \\ nature'); natures.each { th(it) } } }
            tbody {
                classes.each { c ->
                    tr {
                        td { b(c) }
                        natures.each { n -> td(String.valueOf(crosstab[c][n])) }
                    }
                }
            }
        }

        h2('Action — direct fixes (still-failing × bug-as-advertised)')
        if (directFixes) { ul { directFixes.each { v -> li { b(v.key); mkp.yield(" — /groovy-fix-workflow ${v.key}") } } } }
        else { p('None.') }

        h2('Action — partial fixes')
        if (partialFixes) { ul { partialFixes.each { v -> li { b(v.key); mkp.yield(" — ${esc(v.cases_summary)}") } } } }
        else { p('None.') }

        h2('Tracker hygiene — feature-request-disguised-as-bug')
        if (hygiene) { ul { hygiene.each { v -> li { b(v.key); mkp.yield(' — recommend re-typing Bug → Improvement.') } } } }
        else { p('None.') }

        h2('Feature requests (correctly typed)')
        if (features) { ul { features.each { v -> li { b(v.key) } } } }
        else { p('None.') }

        h2('Closure candidates — intended behaviour / not a bug')
        if (notABug) { ul { notABug.each { v -> li { b(v.key); mkp.yield(' — observed behaviour correct per docs; propose Not A Bug after review.') } } } }
        else { p('None.') }

        h2('New-issue candidates from probes')
        if (probeNew) { ul { probeNew.each { c -> li { b(c.key); mkp.yield(" (${c.fam}): ${esc(c.sum)}") } } } }
        else { p('None.') }

        if (stale) {
            h2('Still failing — staleness (from recorded history baselines)')
            ul { stale.each { s -> li { b(s.key); mkp.yield(" — unresolved ~${s.age} year(s) by recorded baselines") } } }
        }

        h2('All issues')
        table {
            thead { tr { ['Key', 'Shape', 'Classification', 'Nature'].each { th(it) } } }
            tbody {
                verdicts.each { v ->
                    tr {
                        td { a(href: "https://issues.apache.org/jira/browse/${v.key}", v.key) }
                        td(esc(v.shape)); td(esc(v.classification)); td(esc(v.nature))
                    }
                }
            }
        }

        div(class: 'footer') {
            p("Campaign: ${campaignDir.name}")
            p("Generated by .agents/skills/groovy-reassess/dashboard.groovy on Groovy ${GroovySystem.version}. " +
              "Health-rating thresholds are proposed defaults pending team review (see the skill).")
        }
    }
}

outFile.text = sw.toString()
System.err.println("wrote ${outFile.canonicalPath} (${sw.toString().size()} bytes, ${verdicts.size()} verdicts, ${parseErrors.size()} parse errors)")
