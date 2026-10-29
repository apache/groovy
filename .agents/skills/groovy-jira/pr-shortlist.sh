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
# pr-shortlist.sh — group open GitHub PRs by how close they are to being merged.
#
# A vetted helper for a deterministic, rarely-changing operation (one
# GitHub GraphQL query over the open PRs), shipped per the "Helper
# mechanisms and token economy" policy in AGENTS.md. The gh + jq below
# IS the documented manual equivalent — run it by hand if you need to
# adapt it.
#
# Usage:
#   ./pr-shortlist.sh [max-size]     # default: 600 (added + deleted lines)
#
# Environment:
#   GROOVY_REPO   GitHub repo to list PRs from (default: apache/groovy)
#
# Each open PR lands in the first group that matches, in this order:
#   - draft                          (not shown, only counted)
#   - merge conflicts                (not shown, only counted)
#   - open discussion                (unresolved review threads or changes requested)
#   - CI failing
#   - CI pending                     (also: no CI result at all)
#   - approved, commits since        (approved, but pushed to after the last approval)
#   - approved, ready to merge
#   - larger than max-size, no approval
#   - small, yours, needs a reviewer (authored by the authenticated gh user)
#   - small, needs your review       (authored by someone else)
# Groups are printed nearest-to-merge first. "Yours" means the `gh` viewer.
#
# Read-only; `gh` must be authenticated for GitHub.
set -euo pipefail

MAX_SIZE="${1:-600}"
REPO="${GROOVY_REPO:-apache/groovy}"

for tool in gh jq; do
  command -v "${tool}" >/dev/null || { echo "$0: requires ${tool} on PATH" >&2; exit 1; }
done

TMP="$(mktemp -d)"
trap 'rm -rf "${TMP}"' EXIT

gh api graphql --paginate \
  -F owner="${REPO%%/*}" -F name="${REPO##*/}" \
  -f query='
query($owner: String!, $name: String!, $endCursor: String) {
  viewer { login }
  repository(owner: $owner, name: $name) {
    pullRequests(states: OPEN, first: 25, after: $endCursor) {
      pageInfo { hasNextPage endCursor }
      nodes {
        number title isDraft additions deletions mergeable
        author { login }
        commits(last: 1) { nodes { commit { committedDate statusCheckRollup { state } } } }
        reviews(last: 20) { nodes { author { login } state submittedAt } }
        reviewThreads(first: 50) { nodes { isResolved } }
      }
    }
  }
}' > "${TMP}/pages.json"

jq -rs --argjson max "${MAX_SIZE}" '
  def groups: [
    ["merge",      "Approved, ready to merge"],
    ["recheck",    "Approved, but commits pushed since the last approval"],
    ["yours",      "Small, yours, needs a reviewer"],
    ["theirs",     "Small, needs your review"],
    ["discussion", "Open discussion (unresolved threads or changes requested)"],
    ["ci-failing", "CI failing"],
    ["ci-pending", "CI pending or missing"],
    ["large",      "Larger than \($max) lines, no approval"]
  ];

  (.[0].data.viewer.login) as $me
  | [.[].data.repository.pullRequests.nodes[]
      | (.commits.nodes[0].commit) as $c
      | ($c.statusCheckRollup.state // "NONE") as $ci
      | [.reviews.nodes[] | select(.state == "APPROVED")] as $appr
      | ([.reviews.nodes[] | select(.state == "CHANGES_REQUESTED")] | length) as $chg
      | ([.reviewThreads.nodes[] | select(.isResolved | not)] | length) as $unres
      | (.additions + .deletions) as $size
      | {number, title, size: $size, ci: $ci, unres: $unres,
         author: .author.login,
         approvers: ([$appr[].author.login] | unique | join(",")),
         group: (if .isDraft then "draft"
                 elif .mergeable == "CONFLICTING" then "conflicting"
                 elif $chg > 0 or $unres > 0 then "discussion"
                 elif $ci == "FAILURE" or $ci == "ERROR" then "ci-failing"
                 elif $ci != "SUCCESS" then "ci-pending"
                 elif ($appr | length) > 0 then
                   (if ([$appr[].submittedAt] | max) < $c.committedDate then "recheck" else "merge" end)
                 elif $size > $max then "large"
                 elif .author.login == $me then "yours"
                 else "theirs" end)}
    ] as $prs
  | "Open PRs in \(env.GROOVY_REPO // "apache/groovy") for \($me): \($prs | length)",
    "",
    (groups[] as [$key, $title]
      | [$prs[] | select(.group == $key)] | sort_by(.size) as $rows
      | select($rows | length > 0)
      | "== \($title) (\($rows | length))",
        ($rows[] | ["#\(.number)", .author, "\(.size) lines", .ci,
                    (if .approvers == "" then "-" else "approved:\(.approvers)" end),
                    "unresolved:\(.unres)", .title[0:100]] | @tsv),
        ""),
    "Not shown: \([$prs[] | select(.group == "draft")] | length) draft, \([$prs[] | select(.group == "conflicting")] | length) with merge conflicts"
' "${TMP}/pages.json"
