#!/usr/bin/env python3
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Per-commit JMH group summary used by .github/workflows/groovy-jmh{,-classic}.yml.
# For each benchmark in the current run's JMH JSON, computes the ratio vs the trailing
# 90-day mean from gh-pages, then geomean within bench/core/grails. Mirrors the same
# normalisation as subprojects/performance/dashboard/jmh-summary.html so the numbers
# are directly comparable with the daily dashboard.

import argparse
import json
import math
import os
import re
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

DAY_MS = 86_400_000
WINDOW_MS = 90 * DAY_MS

# (group label, CI-split parts whose data.js makes up that group)
GROUPS = [
    ('bench',  ['bench']),
    ('core',   ['core-ag', 'core-hz']),
    ('grails', ['grails-ad', 'grails-ez']),
]

BASELINE_URL = 'https://apache.github.io/groovy/dev/bench/jmh/{part}/{mode}/data.js'


def is_higher_better(unit):
    # ops, ops/ms, ops/s -> higher = faster; ms/op, us/op, ns/op, s/op -> invert.
    return bool(re.match(r'^ops\b', unit or ''))


def normalize(value, baseline, unit):
    return value / baseline if is_higher_better(unit) else baseline / value


def load_results(results_dir):
    out = {}
    for p in sorted(Path(results_dir).rglob('results-*.json')):
        m = re.match(r'results-(.+)\.json$', p.name)
        if not m:
            continue
        try:
            data = json.loads(p.read_text())
        except (OSError, json.JSONDecodeError) as e:
            print(f'WARN: could not parse {p}: {e}', file=sys.stderr)
            continue
        bench_map = {}
        for entry in data:
            name = entry.get('benchmark')
            metric = entry.get('primaryMetric') or {}
            value = metric.get('score')
            unit = metric.get('scoreUnit', '') or ''
            if name and isinstance(value, (int, float)) and value > 0:
                bench_map[name] = (float(value), unit)
        if bench_map:
            out[m.group(1)] = bench_map
    return out


def fetch_baseline(part, mode):
    url = BASELINE_URL.format(part=part, mode=mode)
    try:
        with urllib.request.urlopen(url, timeout=30) as resp:
            text = resp.read().decode('utf-8')
    except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError) as e:
        print(f'WARN: could not fetch {url}: {e}', file=sys.stderr)
        return []
    text = re.sub(r'^\s*window\.BENCHMARK_DATA\s*=\s*', '', text)
    text = re.sub(r';\s*$', '', text)
    try:
        return (json.loads(text).get('entries') or {}).get('Benchmark', [])
    except json.JSONDecodeError as e:
        print(f'WARN: could not parse {url}: {e}', file=sys.stderr)
        return []


def baseline_means(entries, now_ms, window_ms):
    sums, counts = {}, {}
    for entry in entries:
        if now_ms - (entry.get('date') or 0) > window_ms:
            continue
        for b in entry.get('benches') or []:
            name = b.get('name')
            value = b.get('value')
            if name and isinstance(value, (int, float)) and value > 0:
                sums[name] = sums.get(name, 0.0) + value
                counts[name] = counts.get(name, 0) + 1
    return {n: sums[n] / counts[n] for n in sums}


def geomean(values):
    return math.exp(sum(math.log(v) for v in values) / len(values)) if values else None


def compute_group_scores(results_by_part, mode, now_ms):
    rows = []
    for label, parts in GROUPS:
        ratios = []
        for part in parts:
            current = results_by_part.get(part) or {}
            if not current:
                continue
            means = baseline_means(fetch_baseline(part, mode), now_ms, WINDOW_MS)
            for name, (value, unit) in current.items():
                base = means.get(name)
                if not base or base <= 0:
                    continue
                r = normalize(value, base, unit)
                if r > 0 and math.isfinite(r):
                    ratios.append(r)
        rows.append((label, geomean(ratios), len(ratios)))
    return rows


