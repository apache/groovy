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
# jira-issue.sh — fetch one JIRA issue's full content and emit description.md.
#
# A vetted helper for a deterministic, rarely-changing operation (the
# ASF JIRA REST v2 issue endpoint), shipped per the "Helper mechanisms
# and token economy" policy in AGENTS.md. The curl + jq below IS the
# documented manual equivalent — run it by hand if you need to adapt it.
#
# Usage:
#   ./jira-issue.sh GROOVY-NNNNN [output-dir]
#
# Creates in <output-dir> (default: ./GROOVY-NNNNN/):
#   issue.json       raw JIRA REST response
#   description.md   description + comments + key metadata, rendered as markdown
#
# Prints attachment URLs at the end for manual download (not auto-fetched).
#
# ASF JIRA is anonymous-readable; no auth needed.
set -euo pipefail

KEY="${1:?usage: $0 GROOVY-NNNNN [output-dir]}"
OUT="${2:-./${KEY}}"
mkdir -p "${OUT}"

curl -fsS "https://issues.apache.org/jira/rest/api/2/issue/${KEY}?expand=names" \
  > "${OUT}/issue.json"

jq -r '
  "# " + .key + " — " + (.fields.summary // ""),
  "",
  "| Field | Value |",
  "|---|---|",
  "| Type | " + (.fields.issuetype.name // "?") + " |",
  "| Status | " + (.fields.status.name // "?") + " |",
  "| Priority | " + (.fields.priority.name // "?") + " |",
  "| Resolution | " + ((.fields.resolution.name // "(unresolved)")) + " |",
  "| Created | " + (.fields.created // "?")[0:10] + " |",
  "| Updated | " + (.fields.updated // "?")[0:10] + " |",
  "| Reporter | " + ((.fields.reporter.displayName // .fields.reporter.name // "?")) + " |",
  "| Assignee | " + ((.fields.assignee.displayName // .fields.assignee.name // "(unassigned)")) + " |",
  "| Components | " + (((.fields.components // []) | map(.name) | join(", ")) | if . == "" then "(none)" else . end) + " |",
  "| Affects Version/s | " + (((.fields.versions // []) | map(.name) | join(", ")) | if . == "" then "(none)" else . end) + " |",
  "| Fix Version/s | " + (((.fields.fixVersions // []) | map(.name) | join(", ")) | if . == "" then "(none)" else . end) + " |",
  "| Labels | " + (((.fields.labels // []) | join(", ")) | if . == "" then "(none)" else . end) + " |",
  "| Attachments | " + (((.fields.attachment // []) | map(.filename) | join(", ")) | if . == "" then "(none)" else . end) + " |",
  "",
  "---",
  "",
  "## Description",
  "",
  (.fields.description // "_(no description)_"),
  "",
  "---",
  "",
  "## Comments (" + (((.fields.comment.comments // []) | length) | tostring) + ")",
  "",
  ((.fields.comment.comments // []) | map(
    "### " + (.author.displayName // .author.name // "?") + " — " + (.created[0:10]) + "\n\n" + .body
  ) | join("\n\n---\n\n"))
' "${OUT}/issue.json" > "${OUT}/description.md"

echo "Wrote ${OUT}/description.md and ${OUT}/issue.json"

ATTACH_COUNT=$(jq '(.fields.attachment // []) | length' "${OUT}/issue.json")
if [[ "${ATTACH_COUNT}" -gt 0 ]]; then
  echo ""
  echo "Attachments (${ATTACH_COUNT}) — download manually if relevant:"
  jq -r '.fields.attachment[] | "  " + .filename + "  (" + (.size|tostring) + " bytes)  →  " + .content' "${OUT}/issue.json"
fi
