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

if [ $# -lt 1 ]; then
  echo "Usage: $0 [semantic.version] <optional download location>"
  exit 1
fi

VERSION=$1
DOWNLOAD_LOCATION="${2:-downloads}"
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

VERSION=${VERSION#v} # in case someone prefixes a v

cd "${DOWNLOAD_LOCATION}/sdk"
ZIP_FILE=$(ls "apache-groovy-sdk-${VERSION}.zip" 2>/dev/null | head -n 1)

if [ -z "${ZIP_FILE}" ]; then
  echo "Error: Could not find apache-groovy-sdk-${VERSION}.zip in ${DOWNLOAD_LOCATION}/sdk"
  exit 1
fi

export GROOVY_GPG_HOME=$(mktemp -d)
cleanup() {
  rm -rf "${GROOVY_GPG_HOME}"
}
trap cleanup EXIT

echo "Verifying checksum..."
EXPECTED_HASH=$(cat apache-groovy-sdk-${VERSION}.zip.sha256 | tr -d '\r\n')
ACTUAL_HASH=$(shasum -a 256 apache-groovy-sdk-${VERSION}.zip | awk '{print $1}')
if [ "${EXPECTED_HASH}" != "${ACTUAL_HASH}" ]; then
    echo "❌ Checksum verification failed"
    exit 1
else
    echo "✅ Checksum Verified"
fi

echo "Importing GPG key to independent GPG home ..."
gpg --homedir "${GROOVY_GPG_HOME}" --import "${DOWNLOAD_LOCATION}/SVN_KEYS"
echo "✅ GPG Key Imported"

echo "Verifying GPG signature..."
gpg --homedir "${GROOVY_GPG_HOME}" --verify "apache-groovy-sdk-${VERSION}.zip.asc" "apache-groovy-sdk-${VERSION}.zip"
echo "✅ GPG Verified"

SRC_DIR="groovy-${VERSION}"

if [ -d "${SRC_DIR}" ]; then
  echo "Previous groovy directory found, removing..."
  rm -rf "${SRC_DIR}" || true
fi

echo "Extracting zip file..."
unzip -q "apache-groovy-sdk-${VERSION}.zip"

if [ ! -d "${SRC_DIR}" ]; then
  echo "Error: Expected extracted folder '${SRC_DIR}' not found."
  exit 1
fi

echo "Checking for required files existence..."
REQUIRED_FILES=("LICENSE" "NOTICE")

for FILE in "${REQUIRED_FILES[@]}"; do
  if [ ! -f "${SRC_DIR}/$FILE" ]; then
    echo "❌ Missing required file: $FILE"
    exit 1
  fi

  echo "✅ Found required file: $FILE"
done

echo "✅ All sdk distribution checks passed successfully for Apache Groovy ${VERSION}."
