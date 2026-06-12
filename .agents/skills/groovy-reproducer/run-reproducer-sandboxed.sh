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
# run-reproducer-sandboxed.sh — run ONE script / self-contained @Test
# reproducer inside a locked-down Docker container against a *pinned
# released* Groovy (Option A). Invoked by the step-6 safety gate in
# SKILL.md when the human chooses "run-sandboxed".
#
# This is BLAST-RADIUS REDUCTION, not a jail. Container escape exists
# (shared kernel; the Docker daemon is host-root-equivalent). It sits
# *behind* the pre-screen + human gate, never instead of them. It does
# NOT cover: the current master build (that is Option B — a different
# path), @Grab/network (network is disabled), seccomp/AppArmor beyond
# Docker defaults, rootless Docker, or multi-file/project reproducers.
#
# The `docker run` below IS the documented manual equivalent (per
# AGENTS.md "Helper mechanisms and token economy"): run it by hand if
# you need to adapt it.
#
# Usage:
#   run-reproducer-sandboxed.sh --line {3|4|5} <reproducer-file> [--timeout S]
#   run-reproducer-sandboxed.sh --image <ref>  <reproducer-file> [--timeout S]
#
#   --line N     latest *released* Groovy of major line N (3|4|5),
#                resolved via the pin table below. There is no 6.x
#                entry: the community Docker Official images do not
#                publish 6.x alpha/beta; testing 6.x / master
#                (6.0.0-SNAPSHOT) is the separate local-build path
#                (Option B), which this script refuses.
#   --image REF  explicit image ref/digest, bypassing the table.
#
# Records, on stderr, the concrete image AND its sha256 digest so the
# caller can stamp verdict.json (`rev` = the resolved Groovy version,
# never the moving "latest" label; `safety_review.isolation` = sandbox).
set -euo pipefail

# ---------------------------------------------------------------------------
# Image pin table.
#
# Source: the Docker Official `groovy` image (https://hub.docker.com/_/groovy),
# built by the community project https://github.com/groovy/docker-groovy.
# Apache Groovy does NOT publish any images under apache/groovy. The
# jdk21 variant is used for all lines (per the docker-groovy
# groovy-N/jdk21/Dockerfile layout). There is deliberately no 6.x line:
# the project does not publish alpha/beta images; 6.x/master is Option B.
#
# Tags are mutable — these pin the *current* latest patch per line; the
# run resolves and records the immutable sha256 (RESOLVED_DIGEST) so the
# verdict is reproducible regardless. Bump these as new patch releases
# publish (a deliberate, reviewed edit — do not auto-track "latest").
# Override per-run with --image <ref> for a specific patch/JDK/digest.
# ---------------------------------------------------------------------------
RATIFIED_IMAGE_3='groovy:3.0.25-jdk21'
RATIFIED_IMAGE_4='groovy:4.0.32-jdk21'
RATIFIED_IMAGE_5='groovy:5.0.6-jdk21'

TIMEOUT=60
LINE=''
IMAGE=''
FILE=''

die() { echo "run-reproducer-sandboxed: $*" >&2; exit 2; }

while [[ $# -gt 0 ]]; do
  case "$1" in
    --line)    LINE="${2:?--line needs a value}"; shift 2 ;;
    --image)   IMAGE="${2:?--image needs a value}"; shift 2 ;;
    --timeout) TIMEOUT="${2:?--timeout needs a value}"; shift 2 ;;
    -h|--help) awk '/^# run-reproducer-sandboxed\.sh/,/^set -euo pipefail/' "$0" | grep '^#' | sed 's/^# \{0,1\}//'; exit 0 ;;
    -*)        die "unknown option: $1" ;;
    *)         FILE="$1"; shift ;;
  esac
done

[[ -n "$FILE" ]]      || die "no reproducer file given (see --help)"
[[ -r "$FILE" ]]      || die "cannot read reproducer file: $FILE"
command -v docker >/dev/null 2>&1 || die "docker not found on PATH — this is the Option-A sandbox path and requires Docker"

# Reject master / SNAPSHOT here — that is Option B (mounted fresh build),
# a different path. Never silently substitute (no-silent-fallback rule).
# Reject master / SNAPSHOT / 6.x here — there is no released community
# image for those; that is Option B (mounted fresh build), a different
# path. Never silently substitute (no-silent-fallback rule).
case "${LINE}" in
  m|master|snapshot|SNAPSHOT) die "--line '$LINE' is the master build (Option B), not this script. Run the local-build path instead." ;;
  6) die "no community-published release image for Groovy 6.x (no alpha/beta images on hub.docker.com/_/groovy). Test 6.x/master via the local-build path (Option B)." ;;
esac

if [[ -z "$IMAGE" ]]; then
  [[ -n "$LINE" ]] || die "need --line {3|4|5} or --image <ref>"
  case "$LINE" in
    3) IMAGE="$RATIFIED_IMAGE_3" ;;
    4) IMAGE="$RATIFIED_IMAGE_4" ;;
    5) IMAGE="$RATIFIED_IMAGE_5" ;;
    *) die "--line must be one of 3 4 5 (got '$LINE')" ;;
  esac
fi

# Fail fast if the image does not exist / is unreachable (Docker images
# lag releases; surface that, do not substitute another line).
echo "run-reproducer-sandboxed: pulling ${IMAGE} ..." >&2
docker pull "$IMAGE" >&2 || die "image not available: ${IMAGE} (Docker publish often lags a release; do not substitute another version — escalate to Option B or an explicit --image)"

DIGEST="$(docker image inspect --format '{{ if .RepoDigests }}{{ index .RepoDigests 0 }}{{ end }}' "$IMAGE" 2>/dev/null || true)"
echo "RESOLVED_IMAGE=${IMAGE}" >&2
echo "RESOLVED_DIGEST=${DIGEST:-<none>}" >&2

SCRATCH="$(cd "$(dirname "$FILE")" && pwd)"
BASE="$(basename "$FILE")"

# Hardened, ephemeral. Load-bearing: --network none (no exfil/pull),
# --read-only + tmpfs (no persistence), only the throwaway scratch dir
# mounted rw, no $HOME, no docker socket. The rest are one-flag wins.
set -x
timeout "${TIMEOUT}" docker run --rm \
  --network none \
  --read-only --tmpfs /tmp \
  --env HOME=/tmp \
  --volume "${SCRATCH}:/work:rw" \
  --workdir /work \
  --cap-drop=ALL --security-opt=no-new-privileges \
  --pids-limit=256 --memory=1g --memory-swap=1g --cpus=2 \
  "$IMAGE" \
  groovy "/work/${BASE}"
rc=$?
set +x
exit $rc
