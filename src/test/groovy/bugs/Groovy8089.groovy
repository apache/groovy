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
 * GROOVY-8089: a closure stored in a static field was not callable
 * as a first-class function from another closure, while the same
 * call through a local variable worked.
 */
final class Groovy8089 {

    @Test
    void testFirstClassFunctionHeldInStaticField() {
        assertScript '''
            class Test {
                static def staticDo
                static main(args) {
                    def log = []
                    def normalDo = { log << 'normal'; it() }
                    staticDo     = { log << 'static'; it() }

                    def test = { log << 'TEST' }

                    def clo = { normalDo(test) }
                    clo()
                    clo = { staticDo(test) }
                    clo()
                    clo = { Test.staticDo(test) }
                    clo()

                    assert log == ['normal', 'TEST', 'static', 'TEST', 'static', 'TEST']
                }
            }
        '''
    }
}
