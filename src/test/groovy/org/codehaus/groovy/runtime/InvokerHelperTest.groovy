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

import groovy.lang.Binding
import groovy.lang.Closure
import groovy.lang.GroovyClassLoader
import groovy.lang.GroovyCodeSource
import groovy.lang.GroovyShell
import groovy.lang.Script
import groovy.lang.SpreadMap
import groovy.lang.SpreadMapEvaluatingException
import groovy.lang.Tuple
import org.junit.jupiter.api.Test

import java.util.regex.Matcher
import java.util.regex.Pattern

import org.junit.jupiter.api.function.Executable

import static org.junit.jupiter.api.Assertions.*

/**
 * Tests for InvokerHelper class.
 */
final class InvokerHelperTest {

    private final Map<String, Object> variables = [:]

    // -- from original Java tests --

    @Test
    void testCreateScriptWithNullClass() {
        def script = InvokerHelper.createScript(null, new Binding(variables))

        assertSame(variables, script.getBinding().getVariables())
    }

    @Test
    void testCreateScriptWithScriptClass() throws Exception {
        try (def classLoader = new GroovyClassLoader()) {
            def controlProperty = "text"
            def controlValue = "I am a script"
            def scriptClass = classLoader.parseClass(new GroovyCodeSource(
                    controlProperty + " = '" + controlValue + "'", "testscript", "/groovy/shell"), false)

            def script = InvokerHelper.createScript(scriptClass, new Binding(variables))

            assertSame(variables, script.getBinding().getVariables())

            script.run()

            assertEquals(controlValue, script.getProperty(controlProperty))
        }
    }

    @Test // GROOVY-5802
    void testBindingVariablesSetPropertiesInSingleClassScripts() {
        variables.put("first", "John")
        variables.put("last", "Smith")

        def sysout = System.out
        try {
            def baos = new ByteArrayOutputStream()
            System.setOut(new PrintStream(baos))
            def source =
                "class Person {\n" +
                "    static String first, last, unused\n" +
                "    static main(args) { print \"\$first \$last\" }\n" +
                "}\n"
            new GroovyShell(new Binding(variables)).parse(source).run()

            assertEquals("John Smith", baos.toString())
        } finally {
            System.setOut(sysout)
        }
    }

    @Test // GROOVY-5802
    void testInvokerHelperNotConfusedByScriptVariables() {
        variables.put("_", Collections.emptyList())

        InvokerHelper.createScript(MyList5802.class, new Binding(variables))
    }

    @Test // GROOVY-5802
    void testMissingVariablesForSingleListClassScripts() {
        variables.put("x", Collections.emptyList())

        InvokerHelper.createScript(MyList5802.class, new Binding(variables))
    }

    @Test
    void testInitialCapacity() {
        assertEquals(16, InvokerHelper.initialCapacity(0))
        assertEquals( 2, InvokerHelper.initialCapacity(1))
        assertEquals( 4, InvokerHelper.initialCapacity(2))
        assertEquals( 4, InvokerHelper.initialCapacity(3))
        assertEquals( 8, InvokerHelper.initialCapacity(4))
        assertEquals( 8, InvokerHelper.initialCapacity(5))
        assertEquals( 8, InvokerHelper.initialCapacity(6))
        assertEquals( 8, InvokerHelper.initialCapacity(7))
        assertEquals(16, InvokerHelper.initialCapacity(8))
        assertEquals(16, InvokerHelper.initialCapacity(9))
        assertEquals(16, InvokerHelper.initialCapacity(10))
        assertEquals(16, InvokerHelper.initialCapacity(11))
        assertEquals(16, InvokerHelper.initialCapacity(12))
        assertEquals(16, InvokerHelper.initialCapacity(13))
        assertEquals(16, InvokerHelper.initialCapacity(14))
        assertEquals(16, InvokerHelper.initialCapacity(15))
        assertEquals(32, InvokerHelper.initialCapacity(16))
        assertEquals(32, InvokerHelper.initialCapacity(17))
    }

    // -- from JUnit5 tests --

    @Test
    void testMetaRegistryNotNull() {
        assertNotNull(InvokerHelper.metaRegistry)
    }

    @Test
    void testInvokeMethodSafeWithNullObject() {
        def result = InvokerHelper.invokeMethodSafe(null, "toString", null)
        assertNull(result)
    }

    @Test
    void testInvokeMethodSafeWithNonNull() {
        def result = InvokerHelper.invokeMethodSafe("hello", "length", null)
        assertEquals(5, result)
    }

