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

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

/**
 * Tests for FormatHelper class.
 */
class FormatHelperTest {

    // -- from original Java tests --

    @Test
    void testToTypeStringWithMaxSizeParam() {
        Object[] objects = null
        assertEquals("null", FormatHelper.toTypeString(objects, 42))

        objects = new Object[0]
        assertEquals("", FormatHelper.toTypeString(objects, 42))

        objects = [null] as Object[]
        assertEquals("null", FormatHelper.toTypeString(objects, 42))

        objects = [0] as Object[]
        assertEquals("Integer", FormatHelper.toTypeString(objects, 42))

        objects = [0, 1d] as Object[]
        assertEquals("Integer, Double", FormatHelper.toTypeString(objects, 42))

        objects = [new DummyBean(), new DummyBean()] as Object[] // GROOVY-11270
        assertEquals("o.c.g.r.DummyBean, o.c.g.r.DummyBean", FormatHelper.toTypeString(objects, 42))
    }

    // -- from JUnit5 tests --

    @Test
    void testToStringNull() {
        def result = FormatHelper.toString(null)
        assertEquals("null", result)
    }

    @Test
    void testToStringString() {
        def result = FormatHelper.toString("hello")
        assertEquals("hello", result)
    }

    @Test
    void testToStringInteger() {
        def result = FormatHelper.toString(42)
        assertEquals("42", result)
    }

    @Test
    void testToStringCollection() {
        def list = ["a", "b", "c"]
        def result = FormatHelper.toString(list)
        assertEquals("[a, b, c]", result)
    }

    @Test
    void testToStringMap() {
        def map = new LinkedHashMap<String, Integer>()
        map.put("one", 1)
        map.put("two", 2)
        def result = FormatHelper.toString(map)
        assertTrue(result.contains("one") && result.contains("1"))
    }

    @Test
    void testToStringObjectArray() {
        Object[] arr = ["a", "b", "c"]
        def result = FormatHelper.toString(arr)
        assertEquals("[a, b, c]", result)
    }

    @Test
    void testToStringCharArray() {
        char[] chars = ['h', 'i']
        def result = FormatHelper.toString(chars)
        assertEquals("hi", result)
    }

    @Test
    void testToStringPrimitiveIntArray() {
        int[] arr = [1, 2, 3]
        def result = FormatHelper.toString(arr)
        assertEquals("[1, 2, 3]", result)
    }

    @Test
    void testInspectString() {
        def result = FormatHelper.inspect("hello")
        assertTrue(result.contains("hello"))
    }

    @Test
    void testInspectNull() {
        def result = FormatHelper.inspect(null)
        assertEquals("null", result)
    }

    @Test
    void testFormatVerbose() {
        def result = FormatHelper.format("hello", true)
        assertTrue(result.contains("hello"))
    }

    @Test
    void testFormatWithMaxSize() {
        def longString = "This is a very long string that should be truncated"
        def result = FormatHelper.format(longString, false, 10)
        // Format doesn't truncate simple strings, just collections/maps
        assertNotNull(result)
    }

    @Test
    void testFormatWithMaxSizeOnCollection() {
        def list = ["item1", "item2", "item3", "item4", "item5"]
        def result = FormatHelper.format(list, false, 20)
        assertNotNull(result)
    }

    @Test
    void testFormatEmptyCollection() {
        def list = Collections.emptyList()
        def result = FormatHelper.format(list, false)
        assertEquals("[]", result)
    }

    @Test
    void testFormatEmptyMap() {
        def map = Collections.emptyMap()
        def result = FormatHelper.format(map, false)
        assertEquals("[:]", result)
    }

    @Test
    void testFormatMapWithInspect() {
        def map = new LinkedHashMap<String, String>()
        map.put("key", "value")
        def result = FormatHelper.format(map, true)
        assertTrue(result.contains("key") && result.contains("value"))
    }

    @Test
    void testToStringWithOptions() {
        def options = [:]
        options.put("safe", true)
        options.put("maxSize", 100)

        def result = FormatHelper.toString(options, "hello")
        assertEquals("hello", result)
    }

    @Test
    void testToStringWithOptionsVerbose() {
        def options = [:]
        options.put("verbose", true)

        def result = FormatHelper.toString(options, "hello")
        assertNotNull(result)
    }

    @Test
    void testToStringWithOptionsInspect() {
        def options = [:]
        options.put("inspect", true)

        def result = FormatHelper.toString(options, "hello")
        assertTrue(result.contains("hello"))
    }

    @Test
    void testToStringWithStringBuilder() {
        def sb = new StringBuilder("test")
        def result = FormatHelper.toString(sb)
        assertEquals("test", result)
    }

