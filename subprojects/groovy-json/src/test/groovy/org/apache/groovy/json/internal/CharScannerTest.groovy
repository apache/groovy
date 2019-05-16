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
package org.apache.groovy.json.internal

import groovy.test.GroovyTestCase

class CharScannerTest extends GroovyTestCase {

    void testParseInt() {
        int i = CharScanner.parseInt("-22".toCharArray())
        assert i == -22

        i = CharScanner.parseInt("22".toCharArray())
        assert i == 22
    }

    void testParseLongTest() {
        long value = CharScanner.parseLong("-22".toCharArray())
        assert value == -22L

        value = CharScanner.parseInt("22".toCharArray())
        assert value == 22
    }

    void testParseLongTest2() {
        String test = "" + ((long) (Long.MAX_VALUE / 2L))
        long value = CharScanner.parseLong(test.toCharArray())
        assert value == Long.parseLong(test)
    }

    void testParseLongTest3() {
        String test = "" + (Long.MIN_VALUE / 2L)
        long value = CharScanner.parseLong(test.toCharArray())
        assert value == Long.parseLong(test)
    }

    void testParseLongTest4() {
        String test = "" + (Long.MAX_VALUE)
        long value = CharScanner.parseLong(test.toCharArray())
        assert value == Long.parseLong(test)
    }

    void testParseLongTest5() {
        String test = "" + (Long.MIN_VALUE)
        long value = CharScanner.parseLong(test.toCharArray())
        assert value == Long.parseLong(test)
    }

    void testParseIntMax() {
        int i = 0
        i = CharScanner.parseInt(("" + Integer.MAX_VALUE).toCharArray())
        assert i == Integer.MAX_VALUE
    }

    void testParseIntMin() {
        int i = 0
        i = CharScanner.parseInt(("" + Integer.MIN_VALUE).toCharArray())
        assert i == Integer.MIN_VALUE
    }

    void testParseLongMax() {
        long l = 0
        l = CharScanner.parseLong(("" + Long.MAX_VALUE).toCharArray())
        assert l == Long.MAX_VALUE
    }

    void testParseLongMin() {
        long l = 0
        l = CharScanner.parseLong(("" + Long.MIN_VALUE).toCharArray())
        assert l == Long.MIN_VALUE || die("l", l, "MIN", Long.MIN_VALUE)
    }

    void testParseDouble() {
        String str = "123456789"
        double num =
            CharScanner.parseJsonNumber(str.toCharArray(), 0, str.length()).doubleValue()
        assert num == 123456789d
    }

    void testParseDoubleNegative() {
        String str = "-1.23456789E8"
        double num =
            (Double) CharScanner.parseJsonNumber(str.toCharArray(), 0, str.length())
        assert num == -1.23456789E8
    }

    void testParseDoubleNegativeNoE() {
        String str = "-123456789"
        testDouble(str)
    }

    void testParseDoubleNegativeNoE2() {
        String str = "-1234567890"
        testDouble(str)
    }

    void testParseDoubleMax() {
        String str = "" + Double.MAX_VALUE
        testDouble(str)
    }

    void testParseDoubleMin() {
        String str = "" + Double.MIN_VALUE
        testDouble(str)
    }

    void testManyDoubles() {
        List<String> doubles = ["" + 1.01d, "" + 123456789.234D, "" + 55D,
                "" + Integer.MAX_VALUE + "." + Integer.MAX_VALUE,
                "66666666.666", "-6666666666.6666", "1E10"]

        for (String str : doubles) {
            testDouble(str)
        }
    }

    private void testDouble(String str) {
        double num = CharScanner.parseJsonNumber(str.toCharArray(), 0, str.length()).doubleValue()
        assert num == Double.parseDouble(str)
    }

    private void testDoubleInStringThreeOver(String str) {
        double numTest = Double.parseDouble(str)
        double num = CharScanner.parseJsonNumber(("   " + str).toCharArray(), 3, str.length() + 3).doubleValue()
        assert num == numTest
    }

    void testParseIntIgnore0() {
        int i = CharScanner.parseIntFromToIgnoreDot("1.1".toCharArray(), 0, "1.1".length())
        assert i == 11
    }

    void testSimpleDoubleInString() {
        testDoubleInStringThreeOver("1.1")
    }

    void testLongMaxWithOffset() {
        testDoubleInStringThreeOver("" + Long.MAX_VALUE)
    }

    void testLargeDecimal() {
        testDoubleInStringThreeOver("" + Integer.MAX_VALUE + "." + Integer.MAX_VALUE)
    }

    void testLargeDecimal2() {
        testDoubleInStringThreeOver("1000" + "." + "10001")
    }

