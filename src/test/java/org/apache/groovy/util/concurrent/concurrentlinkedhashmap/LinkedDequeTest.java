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
package org.apache.groovy.util.concurrent.concurrentlinkedhashmap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for LinkedDeque class.
 */
class LinkedDequeTest {

    private LinkedDeque<TestNode> deque;

    @BeforeEach
    void setUp() {
        deque = new LinkedDeque<>();
    }

    // Test node implementation
    static class TestNode implements Linked<TestNode> {
        private TestNode prev;
        private TestNode next;
        private final String value;

        TestNode(String value) {
            this.value = value;
        }

        @Override
        public TestNode getPrevious() {
            return prev;
        }

        @Override
        public void setPrevious(TestNode prev) {
            this.prev = prev;
        }

        @Override
        public TestNode getNext() {
            return next;
        }

        @Override
        public void setNext(TestNode next) {
            this.next = next;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @Test
    void testIsEmptyOnNew() {
        assertTrue(deque.isEmpty());
    }

    @Test
    void testSizeOnEmpty() {
        assertEquals(0, deque.size());
    }

    @Test
    void testAddFirst() {
        TestNode node = new TestNode("first");
        deque.addFirst(node);
        
        assertFalse(deque.isEmpty());
        assertEquals(1, deque.size());
        assertSame(node, deque.peekFirst());
    }

    @Test
    void testAddLast() {
        TestNode node = new TestNode("last");
        deque.addLast(node);
        
        assertFalse(deque.isEmpty());
        assertEquals(1, deque.size());
        assertSame(node, deque.peekLast());
    }

    @Test
    void testOfferFirst() {
        TestNode node = new TestNode("offer-first");
        assertTrue(deque.offerFirst(node));
        assertSame(node, deque.peekFirst());
    }

    @Test
    void testOfferLast() {
        TestNode node = new TestNode("offer-last");
        assertTrue(deque.offerLast(node));
        assertSame(node, deque.peekLast());
    }

    @Test
    void testOfferFirstDuplicateReturnsfalse() {
        TestNode node = new TestNode("dup");
        assertTrue(deque.offerFirst(node));
        assertFalse(deque.offerFirst(node)); // Already in deque
    }

    @Test
    void testOfferLastDuplicateReturnsFalse() {
        TestNode node = new TestNode("dup");
        assertTrue(deque.offerLast(node));
        assertFalse(deque.offerLast(node)); // Already in deque
    }

    @Test
    void testOffer() {
        TestNode node = new TestNode("offer");
        assertTrue(deque.offer(node));
        assertSame(node, deque.peekLast());
    }

    @Test
    void testAdd() {
        TestNode node = new TestNode("add");
        assertTrue(deque.add(node));
        assertEquals(1, deque.size());
    }

    @Test
    void testPeekOnEmpty() {
        assertNull(deque.peek());
    }

    @Test
    void testPeekFirstOnEmpty() {
        assertNull(deque.peekFirst());
    }

    @Test
    void testPeekLastOnEmpty() {
        assertNull(deque.peekLast());
    }

    @Test
    void testPeek() {
        TestNode node = new TestNode("peek-test");
        deque.addFirst(node);
        assertSame(node, deque.peek());
    }

    @Test
    void testGetFirstOnEmpty() {
        assertThrows(NoSuchElementException.class, () -> deque.getFirst());
    }

    @Test
    void testGetLastOnEmpty() {
        assertThrows(NoSuchElementException.class, () -> deque.getLast());
    }

    @Test
    void testElementOnEmpty() {
        assertThrows(NoSuchElementException.class, () -> deque.element());
    }

    @Test
    void testGetFirst() {
        TestNode node = new TestNode("get-first");
        deque.addFirst(node);
        assertSame(node, deque.getFirst());
    }

    @Test
    void testGetLast() {
        TestNode node = new TestNode("get-last");
        deque.addLast(node);
        assertSame(node, deque.getLast());
    }

    @Test
    void testElement() {
        TestNode node = new TestNode("element");
        deque.addFirst(node);
        assertSame(node, deque.element());
    }

    @Test
    void testPollOnEmpty() {
        assertNull(deque.poll());
    }

    @Test
    void testPollFirstOnEmpty() {
        assertNull(deque.pollFirst());
    }

    @Test
    void testPollLastOnEmpty() {
        assertNull(deque.pollLast());
    }

    @Test
    void testPoll() {
        TestNode node = new TestNode("poll");
        deque.addFirst(node);
        assertSame(node, deque.poll());
        assertTrue(deque.isEmpty());
    }

    @Test
    void testPollFirst() {
        TestNode first = new TestNode("first");
        TestNode second = new TestNode("second");
        deque.addLast(first);
        deque.addLast(second);
        
        assertSame(first, deque.pollFirst());
        assertEquals(1, deque.size());
        assertSame(second, deque.peekFirst());
    }

    @Test
    void testPollLast() {
        TestNode first = new TestNode("first");
        TestNode second = new TestNode("second");
        deque.addLast(first);
        deque.addLast(second);
        
        assertSame(second, deque.pollLast());
        assertEquals(1, deque.size());
        assertSame(first, deque.peekLast());
    }

    @Test
    void testRemoveOnEmpty() {
        assertThrows(NoSuchElementException.class, () -> deque.remove());
    }

    @Test
    void testRemoveFirstOnEmpty() {
        assertThrows(NoSuchElementException.class, () -> deque.removeFirst());
    }

    @Test
    void testRemoveLastOnEmpty() {
        assertThrows(NoSuchElementException.class, () -> deque.removeLast());
    }

    @Test
    void testRemove() {
        TestNode node = new TestNode("remove");
        deque.addFirst(node);
        assertSame(node, deque.remove());
        assertTrue(deque.isEmpty());
    }

    @Test
    void testRemoveFirst() {
        TestNode first = new TestNode("first");
        TestNode second = new TestNode("second");
        deque.addLast(first);
        deque.addLast(second);
        
        assertSame(first, deque.removeFirst());
        assertEquals(1, deque.size());
    }

    @Test
    void testRemoveLast() {
        TestNode first = new TestNode("first");
        TestNode second = new TestNode("second");
        deque.addLast(first);
        deque.addLast(second);
        
        assertSame(second, deque.removeLast());
        assertEquals(1, deque.size());
    }

    @Test
    void testRemoveObject() {
        TestNode node = new TestNode("to-remove");
        deque.addFirst(node);
        
        assertTrue(deque.remove(node));
        assertTrue(deque.isEmpty());
    }

    @Test
    void testRemoveObjectNotFound() {
        TestNode node1 = new TestNode("in-deque");
        TestNode node2 = new TestNode("not-in-deque");
        deque.addFirst(node1);
        
        assertFalse(deque.remove(node2));
        assertEquals(1, deque.size());
    }

    @Test
    void testRemoveNonLinked() {
        assertFalse(deque.remove("not a linked object"));
    }

    @Test
    void testContains() {
        TestNode node = new TestNode("contained");
        deque.addFirst(node);
        
        assertTrue(deque.contains(node));
    }

    @Test
    void testContainsNotFound() {
        TestNode node1 = new TestNode("in-deque");
        TestNode node2 = new TestNode("not-in-deque");
        deque.addFirst(node1);
        
        assertFalse(deque.contains(node2));
    }

    @Test
    void testContainsNonLinked() {
        assertFalse(deque.contains("not a linked object"));
    }

    @Test
    void testClear() {
        deque.addLast(new TestNode("a"));
        deque.addLast(new TestNode("b"));
        deque.addLast(new TestNode("c"));
        
        assertEquals(3, deque.size());
        
        deque.clear();
        
        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
    }

    @Test
    void testPush() {
        TestNode node = new TestNode("pushed");
        deque.push(node);
        
        assertSame(node, deque.peekFirst());
    }

    @Test
    void testPop() {
        TestNode node = new TestNode("popped");
        deque.push(node);
        
        assertSame(node, deque.pop());
        assertTrue(deque.isEmpty());
    }

    @Test
    void testPopOnEmpty() {
        assertThrows(NoSuchElementException.class, () -> deque.pop());
    }

    @Test
    void testIterator() {
        TestNode a = new TestNode("a");
        TestNode b = new TestNode("b");
        TestNode c = new TestNode("c");
        
        deque.addLast(a);
        deque.addLast(b);
        deque.addLast(c);
        
        List<String> values = new ArrayList<>();
        for (TestNode node : deque) {
            values.add(node.value);
        }
        
        assertEquals(Arrays.asList("a", "b", "c"), values);
    }

    @Test
    void testIteratorHasNextOnEmpty() {
        Iterator<TestNode> it = deque.iterator();
        assertFalse(it.hasNext());
    }

    @Test
    void testIteratorNextOnEmpty() {
        Iterator<TestNode> it = deque.iterator();
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void testIteratorRemoveUnsupported() {
        TestNode node = new TestNode("test");
        deque.addFirst(node);
        
        Iterator<TestNode> it = deque.iterator();
        it.next();
        
        assertThrows(UnsupportedOperationException.class, it::remove);
    }

    @Test
    void testDescendingIterator() {
        TestNode a = new TestNode("a");
        TestNode b = new TestNode("b");
        TestNode c = new TestNode("c");
        
        deque.addLast(a);
        deque.addLast(b);
        deque.addLast(c);
        
        List<String> values = new ArrayList<>();
        Iterator<TestNode> it = deque.descendingIterator();
        while (it.hasNext()) {
            values.add(it.next().value);
        }
        
        assertEquals(Arrays.asList("c", "b", "a"), values);
    }

    @Test
    void testDescendingIteratorOnEmpty() {
        Iterator<TestNode> it = deque.descendingIterator();
        assertFalse(it.hasNext());
    }

    @Test
    void testRemoveFirstOccurrence() {
        TestNode node = new TestNode("target");
        deque.addFirst(node);
        
        assertTrue(deque.removeFirstOccurrence(node));
        assertTrue(deque.isEmpty());
    }

    @Test
    void testRemoveFirstOccurrenceNotFound() {
        TestNode node1 = new TestNode("in");
        TestNode node2 = new TestNode("out");
        deque.addFirst(node1);
        
        assertFalse(deque.removeFirstOccurrence(node2));
    }

    @Test
    void testRemoveLastOccurrence() {
        TestNode node = new TestNode("target");
        deque.addLast(node);
        
        assertTrue(deque.removeLastOccurrence(node));
        assertTrue(deque.isEmpty());
    }

    @Test
    void testRemoveAll() {
        TestNode a = new TestNode("a");
        TestNode b = new TestNode("b");
        TestNode c = new TestNode("c");
        
        deque.addLast(a);
        deque.addLast(b);
        deque.addLast(c);
        
        assertTrue(deque.removeAll(Arrays.asList(a, c)));
        assertEquals(1, deque.size());
        assertSame(b, deque.peekFirst());
    }

    @Test
    void testRemoveAllEmpty() {
        assertFalse(deque.removeAll(Arrays.asList()));
    }

    @Test
    void testMoveToFront() {
        TestNode a = new TestNode("a");
        TestNode b = new TestNode("b");
        TestNode c = new TestNode("c");
        
        deque.addLast(a);
        deque.addLast(b);
        deque.addLast(c);
        
        deque.moveToFront(c);
        
        assertSame(c, deque.peekFirst());
        assertSame(b, deque.peekLast());
    }

    @Test
    void testMoveToFrontAlreadyFirst() {
        TestNode a = new TestNode("a");
        TestNode b = new TestNode("b");
        
        deque.addLast(a);
        deque.addLast(b);
        
        deque.moveToFront(a);
        
        assertSame(a, deque.peekFirst());
        assertSame(b, deque.peekLast());
    }

    @Test
    void testMoveToBack() {
        TestNode a = new TestNode("a");
        TestNode b = new TestNode("b");
        TestNode c = new TestNode("c");
        
        deque.addLast(a);
        deque.addLast(b);
        deque.addLast(c);
        
        deque.moveToBack(a);
        
        assertSame(b, deque.peekFirst());
        assertSame(a, deque.peekLast());
    }

    @Test
    void testMoveToBackAlreadyLast() {
        TestNode a = new TestNode("a");
        TestNode b = new TestNode("b");
        
        deque.addLast(a);
        deque.addLast(b);
        
        deque.moveToBack(b);
        
        assertSame(a, deque.peekFirst());
        assertSame(b, deque.peekLast());
    }

    @Test
    void testMultipleAddRemove() {
        TestNode a = new TestNode("a");
        TestNode b = new TestNode("b");
        TestNode c = new TestNode("c");
        
        deque.addLast(a);
        deque.addLast(b);
        deque.addLast(c);
        
        assertEquals(3, deque.size());
        
        deque.removeFirst();
        assertEquals(2, deque.size());
        assertSame(b, deque.peekFirst());
        
        deque.removeLast();
        assertEquals(1, deque.size());
        assertSame(b, deque.peekFirst());
        assertSame(b, deque.peekLast());
        
        deque.remove(b);
        assertTrue(deque.isEmpty());
    }

    @Test
    void testAddFirstDuplicateThrows() {
        TestNode node = new TestNode("dup");
        deque.addFirst(node);
        
        assertThrows(IllegalArgumentException.class, () -> deque.addFirst(node));
    }

    @Test
    void testAddLastDuplicateThrows() {
        TestNode node = new TestNode("dup");
        deque.addLast(node);
        
        assertThrows(IllegalArgumentException.class, () -> deque.addLast(node));
    }

    @Test
    void testSingleElement() {
        TestNode single = new TestNode("single");
        deque.addFirst(single);
        
        assertSame(single, deque.peekFirst());
        assertSame(single, deque.peekLast());
        assertSame(deque.removeFirst(), deque.peekFirst() == null ? single : null);
    }

    @Test
    void testRemoveMiddleElement() {
        TestNode a = new TestNode("a");
        TestNode b = new TestNode("b");
        TestNode c = new TestNode("c");
        
        deque.addLast(a);
        deque.addLast(b);
        deque.addLast(c);
        
        assertTrue(deque.remove(b));
        
        assertEquals(2, deque.size());
        assertSame(a, deque.peekFirst());
        assertSame(c, deque.peekLast());
    }
}
