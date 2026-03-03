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
package org.codehaus.groovy.runtime

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
/**
 * STC Tests for ArrayGroovyMethods
 */
final class ArrayGroovyMethodsSTCTest extends StaticTypeCheckingTestCase {

    @Test
    void testAnyForBooleanArray() {
        assertScript '''
        boolean[] array = [true]
        assert array.any{ it.booleanValue() }
        '''
    }

    @Test
    void testAnyForByteArray() {
        assertScript '''
        byte[] array = [0, 1, 2]
        assert array.any{ 1 == it.byteValue() }
        '''
    }

    @Test
    void testAnyForCharArray() {
        assertScript '''
        char[] array = 'abc'.chars
        assert array.any{ 'c' == it.charValue() }
        '''
    }

    @Test
    void testAnyForShortArray() {
        assertScript '''
        short[] array = [0, 1, 2]
        assert array.any{ 2 == it.shortValue() }
        '''
    }

    @Test
    void testAnyForIntArray() {
        assertScript '''
        int[] array = [0, 1, 2]
        assert array.any{ 1 == it.intValue() }
        '''
    }

    @Test
    void testAnyForLongArray() {
        assertScript '''
        long[] array = [0L, 1L, 2L]
        assert array.any{ 0L == it.longValue() }
        '''
    }

    @Test
    void testAnyForFloatArray() {
        assertScript '''
        float[] array = [0.0f, 1.0f, 2.0f]
        assert array.any{ 0.0f == it.floatValue() }
        '''
    }

    @Test
    void testAnyForDoubleArray() {
        assertScript '''
        double[] array = [0.0d, 1.0d, 2.0d]
        assert array.any{ 0.0d == it.doubleValue() }
        '''
    }

    @Disabled @Test
    void testEachForBooleanArray() {
        assertScript '''
        boolean[] array = [false, true, false]
        String result = ''
        array.each{ result += it.booleanValue() }
        assert result == 'falsetruefalse'
        '''
    }

    @Disabled @Test
    void testEachForByteArray() {
        assertScript '''
        byte[] array = [0, 1, 2]
        String result = ''
        array.each{ result += it.intValue() }
        assert result == '012'
        '''
    }

    @Disabled @Test
    void testEachForCharArray() {
        assertScript '''
        char[] array = 'abc'.chars
        String result = ''
        array.each{ result += it.charValue() }
        assert result == 'abc'
        '''
    }

    @Disabled @Test
    void testEachForShortArray() {
        assertScript '''
        short[] array = [0, 1, 2]
        String result = ''
        array.each{ result += it.shortValue() }
        assert result == '012'
        '''
    }

    @Disabled @Test
    void testEachForIntArray() {
        assertScript '''
        int[] array = [0, 1, 2]
        String result = ''
        array.each{ result += it.intValue() }
        assert result == '012'
        '''
    }

    @Disabled @Test
    void testEachForLongArray() {
        assertScript '''
        long[] array = [0L, 1L, 2L]
        String result = ''
        array.each{ result += it.longValue() }
        assert result == '012'
        '''
    }

    @Disabled @Test
    void testEachForFloatArray() {
        assertScript '''
        float[] array = [0f, 1f, 2f]
        String result = ''
        array.each{ result += it.floatValue() }
        assert result == '0.01.02.0'
        '''
    }

    @Disabled @Test
    void testEachForDoubleArray() {
        assertScript '''
        double[] array = [0d, 1d, 2d]
        String result = ''
        array.each{ result += it.doubleValue() }
        assert result == '0.01.02.0'
        '''
    }

    @Test
    void testEachWithIndexForBooleanArray() {
        assertScript '''
        boolean[] array = [false, true, false]
        String result = ''
        array.eachWithIndex{ item, index -> result += "$index:${item.booleanValue()}" }
        assert result == '0:false1:true2:false'
        '''
    }

    @Test
    void testEachWithIndexForByteArray() {
        assertScript '''
        byte[] array = [7, 8, 9]
        String result = ''
        array.eachWithIndex{ item, index -> result += "$index:${item.byteValue()}" }
        assert result == '0:71:82:9'
        '''
    }

