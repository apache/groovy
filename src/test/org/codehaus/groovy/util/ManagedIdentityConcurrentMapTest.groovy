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


import org.junit.Test

class ManagedIdentityConcurrentMapTest {
    @Test
    void testRemovingEntriesFromMapAfterGC() {
        def m = new ManagedIdentityConcurrentMap<Object, String>(ReferenceBundle.getWeakBundle())
        def k1 = new Object()
        m.put(k1, "a")
        def k2 = new Object()
        m.put(k2, "b")
        def k3 = new Object()
        m.put(k3, "c")

        assert 3 == m.size()

        // the related entries should be removed after GC happens
        k1 = null
        k2 = null
        k3 = null

        // finalize via GC, which is hard to predicate though it will happen at last
        for (int i = 0; i < 20; i++) {
            System.gc()

            if (m.values().size() == 0) {
                break
            }

            Thread.sleep(100)
        }

        // finalize manually
        if (!m.isEmpty()) {
            m.internalMap.keySet().stream().forEach(e -> e.finalizeReference())
        }

        assert m.isEmpty()
    }
}
