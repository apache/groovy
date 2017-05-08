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
outer:
for (def i in [1, 2]) {
    for (def j in [1, 2, 3, 4, 5]) {
        if (j == 1) {
            break outer;
        } else if (j == 2) {
            continue outer;
        }

        if (j == 3) {
            continue;
        }

        if (j == 4) {
            break;
        }
    }
}


for (;;)
    int number = 1


int i
for (i = 0; i < 5; i++);




for (Object child in children()) {
    if (child instanceof String) {
        break
    } else {
        continue
    }
}
