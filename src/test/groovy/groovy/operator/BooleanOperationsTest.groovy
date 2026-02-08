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
package groovy.operator

import groovy.test.GroovyTestCase

class BooleanOperationsTest extends GroovyTestCase {

    void testComparisons() {
        assert true
        assert true != false

        def x = true
        assert x
        assert x == true
        assert x != false

        x = false
        assert x == false
        assert x != true
        assert !x

        def y = false
        assert x == y

        y = true
        assert x != y
    }

    void testIfBranch() {
        def x = false
        def r = false
        if (x) {
            // ignore
        }
        else {
            r = true
        }
        assert r

        x = true
        r = false
        if (x) {
            r = true
        }
        else {
            // ignore
        }
        assert r

        if (!x) {
            r = false
        }
        else {
            r = true
        }
        assert r
    }

    void testBooleanExpression() {
        def x = 5
        def value = x > 2
        assert value

        value = x < 2
        assert value == false
    }


    void testBooleanOps() {
        boolean x = true
        boolean y = false
        assert (x & x) == true
        assert (x & y) == false
        assert (y & x) == false
        assert (y & y) == false

        assert (x | x) == true
        assert (x | y) == true
        assert (y | x) == true
        assert (y | y) == false

        assert (x ^ x) == false
        assert (x ^ y) == true
        assert (y ^ x) == true
        assert (y ^ y) == false

        assert (!x) == false
        assert (!y) == true
    }

    void testImplies() {
        assert false.implies(true)
        assert false.implies(false)
        assert true.implies(true)
        assert !true.implies(false)
    }

    void testBooleanAssignOps() {
        boolean z = true
        z &= true
        assert z == true
        z &= false
        assert z == false

        z = true
        z |= true
        assert z == true
        z |= false
        assert z == true
        z = false
        z |= false
        assert z == false
        z |= true
        assert z == true

        z = true
        z ^= true
        assert z == false
        z ^= true
        assert z == true
        z ^= false
        assert z == true
        z ^= true
        assert z == false
        z ^= false
        assert z == false
    }

    void testBooleanAssignArrayOps() {
        boolean[] b = [true]
        b[0] &= false
        assert b == [false]
        b[0] ^= true
        assert b == [true]
        b[0] ^= true
        assert b == [false]
        b[0] |= true
        assert b == [true]
        b[0] |= false
        assert b == [true]
    }
}
