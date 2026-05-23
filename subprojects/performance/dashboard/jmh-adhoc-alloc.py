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
# Used by .github/workflows/groovy-jmh-adhoc.yml.
#
# JMH's own JSON only feeds the *primary* metric (throughput) into
# github-action-benchmark's `jmh` tool. The whole point of the adhoc
# "fat-free" benchmarks is allocation, captured by the `gc` profiler as the
# `gc.alloc.rate.norm` secondary metric (bytes/op). This script lifts that
# secondary metric out of a JMH results.json into the lightweight
# `customSmallerIsBetter` shape, so allocation can be tracked as its own
# dashboard series (lower = better) alongside throughput.

import argparse
import json
import sys
from pathlib import Path

ALLOC_KEY = 'gc.alloc.rate.norm'


def short_name(benchmark, params):
    # org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith -> findG_lambdasCurryWith
    leaf = benchmark.rsplit('.', 1)[-1]
    size = (params or {}).get('size')
    return f'{leaf} (size={size})' if size is not None else leaf


def convert(results):
    out = []
    for entry in results:
        sec = entry.get('secondaryMetrics', {})
        alloc = sec.get(ALLOC_KEY)
        if not alloc or 'score' not in alloc:
            continue
        out.append({
            'name': short_name(entry.get('benchmark', '?'), entry.get('params')),
            'unit': 'B/op',
            'value': round(float(alloc['score']), 1),
            # extra is rendered by github-action-benchmark on hover
            'extra': f"{alloc.get('scoreUnit', 'B/op')} (gc.alloc.rate.norm)",
        })
    out.sort(key=lambda r: r['name'])
    return out


def main():
    ap = argparse.ArgumentParser(description='Extract gc.alloc.rate.norm from a JMH results.json')
    ap.add_argument('input', help='JMH results.json (with gc profiler enabled)')
    ap.add_argument('output', help='destination customSmallerIsBetter JSON')
    args = ap.parse_args()

    results = json.loads(Path(args.input).read_text())
    rows = convert(results)
    if not rows:
        print(f'ERROR: no "{ALLOC_KEY}" secondary metric found in {args.input}; '
              'was the run launched with -PjmhProfilers=gc?', file=sys.stderr)
        return 1
    Path(args.output).write_text(json.dumps(rows, indent=2) + '\n')
    print(f'Wrote {len(rows)} allocation rows to {args.output}')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
