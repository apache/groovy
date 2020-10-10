/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
while (true) {
    if (r == n) {
        return maxFlipsCount
    }
    int perm0 = perm1[0]
    int i = 0
    while (i < r) {
        int j = i + 1
        perm1[i] = perm1[j]
        i = j
    }
    perm1[r] = perm0

    count[r] = count[r] - 1
    if (count[r] > 0) {
        break
    }
    r++
}
