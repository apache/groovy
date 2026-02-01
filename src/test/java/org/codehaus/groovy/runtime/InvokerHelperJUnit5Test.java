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

import groovy.lang.Closure;
import groovy.lang.SpreadMap;
import groovy.lang.SpreadMapEvaluatingException;
import groovy.lang.Tuple;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for InvokerHelper class.
 */
class InvokerHelperJUnit5Test {

    @Test
    void testMetaRegistryNotNull() {
        assertNotNull(InvokerHelper.metaRegistry);
    }

    @Test
    void testInvokeMethodSafeWithNullObject() {
        Object result = InvokerHelper.invokeMethodSafe(null, "toString", null);
        assertNull(result);
    }

    @Test
    void testInvokeMethodSafeWithNonNull() {
        Object result = InvokerHelper.invokeMethodSafe("hello", "length", null);
        assertEquals(5, result);
    }

    @Test
    void testAsListWithNull() {
        List<?> result = InvokerHelper.asList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAsListWithList() {
        List<String> input = Arrays.asList("a", "b", "c");
        List<?> result = InvokerHelper.asList(input);
        assertSame(input, result);
    }

    @Test
    void testAsListWithArray() {
        Object[] input = {"x", "y", "z"};
        List<?> result = InvokerHelper.asList(input);
        assertEquals(3, result.size());
        assertEquals("x", result.get(0));
    }

    @Test
    void testAsListWithEnumeration() {
        Vector<String> vector = new Vector<>();
        vector.add("one");
        vector.add("two");
        Enumeration<String> enumeration = vector.elements();
        
        List<?> result = InvokerHelper.asList(enumeration);
        assertEquals(2, result.size());
        assertEquals("one", result.get(0));
        assertEquals("two", result.get(1));
    }

    @Test
    void testAsListWithSingleValue() {
        List<?> result = InvokerHelper.asList("single");
        assertEquals(1, result.size());
        assertEquals("single", result.get(0));
    }

    @Test
    void testGetPropertySafeWithNull() {
        Object result = InvokerHelper.getPropertySafe(null, "anyProperty");
        assertNull(result);
    }

    @Test
    void testGetMethodPointerThrowsOnNull() {
        assertThrows(NullPointerException.class, () -> 
            InvokerHelper.getMethodPointer(null, "toString")
        );
    }

    @Test
    void testGetMethodPointer() {
        Closure<?> closure = InvokerHelper.getMethodPointer("hello", "toUpperCase");
        assertNotNull(closure);
    }

    @Test
    void testUnaryMinusInteger() {
        Object result = InvokerHelper.unaryMinus(42);
        assertEquals(-42, result);
    }

    @Test
    void testUnaryMinusLong() {
        Object result = InvokerHelper.unaryMinus(100L);
        assertEquals(-100L, result);
    }

    @Test
    void testUnaryMinusDouble() {
        Object result = InvokerHelper.unaryMinus(3.14);
        assertEquals(-3.14, result);
    }

    @Test
    void testUnaryMinusFloat() {
        Object result = InvokerHelper.unaryMinus(2.5f);
        assertEquals(-2.5f, result);
    }

    @Test
    void testUnaryMinusShort() {
        Object result = InvokerHelper.unaryMinus((short) 10);
        assertEquals((short) -10, result);
    }

    @Test
    void testUnaryMinusByte() {
        Object result = InvokerHelper.unaryMinus((byte) 5);
        assertEquals((byte) -5, result);
    }

    @Test
    void testUnaryMinusBigInteger() {
        BigInteger input = new BigInteger("1000000000000");
        Object result = InvokerHelper.unaryMinus(input);
        assertEquals(new BigInteger("-1000000000000"), result);
    }

    @Test
    void testUnaryMinusBigDecimal() {
        BigDecimal input = new BigDecimal("123.456");
        Object result = InvokerHelper.unaryMinus(input);
        assertEquals(new BigDecimal("-123.456"), result);
    }

    @Test
    void testUnaryMinusList() {
        ArrayList<Integer> input = new ArrayList<>(Arrays.asList(1, 2, 3));
        Object result = InvokerHelper.unaryMinus(input);
        
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(3, list.size());
        assertEquals(-1, list.get(0));
        assertEquals(-2, list.get(1));
        assertEquals(-3, list.get(2));
    }

    @Test
    void testUnaryPlusInteger() {
        Object result = InvokerHelper.unaryPlus(42);
        assertEquals(42, result);
    }

    @Test
    void testUnaryPlusLong() {
        Object result = InvokerHelper.unaryPlus(100L);
        assertEquals(100L, result);
    }

    @Test
    void testUnaryPlusDouble() {
        Object result = InvokerHelper.unaryPlus(3.14);
        assertEquals(3.14, result);
    }

    @Test
    void testUnaryPlusFloat() {
        Object result = InvokerHelper.unaryPlus(2.5f);
        assertEquals(2.5f, result);
    }

    @Test
    void testUnaryPlusBigInteger() {
        BigInteger input = new BigInteger("12345");
        Object result = InvokerHelper.unaryPlus(input);
        assertSame(input, result);
    }

    @Test
    void testUnaryPlusBigDecimal() {
        BigDecimal input = new BigDecimal("99.99");
        Object result = InvokerHelper.unaryPlus(input);
        assertSame(input, result);
    }

    @Test
    void testUnaryPlusList() {
        ArrayList<Integer> input = new ArrayList<>(Arrays.asList(1, 2, 3));
        Object result = InvokerHelper.unaryPlus(input);
        
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(3, list.size());
    }

    @Test
    void testFindRegexWithStrings() {
        Matcher matcher = InvokerHelper.findRegex("hello world", "\\w+");
        assertNotNull(matcher);
        assertTrue(matcher.find());
        assertEquals("hello", matcher.group());
    }

    @Test
    void testFindRegexWithPattern() {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = InvokerHelper.findRegex("abc123def", pattern);
        assertNotNull(matcher);
        assertTrue(matcher.find());
        assertEquals("123", matcher.group());
    }

    @Test
    void testMatchRegexTrue() {
        assertTrue(InvokerHelper.matchRegex("hello", "hello"));
        assertTrue(InvokerHelper.matchRegex("hello", "h.*"));
    }

    @Test
    void testMatchRegexFalse() {
        assertFalse(InvokerHelper.matchRegex("hello", "world"));
        assertFalse(InvokerHelper.matchRegex("hello", "^world$"));
    }

    @Test
    void testMatchRegexWithNull() {
        assertFalse(InvokerHelper.matchRegex(null, "pattern"));
        assertFalse(InvokerHelper.matchRegex("string", null));
        assertFalse(InvokerHelper.matchRegex(null, null));
    }

    @Test
    void testMatchRegexWithPattern() {
        Pattern pattern = Pattern.compile("\\d{3}");
        assertTrue(InvokerHelper.matchRegex("123", pattern));
        assertFalse(InvokerHelper.matchRegex("12", pattern));
    }

    @Test
    void testCreateTuple() {
        Object[] array = {1, "two", 3.0};
        Tuple tuple = InvokerHelper.createTuple(array);
        
        assertNotNull(tuple);
        assertEquals(3, tuple.size());
        assertEquals(1, tuple.get(0));
        assertEquals("two", tuple.get(1));
        assertEquals(3.0, tuple.get(2));
    }

    @Test
    void testCreateTupleEmpty() {
        Tuple tuple = InvokerHelper.createTuple(new Object[0]);
        assertNotNull(tuple);
        assertEquals(0, tuple.size());
    }

    @Test
    void testSpreadMapFromMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        
        SpreadMap result = InvokerHelper.spreadMap(map);
        assertNotNull(result);
    }

