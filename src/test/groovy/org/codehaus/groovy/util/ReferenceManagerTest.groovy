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

import java.lang.ref.ReferenceQueue

class ReferenceManagerTest extends GroovyTestCase {

    int finalizeCounter
    TestReference<Object> testReference
    TestQueue<Object> queue
    ReferenceManager callback

    void setUp() {
        finalizeCounter = 0
        testReference = new TestReference<Object>(new Object())
        queue = new TestQueue<Object>()
        callback = ReferenceManager.createCallBackedManager(queue)
    }

    void testCallbackManagerRemovesFromQueueAfterCreation() {
        3.times {
            queue.add(testReference)
        }
        callback.afterReferenceCreation(testReference)
        assert queue.size() == 0
        assert finalizeCounter == 3
    }

    void testCallbackManagerRemovesStalledEntriesFromQueue() {
        5.times {
            queue.add(testReference)
        }
        callback.removeStallEntries()
        assert queue.size() == 0
        assert finalizeCounter == 5
    }

    void testThresholdManagerRemovesFromQueueAfterCreationWhenThresholdIsReached() {
        ReferenceManager manager = ReferenceManager.createThresholdedIdlingManager(queue, callback, 2)
        2.times {
            queue.add(testReference)
            manager.afterReferenceCreation(testReference)
        }
        assert queue.size() == 2
        // Next call should delegate to callback manager and empty queue
        manager.afterReferenceCreation(testReference)
        assert queue.size() == 0
        assert finalizeCounter == 2

        queue.add(testReference)
        manager.afterReferenceCreation(testReference)
        assert queue.size() == 0
        assert finalizeCounter == 3
    }

    void testThresholdManagerRemovesStalledEntriesFromQueueWhenThresholdIsReached() {
        ReferenceManager manager = ReferenceManager.createThresholdedIdlingManager(queue, callback, 2)
        2.times {
            queue.add(testReference)
            manager.afterReferenceCreation(testReference)
        }
        // Threshold not crossed should be a no-op
        manager.removeStallEntries()
        assert queue.size() == 2

        // Next creation should trigger callback
        manager.afterReferenceCreation(testReference)
        assert queue.size() == 0

        // Next call should delegate to callback manager and empty queue
        queue.add(testReference)
        manager.removeStallEntries()
        assert queue.size() == 0
        assert finalizeCounter == 3

        // Make sure callback used for subsequent calls
        queue.add(testReference)
        manager.removeStallEntries()
        assert queue.size() == 0
        assert finalizeCounter == 4
    }

    void testCallbackManagerGuardsAgainstRecursiveQueueProcessing() {
        // Populate queue with enough references that call back into removeStallEntries
        // so that would normally generate a StackOverflowError
        10000.times {
            TestReference<Object> ref = new TestReference<Object>(new Object(), new Finalizable() {
                @Override
                void finalizeReference() {
                    callback.removeStallEntries()
                }
            })
            queue.add(ref)
        }
        callback.removeStallEntries()
        // Success if we made it this far with no StackOverflowError
        assert queue.size() == 0
    }

    private static class TestQueue<T> extends ReferenceQueue<T> {

        final List<T> entries = []

        void add(entry) {
            entries << entry
        }

        int size() {
            return entries.size()
        }

        @Override
        java.lang.ref.Reference<? extends T> poll() {
            return entries.isEmpty() ? null : entries.pop()
        }

        @Override
        java.lang.ref.Reference<? extends T> remove(long timeout) throws IllegalArgumentException, InterruptedException {
            return poll()
        }

        @Override
        java.lang.ref.Reference<? extends T> remove() throws InterruptedException {
            return poll()
        }
    }

    private class TestReference<T>
            extends java.lang.ref.SoftReference<T>
            implements Reference<T, Finalizable> {

        final Finalizable handler

        TestReference(T referent) {
            this(referent, null)
        }

        TestReference(T referent, Finalizable handler) {
            super(referent)
            if (handler != null) {
                this.handler = handler
            } else {
                this.handler = new Finalizable() {
                    @Override
                    void finalizeReference() {
                        ++finalizeCounter
                    }
                }
            }
        }

        @Override
        T get() {
            return super.get()
        }

        @Override
        void clear() {
            super.clear()
        }

        @Override
        Finalizable getHandler() {
            return handler
        }
    }
}
