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
package org.codehaus.groovy.util

import groovy.test.GroovyTestCase

class ManagedConcurrentLinkedQueueTest extends GroovyTestCase {

    def queue

    void setUp() {
        def manager = ReferenceManager.createIdlingManager(null)
        def bundle = new ReferenceBundle(manager, ReferenceType.HARD)
        queue = new ManagedConcurrentLinkedQueue(bundle)
    }

    void testElementAdd() {
        queue.add(1)
        def i = 0
        queue.each {
            assert it==1
            i++
        }
        assert i ==1
    }

    void testEmptylist() {
        assert queue.isEmpty()
    }

    void testRemoveinTheMiddle() {
        queue.add(1)
        queue.add(2)
        queue.add(3)
        queue.add(4)
        queue.add(5)
        def iter = queue.iterator()
        while (iter.hasNext()) {
            if (iter.next()==3) iter.remove()
        }
        def val = queue.inject(0){value, it-> value+it}
        assert val == 12
    }

    void testAddRemove() {
        10.times {
            queue.add(it)
            def iter = queue.iterator()
            while (iter.hasNext()) {
                if (iter.next()==it) iter.remove()
            }
        }
        assert queue.isEmpty()
    }

    void testIteratorThrowsNoSuchElementException() {
        shouldFail(NoSuchElementException) {
            queue.add(1)
            def iter = queue.iterator()
            assert iter.next() == 1
            iter.next()
        }
    }

    void testIteratorThrowsOnRemoveIfNextNotCalled() {
        shouldFail(IllegalStateException) {
            queue.add(1)
            def iter = queue.iterator()
            assert iter.hasNext()
            iter.remove()
        }
    }

}
