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

import groovy.lang.MetaClass;
import org.apache.groovy.stress.util.GCUtils;
import org.apache.groovy.stress.util.ThreadUtils;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ManagedConcurrentValueMapStressTest {

    static final int ENTRY_COUNT = 10371;

    static final ReferenceBundle bundle = ReferenceBundle.getWeakBundle();

    @Test
    public void testMapRemovesCollectedReferences() throws InterruptedException {
        ManagedConcurrentValueMap<String, Object> map = new ManagedConcurrentValueMap<String, Object>(bundle);

        // Keep a hardref so we can test get later
        List<Object> valueList = populate(map);

        // Make sure we still have our entries, sample a few
        Object value77 = map.get("key77");
        assertEquals(valueList.get(77), value77);

        Object value1337 = map.get("key1337");
        assertEquals(valueList.get(1337), value1337);

        // Clear hardrefs and gc()
        value77 = null;
        value1337 = null;
        valueList.clear();

        GCUtils.gc();

        // Add an entries to force ReferenceManager.removeStaleEntries
        map.put("keyLast", new Object());

        // No size() method, so let's just check a few keys we that should have been collected
        assertEquals(null, map.get("key77"));
        assertEquals(null, map.get("key1337"));
        assertEquals(null, map.get("key3559"));

        assertEquals(1, size(map));
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
            ManagedConcurrentValueMap<String, Object> map = new ManagedConcurrentValueMap<String, Object>(bundle);
            multipleThreadsPutWhileRemovingRefs(map);
        }
    }

    private void multipleThreadsPutWhileRemovingRefs(final ManagedConcurrentValueMap<String, Object> map) throws Exception {
        List<Object> valueList1 = populate(map);
        List<Object> valueList2 = populate(map);

        // Place some values on the ReferenceQueue
        valueList1.clear();
        GCUtils.gc();

        final int threadCount = 16;
        final CyclicBarrier barrier = new CyclicBarrier(threadCount + 1);
        final Object[] threadValues = new Object[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Object v = new Object();
                    threadValues[idx] = v;
                    String k = "thread-" + idx;
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
        assertEquals(ENTRY_COUNT + threadValues.length, size(map));
    }

    private List<Object> populate(ManagedConcurrentValueMap<String, Object> map) {
        List<Object> elements = new ArrayList<Object>(ENTRY_COUNT);
        for (int i = 0; i < ENTRY_COUNT; i++) {
            Object val = new Object();
            elements.add(val);
            map.put("key" + i, val);
        }
        return elements;
    }

    private static int size(ManagedConcurrentValueMap<String, Object> map) {
        MetaClass metaClass = InvokerHelper.getMetaClass(map);
        ConcurrentHashMap<String, Object> internalMap = (ConcurrentHashMap<String, Object>)metaClass.getProperty(map, "internalMap");
        return internalMap.size();
    }
}
