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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Under static type checking, a list or map literal assigned to a collection or map
 * interface gets the default implementation the runtime cast uses, and one assigned to
 * a collection class copies the elements rather than passing them to a constructor.
 */
final class Groovy12643 {

    @ParameterizedTest
    @ValueSource(strings = ['TypeChecked', 'CompileStatic'])
    void testLiteralToInterfaceWithDefaultUnderStaticChecks(String mode) {
        assertScript """import java.util.concurrent.*
            @groovy.transform.$mode
            void test() {
                SortedSet<Integer> sortedSet = [3, 1, 2, 1]
                assert sortedSet instanceof TreeSet && sortedSet.first() == 1 && sortedSet.size() == 3
                NavigableSet<Integer> navigableSet = [3, 1, 2]
                assert navigableSet.last() == 3
                Queue<Integer> queue = [3, 1, 2]
                assert queue instanceof LinkedList && queue.peek() == 3
                Deque<Integer> deque = [3, 1, 2]
                assert deque.peekLast() == 2
                BlockingQueue<Integer> blockingQueue = [3, 1, 2]
                assert blockingQueue instanceof LinkedBlockingQueue && blockingQueue.peek() == 3
                SortedSet<String> empty = []
                assert empty instanceof TreeSet && empty.isEmpty()

                SortedMap<String, Integer> sortedMap = [b: 2, a: 1]
                assert sortedMap instanceof TreeMap && sortedMap.firstKey() == 'a'
                NavigableMap<String, Integer> navigableMap = [b: 2, a: 1]
                assert navigableMap.lastKey() == 'b'
                ConcurrentMap<String, Integer> concurrentMap = [a: 1]
                assert concurrentMap instanceof ConcurrentHashMap && concurrentMap.a == 1
                SortedMap<String, Integer> emptyMap = [:]
                assert emptyMap instanceof TreeMap && emptyMap.isEmpty()
            }
            test()
        """
    }

    @ParameterizedTest
    @ValueSource(strings = ['TypeChecked', 'CompileStatic'])
    void testLiteralElementTypeIsCheckedForInterfaceWithDefault(String mode) {
        def err = shouldFail """
            @groovy.transform.$mode
            void test() {
                SortedSet<String> set = [3, 1, 2]
            }
        """
        assert err.message.contains('Incompatible generic argument types')
    }

    @ParameterizedTest
    @ValueSource(strings = ['TypeChecked', 'CompileStatic'])
    void testLiteralToCollectionClassCopiesUnderStaticChecks(String mode) {
        assertScript """
            @groovy.transform.$mode
            void test() {
                Vector<Integer> vector = [5]
                assert vector.size() == 1 && vector[0] == 5
                PriorityQueue<Integer> priorityQueue = [5]
                assert priorityQueue.peek() == 5
                ArrayDeque<Integer> arrayDeque = [5]
                assert arrayDeque.peek() == 5
                LinkedList<Integer> linkedList = [3, 1]
                assert linkedList.peekLast() == 1
                Stack<Integer> stack = [3, 1, 2]
                assert stack.peek() == 2
                TreeSet<Integer> treeSet = [3, 1, 2]
                assert treeSet.first() == 1
                Vector<Integer> empty = []
                assert empty.isEmpty()
            }
            test()
        """
    }

    @ParameterizedTest
    @ValueSource(strings = ['TypeChecked', 'CompileStatic'])
    void testLiteralElementTypeIsCheckedForCollectionClass(String mode) {
        def err = shouldFail """
            @groovy.transform.$mode
            void test() {
                LinkedList<String> list = [3, 1]
            }
        """
        assert err.message.contains('Incompatible generic argument types')
    }

    @ParameterizedTest
    @ValueSource(strings = ['TypeChecked', 'CompileStatic'])
    void testListConstructorRuleStillAppliesToOtherClasses(String mode) {
        assertScript """
            class Pair extends AbstractList<Integer> {
                private final int a, b
                Pair(int a, int b) { this.a = a; this.b = b }
                Integer get(int i) { i == 0 ? a : b }
                int size() { 2 }
            }
            @groovy.transform.$mode
            void test() {
                java.awt.Point point = [1, 2]
                assert point.x == 1 && point.y == 2
                Pair pair = [3, 4]
                assert pair == [3, 4]
            }
            test()
        """
        def err = shouldFail """
            @groovy.transform.$mode
            void test() {
                java.awt.Point point = [1]
            }
        """
        assert err.message.contains('Cannot find matching constructor java.awt.Point(int)')
    }
}