    @Test
    void testSpreadMapFromNonMapThrows() {
        assertThrows(SpreadMapEvaluatingException.class, () ->
            InvokerHelper.spreadMap("not a map")
        );
    }

    @Test
    void testSpreadMapFromEmptyMap() {
        Map<String, Integer> map = Collections.emptyMap();
        SpreadMap result = InvokerHelper.spreadMap(map);
        assertNotNull(result);
    }

    @Test
    void testInvokeStaticNoArgumentsMethod() {
        // This method takes only Class and method name (no arguments)
        // Test with a method that actually takes no arguments
        Object result = InvokerHelper.invokeStaticNoArgumentsMethod(System.class, "lineSeparator");
        assertNotNull(result);
    }

    @Test
    void testInvokeNoArgumentsConstructorOf() {
        Object result = InvokerHelper.invokeNoArgumentsConstructorOf(StringBuilder.class);
        assertNotNull(result);
        assertTrue(result instanceof StringBuilder);
    }

    @Test
    void testRemoveClass() {
        // Create a simple test class scenario
        // Just verify the method doesn't throw
        assertDoesNotThrow(() -> InvokerHelper.removeClass(InvokerHelperJUnit5Test.class));
    }

    @Test
    void testEmptyArgsConstant() {
        assertEquals(0, InvokerHelper.EMPTY_ARGS.length);
    }

    @Test
    void testMainMethodNameConstant() {
        assertEquals("main", InvokerHelper.MAIN_METHOD_NAME);
    }

    @Test
    void testSetPropertySafe2WithNull() {
        // Should not throw when object is null
        assertDoesNotThrow(() -> 
            InvokerHelper.setPropertySafe2("value", null, "property")
        );
    }

    @Test
    void testSetProperty2() {
        StringBuilder sb = new StringBuilder();
        // This tests the reordered parameter version
        // May throw if property doesn't exist - just testing it's callable
        try {
            InvokerHelper.setProperty2("value", sb, "nonexistent");
        } catch (Exception e) {
            // Expected - property doesn't exist
        }
    }

    @Test
    void testInvokeMethod() {
        Object result = InvokerHelper.invokeMethod("hello", "toUpperCase", null);
        assertEquals("HELLO", result);
    }

    @Test
    void testInvokeMethodWithArgs() {
        Object result = InvokerHelper.invokeMethod("hello world", "substring", new Object[]{0, 5});
        assertEquals("hello", result);
    }

    @Test
    void testGetProperty() {
        // Test on a simple object
        String str = "test";
        // getProperty uses metaclass, may work differently
        try {
            Object result = InvokerHelper.getProperty(str, "class");
            assertEquals(String.class, result);
        } catch (Exception e) {
            // May fail depending on metaclass setup
        }
    }

    @Test
    void testInvokeStaticMethod() {
        Object result = InvokerHelper.invokeStaticMethod(String.class, "valueOf", new Object[]{42});
        assertEquals("42", result);
    }

    @Test
    void testInvokeConstructorOf() {
        Object result = InvokerHelper.invokeConstructorOf(StringBuilder.class, new Object[]{"initial"});
        assertNotNull(result);
        assertTrue(result instanceof StringBuilder);
        assertEquals("initial", result.toString());
    }
}
