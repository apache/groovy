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
set -e

if [ $# -lt 2 ]; then
  echo "Usage: $0 ['dev' or 'release'] [semantic.version] <optional download location>"
  exit 1
fi

PROJECT_NAME='groovy'
REPO_NAME='apache/groovy'
DIST_TYPE=$1
VERSION=$2
DOWNLOAD_LOCATION="${3:-downloads}"

if [[ "${DIST_TYPE}" != "dev" && "${DIST_TYPE}" != "release" ]]; then
  echo "Error: DIST_TYPE must be either 'dev' or 'release', got '${DIST_TYPE}'"
  echo "Usage: $0 ['dev' or 'release'] [version] <optional download location>"
  exit 1
fi

echo "Downloading files to ${DOWNLOAD_LOCATION}"
mkdir -p "${DOWNLOAD_LOCATION}"
mkdir -p "${DOWNLOAD_LOCATION}/src"
mkdir -p "${DOWNLOAD_LOCATION}/binary"
mkdir -p "${DOWNLOAD_LOCATION}/docs"
mkdir -p "${DOWNLOAD_LOCATION}/sdk"

VERSION=${VERSION#v} # in case someone prefixes a v


# download into subdirs because they unpack into the same directory name
# Source distro

echo "Downloading SVN source release files"
curl -f -L -o "${DOWNLOAD_LOCATION}/src/apache-${PROJECT_NAME}-src-${VERSION}.zip" "https://dist.apache.org/repos/dist/${DIST_TYPE}/groovy/${VERSION}/sources/apache-${PROJECT_NAME}-src-${VERSION}.zip"
curl -f -L -o "${DOWNLOAD_LOCATION}/src/apache-${PROJECT_NAME}-src-${VERSION}.zip.asc" "https://dist.apache.org/repos/dist/${DIST_TYPE}/groovy/${VERSION}/sources/apache-${PROJECT_NAME}-src-${VERSION}.zip.asc"
curl -f -L -o "${DOWNLOAD_LOCATION}/src/apache-${PROJECT_NAME}-src-${VERSION}.zip.sha256" "https://dist.apache.org/repos/dist/${DIST_TYPE}/groovy/${VERSION}/sources/apache-${PROJECT_NAME}-src-${VERSION}.zip.sha256"

# Binary distro

echo "Downloading SVN distribution binary files"
curl -f -L -o "${DOWNLOAD_LOCATION}/binary/apache-${PROJECT_NAME}-binary-${VERSION}.zip" "https://dist.apache.org/repos/dist/${DIST_TYPE}/groovy/${VERSION}/distribution/apache-${PROJECT_NAME}-binary-${VERSION}.zip"
curl -f -L -o "${DOWNLOAD_LOCATION}/binary/apache-${PROJECT_NAME}-binary-${VERSION}.zip.asc" "https://dist.apache.org/repos/dist/${DIST_TYPE}/groovy/${VERSION}/distribution/apache-${PROJECT_NAME}-binary-${VERSION}.zip.asc"
curl -f -L -o "${DOWNLOAD_LOCATION}/binary/apache-${PROJECT_NAME}-binary-${VERSION}.zip.sha256" "https://dist.apache.org/repos/dist/${DIST_TYPE}/groovy/${VERSION}/distribution/apache-${PROJECT_NAME}-binary-${VERSION}.zip.sha256"

echo "Downloading SVN distribution docs files"
curl -f -L -o "${DOWNLOAD_LOCATION}/docs/apache-${PROJECT_NAME}-docs-${VERSION}.zip" "https://dist.apache.org/repos/dist/${DIST_TYPE}/groovy/${VERSION}/distribution/apache-${PROJECT_NAME}-docs-${VERSION}.zip"
curl -f -L -o "${DOWNLOAD_LOCATION}/docs/apache-${PROJECT_NAME}-docs-${VERSION}.zip.asc" "https://dist.apache.org/repos/dist/${DIST_TYPE}/groovy/${VERSION}/distribution/apache-${PROJECT_NAME}-docs-${VERSION}.zip.asc"
curl -f -L -o "${DOWNLOAD_LOCATION}/docs/apache-${PROJECT_NAME}-docs-${VERSION}.zip.sha256" "https://dist.apache.org/repos/dist/${DIST_TYPE}/groovy/${VERSION}/distribution/apache-${PROJECT_NAME}-docs-${VERSION}.zip.sha256"

echo "Downloading SVN distribution sdk files"
curl -f -L -o "${DOWNLOAD_LOCATION}/sdk/apache-${PROJECT_NAME}-sdk-${VERSION}.zip" "https://dist.apache.org/repos/dist/${DIST_TYPE}/groovy/${VERSION}/distribution/apache-${PROJECT_NAME}-sdk-${VERSION}.zip"
curl -f -L -o "${DOWNLOAD_LOCATION}/sdk/apache-${PROJECT_NAME}-sdk-${VERSION}.zip.asc" "https://dist.apache.org/repos/dist/${DIST_TYPE}/groovy/${VERSION}/distribution/apache-${PROJECT_NAME}-sdk-${VERSION}.zip.asc"
curl -f -L -o "${DOWNLOAD_LOCATION}/sdk/apache-${PROJECT_NAME}-sdk-${VERSION}.zip.sha256" "https://dist.apache.org/repos/dist/${DIST_TYPE}/groovy/${VERSION}/distribution/apache-${PROJECT_NAME}-sdk-${VERSION}.zip.sha256"
