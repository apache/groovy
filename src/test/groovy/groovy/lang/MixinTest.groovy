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

import groovy.transform.CompileStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.util.concurrent.locks.ReentrantLock

import static groovy.test.GroovyAssert.assertScript
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

final class MixinTest {

    @BeforeEach
    @CompileStatic
    void setUp() {
        org.codehaus.groovy.reflection.ClassInfo.clearModifiedExpandos()
    }

    @AfterEach
    void tearDown() {
        ArrayList.metaClass = null
             List.metaClass = null
        ObjToTest.metaClass = null
    }

    @Test
    void testOneClass() {
        List.mixin ListExt
        ArrayList.mixin ArrayListExt
        assertEquals 1, [0, 1].swap()[0]
        assertEquals 0, [0, 1].swap()[1]
        assertEquals 0, [0, 1].swap().unswap()[0]
        assertEquals 1, [0, 1].swap().unswap()[1]
    }

    @Test
    void testWithList() {
        ArrayList.mixin ArrayListExt, ListExt
        assertEquals 1, [0, 1].swap()[0]
        assertEquals 0, [0, 1].swap()[1]
        assertEquals 0, [0, 1].swap().unswap()[0]
        assertEquals 1, [0, 1].swap().unswap()[1]
    }

    @Test
    void testCombined() {
        ArrayList.mixin Combined
        assertEquals 1, [0, 1].swap()[0]
        assertEquals 0, [0, 1].swap()[1]
        assertEquals 0, [0, 1].swap().unswap()[0]
        assertEquals 1, [0, 1].swap().unswap()[1]
    }

    @Test
    void testWithEmc() {
        ArrayList.metaClass.unswap = {
            [delegate[1], delegate[0]]
        }
        ArrayList.mixin ArrayListExt
        assertEquals 1, [0, 1].swap()[0]
        assertEquals 0, [0, 1].swap()[1]
        assertEquals 0, [0, 1].swap().unswap()[0]
        assertEquals 1, [0, 1].swap().unswap()[1]
    }

    @Test
    void testGroovyObject() {
        def obj = new ObjToTest()
        assertEquals 'original', obj.value
        obj.metaClass.mixin ObjToTestCategory
        assertEquals 'changed by category', obj.value
        assertEquals 'original', new ObjToTest().value
    }

    @Test
    void testGroovyObjectWithEmc() {
        ObjToTest.metaClass.getValue = {->
            'emc changed'
        }
        ObjToTest obj = new ObjToTest()
        assertEquals 'emc changed', obj.getValue()
        obj.metaClass.mixin ObjToTestCategory
        assertEquals 'changed by category', obj.value
        assertEquals 'emc changed', new ObjToTest().value
    }

    @Test
    void testFlatten() {
        Object.metaClass.mixin DeepFlattenToCategory
        assertEquals([8, 9, 3, 2, 1, 4], [[8, 9] as Object[], [3, 2, [2: 1, 3: 4]], [2, 3]].flattenTo() as List)

        def x = [-2, -2, -3, -3]
        x.metaClass.mixin NoFlattenArrayListCategory
        assertEquals([x, 8, 9, 3, 2, 1, 4], [x, [8, 9] as Object[], [3, 2, [2: 1, 3: 4]], [2, 3]].flattenTo() as List)

        x.metaClass = null
        x.metaClass.flattenTo = {Set set -> set << delegate }
        assertEquals([x, 8, 9, 3, 2, 1, 4], [x, [8, 9] as Object[], [3, 2, [2: 1, 3: 4]], [2, 3]].flattenTo() as List)

        x.metaClass = null
        Object.metaClass.flattenTo(ArrayList) {->
            LinkedHashSet set = new LinkedHashSet()
            delegate.flattenTo(set)
            return set
        }
        Object.metaClass.flattenTo(ArrayList) {Set set ->
            set << 'oops'
            return Collection.metaClass.invokeMethod(delegate, 'flattenTo', set)
        }
        ArrayList.metaClass = null
        assertEquals(['oops', -2, -3, 8, 9, 3, 2, 1, 4], [x, [8, 9] as Object[], [3, 2, [2: 1, 3: 4]], [2, 3]].flattenTo() as List)

        ArrayList.metaClass = null
        Object.metaClass {
            flattenTo(ArrayList) {->
                LinkedHashSet set = new LinkedHashSet()
                delegate.flattenTo(set)
                return set
            }

            flattenTo(ArrayList) {Set set ->
                set << 'oopsssss'
                return Collection.metaClass.invokeMethod(delegate, 'flattenTo', set)
            }

            asList {->
                delegate as List
            }
        }
        assertEquals(['oopsssss', -2, -3, 8, 9, 3, 2, 1, 4], [x, [8, 9] as Object[], [3, 2, [2: 1, 3: 4]], [2, 3]].flattenTo().asList())

        ArrayList.metaClass = null
        Object.metaClass {
            define(ArrayList) {
                flattenTo {->
                    LinkedHashSet set = new LinkedHashSet()
                    delegate.flattenTo(set)
                    return set
                }

                flattenTo {Set set ->
                    set << 'ssoops'
                    return Collection.metaClass.invokeMethod(delegate, 'flattenTo', set)
                }
            }

            asList {->
                delegate as List
            }
        }
        assertEquals(['ssoops', -2, -3, 8, 9, 3, 2, 1, 4], [x, [8, 9] as Object[], [3, 2, [2: 1, 3: 4]], [2, 3]].flattenTo().asList())

        Object.metaClass = null
    }

