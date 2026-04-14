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

final class ConcurrentConfigTest {

    @Test
    void testDefaultParallelismMatchesProcessorsPlusOne() {
        assertScript '''
            import groovy.concurrent.ConcurrentConfig

            def p = ConcurrentConfig.defaultParallelism
            assert p == Runtime.runtime.availableProcessors() + 1
        '''
    }

    @Test
    void testProgrammaticParallelismOverride() {
        assertScript '''
            import groovy.concurrent.ConcurrentConfig

            ConcurrentConfig.defaultParallelism = 42
            try {
                assert ConcurrentConfig.defaultParallelism == 42
            } finally {
                ConcurrentConfig.defaultParallelism = 0  // reset
            }
        '''
    }

    @Test
    void testDefaultPoolIsNonNull() {
        assertScript '''
            import groovy.concurrent.ConcurrentConfig
            import groovy.concurrent.Pool

            def pool = ConcurrentConfig.defaultPool
            assert pool != null
            assert pool instanceof Pool
        '''
    }
}
