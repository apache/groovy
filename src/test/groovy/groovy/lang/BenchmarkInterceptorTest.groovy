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
package groovy.lang

import groovy.test.GroovyTestCase

/**
 * Test for the BenchmarkInterceptor
 */
class BenchmarkInterceptorTest extends GroovyTestCase {

    Interceptor benchmarkInterceptor
    def proxy

    void setUp() {
        benchmarkInterceptor = new BenchmarkInterceptor()
        proxy = ProxyMetaClass.getInstance(Date)
        proxy.setInterceptor(benchmarkInterceptor)
    }

    void testSimpleInterception() {
        proxy.use {
            def x = new Date(0)
            x++
        }
        def stats = benchmarkInterceptor.statistic()
        assertEquals 2, stats.size()
        assert stats.find { it[0] == 'ctor' }
        assert stats.find { it[0] == 'next' }
        assert stats.every { it[1] == 1 }
        assert stats.every { it[2] < 200 }
    }
}
