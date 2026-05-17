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
# jira-search.sh — run a JQL query against ASF JIRA and print a candidate list.
#
# A vetted helper for a deterministic, rarely-changing operation (the
# ASF JIRA REST v2 search endpoint), shipped per the "Helper mechanisms
# and token economy" policy in AGENTS.md so agents and contributors do
# not re-derive the call every run. The curl invocation below IS the
# documented manual equivalent — run it by hand if you need to adapt it.
#
# Usage:
#   ./jira-search.sh '<JQL>' [max-results]
#
# Examples:
#   ./jira-search.sh 'project = GROOVY AND statusCategory != Done AND created < "2020-01-01" ORDER BY created ASC' 25
#   ./jira-search.sh 'project = GROOVY AND statusCategory != Done AND affectedVersion = "2.4.21" ORDER BY priority DESC' 25
#   ./jira-search.sh 'project = GROOVY AND statusCategory != Done AND component is EMPTY ORDER BY created DESC' 25
#
# ASF JIRA is anonymous-readable; no auth needed.
#
# Output (tab-separated): KEY  CREATED  UPDATED  TYPE  ATTACH  COMP  SUMMARY
set -euo pipefail

JQL="${1:?usage: $0 \"<JQL>\" [max-results]}"
MAX="${2:-25}"

curl -fsSG 'https://issues.apache.org/jira/rest/api/2/search' \
  --data-urlencode "jql=${JQL}" \
  --data-urlencode "maxResults=${MAX}" \
  --data-urlencode 'fields=summary,created,updated,attachment,components,issuetype' \
  | jq -r '
      .issues[] |
      [
        .key,
        (.fields.created  // "")[0:10],
        (.fields.updated  // "")[0:10],
        (.fields.issuetype.name // "?"),
        ("att=" + ((.fields.attachment // []) | length | tostring)),
        ((.fields.components // []) | map(.name) | join("|") | if . == "" then "(none)" else . end),
        (.fields.summary // "")
      ] | @tsv
    '