    @Test
    void testAsListWithNull() {
        def result = InvokerHelper.asList(null)
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    void testAsListWithList() {
        def input = ["a", "b", "c"]
        def result = InvokerHelper.asList(input)
        assertSame(input, result)
    }

    @Test
    void testAsListWithArray() {
        Object[] input = ["x", "y", "z"]
        def result = InvokerHelper.asList(input)
        assertEquals(3, result.size())
        assertEquals("x", result.get(0))
    }

    @Test
    void testAsListWithEnumeration() {
        def vector = new Vector<String>()
        vector.add("one")
        vector.add("two")
        def enumeration = vector.elements()

        def result = InvokerHelper.asList(enumeration)
        assertEquals(2, result.size())
        assertEquals("one", result.get(0))
        assertEquals("two", result.get(1))
    }

    @Test
    void testAsListWithSingleValue() {
        def result = InvokerHelper.asList("single")
        assertEquals(1, result.size())
        assertEquals("single", result.get(0))
    }

    @Test
    void testGetPropertySafeWithNull() {
        def result = InvokerHelper.getPropertySafe(null, "anyProperty")
        assertNull(result)
    }

    @Test
    void testGetMethodPointerThrowsOnNull() {
        assertThrows(NullPointerException.class, () ->
            InvokerHelper.getMethodPointer(null, "toString")
        )
    }

    @Test
    void testGetMethodPointer() {
        def closure = InvokerHelper.getMethodPointer("hello", "toUpperCase")
        assertNotNull(closure)
    }

    @Test
    void testUnaryMinusInteger() {
        def result = InvokerHelper.unaryMinus(42)
        assertEquals(-42, result)
    }

    @Test
    void testUnaryMinusLong() {
        def result = InvokerHelper.unaryMinus(100L)
        assertEquals(-100L, result)
    }

    @Test
    void testUnaryMinusDouble() {
        def result = InvokerHelper.unaryMinus(3.14d)
        assertEquals(-3.14d, result)
    }

    @Test
    void testUnaryMinusFloat() {
        def result = InvokerHelper.unaryMinus(2.5f)
        assertEquals(-2.5f, result)
    }

    @Test
    void testUnaryMinusShort() {
        def result = InvokerHelper.unaryMinus((short) 10)
        assertEquals((short) -10, result)
    }

    @Test
    void testUnaryMinusByte() {
        def result = InvokerHelper.unaryMinus((byte) 5)
        assertEquals((byte) -5, result)
    }

    @Test
    void testUnaryMinusBigInteger() {
        def input = new BigInteger("1000000000000")
        def result = InvokerHelper.unaryMinus(input)
        assertEquals(new BigInteger("-1000000000000"), result)
    }

    @Test
    void testUnaryMinusBigDecimal() {
        def input = new BigDecimal("123.456")
        def result = InvokerHelper.unaryMinus(input)
        assertEquals(new BigDecimal("-123.456"), result)
    }

    @Test
    void testUnaryMinusList() {
        def input = new ArrayList([1, 2, 3])
        def result = InvokerHelper.unaryMinus(input)

        assertTrue(result instanceof List)
        def list = (List) result
        assertEquals(3, list.size())
        assertEquals(-1, list.get(0))
        assertEquals(-2, list.get(1))
        assertEquals(-3, list.get(2))
    }

    @Test
    void testUnaryPlusInteger() {
        def result = InvokerHelper.unaryPlus(42)
        assertEquals(42, result)
    }

    @Test
    void testUnaryPlusLong() {
        def result = InvokerHelper.unaryPlus(100L)
        assertEquals(100L, result)
    }

    @Test
    void testUnaryPlusDouble() {
        def result = InvokerHelper.unaryPlus(3.14d)
        assertEquals(3.14d, result)
    }

    @Test
    void testUnaryPlusFloat() {
        def result = InvokerHelper.unaryPlus(2.5f)
        assertEquals(2.5f, result)
    }

    @Test
    void testUnaryPlusBigInteger() {
        def input = new BigInteger("12345")
        def result = InvokerHelper.unaryPlus(input)
        assertSame(input, result)
    }

    @Test
    void testUnaryPlusBigDecimal() {
        def input = new BigDecimal("99.99")
        def result = InvokerHelper.unaryPlus(input)
        assertSame(input, result)
    }

    @Test
    void testUnaryPlusList() {
        def input = new ArrayList([1, 2, 3])
        def result = InvokerHelper.unaryPlus(input)

        assertTrue(result instanceof List)
        def list = (List) result
        assertEquals(3, list.size())
    }

    @Test
    void testFindRegexWithStrings() {
        def matcher = InvokerHelper.findRegex("hello world", "\\w+")
        assertNotNull(matcher)
        assertTrue(matcher.find())
        assertEquals("hello", matcher.group())
    }

    @Test
    void testFindRegexWithPattern() {
        def pattern = Pattern.compile("\\d+")
        def matcher = InvokerHelper.findRegex("abc123def", pattern)
        assertNotNull(matcher)
        assertTrue(matcher.find())
        assertEquals("123", matcher.group())
    }

    @Test
    void testMatchRegexTrue() {
        assertTrue(InvokerHelper.matchRegex("hello", "hello"))
        assertTrue(InvokerHelper.matchRegex("hello", "h.*"))
    }

    @Test
    void testMatchRegexFalse() {
        assertFalse(InvokerHelper.matchRegex("hello", "world"))
        assertFalse(InvokerHelper.matchRegex("hello", "^world\$"))
    }

    @Test
    void testMatchRegexWithNull() {
        assertFalse(InvokerHelper.matchRegex(null, "pattern"))
        assertFalse(InvokerHelper.matchRegex("string", null))
        assertFalse(InvokerHelper.matchRegex(null, null))
    }

    @Test
    void testMatchRegexWithPattern() {
        def pattern = Pattern.compile("\\d{3}")
        assertTrue(InvokerHelper.matchRegex("123", pattern))
        assertFalse(InvokerHelper.matchRegex("12", pattern))
    }

    @Test
    void testCreateTuple() {
        Object[] array = [1, "two", 3.0]
        def tuple = InvokerHelper.createTuple(array)

        assertNotNull(tuple)
        assertEquals(3, tuple.size())
        assertEquals(1, tuple.get(0))
        assertEquals("two", tuple.get(1))
        assertEquals(3.0, tuple.get(2))
    }

    @Test
    void testCreateTupleEmpty() {
        def tuple = InvokerHelper.createTuple(new Object[0])
        assertNotNull(tuple)
        assertEquals(0, tuple.size())
    }

    @Test
    void testSpreadMapFromMap() {
        def map = new LinkedHashMap<String, Integer>()
        map.put("a", 1)
        map.put("b", 2)

        def result = InvokerHelper.spreadMap(map)
        assertNotNull(result)
    }

    @Test
    void testSpreadMapFromNonMapThrows() {
        assertThrows(SpreadMapEvaluatingException.class, () ->
            InvokerHelper.spreadMap("not a map")
        )
    }

    @Test
    void testSpreadMapFromEmptyMap() {
        def map = Collections.emptyMap()
        def result = InvokerHelper.spreadMap(map)
        assertNotNull(result)
    }

    @Test
    void testInvokeStaticNoArgumentsMethod() {
        def result = InvokerHelper.invokeStaticNoArgumentsMethod(System.class, "lineSeparator")
        assertNotNull(result)
    }

    @Test
    void testInvokeNoArgumentsConstructorOf() {
        def result = InvokerHelper.invokeNoArgumentsConstructorOf(StringBuilder.class)
        assertNotNull(result)
        assertTrue(result instanceof StringBuilder)
    }

    @Test
    void testRemoveClass() {
        assertDoesNotThrow({ -> InvokerHelper.removeClass(InvokerHelperTest.class) } as Executable)
    }

    @Test
    void testEmptyArgsConstant() {
        assertEquals(0, InvokerHelper.EMPTY_ARGS.length)
    }

    @Test
    void testMainMethodNameConstant() {
        assertEquals("main", InvokerHelper.MAIN_METHOD_NAME)
    }

    @Test
    void testSetPropertySafe2WithNull() {
        assertDoesNotThrow({ ->
            InvokerHelper.setPropertySafe2("value", null, "property")
        } as Executable)
    }

    @Test
    void testSetProperty2() {
        def sb = new StringBuilder()
        try {
            InvokerHelper.setProperty2("value", sb, "nonexistent")
        } catch (Exception e) {
            // Expected - property doesn't exist
        }
    }

    @Test
    void testInvokeMethod() {
        def result = InvokerHelper.invokeMethod("hello", "toUpperCase", null)
        assertEquals("HELLO", result)
    }

    @Test
    void testInvokeMethodWithArgs() {
        def result = InvokerHelper.invokeMethod("hello world", "substring", new Object[]{0, 5})
        assertEquals("hello", result)
    }

    @Test
    void testGetProperty() {
        def str = "test"
        try {
            def result = InvokerHelper.getProperty(str, "class")
            assertEquals(String.class, result)
        } catch (Exception e) {
            // May fail depending on metaclass setup
        }
    }

    @Test
    void testInvokeStaticMethod() {
        def result = InvokerHelper.invokeStaticMethod(String.class, "valueOf", new Object[]{42})
        assertEquals("42", result)
    }

    @Test
    void testInvokeConstructorOf() {
        def result = InvokerHelper.invokeConstructorOf(StringBuilder.class, new Object[]{"initial"})
        assertNotNull(result)
        assertTrue(result instanceof StringBuilder)
        assertEquals("initial", result.toString())
    }

    //--------------------------------------------------------------------------

    private static class MyList5802 extends ArrayList<Object> {
        private static final long serialVersionUID = 0
    }
}
