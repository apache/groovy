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
# safety-prescreen.sh — flag obvious dangerous constructs in an adapted
# reproducer BEFORE it is executed.
#
# This is a SCREENING AID, not a sandbox and not a decision-maker. It is
# deterministic and intentionally shallow: it cannot see obfuscation,
# reflection, base64'd payloads, or harm delivered transitively through
# an @Grab'd dependency. Its only job is to annotate the human-review
# gate ("⚠ flagged: …; extra caution / consider a sandboxed run") so the
# reviewer reads the code with the right level of suspicion. A clean
# result is NOT an assurance the code is safe — the human still decides.
# Shipped as a vetted helper per AGENTS.md "Helper mechanisms and token
# economy" so the pattern set is not re-derived (or mis-derived) per run.
#
# Usage:   ./safety-prescreen.sh <reproducer-file>
# Output:  one "CATEGORY  file:line  <matched line>" row per hit, then a
#          final  "PRESCREEN: FLAGGED <categories>"  or  "PRESCREEN: clean"
# Exit:    always 0 — the verdict is the human's, not this script's.
set -euo pipefail

FILE="${1:?usage: $0 <reproducer-file>}"
[[ -r "$FILE" ]] || { echo "PRESCREEN: cannot read $FILE" >&2; exit 0; }

# category => extended-regex of concern. Conservative: prefer a false
# flag (human glances, moves on) over a missed sink.
scan() { grep -nE "$2" -- "$FILE" 2>/dev/null | sed "s#^#$1  $FILE:#"; }

HITS=""
add() { local out; out="$(scan "$1" "$2")" || true; if [[ -n "$out" ]]; then echo "$out"; HITS="$HITS $1"; fi; }

add PROCESS-EXEC      '\.execute\(|Runtime\.getRuntime|ProcessBuilder|ProcessGroovyMethods|\bexecvp?\b'
add FILESYSTEM-WRITE  '\.deleteDir\(|\.delete\(\)|Files\.(delete|write|move|copy)|new +File *\( *["'"'"']/|FileOutputStream|\.bytes *=|\.text *=|\.withWriter|\.append\('
add SECRET-READ       '\.ssh|\.aws|\.gnupg|/etc/passwd|/etc/shadow|id_rsa|id_ed25519|credentials|\.netrc|System\.getenv|user\.home'
add NETWORK           'new +URL *\(|HttpURLConnection|URLConnection|\bSocket\b|\.toURL\(\)|HttpClient|RESTClient|\.openConnection\('
add DEPENDENCY-PULL   '@Grab\b|@GrabResolver|@GrabConfig|Grape\.'
add DYNAMIC-CODE      '\bEval\.|\bGroovyShell\b|\bGroovyClassLoader\b|\.evaluate\(|System\.load(Library)?\(|@ASTTest|getClass\(\)\.classLoader'
add JVM-CONTROL       'System\.exit|Runtime\.[a-zA-Z]*\.?halt|setSecurityManager|addShutdownHook'

if [[ -n "$HITS" ]]; then
  # shellcheck disable=SC2086
  echo "PRESCREEN: FLAGGED$(printf ' %s' $(echo $HITS | tr ' ' '\n' | sort -u))"
else
  echo "PRESCREEN: clean (shallow check — not an assurance; the human still reviews the code)"
fi
exit 0
