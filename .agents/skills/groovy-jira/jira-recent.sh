#!/usr/bin/env bash
#
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.
#
# jira-recent.sh — list recently active open GROOVY issues, linked to their GitHub PRs.
#
# A vetted helper for a deterministic, rarely-changing operation (the
# ASF JIRA REST v2 search endpoint joined with `gh pr list`), shipped per
# the "Helper mechanisms and token economy" policy in AGENTS.md. The
# curl + gh + jq below IS the documented manual equivalent — run it by
# hand if you need to adapt it.
#
# Usage:
#   ./jira-recent.sh [days]          # default: 30
#
# Environment:
#   GROOVY_REPO   GitHub repo to match PRs against (default: apache/groovy)
#   PR_LIMIT      number of most recent PRs (any state) to scan (default: 500)
#
# A PR is linked to an issue when its title contains the issue key
# (GROOVY-NNNNN, or the branch-style GroovyNNNNN).
# Output groups the unresolved issues updated in the last <days> days:
#   - merged PR but issue still open (candidates for resolving)
#   - open PR
#   - no open PR (closed-unmerged PRs, if any, are still shown)
# followed by open PRs updated in the window that name none of the listed issues.
#
# ASF JIRA is anonymous-readable; `gh` must be authenticated for GitHub.
set -euo pipefail

DAYS="${1:-30}"
REPO="${GROOVY_REPO:-apache/groovy}"
PR_LIMIT="${PR_LIMIT:-500}"

for tool in curl jq gh; do
  command -v "${tool}" >/dev/null || { echo "$0: requires ${tool} on PATH" >&2; exit 1; }
done

TMP="$(mktemp -d)"
trap 'rm -rf "${TMP}"' EXIT

curl -fsSG 'https://issues.apache.org/jira/rest/api/2/search' \
  --data-urlencode "jql=project = GROOVY AND statusCategory != Done AND updated >= -${DAYS}d ORDER BY key DESC" \
  --data-urlencode 'maxResults=500' \
  --data-urlencode 'fields=summary,updated' \
  > "${TMP}/issues.json"

gh pr list --repo "${REPO}" --state all --limit "${PR_LIMIT}" \
  --json number,title,state,updatedAt \
  > "${TMP}/prs.json"

jq -r --argjson days "${DAYS}" --slurpfile prs "${TMP}/prs.json" '
  def prs: map("#\(.number)(\(.state))") | join(" ") | if . == "" then "-" else . end;
  def section($title; $rows):
    "== \($title) (\($rows | length))",
    ($rows[] | [.key, .updated, (.prs | prs), .summary[0:100]] | @tsv),
    "";

  ($prs[0] | map(. + {keys: [.title | ascii_upcase | scan("GROOVY-?[0-9]+") | sub("^GROOVY-?"; "GROOVY-")]})) as $p
  | [.issues[] | .key as $k
      | {key: $k, updated: .fields.updated[0:10], summary: (.fields.summary // ""),
         prs: [$p[] | select(any(.keys[]; . == $k))]}
      | . + {open: any(.prs[]; .state == "OPEN"), merged: any(.prs[]; .state == "MERGED")}
    ] as $rows
  | ($rows | map(.key)) as $listed
  | (now - $days * 86400 | strftime("%Y-%m-%d")) as $since
  | "Unresolved GROOVY issues updated since \($since): \($rows | length)",
    "",
    section("Merged PR, issue still open"; [$rows[] | select(.merged and (.open | not))]),
    section("Open PR"; [$rows[] | select(.open)]),
    section("No open PR"; [$rows[] | select((.open or .merged) | not)]),
    ([$p[] | select(.state == "OPEN" and .updatedAt[0:10] >= $since
                    and all(.keys[]; . as $k | $listed | index($k) | not))] as $orphans
     | "== Open PRs updated in window naming no listed issue (\($orphans | length))",
       ($orphans[] | ["#\(.number)", .updatedAt[0:10], .title] | @tsv))
' "${TMP}/issues.json"
