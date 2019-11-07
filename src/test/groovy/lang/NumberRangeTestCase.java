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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Provides unit tests for ranges of numbers.
 */
public abstract class NumberRangeTestCase extends TestCase {

    /**
     * Records the values passed to a closure.
     */
    protected static class RecordingClosure extends Closure {
        /**
         * Holds the values passed in
         */
        final List callLog;

        /**
         * Creates a new <code>RecordingClosure</code>
         *
         * @param callLog is filled with the values passed to <code>doCall</code>
         */
        RecordingClosure(final List callLog) {
            super(null);
            this.callLog = callLog;
        }

        /**
         * Stores <code>params</code> in the <code>callLog</code>.
         *
         * @param params the parameters.
         * @return null
         */
        public Object doCall(final Object params) {
            callLog.add(params);
            return null;
        }
    }

    /**
     * Creates a {@link Range} to test.
     *
     * @param from the first value in the range.
     * @param to   the last value in the range.
     * @return a {@link Range} to test
     */
    protected abstract Range createRange(final int from, final int to);

    /**
     * Creates a value in the range.
     *
     * @param value the value to create.
     * @return a value in the range.
     */
    protected abstract Comparable createValue(final int value);

    /**
     * Tests <code>hashCode</code> and <code>equals</code> comparing one {@link IntRange} to another {@link IntRange}.
     */
    public final void testHashCodeAndEquals() {
        Range a = createRange(1, 11);
        Range b = createRange(1, 11);
        Range c = createRange(2, 11);

        assertEquals("hashcode", a.hashCode(), b.hashCode());
        assertTrue("hashcode", a.hashCode() != c.hashCode());

        assertEquals("a and b", a, b);
        assertFalse("a != c", a.equals(c));
    }

    /**
     * Tests using different classes for 'from' and 'to'.
     */
    public void testDifferentClassesForFromAndTo() {
        final Integer from = Integer.valueOf(1);
        final Comparable to = createValue(5);
        final Range range = new ObjectRange(from, to);

        checkRangeValues(from, to, range);
    }

    protected void checkRangeValues(Integer from, Comparable to, Range range) {
        assertEquals("wrong 'from' value", from, range.getFrom());
        assertEquals("wrong 'to' value", to, range.getTo());
    }

