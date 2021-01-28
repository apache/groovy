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
package org.codehaus.groovy.runtime.typehandling

import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

final class DefaultTypeTransformationTest {

    @Test
    void testCastToType() {
        def input = null, result

        result = DefaultTypeTransformation.castToType(input, int)
        assert result === null

        result = DefaultTypeTransformation.castToType(input, long)
        assert result === null

        result = DefaultTypeTransformation.castToType(input, boolean)
        assert result === false // GROOVY-9916

        result = DefaultTypeTransformation.castToType(input, Object)
        assert result === null

        input = new Object()
        result = DefaultTypeTransformation.castToType(input, Object)
        assert result === input

        input = 'string'
        result = DefaultTypeTransformation.castToType(input, Object)
        assert result === input

        input = "$input"
        result = DefaultTypeTransformation.castToType(input, Object)
        assert result === input
    }

    @Test
    void testCompareTo() {
        // objects
        Object object1 = new Object()
        Object object2 = new Object()
        assert DefaultTypeTransformation.compareTo(null, null) == 0
        assert DefaultTypeTransformation.compareTo(object1, null) == 1
        assert DefaultTypeTransformation.compareTo(null, object1) == -1
        assert DefaultTypeTransformation.compareTo(1, 1) == 0

        shouldFail(IllegalArgumentException) {
            DefaultTypeTransformation.compareTo(object1, object2)
        }

        // chars, int values 49 and 50
        Character char1 = '1'.charAt(0)
        Character char2 = '2'.charAt(0)
        checkCompareToSymmetricSmallerThan(char1, char2)

        MyNumber number1 = new MyNumber(49)
        MyNumber number2 = new MyNumber(50)

        MyNumberCompareTo numCompTo1 = new MyNumberCompareTo(49)
        MyNumberCompareTo numCompTo2 = new MyNumberCompareTo(50)

        MyNumberComparable numComp1 = new MyNumberComparable(49)
        MyNumberComparable numComp2 = new MyNumberComparable(50)

        List lowers = [
                Integer.valueOf(49), Long.valueOf(49L),
                Double.valueOf(49.0),
                Float.valueOf(49.0f),
                49.0G, 49.00G,
                char1, '1',
                number1, numCompTo1, numComp1]
        List highers = [
                Integer.valueOf(50), Long.valueOf(50L),
                Double.valueOf(50.0),
                Float.valueOf(50.0f),
                50.0G, 50.00G,
                char2, '2',
                number2, numCompTo2, numComp2]

        for (lower in lowers) {
            for (lower2 in lowers) {
                assert DefaultTypeTransformation.compareTo(lower, lower2) == 0
            }
            for (higher in highers) {
                checkCompareToSymmetricSmallerThan(lower, higher)
            }
        }

        shouldFail(IllegalArgumentException) {
            DefaultTypeTransformation.compareTo(1, "22")
        }
        shouldFail(IllegalArgumentException) {
            DefaultTypeTransformation.compareTo("22", 1)
        }

        // [G]Strings and chars
        assert DefaultTypeTransformation.compareTo('aa1', '2'.charAt(0)) > 0
        assert DefaultTypeTransformation.compareTo('2'.charAt(0), 'aa1') < 0
        assert DefaultTypeTransformation.compareTo("aa${1}", '2'.charAt(0)) > 0
        assert DefaultTypeTransformation.compareTo('2'.charAt(0), "aa${1}") < 0

        // Strings and GStrings
        List lowers2 = ['aa1', "aa${1}"]
        List highers2 = ['bb2', "b${2}"]
        for (lower in lowers2) {
            assert DefaultTypeTransformation.compareTo(lower, lower) == 0
            for (higher in highers2) {
                checkCompareToSymmetricSmallerThan(lower, higher)
            }
        }
    }

    @Test
    void testNumberEqualsCharacterGString() {
        final String S = 'A'
        final GString G = "$S"
        final Number N = 65

        assert N == S && S == G
        assert N      ==      G
    }

    @Test
    void testCharacterEqualsCharacterGString() {
        final String S = 'A'
        final Character C = 'A'
        final GString G = "$S"

        assert C == S && S == G
        assert C      ==      G
    }

    @Test
    void testCharacterGStringsEqualsCharacter() {
        final String S = 'A'
        final Character C = 'A'
        final GString G = "$S"

        assert S == C && G == S
        assert      G == C
    }

    @Test
    void testCharacterGStringEqualsNumber() {
        final String S = 'A'
        final GString G = "$S"
        final Number N = 65

        assert S == N && G == S
        assert      G == N
    }

    //--------------------------------------------------------------------------

    private static void checkCompareToSymmetricSmallerThan(a, b) {
        try {
            assert DefaultTypeTransformation.compareTo(a, b) < 0
            assert DefaultTypeTransformation.compareTo(b, a) > 0
        } catch (AssertionError e) {
            System.err.print(a.class.toString() + ' compared to ' + b.class.toString())
            throw e
        }
    }

    static class MyNumber extends Number {
        def n
        MyNumber(n) {
            this.n = n
        }
        @Override
        int intValue() { n }
        @Override
        long longValue() { n }
        @Override
        float floatValue() { n }
        @Override
        double doubleValue() { n }
        @Override
        int hashCode() { -n }
        @Override
        boolean equals(other) {
            if (other instanceof MyNumber) {
                return n == other.n
            }
            return false
        }
        @Override
        String toString() { n.toString() }
    }

    static class MyNumberCompareTo extends MyNumber {
        MyNumberCompareTo(Object n) {
            super(n)
        }
        int compareTo(MyNumber other) {
            return n <=> other.n
        }
    }

    static class MyNumberComparable extends MyNumberCompareTo implements Comparable<MyNumber> {
        MyNumberComparable(Object n) {
            super(n)
        }
        int compareTo(Object other) {
            return n <=> (MyNumber) other;
        }
    }
}
