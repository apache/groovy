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
#    https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.
#
set -euo pipefail

if [ $# -lt 2 ]; then
  echo "Usage: $0 ['dev' or 'release'] [semantic.version] <optional download location>"
  exit 1
fi

DIST_TYPE=$1
VERSION=$2
DOWNLOAD_LOCATION="${3:-downloads}"

if [[ "${DIST_TYPE}" != "dev" && "${DIST_TYPE}" != "release" ]]; then
  echo "Error: DIST_TYPE must be either 'dev' or 'release', got '${DIST_TYPE}'"
  echo "Usage: $0 ['dev' or 'release'] [semantic.version] <optional download location>"
  exit 1
fi

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
CWD=$(pwd)
VERSION=${VERSION#v} # in case someone prefixes a v

cleanup() {
  echo "❌ Verification failed. ❌"
}
trap cleanup ERR

mkdir -p "${DOWNLOAD_LOCATION}"

echo "Downloading KEYS file ..."
curl -f -L -o "${DOWNLOAD_LOCATION}/SVN_KEYS" "https://dist.apache.org/repos/dist/release/groovy/KEYS"
echo "✅ KEYS Downloaded"

echo "Downloading Artifacts ..."
"${SCRIPT_DIR}/download-release-artifacts.sh" "${DIST_TYPE}" "${VERSION}" "${DOWNLOAD_LOCATION}"
echo "✅ Artifacts Downloaded"

echo "Verifying Source Distribution ..."
"${SCRIPT_DIR}/verify-source-distribution.sh" "${VERSION}" "${DOWNLOAD_LOCATION}"
echo "✅ Source Distribution Verified"

echo "Verifying Binary Distribution ..."
"${SCRIPT_DIR}/verify-binary-distribution.sh" "${VERSION}" "${DOWNLOAD_LOCATION}"
echo "✅ Binary Distribution Verified"

echo "Verifying Docs Distribution ..."
"${SCRIPT_DIR}/verify-docs-distribution.sh" "${VERSION}" "${DOWNLOAD_LOCATION}"
echo "✅ Docs Distribution Verified"

echo "Verifying SDK Distribution ..."
"${SCRIPT_DIR}/verify-sdk-distribution.sh" "${VERSION}" "${DOWNLOAD_LOCATION}"
echo "✅ SDK Distribution Verified"

echo "Using Java at ..."
which java
java -version


echo "Determining Gradle on PATH ..."
if GRADLE_CMD="$(command -v gradlew 2>/dev/null)"; then
    :   # found the wrapper on PATH
elif GRADLE_CMD="$(command -v gradle 2>/dev/null)"; then
    :   # fall back to system-wide Gradle
else
    echo "❌ ERROR: Neither gradlew nor gradle found on \$PATH." >&2
    exit 1
fi
# get rid of the path
GRADLE_CMD=$(basename "${GRADLE_CMD}")
echo "✅ Using Gradle command: ${GRADLE_CMD}"

if [ "${GRADLE_CMD}" = "gradle" ]; then
  echo "Bootstrap Gradle ..."
  cd "${DOWNLOAD_LOCATION}/src/groovy-${VERSION}"
  "${GRADLE_CMD}" -p bootstrap
  echo "✅ Gradle Bootstrapped"
else
  echo "Gradle Bootstrap not needed ..."
fi

echo "Applying License Audit ..."
cd "${DOWNLOAD_LOCATION}/src/groovy-${VERSION}"
./gradlew rat
echo "✅ RAT passed"

echo "✅✅✅ Automatic verification finished."
