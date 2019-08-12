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
package groovy.bugs

import org.junit.Test

final class Groovy1759 {

    @Test
    void testInterception() {
        def benchmarkInterceptor = new BenchmarkInterceptor()
        def proxy = ProxyMetaClass.getInstance(A.class)
        proxy.setInterceptor(benchmarkInterceptor)
        proxy.use {
            def a = new A()
            a.a()
            a.b()
        }

        def actual = benchmarkInterceptor.statistic().collect { [it[0], it[1]] }
        def expected = [
            ['ctor', 1],
            ['a', 1],
            ['b', 2]
        ]
        assert expected == actual
    }

    static class A {
        void a() { b() }
        void b() {     }
    }
}
