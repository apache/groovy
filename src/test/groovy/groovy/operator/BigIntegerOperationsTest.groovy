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

class BigIntegerOperationsTest extends GroovyTestCase {
    def x, y

    void testAssign() {
        BigInteger foo
        foo = (byte) 20
        assert foo.class == BigInteger
        assert foo == 20

        foo = (short) 20
        assert foo.class == BigInteger
        assert foo == 20

        foo = (int) 20
        assert foo.class == BigInteger
        assert foo == 20

        foo = (long) 20
        assert foo.class == BigInteger
        assert foo == 20

        foo = (float) 0.5f
        assert foo.class == BigInteger
        assert foo == 0

        foo = (double) 0.5d
        assert foo.class == BigInteger
        assert foo == 0

        foo = 10.5G
        assert foo.class == BigInteger
        assert foo == 10

        double d = 1000
        d *= d
        d *= d
        d *= d
        assert (long)d != d
        assert (BigInteger) d == d
    }

    void testPlus() {
        x = 2G + 3G
        assert x instanceof BigInteger
        assert x == 5G
    }

    void testMultiply() {
        x = 2G * 3G
        assert x instanceof BigInteger
        assert x == 6G
    }

    void testMod() {
        x = 100G.mod(3)
        assert x == 1G

        y = 11G
        y = y.mod(3)
        assert y == 2G
        y = -11G
        y = y.mod(3)
        assert y == 1G
    }

    void testRemainder() {
        x = 100G % 3
        assert x == 1G

        y = 11G
        y %= 3
        assert y == 2G
        y = -11G
        y %= 3
        assert y == -2G
    }

    void testAsOperatorPrecisionLoss() {
        def value = BigInteger.valueOf(Long.MAX_VALUE) + 1
        def value2 = value as BigInteger
        assert value == value2
    }
}
