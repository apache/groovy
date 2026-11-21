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
 * Under static type checking, a list or map literal returned from a method is checked
 * as it is when assigned to a variable of the method's return type.
 */
final class Groovy12644 {

    @ParameterizedTest
    @ValueSource(strings = ['TypeChecked', 'CompileStatic'])
    void testReturnLiteralToTypeLiteralClassDoesNotImplement(String mode) {
        assertScript """import java.util.concurrent.*
            @groovy.transform.$mode
            class C {
                Set<Integer> set() { [3, 1, 3] }
                Set<Integer> explicit() { return [3, 1, 3] }
                Set<Integer> branch(boolean b) { if (b) return [3, 1]; [2] }
                Set<Integer> emptySet() { [] }
                SortedSet<Integer> sortedSet() { [3, 1, 2] }
                Deque<Integer> deque() { [3, 1, 2] }
                BlockingQueue<Integer> blockingQueue() { [3, 1] }
                SortedMap<String, Integer> sortedMap() { [b: 2, a: 1] }
                SortedMap<String, Integer> emptySortedMap() { [:] }
                ConcurrentMap<String, Integer> concurrentMap() { [a: 1] }
                TreeMap<String, Integer> treeMap() { [b: 2, a: 1] }
                LinkedList<Integer> linkedList() { [3, 1] }
                Vector<Integer> vector() { [5] }
                java.awt.Point point() { [1, 2] }
            }
            def c = new C()
            assert c.set() instanceof LinkedHashSet && c.set() as List == [3, 1]
            assert c.explicit() == [3, 1] as Set
            assert c.branch(true) == [3, 1] as Set && c.branch(false) == [2] as Set
            assert c.emptySet() instanceof LinkedHashSet && c.emptySet().isEmpty()
            assert c.sortedSet() instanceof TreeSet && c.sortedSet().first() == 1
            assert c.deque() instanceof LinkedList && c.deque().peekLast() == 2
            assert c.blockingQueue() instanceof LinkedBlockingQueue
            assert c.sortedMap() instanceof TreeMap && c.sortedMap().firstKey() == 'a'
            assert c.emptySortedMap() instanceof TreeMap && c.emptySortedMap().isEmpty()
            assert c.concurrentMap() instanceof ConcurrentHashMap
            assert c.treeMap().firstKey() == 'a'
            assert c.linkedList() == [3, 1]
            assert c.vector() == [5]
            assert c.point().x == 1 && c.point().y == 2
        """
    }

    @ParameterizedTest
    @ValueSource(strings = ['TypeChecked', 'CompileStatic'])
    void testReturnLiteralElementTypeIsChecked(String mode) {
        def err = shouldFail """
            @groovy.transform.$mode
            SortedSet<String> m() { [3, 1] }
        """
        assert err.message.contains('Incompatible generic argument types')
    }

    @ParameterizedTest
    @ValueSource(strings = ['TypeChecked', 'CompileStatic'])
    void testReturnLiteralConstructorMustMatch(String mode) {
        def err = shouldFail """
            @groovy.transform.$mode
            java.awt.Point m() { [1] }
        """
        assert err.message.contains('Cannot find matching constructor java.awt.Point(int)')
    }

    @ParameterizedTest
    @ValueSource(strings = ['TypeChecked', 'CompileStatic'])
    void testReturnNonLiteralIsStillChecked(String mode) {
        def err = shouldFail """
            @groovy.transform.$mode
            Set<Integer> m(List<Integer> list) { list }
        """
        assert err.message.contains('Cannot return value of type java.util.List<java.lang.Integer> for method returning java.util.Set<java.lang.Integer>')
    }

    @ParameterizedTest
    @ValueSource(strings = ['TypeChecked', 'CompileStatic'])
    void testClosureReturnIsUnchanged(String mode) {
        def err = shouldFail """
            @groovy.transform.$mode
            def m() {
                java.util.function.Supplier<Set<Integer>> s = () -> [3, 1]
            }
        """
        assert err.message.contains('Cannot return value of type java.util.ArrayList<java.lang.Integer> for lambda expecting java.util.Set<java.lang.Integer>')
    }
}
