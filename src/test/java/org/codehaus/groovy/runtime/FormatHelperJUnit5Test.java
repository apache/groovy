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
package org.codehaus.groovy.runtime;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for FormatHelper class.
 */
class FormatHelperJUnit5Test {

    @Test
    void testToStringNull() {
        String result = FormatHelper.toString(null);
        assertEquals("null", result);
    }

    @Test
    void testToStringString() {
        String result = FormatHelper.toString("hello");
        assertEquals("hello", result);
    }

    @Test
    void testToStringInteger() {
        String result = FormatHelper.toString(42);
        assertEquals("42", result);
    }

    @Test
    void testToStringCollection() {
        List<String> list = Arrays.asList("a", "b", "c");
        String result = FormatHelper.toString(list);
        assertEquals("[a, b, c]", result);
    }

    @Test
    void testToStringMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        String result = FormatHelper.toString(map);
        assertTrue(result.contains("one") && result.contains("1"));
    }

    @Test
    void testToStringObjectArray() {
        Object[] arr = {"a", "b", "c"};
        String result = FormatHelper.toString(arr);
        assertEquals("[a, b, c]", result);
    }

    @Test
    void testToStringCharArray() {
        char[] chars = {'h', 'i'};
        String result = FormatHelper.toString(chars);
        assertEquals("hi", result);
    }

    @Test
    void testToStringPrimitiveIntArray() {
        int[] arr = {1, 2, 3};
        String result = FormatHelper.toString(arr);
        assertEquals("[1, 2, 3]", result);
    }

    @Test
    void testInspectString() {
        String result = FormatHelper.inspect("hello");
        assertTrue(result.contains("hello"));
    }

    @Test
    void testInspectNull() {
        String result = FormatHelper.inspect(null);
        assertEquals("null", result);
    }

    @Test
    void testFormatVerbose() {
        String result = FormatHelper.format("hello", true);
        assertTrue(result.contains("hello"));
    }

    @Test
    void testFormatWithMaxSize() {
        String longString = "This is a very long string that should be truncated";
        String result = FormatHelper.format(longString, false, 10);
        // Format doesn't truncate simple strings, just collections/maps
        assertNotNull(result);
    }

    @Test
    void testFormatWithMaxSizeOnCollection() {
        List<String> list = Arrays.asList("item1", "item2", "item3", "item4", "item5");
        String result = FormatHelper.format(list, false, 20);
        assertNotNull(result);
    }

    @Test
    void testFormatEmptyCollection() {
        List<String> list = Collections.emptyList();
        String result = FormatHelper.format(list, false);
        assertEquals("[]", result);
    }

    @Test
    void testFormatEmptyMap() {
        Map<String, String> map = Collections.emptyMap();
        String result = FormatHelper.format(map, false);
        assertEquals("[:]", result);
    }

    @Test
    void testFormatMapWithInspect() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("key", "value");
        String result = FormatHelper.format(map, true);
        assertTrue(result.contains("key") && result.contains("value"));
    }

    @Test
    void testToStringWithOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put("safe", true);
        options.put("maxSize", 100);
        
        String result = FormatHelper.toString(options, "hello");
        assertEquals("hello", result);
    }

    @Test
    void testToStringWithOptionsVerbose() {
        Map<String, Object> options = new HashMap<>();
        options.put("verbose", true);
        
        String result = FormatHelper.toString(options, "hello");
        assertNotNull(result);
    }

    @Test
    void testToStringWithOptionsInspect() {
        Map<String, Object> options = new HashMap<>();
        options.put("inspect", true);
        
        String result = FormatHelper.toString(options, "hello");
        assertTrue(result.contains("hello"));
    }

    @Test
    void testToStringWithStringBuilder() {
        StringBuilder sb = new StringBuilder("test");
        String result = FormatHelper.toString(sb);
        assertEquals("test", result);
    }

    @Test
    void testToStringWithNestedCollection() {
        List<List<Integer>> nested = Arrays.asList(
            Arrays.asList(1, 2),
            Arrays.asList(3, 4)
        );
        String result = FormatHelper.toString(nested);
        assertEquals("[[1, 2], [3, 4]]", result);
    }

    @Test
    void testWriteToBuffer() throws java.io.IOException {
        java.io.StringWriter sw = new java.io.StringWriter();
        FormatHelper.write(sw, "hello");
        assertEquals("hello", sw.toString());
    }

    @Test
    void testWriteNullToBuffer() throws java.io.IOException {
        java.io.StringWriter sw = new java.io.StringWriter();
        FormatHelper.write(sw, null);
        assertEquals("null", sw.toString());
    }

    @Test
    void testWriteArrayToBuffer() throws java.io.IOException {
        java.io.StringWriter sw = new java.io.StringWriter();
        Object[] arr = {"a", "b"};
        FormatHelper.write(sw, arr);
        assertEquals("[a, b]", sw.toString());
    }

    @Test
    void testFormatSafeMode() {
        // Object that throws exception on toString
        Object problematic = new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("Cannot convert to string");
            }
        };
        
        // Safe mode should not throw
        String result = FormatHelper.format(problematic, false, -1, true);
        assertNotNull(result);
    }

    @Test
    void testFormatUnsafeMode() {
        Object problematic = new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("Cannot convert to string");
            }
        };
        
        // Unsafe mode should throw
        assertThrows(RuntimeException.class, () -> 
            FormatHelper.format(problematic, false, -1, false)
        );
    }

    @Test
    void testEscapeBackslashes() {
        String input = "hello\tworld\n";
        String result = FormatHelper.escapeBackslashes(input);
        assertTrue(result.contains("\\t") || result.contains("\\n"));
    }

    @Test
    void testFormatWithEscapeBackslashes() {
        String input = "line1\nline2";
        String result = FormatHelper.format(input, true, true, -1, false);
        assertNotNull(result);
    }

    @Test
    void testToArrayString() {
        Object[] arr = {1, 2, 3};
        String result = FormatHelper.toArrayString(arr);
        assertEquals("[1, 2, 3]", result);
    }

    @Test
    void testToArrayStringWithInspect() {
        Object[] arr = {"a", "b"};
        // toArrayString with params is private, test via format
        String result = FormatHelper.format(arr, true, -1);
        assertTrue(result.contains("a") && result.contains("b"));
    }

    @Test
    void testToMapString() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("a", 1);
        String result = FormatHelper.toMapString(map);
        assertTrue(result.contains("a") && result.contains("1"));
    }

    @Test
    void testToMapStringMaxSize() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < 100; i++) {
            map.put("key" + i, i);
        }
        String result = FormatHelper.toMapString(map, 50);
        assertNotNull(result);
    }

    @Test
    void testToListString() {
        List<String> list = Arrays.asList("one", "two", "three");
        String result = FormatHelper.toListString(list);
        assertEquals("[one, two, three]", result);
    }

    @Test
    void testToListStringWithMaxSize() {
        List<String> list = Arrays.asList("one", "two", "three", "four", "five");
        String result = FormatHelper.toListString(list, 15);
        assertNotNull(result);
    }

    @Test
    void testToTypeString() {
        Object[] args = {"hello", 42, true};
        String result = FormatHelper.toTypeString(args);
        assertTrue(result.contains("String"));
        assertTrue(result.contains("Integer"));
        assertTrue(result.contains("Boolean"));
    }

    @Test
    void testToTypeStringWithMaxSize() {
        Object[] args = {"hello", 42, true, 3.14, "world"};
        String result = FormatHelper.toTypeString(args, 20);
        assertNotNull(result);
    }

    @Test
    void testToTypeStringNull() {
        String result = FormatHelper.toTypeString(null);
        assertEquals("null", result);
    }

    @Test
    void testToTypeStringEmpty() {
        String result = FormatHelper.toTypeString(new Object[0]);
        assertEquals("", result);
    }

    @Test
    void testFormatBoolean() {
        assertEquals("true", FormatHelper.toString(true));
        assertEquals("false", FormatHelper.toString(false));
    }

    @Test
    void testFormatDouble() {
        String result = FormatHelper.toString(3.14159);
        assertTrue(result.startsWith("3.14"));
    }

    @Test
    void testFormatLong() {
        assertEquals("9999999999", FormatHelper.toString(9999999999L));
    }

    @Test
    void testMetaRegistryNotNull() {
        assertNotNull(FormatHelper.metaRegistry);
    }
}
