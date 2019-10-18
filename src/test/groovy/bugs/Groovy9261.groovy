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

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class Groovy9261 {

    @Test
    void testInvalidResourceInARM() {
        def err = shouldFail '''\
            @groovy.transform.CompileStatic
            void test() {
                try (String str = '123') {
                }
            }
            test()
        '''

        assert err.message.contains('Resource[java.lang.String] in ARM should be of type AutoCloseable')
        assert err.message.contains('@ line 3, column 22.')
    }

    @Test
    void testInvalidResourceInARM2() {
        def err = shouldFail '''\
            @groovy.transform.CompileStatic
            void test() {
                try (str = '123') {
                }
            }
            test()
        '''

        assert err.message.contains('Resource[java.lang.String] in ARM should be of type AutoCloseable')
        assert err.message.contains('@ line 3, column 22.')
    }

    @Test
    void testInvalidResourceInARM3() {
        def err = shouldFail '''\
            @groovy.transform.CompileStatic
            void test() {
                try (def sr = new StringReader(''); str = '123') {
                }
            }
            test()
        '''

        assert err.message.contains('Resource[java.lang.String] in ARM should be of type AutoCloseable')
        assert err.message.contains('@ line 3, column 53.')
    }

    @Test
    void testInvalidResourceInEnhancedARM() {
        def err = shouldFail '''\
            @groovy.transform.CompileStatic
            void test() {
                String str = '123'
                try (str) {
                }
            }
            test()
        '''

        assert err.message.contains('Resource[java.lang.String] in ARM should be of type AutoCloseable')
        assert err.message.contains('@ line 4, column 22.')
    }
}
