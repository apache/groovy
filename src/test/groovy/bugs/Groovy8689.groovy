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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-8689: a newline between a constructor call and a closure
 * should be parsed as {@code new A().call({...})}, not as a method
 * call whose name is the constructor result.
 */
final class Groovy8689 {

    @Test
    void testNewlineBetweenConstructorCallAndClosureIsCall() {
        assertScript '''
            class A {
                def call(c) { 'called' }
            }
            def r = new A()
            {}
            assert r == 'called'
        '''
    }
}
