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
import groovy.transform.CompileStatic

@CompileStatic
def testCS() {
    int result = 0
    for (int i = 0, n = 5; i < n; i++) {
        result += i
    }
    assert 10 == result

    result = 0;
    int i;
    int j;
    for (i = 1, j = 5; i < j; i++, j--) {
        result += i;
        result += j;
    }
    assert 12 == result
}
testCS();

def test() {
    int result = 0
    for (int i = 0, n = 5; i < n; i++) {
        result += i
    }
    assert 10 == result

    result = 0;
    int i;
    int j;
    for (i = 1, j = 5; i < j; i++, j--) {
        result += i;
        result += j;
    }
    assert 12 == result
}
test();
