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

def testConditionalStatementAsExpression() {
    def a = 1
    def b = 2
    def max = if (a > b) a else b
    assert 2 == max

    def c = 1
    def result = switch (c) {
        case 1:
            2; break
        default:
            0
    }
    assert 2 == result

    def d = 6
    d = if (d > 0) 1 else if (d == 0) 0 else -1
    assert 1 == d

    def e = 7
    e = switch (e) {
        case 5:
            6; break
        case 6:
            7; break
        case 7:
            8; break
        default:
            0
    }
    assert 8 == e
}

testConditionalStatementAsExpression()

@CompileStatic
def testCsConditionalStatementAsExpression() {
    def a = 1
    def b = 2
    def max = if (a > b) a else b
    assert 2 == max

    def c = 1
    def result = switch (c) {
        case 1:
            2; break
        default:
            0
    }
    assert 2 == result

    def d = 6
    d = if (d > 0) 1 else if (d == 0) 0 else -1
    assert 1 == d

    def e = 7
    e = switch (e) {
        case 5:
            6; break
        case 6:
            7; break
        case 7:
            8; break
        default:
            0
    }
    assert 8 == e
}

testCsConditionalStatementAsExpression()