    @Test
    void testToStringWithNestedCollection() {
        def nested = [[1, 2], [3, 4]]
        def result = FormatHelper.toString(nested)
        assertEquals("[[1, 2], [3, 4]]", result)
    }

    @Test
    void testWriteToBuffer() throws IOException {
        def sw = new StringWriter()
        FormatHelper.write(sw, "hello")
        assertEquals("hello", sw.toString())
    }

    @Test
    void testWriteNullToBuffer() throws IOException {
        def sw = new StringWriter()
        FormatHelper.write(sw, null)
        assertEquals("null", sw.toString())
    }

    @Test
    void testWriteArrayToBuffer() throws IOException {
        def sw = new StringWriter()
        Object[] arr = ["a", "b"]
        FormatHelper.write(sw, arr)
        assertEquals("[a, b]", sw.toString())
    }

    @Test
    void testFormatSafeMode() {
        // Object that throws exception on toString
        def problematic = new Object() {
            String toString() {
                throw new RuntimeException("Cannot convert to string")
            }
        }

        // Safe mode should not throw
        def result = FormatHelper.format(problematic, false, -1, true)
        assertNotNull(result)
    }

    @Test
    void testFormatUnsafeMode() {
        def problematic = new Object() {
            String toString() {
                throw new RuntimeException("Cannot convert to string")
            }
        }

        // Unsafe mode should throw
        assertThrows(RuntimeException.class, () ->
            FormatHelper.format(problematic, false, -1, false)
        )
    }

    @Test
    void testEscapeBackslashes() {
        def input = "hello\tworld\n"
        def result = FormatHelper.escapeBackslashes(input)
        assertTrue(result.contains("\\t") || result.contains("\\n"))
    }

    @Test
    void testFormatWithEscapeBackslashes() {
        def input = "line1\nline2"
        def result = FormatHelper.format(input, true, true, -1, false)
        assertNotNull(result)
    }

    @Test
    void testToArrayString() {
        Object[] arr = [1, 2, 3]
        def result = FormatHelper.toArrayString(arr)
        assertEquals("[1, 2, 3]", result)
    }

    @Test
    void testToArrayStringWithInspect() {
        Object[] arr = ["a", "b"]
        // toArrayString with params is private, test via format
        def result = FormatHelper.format(arr, true, -1)
        assertTrue(result.contains("a") && result.contains("b"))
    }

    @Test
    void testToMapString() {
        def map = new LinkedHashMap<String, Integer>()
        map.put("a", 1)
        def result = FormatHelper.toMapString(map)
        assertTrue(result.contains("a") && result.contains("1"))
    }

    @Test
    void testToMapStringMaxSize() {
        def map = new LinkedHashMap<String, Integer>()
        for (int i = 0; i < 100; i++) {
            map.put("key" + i, i)
        }
        def result = FormatHelper.toMapString(map, 50)
        assertNotNull(result)
    }

    @Test
    void testToListString() {
        def list = ["one", "two", "three"]
        def result = FormatHelper.toListString(list)
        assertEquals("[one, two, three]", result)
    }

    @Test
    void testToListStringWithMaxSize() {
        def list = ["one", "two", "three", "four", "five"]
        def result = FormatHelper.toListString(list, 15)
        assertNotNull(result)
    }

    @Test
    void testToTypeString() {
        Object[] args = ["hello", 42, true]
        def result = FormatHelper.toTypeString(args)
        assertTrue(result.contains("String"))
        assertTrue(result.contains("Integer"))
        assertTrue(result.contains("Boolean"))
    }

    @Test
    void testToTypeStringWithMaxSize() {
        Object[] args = ["hello", 42, true, 3.14, "world"]
        def result = FormatHelper.toTypeString(args, 20)
        assertNotNull(result)
    }

    @Test
    void testToTypeStringNull() {
        def result = FormatHelper.toTypeString(null)
        assertEquals("null", result)
    }

    @Test
    void testToTypeStringEmpty() {
        def result = FormatHelper.toTypeString(new Object[0])
        assertEquals("", result)
    }

    @Test
    void testFormatBoolean() {
        assertEquals("true", FormatHelper.toString(true))
        assertEquals("false", FormatHelper.toString(false))
    }

    @Test
    void testFormatDouble() {
        def result = FormatHelper.toString(3.14159d)
        assertTrue(result.startsWith("3.14"))
    }

    @Test
    void testFormatLong() {
        assertEquals("9999999999", FormatHelper.toString(9999999999L))
    }

    @Test
    void testMetaRegistryNotNull() {
        assertNotNull(FormatHelper.metaRegistry)
    }
}