    @Test
    void testEachWithIndexForCharArray() {
        assertScript '''
        char[] array = 'abc'.chars
        String result = ''
        array.eachWithIndex{ item, index -> result += "$index:${item.charValue()}" }
        assert result == '0:a1:b2:c'
        '''
    }

    @Test
    void testEachWithIndexForShortArray() {
        assertScript '''
        short[] array = [7, 8, 9]
        String result = ''
        array.eachWithIndex{ item, index -> result += "$index:${item.shortValue()}" }
        assert result == '0:71:82:9'
        '''
    }

    @Test
    void testEachWithIndexForIntArray() {
        assertScript '''
        int[] array = [7, 8, 9]
        String result = ''
        array.eachWithIndex{ item, index -> result += "$index:${item.intValue()}" }
        assert result == '0:71:82:9'
        '''
    }

    @Test
    void testEachWithIndexForLongArray() {
        assertScript '''
        long[] array = [7L, 8L, 9L]
        String result = ''
        array.eachWithIndex{ item, index -> result += "$index:${item.longValue()}" }
        assert result == '0:71:82:9'
        '''
    }

    @Test
    void testEachWithIndexForFloatArray() {
        assertScript '''
        float[] array = [0f, 1f, 2f]
        String result = ''
        array.eachWithIndex{ item, index -> result += "$index:${item.floatValue()}" }
        assert result == '0:0.01:1.02:2.0'
        '''
    }

    @Test
    void testEachWithIndexForDoubleArray() {
        assertScript '''
        double[] array = [0d, 1d, 2d]
        String result = ''
        array.eachWithIndex{ item, index -> result += "$index:${item.doubleValue()}" }
        assert result == '0:0.01:1.02:2.0'
        '''
    }

    @Test
    void testReverseEachForBooleanArray() {
        assertScript '''
        boolean[] array = [false, true, true]
        String result = ''
        array.reverseEach{ result += it.booleanValue() }
        assert result == 'truetruefalse'
        '''
    }

    @Test
    void testReverseEachForByteArray() {
        assertScript '''
        byte[] array = [0, 1, 2]
        String result = ''
        array.reverseEach{ result += it.intValue() }
        assert result == '210'
        '''
    }

    @Test
    void testReverseEachForCharArray() {
        assertScript '''
        char[] array = 'abc'.chars
        String result = ''
        array.reverseEach{ result += it.charValue() }
        assert result == 'cba'
        '''
    }

    @Test
    void testReverseEachForShortArray() {
        assertScript '''
        short[] array = [0, 1, 2]
        String result = ''
        array.reverseEach{ result += it.shortValue() }
        assert result == '210'
        '''
    }

    @Test
    void testReverseEachForIntArray() {
        assertScript '''
        int[] array = [0, 1, 2]
        String result = ''
        array.reverseEach{ result += it.intValue() }
        assert result == '210'
        '''
    }

    @Test
    void testReverseEachForLongArray() {
        assertScript '''
        long[] array = [0L, 1L, 2L]
        String result = ''
        array.reverseEach{ result += it.longValue() }
        assert result == '210'
        '''
    }

    @Test
    void testReverseEachForFloatArray() {
        assertScript '''
        float[] array = [0f, 1f, 2f]
        String result = ''
        array.reverseEach{ result += it.floatValue() }
        assert result == '2.01.00.0'
        '''
    }

    @Test
    void testReverseEachForDoubleArray() {
        assertScript '''
        double[] array = [0d, 1d, 2d]
        String result = ''
        array.reverseEach{ result += it.doubleValue() }
        assert result == '2.01.00.0'
        '''
    }

    @Test
    void testAsBooleanForNullBooleanArray() {
        assertScript '''
        @groovy.transform.CompileStatic
        def method() {
            boolean[] array = null
            assert !array.asBoolean()
        }

        method()
        '''
    }

    @Test
    void testAsBooleanForNullByteArray() {
        assertScript '''
        @groovy.transform.CompileStatic
        def method() {
            byte[] array = null
            assert !array.asBoolean()
        }

        method()
        '''
    }
}
