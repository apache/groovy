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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.codehaus.groovy.runtime.typehandling.ShortTypeHandling.castToChar
import static org.codehaus.groovy.runtime.typehandling.ShortTypeHandling.castToClass
import static org.codehaus.groovy.runtime.typehandling.ShortTypeHandling.castToEnum
import static org.codehaus.groovy.runtime.typehandling.ShortTypeHandling.castToString

class ShortTypeHandlingTest {

    @Test
    void testCastToClass() {
        assert castToClass(null) == null
        assert castToClass(Integer.class) == Integer.class
        assert castToClass('java.lang.String') == String.class
        shouldFail(GroovyCastException) {
            castToClass(Collections.emptyList())
        }
    }

    @Test
    void testCastToString() {
        assert castToString(null) == null
        assert castToString(String.class) == 'class java.lang.String'
        assert castToString(List.class) == 'interface java.util.List'
    }

    @Test
    void testCastToCharacter() {
        assert castToChar(null) == null
        char c = (char)'c'
        assert castToChar((Object)c) == c
        assert castToChar(Integer.valueOf(99)) == c
        assert castToChar("${c}") == c
        assert castToChar('c') == c
        shouldFail(GroovyCastException) {
            castToChar(new Date())
        }
    }

    @Test
    void testCastToEnum() {
        assert castToEnum(null, TestStages) == null
        assert castToEnum((Object)TestStages.AFTER_CLASS, TestStages) == TestStages.AFTER_CLASS
        assert castToEnum("BEFORE_TEST", TestStages) == TestStages.BEFORE_TEST
    }

    enum TestStages {
        BEFORE_CLASS, BEFORE_TEST, TEST, AFTER_TEST, AFTER_CLASS
    }

    // --- Merged from ShortTypeHandlingJUnit5Test ---

    enum TestEnum { VALUE_ONE, VALUE_TWO, VALUE_THREE }

    @Test
    void testCastToClassWithNull() {
        assert castToClass(null) == null
    }

    @Test
    void testCastToClassWithClass() {
        def result = castToClass(String.class)
        assert String.class == result
    }

    @Test
    void testCastToClassWithClassName() {
        def result = castToClass("java.lang.String")
        assert String.class == result
    }

    @Test
    void testCastToClassWithInvalidClassName() {
        shouldFail(GroovyCastException) {
            castToClass("invalid.class.Name")
        }
    }

    @Test
    void testCastToStringWithNull() {
        assert castToString(null) == null
    }

    @Test
    void testCastToStringWithString() {
        assert "hello" == castToString("hello")
    }

    @Test
    void testCastToStringWithObject() {
        assert "42" == castToString(42)
    }

    @Test
    void testCastToStringWithBooleanArray() {
        boolean[] arr = [true, false, true]
        assert "[true, false, true]" == ShortTypeHandling.castToString(arr)
    }

    @Test
    void testCastToStringWithByteArray() {
        byte[] arr = [1, 2, 3]
        assert "[1, 2, 3]" == ShortTypeHandling.castToString(arr)
    }

    @Test
    void testCastToStringWithCharArray() {
        char[] arr = ['a', 'b', 'c']
        assert "abc" == ShortTypeHandling.castToString(arr)
    }

    @Test
    void testCastToStringWithDoubleArray() {
        double[] arr = [1.1, 2.2, 3.3]
        assert "[1.1, 2.2, 3.3]" == ShortTypeHandling.castToString(arr)
    }

    @Test
    void testCastToStringWithFloatArray() {
        float[] arr = [1.1f, 2.2f, 3.3f]
        assert "[1.1, 2.2, 3.3]" == ShortTypeHandling.castToString(arr)
    }

    @Test
    void testCastToStringWithIntArray() {
        int[] arr = [1, 2, 3]
        assert "[1, 2, 3]" == ShortTypeHandling.castToString(arr)
    }

    @Test
    void testCastToStringWithLongArray() {
        long[] arr = [1L, 2L, 3L]
        assert "[1, 2, 3]" == ShortTypeHandling.castToString(arr)
    }

    @Test
    void testCastToStringWithShortArray() {
        short[] arr = [1, 2, 3]
        assert "[1, 2, 3]" == ShortTypeHandling.castToString(arr)
    }

    @Test
    void testCastToStringWithObjectArray() {
        Object[] arr = ["a", "b", "c"]
        assert "[a, b, c]" == ShortTypeHandling.castToString(arr)
    }

    @Test
    void testCastToEnumWithNull() {
        assert castToEnum(null, TestEnum) == null
    }

    @Test
    void testCastToEnumWithSameEnum() {
        def result = castToEnum(TestEnum.VALUE_ONE, TestEnum)
        assert TestEnum.VALUE_ONE == result
    }

    @Test
    void testCastToEnumWithString() {
        def result = castToEnum("VALUE_TWO", TestEnum)
        assert TestEnum.VALUE_TWO == result
    }

    @Test
    void testCastToEnumWithInvalidString() {
        shouldFail(IllegalArgumentException) {
            castToEnum("INVALID_VALUE", TestEnum)
        }
    }

    @Test
    void testCastToEnumWithInvalidType() {
        shouldFail(GroovyCastException) {
            castToEnum(123, TestEnum)
        }
    }

    @Test
    void testCastToCharWithNull() {
        assert castToChar(null) == null
    }

    @Test
    void testCastToCharWithCharacter() {
        assert ('X' as char) == castToChar('X')
    }

    @Test
    void testCastToCharWithNumber() {
        assert ('A' as char) == castToChar(65)
    }

    @Test
    void testCastToCharWithSingleCharString() {
        assert ('Z' as char) == castToChar("Z")
    }

    @Test
    void testCastToCharWithMultiCharString() {
        shouldFail(GroovyCastException) {
            castToChar("Hello")
        }
    }

    @Test
    void testCastToCharWithEmptyString() {
        shouldFail(GroovyCastException) {
            castToChar("")
        }
    }

    @Test
    void testCastToCharWithLongNumber() {
        assert ('a' as char) == castToChar(97L)
    }

    @Test
    void testCastToCharWithDoubleNumber() {
        assert ('A' as char) == castToChar(65.9)
    }
}
