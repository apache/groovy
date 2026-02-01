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
package groovy.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Reference class.
 */
class ReferenceJUnit5Test {

    @Test
    void testDefaultConstructor() {
        Reference<String> ref = new Reference<>();
        assertNull(ref.get());
    }

    @Test
    void testConstructorWithValue() {
        Reference<String> ref = new Reference<>("hello");
        assertEquals("hello", ref.get());
    }

    @Test
    void testGetAndSet() {
        Reference<Integer> ref = new Reference<>();
        assertNull(ref.get());
        
        ref.set(42);
        assertEquals(42, ref.get());
        
        ref.set(100);
        assertEquals(100, ref.get());
    }

    @Test
    void testSetNull() {
        Reference<String> ref = new Reference<>("initial");
        assertEquals("initial", ref.get());
        
        ref.set(null);
        assertNull(ref.get());
    }

    @Test
    void testWithDifferentTypes() {
        Reference<Double> doubleRef = new Reference<>(3.14);
        assertEquals(3.14, doubleRef.get());
        
        Reference<Boolean> boolRef = new Reference<>(true);
        assertTrue(boolRef.get());
        
        Reference<Object> objRef = new Reference<>(new int[]{1, 2, 3});
        assertArrayEquals(new int[]{1, 2, 3}, (int[]) objRef.get());
    }

    @Test
    void testGetPropertyOnNullValue() {
        Reference<String> ref = new Reference<>();
        // When value is null, getProperty should delegate to super
        assertThrows(MissingPropertyException.class, () -> {
            ref.getProperty("length");
        });
    }

    @Test
    void testSetPropertyOnNullValue() {
        Reference<Object> ref = new Reference<>();
        // When value is null, setProperty should delegate to super
        assertThrows(MissingPropertyException.class, () -> {
            ref.setProperty("someProp", "value");
        });
    }

    @Test
    void testInvokeMethodOnNullValue() {
        Reference<String> ref = new Reference<>();
        // When value is null, invokeMethod delegates to super (GroovyObjectSupport)
        // It throws MissingMethodException for undefined methods
        assertThrows(MissingMethodException.class, () -> {
            ref.invokeMethod("someUndefinedMethod", null);
        });
    }

    @Test
    void testReferenceIsSerializable() {
        Reference<String> ref = new Reference<>("test");
        assertTrue(ref instanceof java.io.Serializable);
    }

    @Test
    void testReferenceExtendsGroovyObjectSupport() {
        Reference<String> ref = new Reference<>();
        assertTrue(ref instanceof GroovyObjectSupport);
    }

    @Test
    void testMultipleReferences() {
        Reference<String> ref1 = new Reference<>("one");
        Reference<String> ref2 = new Reference<>("two");
        
        assertNotEquals(ref1.get(), ref2.get());
        
        ref1.set("same");
        ref2.set("same");
        assertEquals(ref1.get(), ref2.get());
    }

    @Test
    void testReferenceWithComplexObject() {
        class Person {
            String name;
            int age;
            Person(String name, int age) {
                this.name = name;
                this.age = age;
            }
        }
        
        Person person = new Person("John", 30);
        Reference<Person> ref = new Reference<>(person);
        
        assertSame(person, ref.get());
        assertEquals("John", ref.get().name);
        assertEquals(30, ref.get().age);
    }

    @Test
    void testReferenceValueMutation() {
        StringBuilder sb = new StringBuilder("initial");
        Reference<StringBuilder> ref = new Reference<>(sb);
        
        sb.append(" modified");
        
        // Since Reference holds the same object, mutation is visible
        assertEquals("initial modified", ref.get().toString());
    }
}
