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
package groovy.concurrent

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class PoolTest {

    @Test
    void testFixedPoolSize() {
        assertScript '''
            import groovy.concurrent.Pool

            def pool = Pool.fixed(4)
            assert pool.poolSize == 4
            assert !pool.usesVirtualThreads()
            pool.close()
        '''
    }

    @Test
    void testCpuPoolSizedToProcessors() {
        assertScript '''
            import groovy.concurrent.Pool

            def pool = Pool.cpu()
            assert pool.poolSize == Runtime.runtime.availableProcessors()
            assert !pool.usesVirtualThreads()
            pool.close()
        '''
    }

    @Test
    void testPoolExecutesWork() {
        assertScript '''
            import groovy.concurrent.Pool
            import java.util.concurrent.CountDownLatch

            def pool = Pool.fixed(2)
            def latch = new CountDownLatch(2)
            pool.execute { latch.countDown() }
            pool.execute { latch.countDown() }
            assert latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
            pool.close()
        '''
    }

    @Test
    void testFixedPoolRejectsInvalidSize() {
        shouldFail(IllegalArgumentException, '''
            import groovy.concurrent.Pool
            Pool.fixed(0)
        ''')
    }

    @Test
    void testPoolWorksAsAsyncScopeExecutor() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import groovy.concurrent.Pool

            def pool = Pool.fixed(2)
            def result = AsyncScope.withScope(pool) { scope ->
                def a = scope.async { 10 }
                def b = scope.async { 20 }
                [await(a), await(b)]
            }
            assert result == [10, 20]
            pool.close()
        '''
    }
}