    /**
     * Tests a <code>null</code> 'from' value.
     */
    public void testNullFrom() {
        try {
            new ObjectRange(null, createValue(5));
            fail("null 'from' accepted");
        }
        catch (IllegalArgumentException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Tests a <code>null</code> 'to' value.
     */
    public void testNullTo() {
        try {
            new ObjectRange(createValue(23), null);
            fail("null 'to' accepted");
        }
        catch (IllegalArgumentException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Tests stepping through a range by two with a closure.
     */
    public void testStepByTwoWithClosure() {
        final List callLog = new ArrayList();
        final Closure closure = new RecordingClosure(callLog);

        final Range range = createRange(0, 4);
        range.step(2, closure);

        assertEquals("wrong number of calls to closure", 3, callLog.size());
        final Iterator iter = callLog.iterator();
        for (int i = 0; i <= 4; i += 2) {
            assertEquals("wrong argument passed to closure", createValue(i), iter.next());
        }
    }

    /**
     * Tests iterating over a one-element range.
     */
    public void testOneElementRange() {
        final Range range = createRange(1, 1);
        int next = 1;
        for (Object value : range) {
            final Number number = (Number) value;
            assertEquals("wrong number", createValue(next++), number);
        }
        assertEquals("wrong number of elements in iteration", 2, next);
    }

    /**
     * Tests stepping through a reversed range by two with a closure.
     */
    public void testReverseStepByTwoWithClosure() {
        final List callLog = new ArrayList();
        final Closure closure = new RecordingClosure(callLog);

        final Range range = createRange(4, 0);
        range.step(2, closure);

        assertEquals("wrong number of calls to closure", 3, callLog.size());
        final Iterator iter = callLog.iterator();
        for (int i = 4; i >= 0; i -= 2) {
            assertEquals("wrong argument passed to closure", createValue(i), iter.next());
        }
    }

    /**
     * Tests stepping through a range with a closure.
     */
    public void testStepByOneWithClosure() {
        final List callLog = new ArrayList();
        final Closure closure = new RecordingClosure(callLog);

        final Range range = createRange(1, 5);
        range.step(1, closure);

        assertEquals("wrong number of calls to closure", 5, callLog.size());
        final Iterator iter = callLog.iterator();
        for (int i = 1; i <= 5; i++) {
            assertEquals("wrong argument passed to closure", createValue(i), iter.next());
        }
    }

    /**
     * Tests stepping through a reversed range by one with a closure.
     */
    public void testReverseStepByOneWithClosure() {
        final List callLog = new ArrayList();
        final Closure closure = new RecordingClosure(callLog);

        final Range range = createRange(5, 1);
        range.step(1, closure);

        assertEquals("wrong number of calls to closure", 5, callLog.size());
        final Iterator iter = callLog.iterator();
        for (int i = 5; i >= 1; i--) {
            assertEquals("wrong argument passed to closure", createValue(i), iter.next());
        }
    }

    /**
     * Tests stepping backwards through a range with a closure.
     */
    public void testNegativeStepByOneWithClosure() {
        final List callLog = new ArrayList();
        final Closure closure = new RecordingClosure(callLog);

        final Range range = createRange(1, 5);
        range.step(-1, closure);

        assertEquals("wrong number of calls to closure", 5, callLog.size());
        final Iterator iter = callLog.iterator();
        for (int i = 5; i >= 1; i--) {
            assertEquals("wrong argument passed to closure", createValue(i), iter.next());
        }
    }

    /**
     * Tests stepping backwards through a reversed range with a closure.
     */
    public void testNegativeReverseStepByOneWithClosure() {
        final List callLog = new ArrayList();
        final Closure closure = new RecordingClosure(callLog);

        final Range range = createRange(5, 1);
        range.step(-1, closure);

        assertEquals("wrong number of calls to closure", 5, callLog.size());
        final Iterator iter = callLog.iterator();
        for (int i = 1; i <= 5; i++) {
            assertEquals("wrong argument passed to closure", createValue(i), iter.next());
        }
    }

    /**
     * Tests stepping backwards through a range with a step size greater than the range size.
     */
    public void testStepLargerThanRange() {
        final List callLog = new ArrayList();
        final Closure closure = new RecordingClosure(callLog);

        final Range range = createRange(1, 5);

        range.step(6, closure);
        assertEquals("wrong number of calls to closure", 1, callLog.size());
        assertEquals("wrong value", createValue(1), callLog.get(0));

        final List stepList = range.step(6);
        assertEquals("wrong number of values in result", 1, stepList.size());
        assertEquals("wrong value", createValue(1), callLog.get(0));
    }

    /**
     * Tests stepping through a range by one.
     */
    public void testStepByOne() {
        final Range range = createRange(1, 5);
        final List result = range.step(1);

        assertEquals("wrong number of calls", 5, result.size());
        final Iterator iter = result.iterator();
        for (int i = 1; i <= 5; i++) {
            assertEquals("incorrect value in result", createValue(i), iter.next());
        }
    }

    /**
     * Tests stepping through a range by two.
     */
    public void testStepByTwo() {
        final Range range = createRange(1, 5);
        final List result = range.step(2);

        assertEquals("wrong number of calls", 3, result.size());
        final Iterator iter = result.iterator();
        for (int i = 1; i <= 5; i += 2) {
            assertEquals("incorrect value in result", createValue(i), iter.next());
        }
    }

    /**
     * Tests getting the size.
     */
    public void testSize() {
        Range range = createRange(0, 10);
        assertEquals("Size of " + range, 11, range.size());
        range = createRange(0, 1);
        assertEquals("Size of " + range, 2, range.size());
        range = createRange(0, 0);
        assertEquals("Size of " + range, 1, range.size());
    }

    /**
     * Tests asking for an index outside of the valid range
     */
    public void testGetOutOfRange() {
        Range r = createRange(10, 20);

        try {
            r.get(-1);
            fail("Should have thrown IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException e) {
            assertTrue("expected exception thrown", true);
        }
        try {
            r.get(11);
            fail("Should have thrown IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException e) {
            assertTrue("expected exception thrown", true);
        }

    }

    /**
     * Tests getting a sub list.
     */
    public void testSubList() {
        Range range = createRange(0, 5);

        List subList = range.subList(2, 4);
        assertEquals("size", 2, subList.size());

        assertTrue("sublist not a range", subList instanceof Range);
        Range subListRange = (Range) subList;

        assertEquals("from", createValue(2), subListRange.getFrom());
        assertEquals("to", createValue(3), subListRange.getTo());

        subList = range.subList(0, 6);
        assertEquals("size", 6, subList.size());

        assertTrue("sublist not a range", subList instanceof Range);
        subListRange = (Range) subList;

        assertEquals("from", createValue(0), subListRange.getFrom());
        assertEquals("to", createValue(5), subListRange.getTo());
    }

    /**
     * Tests creating a sub list with a negative "from" index.
     */
    public void testSubListNegativeFrom() {
        try {
            final Range range = createRange(1, 5);
            range.subList(-1, 3);
            fail("accepted sub list with negative index");
        }
        catch (IndexOutOfBoundsException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Tests creating a sub list with an out of range "to" index.
     */
    public void testSubListOutOfRangeTo() {
        try {
            final Range range = createRange(0, 3);
            range.subList(0, 5);
            fail("accepted sub list with invalid 'to'");
        }
        catch (IndexOutOfBoundsException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Tests creating a sub list with "from" grater than "to."
     */
    public void testSubListFromGreaterThanTo() {
        try {
            final Range range = createRange(1, 5);
            range.subList(3, 2);
            fail("accepted sub list with 'from' greater than 'to'");
        }
        catch (IllegalArgumentException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Tests creating an empty sub list.
     */
    public void testEmptySubList() {
        final Range range = createRange(1, 5);

        List subList = range.subList(0, 0);
        assertEquals("wrong number of elements in sub list", 0, subList.size());

        subList = range.subList(2, 2);
        assertEquals("wrong number of elements in sub list", 0, subList.size());
    }

    /**
     * Tests iterating over a non-reversed range.
     */
    public void testIterate() {
        final Range range = createRange(1, 5);
        int next = 1;
        final Iterator iter = range.iterator();
        while (iter.hasNext()) {
            final Object value = iter.next();
            assertEquals("wrong next value", createValue(next++), value);
        }
        assertEquals("wrong number of elements in iteration", 6, next);
        try {
            iter.next();
            fail("successfully got element from exhausted iterator");
        } catch(NoSuchElementException ignore) {
        }
    }

    /**
     * Tests removing an element from the range using an iterator (not supported).
     */
    public void testRemoveFromIterator() {
        final Range range = createRange(1, 5);

        try {
            final Iterator iter = range.iterator();
            iter.remove();
            fail("successfully removed an element using an iterator");
        }
        catch (UnsupportedOperationException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Tests iterating over a reversed range.
     */
    public void testIterateReversed() {
        final Range range = createRange(5, 1);
        int next = 5;
        for (Object value : range) {
            assertEquals("wrong number", createValue(next--), value);
        }
        assertEquals("wrong number of elements in iteration", 0, next);
    }

    /**
     * Tests creating an <code>IntRange</code> with from > to.
     */
    public void testFromGreaterThanTo() {
        final int from = 9;
        final int to = 0;
        final Range range = createRange(from, to);

        assertTrue("range not reversed", range.isReverse());

        // make sure to/from are swapped
        assertEquals("from incorrect", createValue(to), range.getFrom());
        assertEquals("to incorrect", createValue(from), range.getTo());

        assertEquals("wrong size", 10, range.size());

        assertEquals("wrong first element", createValue(9), range.get(0));
        assertEquals("wrong last element", createValue(0), range.get(9));
    }

    /**
     * Tests creating an <code>IntRange</code> with from == to.
     */
    public void testFromEqualsTo() {
        final Range range = createRange(5, 5);

        assertFalse("range reversed", range.isReverse());
        assertEquals("wrong size", 1, range.size());
    }

    /**
     * Tests creating an <code>IntRange</code> with from < to.
     */
    public void testFromLessThanTo() {
        final int from = 1;
        final int to = 4;
        final Range range = createRange(from, to);

        assertFalse("range reversed", range.isReverse());

        assertEquals("to incorrect", createValue(from), range.getFrom());
        assertEquals("from incorrect", createValue(to), range.getTo());

        assertEquals("wrong size", 4, range.size());
    }

    /**
     * Making a range equal a list is not actually possible, since list.equals(range) will not evaluate to
     * <code>true</code> and <code>equals</code> should be symmetric.
     */
    public void testEqualsList() {
        final List list = new ArrayList();
        list.add(createValue(1));
        list.add(createValue(2));

        final Range range = createRange(1, 2);

        // cast to Object to test routing through equals(Object)
        assertTrue("range does not equal list", range.equals((Object) list));
        assertTrue("list does not equal range", list.equals(range));
        assertEquals("hash codes are not equal", range.hashCode(), list.hashCode());

        // compare lists that are the same size but contain different elements
        list.set(0, createValue(3));
        assertFalse("range equals list", range.equals(list));
        assertFalse("list equals range", list.equals(range));
        assertFalse("hash codes are equal", range.hashCode() == list.hashCode());

        // compare a list longer than the range
        list.set(0, createValue(1));
        list.add(createValue(3));
        assertFalse("range equals list", range.equals(list));
        assertFalse("list equals range", list.equals(range));
        assertFalse("hash are equal", range.hashCode() == list.hashCode());

        // compare a list shorter than the range
        list.remove(2);
        list.remove(1);
        assertFalse("range equals list", range.equals(list));
        assertFalse("list equals range", list.equals(range));
        assertFalse("hash are equal", range.hashCode() == list.hashCode());
    }

    /**
     * Tests comparing {@link Range} to an object that is not a {@link Range}.
     */
    public void testEqualsNonRange() {
        final Range range = createRange(1, 5);
        assertFalse("range equal to string", range.equals("hello"));
    }

    /**
     * Tests comparing a {@link Range} cast to an {@link Object}
     */
    public void testEqualsRangeAsObject() {
        final Range range1 = createRange(1, 5);
        final Range range2 = createRange(1, 5);
        assertTrue("ranges not equal", range1.equals((Object) range2));
    }

    /**
     * Tests comparing two {@link Range}s to each other.
     */
    public void testEqualsRange() {
        final Range range1 = createRange(1, 5);
        Range range2 = createRange(1, 5);
        assertTrue("ranges not equal", range1.equals((Object) range2));
        assertTrue("ranges not equal", range2.equals((Object) range1));
        assertEquals("hash codes not equal", range1.hashCode(), range2.hashCode());

        range2 = createRange(0, 5);
        assertFalse("ranges equal", range1.equals((Object) range2));
        assertFalse("ranges equal", range2.equals((Object) range1));
        assertFalse("hash codes equal", range1.hashCode() == range2.hashCode());

        range2 = createRange(1, 6);
        assertFalse("ranges equal", range1.equals((Object) range2));
        assertFalse("ranges equal", range2.equals((Object) range1));
        assertFalse("hash codes equal", range1.hashCode() == range2.hashCode());

        range2 = createRange(0, 6);
        assertFalse("ranges equal", range1.equals((Object) range2));
        assertFalse("ranges equal", range2.equals((Object) range1));
        assertFalse("hash codes equal", range1.hashCode() == range2.hashCode());

        range2 = createRange(2, 4);
        assertFalse("ranges equal", range1.equals((Object) range2));
        assertFalse("ranges equal", range2.equals((Object) range1));
        assertFalse("hash codes equal", range1.hashCode() == range2.hashCode());

        range2 = createRange(5, 1);
        assertFalse("ranges equal", range1.equals((Object) range2));
        assertFalse("ranges equal", range2.equals((Object) range1));
        assertFalse("hash codes equal", range1.hashCode() == range2.hashCode());
    }

    /**
     * Tests <code>toString</code> and <code>inspect</code>
     */
    public void testToStringAndInspect() {
        Range range = createRange(1, 5);
        String expected = range.getFrom() + ".." + range.getTo();
        assertEquals("wrong string representation", expected, range.toString());
        assertEquals("wrong string representation", expected, range.inspect());

        range = createRange(5, 1);
        expected = range.getTo() + ".." + range.getFrom();
        assertEquals("wrong string representation", expected, range.toString());
        assertEquals("wrong string representation", expected, range.inspect());
    }

    /**
     * Tests <code>getFrom</code> and <code>getTo</code>.
     */
    public void testGetFromAndTo() {
        final int from = 1, to = 5;
        final Range range = createRange(from, to);

        assertEquals("wrong 'from' value", createValue(from), range.getFrom());
        assertEquals("wrong 'to' value", createValue(to), range.getTo());
    }

    /**
     * Tests comparing a {@link Range} to <code>null</code>.
     */
    public void testEqualsNull() {
        final Range range = createRange(1, 5);
        assertFalse("range equal to null", range.equals(null));
        assertFalse("range equal to null Object", range.equals((Object) null));
        assertFalse("range equal to null Range", range.equals((Range) null));
        assertFalse("range equal to null List", range.equals((List) null));
    }

    /**
     * Tests attempting to add a value to a range.
     */
    public void testAddValue() {
        try {
            final Range range = createRange(1, 5);
            range.add(createValue(20));
            fail("expected exception not thrown");
        }
        catch (UnsupportedOperationException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    /**
     * Tests attempting to remove a value from a range.
     */
    public void testRemoveValue() {
        try {
            final Range range = createRange(1, 5);
            range.remove(0);
            fail("expected exception not thrown");
        }
        catch (UnsupportedOperationException e) {
            assertTrue("expected exception thrown", true);
        }
    }

    private void doTestContains(int from, int to, Range range) {
        // test integers
        assertTrue("missing 'from' value", range.contains(createValue(from)));
        assertTrue("missing 'to' value", range.contains(createValue(to)));
        assertTrue("missing mid point", range.contains(createValue((from + to) / 2)));
        assertFalse("contains out of range value", range.contains(createValue(from - 1)));
        assertFalse("contains out of range value", range.contains(createValue(to + 1)));

        // test ranges
        assertTrue("missing same range", range.containsAll(createRange(from, to)));
        assertTrue("missing same range", range.containsAll(createRange(to, from)));
        assertTrue("missing strict subset", range.containsAll(createRange(from + 1, to - 1)));
        assertTrue("missing subset", range.containsAll(createRange(from, to - 1)));
        assertTrue("missing subset", range.containsAll(createRange(from + 1, to)));
        assertFalse("contains non-subset", range.containsAll(createRange(from - 1, to)));
        assertFalse("contains non-subset", range.containsAll(createRange(from, to + 1)));
        assertFalse("contains non-subset", range.containsAll(createRange(from - 2, from - 1)));

        // ranges don't contain other ranges
        assertFalse("range contains sub-range", range.contains(createRange(from + 1, to - 1)));

        // test list
        final List list = new ArrayList();
        list.add(createValue(from));
        list.add(createValue(to));
        assertTrue("missing strict subset", range.containsAll(list));

        // test non-integer number
        assertFalse("contains Float", range.contains(new Float((to + from) / 2.0 + 0.3)));
    }

    /**
     * Tests whether the range contains a {@link Comparable} object which is not comparable with a {@link Number}.
     */
    public void testContainsIncompatibleComparable() {
        final Range range = createRange(1, 5);
        assertFalse("range contains string", range.contains("hello"));
        assertFalse("range contains string", range.contains("1"));
    }

    /**
     * Tests whether the range contains a non-comparable object.
     */
    public void testContainsNonComparable() {
        final Range range = createRange(1, 5);
        assertFalse("range contains hash map", range.contains(new HashMap()));
    }

    /**
     * Tests whether a {@link Range} contains another {@link Range} or a specific integer.
     */
    public void testContains() {
        final int from = 1, to = 5;
        doTestContains(from, to, createRange(from, to));
        doTestContains(from, to, createRange(to, from));
    }

    /**
     * Tests <code>get</code> from a reversed range.
     */
    public void testGetFromReversedRange() {
        final Range range = createRange(5, 1);

        for (int i = 0; i < 5; i++) {
            assertEquals("wrong element at position " + i, createValue(5 - i), range.get(i));
        }
    }

    /**
     * Tests getting values from the range.
     */
    public void testGet() {
        final Range range = createRange(10, 20);
        for (int i = 0; i <= 10; i++) {
            assertEquals("Item at index: " + i, createValue(i + 10), range.get(i));
        }
    }
}