    @Test
    void testMixingLockable() {
        Object.metaClass.mixin ReentrantLock
        def name = 'abcdef'
        name.lock()
        try {
            assertTrue name.isLocked()
        }
        finally {
            name.unlock()
        }
        Object.metaClass = null
    }

    @Test
    void testConcurrentQueue() {
        ReentrantLock.metaClass {
            withLock {Closure operation ->
                lock()
                try {
                    operation()
                }
                finally {
                    unlock()
                }
            }
        }

        def queue = new ConcurrentQueue()
        queue.put 1
        queue.put 2
        queue.put 3
        assertEquals 1, queue.get()
        assertEquals 2, queue.get()
        assertEquals 3, queue.get()

        ReentrantLock.metaClass = null
    }

    @Test
    void testDynamicConcurrentQueue() {
        ReentrantLock.metaClass {
            withLock {Closure operation ->
                lock()
                try {
                    operation()
                }
                finally {
                    unlock()
                }
            }
        }

        def queue = new Object()
        queue.metaClass {
            mixin LinkedList, ReentrantLock

            get {->
                withLock {
                    removeFirst()
                }
            }

            put {obj ->
                withLock {
                    addLast(obj)
                }
            }
        }

        queue.put 1
        queue.put 2
        queue.put 3
        assertEquals 1, queue.get()
        assertEquals 2, queue.get()
        assertEquals 3, queue.get()

        queue.metaClass {
            iterator {->
                mixedIn[LinkedList].iterator()
            }

            duplicateEachElement {
                withLock {
                    LinkedList newList = new LinkedList()
                    mixedIn[LinkedList].each {
                        newList << it
                        newList << it
                    }
                    mixedIn[LinkedList] = newList
                }
            }
        }

        queue.put 1
        queue.put 2
        queue.put 3
        queue.duplicateEachElement()
        assertEquals 1, queue.get()
        assertEquals 1, queue.get()
        assertEquals 2, queue.get()
        assertEquals 2, queue.get()
        assertEquals 3, queue.get()
        assertEquals 3, queue.get()

        ReentrantLock.metaClass = null
    }

    @Test
    void testNoDupCollection() {
        def list = new Object()
        list.metaClass {
            mixin NoDuplicateCollection, LinkedList

            find = {Closure check ->
                mixedIn[LinkedList].find(check)
            }
        }

        list.put 1
        list.put 1
        list.put 2
        list.put 2
        list.put 3
        list.put 3

        assertEquals(3, list.size())
        assertEquals 1, list[0]
        assertEquals 2, list[1]
        assertEquals 3, list[2]
    }

    @Test
    void testList() {
        def u = []
        u.metaClass {
            mixin HashSet

            leftShift {obj ->
                mixedIn[List] << obj
                mixedIn[Set] << obj
            }
        }

        u << 1
        u << 2
        u << 1
        u << 2
        u << 1
        u << 2

        assertEquals 6, u.size()
        assertEquals 6, ((Collection) u).size()
        assertEquals 6, ((List) u).size()
        assertEquals 2, ((Set) u).size()
    }

