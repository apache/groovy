#!/usr/bin/env bash
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------
commit=$2
cmd=$3

function version() {
    echo 1
}

function applicable() {
    echo "true"
}

function gettool() {
  pushd /tmp >/dev/null
  curl -s -o CodeNarc-2.0.0.tgz -LO https://github.com/smagill/codenarc-muse/blob/main/CodeNarc-2.0.0.tgz?raw=true
  tar xzf CodeNarc-2.0.0.tgz
  popd >/dev/null
}

function emit_results() {
  echo "$1"
}

function run() {
  gettool
  raw_results=$(/tmp/codenarc ./)
  emit_results "$raw_results"
}

if [[ "$cmd" = "run" ]] ; then
  run
fi
if [[ "$cmd" = "applicable" ]] ; then
  applicable
fi
if [[ "$cmd" = "version" ]] ; then
  version
fi
