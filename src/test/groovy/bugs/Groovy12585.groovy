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
package bugs

import org.junit.jupiter.api.Test

import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.LinkedBlockingQueue

import static groovy.test.GroovyAssert.shouldFail

/**
 * Assignment or cast ({@code castToType}) and {@code as} coercion ({@code asType}) of a
 * collection, array or map to a collection or map type use the same default implementations.
 */
final class Groovy12585 {

    @Test
    void testAssignCollectionToInterfaceWithDefault() {
        SortedSet sortedSet = [3, 1, 2, 1]
        assert sortedSet instanceof TreeSet && sortedSet as List == [1, 2, 3]
        NavigableSet navigableSet = [3, 1, 2, 1]
        assert navigableSet instanceof TreeSet && navigableSet.first() == 1
        Queue queue = [3, 1, 2, 1]
        assert queue instanceof LinkedList && queue as List == [3, 1, 2, 1]
        Deque deque = [3, null, 2]
        assert deque instanceof LinkedList && deque.peekFirst() == 3 && deque.peekLast() == 2
        BlockingQueue blockingQueue = [3, 1, 2]
        assert blockingQueue instanceof LinkedBlockingQueue && blockingQueue.peek() == 3
        List list = [3, 1, 2] as LinkedHashSet
        assert list instanceof ArrayList && list == [3, 1, 2]
    }

    @Test
    void testCoerceCollectionToInterfaceWithDefault() {
        def navigableSet = [3, 1, 2, 1] as NavigableSet
        assert navigableSet instanceof TreeSet && navigableSet.size() == 3 && navigableSet.first() == 1
        def deque = [3, 1, 2] as Deque
        assert deque instanceof LinkedList && deque.peekFirst() == 3
        assert ([3, 1, 2] as BlockingQueue) instanceof LinkedBlockingQueue
        assert ([3, 1, 2] as Queue) instanceof LinkedList
        assert ([3, 1, 2] as SortedSet) instanceof TreeSet
    }

    @Test
    void testAssignCollectionToClassWithoutCopyConstructor() {
        Stack stack = [3, 1, 2]
        assert stack.peek() == 2 && stack.size() == 3
        Stack fromArray = new Integer[]{3, 1, 2}
        assert fromArray == stack
    }

    @Test
    void testSingleElementIsNotPassedToCapacityConstructor() {
        Vector vector = [5]
        assert vector == [5]
        PriorityQueue priorityQueue = [5]
        assert priorityQueue.peek() == 5
        ArrayDeque arrayDeque = [5]
        assert arrayDeque.peek() == 5
    }

    @Test
    void testMapToInterfaceWithDefault() {
        SortedMap sortedMap = [b: 2, a: 1]
        assert sortedMap instanceof TreeMap && sortedMap.firstKey() == 'a'
        NavigableMap navigableMap = [b: 2, a: 1]
        assert navigableMap instanceof TreeMap && navigableMap.firstKey() == 'a'
        ConcurrentMap concurrentMap = [b: 2, a: 1]
        assert concurrentMap instanceof ConcurrentHashMap && concurrentMap.b == 2

        def coercedSorted = [b: 2, a: 1] as SortedMap
        assert coercedSorted instanceof TreeMap && coercedSorted.firstKey() == 'a'
        assert ([b: 2, a: 1] as NavigableMap) instanceof TreeMap
        assert ([b: 2, a: 1] as ConcurrentMap) instanceof ConcurrentHashMap
    }

    @Test
    void testMapAsSingleMethodInterfaceIsStillAProxy() {
        def runnable = [run: { -> 'ran' }] as Runnable
        assert !(runnable instanceof Map)
        def comparator = [compare: { a, b -> b <=> a }] as Comparator
        assert [1, 3, 2].sort(false, comparator) == [3, 2, 1]
    }

    @Test
    void testArraySource() {
        Integer[] nums = [3, 1, 2, 1]
        Iterable iterable = nums
        assert iterable instanceof ArrayList && iterable == [3, 1, 2, 1]
        assert (nums as Iterable) instanceof ArrayList

        def set = nums as Set
        assert set instanceof LinkedHashSet && set as List == [3, 1, 2]
        Set assigned = nums
        assert assigned instanceof LinkedHashSet && assigned as List == [3, 1, 2]

        Deque deque = nums
        assert deque instanceof LinkedList && deque.peekLast() == 1
        assert (nums as Deque) instanceof LinkedList
        SortedSet sortedSet = nums
        assert sortedSet as List == [1, 2, 3]
        assert (nums as List) == [3, 1, 2, 1]
    }

    @Test
    void testStreamSource() {
        Deque deque = [3, 1, 2].stream()
        assert deque instanceof LinkedList && deque as List == [3, 1, 2]
        SortedSet sortedSet = [3, 1, 2].stream()
        assert sortedSet as List == [1, 2, 3]
    }

    @Test
    void testInstanceOfTargetIsReturnedUnchanged() {
        def list = new LinkedList([3, 1, 2])
        assert list.is(list as Deque)
        Queue queue = list
        assert queue.is(list)
        def treeMap = new TreeMap([a: 1])
        assert treeMap.is(treeMap as SortedMap)
    }

    @Test
    void testSortedTargetWithIncomparableElements() {
        shouldFail(ClassCastException) {
            SortedSet set = [new Object(), new Object()]
        }
        shouldFail(ClassCastException) {
            [new Object(), new Object()] as NavigableSet
        }
    }
}
