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

import java.util.concurrent.atomic.AtomicInteger

class MemoizeAtLeastTest extends AbstractMemoizeTestCase {

    Closure buildMemoizeClosure(Closure cl) {
        cl.memoizeAtLeast(100)
    }

    void testZeroCache() {
        def flag = false
        Closure cl = {
            flag = true
            it * 2
        }
        Closure mem = cl.memoizeAtLeast(0)
        [1, 2, 3, 4, 5, 6].each { mem(it) }
        assert flag
    }

    void testMemoizeAtLeastConcurrently() {
        AtomicInteger cnt = new AtomicInteger(0)
        Closure cl = {
            cnt.incrementAndGet()
            it * 2
        }
        Closure mem = cl.memoizeAtLeast(3)
        [4, 5, 6, 4, 5, 6, 4, 5, 6].collect { num -> Thread.start { mem(num) } }*.join()

        assert 3 == cnt.get()
    }
}
