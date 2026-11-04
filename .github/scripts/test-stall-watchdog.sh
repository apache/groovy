#!/usr/bin/env bash
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
# Thread-dump the JVMs of a test run that has stopped making progress, so a
# CI step that would otherwise just hit its timeout records what was stuck.
# Used by the "Test with Gradle" steps in groovy-build-test.yml, started in
# the background next to ./gradlew:
#
#   .github/scripts/test-stall-watchdog.sh "$RUNNER_TEMP/stall-dumps" "$TARGET_JAVA_HOME" &
#   trap "kill $! 2>/dev/null" EXIT
#   ./gradlew test ...
#
# Progress is a new JUnit XML result under */build/test-results/, which Gradle
# writes as each test class completes. Console output is no signal: near the
# end of a good run only the root :test task is left, and it prints nothing
# for many minutes while classes keep completing. The watchdog arms when the
# first result appears, so compilation time never counts as a stall. After
# STALL_SECONDS without a new result it dumps every running JVM (the Gradle
# daemon and each test worker), then again every REPEAT_SECONDS while the
# stall lasts, up to MAX_DUMPS rounds: comparing rounds tells a deadlock from
# a slow test. It only observes and never kills anything; a false alarm (a
# legitimately long test class) costs one dump. Exits 0 in every case.
#
# Manual equivalent on a stuck runner: jps -l, then jcmd <pid> Thread.print -l
# for each GradleWorkerMain and the GradleDaemon.

out_dir="${1:?usage: test-stall-watchdog.sh <dump-dir> [target-java-home]}"
target_java_home="${2:-}"
STALL_SECONDS="${STALL_SECONDS:-600}"
REPEAT_SECONDS="${REPEAT_SECONDS:-300}"
MAX_DUMPS="${MAX_DUMPS:-3}"
POLL_SECONDS="${POLL_SECONDS:-30}"

mkdir -p "$out_dir" || exit 0
marker="$out_dir/.last-progress"

newest_results_since() { # prints one result file newer than $1, if any
    find . -path '*/build/test-results/*' -name 'TEST-*.xml' -newer "$1" -print -quit 2>/dev/null
}

any_results() {
    find . -path '*/build/test-results/*' -name 'TEST-*.xml' -print -quit 2>/dev/null
}

tool() { # first usable JDK tool: the test target's, then the Gradle JVM's, then PATH
    local name="$1" home
    for home in "$target_java_home" "$JAVA_HOME"; do
        [ -n "$home" ] || continue
        if [ -x "$home/bin/$name" ] || [ -x "$home/bin/$name.exe" ]; then
            echo "$home/bin/$name"; return
        fi
    done
    command -v "$name"
}

dump_all() {
    local round="$1" stamp jps jcmd file pid main stalled
    stamp=$(date -u +%Y%m%dT%H%M%SZ)
    stalled=$(( $(date +%s) - last_progress ))
    jps=$(tool jps); jcmd=$(tool jcmd)
    file="$out_dir/round$round-$stamp.txt"
    {
        echo "# stall round $round at $stamp: no new test result for ${stalled}s"
        echo "# most recently completed test classes:"
        find . -path '*/build/test-results/*' -name 'TEST-*.xml' -print 2>/dev/null \
            | xargs ls -t 2>/dev/null | head -10 | sed 's/^/#   /'
        echo "# JVMs:"
        "$jps" -l 2>&1 | sed 's/^/#   /'
    } > "$file"
    "$jps" -l 2>/dev/null | while read -r pid main; do
        case "$pid" in
            ""|*[!0-9]*) continue ;; # e.g. "Picked up JAVA_TOOL_OPTIONS: ..."
        esac
        case "$main" in
            *Jps*) continue ;;
        esac
        {
            echo
            echo "======== pid $pid $main ========"
            "$jcmd" "$pid" Thread.print -l 2>&1 || "$(tool jstack)" -l "$pid" 2>&1
        } >> "$file"
    done
    echo "::warning title=Test stall::No new test result for ${stalled}s; thread dumps (round $round) in the stall-dumps artifact"
}

# Arm on the first result, so build-logic and compilation never look like a stall.
until [ -n "$(any_results)" ]; do sleep "$POLL_SECONDS"; done
touch "$marker"
last_progress=$(date +%s)
rounds=0
next_dump=$(( last_progress + STALL_SECONDS ))

while :; do
    sleep "$POLL_SECONDS"
    now=$(date +%s)
    if [ -n "$(newest_results_since "$marker")" ]; then
        touch "$marker"
        last_progress=$now
        next_dump=$(( now + STALL_SECONDS ))
        continue
    fi
    if [ "$rounds" -lt "$MAX_DUMPS" ] && [ "$now" -ge "$next_dump" ]; then
        rounds=$(( rounds + 1 ))
        dump_all "$rounds"
        next_dump=$(( now + REPEAT_SECONDS ))
    fi
done
