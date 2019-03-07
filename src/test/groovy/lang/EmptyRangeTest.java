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

import groovy.util.GroovyTestCase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Provides unit tests for the {@link EmptyRange} class.
 */
public class EmptyRangeTest extends GroovyTestCase {

    /**
     * The 'from' value for the {@link Range}.
     */
    private static final Integer AT = 17;

    /**
     * The {@link Range} to test.
     */
    private Range range = null;

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();

        range = new EmptyRange(AT);
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#getFrom()}.
     */
    public void testGetFrom() {
        assertEquals("wrong 'from' value", AT, range.getFrom());
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#getTo()}.
     */
    public void testGetTo() {
        assertEquals("wrong 'from' value", AT, range.getTo());
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#isReverse()}.
     */
    public void testIsReverse() {
        assertFalse("empty range reversed", range.isReverse());
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#inspect()}.
     */
    public void testInspect() {
        assertEquals("wrong 'inspect' value", AT + "..<" + AT, range.inspect());
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#inspect()()} with a range with a <code>null</code> 'at' value.
     */
    public void testInspectNullAt() {
        final Range nullAtRange = new EmptyRange(null);
        assertEquals("wrong inspect value", "null..<null", nullAtRange.inspect());
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#toString()}.
     */
    public void testToString() {
        assertEquals("wrong string value", AT + "..<" + AT, range.toString());
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#toString()} with a range with a <code>null</code> 'at' value.
     */
    public void testToStringNullAt() {
        final Range nullAtRange = new EmptyRange(null);
        assertEquals("wrong string value", "null..<null", nullAtRange.toString());
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#size()}.
     */
    public void testSize() {
        assertEquals("wrong size", 0, range.size());
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#clear()}.
     */
    public void testClear() {
        range.clear();
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#isEmpty()}.
     */
    public void testIsEmpty() {
        assertTrue("range not empty", range.isEmpty());
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#toArray()}.
     */
    public void testToArray() {
        assertArrayEquals(new Object[0], range.toArray());
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#get(int)}.
     */
    public void testGet() {
        try {
            range.get(0);
            fail("got value from empty range");
        } catch (IndexOutOfBoundsException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#remove(int)}.
     */
    public void testRemoveInt() {
        try {
            range.remove(0);
            fail("removed value from empty range");
        } catch (UnsupportedOperationException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#add(int, java.lang.Object)}.
     */
    public void testAddIntObject() {
        try {
            range.add(0, 12);
            fail("added value to empty range");
        } catch (UnsupportedOperationException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#indexOf(java.lang.Object)}.
     */
    public void testIndexOf() {
        assertEquals("found value in empty range", -1, range.indexOf(AT));
        assertEquals("found null in empty range", -1, range.indexOf(null));
        assertEquals("found string in empty range", -1, range.indexOf("hello"));
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#lastIndexOf(java.lang.Object)}.
     */
    public void testLastIndexOf() {
        assertEquals("found value in empty range", -1, range.lastIndexOf(AT));
        assertEquals("found null in empty range", -1, range.lastIndexOf(null));
        assertEquals("found string in empty range", -1, range.lastIndexOf("hello"));
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#add(java.lang.Object)}.
     */
    public void testAddObject() {
        try {
            range.add(12);
            fail("added value to empty range");
        } catch (UnsupportedOperationException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#contains(java.lang.Object)}.
     */
    public void testContains() {
        assertFalse("empty range contains a value", range.contains(AT));
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#remove(java.lang.Object)}.
     */
    public void testRemoveObject() {
        try {
            range.remove(AT);
            fail("removed value from empty range");
        } catch (UnsupportedOperationException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#addAll(int, java.util.Collection)}.
     */
    public void testAddAllIntCollection() {
        try {
            range.addAll(0, new ArrayList());
            fail("added values to empty range");
        } catch (UnsupportedOperationException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#addAll(java.util.Collection)}.
     */
    public void testAddAllCollection() {
        try {
            range.addAll(new ArrayList());
            fail("added values to empty range");
        } catch (UnsupportedOperationException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#containsAll(java.util.Collection)}.
     */
    public void testContainsAll() {
        final List list = new ArrayList();
        assertTrue("range contains all elements of an empty list", range.containsAll(list));

        list.add(AT);
        assertFalse("range contains all elements of single element list", range.containsAll(list));
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#removeAll(java.util.Collection)}.
     */
    public void testRemoveAll() {
        try {
            range.removeAll(new ArrayList());
            fail("removed values from an empty range");
        } catch (UnsupportedOperationException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#retainAll(java.util.Collection)}.
     */
    public void testRetainAll() {
        try {
            range.retainAll(new ArrayList());
            fail("retained values in an empty range");
        } catch (UnsupportedOperationException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#iterator()}.
     */
    public void testIterator() {
        final Iterator iterator = range.iterator();
        assertFalse("iterator has next value", iterator.hasNext());

        try {
            iterator.next();
            fail("got next value in an empty range");
        } catch (NoSuchElementException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Tests removing via an iterator.
     */
    public void testIteratorRemove() {
        try {
            final Iterator iterator = range.iterator();
            iterator.remove();
            fail("removed via iterator");
        } catch (IllegalStateException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#subList(int, int)}.
     */
    public void testSubList() {
        final List list = range.subList(0, 0);
        assertTrue("list not empty", list.isEmpty());
        try {
            range.subList(0, 1);
            fail("got sub list in an empty range");
        } catch (IndexOutOfBoundsException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#listIterator()}.
     */
    public void testListIterator() {
        final ListIterator iterator = range.listIterator();
        assertFalse("iterator has next value", iterator.hasNext());
        assertFalse("iterator has previous value", iterator.hasPrevious());

        try {
            iterator.next();
            fail("got next value in an empty range");
        } catch (NoSuchElementException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#listIterator(int)}.
     */
    public void testListIteratorInt() {
        final ListIterator iterator = range.listIterator(0);
        assertFalse("iterator has next value", iterator.hasNext());
        assertFalse("iterator has previous value", iterator.hasPrevious());

        try {
            range.listIterator(1);
            fail("got list iterator at index 1");
        } catch (IndexOutOfBoundsException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#set(int, java.lang.Object)}.
     */
    public void testSet() {
        try {
            range.set(0, AT);
            fail("got set value 0");
        } catch (UnsupportedOperationException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#toArray(java.lang.Object[])}.
     */
    public void testToArrayObjectArray() {
        final Integer[] actual = (Integer[]) range.toArray(new Integer[0]);
        assertArrayEquals(new Integer[0], actual);
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#step(int, groovy.lang.Closure)}.
     */
    public void testStepIntClosure() {
        final List callLog = new ArrayList();
        final Closure closure = new NumberRangeTestCase.RecordingClosure(callLog);
        range.step(1, closure);
        assertEquals("wrong number of calls to closure", 0, callLog.size());
    }

    /**
     * Test method for {@link groovy.lang.EmptyRange#step(int)}.
     */
    public void testStepInt() {
        List result = range.step(1);
        assertTrue("too many elements", result.isEmpty());

        // make sure a new list is returned each time
        result.add(1);
        result = range.step(1);
        assertTrue("too many elements", result.isEmpty());
    }
}