    void testLargeDecimal3() {
        testDoubleInStringThreeOver("10000" + "." + "100001")
    }

    void testLargeDecimal4() {
        testDoubleInStringThreeOver("" + 10_000_000 + "." + 10_000_001)
    }

    void testLargeDecimal5() {
        testDoubleInStringThreeOver("" + 100_000_000 + "." + 100_000_001)
    }

    void testLargeDecimal6() {
        testDoubleInStringThreeOver("" + 100_000_000 + "." + 1_000_000_001)
    }

    void testLargeDecimal7() {
        testDoubleInStringThreeOver("" + 100_000_000 + "." + 1_000_000_001L)
    }

    void testLargeDecimal8() {
        testDoubleInStringThreeOver("" + 1_000_000_000_000L + "." + 1_000_000_001L)
    }

    void testLargeDecimal9() {
        testDoubleInStringThreeOver("" + 10_000_000_000_000L + "." + 1_000_000_001L)
    }

    void testLargeDecimal10() {
        testDoubleInStringThreeOver("" + 100_000_000_000_000_000L + "." + 1_000_000_001L)
    }

    void testLargeDecimal11() {
        testDoubleInStringThreeOver("" + 1_000_000_000_000_000_000L + "." + 1_000_000_001L)
    }

    void testLongMinWithOffset() {
        testDoubleInStringThreeOver("" + Long.MIN_VALUE)
    }

    void testDoubleMaxWithOffset() {
        testDoubleInStringThreeOver("" + Double.MAX_VALUE)
    }

    void testDoubleMinWithOffset() {
        testDoubleInStringThreeOver("" + Double.MIN_VALUE)
    }

    void testDoubleMaxWithOffset2() {
        testDoubleInStringThreeOver("" + Double.MAX_VALUE / 2)
    }

    void testDoubleMinWithOffset2() {
        testDoubleInStringThreeOver("" + Double.MIN_VALUE / 2)
    }

    void testDoubleMaxWithOffset3() {
        testDoubleInStringThreeOver("" + (Double.MAX_VALUE / 9) * 8)
    }

    void testDoubleMinWithOffset3() {
        testDoubleInStringThreeOver("" + (Double.MIN_VALUE / 9) * 8)
    }

    void testParseLong() {
        String str = "12345678910"
        long l1 = CharScanner.parseLongFromTo(str.toCharArray(), 0, str.length())
        assert l1 == 12345678910L

        str = "abc12345678910"
        l1 = CharScanner.parseLongFromTo(str.toCharArray(), 3, str.length())
        assert l1 == 12345678910L

        str = "abcdefghijklmnopqrstuvwxyz12345678910"
        l1 = CharScanner.parseLongFromTo(str.toCharArray(), 26, str.length())
        assert l1 == 12345678910L

        String str2 = "abcdefghijklmnopqrstuvwxyz12345678910mymilkshakemakestheboysintheyard"
        l1 = CharScanner.parseLongFromTo(str2.toCharArray(), 26, str.length())
        assert l1 == 12345678910L
    }

    void testParseIntFromTo() {
        String str = "123456789"
        int i = CharScanner.parseIntFromTo(str.toCharArray(), 0, str.length())
        assert i == 123456789

        str = "abc-123456789"
        i = CharScanner.parseIntFromTo(str.toCharArray(), 3, str.length())
        assert i == -123456789

        str = "abcdefghijklmnopqrstuvwxyz56789"
        i = CharScanner.parseIntFromTo(str.toCharArray(), 26, str.length())
        assert i == 56789

        str = "abcdefghijklmnopqrstuvwxyz-6789mymilkshakemakestheboysintheyard"
        i = CharScanner.parseIntFromTo(str.toCharArray(), 26, 31)
        assert i == -6789
    }

    void testParseJsonNumberToDecimal() {
        def num = CharScanner.parseJsonNumber('123.40'.toCharArray())
        assert num instanceof BigDecimal
        assert num == 123.40G
        assert num.scale() == 2

        num = CharScanner.parseJsonNumber('-123.400'.toCharArray())
        assert num instanceof BigDecimal
        assert num == -123.400G
        assert num.scale() == 3

        num = CharScanner.parseJsonNumber('3.7e-5'.toCharArray())
        assert num instanceof BigDecimal
        assert num == 0.000037G
        assert num.scale() == 6

        num = CharScanner.parseJsonNumber('-1.25E+7'.toCharArray())
        assert num instanceof BigDecimal
        assert num == -12500000.0G
        assert num.scale() == -5
    }

    protected assertArrayEquals(char[] expected, char[] actual) {
        assertArrayEquals((Object[]) expected, (Object[]) actual)
    }
}
