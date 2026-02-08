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
package org.codehaus.groovy.runtime

import groovy.test.GroovyTestCase

import java.util.concurrent.*

import static org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport.createSimilarCollection
import static org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport.createSimilarMap

class DefaultGroovyMethodsSupportTest extends GroovyTestCase {
    void testCreateSimilarCollectionForSets() {
        def comparator = [
                compare: { a, b -> b <=> a },
                equals : { a, b -> a == b },
        ] as Comparator

        Set set1 = new ConcurrentSkipListSet(comparator)
        Set set2 = createSimilarCollection(set1)
        set2 << 1 << 2 << 3
        assert set2 instanceof ConcurrentSkipListSet
        assert 3 == set2.head()
        assert 1 == set2.last()

        set1 = new TreeSet(comparator)
        set2 = createSimilarCollection(set1)
        set2 << 1 << 2 << 3
        assert set2 instanceof TreeSet
        assert 3 == set2.head()
        assert 1 == set2.last()

        set1 = new CopyOnWriteArraySet()
        set2 = createSimilarCollection(set1)
        set2 << 1 << 2 << 3
        assert set2 instanceof CopyOnWriteArraySet
        assert 1 in set2
        assert 3 in set2

        set1 = new LinkedHashSet()
        set2 = createSimilarCollection(set1)
        set2 << 1 << 2 << 3
        assert set2 instanceof LinkedHashSet
        assert 1 == set2.head()
        assert 3 == set2.last()

        set1 = new HashSet()
        set2 = createSimilarCollection(set1)
        set2 << 1 << 2 << 3
        assert set2 instanceof LinkedHashSet // Not HashSet
        assert 1 == set2.head()
        assert 3 == set2.last()
    }

    void testCreateSimilarCollectionForLists() {
        [
                CopyOnWriteArrayList.class,
                LinkedList.class,
                Stack.class,
                Vector.class,
                ArrayList.class,
        ].each { testCase ->
            List list = createSimilarCollection(testCase.newInstance() as Collection)
            list << 1 << 2 << 3
            assert testCase.is(list.getClass())
            assert 1 == list.head()
            assert 3 == list.last()
        }
    }

    void testCreateSimilarCollectionForQueues() {
        [
                LinkedBlockingDeque.class,
                LinkedList.class,
                ArrayDeque.class,
                ArrayBlockingQueue.class,
                ConcurrentLinkedQueue.class,
                DelayQueue.class,
                LinkedBlockingQueue.class,
                PriorityBlockingQueue.class,
                PriorityQueue.class,
                SynchronousQueue.class,
        ].each { testCase ->
            Queue queue
            if (testCase == ArrayBlockingQueue.class) {
                queue = createSimilarCollection(new ArrayBlockingQueue(11))
            } else if (testCase == PriorityQueue.class) {
                def comparator = [
                        compare: { a, b -> b <=> a },
                        equals : { a, b -> a == b },
                ] as Comparator
                queue = createSimilarCollection(new PriorityQueue(13, comparator))
                assert comparator == ((PriorityQueue) queue).comparator()
            } else {
                queue = createSimilarCollection(testCase.newInstance() as Collection)
            }

            assert testCase == queue.getClass()

            if (queue instanceof PriorityQueue) {
                queue << 1 << 2 << 3
                assert 3 == queue.head()
            } else if (queue instanceof DelayQueue) {
                [1, 2, 3].each { int i ->
                    queue << ([
                            getDelay : { TimeUnit unit -> i as long },
                            compareTo: { Delayed o -> i <=> o.getDelay(null) },
                    ] as Delayed)
                }
                assert 1 == (queue.head() as Delayed).getDelay(null)
                assert 3 == (queue.last() as Delayed).getDelay(null)
            } else if (queue instanceof SynchronousQueue) {
                // Do not do a add test
            } else {
                queue << 1 << 2 << 3
                assert 1 == queue.head()
                assert 3 == queue.last()
            }
        }
    }

    void testCreateSimilarMap() {
        def comparator = [
                compare: { a, b -> b <=> a },
                equals : { a, b -> a == b },
        ] as Comparator
        [
                [ConcurrentSkipListMap.class, new ConcurrentSkipListMap(comparator)],
                [TreeMap.class, new TreeMap(comparator)],
        ].each { testCase ->
            Map map = createSimilarMap(testCase[1] as Map)
            map[1] = 2
            map[3] = 4
            assert testCase[0] == map.getClass()
            assert comparator == ((SortedMap) map).comparator()
            assert 2 == map[1]
        }

        [
                ConcurrentHashMap.class,
                Properties.class,
                Hashtable.class,
                IdentityHashMap.class,
                WeakHashMap.class,
                LinkedHashMap.class,
                HashMap.class,
        ].each { testCase ->
            Map map = createSimilarMap(testCase.newInstance())
            map[1] = 2
            map[3] = 4
            assert testCase == map.getClass() || (HashMap.class == testCase && LinkedHashMap.class == map.getClass())
            assert 2 == map[1]
        }
    }
}
