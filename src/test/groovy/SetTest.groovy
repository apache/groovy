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
package groovy

import groovy.test.GroovyTestCase

class SetTest extends GroovyTestCase {

    void testSetPlus() {
        Set s1 = [6, 4, 5, 1, 7, 2]
        def s2 = [6, 4, 5, 1, 7, [4,5]]
        def s3 = s1 + s2
        assert s3 == [1, 2, 4, 5, 6, 7, [4,5]] as Set
    }

    void testSetSimpleMinus() {
        Set s1 = [1, 1, 2, 2, 3, 3, 3, 4, 5, 3, 5]
        def s2 = s1 - [1, 4]
        assert s2 == [2, 3, 5] as Set
        def s3 = s1 - 4.0
        assert s3 == [1, 2, 3, 5] as Set
    }

    void testSetFlatten() {
        Set orig = [[[4, 5, 6, [46, 7, "erer"] as Set] as Set, 4, [3, 6, 78] as Set] as Set, 4]
        def flat = orig.flatten()
        assert flat instanceof Set
        assert flat == [3, 4, 5, 6, 7, 46, 78, "erer"] as Set
    }

    void testFlattenSetOfMapsWithClosure() {
        Set orig = [[a:1, b:2], [c:3, d:4]] as Set
        def flat = orig.flatten{ it instanceof Map ? it.values() : it }
        assert flat instanceof Set
        assert flat == [1, 2, 3 ,4] as Set
        flat = orig.flatten{ it instanceof Map ? it.keySet() : it }
        assert flat == ["a", "b", "c", "d"] as Set
    }

    void testSetEquality() {
        def a = [1, 'a', null] as Set
        def b = ['a', null, 1] as Set
        assert a == a
        assert a.equals(a)
        assert a == b
        assert a.equals(b)
    }
}
