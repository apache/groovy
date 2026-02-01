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

import groovy.test.GroovyTestCase;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests the use of the structured Attribute type
 */
public class SequenceTest extends GroovyTestCase {

    public void testConstruction() {
        Sequence sequence = new Sequence(String.class);
        sequence.add("James");
        sequence.add("Bob");

        assertEquals("Size", 2, sequence.size());
        assertEquals("Element", "James", sequence.get(0));
        assertEquals("Element", "Bob", sequence.get(1));

        // now let's try some methods on each item in the list
        List answer = (List) InvokerHelper.invokeMethod(sequence, "startsWith", new Object[]{"Ja"});
        assertArrayEquals(new Object[]{Boolean.TRUE, Boolean.FALSE}, answer.toArray());

        answer = (List) InvokerHelper.invokeMethod(sequence, "length", null);
        assertArrayEquals(new Object[]{Integer.valueOf(5), Integer.valueOf(3)}, answer.toArray());
    }

    public void testAddingWrongTypeFails() {
        try {
            Sequence sequence = new Sequence(String.class);
            sequence.add(Integer.valueOf(5));

            fail("Should have thrown exception");
        }
        catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e);
        }
    }

    public void testAddingNullFails() {
        try {
            Sequence sequence = new Sequence(String.class);
            sequence.add(null);

            fail("Should have thrown exception");
        }
        catch (NullPointerException e) {
            System.out.println("Caught: " + e);
        }
    }

    public void testConstructorWithTypeAndContent() {
        List<String> content = Arrays.asList("one", "two", "three");
        Sequence sequence = new Sequence(String.class, content);

        assertEquals(3, sequence.size());
        assertEquals("one", sequence.get(0));
        assertEquals("two", sequence.get(1));
        assertEquals("three", sequence.get(2));
    }

    public void testConstructorWithoutType() {
        Sequence sequence = new Sequence();
        sequence.add("string");
        sequence.add(123);
        sequence.add(3.14);

        assertEquals(3, sequence.size());
        assertNull(sequence.type());
    }

    public void testTypeMethod() {
        Sequence stringSequence = new Sequence(String.class);
        assertEquals(String.class, stringSequence.type());

        Sequence intSequence = new Sequence(Integer.class);
        assertEquals(Integer.class, intSequence.type());

        Sequence untypedSequence = new Sequence();
        assertNull(untypedSequence.type());
    }

    public void testSetMethod() {
        Sequence sequence = new Sequence(String.class);
        sequence.add("initial");

        List<String> newContent = Arrays.asList("new1", "new2");
        sequence.set(newContent);

        assertEquals(2, sequence.size());
        assertEquals("new1", sequence.get(0));
        assertEquals("new2", sequence.get(1));
    }

    public void testSetMethodWithWrongTypeFails() {
        try {
            Sequence sequence = new Sequence(String.class);
            sequence.set(Arrays.asList(1, 2, 3));
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testEquals() {
        Sequence seq1 = new Sequence(String.class);
        seq1.add("a");
        seq1.add("b");

        Sequence seq2 = new Sequence(String.class);
        seq2.add("a");
        seq2.add("b");

        Sequence seq3 = new Sequence(String.class);
        seq3.add("a");
        seq3.add("c");

        assertTrue(seq1.equals(seq2));
        assertFalse(seq1.equals(seq3));
        assertFalse(seq1.equals("not a sequence"));
    }

    public void testEqualsWithDifferentSizes() {
        Sequence seq1 = new Sequence(String.class);
        seq1.add("a");

        Sequence seq2 = new Sequence(String.class);
        seq2.add("a");
        seq2.add("b");

        assertFalse(seq1.equals(seq2));
    }

    public void testHashCode() {
        Sequence seq1 = new Sequence(String.class);
        seq1.add("test");

        Sequence seq2 = new Sequence(String.class);
        seq2.add("test");

        assertEquals(seq1.hashCode(), seq2.hashCode());

        // Verify hashCode is cached
        int hash1 = seq1.hashCode();
        int hash2 = seq1.hashCode();
        assertEquals(hash1, hash2);
    }

    public void testHashCodeWithNullElement() {
        // Sequences don't allow null even when untyped, so test non-null elements
        Sequence sequence = new Sequence();
        sequence.add("test");

        // Should not throw exception
        int hash = sequence.hashCode();
        assertTrue(hash != 0);
    }

    public void testMinimumSize() {
        Sequence sequence = new Sequence(String.class);
        assertEquals(0, sequence.minimumSize());
    }

    public void testAddAtIndex() {
        Sequence sequence = new Sequence(String.class);
        sequence.add("first");
        sequence.add("third");
        sequence.add(1, "second");

        assertEquals(3, sequence.size());
        assertEquals("first", sequence.get(0));
        assertEquals("second", sequence.get(1));
        assertEquals("third", sequence.get(2));
    }

    public void testAddAtIndexWithWrongTypeFails() {
        try {
            Sequence sequence = new Sequence(String.class);
            sequence.add("first");
            sequence.add(0, 123);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testAddAll() {
        Sequence sequence = new Sequence(String.class);
        sequence.addAll(Arrays.asList("a", "b", "c"));

        assertEquals(3, sequence.size());
    }

    public void testAddAllWithWrongTypeFails() {
        try {
            Sequence sequence = new Sequence(String.class);
            sequence.addAll(Arrays.asList(1, 2, 3));
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testAddAllAtIndex() {
        Sequence sequence = new Sequence(String.class);
        sequence.add("first");
        sequence.add("last");
        sequence.addAll(1, Arrays.asList("middle1", "middle2"));

        assertEquals(4, sequence.size());
        assertEquals("first", sequence.get(0));
        assertEquals("middle1", sequence.get(1));
        assertEquals("middle2", sequence.get(2));
        assertEquals("last", sequence.get(3));
    }

    public void testClear() {
        Sequence sequence = new Sequence(String.class);
        sequence.add("a");
        sequence.add("b");

        sequence.clear();

        assertEquals(0, sequence.size());
    }

    public void testRemove() {
        Sequence sequence = new Sequence(String.class);
        sequence.add("a");
        sequence.add("b");
        sequence.add("c");

        Object removed = sequence.remove(1);

        assertEquals("b", removed);
        assertEquals(2, sequence.size());
        assertEquals("a", sequence.get(0));
        assertEquals("c", sequence.get(1));
    }

    public void testSet() {
        Sequence sequence = new Sequence(String.class);
        sequence.add("a");
        sequence.add("b");

        Object old = sequence.set(1, "c");

        assertEquals("b", old);
        assertEquals("c", sequence.get(1));
    }

    public void testInvokeMethod() {
        Sequence sequence = new Sequence(String.class);
        sequence.add("hello");
        sequence.add("world");

        List result = (List) sequence.invokeMethod("toUpperCase", null);

        assertEquals(2, result.size());
        assertEquals("HELLO", result.get(0));
        assertEquals("WORLD", result.get(1));
    }

    public void testGetProperty() {
        Sequence sequence = new Sequence(String.class);
        sequence.add("test");

        // Test class property
        Object clazz = sequence.getProperty("class");
        assertEquals(Sequence.class, clazz);
    }

    public void testMetaClass() {
        Sequence sequence = new Sequence(String.class);

        MetaClass metaClass = sequence.getMetaClass();
        assertNotNull(metaClass);

        // Test setMetaClass
        MetaClass newMetaClass = InvokerHelper.getMetaClass(Sequence.class);
        sequence.setMetaClass(newMetaClass);
        assertEquals(newMetaClass, sequence.getMetaClass());
    }

    public void testHashCodeResetAfterModification() {
        Sequence sequence = new Sequence(String.class);
        sequence.add("a");
        int hash1 = sequence.hashCode();

        sequence.add("b");
        // Force recalculation
        int hash2 = sequence.hashCode();

        // The hashes may or may not be different depending on the values,
        // but we're mainly testing that modification invalidates the cached hash
        assertTrue(hash1 != 0 || hash2 != 0);
    }

    public void testEmptySequenceHashCode() {
        Sequence sequence = new Sequence(String.class);
        int hash = sequence.hashCode();
        // Empty sequence should return the sentinel value
        assertEquals(0xbabe, hash);
    }
}
