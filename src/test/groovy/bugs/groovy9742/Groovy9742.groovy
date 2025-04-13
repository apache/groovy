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
package groovy.bugs.groovy9742

import org.junit.jupiter.api.Test

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class Groovy9742 {
    @Test
    void testDeadLock() {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
        Future future = fixedThreadPool.submit((Callable<Class<?>>) () -> {
            DelegatingGroovyClassLoader ccl = new DelegatingGroovyClassLoader(Groovy9742.class.getClassLoader())
            Class<?> clz = ccl.loadClass("groovy.bugs.groovy9742.Foo")
            assert ccl.loadedCount == 2
            return clz
        })
        Class c = future.get(3000, TimeUnit.MILLISECONDS)
        assert c instanceof Class
        fixedThreadPool.shutdownNow()
    }

    @Test
    void testCustomGroovyClassLoader() {
        CustomGroovyClassLoader gcl = new CustomGroovyClassLoader()
        assert gcl.evaluate('1 + 1') == 2
    }
}