    @Test
    void testWPM() {
        new WPM_B().foo()

        WPM_C.metaClass { mixin WPM_B }

        def c = new WPM_C()
        c.foo()
        c.foobar()
    }

    @Test
    void testStackOverflow() {
        Overflow_B.metaClass {
            mixin Overflow_A

            foo = {->
                println 'New foo ' + receive('')
            }
        }

        final Overflow_A a = new Overflow_A()
        a.foo()

        final Overflow_B b = new Overflow_B()
        b.foo()
    }

    // GROOVY-3474
    @Test
    void testStackOverflowErrorWithMixinsAndClosure() {
        assertScript '''
            class Groovy3474A {
                int counter = 1
                protected final foo() {
                    bar { counter }
                }
                private final String bar(Closure code) {
                    return "Bar " + code()
                }
            }

            class Groovy3474B extends Groovy3474A {}

            class Groovy3474C {}
            Groovy3474C.metaClass { mixin Groovy3474B }
            def c = new Groovy3474C()
            assert c.foo() == 'Bar 1'
            println "testStackOverflowErrorWithMixinsAndClosure() Done"
        '''
    }

    void testMixinWithVarargs() {
        assertScript '''
            class Dsl {
                static novarargs(java.util.List s) { "novarargs" + s.size() }
                static plainVarargs(Object... s) { "plainVarargs" + s.size() }
                static mixedVarargs(int foo, String... s) { "mixedVarargs" + s.size() }
            }
            this.metaClass.mixin(Dsl)
            assert novarargs(["a", "b"]) == "novarargs2"
            assert plainVarargs("a", "b", 35) == "plainVarargs3"
            assert mixedVarargs(3, "a", "b", "c", "d") == "mixedVarargs4"
        '''
    }

    //--------------------------------------------------------------------------

    static class ArrayListExt {
        static def swap(ArrayList self) {
            [self[1], self[0]]
        }
    }

    static class ListExt {
        static def unswap(List self) {
            [self[1], self[0]]
        }
    }

    static class Combined {
        static def swap(ArrayList self) {
            [self[1], self[0]]
        }

        static def unswap(List self) {
            [self[1], self[0]]
        }
    }

    static class ObjToTest {
        def getValue() {
            'original'
        }
    }

    static class ObjToTestCategory {
        static getValue(ObjToTest self) {
            'changed by category'
        }
    }

    static class DeepFlattenToCategory {
        static Set flattenTo(element) {
            LinkedHashSet set = new LinkedHashSet()
            element.flattenTo(set)
            return set
        }

        // Object - put to result set
        static void flattenTo(element, Set addTo) {
            addTo << element
        }

        // Collection - flatten each element
        static void flattenTo(Collection elements, Set addTo) {
            elements.each {element ->
                element.flattenTo(addTo)
            }
        }

        // Map - flatten each value
        static void flattenTo(Map elements, Set addTo) {
            elements.values().flattenTo(addTo)
        }

        // Array - flatten each element
        static void flattenTo(Object[] elements, Set addTo) {
            elements.each {element ->
                element.flattenTo(addTo)
            }
        }
    }

    static class NoFlattenArrayListCategory {
        // Object - put to result set

        static void flattenTo(ArrayList element, Set addTo) {
            addTo << element
        }
    }

    static class ConcurrentQueue {
        static {
            ConcurrentQueue.metaClass.mixin LinkedList, ReentrantLock
        }

        def get() {
            withLock {
                removeFirst()
            }
        }

        void put(def obj) {
            withLock {
                addLast(obj)
            }
        }
    }

    static class NoDuplicateCollection {
        void put(def obj) {
            def clone = find {
                it == obj
            }

            if (!clone)
                add obj
        }
    }

    static class WPM_A {
        final foo() {
            bar()
        }

        private final String bar() {
            return 'Bar'
        }
    }

    static class WPM_B extends WPM_A {
        def foobar() {
            super.foo()
        }
    }

    static class WPM_C {
    }

    static class Overflow_A {
        public void foo() {
            println 'Original foo ' + receive('')
        }

        protected Object receive() {
            return 'Message'
        }

        protected Object receive(param) {
            receive() + param
        }
    }

    static class Overflow_B {
    }
}