def render_markdown(rows, mode, commit_sha, marker):
    short = (commit_sha or '')[:7] or 'unknown'
    lines = [
        f'### JMH summary — {mode} (commit `{short}`)',
        '',
        'Speedup vs trailing 90-day baseline on gh-pages. Higher = faster.',
        '`1.00` = in line with history. Per-benchmark ratio, geomean within group.',
        'Time-per-op units inverted so direction is consistent.',
        '',
        '| Group  | Speedup | n |',
        '|--------|---------|---|',
    ]
    any_data = False
    for label, score, n in rows:
        if score is None:
            lines.append(f'| {label} | _no overlap with baseline_ | {n} |')
        else:
            any_data = True
            lines.append(f'| {label} | {score:.3f} × | {n} |')
    lines += [
        '',
        f'<sub>Baseline: <code>dev/bench/jmh/&lt;part&gt;/{mode}/data.js</code> on '
        'gh-pages, trailing 90 days. '
        '<a href="https://apache.github.io/groovy/dev/bench/jmh/summary.html">Daily dashboard</a> · '
        '<a href="https://apache.github.io/groovy/dev/bench/jmh/">Per-suite raw data</a></sub>',
        '',
        marker,
    ]
    return '\n'.join(lines), any_data


def gh_request(url, token, method='GET', body=None):
    data = json.dumps(body).encode('utf-8') if body is not None else None
    req = urllib.request.Request(url, data=data, method=method, headers={
        'Authorization': f'Bearer {token}',
        'Accept': 'application/vnd.github+json',
        'X-GitHub-Api-Version': '2022-11-28',
        **({'Content-Type': 'application/json'} if data else {}),
    })
    with urllib.request.urlopen(req) as resp:
        payload = resp.read()
        return json.loads(payload) if payload else None, resp.headers.get('Link', '')


def find_existing_comment(repo, pr, marker, token):
    url = f'https://api.github.com/repos/{repo}/issues/{pr}/comments?per_page=100'
    while url:
        comments, link = gh_request(url, token)
        for c in comments or []:
            if marker in (c.get('body') or ''):
                return c['id']
        m = re.search(r'<([^>]+)>;\s*rel="next"', link)
        url = m.group(1) if m else None
    return None


def upsert_pr_comment(repo, pr, body, marker, token):
    existing = find_existing_comment(repo, pr, marker, token)
    if existing:
        gh_request(f'https://api.github.com/repos/{repo}/issues/comments/{existing}',
                   token, 'PATCH', {'body': body})
    else:
        gh_request(f'https://api.github.com/repos/{repo}/issues/{pr}/comments',
                   token, 'POST', {'body': body})


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--mode', required=True, choices=['indy', 'classic'])
    ap.add_argument('--results-dir', required=True)
    ap.add_argument('--commit', default=os.environ.get('GITHUB_SHA', ''))
    ap.add_argument('--pr-number', default='')
    ap.add_argument('--repo', default=os.environ.get('GITHUB_REPOSITORY', ''))
    args = ap.parse_args()

    results_by_part = load_results(args.results_dir)
    if not results_by_part:
        print(f'No results-*.json found under {args.results_dir}', file=sys.stderr)
        return 1

    rows = compute_group_scores(results_by_part, args.mode, int(time.time() * 1000))
    marker = f'<!-- jmh-summary:{args.mode} -->'
    body, any_data = render_markdown(rows, args.mode, args.commit, marker)
    print(body)

    pr = (args.pr_number or '').strip()
    token = os.environ.get('GITHUB_TOKEN', '')
    if any_data and pr and pr != 'null' and token and args.repo:
        try:
            upsert_pr_comment(args.repo, pr, body, marker, token)
            print(f'Posted/updated PR #{pr} comment', file=sys.stderr)
        except (urllib.error.URLError, urllib.error.HTTPError) as e:
            print(f'WARN: could not post PR comment: {e}', file=sys.stderr)

    return 0


if __name__ == '__main__':
    sys.exit(main())
