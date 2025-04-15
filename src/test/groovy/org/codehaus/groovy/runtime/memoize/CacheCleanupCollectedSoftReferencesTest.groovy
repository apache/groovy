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
package org.codehaus.groovy.runtime.memoize

import groovy.test.GroovyTestCase
import org.junit.Ignore

@Ignore("does not run consistently on the build server")
class CacheCleanupCollectedSoftReferencesTest extends GroovyTestCase {

    void testCollectedCacheValuesAreEnqueued() {

        Closure cl = { 
            new Integer(it + 1) 
        }

        UnlimitedConcurrentCache cache = new UnlimitedConcurrentCache()
        Closure memoizedClosure = Memoize.buildSoftReferenceMemoizeFunction(0, cache, cl)

        assert cache.map.size() == 0
        memoizedClosure.call(1)
        assert cache.map.size() == 1

        forceSoftReferencesRecollection()

        def softReference = cache.get([1])
        checkSoftReferenceAreSoftlyReachable(softReference)
        checkCollectedSoftReferenceAreEnqueued(softReference)
    }

    void testCollectedCacheValuesAreRemovedFromCache() {

        Closure cl = { 
            new Integer(it + 1) 
        }

        UnlimitedConcurrentCache cache = new UnlimitedConcurrentCache()
        Closure memoizedClosure = Memoize.buildSoftReferenceMemoizeFunction(0, cache, cl)

        assert cache.map.size() == 0
        memoizedClosure.call(1)
        assert cache.map.size() == 1

        forceSoftReferencesRecollection()

        assert cache.map.size() == 1

        // As there is not a cleanup thread polling the ReferenceQueue, 
        // a call() invocation is needed to fire the cleaning up of null references.
        memoizedClosure.call(2)

        assert cache.map.size() == 1 : 'collected SoftReferences should be removed from cache'
    }

    private void checkSoftReferenceAreSoftlyReachable(softReference) {
        assert softReference.get() == null : 
            'cache values should be softly reachable and collected before an OOME'
    }

    private void checkCollectedSoftReferenceAreEnqueued(softReference) {
        assert softReference.enqueued : 'collected cache values should be enqueued'
    }

    private void forceSoftReferencesRecollection() {
        try {
            int maxMemory = Runtime.getRuntime().maxMemory()
            Object[] fillingMemory = new Object[maxMemory]
        } catch (Throwable e) {
            // Ignore OOME
        }
    }
}
