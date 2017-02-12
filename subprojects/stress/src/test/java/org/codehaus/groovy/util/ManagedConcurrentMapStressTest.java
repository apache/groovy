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
package org.codehaus.groovy.util;

import org.apache.groovy.stress.util.GCUtils;
import org.apache.groovy.stress.util.ThreadUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ManagedConcurrentMapStressTest {

    static final int ENTRY_COUNT = 10371;

    static final ReferenceBundle bundle = ReferenceBundle.getWeakBundle();

    @Test
    public void testMapRemovesCollectedReferences() throws Exception {
        ManagedConcurrentMap<Object, String> map = new ManagedConcurrentMap<Object, String>(bundle);

        // Keep a hardref so we can test get later
        List<Object> keyList = populate(map);
        assertEquals(ENTRY_COUNT, map.size());

        // Make sure we still have our entries, sample a few
        Object key1337 = keyList.remove(1337);
        assertEquals("value1337", map.get(key1337));

        Object key77 = keyList.remove(77);
        assertEquals("value77", map.get(key77));

        key1337 = null;
        key77 = null;

        GCUtils.gc();
        assertEquals(ENTRY_COUNT - 2, map.size());
        for (Object o : map.values()) {
            if (o instanceof AbstractConcurrentMapBase.Entry<?>) {
                @SuppressWarnings("unchecked")
                AbstractConcurrentMapBase.Entry<String> e = (AbstractConcurrentMapBase.Entry)o;
                if ("value77".equals(e.getValue()) || "value1337".equals(e.getValue())) {
                    fail("Entries not removed from map");
                }
            } else {
                fail("No Entry found");
            }
        }

        // Clear all refs and gc()
        keyList.clear();
        GCUtils.gc();

        // Add an entries to force ReferenceManager.removeStaleEntries
        map.put(new Object(), "last");
        assertEquals("Map removed weak entries", 1, map.size());
    }

    /**
     * This tests for deadlock which can happen if more than one thread is allowed
     * to process entries from the same RefQ. We run multiple iterations because it
     * wont always be detected one run.
     *
     * @throws Exception
     */
    @Test
    public void testMultipleThreadsPutWhileRemovingRefs() throws Exception {
        for (int i = 0; i < 10; i++) {
            ManagedConcurrentMap<Object, String> map = new ManagedConcurrentMap<Object, String>(bundle);
            multipleThreadsPutWhileRemovingRefs(map);
        }
    }

    private void multipleThreadsPutWhileRemovingRefs(final ManagedConcurrentMap<Object, String> map) throws Exception {
        List<Object> keyList1 = populate(map);
        List<Object> keyList2 = populate(map);
        assertEquals(keyList1.size() + keyList2.size(), map.size());

        // Place some values on the ReferenceQueue
        keyList1.clear();
        GCUtils.gc();

        final int threadCount = 16;
        final CyclicBarrier barrier = new CyclicBarrier(threadCount + 1);
        final Object[] threadKeys = new Object[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Object k = new Object();
                    threadKeys[idx] = k;
                    String v = "thread-" + idx;
                    ThreadUtils.await(barrier);
                    map.put(k, v);
                    ThreadUtils.await(barrier);
                }
            });
            t.setDaemon(true);
            t.start();
        }
        barrier.await(); // start threads
        barrier.await(30L, TimeUnit.SECONDS); // wait for them to complete
        assertEquals(keyList2.size() + threadCount, map.size());
    }

    private List<Object> populate(ManagedConcurrentMap<Object, String> map) {
        List<Object> elements = new ArrayList<Object>(ENTRY_COUNT);
        for (int i = 0; i < ENTRY_COUNT; i++) {
            Object key = new Object();
            elements.add(key);
            map.put(key, "value" + i);
        }
        return elements;
    }
}
