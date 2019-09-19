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

import groovy.test.GroovyTestCase

/**
 * A little performance test
 */
class BenchmarkBug extends GroovyTestCase {
    
    void testPerformance() {
        def start = System.currentTimeMillis()

        def total = 0
        def size = 10000
        for (i in 0..size) {
            total = total + callSomeMethod("hello", total)
        }

        def end = System.currentTimeMillis()

        def time = end - start

        println "Performed ${size} iterations in ${time / 1000} seconds which is ${time / size} ms per iteration"

        // TODO: parser bug
        // assert total == size * 10 + 10
        assert total == 100010
    }
    
    def callSomeMethod(text, total) {
        return 10
    }
}