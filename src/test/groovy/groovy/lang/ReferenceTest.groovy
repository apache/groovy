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
package groovy.lang

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

/**
 * JUnit 5 tests for Reference class.
 */
class ReferenceTest {

    @Test
    void testDefaultConstructor() {
        def ref = new Reference<String>()
        assertNull(ref.get())
    }

    @Test
    void testConstructorWithValue() {
        def ref = new Reference<String>("hello")
        assertEquals("hello", ref.get())
    }

    @Test
    void testGetAndSet() {
        def ref = new Reference<Integer>()
        assertNull(ref.get())

        ref.set(42)
        assertEquals(42, ref.get())

        ref.set(100)
        assertEquals(100, ref.get())
    }

    @Test
    void testSetNull() {
        def ref = new Reference<String>("initial")
        assertEquals("initial", ref.get())

        ref.set(null)
        assertNull(ref.get())
    }

    @Test
    void testWithDifferentTypes() {
        def doubleRef = new Reference<Double>(3.14)
        assertEquals(3.14, doubleRef.get())

        def boolRef = new Reference<Boolean>(true)
        assertTrue(boolRef.get())

        def objRef = new Reference<Object>([1, 2, 3] as int[])
        assertArrayEquals([1, 2, 3] as int[], (int[]) objRef.get())
    }

    @Test
    void testGetPropertyOnNullValue() {
        def ref = new Reference<String>()
        // When value is null, getProperty should delegate to super
        assertThrows(MissingPropertyException, { ->
            ref.getProperty("length")
        })
    }

    @Test
    void testSetPropertyOnNullValue() {
        def ref = new Reference<Object>()
        // When value is null, setProperty should delegate to super
        assertThrows(MissingPropertyException, { ->
            ref.setProperty("someProp", "value")
        })
    }

    @Test
    void testInvokeMethodOnNullValue() {
        def ref = new Reference<String>()
        // When value is null, invokeMethod delegates to super (GroovyObjectSupport)
        // It throws MissingMethodException for undefined methods
        assertThrows(MissingMethodException, { ->
            ref.invokeMethod("someUndefinedMethod", null)
        })
    }

    @Test
    void testReferenceIsSerializable() {
        def ref = new Reference<String>("test")
        assertTrue(ref instanceof java.io.Serializable)
    }

    @Test
    void testReferenceExtendsGroovyObjectSupport() {
        def ref = new Reference<String>()
        assertTrue(ref instanceof GroovyObjectSupport)
    }

    @Test
    void testMultipleReferences() {
        def ref1 = new Reference<String>("one")
        def ref2 = new Reference<String>("two")

        assertNotEquals(ref1.get(), ref2.get())

        ref1.set("same")
        ref2.set("same")
        assertEquals(ref1.get(), ref2.get())
    }

    @Test
    void testReferenceWithComplexObject() {
        def person = new PersonForTest("John", 30)
        def ref = new Reference<PersonForTest>(person)

        assertSame(person, ref.get())
        assertEquals("John", ref.get().name)
        assertEquals(30, ref.get().age)
    }

    @Test
    void testReferenceValueMutation() {
        def sb = new StringBuilder("initial")
        def ref = new Reference<StringBuilder>(sb)

        sb.append(" modified")

        // Since Reference holds the same object, mutation is visible
        assertEquals("initial modified", ref.get().toString())
    }

    // Helper class moved to class level for Groovy compatibility
    static class PersonForTest {
        String name
        int age
        PersonForTest(String name, int age) {
            this.name = name
            this.age = age
        }
    }
}
